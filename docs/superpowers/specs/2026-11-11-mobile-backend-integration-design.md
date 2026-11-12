# Qlic Mobile Backend Integration Design

**Date:** 2026-11-11  
**Status:** Proposed  
**Target:** Native Android application (`qlic-frontend-mobile`)  
**Backend:** `https://qlic-backend-api.onrender.com/`

## 1. Goal

Integrate the native Android application with every user-facing capability currently exposed by the deployed Qlic REST API. All visible operational data must come from the authenticated backend. Screens whose design exists but whose supporting endpoint does not exist must remain accessible and show an explicit unavailable state instead of fabricated data.

## 2. Confirmed API Surface

The deployed OpenAPI contract exposes 23 operations across four contexts:

- Authentication: sign-up and sign-in.
- Monitoring: list/register devices and assign a water point.
- Alerting: list/detail/acknowledge alerts, thresholds, notification preferences and authorized recipients.
- Subscriptions: current subscription, trial, purchase, plan change, cancellation and payment history.

Every route except authentication and documentation requires a Bearer JWT. The authenticated session already persists the encrypted JWT and `accountId` locally.

The backend does not currently expose consumption history, flow, pressure, reports, tanks, water-point catalog, support tickets, or profile editing.

## 3. Product Scope

### 3.1 Dashboard

The dashboard will aggregate only information that can be retrieved reliably:

- total registered devices;
- online/offline device counts;
- active and acknowledged alert counts;
- current subscription summary.

Consumption, flow and pressure cards will use an unavailable state explaining that live telemetry is not provided by the current API. No sample values will be shown.

The three supported dashboard sources load independently. A failure in one source will produce a partial dashboard with a retry action for that section instead of discarding successfully loaded device, alert or subscription data. A `404` from the current-subscription endpoint means that the account has no subscription and is an empty state, not a service failure.

### 3.2 Alerts

The Alerts destination will support:

- list and empty states;
- filtering by state and urgency on data already returned by the API;
- alert detail;
- acknowledgement with an action description;
- consumption thresholds;
- notification grouping/minimum urgency preferences;
- authorized recipient list, registration and enabled/disabled state.

The reading-evaluation endpoint is considered an ingestion/backend function and will not be exposed as a normal end-user action in the mobile UI.

### 3.3 Devices and Business

The Devices screen will support:

- listing the account's devices;
- device status, battery and last synchronization display;
- device registration through a real QR scan plus serial number and model;
- water-point assignment only when the user already has a valid water-point UUID.

Because the API does not expose a water-point catalog, the app will not invent a selector. The assignment action will use explicit identifier entry with validation and a clear explanation until the backend provides a catalog.

QR registration will use the device camera, a native camera preview and on-device barcode recognition. The flow will request camera permission at the moment it is needed, explain a denial, support a retry, and provide manual QR-payload entry as an accessibility and device-compatibility fallback.

The Tanks screen remains visible with an unavailable state. The Business summary will be based on device data, not tank data.

### 3.4 Reports

Reports remains visible with an unavailable state because the backend has no consumption-history or aggregation endpoint.

### 3.5 More, Account and Subscriptions

More will expose language, local session termination and subscription management. The backend has no logout endpoint, so signing out clears the encrypted local session without claiming server-side token revocation. Subscription management will support:

- current plan and status;
- starting the one-time trial;
- purchasing a plan;
- scheduling a plan change;
- cancellation while preserving the backend-reported period;
- payment history.

The accepted plan values are `TRIAL`, `HOME_BASIC`, `HOME_PRO`, `PYME_BUSINESS` and `PYME_ENTERPRISE`. `TRIAL` is started through its dedicated endpoint and is not presented as a purchasable plan. Purchases use the backend's simulated payment method and never request card details.

Profile editing and support tickets remain visible as unavailable states. The authentication session will be extended to persist the signed-in user's name and email, so the Account screen can show read-only identity data without implying that it can update the account.

## 4. Architecture

The existing Kotlin, Jetpack Compose, Material 3 and Retrofit stack will be retained. The implementation will use feature-oriented layers:

