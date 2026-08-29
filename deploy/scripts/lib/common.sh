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

# Read the first non-empty line from a release metadata file.
read_release_tag() {
  local tag_file=$1
  [ -f "$tag_file" ] || return 1
  awk 'NF {print; exit}' "$tag_file"
}

# Replace release metadata atomically so an interrupted deployment cannot leave
# a partially-written image tag behind.
write_release_tag() {
  local tag_file=$1
  local tag=$2
  local tag_dir temp_file

  tag_dir=$(dirname "$tag_file")
  mkdir -p "$tag_dir"
  temp_file=$(mktemp "${tag_file}.tmp.XXXXXX")
  printf '%s\n' "$tag" > "$temp_file"
  chmod 600 "$temp_file"
  mv "$temp_file" "$tag_file"
}

# Resolve the backend repository without a tag. BACKEND_REPOSITORY is useful
# for one-off maintenance; production normally reads BACKEND_IMAGE from .env.
resolve_backend_repository() {
  local repository last_component docker_username

  repository=${BACKEND_REPOSITORY:-}
  if [ -z "$repository" ]; then
    repository=$(read_env_value BACKEND_IMAGE 2>/dev/null || true)
  fi
  if [ -z "$repository" ]; then
    docker_username=$(read_env_value DOCKER_USERNAME 2>/dev/null || true)
    repository=${docker_username:-tinh08042005}/auction-backend
  fi

  repository=${repository%@*}
  last_component=${repository##*/}
  case "$last_component" in
    *:*) repository=${repository%:*} ;;
  esac

  [ -n "$repository" ] || fail "Could not resolve the backend image repository."
  printf '%s\n' "$repository"
}

# Print the tag only when an image reference belongs to the expected repository.
image_tag_for_repository() {
  local image_ref=$1
  local repository=$2

  case "$image_ref" in
    "$repository":*) printf '%s\n' "${image_ref#"$repository:"}" ;;
    *) return 1 ;;
  esac
}

# Wrapper for docker compose with the configured environment and compose file
compose() {
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

# Discover the immutable tag used by the currently running Compose backend.
running_backend_tag() {
  local repository=$1
  local container_id image_ref

  container_id=$(compose ps -q backend 2>/dev/null | head -n 1 || true)
  [ -n "$container_id" ] || return 1
  image_ref=$(docker container inspect --format '{{.Config.Image}}' "$container_id" 2>/dev/null || true)
  [ -n "$image_ref" ] || return 1
  image_tag_for_repository "$image_ref" "$repository"
}

# Rotate current/previous metadata only after a release has passed health checks.
record_successful_release() {
  local new_tag=$1
  local current_before=$2
  local current_tag_file=$3
  local previous_tag_file=$4

  if [ -n "$current_before" ] && [ "$current_before" != "$new_tag" ]; then
    write_release_tag "$previous_tag_file" "$current_before"
  fi
  write_release_tag "$current_tag_file" "$new_tag"
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
