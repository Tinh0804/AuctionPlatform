#!/usr/bin/env bash
# push-db-to-vm.sh - Push and restore local PostgreSQL database/data to GCP VM
set -Eeuo pipefail

C_RESET='\033[0m'
C_RED='\033[0;31m'
C_GREEN='\033[0;32m'
C_YELLOW='\033[0;33m'
C_BLUE='\033[0;34m'
C_CYAN='\033[0;36m'

log_info() { printf "${C_BLUE}[INFO]${C_RESET} %s\n" "$*"; }
log_success() { printf "${C_GREEN}[SUCCESS]${C_RESET} %s\n" "$*"; }
log_warn() { printf "${C_YELLOW}[WARN]${C_RESET} %s\n" "$*" >&2; }
log_error() { printf "${C_RED}[ERROR]${C_RESET} %s\n" "$*" >&2; }

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)

# 1. Connection parameters
VM_IP=${1:-${GCP_VM_IP:-35.215.146.63}}
VM_USER=${2:-${GCP_VM_USER:-kutinhtk1}}
SSH_KEY=${3:-${GCP_SSH_KEY:-}}
SOURCE_FILE=${4:-}

if [ -z "$SSH_KEY" ]; then
  for candidate in "$HOME/.ssh/gcp_key" "$REPO_ROOT/../KeyProjectCV.pem" "$HOME/.ssh/id_rsa" "$HOME/.ssh/id_ed25519"; do
    if [ -f "$candidate" ]; then
      SSH_KEY="$candidate"
      break
    fi
  done
  read -r -p "Enter path to SSH Private Key [${SSH_KEY:-default}]: " input_key
  input_key=$(printf '%s' "$input_key" | tr -d '\r\n\t' | sed 's/^[[:space:]]*//;s/[[:space:]]*$//;s/\x1b\[[0-9;]*[a-zA-Z]//g')
  if [ -n "$input_key" ]; then
    SSH_KEY="$input_key"
  fi
fi

SSH_KEY=$(printf '%s' "$SSH_KEY" | tr -d '\r\n\t' | sed 's/^[[:space:]]*//;s/[[:space:]]*$//;s/\x1b\[[0-9;]*[a-zA-Z]//g')
VM_IP=$(printf '%s' "$VM_IP" | tr -d '[:space:]')
VM_USER=$(printf '%s' "$VM_USER" | tr -d '[:space:]')

# 2. Select Local Data Source
TEMP_DUMP=""
cleanup() {
  if [ -n "$TEMP_DUMP" ] && [ -f "$TEMP_DUMP" ]; then
    rm -f "$TEMP_DUMP"
  fi
}
trap cleanup EXIT

if [ -z "$SOURCE_FILE" ]; then
  printf "\n${C_CYAN}=== Chọn nguồn dữ liệu Local muốn đẩy lên GCP VM ===${C_RESET}\n"
  echo "1) Dùng file SQL có sẵn: Backend/database.sql"
  echo "2) Dump trực tiếp từ PostgreSQL Container đang chạy trên máy Local"
  echo "3) Nhập đường dẫn tới file .sql khác trên máy"
  read -r -p "Chọn nguồn (1/2/3) [mặc định: 1]: " source_choice
  source_choice=$(printf '%s' "$source_choice" | tr -d '[:space:]')

  case "$source_choice" in
    2)
      log_info "Đang tìm PostgreSQL container đang chạy ở Local..."
      LOCAL_CID=$(docker ps --filter "name=postgres" --format '{{.ID}}' | head -n 1)
      if [ -z "$LOCAL_CID" ]; then
        log_error "Không tìm thấy Docker container postgres nào đang chạy ở Local."
        exit 1
      fi
      TEMP_DUMP=$(mktemp /tmp/local_pg_dump_XXXXXX.sql)
      log_info "Đang export dữ liệu từ container local ($LOCAL_CID)..."
      docker exec "$LOCAL_CID" pg_dump -U root -d auctiondb --clean --if-exists --no-owner --no-privileges > "$TEMP_DUMP" 2>/dev/null || \
      docker exec "$LOCAL_CID" pg_dump -U postgres -d auctiondb --clean --if-exists --no-owner --no-privileges > "$TEMP_DUMP"
      SOURCE_FILE="$TEMP_DUMP"
      ;;
    3)
      read -r -p "Nhập đường dẫn file .sql: " custom_path
      SOURCE_FILE=$(printf '%s' "$custom_path" | tr -d '\r\n\t' | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
      ;;
    *)
      SOURCE_FILE="$REPO_ROOT/Backend/database.sql"
      ;;
  esac
fi

[ -f "$SOURCE_FILE" ] || { log_error "File '$SOURCE_FILE' không tồn tại."; exit 1; }
file_size=$(du -h "$SOURCE_FILE" | awk '{print $1}')
log_info "Nguồn dữ liệu : $SOURCE_FILE ($file_size)"
log_info "Đang kết nối tới GCP VM ($VM_USER@$VM_IP) qua SSH key [${SSH_KEY}]..."

SSH_OPTS=(-T -q -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 -o LogLevel=QUIET)
resolved_key="${SSH_KEY/#\~/$HOME}"
if [ -n "$resolved_key" ] && [ -f "$resolved_key" ]; then
  chmod 600 "$resolved_key" 2>/dev/null || true
  SSH_OPTS+=(-i "$resolved_key")
fi

# 3. Stream SQL to remote postgres container
log_info "Đang nạp dữ liệu vào PostgreSQL trên GCP VM..."

ssh "${SSH_OPTS[@]}" "$VM_USER@$VM_IP" bash -s << 'REMOTE_SCRIPT' < "$SOURCE_FILE"
  set -euo pipefail
  
  # Find running postgres container on VM
  CONTAINER_ID=$(sudo docker ps --filter "label=com.docker.compose.service=postgres" --format '{{.ID}}' | head -n 1)
  if [ -z "$CONTAINER_ID" ]; then
    CONTAINER_ID=$(sudo docker ps --filter "name=postgres" --format '{{.ID}}' | head -n 1)
  fi

  if [ -z "$CONTAINER_ID" ]; then
    echo "ERROR: Không tìm thấy container PostgreSQL trên VM." >&2
    exit 1
  fi

  PG_USER=$(sudo docker exec "$CONTAINER_ID" printenv POSTGRES_USER 2>/dev/null || echo "")
  PG_DB=$(sudo docker exec "$CONTAINER_ID" printenv POSTGRES_DB 2>/dev/null || echo "")
  PG_USER=${PG_USER:-postgres}
  PG_DB=${PG_DB:-auctiondb}

  # Import input stream directly into postgres
  sudo docker exec -i "$CONTAINER_ID" psql -U "$PG_USER" -d "$PG_DB" 2>&1 | grep -v 'NOTICE:' || true

  echo "=== Kiểm tra danh sách bảng trên VM sau khi nạp ==="
  sudo docker exec "$CONTAINER_ID" psql -U "$PG_USER" -d "$PG_DB" -c "\dt"
REMOTE_SCRIPT

log_success "Đã sao chép và nạp toàn bộ dữ liệu từ Local lên PostgreSQL trên GCP VM thành công!"
