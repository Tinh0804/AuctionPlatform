#!/usr/bin/env bash
# common.sh - Shared utility functions for deployment scripts
set -Eeuo pipefail

# ANSI color codes
C_RESET='\033[0m'
C_RED='\033[0;31m'
C_GREEN='\033[0;32m'
C_YELLOW='\033[0;33m'
C_BLUE='\033[0;34m'
C_CYAN='\033[0;36m'

log_info() {
  printf "${C_BLUE}[INFO]${C_RESET} %s\n" "$*"
}

log_success() {
  printf "${C_GREEN}[SUCCESS]${C_RESET} %s\n" "$*"
}

log_warn() {
  printf "${C_YELLOW}[WARN]${C_RESET} %s\n" "$*" >&2
}

log_error() {
  printf "${C_RED}[ERROR]${C_RESET} %s\n" "$*" >&2
}

fail() {
  log_error "$1"
  exit "${2:-1}"
}

# Read a specific KEY from an environment file
read_env_value() {
  local key=$1
  local env_path=${2:-$ENV_FILE}
  [ -f "$env_path" ] || return 1
  awk -F= -v wanted="$key" '
    $1 == wanted {
      value=substr($0, index($0, "=") + 1)
      sub(/\r$/, "", value)
      print value
      exit
    }
  ' "$env_path"
}

# Wrapper for docker compose with the configured environment and compose file
compose() {
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

# Check if required CLI tools exist on the host
check_required_tools() {
  local missing=()
  for tool in "$@"; do
    command -v "$tool" >/dev/null 2>&1 || missing+=("$tool")
  done
  if [ "${#missing[@]}" -gt 0 ]; then
    fail "Missing required tool(s): ${missing[*]}"
  fi
}

# Wait for a docker compose service to report healthy status
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

# Acquire deployment lock to prevent concurrent runs
acquire_deploy_lock() {
  local lock_file=$1
  mkdir -p "$(dirname "$lock_file")"
  exec 9>"$lock_file"
  flock -n 9 || fail "Another deployment or maintenance task is currently running."
}
