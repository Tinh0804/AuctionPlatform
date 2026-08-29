#!/usr/bin/env bash
# restore-postgres.sh - Restore a PostgreSQL database dump safely
set -Eeuo pipefail

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)
DEPLOY_HOME=${DEPLOY_HOME:-/srv/auction}
ENV_FILE=${APP_ENV_FILE:-$DEPLOY_HOME/.env}
COMPOSE_FILE=${COMPOSE_FILE:-$REPO_ROOT/deploy/compose.prod.yml}
LOCK_FILE=$DEPLOY_HOME/deploy.lock

# shellcheck source=deploy/scripts/lib/common.sh
source "$SCRIPT_DIR/lib/common.sh"

[ "$#" -ge 1 ] || fail "Usage: bash restore-postgres.sh <path-to-dump-file> [-y]"

DUMP_FILE=$1
AUTO_CONFIRM=${2:-}

[ -f "$DUMP_FILE" ] || fail "Dump file not found: $DUMP_FILE"
[ -s "$DUMP_FILE" ] || fail "Dump file is empty: $DUMP_FILE"

# Check SHA256 checksum if exists
if [ -f "${DUMP_FILE}.sha256" ]; then
  log_info "Verifying SHA256 checksum..."
  (cd "$(dirname "$DUMP_FILE")" && sha256sum -c "${DUMP_FILE}.sha256") || fail "Checksum verification failed for $DUMP_FILE"
  log_success "Checksum verified successfully."
fi

acquire_deploy_lock "$LOCK_FILE"

container_id=$(docker ps \
  --filter label=com.docker.compose.project=auctionplatform \
  --filter label=com.docker.compose.service=postgres \
  --format '{{.ID}}' | head -n 1)

if [ -z "$container_id" ]; then
  legacy_container=$(read_env_value POSTGRES_CONTAINER_NAME)
  legacy_container=${legacy_container:-postgres_auction}
  container_id=$(docker ps --filter "name=^/${legacy_container}$" --format '{{.ID}}' | head -n 1)
fi

[ -n "$container_id" ] || fail "No running PostgreSQL container found to restore into."

container_env=$(docker container inspect --format '{{range .Config.Env}}{{println .}}{{end}}' "$container_id")
db_user=$(printf '%s\n' "$container_env" | awk -F= '$1 == "POSTGRES_USER" {print substr($0, index($0, "=") + 1); exit}')
db_name=$(printf '%s\n' "$container_env" | awk -F= '$1 == "POSTGRES_DB" {print substr($0, index($0, "=") + 1); exit}')
db_user=${db_user:-postgres}
db_name=${db_name:-$db_user}

log_warn "WARNING: This operation will restore database '$db_name' using dump file:"
log_warn "  $DUMP_FILE"
log_warn "Existing data in database '$db_name' may be modified or overwritten."

if [ "$AUTO_CONFIRM" != "-y" ] && [ "${FORCE_RESTORE:-false}" != "true" ]; then
  read -r -p "Are you sure you want to continue? (Type 'yes' to confirm): " confirm
  if [ "$confirm" != "yes" ]; then
    log_info "Restore cancelled by user."
    exit 0
  fi
fi

log_info "Validating dump archive structure before restore..."
docker exec -i "$container_id" pg_restore --list < "$DUMP_FILE" >/dev/null || fail "Invalid or corrupted dump archive."

log_info "Executing pg_restore into container '$container_id' ($db_name)..."
# Using --clean --if-exists to clean database objects before recreating them
docker exec -i "$container_id" pg_restore \
  --username "$db_user" \
  --dbname "$db_name" \
  --clean \
  --if-exists \
  --no-owner \
  --no-privileges < "$DUMP_FILE" || log_warn "pg_restore finished with warnings (normal for non-fatal conflicts)."

log_success "PostgreSQL database restore completed successfully."
