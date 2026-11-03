# Vynora Native Android

Native Kotlin + Jetpack Compose implementation of Vynora.

## Requirements
- JDK 17
- Android Gradle Plugin 9.1.1 / Gradle 9.3.1
- Android SDK Platform 37
- Android Studio Panda 3 or newer

## Stack
- Kotlin 2.2.20 + Compose compiler plugin
- Compose Material 3 BOM 2026.09.00
- AndroidX Media3 1.11.1
- Room 2.8.5 + KSP

## Run
Open this directory in Android Studio, or install the required SDK packages and run:

```bash
gradle :app:assembleDebug
gradle :app:lintDebug
gradle :app:testDebugUnitTest
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

The current implementation is a foundation, not a Play Store production release.
