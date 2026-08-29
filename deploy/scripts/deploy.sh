#!/usr/bin/env bash
# deploy.sh - Main zero-downtime deployment orchestrator for GCP VM
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
PREVIOUS_TAG_FILE=$DEPLOY_HOME/previous-image-tag
LOCK_FILE=$DEPLOY_HOME/deploy.lock

# shellcheck source=deploy/scripts/lib/common.sh
source "$SCRIPT_DIR/lib/common.sh"

acquire_deploy_lock "$LOCK_FILE"

backend_repository=$(resolve_backend_repository)
current_before=$(read_release_tag "$CURRENT_TAG_FILE" 2>/dev/null || true)
if [ -z "$current_before" ]; then
  current_before=$(running_backend_tag "$backend_repository" 2>/dev/null || true)
fi

do_rollback() {
  if [ -n "$current_before" ]; then
    log_warn "Deployment step failed. Triggering automatic rollback to previous tag: $current_before"
    DEPLOY_LOCK_HELD=true \
      DEPLOY_HOME="$DEPLOY_HOME" \
      APP_ENV_FILE="$ENV_FILE" \
      COMPOSE_FILE="$COMPOSE_FILE" \
      BACKEND_REPOSITORY="$backend_repository" \
      ROLLBACK_CURRENT_TAG="$current_before" \
      bash "$SCRIPT_DIR/rollback.sh" "$current_before" || log_error "Rollback attempt failed."
  else
    log_warn "Deployment step failed. No previous tag available for automatic rollback."
  fi
}

log_info "Running preflight checks..."
bash "$SCRIPT_DIR/preflight.sh"

log_info "Creating verified pre-deployment PostgreSQL backup..."
bash "$SCRIPT_DIR/backup-postgres.sh"

log_info "Pulling backend image: $IMAGE_TAG..."
compose pull backend

log_info "Starting infrastructure services (PostgreSQL & Redis)..."
compose up -d postgres redis
wait_for_health postgres 24 || fail "PostgreSQL did not become healthy."
wait_for_health redis 24 || fail "Redis did not become healthy."

log_info "Starting new backend service with image tag: $IMAGE_TAG..."
compose up -d --no-deps backend
if ! wait_for_health backend 36; then
  compose logs --tail 150 backend >&2 || true
  do_rollback
  exit 1
fi

log_info "Updating and starting Nginx reverse proxy..."
compose up -d --no-deps nginx
if ! wait_for_health nginx 20; then
  compose logs --tail 100 nginx >&2 || true
  do_rollback
  exit 1
fi

health_url=${HEALTHCHECK_URL:-https://api.auctionplatform.tinhlelaptrinh.id.vn/healthz}
log_info "Verifying public HTTPS endpoint: $health_url..."
if ! curl --fail --silent --show-error --retry 8 --retry-delay 5 "$health_url" >/dev/null; then
  log_error "Public health check failed at $health_url"
  do_rollback
  exit 1
fi

record_successful_release "$IMAGE_TAG" "$current_before" "$CURRENT_TAG_FILE" "$PREVIOUS_TAG_FILE"

log_info "Keeping the current and previous successful backend images..."
if ! DEPLOY_LOCK_HELD=true \
  DEPLOY_HOME="$DEPLOY_HOME" \
  APP_ENV_FILE="$ENV_FILE" \
  COMPOSE_FILE="$COMPOSE_FILE" \
  BACKEND_REPOSITORY="$backend_repository" \
  bash "$SCRIPT_DIR/cleanup-backend-images.sh"; then
  log_warn "Deployment succeeded, but old backend image cleanup failed."
fi

log_success "Deployment succeeded with image tag: $IMAGE_TAG"
