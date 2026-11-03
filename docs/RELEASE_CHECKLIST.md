# Release readiness checklist

A successful CI run verifies the configured build gates only. It does not establish physical-device or Play Store readiness.

## Implemented and verified in CI

- [x] Native Kotlin + Jetpack Compose Android app is the active root project.
- [x] MediaStore audio indexing with granular audio permission.
- [x] Room persistence for tracks, favorites and playlists.
- [x] Compose Library, Favorites, Playlists and Now Playing surfaces.
- [x] Media3 ExoPlayer + MediaSessionService background playback.
- [x] Queue start, play/pause, next/previous, seek, shuffle and repeat.
- [x] Native launcher icon and Android application identity.
- [x] GitHub Actions Android SDK verification and Gradle debug APK build.
- [x] Android lint and configured unit tests pass on the migration commit.
- [x] Debug APK and lint report uploaded as workflow artifacts.
- [x] Dependabot Gradle and GitHub Actions dependency updates enabled.
- [x] Native CI is the sole production Android workflow on main.

## Remaining product work

- [ ] Album, artist and folder browsing with artwork cache.
- [ ] Editable queue, play-next, reorder, queue persistence and playback history.
- [ ] Sleep timer and robust audio focus/interruption handling.
- [ ] Lyrics and equalizer after API and licensing review.
- [ ] Widgets and Android Auto.
- [ ] Room DAO, Compose UI and service lifecycle tests.
- [ ] Accessibility, large font, keyboard/switch access and touch-target audit.

## Required before production release

- [ ] Validate on physical devices across supported Android API levels and OEMs.
- [ ] Verify permission grant/revocation, background playback, notification, headset/Bluetooth, audio focus and process recreation.
- [ ] Verify large libraries, missing/corrupt files, empty states and storage edge cases.
- [ ] Add and publish a privacy policy; complete Play Console Data safety and content rating.
- [ ] Prepare store screenshots, listing, support contact and release notes.
- [ ] Configure a dedicated protected upload keystore in GitHub Secrets.
- [ ] Build a signed Android App Bundle and verify the signing certificate.
- [ ] Complete Play Console internal testing before production rollout.

## Signing security

The Actions debug APK is not a Play Store release. The release pipeline must use protected repository secrets for the keystore, alias and passwords. Never commit keystore files or passwords.
