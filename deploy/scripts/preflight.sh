#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)
DEPLOY_HOME=${DEPLOY_HOME:-/srv/auction}
ENV_FILE=${APP_ENV_FILE:-$DEPLOY_HOME/.env}
COMPOSE_FILE=${COMPOSE_FILE:-$REPO_ROOT/deploy/compose.prod.yml}

fail() {
  printf 'ERROR: %s\n' "$1" >&2
  exit 1
}

read_env_value() {
  local key=$1
  awk -F= -v wanted="$key" '
    $1 == wanted {
      value=substr($0, index($0, "=") + 1)
      sub(/\r$/, "", value)
      print value
      exit
    }
  ' "$ENV_FILE"
}

for command_name in docker awk curl df flock sha256sum; do
  command -v "$command_name" >/dev/null 2>&1 || fail "$command_name is not installed"
done

docker compose version >/dev/null 2>&1 || fail "Docker Compose v2 is required"
[ -f "$ENV_FILE" ] || fail "Production env file not found: $ENV_FILE"
[ -f "$COMPOSE_FILE" ] || fail "Compose file not found: $COMPOSE_FILE"

postgres_volume=$(read_env_value POSTGRES_VOLUME_NAME)
[ -n "$postgres_volume" ] || fail "POSTGRES_VOLUME_NAME is missing from $ENV_FILE"
docker volume inspect "$postgres_volume" >/dev/null 2>&1 \
  || fail "PostgreSQL volume does not exist: $postgres_volume"

legacy_container=$(read_env_value POSTGRES_CONTAINER_NAME)
legacy_container=${legacy_container:-postgres_auction}
if docker container inspect "$legacy_container" >/dev/null 2>&1; then
  mount_record=$(docker container inspect --format \
    '{{range .Mounts}}{{if eq .Destination "/var/lib/postgresql/data"}}{{printf "%s|%s|%s" .Type .Name .Source}}{{end}}{{end}}' \
    "$legacy_container")
  [ -n "$mount_record" ] || fail "$legacy_container has no PostgreSQL data mount"

  mount_type=${mount_record%%|*}
  mount_rest=${mount_record#*|}
  mount_name=${mount_rest%%|*}
  if [ "$mount_type" != "volume" ]; then
    fail "$legacy_container uses a bind mount. Adapt compose.prod.yml before cutover; do not continue automatically."
  fi
  [ "$mount_name" = "$postgres_volume" ] \
    || fail "Configured volume '$postgres_volume' differs from live volume '$mount_name'"

  project_label=$(docker container inspect --format \
    '{{index .Config.Labels "com.docker.compose.project"}}' "$legacy_container" 2>/dev/null || true)
  running=$(docker container inspect --format '{{.State.Running}}' "$legacy_container")
  if [ "$running" = "true" ] && [ "$project_label" != "auctionplatform" ]; then
    fail "Legacy PostgreSQL container '$legacy_container' is still running. Back it up and perform the documented one-time adoption before automated CD."
  fi
fi

available_kb=$(df -Pk "$DEPLOY_HOME" | awk 'NR == 2 {print $4}')
[ "${available_kb:-0}" -ge 2097152 ] || fail "At least 2 GiB free disk space is required"

certificate_dir=/etc/letsencrypt/live/api.auctionplatform.tinhlelaptrinh.id.vn
[ -f "$certificate_dir/fullchain.pem" ] || fail "TLS certificate not found at $certificate_dir/fullchain.pem"
[ -f "$certificate_dir/privkey.pem" ] || fail "TLS private key not found at $certificate_dir/privkey.pem"

IMAGE_TAG=preflight docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" config --quiet

printf 'Preflight passed. PostgreSQL volume: %s\n' "$postgres_volume"
