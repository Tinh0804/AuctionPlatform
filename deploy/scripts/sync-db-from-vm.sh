#!/usr/bin/env bash
# sync-db-from-vm.sh - Pull and sync PostgreSQL database dump from GCP VM to local machine
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
BACKUP_DIR="$REPO_ROOT/deploy/backups"
mkdir -p "$BACKUP_DIR"

# Read connection parameters from arguments or prompt
VM_IP=${1:-${GCP_VM_IP:-}}
VM_USER=${2:-${GCP_VM_USER:-}}
SSH_KEY=${3:-${GCP_SSH_KEY:-}}

if [ -z "$VM_IP" ]; then
  read -r -p "Enter GCP VM External IP: " VM_IP
fi

if [ -z "$VM_USER" ]; then
  read -r -p "Enter GCP VM SSH User: " VM_USER
fi

if [ -z "$SSH_KEY" ]; then
  # Auto-detect common keys if exist
  for candidate in "$HOME/.ssh/gcp_key" "$REPO_ROOT/../KeyProjectCV.pem" "$HOME/.ssh/id_rsa" "$HOME/.ssh/id_ed25519"; do
    if [ -f "$candidate" ]; then
      SSH_KEY="$candidate"
      break
    fi
  done
  read -r -p "Enter path to SSH Private Key [${SSH_KEY:-default}]: " input_key
  # Trim spaces and escape sequences
  input_key=$(printf '%s' "$input_key" | tr -d '\r\n\t' | sed 's/^[[:space:]]*//;s/[[:space:]]*$//;s/\x1b\[[0-9;]*[a-zA-Z]//g')
  if [ -n "$input_key" ]; then
    SSH_KEY="$input_key"
  fi
fi

# Trim any accidental trailing spaces or control chars
SSH_KEY=$(printf '%s' "$SSH_KEY" | tr -d '\r\n\t' | sed 's/^[[:space:]]*//;s/[[:space:]]*$//;s/\x1b\[[0-9;]*[a-zA-Z]//g')
VM_IP=$(printf '%s' "$VM_IP" | tr -d '[:space:]')
VM_USER=$(printf '%s' "$VM_USER" | tr -d '[:space:]')

[ -n "$VM_IP" ] || { log_error "VM IP is required."; exit 1; }
[ -n "$VM_USER" ] || { log_error "VM User is required."; exit 1; }

timestamp=$(date +'%Y%m%d_%H%M%S')
LOCAL_DUMP_FILE="$BACKUP_DIR/auctiondb_vm_${timestamp}.sql"

log_info "Connecting to GCP VM ($VM_USER@$VM_IP) using key [${SSH_KEY:-system-default}]..."

# Run pg_dump inside postgres container on VM and stream stdout directly to local file
SSH_OPTS=(-T -q -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 -o LogLevel=QUIET)
resolved_key="${SSH_KEY/#\~/$HOME}"
if [ -n "$resolved_key" ] && [ -f "$resolved_key" ]; then
  chmod 600 "$resolved_key" 2>/dev/null || true
  SSH_OPTS+=(-i "$resolved_key")
fi

ssh "${SSH_OPTS[@]}" "$VM_USER@$VM_IP" << 'REMOTE_SCRIPT' > "$LOCAL_DUMP_FILE"
  # Find running postgres container
  CONTAINER_ID=$(sudo docker ps --filter "label=com.docker.compose.service=postgres" --format '{{.ID}}' | head -n 1)
  if [ -z "$CONTAINER_ID" ]; then
    CONTAINER_ID=$(sudo docker ps --filter "name=postgres" --format '{{.ID}}' | head -n 1)
  fi

  if [ -z "$CONTAINER_ID" ]; then
    echo "ERROR: PostgreSQL container not found on VM" >&2
    exit 1
  fi

  # Auto detect Postgres user and DB name from container environment
  PG_USER=$(sudo docker exec "$CONTAINER_ID" printenv POSTGRES_USER 2>/dev/null || echo "")
  PG_DB=$(sudo docker exec "$CONTAINER_ID" printenv POSTGRES_DB 2>/dev/null || echo "")
  PG_USER=${PG_USER:-postgres}
  PG_DB=${PG_DB:-auctiondb}

  # Execute pg_dump with detected user, fallback to postgres if needed
  if ! sudo docker exec "$CONTAINER_ID" pg_dump -U "$PG_USER" -d "$PG_DB" --clean --if-exists --no-owner --no-privileges 2>/dev/null; then
    sudo docker exec "$CONTAINER_ID" pg_dump -U postgres -d "$PG_DB" --clean --if-exists --no-owner --no-privileges
  fi
REMOTE_SCRIPT

# Clean up any leftover SSH MOTD / banner lines from top of file
if [ -f "$LOCAL_DUMP_FILE" ]; then
  sed -i '' -e '/^--/,$!d' "$LOCAL_DUMP_FILE" 2>/dev/null || sed -i -e '/^--/,$!d' "$LOCAL_DUMP_FILE" 2>/dev/null || true
fi

if [ ! -s "$LOCAL_DUMP_FILE" ]; then
  rm -f "$LOCAL_DUMP_FILE"
  log_error "Export failed or produced an empty file. Please check VM container status."
  exit 1
fi

file_size=$(du -h "$LOCAL_DUMP_FILE" | awk '{print $1}')
log_success "Database dump successfully downloaded to local machine!"
log_info "File path : $LOCAL_DUMP_FILE"
log_info "File size : $file_size"

# Optional: update deploy/database.sql with the latest dump if requested
read -r -p "Do you want to update 'deploy/database.sql' with this latest VM data? (y/N): " update_choice
update_choice=$(printf '%s' "$update_choice" | tr -d '[:space:]' | sed 's/\x1b\[[0-9;]*[a-zA-Z]//g')
if [[ "$update_choice" =~ ^[Yy]$ ]]; then
  cp "$LOCAL_DUMP_FILE" "$REPO_ROOT/deploy/database.sql"
  cp "$LOCAL_DUMP_FILE" "$REPO_ROOT/Backend/database.sql"
  log_success "Updated 'deploy/database.sql' and 'Backend/database.sql' with latest VM schema & data."
fi

printf "\n${C_CYAN}=== Hướng dẫn Import vào PostgreSQL Local (nếu cần) ===${C_RESET}\n"
echo "docker exec -i <local_postgres_container> psql -U root -d auctiondb < \"$LOCAL_DUMP_FILE\""
echo "hoặc: psql -U postgres -d auctiondb -f \"$LOCAL_DUMP_FILE\""
