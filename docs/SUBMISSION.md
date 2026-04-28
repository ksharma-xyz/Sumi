# Sumi — Submission Checklist

> Per-app one-pager. Each step links to the canonical playbook in
> [app-release-playbooks](https://github.com/ksharma-xyz/app-release-playbooks).

---

## App identity

| Field | Value |
|---|---|
| Public name (App Store + Play) | **`Sumi: Zen Sudoku`** *(or `Sumi Sudoku` — confirm before filing)* |
| Bundle ID (iOS) | `xyz.ksharma.sumi.Sumi` *(verify against `Info.plist` / Xcode target)* |
| Application ID (Android) | `xyz.ksharma.sumi` |
| Debug suffix (Android) | `xyz.ksharma.sumi.debug` |
| Subtitle (App Store, max 30) | *TODO — e.g. "Calm puzzles, ink and paper"* |
| Short description (Play, max 80) | *TODO* |
| Privacy Policy URL | *TODO — host on GitHub Pages, e.g. `https://ksharma-xyz.github.io/sumi-site/privacy`* |
| Support URL | *TODO — same domain, `/support`* |
| Contact email | *TODO — e.g. `heysumi@gmail.com`* |
| Apple Team ID | *from developer.apple.com → Membership* |
| AdMob Android App ID | *currently using Google's test ID `ca-app-pub-3940256099942544~3347511713` — replace with real before release* |
| AdMob iOS App ID | *currently using test ID `ca-app-pub-3940256099942544~1458002511` — replace with real before release* |
| Android keystore alias | `sumi-key` *(in shared `~/ksharma-xyz/krail_key.jks`)* |

---

## Status

### Apple

- [ ] App Store Connect record created
- [ ] App name registered (resolve "Sumi" availability — likely use `Sumi: Zen Sudoku`)
- [ ] Distribution certificate + provisioning profile generated
- [ ] App Store Connect API key generated and stored in 1Password + GitHub Secrets
- [ ] Privacy Nutrition Labels filled → see [`PRIVACY_NUTRITION_LABELS.md`](https://github.com/ksharma-xyz/app-release-playbooks/blob/main/ios/PRIVACY_NUTRITION_LABELS.md)
      - Identifiers → Device ID (linked + tracked, AdMob)
      - Usage Data → Product Interaction + Advertising Data
      - Diagnostics → Crash Data
      - Purchases → Purchase History (when IAP ships)
- [ ] App Tracking declared = Yes (AdMob with personalised ads)
- [ ] Content Rights = **No** (AdMob ads aren't Content Rights; quotes are public-domain attributed aphorisms — see playbook)
- [ ] Age Rating completed → 4+ (no objectionable content)
- [ ] Export Compliance: `ITSAppUsesNonExemptEncryption = false` already in `Info.plist` ✓
- [ ] ATT prompt fires on first launch — verify on real device → see [`ATT_AND_TRACKING.md`](https://github.com/ksharma-xyz/app-release-playbooks/blob/main/ios/ATT_AND_TRACKING.md)
- [ ] `PrivacyInfo.xcprivacy` added (iOS 17+ requirement)
- [ ] First TestFlight build uploaded
- [ ] Internal testing with at least one real device
- [ ] App Review notes drafted → see [`REVIEW_NOTES_TEMPLATE.md`](https://github.com/ksharma-xyz/app-release-playbooks/blob/main/shared/REVIEW_NOTES_TEMPLATE.md)
- [ ] Screenshots prepared (5–7 × iPhone 17 Pro Max, 5–7 × iPad if shipping iPad)
- [ ] Privacy policy URL live + 200 in incognito
- [ ] Submit for App Store Review
- [ ] Approved + released

### Google

- [ ] Play Console record created
- [ ] Play Developer identity verification complete
- [ ] AD_ID permission in `AndroidManifest.xml` ✓ (added in commit `caf3637`)
- [ ] Keystore alias `sumi-key` exists in `~/ksharma-xyz/krail_key.jks` (verify with `keytool -list`)
- [ ] Play App Signing enabled — choose Path A (Google-managed) for v1.0
- [ ] Service account for CI uploads created and JSON key in GitHub Secrets
- [ ] Firebase Android app registered with both `xyz.ksharma.sumi` and `xyz.ksharma.sumi.debug`
- [ ] SHA-1 + SHA-256 of upload key registered in Firebase
- [ ] Data Safety form filled → see [`DATA_SAFETY_FORM.md`](https://github.com/ksharma-xyz/app-release-playbooks/blob/main/android/DATA_SAFETY_FORM.md)
      - Yes, collects data
      - Encrypted in transit
      - No deletion request mechanism (no account)
      - Device IDs (AdMob), App interactions (Firebase Analytics), Crash logs, Diagnostics
- [ ] Content Rating: 3+ (Everyone) → see [`CONTENT_RATING.md`](https://github.com/ksharma-xyz/app-release-playbooks/blob/main/android/CONTENT_RATING.md)
- [ ] App contains ads = Yes
- [ ] Target audience: Adults
- [ ] App access: All functionality available without sign-in
- [ ] Store listing complete: name, descriptions, icon (512×512), feature graphic (1024×500), screenshots (5+ per form factor)
- [ ] Internal Play track release validated on real Android device
- [ ] Native debug symbols uploaded → see [`NATIVE_DEBUG_SYMBOLS.md`](https://github.com/ksharma-xyz/app-release-playbooks/blob/main/android/NATIVE_DEBUG_SYMBOLS.md)
- [ ] Mapping file (`mapping.txt`) uploaded for R8 deobfuscation
- [ ] Promote to Production
- [ ] Approved + 100% rolled out

---

## Sumi-specific notes

**Quotes are public-domain attributed aphorisms** (da Vinci, Confucius, Ram Dass, etc.). Short, well-known sayings with attribution — universally treated as fair use. **Content Rights = No** for both stores.

**No account, no server.** All gameplay is offline; only AdMob + Firebase touch the network.

**Pro features hidden behind a "Simulate Pro" debug toggle for now** (gated by `BuildKonfig.IS_DEBUG`). Real StoreKit + Play Billing wiring is a future task — when shipping IAP:
- Bump nutrition labels to include "Purchase History"
- Add Play Billing product (`sumi_pro` one-time)
- Add StoreKit product to App Store Connect → In-App Purchases
- Update `ProRepository` to read real purchase state

**Test ad unit IDs in production builds.** BuildKonfig currently uses Google's test ad unit IDs in **both** debug and release flavors of `composeApp/build.gradle.kts`. Before shipping the production build:
- Create real Sumi Banner / Interstitial / Rewarded units in AdMob console (separately for Android + iOS)
- Replace the `AD_*` constants in `defaultConfigs("release")` block

**App Store Connect bundle ID anomaly.** Xcode-generated bundle ID is `xyz.ksharma.sumi.Sumi` (with the trailing `.Sumi` from the target name). The Firebase warning (`I-COR000008`) noted this previously — verify what's actually in the Xcode target General tab vs `Info.plist` and confirm Firebase matches before TestFlight.

---

## Open questions / blockers

- [ ] Confirm App Store name availability — try `Sumi`, fall back to `Sumi: Zen Sudoku`
- [ ] Decide privacy/support URL hosting — recommend GitHub Pages at `sumi.app` or `ksharma-xyz.github.io/sumi-site`
- [ ] Generate real AdMob Android + iOS App IDs and unit IDs (test IDs work for TestFlight + Internal track but NOT for Production)
- [ ] Generate App Store Connect API key
- [ ] Verify Apple Developer + Play Developer identity verifications are complete
- [ ] Decide whether to ship v1.0 with IAP wired or defer Pro to v1.1
