#!/bin/sh
set -eu

MODE="${1:-full}"
REF="${BPMT_REF:-v1.7.5}"
REMOTE_ASSETS=0
RAW_BASE_URL="${BPMT_RAW_BASE_URL:-}"
SQL_BASE_URL="${BPMT_SQL_BASE_URL:-}"

if [ -n "$RAW_BASE_URL" ]; then
  REMOTE_ASSETS=1
fi

if [ "$REMOTE_ASSETS" = "1" ] && [ -z "$SQL_BASE_URL" ]; then
  SQL_BASE_URL="$RAW_BASE_URL/database"
fi

usage() {
  echo "Usage: scripts/run.sh [min]" >&2
}

download() {
  url="$1"
  target="$2"
  tmp="$target.tmp"

  if ! command -v curl >/dev/null 2>&1; then
    echo "curl is required." >&2
    exit 1
  fi

  curl -fsSL "$url" -o "$tmp"
  mv "$tmp" "$target"
}

case "$MODE" in
  full|"")
    db_name="bpmt"
    init_arg=""
    ;;
  min)
    db_name="bpmt_min"
    init_arg="min"
    ;;
  -h|--help|help)
    usage
    exit 0
    ;;
  *)
    usage
    exit 2
    ;;
esac

mkdir -p db/init docker/nginx

https_enabled=0
case "${BPMT_HTTPS_ENABLED:-0}" in
  1|true|TRUE|yes|YES|on|ON)
    https_enabled=1
    ;;
esac

if [ "$REMOTE_ASSETS" = "1" ]; then
  download "$RAW_BASE_URL/docker-compose.yml" docker-compose.yml
  download "$RAW_BASE_URL/scripts/init-db.sh" init-db.sh
  download "$RAW_BASE_URL/scripts/upgrade.sh" upgrade.sh
  download "$RAW_BASE_URL/scripts/render-nginx-conf.sh" render-nginx-conf.sh
  download "$RAW_BASE_URL/scripts/generate-self-signed-cert.sh" generate-self-signed-cert.sh
  download "$RAW_BASE_URL/docker/nginx/nginx.conf.template" docker/nginx/nginx.conf.template
  if [ "$https_enabled" = "1" ]; then
    download "$RAW_BASE_URL/docker-compose.https.yml" docker-compose.https.yml
  fi
  chmod +x init-db.sh upgrade.sh render-nginx-conf.sh generate-self-signed-cert.sh
  init_db_script="./init-db.sh"
  render_script="./render-nginx-conf.sh"
  cert_script="./generate-self-signed-cert.sh"
else
  init_db_script="scripts/init-db.sh"
  render_script="scripts/render-nginx-conf.sh"
  cert_script="scripts/generate-self-signed-cert.sh"
fi

if [ -d docker/nginx/nginx.conf ]; then
  rm -rf docker/nginx/nginx.conf
fi

if [ -n "$SQL_BASE_URL" ]; then
  BPMT_SQL_BASE_URL="$SQL_BASE_URL" sh "$init_db_script" $init_arg
else
  sh "$init_db_script" $init_arg
fi

if [ "$https_enabled" = "1" ]; then
  if [ ! -s certs/fullchain.pem ] || [ ! -s certs/privkey.pem ]; then
    sh "$cert_script"
  fi
fi

sh "$render_script"

mkdir -p .bpmt-lite
{
  echo "version=$REF"
  echo "database=$db_name"
  echo "installed_at=$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
} > .bpmt-lite/version

if [ "${BPMT_SKIP_UP:-}" = "1" ] || [ "${BPMT_SKIP_UP:-}" = "true" ]; then
  echo "Prepared docker-compose.yml, database SQL, and nginx config for $db_name."
  exit 0
fi

if [ "$https_enabled" = "1" ]; then
  DB_NAME="$db_name" docker compose -f docker-compose.yml -f docker-compose.https.yml up -d
else
  DB_NAME="$db_name" docker compose up -d
fi

echo "bpmt-lite is starting with database: $db_name"
case "${BPMT_HTTPS_ENABLED:-0}" in
  1|true|TRUE|yes|YES|on|ON)
    echo "URL: https://127.0.0.1:${BPMT_HTTPS_PORT:-443}/"
    echo "HTTP: http://127.0.0.1:${BPMT_HTTP_PORT:-80}/"
    ;;
  *)
    echo "URL: http://127.0.0.1:${BPMT_HTTP_PORT:-80}/"
    ;;
esac
echo "Login: admin/admin"
