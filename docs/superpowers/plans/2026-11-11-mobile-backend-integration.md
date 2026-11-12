# Qlic Mobile Backend Integration Implementation Plan

**Goal:** Connect the native Android application to all user-facing capabilities of the deployed Qlic API while preserving honest unavailable states for unsupported modules.

**Architecture:** Feature-oriented Kotlin/Compose layers backed by one authenticated Retrofit client, encrypted session persistence and manual dependency composition in `QlicApplication`. Dashboard data is aggregated from devices, alerts and subscription repositories. Unsupported backend modules never display fabricated values.

**Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Retrofit, OkHttp, Gson, DataStore, Android Keystore, CameraX 1.5.2, bundled ML Kit barcode scanning 17.3.0, JUnit and Compose UI testing.

**Design:** `docs/superpowers/specs/2026-11-11-mobile-backend-integration-design.md`

## Task 1: Extend and test the authenticated session model

**Files**

- Modify: `app/src/main/java/com/qlic/mobile/feature/auth/domain/AuthModels.kt`
- Modify: `app/src/main/java/com/qlic/mobile/feature/auth/data/SessionStore.kt`
- Modify: `app/src/main/java/com/qlic/mobile/feature/auth/data/EncryptedSessionStore.kt`
- Modify: `app/src/main/java/com/qlic/mobile/feature/auth/data/AuthRepositoryImpl.kt`
- Modify: `app/src/test/java/com/qlic/mobile/feature/auth/data/EncryptedSessionStoreTest.kt`
- Modify: `app/src/test/java/com/qlic/mobile/feature/auth/data/AuthRepositoryImplTest.kt`

**Steps**

1. Add failing tests proving that name, email, account ID, token type and expiration survive encrypted persistence.
2. Extend `AuthSession` and the DataStore keys without weakening token encryption.
3. Map sign-in identity fields into the stored session.
4. Run the two focused test classes and confirm they pass.

## Task 2: Build the session-aware network core

**Files**

- Create: `app/src/main/java/com/qlic/mobile/core/network/SessionSnapshot.kt`
- Create: `app/src/main/java/com/qlic/mobile/core/network/AuthInterceptor.kt`
- Create: `app/src/main/java/com/qlic/mobile/core/network/ApiProblem.kt`
- Create: `app/src/main/java/com/qlic/mobile/core/network/ApiCall.kt`
- Modify: `app/src/main/java/com/qlic/mobile/core/network/ApiClientFactory.kt`
- Modify: `app/src/main/java/com/qlic/mobile/QlicApplication.kt`
- Create: `app/src/test/java/com/qlic/mobile/core/network/AuthInterceptorTest.kt`
- Create: `app/src/test/java/com/qlic/mobile/core/network/ApiCallTest.kt`

**Steps**

1. Add failing tests for Authorization, `Accept-Language`, no-token behavior, ProblemDetail mapping and `401` signaling.
2. Add a thread-safe in-memory session/locale snapshot updated from application coroutines.
3. Add the interceptor without `runBlocking` or token logging.
4. Build one authenticated Retrofit instance alongside the public authentication API.
5. Clear the persisted session asynchronously when a `401` signal is received.
6. Run focused network tests and the existing authentication suite.

## Task 3: Implement the devices data and domain layers

**Files**

- Create: `app/src/main/java/com/qlic/mobile/feature/devices/data/DeviceApi.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/devices/data/DeviceDtos.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/devices/data/DeviceRepositoryImpl.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/devices/domain/DeviceModels.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/devices/domain/DeviceRepository.kt`
- Create: `app/src/test/java/com/qlic/mobile/feature/devices/data/DeviceRepositoryImplTest.kt`

**Steps**

1. Add failing repository tests for list, register, assign, empty, unauthorized and malformed-server cases.
2. Define DTOs with explicit serialized names and map all device statuses safely.
3. Ensure every account-scoped request obtains `accountId` from the session snapshot.
4. Implement repository calls and error mapping.
5. Run focused tests.

