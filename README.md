# CCMS

A single-module Android app built with Kotlin, Jetpack Compose and Material 3.

## Features

- Email + password sign-up and sign-in (Firebase Authentication)
- Google sign-in through Credential Manager + Firebase credential exchange
- Password reset emails
- Email verification with resend support
- Session persistence (Firebase restores the signed-in user on launch)
- Dashboard with live record counts for Membership, Loan or Deposit and Monthly Due
- Sidebar navigation (responsive drawer on phones, permanent drawer on wide screens)
- Add, edit and delete records; dashboard counts update automatically from the database
- Activity history and an About App section at the bottom of the dashboard
- Donut/pie chart showing the distribution of saved records
- Field validation, loading states and friendly Firebase error messages
- Graceful setup banner when Firebase has not been configured yet

## Tech stack

| Layer | Library |
| --- | --- |
| UI | Jetpack Compose + Material 3 |
| Navigation | `androidx.navigation:navigation-compose` |
| State | `ViewModel` + `StateFlow` |
| Auth | `firebase-auth` (BOM 34.19.0) |
| Google sign-in | `androidx.credentials` + `googleid` |
| Local database | Room (`room-runtime`, `room-ktx`, KSP) |
| Async | Kotlin coroutines + `Flow` |

## Getting started

1. Open the project in Android Studio.
2. Follow [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md) to create a Firebase project,
   register the app's SHA-1, enable Email/Password and Google providers, and place
   `google-services.json` in `app/`.
3. Build and install:

```powershell
.\gradlew.bat :app:assembleDebug
```

Authentication is organized by MVVM layer under `feature/auth`: `data/` contains the Firebase
repository and Credential Manager integration, `domain/` contains the auth model, repository
contract, validation and errors, and `presentation/` contains the `AuthViewModel`, UI state and
Compose screens.

The signed-in experience lives under `feature/records`: `data/` holds the Room database,
entities, DAOs and `RecordsRepository`, while `presentation/` holds the dashboard shell,
sidebar, record screens, history and the shared `RecordsViewModel`. All record screens observe
the same Room tables, so the dashboard counts are always the real row counts for the signed-in
account.

The app builds and runs without `google-services.json`; auth actions show a setup message
until Firebase is configured.

## Useful commands

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:lintDebug
```

## Project structure

```
app/src/main/java/com/ragul/ccms/
├── MainActivity.kt                  # theme, session-aware navigation, Google flow
├── Router.kt                        # login / signup / forgot-password routes
├── feature/
│   ├── auth/                        # Firebase auth, Credential Manager, auth UI
│   └── records/
│       ├── data/                    # Room database, entities, DAO, repository
│       └── presentation/            # dashboard, sidebar, record screens, chart
└── ui/theme/                        # colours, typography, theme
```
