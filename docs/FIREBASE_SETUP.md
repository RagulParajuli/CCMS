# Firebase Authentication setup

CCMS uses **Firebase Authentication** for:

- email + password sign-up and sign-in
- Google sign-in (Credential Manager → Google ID token → Firebase credential)
- password reset emails
- email verification emails

The app compiles and runs without Firebase configured, but every auth action shows a setup
message until you complete the steps below.

## 1. Create the Firebase project

1. Open the [Firebase console](https://console.firebase.google.com/) and create a project.
2. Add an **Android** app with the package name `com.ragul.ccms`.
3. Give it a nickname (for example, `CCMS debug`).

## 2. Register your signing fingerprints

Firebase needs the SHA-1 (and preferably SHA-256) of the certificate used to sign the app.
This is the step that most often causes `DEVELOPER_ERROR` / `10:` during Google Sign-In.

For the debug keystore on Windows:

```powershell
keytool -list -v `
  -keystore "$env:USERPROFILE\.android\debug.keystore" `
  -alias androiddebugkey `
  -storepass android `
  -keypass android
```

The fingerprints of this workstation's debug key are:

```
SHA1:   7F:F7:1D:B2:FA:80:51:43:B9:08:43:04:85:47:55:97:ED:82:AF:08
SHA256: 5B:81:B2:B0:0A:0F:28:D3:6B:CC:66:40:6E:B5:55:9D:35:2E:71:7E:AA:83:5B:79:59:35:85:70:F6:41:7E:2E
```

> **Manual Firebase Console action:** copy both values into
> **Project settings → Your apps → com.ragul.ccms → Add fingerprint**, then save.
> Repeat with the release keystore before publishing. The fingerprints are not stored in
> `google-services.json`, so no re-download is needed after adding them.

You can also print the values Gradle uses with:

```powershell
.\gradlew.bat :app:signingReport
```

## 3. Enable the sign-in providers

In **Authentication → Sign-in method**, enable:

- **Email/Password**
- **Google** (choose a support email and save)

> **Manual Firebase Console action:** both providers must be enabled in the console. The app
> cannot enable them. Email/password keeps working even if Google is still disabled.

## 4. Add `google-services.json`

Download `google-services.json` from **Project settings → Your apps** and place it at:

```
app/google-services.json
```

The Gradle build applies the `com.google.gms.google-services` plugin only when this file
exists, so the project still compiles before Firebase is configured.

The file must contain an `oauth_client` entry with `"client_type": 3` (the Web client).
An empty `oauth_client` array is the reason the login screen shows
"Google Sign-In is not configured" even though Firebase itself initialises correctly. The
copy currently in `app/` includes the Web client for project `ccms-d6d89`.

Rebuild after replacing the file:

```powershell
.\gradlew.bat :app:assembleDebug
```

## 5. Google Web Client ID

Credential Manager needs an OAuth **Web** client ID. When the Firebase project has a Web
OAuth client (Firebase creates one for the Google provider), the google-services plugin
generates the `default_web_client_id` string resource automatically.

If your project does not generate it, copy the **Web client ID** from
**Authentication → Google → Web SDK configuration** and add it to `gradle.properties`:

```properties
GOOGLE_WEB_CLIENT_ID=1234567890-xxxxxxxxxxxxxxxxxxxxxxxx.apps.googleusercontent.com
```

Then rebuild. The value is exposed as `BuildConfig.GOOGLE_WEB_CLIENT_ID` and used only when
the generated resource is absent.

## 6. Verify the flow

1. Install the debug build on a device or emulator with Google Play services.
2. **Email/password** — create an account, check the verification email, then sign out and
   sign back in.
3. **Google** — tap *Continue with Google*, choose an account, and confirm that the Home
   screen shows "Signed in with Google".
4. **Password reset** — use *Forgot password?* and confirm the reset email arrives.

## Troubleshooting

| Symptom | Fix |
| --- | --- |
| "Firebase is not configured" | `app/google-services.json` is missing or the app was not rebuilt after adding it. |
| "Google Sign-In is not configured" | `app/google-services.json` has an empty `oauth_client`. Download a fresh file after enabling Google, or set `GOOGLE_WEB_CLIENT_ID`, then rebuild. |
| `DEVELOPER_ERROR` / `10:` from Google | The package name or SHA-1 in Firebase does not match the installed build. Register the debug SHA-1/SHA-256 shown in section 2. |
| Google account picker shows no accounts | Add a Google account to the device and confirm Play services is installed. `NoCredentialException` can also mean the SHA-1 is not registered. |
| "This sign-in method is not enabled" | Enable Email/Password or Google in the Firebase console. |
| Google account picker does not appear | Use an emulator/device with Google Play services and make sure the device has a Google account. |
