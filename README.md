# MateDroid

A native Android application for viewing Tesla vehicle data from your self-hosted [Teslamate](https://github.com/adriankumpf/teslamate) instance via the [TeslamateApi](https://github.com/tobiasehlert/teslamateapi).

## Features

- **Dashboard** - Real-time vehicle status at a glance with 3D car image matching your vehicle's color and wheels
- **Charging History** - View all charging sessions with statistics and charts
- **Charge Details** - Interactive map and detailed power/voltage/temperature charts
- **Drive History** - Track trips with efficiency metrics and route visualization
- **Battery Health** - Monitor battery degradation over time
- **Software Updates** - Track update history
- **Dark Mode** - Follows system theme

## Requirements

- Android 8.0 (API 26) or higher
- A running [Teslamate](https://github.com/adriankumpf/teslamate) instance
- [TeslamateApi](https://github.com/tobiasehlert/teslamateapi) deployed and accessible

**This projects needs Teslamate API to work**, it will not work just with plan Teslamate. Please follow the Teslamate API instructions and make sure it is working as expected before opening any issue, thanks!

## Installation

### From Release (Recommended)

Download the latest APK from the [Releases](https://github.com/yourusername/matedroid/releases) page and install it on your Android device.

### Build from Source

#### Prerequisites

- Java 17 or higher
- Android SDK (API 35)
- (Optional) Android Studio

#### Build Steps

```bash
# Clone the repository
git clone https://github.com/yourusername/matedroid.git
cd matedroid

# Build debug APK
./gradlew assembleDebug

# The APK will be at: app/build/outputs/apk/debug/app-debug.apk

# Or install directly to a connected device/emulator
./gradlew installDebug
```

## Development

Please see [DEVELOPMENT.md](DEVELOPMENT.md) for more details on how this app was created and how to build and test it.

### Utility Scripts

#### `util/fetch_tesla_assets.py`

Python script to download Tesla car 3D renders from Tesla's compositor service. Requires [uv](https://github.com/astral-sh/uv) for dependency management.

```bash
# Download all car images (Model 3 & Y, various colors/wheels)
./util/fetch_tesla_assets.py

# Preview what would be downloaded
./util/fetch_tesla_assets.py --dry-run

# Custom output directory
./util/fetch_tesla_assets.py --output-dir /path/to/assets
```

See [ASSETS.md](ASSETS.md) for detailed documentation on Tesla compositor APIs, color/wheel code mappings, and troubleshooting.

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

### Releasing

Releases are automated via GitHub Actions. When a release is published, the workflow builds the APK and attaches it to the release.

```bash
# 1. Update version in app/build.gradle.kts (versionCode and versionName)
# 2. Update CHANGELOG.md with release notes
# 3. Commit and push

# 4. Create a release with GitHub CLI
gh release create v0.5.0 --generate-notes

# Or create a draft release to edit notes first
gh release create v0.5.0 --generate-notes --draft
```

#### Signing Configuration (Optional)

For release signing with a custom keystore, set these repository secrets:
- `KEYSTORE_BASE64`: Base64-encoded keystore file (`base64 -w0 your.keystore`)
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password

Without secrets, the APK is signed with a debug keystore (fine for sideloading, not for Play Store).

### Development Workflow

1. Start your Android emulator or connect a device
2. Build and install: `./gradlew installDebug`
3. View logs: `adb logcat | grep -i matedroid`

Or use Android Studio:
1. Open the project folder
2. Wait for Gradle sync
3. Click Run (green play button)

## Configuration

On first launch, you'll be prompted to configure your TeslamateApi connection:

1. **Server URL**: Your TeslamateApi instance URL (e.g., `https://teslamate-api.example.com`)
2. **API Token**: (Optional) If your instance requires authentication

## License

This project is licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for details.

## Acknowledgments

- [Teslamate](https://github.com/adriankumpf/teslamate) - Self-hosted Tesla data logger
- [TeslamateApi](https://github.com/tobiasehlert/teslamateapi) - RESTful API for Teslamate
- [t-buddy](https://github.com/garanda21/t-buddy) - iOS app inspiration
