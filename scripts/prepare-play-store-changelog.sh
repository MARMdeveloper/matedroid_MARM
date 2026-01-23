#!/usr/bin/env bash
#
# Prepares changelog files for Google Play upload.
# Converts Fastlane-style changelogs to the whatsnew-<LOCALE> format
# expected by the r0adkll/upload-google-play action.
#
# Usage: ./scripts/prepare-play-store-changelog.sh
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
METADATA_DIR="$PROJECT_ROOT/fastlane/metadata/android"

# Extract versionCode from build.gradle.kts
VERSION_CODE=$(grep 'versionCode' "$PROJECT_ROOT/app/build.gradle.kts" | sed 's/[^0-9]*//g')

if [ -z "$VERSION_CODE" ]; then
    echo "Error: Could not extract versionCode from build.gradle.kts"
    exit 1
fi

echo "Preparing changelogs for versionCode: $VERSION_CODE"

FOUND_ANY=false

# Loop through all locale directories
for locale_dir in "$METADATA_DIR"/*/; do
    locale=$(basename "$locale_dir")

    # Skip if not a locale directory (e.g., if there are other files)
    if [[ ! "$locale" =~ ^[a-z]{2}-[A-Z]{2}$ ]]; then
        continue
    fi

    changelog_file="$locale_dir/changelogs/${VERSION_CODE}.txt"
    whatsnew_file="$METADATA_DIR/whatsnew-${locale}"

    if [ -f "$changelog_file" ]; then
        cp "$changelog_file" "$whatsnew_file"
        echo "Created $whatsnew_file"
        FOUND_ANY=true
    else
        echo "Warning: No changelog found at $changelog_file"
    fi
done

if [ "$FOUND_ANY" = false ]; then
    echo "Error: No changelog files found for versionCode $VERSION_CODE"
    exit 1
fi

echo "Done."