## Task 4: Implement the Devices UI and QR registration

**Files**

- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/qlic/mobile/feature/devices/presentation/DevicesViewModel.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/devices/presentation/DevicesUiState.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/devices/presentation/DevicesScreen.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/devices/presentation/DeviceRegistrationScreen.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/devices/presentation/QrScanner.kt`
- Create: `app/src/test/java/com/qlic/mobile/feature/devices/presentation/DevicesViewModelTest.kt`
- Create: `app/src/androidTest/java/com/qlic/mobile/feature/devices/presentation/DevicesScreenTest.kt`

**Steps**

1. Add failing ViewModel and Compose tests for loading, content, empty, retry, validation and duplicate-submit prevention.
2. Add official CameraX and bundled ML Kit dependencies plus camera permission.
3. Implement QR-only recognition with frame throttling and guaranteed `ImageProxy` closure.
4. Implement permission-denied, retry and manual-payload fallback paths.
5. Implement registration and explicit water-point UUID assignment forms.
6. Run focused unit and connected tests.

## Task 5: Implement the alerting data and domain layers

**Files**

- Create: `app/src/main/java/com/qlic/mobile/feature/alerts/data/AlertApi.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/alerts/data/AlertDtos.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/alerts/data/AlertRepositoryImpl.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/alerts/domain/AlertModels.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/alerts/domain/AlertRepository.kt`
- Create: `app/src/test/java/com/qlic/mobile/feature/alerts/data/AlertRepositoryImplTest.kt`

**Steps**

1. Add failing tests for alert list/detail/acknowledgement, thresholds, preferences and recipients.
2. Model API enums with an unknown-safe mapping strategy.
3. Parse backend-local `LocalDateTime` values without claiming timezone conversion.
4. Implement repositories and ProblemDetail mapping.
5. Run focused tests.

## Task 6: Implement Alerts UI and settings

**Files**

- Create: `app/src/main/java/com/qlic/mobile/feature/alerts/presentation/AlertsViewModel.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/alerts/presentation/AlertsUiState.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/alerts/presentation/AlertsScreen.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/alerts/presentation/AlertDetailScreen.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/alerts/presentation/AlertSettingsScreen.kt`
- Create: `app/src/test/java/com/qlic/mobile/feature/alerts/presentation/AlertsViewModelTest.kt`
- Create: `app/src/androidTest/java/com/qlic/mobile/feature/alerts/presentation/AlertsScreenTest.kt`

**Steps**

1. Add failing tests for list filters, empty results, detail, acknowledgement and settings mutations.
2. Build list/detail screens matching the established Qlic visual system.
3. Add threshold, notification preference and authorized-recipient management.
4. Disable repeated writes and refresh only affected data after success.
5. Run focused tests.

## Task 7: Implement subscriptions and payment-history integration

**Files**

- Create: `app/src/main/java/com/qlic/mobile/feature/subscriptions/data/SubscriptionApi.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/subscriptions/data/SubscriptionDtos.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/subscriptions/data/SubscriptionRepositoryImpl.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/subscriptions/domain/SubscriptionModels.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/subscriptions/domain/SubscriptionRepository.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/subscriptions/presentation/SubscriptionViewModel.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/subscriptions/presentation/SubscriptionScreen.kt`
- Create: `app/src/test/java/com/qlic/mobile/feature/subscriptions/data/SubscriptionRepositoryImplTest.kt`
- Create: `app/src/test/java/com/qlic/mobile/feature/subscriptions/presentation/SubscriptionViewModelTest.kt`
- Create: `app/src/androidTest/java/com/qlic/mobile/feature/subscriptions/presentation/SubscriptionScreenTest.kt`

**Steps**

1. Add failing tests for current/no subscription, trial, purchase, plan change, cancellation and payment history.
2. Treat current-subscription `404` as an empty state.
3. Expose only purchasable plans and the backend's simulated payment method; never collect card details.
4. Implement localized UI states and confirmations for mutations.
5. Run focused tests.

## Task 8: Implement dashboard aggregation and honest unavailable states

**Files**

- Create: `app/src/main/java/com/qlic/mobile/feature/dashboard/presentation/DashboardViewModel.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/dashboard/presentation/DashboardUiState.kt`
- Create: `app/src/main/java/com/qlic/mobile/feature/dashboard/presentation/DashboardScreen.kt`
- Create: `app/src/main/java/com/qlic/mobile/core/designsystem/UnavailableFeature.kt`
- Create: `app/src/test/java/com/qlic/mobile/feature/dashboard/presentation/DashboardViewModelTest.kt`
- Create: `app/src/androidTest/java/com/qlic/mobile/feature/dashboard/presentation/DashboardScreenTest.kt`

**Steps**

1. Add failing tests for complete, partial, empty and fully failed aggregation.
2. Load device, alert and subscription sources independently.
3. Render real counts and per-section retry actions.
4. Render telemetry, reports, tanks and support with explicit unavailable states and no sample numbers.
5. Run focused tests.

## Task 9: Wire navigation, account and local sign-out

**Files**

- Modify: `app/src/main/java/com/qlic/mobile/core/navigation/QlicDestination.kt`
- Modify: `app/src/main/java/com/qlic/mobile/core/navigation/QlicNavHost.kt`
- Modify: `app/src/main/java/com/qlic/mobile/app/QlicApp.kt`
- Modify: `app/src/main/java/com/qlic/mobile/MainActivity.kt`
- Modify: `app/src/main/java/com/qlic/mobile/QlicApplication.kt`
- Modify: `app/src/androidTest/java/com/qlic/mobile/core/navigation/NavigationShellTest.kt`
- Create: `app/src/androidTest/java/com/qlic/mobile/feature/account/AccountScreenTest.kt`

**Steps**

1. Add failing navigation tests for new details, registration, settings and subscription routes.
2. Replace placeholder destinations with integrated screens.
3. Show read-only name/email/account details from the session.
4. Add local sign-out that clears the encrypted session and returns to authentication.
5. Keep unsupported destinations visible through the common unavailable component.
6. Run navigation and authentication regression tests.

## Task 10: Complete bilingual resources and accessibility

**Files**

- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-b+es+419/strings.xml`
- Modify: `app/src/androidTest/java/com/qlic/mobile/core/localization/LanguageSwitchTest.kt`

