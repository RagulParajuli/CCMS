# AGENTS.md

## Project

CCMS is a single-module Android app built with Kotlin, Jetpack Compose, and Material 3.

- Namespace / application ID: `com.ragul.ccms`
- Module: `:app`
- UI: Compose only (no XML layouts), edge-to-edge `ComponentActivity`
- Screens: Login (unauthenticated start destination), Signup, Forgot password, then Dashboard
  (Membership, Loan or Deposit, Monthly Due, About App, History) after sign-in
- Navigation: `androidx.navigation` `NavHost` with string routes in `Router.kt` for auth.
  The signed-in dashboard switches sections in Compose state and exposes them through a
  responsive sidebar (modal drawer on phones, permanent drawer at >= 900dp).
- State: `AuthViewModel` exposes `StateFlow<AuthUiState>`; `RecordsViewModel` exposes record
  lists, live counts, history and snackbar messages as `StateFlow`s.
- Auth: Firebase Authentication for email/password, password reset and email verification.
  Google sign-in uses Credential Manager to obtain a Google ID token, which is exchanged for a
  Firebase credential. `FirebaseAuthRepository` is the only production `AuthRepository`.
- Records: local Room database (`ccms.db`) with per-account `ownerId` scoping. Dashboard cards,
  sidebar badges, module list in About and the pie chart all read the same `RecordCounts` flow.
  No record data is hardcoded. See `docs/DATABASE.md`.
- Firebase is optional at build time: the `com.google.gms.google-services` plugin is applied
  only when `app/google-services.json` exists. Without it the app still runs and shows a setup
  banner on the auth screens. Record screens still work because their data is local.

## Build & Test

Run commands from the project root (`E:\AndroidProjects\Using_Kotlin\jetpack_compose\CCMS`). Note the git repository root is `E:\AndroidProjects`, one level above the project.

```powershell
# Compile Kotlin
.\gradlew.bat :app:compileDebugKotlin

# Build debug APK
.\gradlew.bat :app:assembleDebug

# Unit tests (host)
.\gradlew.bat :app:testDebugUnitTest

# Instrumented tests (requires connected device/emulator)
.\gradlew.bat :app:connectedDebugAndroidTest

# Lint
.\gradlew.bat :app:lintDebug
```

Notes:

- The first build needs network access. `--offline` fails because `com.android.tools.build:aapt2:9.5.1-16666295` may not be cached.
- Gradle configuration cache is enabled in `gradle.properties`. If you hit configuration-cache serialization errors, rerun with `--no-configuration-cache`.
- `local.properties` (SDK path) is machine-specific and must not be committed.
- Toolchain: Gradle 9.6.0/9.8.2 wrapper, AGP 9.5.1, Kotlin 2.2.10, Compose BOM 2026.02.01, compileSdk/targetSdk 37, minSdk 24, Java 11. `gradle/gradle-daemon-jvm.properties` pins the daemon JVM to toolchain 25. Use a full JDK (with `jlink`) if `assembleDebug` fails in `JdkImageTransform`.
- Room uses KSP. `android.disallowKotlinSourceSets=false` is required in `gradle.properties` because AGP 9's built-in Kotlin rejects KSP's generated-source registration by default.
- Firebase: the `com.google.gms.google-services` plugin is applied only when `app/google-services.json` exists. The current `app/google-services.json` includes a Web OAuth client (`client_type: 3`), so the plugin generates `default_web_client_id` and the Google button is enabled. SHA-1/SHA-256 fingerprints still have to be registered in the Firebase console (values in `docs/FIREBASE_SETUP.md`). `GOOGLE_WEB_CLIENT_ID` remains an optional override.
- Full Firebase setup steps: `docs/FIREBASE_SETUP.md`. Local record database details: `docs/DATABASE.md`.

## Build State

`:app:compileDebugKotlin` passes with the Room/KSP setup. Use the commands above to run the
unit tests, lint and APK build. The current `app/google-services.json` includes the Web OAuth
client, so the Google setup banner is gone and Credential Manager has a Web client ID.
SHA-1/SHA-256 registration and the Google/Email providers are Firebase Console settings that
must be verified manually. See `docs/FIREBASE_SETUP.md`.

## Source Layout

```
app/src/main/java/com/ragul/ccms/
├── MainActivity.kt              # entry point, theme, session-aware NavHost
├── Router.kt                    # unauthenticated route constants
├── feature/
│   ├── auth/
│   │   ├── data/                # Firebase repository, Credential Manager, error mapping
│   │   ├── domain/              # UI-safe model, repository contract, validation, errors
│   │   └── presentation/        # AuthViewModel, UI state, Compose screens/components
│   └── records/
│       ├── data/                # Room DB, entities, DAO, repository, live counts
│       └── presentation/        # dashboard shell/sidebar, record screens, history, chart
└── ui/theme/                    # Color.kt, Theme.kt, Type.kt
```

Tests: `app/src/test/java/com/ragul/ccms/feature/auth/domain/AuthValidatorTest.kt` covers auth
validation and the user model. `feature/records/RecordModelsTest.kt` covers count totals and
loan/deposit model helpers. `ExampleUnitTest.kt` / `ExampleInstrumentedTest.kt` are placeholders.

Resources live in `app/src/main/res/` — notable drawables: `ic_leaf.png`, `ic_google.png`. Only
`app_name` is defined in `strings.xml`; UI text is hard-coded in composables.

### Package naming

All production code uses the app namespace (`com.ragul.ccms`, `com.ragul.ccms.feature.auth`,
`com.ragul.ccms.feature.records`, `com.ragul.ccms.ui.theme`). The old `com.ragul.authentication`
and `com.ragul.ccms.ui.auth` packages have been removed.

## Conventions

- Kotlin official code style (`kotlin.code.style=official`); 4-space indentation.
- Add dependencies through the version catalog: edit `gradle/libs.versions.toml`, then reference them as `libs.*` in `app/build.gradle.kts`.
- One screen composable per file; previews use `@PreviewLightDark` (Compose tooling).
- Screen composables are stateless: they take `AuthUiState` plus callbacks (`onSignIn`,
  `onSignUp`, `onGoogleSignIn`, navigation callbacks). `MainActivity` owns navigation and the
  Google/Credential Manager call. `AuthViewModel` owns validation, loading and error state.
- Shared visual language: light green auth background `AuthBackground` / `Color(0xFFCCFFCC)`,
  primary action `AuthPrimary` / `Color(0xFF0023FF)` with `RoundedCornerShape(8.dp)`, leaf +
  Google icons. Brand colors live in `ui/theme/Color.kt`; shared auth widgets live in
  `feature/auth/presentation/AuthComponents.kt`; record accents live in `Color.kt`
  (`RecordMembership`, `RecordLoanDeposit`, `RecordMonthlyDue`).
- Record data must go through `RecordsRepository` and be scoped by the signed-in Firebase
  `uid`. Never hardcode counts or record rows. History entries are written inside the same
  Room transaction as the record change.
- Compose screens in `feature/records` are stateless where practical; the dashboard host owns
  section state, the drawer and the `RecordsViewModel`.
- `CCMSTheme` is applied in `MainActivity` (`CCMSTheme { CCMSApp() }`) with the fixed green/blue
  palette; dynamic colour is opt-in via the `dynamicColor` parameter.
- Manifest theme is `@style/Theme.CCMS` (`android:Theme.Material.Light.NoActionBar`), and
  `MainActivity` uses `windowSoftInputMode="adjustResize"`. `INTERNET` and
  `ACCESS_NETWORK_STATE` permissions are declared for Firebase.
