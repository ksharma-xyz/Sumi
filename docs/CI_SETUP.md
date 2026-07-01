# CI/CD setup — GitHub secrets & variables

> **TODO for the repo owner.** GitHub Actions workflows in `.github/workflows/` are
> committed and ready, but they fail until the secrets and variables below are added.
> This is a one-time setup. Work top-down: do **Tier 1** first to get PR checks green,
> then the rest when you need distribution.

## Where things go in GitHub

| Type | Location in GitHub UI |
|---|---|
| **Secrets** (encrypted, e.g. keys, passwords, JSON) | Repo → **Settings → Secrets and variables → Actions → Secrets** tab → *New repository secret* |
| **Variables** (plain text, e.g. counters, team id) | Repo → **Settings → Secrets and variables → Actions → Variables** tab → *New repository variable* |
| **Environment** named `Firebase` | Repo → **Settings → Environments**. Several jobs declare `environment: Firebase`. Referencing it auto-creates it on first run, so you don't have to pre-create it. Repository-level secrets are visible to those jobs too — **adding everything at the repository level (above) is sufficient.** Only use environment secrets if you later want approval/protection rules. |

After adding everything, re-run the failed workflow from the **Actions** tab (or push a new commit) — there is no CLI command needed; GitHub injects them automatically.

---

## Tier 1 — minimum to make PR checks pass

The `Sumi App CI` workflow (`build.yml`) runs on every PR to `main`. Its jobs:

- `code-quality` (detekt) — **needs nothing.** Already passes.
- `build-android-debug`, `build-android-release` — need the Android secrets below.
- `build-ios` — needs `FIREBASE_IOS_GOOGLE_INFO`.

(The `distribute-*` jobs only run on push to `main`, **not** on PRs — see Tier 2/3.)

### Secrets

| Secret | What it is | Where to get it | Format to paste |
|---|---|---|---|
| `FIREBASE_GOOGLE_SERVICES_JSON_DEBUG` | Firebase config for the `.debug` app | Firebase Console → Project settings → *Your apps* → Android app `xyz.ksharma.sumi.debug` → `google-services.json` | **base64**, one line (see commands) |
| `FIREBASE_GOOGLE_SERVICES_JSON_RELEASE` | Firebase config for the prod app | Firebase Console → Android app `xyz.ksharma.sumi` → `google-services.json` | **base64**, one line |
| `FIREBASE_IOS_GOOGLE_INFO` | Firebase config for the iOS app | Firebase Console → iOS app → `GoogleService-Info.plist` | **base64**, one line |
| `ANDROID_KEYSTORE_FILE` | Upload/release signing keystore | Your `keystore.jks` (the one you sign releases with). If you don't have one yet, create it with `keytool` (command below) | **base64**, one line |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore (store) password | Set when the keystore was created | plain text |
| `ANDROID_KEY_ALIAS` | Key alias inside the keystore | Set when the keystore was created (`keytool -list -keystore keystore.jks` to check) | plain text |
| `ANDROID_KEY_PASSWORD` | Key (alias) password | Set when the keystore was created | plain text |
| `PAT_SUMI_GITHUB` | GitHub token used to bump the `ANDROID_VERSION_CODE` variable | GitHub → Settings → Developer settings → **Personal access tokens**. Classic: `repo` scope. Fine-grained: this repo + **Variables: Read and write** + **Contents: Read and write** | the token string |

### Variables

| Variable | What it is | Value to set |
|---|---|---|
| `ANDROID_VERSION_CODE` | Monotonic Play Store version code; release builds increment it | Start at the last code you uploaded, or `108` if unsure (local default is 107) |

> **Note:** `build-android-release` signs the AAB. If you only care about PR *checks* and
> not signed release artifacts yet, the release job will still fail without the keystore
> secrets — there's no way to skip just that job without editing `build.yml`. Add the four
> `ANDROID_*` keystore secrets to get the whole PR green.

---

## Tier 2 — Android distribution (push to `main`)

Used by `distribute-firebase.yml` and `distribute-google-play.yml`.

