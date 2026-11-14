# SAVII FOCUS — Android

A phone-first, offline-first native Android productivity and learning app.

## Included in this build
- Home dashboard with study-time and task summaries
- Learning subjects: view and add subjects
- Planner: add, complete, and delete tasks
- Focus timer with 15 / 25 / 45 / 60 minute presets
- Focus-session history and basic progress insights
- Settings / privacy information
- Local SQLite storage; no account or network permission required
- GitHub Actions workflow to build and upload a debug APK

## Build
Requirements: JDK 17, Android SDK Platform 36, Android Build Tools, Gradle 8.13.

```bash
gradle assembleDebug
```

APK output:
`app/build/outputs/apk/debug/app-debug.apk`

## Important scope notes
This is a substantial offline-first foundation, not a claim that every future product feature is finished.
- The focus timer currently counts while its screen is active; it does not yet provide a persistent background timer or notifications.
- The AI Assistant is intentionally not connected. A production AI feature needs a secure backend; never place private API keys inside an APK.
- Accounts, cloud backup/sync, and laptop version are future milestones requiring a backend and separate client.
- The original user logo was not supplied as a file in this build, so the app currently uses a typographic SAVII FOCUS wordmark. Add the approved logo asset later.
- Build and device verification must be performed in Android Studio or GitHub Actions before release.

## Performance and privacy
- No analytics SDK, ads SDK, or network permission is included.
- Core data is stored locally using Android SQLite.
- Database reads/writes are moved off the main thread.
- Lazy lists are used for scrollable collections.
