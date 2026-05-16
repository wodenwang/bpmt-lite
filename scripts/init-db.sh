#!/bin/sh
set -eu

MODE="${1:-full}"
BASE_URL="${BPMT_SQL_BASE_URL:-https://raw.githubusercontent.com/wodenwang/bpmt-lite/v1.7.3/database}"
LOCAL_DATABASE_DIR="${BPMT_SQL_LOCAL_DIR:-database}"
INIT_DIR="${BPMT_INIT_DIR:-db/init}"

mkdir -p "$INIT_DIR"

usage() {
  echo "Usage: scripts/init-db.sh [min]" >&2
}

copy_or_download() {
  source_name="$1"
  target="$2"
  local_file="$LOCAL_DATABASE_DIR/$source_name"
  local_gzip_file="$local_file.gz"
  tmp_file="$target.tmp"

  if [ -f "$local_file" ]; then
    cp "$local_file" "$target"
    echo "Initialized $target from $local_file"
    return 0
  fi

  if [ -f "$local_gzip_file" ]; then
    gzip -dc "$local_gzip_file" > "$target"
    echo "Initialized $target from $local_gzip_file"
    return 0
  fi

  if command -v curl >/dev/null 2>&1; then
    if curl -fsSL "$BASE_URL/$source_name" -o "$tmp_file" 2>/dev/null; then
      mv "$tmp_file" "$target"
      echo "Initialized $target from $BASE_URL/$source_name"
      return 0
    fi
    rm -f "$tmp_file"

    if curl -fsSL "$BASE_URL/$source_name.gz" -o "$tmp_file.gz" 2>/dev/null && gzip -dc "$tmp_file.gz" > "$tmp_file"; then
      mv "$tmp_file" "$target"
      rm -f "$tmp_file.gz"
      echo "Initialized $target from $BASE_URL/$source_name.gz"
      return 0
    fi
    rm -f "$tmp_file" "$tmp_file.gz"
  fi

  echo "Cannot find $local_file or $local_gzip_file and cannot download $BASE_URL/$source_name" >&2
  return 1
}

case "$MODE" in
  full|"")
    copy_or_download "bpmt.sql" "$INIT_DIR/bpmt.sql"
    ;;
  min)
    copy_or_download "bpmt-min.sql" "$INIT_DIR/bpmt-min.sql"
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    usage
    exit 2
    ;;
esac
