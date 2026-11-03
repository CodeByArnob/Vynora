# Release readiness checklist

This checklist reflects the actual release gates; generated build artifacts are not proof of Play Store readiness.

## Implemented in repository

- [x] Vynora launcher icon source committed
- [x] Android launcher icon generation configured
- [x] Automated formatting, static analysis and test checks
- [x] Android platform bootstrap and release-mode QA build in GitHub Actions
- [x] QA APK uploaded as a workflow artifact

## Required before production release

- [ ] Confirm CI is green on the committed Android platform
- [ ] Implement and test on-device music indexing and permission handling
- [ ] Implement local playback, persistent queue/playlists and favorites
- [ ] Implement background playback, media notification, lock-screen and headset controls
- [ ] Pin Flutter SDK and dependency versions for reproducible releases
- [ ] Add unit, widget, integration and Android device test coverage
- [ ] Test supported Android API levels and common device vendors
- [ ] Configure a dedicated protected Android upload keystore in GitHub Secrets
- [ ] Configure release signing and verify the signed APK/AAB certificate
- [ ] Configure API key restrictions, quota alerts and key rotation
- [ ] Review YouTube attribution, API terms, branding and permitted usage
- [ ] Publish a privacy policy and complete Play Console Data safety disclosures
- [ ] Complete app screenshots, store listing, content rating and support details
- [ ] Verify offline, empty, timeout, quota-exceeded and permission-denied states
- [ ] Verify accessibility, crash reporting and performance on physical devices
- [ ] Create and verify a signed release artifact before tagging a production version

## Signing requirements

The Actions QA APK is not a Play Store release. A production signing workflow must use repository secrets for a dedicated keystore, key alias, store password and key password. Never commit keystore files or passwords to this repository.
