# Qlic Native Android

Native Android application for Qlic water monitoring, built with Kotlin and Jetpack Compose.

## Current scope

- Native Android application with Kotlin, Jetpack Compose and Material 3.
- English and Latin American Spanish (`es-419`) with persistent language selection.
- Sign-in and account registration against the deployed Qlic API.
- JWT session protected with Android Keystore encryption and DataStore persistence.
- Authenticated navigation: Dashboard, Alerts, Reports, Business and More.
- Devices and Tanks are grouped under Business; Support and Account are grouped under More.

## Requirements

- Android Studio with JDK 17 or newer.
- Android SDK 37.
- An emulator or physical device running Android 8.0 (API 26) or newer.

## Run locally

Open this folder in Android Studio, allow Gradle synchronization to finish, select a device and run the `app` configuration.

The debug build can also be verified from PowerShell:

```powershell
$env:TEMP='C:\Android\Temp'
$env:TMP='C:\Android\Temp'
.\gradlew.bat testDebugUnitTest lintDebug connectedDebugAndroidTest assembleDebug
```

When the checkout is inside OneDrive, generated build files are written to `C:\Android\Build\qlic-frontend-mobile` to prevent synchronization locks. Set `QLIC_ANDROID_BUILD_ROOT` to use a different local build directory.

## Backend

The debug and release builds use:

`https://qlic-backend-api.onrender.com/`

Authentication endpoints:

- `POST /api/v1/authentication/sign-in`
- `POST /api/v1/authentication/sign-up`

## Security

- Cleartext HTTP traffic is disabled.
- Access tokens are never logged or committed.
- JWT values are encrypted with an AES-GCM key held by Android Keystore.
- Backups are disabled to avoid exporting local session data.
- Release builds enable shrinking and obfuscation.
