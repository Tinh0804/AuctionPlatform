#!/usr/bin/env bash
set -Eeuo pipefail

DEPLOY_HOME=${DEPLOY_HOME:-/srv/auction}
ENV_FILE=${APP_ENV_FILE:-$DEPLOY_HOME/.env}
BACKUP_DIR=${BACKUP_DIR:-$DEPLOY_HOME/backups/postgres}

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

[ -f "$ENV_FILE" ] || fail "Production env file not found: $ENV_FILE"

container_id=$(docker ps \
  --filter label=com.docker.compose.project=auctionplatform \
  --filter label=com.docker.compose.service=postgres \
  --format '{{.ID}}' | head -n 1)

if [ -z "$container_id" ]; then
  legacy_container=$(read_env_value POSTGRES_CONTAINER_NAME)
  legacy_container=${legacy_container:-postgres_auction}
  container_id=$(docker ps --filter "name=^/${legacy_container}$" --format '{{.ID}}' | head -n 1)
fi

[ -n "$container_id" ] || fail "No running PostgreSQL container was found"

container_env=$(docker container inspect --format '{{range .Config.Env}}{{println .}}{{end}}' "$container_id")
db_user=$(printf '%s\n' "$container_env" | awk -F= '$1 == "POSTGRES_USER" {print substr($0, index($0, "=") + 1); exit}')
db_name=$(printf '%s\n' "$container_env" | awk -F= '$1 == "POSTGRES_DB" {print substr($0, index($0, "=") + 1); exit}')
db_user=${db_user:-postgres}
db_name=${db_name:-$db_user}

mkdir -p "$BACKUP_DIR"
chmod 700 "$BACKUP_DIR"

timestamp=$(date -u +'%Y%m%dT%H%M%SZ')
backup_file=$BACKUP_DIR/${db_name}_${timestamp}.dump
partial_file=${backup_file}.partial
trap 'rm -f "$partial_file"' EXIT

docker exec "$container_id" pg_isready -U "$db_user" -d "$db_name" >/dev/null
docker exec "$container_id" pg_dump \
  --username "$db_user" \
  --dbname "$db_name" \
  --format custom \
  --no-owner \
  --no-privileges > "$partial_file"

[ -s "$partial_file" ] || fail "pg_dump produced an empty backup"
docker exec -i "$container_id" pg_restore --list < "$partial_file" >/dev/null
mv "$partial_file" "$backup_file"
trap - EXIT

sha256sum "$backup_file" > "${backup_file}.sha256"
chmod 600 "$backup_file" "${backup_file}.sha256"

printf 'Verified PostgreSQL backup created: %s\n' "$backup_file"
