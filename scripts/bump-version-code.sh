#!/usr/bin/env bash
#
# Generates a monotonically increasing versionCode based on Unix epoch / 10.
# Updates app/build.gradle.kts in-place and prints the new value to stdout.
#
# Usage:
#   ./scripts/bump-version-code.sh          # update build.gradle.kts and print new code
#   ./scripts/bump-version-code.sh --dry-run  # print what the code would be, don't write
#
# The epoch/10 scheme produces a unique, always-increasing integer with
# 10-second granularity. Safe until ~2650 (int32 max = 2,147,483,647).
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE_FILE="$SCRIPT_DIR/../app/build.gradle.kts"

NEW_CODE=$(( $(date +%s) / 10 ))

if [[ "${1:-}" == "--dry-run" ]]; then
    echo "$NEW_CODE"
    exit 0
fi

if [[ ! -f "$GRADLE_FILE" ]]; then
    echo "Error: $GRADLE_FILE not found" >&2
    exit 1
fi

CURRENT_CODE=$(grep 'versionCode' "$GRADLE_FILE" | sed 's/[^0-9]*//g')

if [[ "$NEW_CODE" -le "$CURRENT_CODE" ]]; then
    echo "Error: computed versionCode ($NEW_CODE) is not greater than current ($CURRENT_CODE)" >&2
    exit 1
fi

sed -i "s|versionCode = [0-9]*|versionCode = $NEW_CODE|" "$GRADLE_FILE"
echo "$NEW_CODE"