| Secret | What it is | Where to get it | Format |
|---|---|---|---|
| `FIREBASE_SERVICE_ACCOUNT_KEY` | Service account for Firebase App Distribution | Firebase Console → Project settings → **Service accounts** → *Generate new private key* (JSON) | **raw JSON** (paste the file contents as-is, not base64) |
| `FIREBASE_ANDROID_DEBUG_APP_ID` | Firebase App ID for the debug app | Firebase Console → debug app → *App ID* (looks like `1:1234567890:android:abc123`) | plain text |
| `FIREBASE_ANDROID_PROD_APP_ID` | Firebase App ID for the prod app | Firebase Console → prod app → *App ID* | plain text |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Play Console service account for uploads | Google Play Console → **Setup → API access** → create/link a service account in Google Cloud, grant *Release* permissions, download JSON key | **raw JSON** (plain text) |

---

## Tier 3 — iOS distribution / TestFlight

Used by `distribute-testflight.yml`.

| Secret | What it is | Where to get it | Format |
|---|---|---|---|
| `APPSTORE_KEY_ID` | App Store Connect API key ID | App Store Connect → **Users and Access → Integrations → App Store Connect API** → your key's *Key ID* | plain text |
| `APPSTORE_ISSUER_ID` | App Store Connect API issuer ID | Same page, top of the Keys section → *Issuer ID* | plain text |
| `APPSTORE_PRIVATE_KEY` | The API private key | Same page → download `AuthKey_XXXX.p8` (downloadable **once**) | **full contents of the `.p8` file**, including the `-----BEGIN/END PRIVATE KEY-----` lines |
| `IOS_DIST_SIGNING_KEY_BASE64` | Apple distribution certificate | Export your *Apple Distribution* cert + private key from Keychain as a `.p12` | **base64**, one line |
| `IOS_DIST_SIGNING_KEY_PASSWORD` | Password you set on the `.p12` export | You choose it during export | plain text |
| `IOS_PROVISIONING_PROFILE_NAME` | Name of the App Store provisioning profile | Apple Developer → Certificates, IDs & Profiles → your *App Store* profile name (or the one Fastlane manages) | plain text |

### Variables (Tier 3)

| Variable | What it is | Value |
|---|---|---|
| `DEVELOPMENT_TEAM` | Apple Developer Team ID | Apple Developer → Membership → *Team ID* (10 chars) |
| `IOS_BUILD_NUMBER` | Monotonic floor for the TestFlight build number | Start at the last build number you uploaded, or `1` |

---

## Encoding commands

GitHub secrets must be a single line. Base64-encode files like this:

```bash
# macOS
base64 -i google-services.json | tr -d '\n' | pbcopy        # now paste into the secret
base64 -i keystore.jks         | tr -d '\n' | pbcopy
base64 -i GoogleService-Info.plist | tr -d '\n' | pbcopy
base64 -i dist_cert.p12        | tr -d '\n' | pbcopy

# Linux
base64 -w0 google-services.json   # copy the printed line
```

Create an Android keystore (only if you don't already have one):

```bash
keytool -genkeypair -v -keystore keystore.jks \
  -alias sumi-upload -keyalg RSA -keysize 2048 -validity 10000
# remember the store password, alias (sumi-upload), and key password —
# those become ANDROID_KEYSTORE_PASSWORD / ANDROID_KEY_ALIAS / ANDROID_KEY_PASSWORD
```

> Raw-JSON secrets (`FIREBASE_SERVICE_ACCOUNT_KEY`, `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`)
> and the `.p8` key are pasted **as-is, not base64**. Only the files in the
> "base64, one line" rows get encoded.

---

## Verify it worked

1. Push a commit or open a PR to `main`, then watch **Actions → Sumi App CI**.
2. `code-quality` should already be green. Once Tier 1 is in, `build-android-debug`,
   `build-android-release`, and `build-ios` go green too.
3. If a job fails, open its log — missing-secret failures point at the exact step
   (e.g. the keystore decode or the `google-services.json` write).

Local equivalent of the no-secret check (always runnable):

```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64 :composeApp:compileAndroidMain :composeApp:detekt :game:testAndroidHostTest
```
