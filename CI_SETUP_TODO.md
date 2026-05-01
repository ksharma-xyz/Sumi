# CI/CD setup — TODO

One-time setup before any of the new GitHub Actions workflows can run. Tick as you go; delete this file once everything below is set up and a release run has succeeded end-to-end.

Reference: the `kmp-ci-cd` skill at `/Users/ksharma/code/apps/Utility/claude-skills/kmp-ci-cd/SKILL.md` documents the full architecture.

> **Don't paste real values in this file.** Everything below is the *list of things to add* — the actual secrets / passwords / certs go straight into GitHub repo settings.

---

## 1. Create a GitHub Environment

Settings → Environments → **New environment** → name: `Firebase`

Several workflows reference `environment: Firebase`. Once created, the secrets listed below can either live at repo level or scoped to this environment — both are read by `${{ secrets.X }}`. Environment-level keeps prod secrets gated behind environment protection rules if you want.

- [ ] Created environment `Firebase`

---

## 2. Repo *secrets* — Settings → Secrets and variables → Actions → Secrets

### Android signing
- [ ] `ANDROID_KEYSTORE_FILE` — base64 of your existing Sumi `keystore.jks`. On macOS: `base64 -i keystore.jks | pbcopy`.
- [ ] `ANDROID_KEYSTORE_PASSWORD` — store password (set when you ran `keytool -genkey`).
- [ ] `ANDROID_KEY_ALIAS` — alias inside the keystore. Read with `keytool -list -keystore keystore.jks`.
- [ ] `ANDROID_KEY_PASSWORD` — key password (often the same as the store password).

### Firebase
- [ ] `FIREBASE_GOOGLE_SERVICES_JSON_DEBUG` — base64 of `google-services.json` for the **debug** Android app. Firebase Console → Project Settings → debug Android app → download → `base64 -i google-services.json`.
- [ ] `FIREBASE_GOOGLE_SERVICES_JSON_RELEASE` — same, for the release Android app.
- [ ] `FIREBASE_IOS_GOOGLE_INFO` — base64 of `GoogleService-Info.plist`. Firebase Console → iOS app → download → `base64 -i …`.
- [ ] `FIREBASE_SERVICE_ACCOUNT_KEY` — base64 of a Firebase service-account JSON with the **App Distribution Admin** role. Firebase Console → Project Settings → Service accounts → Generate new private key → `base64 -i ...`.
- [ ] `FIREBASE_ANDROID_DEBUG_APP_ID` — App ID like `1:NUMBER:android:HASH` for the debug app. Firebase Console → Project Settings → debug Android app → "App ID".
- [ ] `FIREBASE_ANDROID_PROD_APP_ID` — same for the release Android app.

### Google Play
- [ ] `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` — service-account JSON (raw, **not** base64).
  Google Cloud Console → IAM → Service Accounts → create or pick one → Keys → Add key → JSON → download.
  Then in Play Console → Setup → API access → Grant access to that service account → assign "Release manager" role.

### App Store Connect (TestFlight)
- [ ] `APPSTORE_KEY_ID` — 10-char Key ID from App Store Connect → Users and Access → Keys (Team Keys tab) → "Key ID" column.
- [ ] `APPSTORE_ISSUER_ID` — UUID at the top of the Keys page (applies to all keys).
- [ ] `APPSTORE_PRIVATE_KEY` — paste the entire `.p8` file contents (the `-----BEGIN PRIVATE KEY----- … -----END PRIVATE KEY-----` block).
  This is the file you downloaded ONCE when creating the key. If you lost it, create a new key and rotate.

### iOS distribution cert + provisioning profile
- [ ] `IOS_DIST_SIGNING_KEY_BASE64` — base64 of an Apple Distribution `.p12` exported from Keychain.
  Keychain Access → "login" keychain → Certificates → right-click your "Apple Distribution: …" cert → Export → `.p12` (set a password) → `base64 -i cert.p12 | pbcopy`.
- [ ] `IOS_DIST_SIGNING_KEY_PASSWORD` — the password you set during the `.p12` export.
- [ ] `IOS_PROVISIONING_PROFILE_NAME` — the **name** of your App Store provisioning profile (not the UUID).
  developer.apple.com → Certificates, IDs & Profiles → Profiles → your App Store profile → "Profile Name" field.

### GitHub PAT (for repo-variable write-back)
- [ ] `PAT_SUMI_GITHUB` — fine-grained PAT scoped to `ksharma-xyz/Sumi` only.
  github.com → Settings (account) → Developer settings → Personal access tokens → Fine-grained tokens → Generate new token.
  Repository access: **only** Sumi.
  Permissions: Variables = R/W, Contents = R/W, Metadata = R+W.

---

## 3. Repo *variables* — Settings → Secrets and variables → Actions → Variables

These are plain-text and visible in the repo settings; not secret.

- [ ] `ANDROID_VERSION_CODE` — initial value `10` (or higher than any Play Console upload that already happened).
- [ ] `IOS_BUILD_NUMBER` — initial value `1` (or higher than any TestFlight build number that already exists for Sumi).
- [ ] `DEVELOPMENT_TEAM` — your 10-char Apple Team ID (e.g. `ABCD123XYZ`). One-time, never auto-bumped.

---

## 4. Verify with a smoke run

- [ ] Push any small commit to `main` (or open a PR). `build.yml` should run code-quality + build-android-debug + build-ios → all green. (Release-build job needs the Android signing secrets above to pass.)
- [ ] Once green, dispatch `release-1-cut.yml` from the Actions tab to cut `prod/1.0`.
- [ ] `release-2-deploy-rc.yml` should fire automatically on the `prod/1.0` push and ship to Play Internal + TestFlight.
- [ ] Confirm the build appears in Play Console (Internal track) and App Store Connect (TestFlight builds list).
- [ ] Once QA passes, dispatch `release-3-tag.yml` with version `1.0` → final tag pushes → `create-github-release.yml` drafts a Release on GitHub.

---

## Notes

- Keystore: don't generate a new one — sign with the same one Sumi used for prior Play uploads, otherwise Play Console will reject the bundle.
- The Apple Distribution cert + App Store provisioning profile must be valid and current. If yours has expired, regenerate at developer.apple.com first.
- ASC App ID `6764288103` is already baked into `distribute-testflight.yml` and `iosApp/fastlane/Fastfile` — don't add it as a secret.
- Bundle ID `xyz.ksharma.sumi` is baked into the workflows.
- If a workflow run fails complaining about a missing secret, the secret name is in the error — find it on this list, source it, add it.
