.PHONY: build install run build-release install-release run-release clean test help

# Default target
help:
	@echo "Available targets:"
	@echo "  build           - Build debug APK"
	@echo "  install         - Build and install debug APK on connected device"
	@echo "  run             - Build, install, and launch the app (debug)"
	@echo "  build-release   - Build release APK"
	@echo "  install-release - Build and install release APK on connected device"
	@echo "  run-release     - Build, install, and launch the app (release)"
	@echo "  clean           - Clean build artifacts"
	@echo "  test            - Run unit tests"

# Build debug APK
build:
	./gradlew assembleDebug

# Build and install debug APK on connected device
install: build
	adb install -r app/build/outputs/apk/debug/app-debug.apk

# Build, install, and launch the app
run: install
	adb shell am start -n com.matedroid/.MainActivity

# Build release APK
build-release:
	./gradlew assembleRelease

# Build and install release APK on connected device
install-release: build-release
	adb install -r app/build/outputs/apk/release/app-release.apk

# Build, install, and launch the app (release)
run-release: install-release
	adb shell am start -n com.matedroid/.MainActivity

# Clean build artifacts
clean:
	./gradlew clean

# Run unit tests
test:
	./gradlew testDebugUnitTest
