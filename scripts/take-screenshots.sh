#!/bin/bash
#
# take-screenshots.sh - Capture screenshots of all main app screens
#
# Usage: ./scripts/take-screenshots.sh [CAR_ID] [EXTERIOR_COLOR]
#
# Requires: adb, imagemagick (convert)
# The app must be installed on the connected device and configured
# with at least one car.
#
# Each screen is themed with a different car color for visual variety.
# The default color (used for most screens) can be overridden via the
# EXTERIOR_COLOR argument.

set -euo pipefail

PACKAGE="com.matedroid"
ACTIVITY=".MainActivity"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SCREENSHOT_DIR="$SCRIPT_DIR/../docs/screenshots"
CAR_ID="${1:-1}"
DEFAULT_COLOR="${2:-MidnightSilver}"
LOAD_WAIT=4  # seconds to wait for each screen to load

# Crop coordinates for Pixel 8a (1080x2400)
# Status bar: 132px, Navigation bar: 126px
STATUS_BAR=132
NAV_BAR=126
SCREEN_WIDTH=1080
SCREEN_HEIGHT=2400
CROP_HEIGHT=$((SCREEN_HEIGHT - STATUS_BAR - NAV_BAR))

# Target width matching existing screenshots (~576px wide)
TARGET_WIDTH=576

# Screen definitions: intent_value:output_filename:exterior_color
# Each screen uses a different color for visual variety in docs,
# with the default color for the rest.
SCREENS=(
    "dashboard:main-dashboard:${DEFAULT_COLOR}"
    "charges:charges:PearlWhite"
    "drives:drives:UltraRed"
    "mileage:mileage:DeepBlue"
    "stats:stats-for-nerds:${DEFAULT_COLOR}"
    "countries_visited:visited-countries:${DEFAULT_COLOR}"
    "battery:battery-health:${DEFAULT_COLOR}"
    "updates:software-versions:${DEFAULT_COLOR}"
)

check_deps() {
    if ! command -v adb &>/dev/null; then
        echo "Error: adb not found in PATH" >&2
        exit 1
    fi
    if ! command -v convert &>/dev/null; then
        echo "Error: imagemagick (convert) not found in PATH" >&2
        exit 1
    fi
    if ! adb get-state &>/dev/null 2>&1; then
        echo "Error: no device connected via adb" >&2
        exit 1
    fi
}

wake_and_unlock() {
    # Wake the screen
    adb shell input keyevent KEYCODE_WAKEUP
    sleep 1
    # Swipe up to dismiss lock screen (no PIN assumed)
    adb shell input swipe 540 2000 540 800 300
    sleep 1
    # Keep screen on while USB is connected
    adb shell svc power stayon usb
}

take_screenshot() {
    local navigate_to="$1"
    local filename="$2"
    local exterior_color="$3"
    local device_tmp="/sdcard/matedroid_screenshot.png"
    local local_tmp="/tmp/matedroid_screenshot_raw.png"

    echo -n "  $filename ($exterior_color)..."

    # Force stop the app
    adb shell am force-stop "$PACKAGE"
    sleep 1

    # Start the app, navigating directly to the target screen
    if [ "$navigate_to" = "dashboard" ]; then
        adb shell am start -n "$PACKAGE/$ACTIVITY" \
            --es EXTRA_EXTERIOR_COLOR "$exterior_color" \
            --ei EXTRA_CAR_ID "$CAR_ID" >/dev/null 2>&1
    else
        adb shell am start -n "$PACKAGE/$ACTIVITY" \
            --es EXTRA_NAVIGATE_TO "$navigate_to" \
            --es EXTRA_EXTERIOR_COLOR "$exterior_color" \
            --ei EXTRA_CAR_ID "$CAR_ID" >/dev/null 2>&1
    fi

    # Wait for the screen to render
    sleep "$LOAD_WAIT"

    # Capture, pull, and clean up on device
    adb shell screencap -p "$device_tmp"
    adb pull "$device_tmp" "$local_tmp" >/dev/null 2>&1
    adb shell rm "$device_tmp"

    # Crop (remove status bar + nav bar), resize, and save as JPEG
    convert "$local_tmp" \
        -crop "${SCREEN_WIDTH}x${CROP_HEIGHT}+0+${STATUS_BAR}" +repage \
        -resize "${TARGET_WIDTH}x" \
        -quality 85 \
        "$SCREENSHOT_DIR/$filename.jpg"

    rm -f "$local_tmp"
    echo " done"
}

main() {
    check_deps
    mkdir -p "$SCREENSHOT_DIR"

    echo "Waking device..."
    wake_and_unlock

    echo "Taking screenshots (carId=$CAR_ID, default color=$DEFAULT_COLOR)..."
    echo ""

    for screen in "${SCREENS[@]}"; do
        IFS=':' read -r navigate_to filename exterior_color <<< "$screen"
        take_screenshot "$navigate_to" "$filename" "$exterior_color"
    done

    echo ""
    echo "All screenshots saved to docs/screenshots/"
}

main