**Steps**

1. Add failing assertions for representative content on every new screen in both locales.
2. Add all labels, API statuses, errors, empty states and unavailable messages to both resource sets.
3. Add content descriptions and verify interactive touch targets.
4. Confirm language changes update screen content, not only navigation labels.
5. Run localization and Compose tests.

## Task 11: Verify the complete application

**Files**

- Modify only files required by discovered defects.

**Steps**

1. Run `gradlew.bat testDebugUnitTest`.
2. Run `gradlew.bat lintDebug`.
3. Run `gradlew.bat assembleDebug assembleRelease`.
4. Run `gradlew.bat connectedDebugAndroidTest` on the configured emulator.
5. Re-run read-only live API checks for OpenAPI availability, protected-route `401`, and localized invalid credentials.
6. With a user-created test account, verify sign-in and read-only protected requests; do not create billable or persistent demo records without explicit approval.
7. Inspect the release artifact and repository for secrets, tokens, personal data, generated credentials or assistant traces.
8. Review the final diff and report unsupported backend features and the production-blocking authorization issue.

## Task 12: Documentation and handoff

**Files**

- Modify: `README.md`

**Steps**

1. Document Android Studio setup, API base URL, emulator execution, QR camera requirement and test commands.
2. Document supported and unavailable backend capabilities without overstating completion.
3. Document the backend authorization blocker and safe demo scope.
4. Do not push, deploy or publish unless explicitly requested.
