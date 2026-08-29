#!/usr/bin/env bash
set -Eeuo pipefail

TEST_DIR=$(mktemp -d)
trap 'rm -rf "$TEST_DIR"' EXIT

PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
BACKEND_REPOSITORY=tester/auction-backend
export TEST_DIR BACKEND_REPOSITORY

printf '%s\n' new-sha > "$TEST_DIR/current-image-tag"
printf '%s\n' previous-sha > "$TEST_DIR/previous-image-tag"
: > "$TEST_DIR/removed-containers"
: > "$TEST_DIR/removed-images"

docker() {
  local command=${1:-}
  local object=${2:-}
  local last_arg=${!#}

  case "$command $object" in
    'image inspect')
      case "$last_arg" in
        "$BACKEND_REPOSITORY:new-sha") printf '%s\n' 'sha256:new' ;;
        "$BACKEND_REPOSITORY:previous-sha") printf '%s\n' 'sha256:previous' ;;
        "$BACKEND_REPOSITORY:old-sha") printf '%s\n' 'sha256:old' ;;
        *) return 1 ;;
      esac
      ;;
    'image ls')
      printf '%s\n' \
        "$BACKEND_REPOSITORY|new-sha|sha256:new" \
        "$BACKEND_REPOSITORY|previous-sha|sha256:previous" \
        "$BACKEND_REPOSITORY|old-sha|sha256:old" \
        'postgres|15-alpine|sha256:postgres'
      ;;
    'image rm')
      printf '%s\n' "$last_arg" >> "$TEST_DIR/removed-images"
      ;;
    'container ls')
      case " $* " in
        *' status=exited '*) printf '%s\n' stale-backend ;;
        *' status=created '*|*' status=dead '*) ;;
        *' -a '*) ;;
        *) printf '%s\n' running-backend ;;
      esac
      ;;
    'container inspect')
      case " $* " in
        *'{{.Config.Image}}'*) printf '%s\n' "$BACKEND_REPOSITORY:new-sha" ;;
        *'{{.Image}}'*)
          case "$last_arg" in
            running-backend) printf '%s\n' 'sha256:new' ;;
            stale-backend) printf '%s\n' 'sha256:old' ;;
            *) return 1 ;;
          esac
          ;;
        *) return 1 ;;
      esac
      ;;
    'container rm')
      printf '%s\n' "$last_arg" >> "$TEST_DIR/removed-containers"
      ;;
    *)
      printf 'Unexpected fake docker call: %s\n' "$*" >&2
      return 1
      ;;
  esac
}
export -f docker

DEPLOY_LOCK_HELD=true \
DEPLOY_HOME="$TEST_DIR" \
APP_ENV_FILE="$TEST_DIR/.env" \
COMPOSE_FILE="$TEST_DIR/compose.yml" \
bash "$PROJECT_ROOT/deploy/scripts/cleanup-backend-images.sh"

grep -Fxq stale-backend "$TEST_DIR/removed-containers"
grep -Fxq "$BACKEND_REPOSITORY:old-sha" "$TEST_DIR/removed-images"
[ "$(wc -l < "$TEST_DIR/removed-containers" | tr -d ' ')" -eq 1 ]
[ "$(wc -l < "$TEST_DIR/removed-images" | tr -d ' ')" -eq 1 ]

# Verify current/previous rotation and the roll-forward swap behavior.
ENV_FILE="$TEST_DIR/.env"
# shellcheck source=deploy/scripts/lib/common.sh
source "$PROJECT_ROOT/deploy/scripts/lib/common.sh"
record_successful_release release-two release-one \
  "$TEST_DIR/current-image-tag" "$TEST_DIR/previous-image-tag"
[ "$(read_release_tag "$TEST_DIR/current-image-tag")" = release-two ]
[ "$(read_release_tag "$TEST_DIR/previous-image-tag")" = release-one ]

record_successful_release release-one release-two \
  "$TEST_DIR/current-image-tag" "$TEST_DIR/previous-image-tag"
[ "$(read_release_tag "$TEST_DIR/current-image-tag")" = release-one ]
[ "$(read_release_tag "$TEST_DIR/previous-image-tag")" = release-two ]

printf 'cleanup-backend-images test passed\n'
