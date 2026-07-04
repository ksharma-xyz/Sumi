# CI/CD setup — GitHub secrets & variables

> **TODO for the repo owner.** GitHub Actions workflows in `.github/workflows/` are
> committed and ready, but they fail until the secrets and variables below are added.
> One-time setup. Do steps in order — each step is a click-path to get a value, then
> a command that uploads it straight to GitHub via `gh`. No manual pasting into the
> GitHub UI needed.

## Before you start

a) Open Terminal.
b) Run:
```bash
cd /Users/ksharma/code/apps/Sumi
gh auth status
```
c) Confirm it says you're logged in to `ksharma-xyz/Sumi` (or GitHub generally).
d) Run:
```bash
gh secret list
gh variable list
```
e) Both should print nothing right now — that's the starting point.

Environment `Firebase` already exists on the repo — nothing to create there.

---

## Tier 1 — makes PR checks pass

### Step 1 — `FIREBASE_GOOGLE_SERVICES_JSON_DEBUG`
a) Go to https://console.firebase.google.com
b) Click your Sumi project.
c) Click the gear icon (top left) → **Project settings**.
d) Scroll to **Your apps**, find the Android app with package name `xyz.ksharma.sumi.debug`.
e) Click that app, click **google-services.json** to download it.
f) In Terminal, run (adjust path if it downloaded somewhere else):
```bash
base64 -i ~/Downloads/google-services.json | tr -d '\n' | gh secret set FIREBASE_GOOGLE_SERVICES_JSON_DEBUG
```
g) Rename or move that file out of `~/Downloads` now (so step 2 doesn't overwrite it).

### Step 2 — `FIREBASE_GOOGLE_SERVICES_JSON_RELEASE`
a) Same **Project settings → Your apps** page.
b) Find the Android app with package name `xyz.ksharma.sumi` (no `.debug` suffix).
c) Click it, click **google-services.json** to download.
d) Run:
```bash
base64 -i ~/Downloads/google-services.json | tr -d '\n' | gh secret set FIREBASE_GOOGLE_SERVICES_JSON_RELEASE
```

### Step 3 — `FIREBASE_IOS_GOOGLE_INFO`
a) Same **Your apps** page → find the iOS app.
b) Click it, click **GoogleService-Info.plist** to download.
c) Run:
```bash
base64 -i ~/Downloads/GoogleService-Info.plist | tr -d '\n' | gh secret set FIREBASE_IOS_GOOGLE_INFO
```

### Step 4 — `ANDROID_KEYSTORE_FILE`
Sumi already shipped 1.2 on Play, so a release keystore exists somewhere on your
machine already — find and reuse it. **Do not generate a new one**; a new
keystore can't update an existing Play listing.

a) Run:
```bash
mdfind -name keystore.jks
```
b) If it finds a path, run (swap in that path):
```bash
base64 -i /path/to/keystore.jks | tr -d '\n' | gh secret set ANDROID_KEYSTORE_FILE
```
c) If `mdfind` finds nothing AND you're certain this is genuinely the first
   release build ever signed, only then create one:
```bash
keytool -genkeypair -v -keystore keystore.jks -alias sumi-upload -keyalg RSA -keysize 2048 -validity 10000
```
d) It will prompt for a store password and a key password interactively — type
   them in and **write both down**, you need them in steps 5–7.
e) Then run:
```bash
base64 -i keystore.jks | tr -d '\n' | gh secret set ANDROID_KEYSTORE_FILE
```

### Step 5 — `ANDROID_KEYSTORE_PASSWORD`
a) This is the store password from step 4 (either the one you already had, or
   the one you just typed when creating the keystore).
b) Run (swap in the real password):
```bash
gh secret set ANDROID_KEYSTORE_PASSWORD --body "PASTE_PASSWORD_HERE"
```

