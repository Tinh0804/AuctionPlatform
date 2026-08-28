#!/usr/bin/env bash
set -Eeuo pipefail

[ "$#" -eq 1 ] || { printf 'Usage: bash deploy.sh <image-tag>\n' >&2; exit 2; }

IMAGE_TAG=$1
export IMAGE_TAG

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)
DEPLOY_HOME=${DEPLOY_HOME:-/srv/auction}
ENV_FILE=${APP_ENV_FILE:-$DEPLOY_HOME/.env}
COMPOSE_FILE=${COMPOSE_FILE:-$REPO_ROOT/deploy/compose.prod.yml}
CURRENT_TAG_FILE=$DEPLOY_HOME/current-image-tag
LOCK_FILE=$DEPLOY_HOME/deploy.lock

mkdir -p "$DEPLOY_HOME"
exec 9>"$LOCK_FILE"
flock -n 9 || { printf 'Another deployment is already running.\n' >&2; exit 1; }

compose() {
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

wait_for_health() {
  local service=$1
  local attempts=${2:-30}
  local container_id status
  container_id=$(compose ps -q "$service")
  [ -n "$container_id" ] || return 1

  while [ "$attempts" -gt 0 ]; do
    status=$(docker container inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container_id")
    case "$status" in
      healthy|running) return 0 ;;
      unhealthy|exited|dead) return 1 ;;
    esac
    attempts=$((attempts - 1))
    sleep 5
  done
  return 1
}

rollback_backend() {
  local previous_tag=$1
  local flyway_enabled
  flyway_enabled=$(awk -F= '$1 == "FLYWAY_ENABLED" {print tolower(substr($0, index($0, "=") + 1)); exit}' "$ENV_FILE")
  if [ "$flyway_enabled" = "true" ]; then
    printf 'Automatic image rollback skipped because Flyway was enabled. Manual schema compatibility review is required.\n' >&2
    return 1
  fi
  [ -n "$previous_tag" ] || return 1
  printf 'Rolling backend back to image tag %s\n' "$previous_tag" >&2
  IMAGE_TAG=$previous_tag compose up -d --no-deps backend
  IMAGE_TAG=$previous_tag wait_for_health backend 30
}

previous_tag=''
if [ -f "$CURRENT_TAG_FILE" ]; then
  previous_tag=$(head -n 1 "$CURRENT_TAG_FILE")
fi

bash "$SCRIPT_DIR/preflight.sh"
bash "$SCRIPT_DIR/backup-postgres.sh"

compose pull backend
compose up -d postgres redis
wait_for_health postgres 24 || { printf 'PostgreSQL did not become healthy.\n' >&2; exit 1; }
wait_for_health redis 24 || { printf 'Redis did not become healthy.\n' >&2; exit 1; }

compose up -d --no-deps backend
if ! wait_for_health backend 36; then
  compose logs --tail 150 backend >&2 || true
  rollback_backend "$previous_tag" || true
  exit 1
fi

compose up -d --no-deps nginx
if ! wait_for_health nginx 20; then
  compose logs --tail 100 nginx >&2 || true
  rollback_backend "$previous_tag" || true
  exit 1
fi

health_url=${HEALTHCHECK_URL:-https://api.auctionplatform.tinhlelaptrinh.id.vn/healthz}
curl --fail --silent --show-error --retry 8 --retry-delay 5 "$health_url" >/dev/null \
  || { rollback_backend "$previous_tag" || true; exit 1; }

printf '%s\n' "$IMAGE_TAG" > "$CURRENT_TAG_FILE"
chmod 600 "$CURRENT_TAG_FILE"
docker image prune -f >/dev/null

printf 'Deployment succeeded with image tag %s\n' "$IMAGE_TAG"
