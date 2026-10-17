#!/bin/bash
# Мониторинг процессов: снимок ps каждые 5 секунд дописывается в processes.log.
# Второй запуск цикла не создаёт новую копию: занят lock-файл.
# Один снимок без цикла: monitor.sh --once (так скрипт вызывает cron).

set -u

DIR="$(cd "$(dirname "$0")" && pwd)"
LOG="${LOG_FILE:-$DIR/processes.log}"
LOCK="${LOCK_FILE:-$DIR/monitor.lock}"

snapshot() {
    {
        echo "===== $(date '+%Y-%m-%d %H:%M:%S') ====="
        ps -ef
        echo
    } >> "$LOG"
}

if [[ "${1:-}" == "--once" ]]; then
    snapshot
    exit 0
fi

exec 9>"$LOCK"
if ! flock -n 9; then
    echo "$(date '+%Y-%m-%d %H:%M:%S') monitor.sh already running" >> "$LOG"
    exit 0
fi

while true; do
    snapshot
    sleep 5
done