### Step 6 — `ANDROID_KEY_ALIAS`
a) Run:
```bash
keytool -list -keystore /path/to/keystore.jks
```
b) It'll ask for the store password — type it in.
c) It prints one alias name (e.g. `sumi-upload`). Copy that exact string.
d) Run:
```bash
gh secret set ANDROID_KEY_ALIAS --body "the-alias-name"
```

### Step 7 — `ANDROID_KEY_PASSWORD`
a) The key (alias) password from when the keystore was created — often the
   same as the store password, but not always.
b) Run:
```bash
gh secret set ANDROID_KEY_PASSWORD --body "PASTE_PASSWORD_HERE"
```

### Step 8 — `PAT_SUMI_GITHUB`
a) Go to https://github.com/settings/tokens?type=beta
b) Click **Generate new token**.
c) Under **Repository access**, choose **Only select repositories** → pick `ksharma-xyz/Sumi`.
d) Under **Permissions**, set **Contents** → Read and write, **Variables** → Read and write.
e) Click **Generate token**.
f) Click the copy icon next to the token (shown once — copy it now).
g) Run:
```bash
gh secret set PAT_SUMI_GITHUB --body "$(pbpaste)"
```

### Step 9 — `ANDROID_VERSION_CODE` (variable, not secret)
a) Go to https://play.google.com/console
b) Click into the Sumi app.
c) Left sidebar → **Release → Production** (or whichever track has the latest build) → note the version code number shown.
d) Run (swap in that number, or use `108` if you can't find it — local fallback is 107):
```bash
gh variable set ANDROID_VERSION_CODE --body "108"
```

---

## Tier 2 — Android distribution (push to `main`)

### Step 10 — `FIREBASE_SERVICE_ACCOUNT_KEY`
a) Firebase Console → gear icon → **Project settings** → **Service accounts** tab.
b) Click **Generate new private key** → confirm → a JSON file downloads.
c) Run (adjust the filename glob to match what actually downloaded):
```bash
gh secret set FIREBASE_SERVICE_ACCOUNT_KEY < ~/Downloads/*-firebase-adminsdk-*.json
```
(raw JSON, not base64)

### Step 11 — `FIREBASE_ANDROID_DEBUG_APP_ID`
a) Firebase Console → **Project settings → Your apps** → click the debug app (`xyz.ksharma.sumi.debug`).
b) Copy the **App ID** shown (looks like `1:1234567890:android:abc123`).
c) Run:
```bash
gh secret set FIREBASE_ANDROID_DEBUG_APP_ID --body "$(pbpaste)"
```

### Step 12 — `FIREBASE_ANDROID_PROD_APP_ID`
a) Same page → click the prod app (`xyz.ksharma.sumi`).
b) Copy its **App ID**.
c) Run:
```bash
gh secret set FIREBASE_ANDROID_PROD_APP_ID --body "$(pbpaste)"
```

### Step 13 — `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`
a) Play Console → **Setup → API access** (left sidebar).
b) If no Cloud project linked yet, click **Create new project** (or link an existing one).
c) Click **Create new service account** — this opens Google Cloud Console in a new tab with instructions; follow through to create it.
d) Back in Play Console's API access page, find your new service account under **Service accounts**, click **Grant access**.
e) Give it the **Release manager** role (or Admin), click **Invite user** / **Send invite**.
f) In Google Cloud Console → **IAM & Admin → Service Accounts** → click your account → **Keys** tab → **Add key → Create new key → JSON** → downloads a JSON file.
g) Run:
```bash
gh secret set GOOGLE_PLAY_SERVICE_ACCOUNT_JSON < ~/Downloads/your-project-*.json
```

---

## Tier 3 — iOS distribution / TestFlight

### Step 14 — `APPSTORE_KEY_ID`
a) Go to https://appstoreconnect.apple.com
b) Click **Users and Access** (top nav or hamburger menu).
c) Click **Integrations** tab → **App Store Connect API**.
d) Click **+** to generate a new team key, name it (e.g. "CI"), role **App Manager**, click **Generate**.
e) Copy the **Key ID** shown in the new row.
f) Run:
```bash
gh secret set APPSTORE_KEY_ID --body "$(pbpaste)"
```

