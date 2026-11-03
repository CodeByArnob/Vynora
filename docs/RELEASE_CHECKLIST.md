# Release readiness checklist

This checklist reflects actual implementation; a successful CI run alone is not proof of Play Store readiness.

## Implemented in repository

- [x] Vynora launcher icon source committed
- [x] Android launcher icon generation configured
- [x] User-initiated local audio import into app-private storage
- [x] Local audio playback queue and mini-player
- [x] Background audio service and media notification integration configured
- [x] Automated formatting, static analysis and test checks
- [x] Android platform bootstrap and release-mode QA build in GitHub Actions
- [x] QA APK uploaded as a workflow artifact

## Required before production release

- [ ] Confirm CI is green on the committed Android platform
- [ ] Verify import, playback, deletion and file persistence on physical devices
- [ ] Verify background playback, audio focus, interruptions, notification and headset controls
- [ ] Add metadata extraction, artwork, search, playlists, favorites and queue editing
- [ ] Add widget and integration tests for the local player and navigation
- [ ] Pin all dependency versions in the committed lockfile and audit updates
- [ ] Test supported Android API levels and common device vendors
- [ ] Configure a dedicated protected Android upload keystore in GitHub Secrets
- [ ] Configure release signing and verify the signed APK/AAB certificate
- [ ] Configure API key restrictions, quota alerts and key rotation
- [ ] Review YouTube attribution, API terms, branding and permitted usage
- [ ] Publish a privacy policy and complete Play Console Data safety disclosures
- [ ] Complete app screenshots, store listing, content rating and support details
- [ ] Verify offline, empty, timeout, quota-exceeded and permission-denied states
- [ ] Verify accessibility, crash reporting and performance on physical devices
- [ ] Create and verify a signed AAB before tagging a production version

## Signing requirements

The Actions QA APK is not a Play Store release. A production signing workflow must use repository secrets for a dedicated keystore, key alias, store password and key password. Never commit keystore files or passwords to this repository.
