#!/bin/sh
set -eu

MODE="${1:-full}"
REF="${BPMT_REF:-v1.7.1}"
INSTALL_DIR="${BPMT_HOME:-bpmt-lite}"
RAW_BASE_URL="${BPMT_RAW_BASE_URL:-https://raw.githubusercontent.com/wodenwang/bpmt-lite/$REF}"
SQL_BASE_URL="${BPMT_SQL_BASE_URL:-$RAW_BASE_URL/database}"

usage() {
  echo "Usage: install.sh [min]" >&2
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
    run_arg=""
    ;;
  min)
    run_arg="min"
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

mkdir -p "$INSTALL_DIR"
cd "$INSTALL_DIR"

download "$RAW_BASE_URL/scripts/run.sh" run.sh
download "$RAW_BASE_URL/scripts/upgrade.sh" upgrade.sh
chmod +x run.sh
chmod +x upgrade.sh

# HTTPS-related BPMT_* environment variables intentionally pass through to run.sh.
BPMT_REF="$REF" BPMT_RAW_BASE_URL="$RAW_BASE_URL" BPMT_SQL_BASE_URL="$SQL_BASE_URL" sh ./run.sh $run_arg