### Step 15 — `APPSTORE_ISSUER_ID`
a) Same **Integrations → App Store Connect API** page.
b) Copy the **Issuer ID** shown at the top of the Keys section (same for all keys).
c) Run:
```bash
gh secret set APPSTORE_ISSUER_ID --body "$(pbpaste)"
```

### Step 16 — `APPSTORE_PRIVATE_KEY`
a) Same page → click **Download API Key** next to the key you just created.
b) This works **once only** — if you lose the file you must generate a new key and redo steps 14–16.
c) Run:
```bash
gh secret set APPSTORE_PRIVATE_KEY < ~/Downloads/AuthKey_*.p8
```
(full file contents, not base64)

### Step 17 — `IOS_DIST_SIGNING_KEY_BASE64`
a) Open Xcode → **Settings** (Cmd+,) → **Accounts** tab.
b) Select your Apple ID → click **Manage Certificates...**.
c) Check if an **Apple Distribution** certificate already exists — if yes, reuse it (skip to step d). If not, click **+** → **Apple Distribution**.
d) Open **Keychain Access** app (Spotlight → "Keychain Access").
e) Find the **Apple Distribution: [your name]** certificate in the *login* keychain, under **My Certificates**.
f) Right-click it → **Export "Apple Distribution: ..."** → save as `DistributionCert.p12` to your Desktop.
g) It'll prompt for an export password — set one and remember it (needed in step 18).
h) It may also ask for your Mac login password to allow the export — enter it.
i) Run:
```bash
base64 -i ~/Desktop/DistributionCert.p12 | tr -d '\n' | gh secret set IOS_DIST_SIGNING_KEY_BASE64
```

### Step 18 — `IOS_DIST_SIGNING_KEY_PASSWORD`
a) The export password you set in step 17g.
b) Run:
```bash
gh secret set IOS_DIST_SIGNING_KEY_PASSWORD --body "PASTE_PASSWORD_HERE"
```

### Step 19 — `IOS_PROVISIONING_PROFILE_NAME`
a) App Store Connect → **Users and Access** → or directly https://developer.apple.com/account/resources/profiles/list
b) Look for an **App Store** type profile for bundle ID `xyz.ksharma.sumi`.
c) If none exists, click **+** → **App Store** distribution → select the `xyz.ksharma.sumi` App ID → select your distribution certificate → name it (e.g. "Sumi App Store") → **Generate**.
d) Copy the exact profile name shown.
e) Run:
```bash
gh secret set IOS_PROVISIONING_PROFILE_NAME --body "$(pbpaste)"
```

### Step 20 — `DEVELOPMENT_TEAM` (variable)
a) Go to https://developer.apple.com/account → **Membership details**.
b) Copy the **Team ID** (10 characters, e.g. `ABCDE12345`).
c) Run:
```bash
gh variable set DEVELOPMENT_TEAM --body "$(pbpaste)"
```

### Step 21 — `IOS_BUILD_NUMBER` (variable)
a) App Store Connect → your app → **TestFlight** tab → note the highest build number already uploaded.
b) Run (swap in that number, or `0` if unsure):
```bash
gh variable set IOS_BUILD_NUMBER --body "0"
```

---

## Verify everything landed

a) Run:
```bash
gh secret list
```
   Expect 18 secrets listed.
b) Run:
```bash
gh variable list
```
   Expect 3 variables: `ANDROID_VERSION_CODE`, `DEVELOPMENT_TEAM`, `IOS_BUILD_NUMBER`.
c) Go to https://github.com/ksharma-xyz/Sumi/actions → click **Distribute Google Play** in the left list → **Run workflow** → track `internal` → **Run workflow** button. This is a low-stakes dry run before trusting the full cut → RC → tag chain.

Local no-secret check (always runnable, doesn't need any of the above):
```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64 :composeApp:compileAndroidMain :composeApp:detekt :game:testAndroidHostTest
```
