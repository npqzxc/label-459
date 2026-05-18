#!/usr/bin/env bash
set -euo pipefail

java -jar /app/backend/app.jar &
backend_pid=$!

nginx -g 'daemon off;' &
nginx_pid=$!

cleanup() {
    kill "$backend_pid" "$nginx_pid" 2>/dev/null || true
    wait "$backend_pid" 2>/dev/null || true
    wait "$nginx_pid" 2>/dev/null || true
}

trap cleanup SIGINT SIGTERM EXIT

wait -n "$backend_pid" "$nginx_pid"
