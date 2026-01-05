# MateDroid - Android App for Teslamate

## Overview

MateDroid is a native Android application that displays Tesla vehicle data and statistics from a self-hosted [Teslamate](https://github.com/adriankumpf/teslamate) instance via the [TeslamateApi](https://github.com/tobiasehlert/teslamateapi).

The app provides a clean, modern interface for viewing:
- Real-time vehicle status
- Charging history and statistics
- Drive/trip history and efficiency metrics
- Battery health tracking
- Software update history

---

## Technology Stack Recommendation

### Language: **Kotlin**
- Official language for Android development since 2019
- Concise, expressive, and null-safe
- Excellent coroutine support for async operations
- Massive documentation and LLM training data coverage

### UI Framework: **Jetpack Compose**
- Modern declarative UI toolkit (official Google recommendation)
- Less boilerplate than XML-based layouts
- Excellent for building charts and custom visualizations
- Hot reload support for faster development
- Material Design 3 built-in

### Build System: **Gradle (Kotlin DSL)**
- Full CLI support (`./gradlew build`, `./gradlew installDebug`)
- Works perfectly on Linux terminal
- No IDE required (though Android Studio available if wanted)

### Networking: **Retrofit + OkHttp**
- Industry standard for REST APIs
- Kotlin coroutines integration
- Easy JSON parsing with Moshi/Kotlinx.serialization

### Charts: **Vico**
- Modern Jetpack Compose-native charting library
- Beautiful, customizable charts
- Active development and good documentation

### Architecture: **MVVM + Clean Architecture**
- ViewModels for UI state management
- Repository pattern for data layer
- Use cases for business logic
- Easy to test and maintain

### Dependency Injection: **Hilt**
- Official Android DI solution
- Reduces boilerplate
- Compile-time verification

---

## Development Environment Setup

### Required Tools (Linux)

```bash
# 1. Install Java 17 (required for Android development)
sudo apt install openjdk-17-jdk

# 2. Install Android SDK command-line tools
# Download from: https://developer.android.com/studio#command-tools
mkdir -p ~/Android/Sdk/cmdline-tools
cd ~/Android/Sdk/cmdline-tools
unzip commandlinetools-linux-*.zip
mv cmdline-tools latest

# 3. Set environment variables (add to ~/.bashrc or ~/.zshrc)
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

# 4. Accept licenses and install required SDK components
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# 5. (Optional) Install Android Studio for visual debugging
# Download from: https://developer.android.com/studio
```

### CLI Workflow

```bash
# Build the project
./gradlew build

# Run unit tests
./gradlew test

# Install debug APK to connected device/emulator
./gradlew installDebug

# Create release APK
./gradlew assembleRelease

# Lint checks
./gradlew lint
```

---

## Project Structure

```
matedroid/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/matedroid/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── MateDroidApp.kt
│   │   │   │   │
│   │   │   │   ├── data/
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── TeslamateApi.kt          # Retrofit interface
│   │   │   │   │   │   └── models/                  # API response DTOs
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── TeslamateRepository.kt
│   │   │   │   │   └── local/
│   │   │   │   │       └── SettingsDataStore.kt     # Local preferences
│   │   │   │   │
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/                       # Domain models
│   │   │   │   │   └── usecase/                     # Business logic
│   │   │   │   │
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   └── Type.kt
│   │   │   │   │   │
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   └── NavGraph.kt
│   │   │   │   │   │
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   │   ├── DashboardScreen.kt
│   │   │   │   │   │   │   └── DashboardViewModel.kt
│   │   │   │   │   │   ├── charges/
│   │   │   │   │   │   │   ├── ChargesScreen.kt
│   │   │   │   │   │   │   ├── ChargeDetailScreen.kt
│   │   │   │   │   │   │   └── ChargesViewModel.kt
│   │   │   │   │   │   ├── drives/
│   │   │   │   │   │   │   ├── DrivesScreen.kt
│   │   │   │   │   │   │   ├── DriveDetailScreen.kt
│   │   │   │   │   │   │   └── DrivesViewModel.kt
│   │   │   │   │   │   ├── battery/
│   │   │   │   │   │   │   ├── BatteryHealthScreen.kt
│   │   │   │   │   │   │   └── BatteryViewModel.kt
│   │   │   │   │   │   ├── updates/
│   │   │   │   │   │   │   └── UpdatesScreen.kt
│   │   │   │   │   │   └── settings/
│   │   │   │   │   │       ├── SettingsScreen.kt
│   │   │   │   │   │       └── SettingsViewModel.kt
│   │   │   │   │   │
│   │   │   │   │   └── components/
│   │   │   │   │       ├── StatCard.kt
│   │   │   │   │       ├── ChargeChart.kt
│   │   │   │   │       ├── EfficiencyChart.kt
│   │   │   │   │       └── LoadingIndicator.kt
│   │   │   │   │
│   │   │   │   └── di/
│   │   │   │       ├── AppModule.kt
│   │   │   │       └── NetworkModule.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── colors.xml
│   │   │   │   └── drawable/
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── test/                                    # Unit tests
│   │
│   └── build.gradle.kts
│
├── gradle/
├── build.gradle.kts                                 # Root build file
├── settings.gradle.kts
├── gradle.properties
├── local.properties                                 # SDK path (gitignored)
└── README.md
```

---

## TeslamateApi Integration

### API Endpoints to Implement

| Priority | Endpoint | Purpose |
|----------|----------|---------|
| P0 | `GET /api/v1/cars` | List vehicles (needed first) |
| P0 | `GET /api/v1/cars/:id/status` | Real-time vehicle status |
| P0 | `GET /api/v1/cars/:id/charges` | Charging history |
| P0 | `GET /api/v1/cars/:id/drives` | Drive history |
| P1 | `GET /api/v1/cars/:id/battery-health` | Battery degradation |
| P1 | `GET /api/v1/cars/:id/charges/:id` | Charge session detail |
| P1 | `GET /api/v1/cars/:id/drives/:id` | Drive detail |
| P2 | `GET /api/v1/cars/:id/updates` | Software updates |
| P2 | `POST /api/v1/cars/:id/wake_up` | Wake vehicle |
| P3 | `POST /api/v1/cars/:id/command/:cmd` | Vehicle commands |

### Authentication

The API supports token-based authentication:
- Header: `Authorization: Bearer <token>`
- Query param: `?token=<token>`

The app will store the API URL and token securely in encrypted SharedPreferences (DataStore).

### Retrofit Interface

```kotlin
interface TeslamateApi {
    @GET("api/v1/cars")
    suspend fun getCars(): Response<CarsResponse>

    @GET("api/v1/cars/{carId}/status")
    suspend fun getCarStatus(@Path("carId") carId: Int): Response<CarStatus>

    @GET("api/v1/cars/{carId}/charges")
    suspend fun getCharges(
        @Path("carId") carId: Int,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ChargesResponse>

    @GET("api/v1/cars/{carId}/drives")
    suspend fun getDrives(
        @Path("carId") carId: Int,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<DrivesResponse>

    @GET("api/v1/cars/{carId}/battery-health")
    suspend fun getBatteryHealth(@Path("carId") carId: Int): Response<BatteryHealth>
}
```

---

## App Screens & Features

### 1. Settings / Onboarding (First Launch)

**Purpose:** Configure connection to TeslamateApi instance

**UI Elements:**
- Server URL input field (e.g., `https://teslamate.example.com`)
- API Token input field (password masked)
- "Test Connection" button
- Save button

**Behavior:**
- Validates URL format
- Tests connection with `/api/ping`
- Stores credentials in encrypted DataStore
- Navigates to Dashboard on success

---

### 2. Dashboard Screen

**Purpose:** At-a-glance vehicle status

**UI Elements:**
- Vehicle name and image/icon
- Battery level with circular progress indicator
- Charging state (charging, not charging, plugged in)
- Current location (if available)
- Odometer reading
- Inside/outside temperature
- Last seen timestamp
- Quick stats cards:
  - Today's drives (count + distance)
  - This month's charges (count + kWh)
  - Efficiency (Wh/km)

**Data Source:** `GET /api/v1/cars/:id/status`

---

### 3. Charges Screen

**Purpose:** Charging history with statistics

**UI Elements:**
- Summary card at top:
  - Total charges count
  - Total energy added (kWh)
  - Average charge cost (if available)
  - Total cost (if available)
- Filter chips: Last 7 days / 30 days / 90 days / All time
- Line chart: Energy added over time
- List of charge sessions:
  - Date/time
  - Location
  - Energy added (kWh)
  - Duration
  - Cost (if available)
  - Start/end battery %

**Data Source:** `GET /api/v1/cars/:id/charges`

**Detail Screen:**
- Full charge session details
- Charge curve chart (if data available)
- Cost breakdown

---

### 4. Drives Screen

**Purpose:** Trip history with efficiency metrics

**UI Elements:**
- Summary card:
  - Total drives count
  - Total distance
  - Average efficiency (Wh/km)
  - Total duration
- Filter chips: Last 7 days / 30 days / 90 days / All time
- Bar chart: Daily/weekly distance
- List of drives:
  - Date/time
  - Start → End location
  - Distance
  - Duration
  - Efficiency (Wh/km)
  - Battery used %

**Data Source:** `GET /api/v1/cars/:id/drives`

**Detail Screen:**
- Full drive details
- Route map (if coordinates available)
- Speed/efficiency graphs

---

### 5. Battery Health Screen

**Purpose:** Monitor battery degradation over time

**UI Elements:**
- Current battery health percentage
- Original vs current capacity
- Line chart: Battery health over time
- Statistics:
  - Total charge cycles
  - Battery age
  - Degradation rate

**Data Source:** `GET /api/v1/cars/:id/battery-health`

---

### 6. Software Updates Screen

**Purpose:** Track software update history

**UI Elements:**
- Current software version
- List of past updates:
  - Version number
  - Update date
  - Time between updates

**Data Source:** `GET /api/v1/cars/:id/updates`

---

## Implementation Phases

### Phase 1: Foundation (MVP)
1. ✅ Create plan document
2. Project scaffolding with Gradle
3. Implement Settings screen (server config)
4. Basic API client with Retrofit
5. Dashboard screen with vehicle status
6. Basic error handling and loading states

**Deliverable:** App that connects to TeslamateApi and shows vehicle status

---

### Phase 2: Core Features
1. Charges screen with list and summary stats
2. Drives screen with list and summary stats
3. Pull-to-refresh functionality
4. Date filtering for charges/drives
5. Detail screens for individual charges/drives

**Deliverable:** Full browsing of charge and drive history

---

### Phase 3: Visualizations
1. Integrate Vico charting library
2. Charge history line chart
3. Drive distance bar chart
4. Battery level trends
5. Efficiency trends

**Deliverable:** Rich data visualizations

---

### Phase 4: Battery & Updates
1. Battery health screen with degradation tracking
2. Software updates history screen
3. Battery health trend chart

**Deliverable:** Complete vehicle health monitoring

---

### Phase 5: Polish & Extras
1. Dark/light theme with Material You
2. Multi-vehicle support (vehicle selector)
3. Offline caching with Room database
4. Widget for home screen (battery status)
5. Notifications for charge completion (optional)
6. Vehicle commands (wake, etc.) - requires careful consideration

**Deliverable:** Production-ready polished app

---

## Dependencies (build.gradle.kts)

```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // DataStore (encrypted preferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Charts
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.9")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

---

## Design Guidelines

### Visual Style
- Material Design 3 (Material You)
- Support dynamic color theming on Android 12+
- Dark and light mode support
- Tesla-inspired color accents (red for Model S/X, blue for Model 3/Y)

### Color Palette
```
Primary: #E31937 (Tesla Red)
Secondary: #171A20 (Tesla Dark)
Surface: Material default
On-Surface: Material default
Accent Blue: #3E6AE1 (for Model 3/Y)
Success: #4CAF50 (battery full, charge complete)
Warning: #FF9800 (low battery)
Error: #F44336 (connection issues)
```

### Typography
- Roboto (Android default)
- Large numbers for key metrics
- Clear hierarchy with Material type scale

### Icons
- Material Icons (filled style)
- Custom battery/charging icons where needed

---

## Security Considerations

1. **Token Storage:** Use EncryptedSharedPreferences via AndroidX Security
2. **Network Security:** Enforce HTTPS (network security config)
3. **No Sensitive Logging:** Mask tokens in debug logs
4. **Certificate Pinning:** Optional for self-hosted instances

---

## Testing Strategy

Testing will be set up from Phase 1 and expanded as features are added.

### Unit Tests (src/test/)
**What:** Test business logic in isolation without Android framework

| Component | What to Test |
|-----------|--------------|
| ViewModels | State updates, error handling, data transformations |
| Repositories | Data fetching logic, caching behavior |
| Use Cases | Business rules, calculations (efficiency, totals) |
| API Models | JSON parsing, null handling |

**Tools:**
- JUnit 5 - Test framework
- MockK - Kotlin-friendly mocking
- Kotlinx Coroutines Test - Testing suspend functions
- Turbine - Testing Kotlin Flows

**Example:**
```kotlin
@Test
fun `dashboard shows error state when API fails`() = runTest {
    // Given
    coEvery { repository.getCarStatus(any()) } throws IOException()

    // When
    viewModel.loadDashboard()

    // Then
    assertIs<DashboardState.Error>(viewModel.state.value)
}
```

### Integration Tests (src/androidTest/)
**What:** Test components working together with real Android framework

| Test | Purpose |
|------|---------|
| API Client | Verify Retrofit correctly parses real API responses |
| Repository | Test data flow from API to domain models |
| DataStore | Verify settings persistence |

**Tools:**
- MockWebServer - Fake HTTP server for API tests
- Hilt Testing - DI in tests

### UI Tests (src/androidTest/)
**What:** Test Compose UI behavior and navigation

**Tools:**
- Compose UI Test - Find elements, perform clicks, verify state
- Navigation Testing - Verify screen transitions

**Example:**
```kotlin
@Test
fun settingsScreen_validUrl_enablesSaveButton() {
    composeTestRule.setContent {
        SettingsScreen()
    }

    composeTestRule
        .onNodeWithTag("urlInput")
        .performTextInput("https://teslamate.example.com")

    composeTestRule
        .onNodeWithTag("saveButton")
        .assertIsEnabled()
}
```

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run unit tests with coverage report
./gradlew testDebugUnitTest jacocoTestReport

# Run instrumented tests (requires emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.matedroid.ui.DashboardViewModelTest"
```

---

## Development Workflow

### Prerequisites
- Android Studio installed ✓
- Android Emulator configured ✓
- Java 17 installed

### Daily Development Cycle

#### 1. Start the Emulator
**Option A - Android Studio:**
1. Open Android Studio
2. **Tools → Device Manager**
3. Click the play button next to your emulator

**Option B - Command Line:**
```bash
# List available emulators
emulator -list-avds

# Start emulator (replace with your AVD name)
emulator -avd Pixel_7_API_34 &
```

#### 2. Build and Run the App

**Option A - Android Studio (Recommended for beginners):**
1. Open the `matedroid` folder in Android Studio
2. Wait for Gradle sync (bottom progress bar)
3. Click the green **Run ▶** button (top toolbar)
4. Select your emulator from the dropdown
5. App launches automatically

**Option B - Terminal:**
```bash
# Build and install debug APK
./gradlew installDebug

# Launch the app
adb shell am start -n com.matedroid/.MainActivity
```

#### 3. See Changes

**Hot Reload (Compose Preview):**
- In Android Studio, Compose `@Preview` functions render live
- Changes to UI code update instantly in the preview pane
- No need to rebuild for visual tweaks

**Apply Changes (Running App):**
- Android Studio: Click **Apply Changes** (⚡ button) for code changes
- Or **Apply Code Changes** (⚡⚡) for structural changes
- Full rebuild only needed for manifest/resource changes

**Manual Rebuild:**
```bash
# Rebuild and reinstall
./gradlew installDebug
```

#### 4. View Logs

**Android Studio:**
- **View → Tool Windows → Logcat**
- Filter by app: Select `com.matedroid` from dropdown

**Terminal:**
```bash
# All logs from the app
adb logcat | grep -i matedroid

# Or with pidcat (cleaner output, install separately)
pidcat com.matedroid
```

### Android Studio Tips for Beginners

| Task | How |
|------|-----|
| Open project | File → Open → Select `matedroid` folder |
| Run app | Green ▶ button or `Shift+F10` |
| Stop app | Red ■ button or `Ctrl+F2` |
| View logs | View → Tool Windows → Logcat |
| Compose preview | Open a file with `@Preview`, see right panel |
| Rebuild project | Build → Rebuild Project |
| Sync Gradle | Click "Sync Now" when prompted, or File → Sync Project |
| Find files | Double-tap `Shift`, then type filename |
| Find in files | `Ctrl+Shift+F` |

### Inspecting the Emulator

The emulator behaves like a real phone:
- **Swipe** to navigate
- **Click** to tap
- **Extended Controls** (... button): Simulate location, battery, network conditions
- **Screenshot**: Click camera icon in emulator toolbar
- **Screen Recording**: Click video icon for recordings

### Debugging

1. Set breakpoints: Click in the gutter (left of line numbers)
2. Run in debug mode: Click **Debug** (bug icon) instead of Run
3. Inspect variables when breakpoint hits
4. Step through code with F8 (step over) / F7 (step into)

---

## Future Considerations (Out of Scope for v1)

- iOS version with Kotlin Multiplatform
- Wear OS companion app
- Android Auto integration
- Geofencing (notifications when arriving/leaving locations)
- Integration with home automation (MQTT)
- Cost tracking with electricity rates
- Comparison with other vehicles

---

## Resources

- [TeslamateApi Documentation](https://github.com/tobiasehlert/teslamateapi)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Vico Charts](https://patrykandpatrick.com/vico/)
- [Android Developers Guide](https://developer.android.com/guide)

---

## Getting Started (Next Steps)

1. Install Java 17 and Android SDK command-line tools
2. Initialize the project structure
3. Configure Gradle build files
4. Implement the Settings screen for API configuration
5. Create the API client and test connection
6. Build the Dashboard screen

Ready to proceed with implementation when you are!

---

# Feature: Stats for Nerds (v0.8.0)

## Overview

Add a "Stats for Nerds" screen accessible by tapping the car image on the Dashboard. This feature provides advanced statistics and records computed from historical Teslamate data, stored locally in SQLite for fast access.

**Entry Point:** Tap the car image on Dashboard → navigates to Stats screen

**Visual Hint:** Small 📊 chart icon overlaid on the car image (right side)

## Architecture

### Two-Tier Data Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                     Stats for Nerds Screen                       │
├─────────────────────────────────────────────────────────────────┤
│  🚀 QUICK STATS (instant, from list endpoints)                  │
│  ✓ Available immediately after first sync                       │
├─────────────────────────────────────────────────────────────────┤
│  🔬 DEEP STATS (requires detail sync)                           │
│  ◐ Shows progress while syncing, N/A until complete             │
└─────────────────────────────────────────────────────────────────┘
         │                              │
         ▼                              ▼
┌─────────────────────┐    ┌─────────────────────────────────────┐
│ drives_summary      │    │ drive_detail_aggregates             │
│ charges_summary     │    │ charge_detail_aggregates            │
│ (~6.5 MB for 15k)   │    │ (~3.3 MB for 23k records)           │
└─────────────────────┘    └─────────────────────────────────────┘
         │                              │
         ▼                              ▼
┌─────────────────────┐    ┌─────────────────────────────────────┐
│ 2 API calls total   │    │ 1 API call per drive/charge         │
│ (list endpoints)    │    │ (detail endpoints, background)      │
└─────────────────────┘    └─────────────────────────────────────┘
```

### Sync Flow

```
App Launch
    │
    ├──► Start DataSyncWorker (background, all cars in parallel)
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│ Phase 1: Summary Sync (~5-10 seconds)                           │
│ • GET /cars                                                     │
│ • For each car (parallel):                                      │
│   • GET /drives → upsert all                                    │
│   • GET /charges → upsert all                                   │
│ • Quick stats now available!                                    │
└─────────────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│ Phase 2: Detail Sync (background)                               │
│ • For each car (parallel):                                      │
│   • Find unprocessed drives                                     │
│   • GET /drives/{id} → compute agg                              │
│   • Find unprocessed charges                                    │
│   • GET /charges/{id} → compute agg                             │
│ • Progress exposed via StateFlow                                │
│ • Resumable across app sessions                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Stats Categories & Metrics

### 🚗 Driving Records
| Stat | Source | Description |
|------|--------|-------------|
| Total Distance | Summary | Lifetime kilometers driven |
| Total Drives | Summary | Number of drives |
| Longest Drive | Summary | Single longest trip (km) |
| Max Speed | Summary | Highest speed recorded |
| Best Efficiency | Summary | Most efficient drive (Wh/km) |
| Worst Efficiency | Summary | Least efficient drive (Wh/km) |
| Highest Altitude | Detail | Peak elevation reached |
| Lowest Altitude | Detail | Lowest point visited |
| Most Elevation Gain | Detail | Single drive with most climbing |

### ⚡ Charging Records
| Stat | Source | Description |
|------|--------|-------------|
| Total Energy | Summary | Lifetime kWh charged |
| Total Cost | Summary | Lifetime charging cost |
| Total Charges | Summary | Number of charge sessions |
| Biggest Charge | Summary | Most energy in single session |
| Most Expensive | Summary | Highest cost single charge |
| Max Charge Power | Detail | Peak kW achieved |
| AC Charges | Detail | Count of AC (slow) charges |
| DC Charges | Detail | Count of DC (fast) charges |
| AC/DC Ratio | Detail | Percentage breakdown |

### 🌡️ Temperature Records
| Stat | Source | Description |
|------|--------|-------------|
| Hottest Drive | Detail | Max outside temp while driving |
| Coldest Drive | Detail | Min outside temp while driving |
| Hottest Cabin | Detail | Max inside temp recorded |
| Coldest Cabin | Detail | Min inside temp recorded |
| Hottest Charge | Detail | Max temp during charging |
| Coldest Charge | Detail | Min temp during charging |

### 📅 Activity Stats
| Stat | Source | Description |
|------|--------|-------------|
| Busiest Day | Summary | Date with most drives |
| Drives on Busiest | Summary | Count on busiest day |
| Average Daily Distance | Summary | Avg km per day with drives |
| Average Drive Duration | Summary | Avg minutes per drive |
| Average Charge Duration | Summary | Avg minutes per charge |
| First Drive | Summary | Date of first recorded drive |
| Days Since First Drive | Summary | Total days of ownership |

### 🔋 Energy Stats
| Stat | Source | Description |
|------|--------|-------------|
| Total Energy Used | Summary | Lifetime kWh consumed driving |
| Average Efficiency | Summary | Overall Wh/km |
| Energy per Day | Summary | Avg kWh consumed per driving day |

---

## Database Schema

### Room Entities

```kotlin
// Sync state tracking per car
@Entity(tableName = "sync_state")
data class SyncState(
    @PrimaryKey val carId: Int,
    val lastDriveSyncAt: Long = 0,
    val lastChargeSyncAt: Long = 0,
    val lastDriveDetailId: Int = 0,
    val lastChargeDetailId: Int = 0,
    val detailSchemaVersion: Int = 1,
    val totalDrivesToProcess: Int = 0,
    val totalChargesToProcess: Int = 0,
    val drivesProcessed: Int = 0,
    val chargesProcessed: Int = 0
)

// Drive list data (from /drives endpoint)
@Entity(tableName = "drives_summary")
data class DriveSummary(
    @PrimaryKey val driveId: Int,
    val carId: Int,
    val startDate: String,
    val endDate: String,
    val startAddress: String,
    val endAddress: String,
    val distance: Double,           // km
    val durationMin: Int,
    val speedMax: Int,              // km/h
    val speedAvg: Int,
    val powerMax: Int,              // kW
    val powerMin: Int,
    val startBatteryLevel: Int,
    val endBatteryLevel: Int,
    val outsideTempAvg: Double?,
    val insideTempAvg: Double?,
    val energyConsumed: Double?,    // kWh
    val efficiency: Double?         // Wh/km (computed)
)

// Charge list data (from /charges endpoint)
@Entity(tableName = "charges_summary")
data class ChargeSummary(
    @PrimaryKey val chargeId: Int,
    val carId: Int,
    val startDate: String,
    val endDate: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val energyAdded: Double,        // kWh
    val energyUsed: Double?,
    val cost: Double?,
    val durationMin: Int,
    val startBatteryLevel: Int,
    val endBatteryLevel: Int,
    val outsideTempAvg: Double?,
    val odometer: Double
)

// Aggregated data from drive details (from /drives/{id} endpoint)
@Entity(tableName = "drive_detail_aggregates")
data class DriveDetailAggregate(
    @PrimaryKey val driveId: Int,
    val carId: Int,
    val schemaVersion: Int,
    val computedAt: Long,

    // Elevation
    val maxElevation: Int?,
    val minElevation: Int?,
    val elevationGain: Int?,        // Sum of positive deltas
    val elevationLoss: Int?,        // Sum of negative deltas
    val hasElevationData: Boolean,

    // Temperature extremes
    val maxInsideTemp: Double?,
    val minInsideTemp: Double?,
    val maxOutsideTemp: Double?,
    val minOutsideTemp: Double?,

    // Power extremes
    val maxPower: Int?,
    val minPower: Int?,             // Most regen

    // Climate
    val climateOnPositions: Int,    // Count of positions with climate on

    // Metadata
    val positionCount: Int,

    // Future extensibility
    val extraJson: String? = null
)

// Aggregated data from charge details (from /charges/{id} endpoint)
@Entity(tableName = "charge_detail_aggregates")
data class ChargeDetailAggregate(
    @PrimaryKey val chargeId: Int,
    val carId: Int,
    val schemaVersion: Int,
    val computedAt: Long,

    // Charger info
    val isFastCharger: Boolean,
    val fastChargerBrand: String?,
    val connectorType: String?,

    // Power extremes
    val maxChargerPower: Int?,
    val maxChargerVoltage: Int?,
    val maxChargerCurrent: Int?,
    val chargerPhases: Int?,

    // Temperature
    val maxOutsideTemp: Double?,
    val minOutsideTemp: Double?,

    // Metadata
    val chargePointCount: Int,

    // Future extensibility
    val extraJson: String? = null
)
```

### Indexes

```kotlin
@Entity(
    tableName = "drives_summary",
    indices = [
        Index(value = ["carId"]),
        Index(value = ["carId", "startDate"])
    ]
)

@Entity(
    tableName = "charges_summary",
    indices = [
        Index(value = ["carId"]),
        Index(value = ["carId", "startDate"])
    ]
)
```

---

## File Structure

### New Files

```
app/src/main/java/com/matedroid/
├── data/
│   ├── local/
│   │   ├── StatsDatabase.kt                    # Room database
│   │   ├── Converters.kt                       # Type converters
│   │   ├── entity/
│   │   │   ├── SyncState.kt
│   │   │   ├── DriveSummary.kt
│   │   │   ├── ChargeSummary.kt
│   │   │   ├── DriveDetailAggregate.kt
│   │   │   └── ChargeDetailAggregate.kt
│   │   └── dao/
│   │       ├── SyncStateDao.kt
│   │       ├── DriveSummaryDao.kt
│   │       ├── ChargeSummaryDao.kt
│   │       └── AggregateDao.kt
│   ├── repository/
│   │   ├── SyncRepository.kt                   # Sync orchestration
│   │   └── StatsRepository.kt                  # Stats queries
│   └── sync/
│       ├── SyncManager.kt                      # Manages sync state/progress
│       └── DataSyncWorker.kt                   # WorkManager worker
├── domain/
│   └── model/
│       ├── QuickStats.kt                       # Stats from summaries
│       ├── DetailStats.kt                      # Stats from aggregates
│       ├── StatRecord.kt                       # A single record (value + context)
│       └── SyncProgress.kt                     # Sync status
└── ui/
    └── screens/
        └── stats/
            ├── StatsScreen.kt                  # Main screen
            ├── StatsViewModel.kt               # ViewModel
            └── components/
                ├── StatsHeader.kt              # Year filter + sync status
                ├── DrivingRecordsSection.kt    # 🚗 section
                ├── ChargingRecordsSection.kt   # ⚡ section
                ├── TemperatureRecordsSection.kt # 🌡️ section
                ├── ActivityStatsSection.kt     # 📅 section
                ├── EnergyStatsSection.kt       # 🔋 section
                ├── StatCard.kt                 # Individual stat card
                ├── RecordCard.kt               # Record with linked drive/charge
                └── SyncProgressCard.kt         # Shows sync progress
```

### Modified Files

```
app/build.gradle.kts                            # Add Room, WorkManager deps
app/src/main/java/com/matedroid/
├── MateDroidApplication.kt                     # Initialize WorkManager
├── di/
│   ├── AppModule.kt                            # Provide database
│   └── DatabaseModule.kt                       # New module for DB
├── ui/
│   ├── navigation/NavGraph.kt                  # Add stats route
│   └── screens/
│       └── dashboard/
│           ├── DashboardScreen.kt              # Add tap on car image
│           └── components/
│               └── CarImageCard.kt             # Add stats icon overlay
```

---

## Implementation Phases

### Phase 1: Database Foundation
**Scope:** Set up Room database with all entities and DAOs

**Tasks:**
1. Add Room and WorkManager dependencies to `build.gradle.kts`
2. Create entity classes: `SyncState`, `DriveSummary`, `ChargeSummary`, `DriveDetailAggregate`, `ChargeDetailAggregate`
3. Create DAO interfaces with CRUD operations and stat queries
4. Create `StatsDatabase` class with Room configuration
5. Create `DatabaseModule` for Hilt dependency injection
6. Add database instance to Hilt graph

**Deliverable:** Compiling database layer ready for use

### Phase 2: Sync Infrastructure
**Scope:** Background sync system for fetching and storing data

**Tasks:**
1. Create `SyncManager` class to track sync state and emit progress
2. Create `SyncRepository` with sync orchestration logic:
   - `syncSummaries(carId)` - fetch lists, upsert to DB
   - `syncDriveDetails(carId)` - iterate drives, fetch details, compute/store aggregates
   - `syncChargeDetails(carId)` - iterate charges, fetch details, compute/store aggregates
3. Create `DataSyncWorker` (WorkManager CoroutineWorker):
   - Enqueue on app start with unique work name per car
   - Run all cars in parallel using coroutineScope
   - Report progress via WorkInfo
4. Implement incremental sync logic (compare last IDs)
5. Implement schema version checking for reprocessing
6. Add throttling between API calls (100ms delay)
7. Handle errors with exponential backoff
8. Initialize sync on app launch in `MateDroidApplication`

**Deliverable:** Background sync running on app launch, resumable across sessions

### Phase 3: Stats Computation
**Scope:** Repository layer for computing stats from local database

**Tasks:**
1. Create domain models: `QuickStats`, `DetailStats`, `StatRecord`, `SyncProgress`
2. Create `StatsRepository` with query methods:
   - `getQuickStats(carId, year?)` - SQL aggregations on summaries
   - `getDetailStats(carId, year?)` - SQL aggregations on detail aggregates
   - `getDrivingRecords(carId, year?)` - records with drive context
   - `getChargingRecords(carId, year?)` - records with charge context
   - `getTemperatureRecords(carId, year?)`
   - `getActivityStats(carId, year?)`
   - `getEnergyStats(carId, year?)`
3. Implement year filtering with date range WHERE clauses
4. Implement AC/DC ratio computation from charge aggregates
5. Implement "busiest day" query (GROUP BY date, ORDER BY count)

**Deliverable:** Complete stats computation layer with year filtering

### Phase 4: Stats UI - Screen & ViewModel
**Scope:** Main stats screen structure and state management

**Tasks:**
1. Create `StatsViewModel`:
   - Load quick stats immediately
   - Observe sync progress from SyncManager
   - Load detail stats when sync completes
   - Handle year filter changes
   - Expose `UiState` with all sections
2. Create `StatsScreen` layout:
   - Top bar with title and back navigation
   - Year filter dropdown (All time, 2024, 2023, ...)
   - Sync progress indicator when syncing
   - Scrollable column with sections
3. Create `StatsHeader` composable:
   - Year dropdown menu
   - Sync status indicator (percentage or checkmark)
4. Add navigation route in `NavGraph.kt`

**Deliverable:** Navigable stats screen with year filtering and sync status

### Phase 5: Stats UI - Section Components
**Scope:** Individual stat cards and section layouts

**Tasks:**
1. Create `StatCard` composable:
   - Icon + title row
   - Large value display
   - Optional subtitle (context)
   - N/A state with "Analyzing..." during sync
   - Support for progress indicator
2. Create `RecordCard` composable:
   - Tappable to navigate to drive/charge detail
   - Shows date, location, value
3. Create section composables with emoji headers:
   - `DrivingRecordsSection` 🚗
   - `ChargingRecordsSection` ⚡
   - `TemperatureRecordsSection` 🌡️
   - `ActivityStatsSection` 📅
   - `EnergyStatsSection` 🔋
4. Create `AcDcRatioCard` with visual bar representation
5. Style all cards to match existing app visual language

**Deliverable:** Complete, styled stats screen with all sections

### Phase 6: Dashboard Integration
**Scope:** Navigation entry point from Dashboard

**Tasks:**
1. Modify `CarImageCard` component:
   - Add 📊 icon overlay (positioned right side)
   - Make entire card clickable
   - Navigate to stats screen on tap
2. Pass carId to stats screen via navigation argument
3. Handle navigation in `DashboardScreen`

**Deliverable:** Tappable car image navigates to stats

### Phase 7: Polish & Edge Cases
**Scope:** Error handling, edge cases, and refinements

**Tasks:**
1. Handle empty state (no drives/charges yet)
2. Handle API errors during sync (show retry option)
3. Add "Clear stats cache" option in Settings
4. Add "Resync now" option in Stats screen (long press or menu)
5. Optimize slow queries with EXPLAIN QUERY PLAN
6. Add appropriate indexes
7. Test with large datasets (6500+ drives)
8. Update CHANGELOG.md
9. Update README.md with new feature

**Deliverable:** Production-ready feature

---

## UI Design

### Stats Screen Layout

```
┌─────────────────────────────────────────────────────────────────┐
│ ← Stats for Nerds                              [All time ▼] 🔄  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  🚗 DRIVING RECORDS                                             │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │ 🏁 Total        │ │ 📏 Longest      │ │ ⚡ Max Speed    │   │
│  │ 847,293 km      │ │ 892 km          │ │ 215 km/h       │   │
│  │ 6,547 drives    │ │ Alps Road Trip  │ │ Autobahn A8    │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │ ⛰️ Highest      │ │ 🏔️ Lowest       │ │ 🌿 Best Eff.   │   │
│  │ 2,847 m         │ │ -12 m           │ │ 98 Wh/km       │   │
│  │ Col du Galibier │ │ Dead Sea Trip   │ │ Coasting home  │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
│                                                                  │
│  ⚡ CHARGING RECORDS                                            │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │ 🔌 Total        │ │ 💰 Total Cost   │ │ ⚡ Max Power    │   │
│  │ 45,892 kWh      │ │ €4,521          │ │ 250 kW         │   │
│  │ 2,891 charges   │ │ €0.10/kWh avg   │ │ Ionity Munich  │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
│  ┌─────────────────────────────────────┐                        │
│  │ 🔄 AC/DC Split                      │                        │
│  │ ████████████░░░░░ 72% AC (2,082)    │                        │
│  │ ░░░░░░░░░░░░█████ 28% DC (809)      │                        │
│  └─────────────────────────────────────┘                        │
│                                                                  │
│  🌡️ TEMPERATURE RECORDS                                        │
│  ┌─────────────────┐ ┌─────────────────┐                        │
│  │ 🔥 Hottest      │ │ 🥶 Coldest      │                        │
│  │ 42°C outside    │ │ -18°C outside   │                        │
│  │ Summer 2023     │ │ Winter 2024     │                        │
│  └─────────────────┘ └─────────────────┘                        │
│                                                                  │
│  📅 ACTIVITY STATS                                              │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │ 📆 Busiest Day  │ │ 📊 Avg Daily    │ │ ⏱️ Avg Drive    │   │
│  │ 8 drives        │ │ 47 km           │ │ 28 min         │   │
│  │ 2024-03-15      │ │                 │ │                 │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
│                                                                  │
│  🔋 ENERGY STATS                                                │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │ ⚡ Total Used   │ │ 📈 Avg Eff.     │ │ 📊 Per Day      │   │
│  │ 142,847 kWh     │ │ 168 Wh/km       │ │ 12.4 kWh        │   │
│  │                 │ │                 │ │                 │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Stat Card States

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ ⛰️ Highest      │  │ ⛰️ Highest      │  │ ⛰️ Highest      │
│ 2,847 m         │  │ Analyzing...    │  │ N/A             │
│ Col du Galibier │  │ ████░░░░ 45%    │  │ No data         │
│      ✓ Ready    │  │   ⟳ Syncing     │  │   ─ Unavailable │
└─────────────────┘  └─────────────────┘  └─────────────────┘
     Complete           In Progress           No Data
```

### Year Filter Dropdown

```
┌────────────────┐
│ All time     ▼ │
├────────────────┤
│ ● All time     │
│ ○ 2025         │
│ ○ 2024         │
│ ○ 2023         │
│ ○ 2022         │
└────────────────┘
```

---

## Dependencies to Add

```kotlin
// build.gradle.kts (app)
dependencies {
    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Hilt WorkManager integration
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
}
```

---

## Schema Versioning Strategy

### Handling New Fields

When adding new aggregate fields in the future:

1. **Increment `CURRENT_SCHEMA_VERSION`** in code
2. **Add new fields with nullable defaults** to entity
3. **Add Room migration** for database schema
4. **Sync worker automatically reprocesses** records where `schemaVersion < CURRENT_SCHEMA_VERSION`

```kotlin
object SchemaVersion {
    const val CURRENT = 1

    // Changelog:
    // V1 (initial): elevation, temp extremes, power, climate, charger info
    // V2 (future): battery_temp_max, battery_temp_min
    // V3 (future): regen_energy_total
}

// Query for records needing reprocessing:
@Query("""
    SELECT d.driveId FROM drives_summary d
    LEFT JOIN drive_detail_aggregates a ON d.driveId = a.driveId
    WHERE d.carId = :carId
    AND (a.driveId IS NULL OR a.schemaVersion < :currentVersion)
    ORDER BY d.driveId
""")
suspend fun getUnprocessedDrives(carId: Int, currentVersion: Int): List<Int>
```

### Future-Proofing

- **Capture all plausible extremes upfront** to minimize need for reprocessing
- **Use `extraJson` field** for experimental/rare data without schema changes
- **Selective reprocessing** only affects outdated records, not all data

---

## Storage Estimates

| Table | Records (15k drives, 8k charges) | Size/Record | Total |
|-------|----------------------------------|-------------|-------|
| `sync_state` | 1-5 (per car) | 100 bytes | ~0.5 KB |
| `drives_summary` | 15,000 | 300 bytes | 4.5 MB |
| `charges_summary` | 8,000 | 250 bytes | 2.0 MB |
| `drive_detail_aggregates` | 15,000 | 150 bytes | 2.3 MB |
| `charge_detail_aggregates` | 8,000 | 120 bytes | 1.0 MB |
| **Total** | | | **~10 MB** |

For comparison: A single high-res photo is 3-5 MB. This is negligible.

---

## Sync Time Estimates

### Phase 1: Summary Sync (Fast)
- 2 API calls per car (drives list + charges list)
- ~5-10 seconds total regardless of data size

### Phase 2: Detail Sync (Slow, Background)

| Drives | Time @ 500ms/call | Notes |
|--------|-------------------|-------|
| 1,000 | ~8 minutes | Light user |
| 6,500 | ~54 minutes | Your data |
| 15,000 | ~2 hours | Heavy user |

**Mitigation:**
- Runs entirely in background
- Quick stats available immediately
- Progress shown in UI
- Resumable across app sessions
- Incremental after initial sync (seconds for daily use)

---

## Testing Strategy

### Unit Tests
- DAO queries return correct results
- Aggregate computation is accurate (elevation gain, etc.)
- Stats calculations handle edge cases (nulls, zeros)
- Schema migrations preserve data

### Integration Tests
- Sync worker completes successfully
- Progress reporting is accurate
- Incremental sync only processes new items
- Schema version upgrade triggers reprocessing

### Manual Testing
- Test with real 6500+ drive dataset
- Verify memory usage stays reasonable
- Verify battery impact is minimal
- Test sync resume after app kill

---

## Future Enhancements

1. **Trend Charts**: Add historical trend visualization (efficiency over time, monthly distance)
2. **Global Stats**: Aggregate across all cars for multi-car owners
3. **Achievements/Badges**: Gamification ("1000 km club", "DC warrior", "Early Bird")
4. **Export Stats**: Share stats as image or text
5. **Comparisons**: Compare stats between years or cars
6. **Widgets**: Home screen widget showing key stats
