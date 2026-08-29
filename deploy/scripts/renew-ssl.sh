#!/usr/bin/env bash
# renew-ssl.sh - Renew Let's Encrypt SSL certificates and reload Nginx
set -Eeuo pipefail

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)
DEPLOY_HOME=${DEPLOY_HOME:-/srv/auction}
ENV_FILE=${APP_ENV_FILE:-$DEPLOY_HOME/.env}
COMPOSE_FILE=${COMPOSE_FILE:-$REPO_ROOT/deploy/compose.prod.yml}
CERTBOT_WEBROOT=${CERTBOT_WEBROOT:-/srv/auction/certbot/www}

# shellcheck source=deploy/scripts/lib/common.sh
source "$SCRIPT_DIR/lib/common.sh"

log_info "Attempting to renew Let's Encrypt SSL certificate..."

if command -v certbot >/dev/null 2>&1; then
  certbot renew --webroot -w "$CERTBOT_WEBROOT" --quiet
  log_info "Certbot renew executed on host."
else
  # Use Docker certbot if host certbot is not installed
  docker run --rm \
    -v /etc/letsencrypt:/etc/letsencrypt \
    -v "$CERTBOT_WEBROOT:/var/www/certbot" \
    certbot/certbot renew --webroot -w /var/www/certbot --quiet
  log_info "Certbot container renew executed."
fi

# Reload Nginx container to pick up renewed certificates
nginx_container=$(compose ps -q nginx 2>/dev/null || true)
if [ -n "$nginx_container" ]; then
  log_info "Reloading Nginx configuration inside container '$nginx_container'..."
  docker exec "$nginx_container" nginx -s reload
  log_success "Nginx reloaded successfully with updated certificates."
else
  log_warn "Nginx container not found or not running. SSL certificate renewed but reload skipped."
fi
