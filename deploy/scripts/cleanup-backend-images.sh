#!/usr/bin/env bash
# Keep only the current and previous successful backend images on the VM.
set -Eeuo pipefail

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)
DEPLOY_HOME=${DEPLOY_HOME:-/srv/auction}
ENV_FILE=${APP_ENV_FILE:-$DEPLOY_HOME/.env}
COMPOSE_FILE=${COMPOSE_FILE:-$REPO_ROOT/deploy/compose.prod.yml}
CURRENT_TAG_FILE=$DEPLOY_HOME/current-image-tag
PREVIOUS_TAG_FILE=$DEPLOY_HOME/previous-image-tag
LOCK_FILE=$DEPLOY_HOME/deploy.lock
KEEP_BACKEND_IMAGE_COUNT=${KEEP_BACKEND_IMAGE_COUNT:-2}

# shellcheck source=deploy/scripts/lib/common.sh
source "$SCRIPT_DIR/lib/common.sh"

case "$KEEP_BACKEND_IMAGE_COUNT" in
  ''|*[!0-9]*) fail "KEEP_BACKEND_IMAGE_COUNT must be a positive integer." ;;
esac
[ "$KEEP_BACKEND_IMAGE_COUNT" -ge 1 ] || fail "KEEP_BACKEND_IMAGE_COUNT must be at least 1."

check_required_tools docker awk
if [ "${DEPLOY_LOCK_HELD:-false}" != "true" ]; then
  check_required_tools flock
  acquire_deploy_lock "$LOCK_FILE"
fi

backend_repository=$(resolve_backend_repository)
# The empty sentinel keeps Bash 3.2 + nounset compatible (the GCP VM uses a
# newer Bash, but operators may validate the script from macOS).
declare -a keep_tags=('')
declare -a keep_image_ids=('')
kept_image_count=0

array_contains() {
  local wanted=$1
  shift
  local item
  for item in "$@"; do
    [ "$item" = "$wanted" ] && return 0
  done
  return 1
}

add_keep_image_id() {
  local image_id=$1
  [ -n "$image_id" ] || return 0
  if ! array_contains "$image_id" "${keep_image_ids[@]}"; then
    keep_image_ids+=("$image_id")
    kept_image_count=$((kept_image_count + 1))
  fi
}

add_keep_tag() {
  local tag=$1
  local image_ref image_id
  [ -n "$tag" ] || return 0

  image_ref=$backend_repository:$tag
  image_id=$(docker image inspect --format '{{.Id}}' "$image_ref" 2>/dev/null || true)
  if [ -z "$image_id" ]; then
    log_warn "Retention tag is not present locally and cannot be protected: $image_ref"
    return 0
  fi

  if ! array_contains "$tag" "${keep_tags[@]}"; then
    keep_tags+=("$tag")
  fi
  add_keep_image_id "$image_id"
}

current_tag=$(read_release_tag "$CURRENT_TAG_FILE" 2>/dev/null || true)
previous_tag=$(read_release_tag "$PREVIOUS_TAG_FILE" 2>/dev/null || true)
add_keep_tag "$current_tag"
if [ "$KEEP_BACKEND_IMAGE_COUNT" -ge 2 ]; then
  add_keep_tag "$previous_tag"
fi

# Every running container using this backend repository is protected, including
# legacy containers created before the Compose project name was standardized.
while IFS= read -r container_id; do
  [ -n "$container_id" ] || continue
  container_image_ref=$(docker container inspect --format '{{.Config.Image}}' "$container_id")
  running_tag=$(image_tag_for_repository "$container_image_ref" "$backend_repository" 2>/dev/null || true)
  [ -n "$running_tag" ] || continue
  container_image_id=$(docker container inspect --format '{{.Image}}' "$container_id")
  add_keep_image_id "$container_image_id"
  add_keep_tag "$running_tag"
done < <(docker container ls --format '{{.ID}}')

image_inventory=$(docker image ls --no-trunc \
  --format '{{.Repository}}|{{.Tag}}|{{.ID}}' "$backend_repository")

# During the first deployment after adopting this policy, metadata may not exist.
# Fill any free retention slots with the newest unique local image IDs.
while IFS='|' read -r repository tag image_id; do
  [ "$repository" = "$backend_repository" ] || continue
  [ -n "$tag" ] && [ "$tag" != '<none>' ] || continue
  if [ "$kept_image_count" -lt "$KEEP_BACKEND_IMAGE_COUNT" ]; then
    add_keep_tag "$tag"
  fi
done <<< "$image_inventory"

[ "$kept_image_count" -gt 0 ] \
  || fail "No local image could be selected for retention in $backend_repository; refusing cleanup."

removed_containers=0
for container_status in created exited dead; do
  while IFS= read -r container_id; do
    [ -n "$container_id" ] || continue
    container_image_ref=$(docker container inspect --format '{{.Config.Image}}' "$container_id")
    stale_tag=$(image_tag_for_repository "$container_image_ref" "$backend_repository" 2>/dev/null || true)
    [ -n "$stale_tag" ] || continue
    container_image_id=$(docker container inspect --format '{{.Image}}' "$container_id")
    if array_contains "$container_image_id" "${keep_image_ids[@]}"; then
      continue
    fi
    docker container rm "$container_id" >/dev/null
    removed_containers=$((removed_containers + 1))
  done < <(docker container ls -a \
    --filter "status=$container_status" \
    --format '{{.ID}}')
done

removed_references=0
while IFS='|' read -r repository tag image_id; do
  [ "$repository" = "$backend_repository" ] || continue
  [ -n "$tag" ] && [ "$tag" != '<none>' ] || continue
  if array_contains "$tag" "${keep_tags[@]}"; then
    continue
  fi

  image_ref=$backend_repository:$tag
  if docker image rm "$image_ref" >/dev/null; then
    log_info "Removed old backend image reference: $image_ref"
    removed_references=$((removed_references + 1))
  else
    log_warn "Could not remove backend image reference (it may still be in use): $image_ref"
  fi
done <<< "$image_inventory"

log_success "Backend image cleanup completed for $backend_repository: kept $kept_image_count image(s), removed $removed_references reference(s) and $removed_containers stale container(s)."