- `core/network`: Retrofit creation, authenticated request interceptor, locale header, common API error parsing and unauthorized-session handling.
- `feature/devices`: API DTOs, repository, domain models, use cases, ViewModel and Compose screens.
- `feature/alerts`: API DTOs, repository, domain models, use cases, ViewModel and Compose screens.
- `feature/subscriptions`: API DTOs, repository, domain models, use cases, ViewModel and Compose screens.
- `feature/dashboard`: aggregation of device, alert and subscription repositories without a synthetic dashboard endpoint.
- existing `feature/auth`: retained and extended only where shared session metadata is required.

Manual dependency composition will remain in `QlicApplication` to avoid introducing a dependency-injection framework solely for this integration.

## 5. Session and Request Flow

1. Authentication stores the encrypted token, token type, expiration and account identifier.
2. A process-scoped, thread-safe session snapshot mirrors the latest persisted session and selected language for synchronous network access. It is initialized before protected calls and updated whenever either persisted flow changes.
3. A session-aware HTTP interceptor reads that in-memory snapshot and adds `Authorization: Bearer <token>` to protected calls without blocking on a `Flow` or using `runBlocking`.
4. The interceptor also adds `Accept-Language` from the locale snapshot.
5. Repositories obtain `accountId` only from the session provider; UI input cannot replace it.
6. A `401` response emits a session-expired signal. An application coroutine handles that signal by clearing persisted session state and returning the user to sign-in; the interceptor performs no blocking storage work.
7. The token and sensitive request data are never logged.

The backend currently trusts request-supplied `accountId`; some mutation routes also accept only a resource UUID. The mobile client reduces accidental misuse by sourcing account identifiers exclusively from the encrypted session and by exposing only resources returned for that session. This does not prevent an authenticated attacker from attempting insecure direct-object references. Server-side ownership validation for both account and resource identifiers is a production release blocker; the current API is suitable only for controlled academic/demo use until that is fixed.

## 6. State and Error Handling

Every integrated screen will model these states explicitly:

- loading;
- content;
- empty;
- recoverable network error with retry;
- validation error;
- unauthorized/session expired;
- service error;
- feature unavailable because no endpoint exists.

Backend `ProblemDetail` responses will be mapped to localized user-facing messages. Raw backend exceptions, stack traces and identifiers will not be displayed.

Write operations will disable duplicate submission while running and refresh the affected resource after success.

Backend timestamps use `LocalDateTime` without an offset. The app will parse and display them as backend-local values using a consistent date format and will not claim timezone conversion.

## 7. Localization and Accessibility

All new labels, errors, empty states and API status translations will be available in English and Latin American Spanish. Existing runtime language switching remains global. Interactive controls will have meaningful content descriptions, adequate touch targets and visible loading/disabled states.

## 8. Security

- HTTPS only; the existing network security configuration remains restrictive.
- Encrypted JWT storage through Android Keystore and DataStore.
- No credentials, tokens or personal data in source code or logs.
- Account identifiers are derived from the authenticated session.
- Expired or rejected sessions are cleared immediately.
- Release shrinking compatibility is preserved with explicit serialized field names.
- Subscription payment is treated as backend simulation; no card secrets are collected or stored by the app.
- The app is not approved for production while the backend lacks token-to-account and resource-ownership authorization checks.

## 9. Testing and Verification

The integration will include:

- unit tests for DTO mapping, repositories, validation and ViewModel state transitions;
- HTTP contract tests using deterministic fake responses for success, empty, error and unauthorized cases;
- Compose tests for loading, content, empty, unavailable and retry states;
- navigation and global-language regression tests;
- debug and release builds;
- lint and local unit tests;
- connected Android tests on the available emulator;
- read-only smoke checks against the deployed API plus authenticated checks through a user-created Qlic account.

Tests will not create production subscriptions, devices, recipients or alerts without an explicit test account and deliberate execution.

## 10. Out of Scope

- inventing telemetry, reports, tank data or support tickets;
- modifying the backend;
- implementing push notification delivery infrastructure;
- processing real payments;
- publishing or pushing the mobile repository.

## 11. Acceptance Criteria

- Authentication continues working against the deployed API.
- Protected requests include a valid JWT and the session account identifier.
- Devices, alerts, settings, recipients and subscriptions use live API data.
- Dashboard metrics are derived only from supported API responses.
- Unsupported screens clearly state that backend support is unavailable and show no mock values.
- Session expiry returns the user to sign-in.
- English/Spanish switching covers every new screen and message.
- Unit, lint, debug, release and connected test suites pass.
