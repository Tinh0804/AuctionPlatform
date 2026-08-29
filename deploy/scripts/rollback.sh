#!/usr/bin/env bash
# rollback.sh - Safely roll back the backend image to a previous version
set -Eeuo pipefail

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

if [ "${DEPLOY_LOCK_HELD:-false}" != "true" ]; then
  acquire_deploy_lock "$LOCK_FILE"
fi

backend_repository=$(resolve_backend_repository)
current_before=${ROLLBACK_CURRENT_TAG:-}
if [ -z "$current_before" ]; then
  current_before=$(read_release_tag "$CURRENT_TAG_FILE" 2>/dev/null || true)
fi
if [ -z "$current_before" ]; then
  current_before=$(running_backend_tag "$backend_repository" 2>/dev/null || true)
fi

target_tag=${1:-}
if [ -z "$target_tag" ]; then
  target_tag=$(read_release_tag "$PREVIOUS_TAG_FILE" 2>/dev/null || true)
fi

[ -n "$target_tag" ] \
  || fail "No target rollback image tag specified and no previous-image-tag found."

flyway_enabled=$(read_env_value FLYWAY_ENABLED)
flyway_enabled=$(printf '%s' "${flyway_enabled:-false}" | tr '[:upper:]' '[:lower:]')

if [ "$flyway_enabled" = "true" ]; then
  log_warn "Flyway migration is ENABLED in $ENV_FILE."
  log_warn "Rolling back application image may lead to schema mismatch if migrations have been applied."
  if [ "${FORCE_ROLLBACK:-false}" != "true" ]; then
    fail "Automatic rollback halted for safety. To proceed anyway, run with FORCE_ROLLBACK=true"
  fi
fi

log_info "Initiating rollback of backend service to image tag: $target_tag"

IMAGE_TAG="$target_tag" compose pull backend || log_warn "Pull failed, attempting to use local cached image..."
IMAGE_TAG="$target_tag" compose up -d --no-deps backend

if ! IMAGE_TAG="$target_tag" wait_for_health backend 36; then
  compose logs --tail 100 backend >&2 || true
  fail "Rollback failed: backend did not report healthy status on tag $target_tag"
fi

IMAGE_TAG="$target_tag" compose up -d --no-deps nginx
IMAGE_TAG="$target_tag" wait_for_health nginx 20 || log_warn "Nginx health check reported warning during rollback."

record_successful_release "$target_tag" "$current_before" "$CURRENT_TAG_FILE" "$PREVIOUS_TAG_FILE"

if ! DEPLOY_LOCK_HELD=true \
  DEPLOY_HOME="$DEPLOY_HOME" \
  APP_ENV_FILE="$ENV_FILE" \
  COMPOSE_FILE="$COMPOSE_FILE" \
  BACKEND_REPOSITORY="$backend_repository" \
  bash "$SCRIPT_DIR/cleanup-backend-images.sh"; then
  log_warn "Rollback succeeded, but old backend image cleanup failed."
fi

log_success "Successfully rolled back backend to tag: $target_tag"
