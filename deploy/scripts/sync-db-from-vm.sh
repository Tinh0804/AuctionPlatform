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
SSH_KEY=${3:-${GCP_SSH_KEY:-~/.ssh/gcp_deploy_key}}

if [ -z "$VM_IP" ]; then
  read -r -p "Enter GCP VM External IP: " VM_IP
fi

if [ -z "$VM_USER" ]; then
  read -r -p "Enter GCP VM SSH User: " VM_USER
fi

[ -n "$VM_IP" ] || { log_error "VM IP is required."; exit 1; }
[ -n "$VM_USER" ] || { log_error "VM User is required."; exit 1; }

timestamp=$(date +'%Y%m%d_%H%M%S')
LOCAL_DUMP_FILE="$BACKUP_DIR/auctiondb_vm_${timestamp}.sql"

log_info "Connecting to GCP VM ($VM_USER@$VM_IP) to export PostgreSQL database..."

# Run pg_dump inside postgres container on VM and stream stdout directly to local file
SSH_OPTS=(-o StrictHostKeyChecking=accept-new -o ConnectTimeout=10)
if [ -f "${SSH_KEY/#\~/$HOME}" ]; then
  SSH_OPTS+=(-i "${SSH_KEY/#\~/$HOME}")
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

  # Execute pg_dump plain SQL format (clean, inserts, no owner)
  sudo docker exec "$CONTAINER_ID" pg_dump \
    -U root \
    -d auctiondb \
    --clean \
    --if-exists \
    --no-owner \
    --no-privileges
REMOTE_SCRIPT

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
if [[ "$update_choice" =~ ^[Yy]$ ]]; then
  cp "$LOCAL_DUMP_FILE" "$REPO_ROOT/deploy/database.sql"
  cp "$LOCAL_DUMP_FILE" "$REPO_ROOT/Backend/database.sql"
  log_success "Updated 'deploy/database.sql' and 'Backend/database.sql' with latest VM schema & data."
fi

printf "\n${C_CYAN}=== Hướng dẫn Import vào PostgreSQL Local (nếu cần) ===${C_RESET}\n"
echo "docker exec -i <local_postgres_container> psql -U root -d auctiondb < \"$LOCAL_DUMP_FILE\""
echo "hoặc: psql -U postgres -d auctiondb -f \"$LOCAL_DUMP_FILE\""
