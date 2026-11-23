# Tasbih v1 — release checklist

Version is **1 (1.0)** — versionCode 1, versionName "1.0". Do not change it for the first upload.
Every later upload must have a higher versionCode (app/build.gradle.kts).

## 0. Decide the application ID (once)
`app/build.gradle.kts` → `applicationId = "com.akeel.tasbih"`.
It must be unique on Google Play and **can never be changed after the first upload**. Change it now if you want another one
(the code package `com.tasbih.app` does not need to match).

## 1. Create your upload key (once) — and back it up
```
keytool -genkeypair -v -keystore tasbih-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```
- Store the .jks file and both passwords in a safe place (password manager + an offline copy).
- Copy `keystore.properties.example` → `keystore.properties` and fill it in. Both files are git-ignored. Never commit them.
- In Play Console, keep **Play App Signing** on (default). Google keeps the real app-signing key; you only hold the upload key,
  which Google can reset if you ever lose it.

## 2. Build the release bundle
Android Studio: **Build → Generate Signed App Bundle / APK → Android App Bundle → release**.
Or from the terminal (first run `gradle wrapper --gradle-version 8.11.1` once to create ./gradlew):
```
./gradlew testDebugUnitTest        # unit tests
./gradlew bundleRelease            # → app/build/outputs/bundle/release/app-release.aab
```
R8 shrinking is on for release. The AAB already contains the mapping file, so crash reports in Play Console are readable.

## 3. Test the RELEASE build (not just debug) before uploading
R8 can behave differently from debug, so install the release build on a real phone:
```
./gradlew installRelease      # needs the signing config, or use bundletool
```
Walk through:
- [ ] First launch asks for a name; the greeting uses it; Settings → name edit works
- [ ] Slide and tap beads; count rises; reaching the target shows the round message; markers at 33 / 66
- [ ] Sound: Soft click, Wooden, Silent; volume slider; phone on silent = no sound; vibration on/off
- [ ] Switch dhikr; "After every prayer" changes words at 33, 66, 99
- [ ] Kill the app (swipe away) and reopen: count, dhikr, look, name are all still there
- [ ] Progress: today's total grows; tap other days and the calendar; glad-tidings cards show progress
- [ ] Change the daily goal; clear history (confirm dialog) works
- [ ] All 4 looks; floating bar on a phone with gesture navigation AND with 3-button navigation
- [ ] Airplane mode: everything works (it never needs internet)
- [ ] Rotate / large font (Settings → Display → Font size → Largest) — nothing is cut off badly
- [ ] TalkBack: the bead area announces the count and "Count one bead"
- [ ] Test on at least: one Android 8–9 phone/emulator, one Android 12–13, one Android 14–16

## 4. Play Console — new app
- **App access:** all functionality is available without login
- **Ads:** No
- **Content rating:** answer the IARC questionnaire honestly — no violence, sexuality, gambling or user-generated content → rated for everyone
- **Target audience:** 13+ (or 18+). Do not target children under 13 (it would pull in the Families policy).
- **News app / Health / Financial / Government:** No
- **Data safety:** "Does your app collect or share any of the required user data types?" → **No**. Data is not encrypted in transit
  question does not apply (no data is transmitted). Add the privacy policy URL.
- **Privacy policy:** host `PRIVACY_POLICY.md` as a web page (GitHub Pages is fine) and paste the link. Fill in the date and email first.
- **Store listing:** paste from `store_listing.md`; upload `play_store/icon_512.png`, `play_store/feature_graphic_1024x500.png` and 2–8 phone screenshots.
- **Countries:** all, or the ones you choose.
- **Permissions:** only VIBRATE (normal permission, no declaration needed).

## 5. Release path for a NEW developer account
Google currently requires new personal developer accounts to run a **closed test with at least 12 testers for 14 days** before
you can apply for production access (check Play Console — the rule changes from time to time). Typical flow:
1. Create a closed-testing track, upload `app-release.aab`, add 12+ testers by email / Google Group.
2. Testers opt in and keep it installed for 14 days.
3. Apply for production access, then promote the same AAB to Production.

## 6. After launch
- Watch Play Console → Android vitals → Crashes & ANRs.
- Add a new feature? Increase `versionCode` and `versionName`, rebuild, upload.
- If you ever change the database (Room), bump the DB version and write a Migration. The schema file for v1 is in `app/schemas/`; commit it.
