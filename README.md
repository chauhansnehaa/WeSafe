# 🛡️ WeSafe

WeSafe is a personal safety Android app. Users sign in with their phone number, build a list of trusted contacts, and can trigger an SOS in one tap — or automatically by shaking the phone — which texts their live location to selected contacts.

## Features

- **Phone number sign-in** — OTP verification via Firebase Authentication, followed by a local app password/PIN for quick re-entry.
- **Trusted contacts (Friends)** — add friends by phone number, stored locally in a Room database, and choose which ones should receive SOS alerts.
- **One-tap SOS** — sends an SMS with a live Google Maps location link to all designated SOS contacts.
- **Shake-to-trigger SOS** — a background foreground service listens to the accelerometer and fires an SOS automatically after a configurable number of shakes, with a debounce so it doesn't spam.
- **SOS settings** — configure how many shakes are needed to trigger an alert.
- **Track-me sheet** — a bottom sheet for sharing your live location with a friend on demand.
- **Background-safe** — runs as a foreground service with a persistent notification so SOS detection keeps working while the app isn't in the foreground.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| Platform | Android (minSdk 26, targetSdk/compileSdk 36) |
| Auth | Firebase Authentication (Phone/OTP) |
| Cloud data | Firebase Firestore |
| Local storage | Room (trusted contacts / friends) |
| Maps | [osmdroid](https://github.com/osmdroid/osmdroid) (OpenStreetMap) |
| Location | Google Play Services Location (FusedLocationProviderClient) |
| Image loading | Glide |
| SMS | Android `SmsManager` |
| Build system | Gradle (Kotlin DSL) |

## Project Structure

```
wesafe/
├── app/
│   ├── src/main/java/com/sneha/wesafe/
│   │   ├── SplashActivity.java           # App entry point / launcher
│   │   ├── LoginActivity.java            # Phone number entry, starts OTP flow
│   │   ├── VerifyOtpActivity.java        # OTP verification
│   │   ├── PasswordSetupActivity.java    # First-time local app password setup
│   │   ├── EnterPasswordActivity.java    # Returning-user local password check
│   │   ├── HomeActivity.java             # Main home screen
│   │   ├── MenuActivity.java             # App menu / navigation
│   │   ├── AddFriendActivity.java        # Add a trusted contact
│   │   ├── FriendsListActivity.java      # List/manage trusted contacts
│   │   ├── SosSettingsActivity.java      # Configure shake-trigger sensitivity
│   │   ├── SosService.java               # Foreground service: shake detection + SOS SMS
│   │   ├── TrackMeBottomSheet.java       # Share live location on demand
│   │   ├── PermissionHelper.java         # Runtime permission handling
│   │   ├── AppDatabase.java / FriendDao.java / Friend.java  # Room database layer
│   │   └── FriendAdapter.java / FriendCheckboxAdapter.java  # RecyclerView adapters
│   └── src/main/res/                     # Layouts, drawables, strings, themes
└── build.gradle.kts / settings.gradle.kts
```

## Getting Started

### Prerequisites

- Android Studio (Koala or newer recommended)
- JDK 11
- A Firebase project with **Phone Authentication** and **Firestore** enabled
- A physical device or emulator with SIM/SMS capability to test the SOS feature (emulators can simulate SMS but won't deliver real texts)

### Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/<your-username>/wesafe.git
   cd wesafe
   ```

2. **Add your Firebase config**
   This repo does not include `google-services.json` since it contains project-specific keys. Create your own Firebase project at the [Firebase Console](https://console.firebase.google.com/), enable **Phone** sign-in under Authentication and **Firestore**, register an Android app with package name `com.sneha.wesafe`, download the generated `google-services.json`, and place it at:
   ```
   app/google-services.json
   ```
   A template with the expected fields is provided at `app/google-services.json.example`.

3. **Open in Android Studio**
   Open the project root, let Gradle sync, and run on an emulator or device (minSdk 26+).

4. **Permissions**
   The app requests SMS, fine/coarse/background location, foreground service, and notification permissions at runtime. All must be granted for shake-detection SOS and location sharing to work.

## How SOS Works

`SosService` runs as a foreground service and listens to the device accelerometer. When shake intensity crosses a threshold for a configurable number of consecutive shakes (set in SOS Settings), it fetches the current GPS location and sends an SMS — with a Google Maps link to that location — to every contact marked as an SOS contact in the Friends list.

## License

No license has been specified yet. Add a `LICENSE` file (e.g. MIT) if you'd like others to be able to reuse this code.
