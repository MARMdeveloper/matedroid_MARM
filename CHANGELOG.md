# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial project structure with Kotlin and Jetpack Compose
- Settings screen for TeslamateApi server configuration
- Dashboard screen with real-time vehicle status display
  - Battery level and range information
  - Charging status with power, energy added, and time remaining
  - Climate information (inside/outside temperature)
  - Vehicle state (locked, location, odometer, software version)
- TeslamateApi Retrofit client with all endpoints
- Repository layer with ApiResult sealed class for error handling
- Material Design 3 theming with Tesla-inspired colors
- Dark/light mode support with dynamic colors on Android 12+
- DataStore for secure settings persistence
- Hilt dependency injection setup
- Navigation component with Compose integration
- Pull-to-refresh on Dashboard screen
- Automatic navigation to Dashboard if server already configured

## [0.1.0] - Unreleased

Initial development release (MVP in progress).

### Planned
- Dashboard with real-time vehicle status
- Charging history with statistics and charts
- Drive history with efficiency metrics
- Battery health tracking
- Software update history

[Unreleased]: https://github.com/yourusername/matedroid/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/yourusername/matedroid/releases/tag/v0.1.0
