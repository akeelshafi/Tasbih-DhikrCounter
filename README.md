# Tasbih  (v1 · 1.0)

A calm, fully offline tasbih (prayer beads) app with a daily dhikr tracker.
Kotlin · Jetpack Compose · Material 3 · Navigation Compose (type-safe routes) · Room · DataStore · Splash Screen API · edge-to-edge · predictive back.
No internet permission, no ads, no accounts, no audio files.

Read **PLAY_STORE_CHECKLIST.md** to publish. Listing text: `store_listing.md`. Privacy policy: `PRIVACY_POLICY.md`.

## Run it
1. Open this folder in Android Studio (latest stable) and let Gradle sync.
2. Run on a phone or emulator (Android 8.0+).

## What is inside
- **Tasbih** — real beads on a string with a tassel, inside a mihrab arch. Slide a bead up or tap.
- **Progress** — daily history saved on the phone: pick a day, see the total, the daily-goal ring, what was recited, consistent days, best day and all-time total.
- **More dhikr** — 24 dhikr in groups (after prayer, glorify Allah, oneness, forgiveness, salawat, trust and duas).
- **Glad tidings** — hadith-based virtues (Sahih al-Bukhari, Sahih Muslim) with progress for the selected day. No reward numbers are invented.
- **Settings** — name, four looks, sound (soft click / wooden / silent) with volume, vibration, keep-screen-on, daily goal, clear history.
- A floating navigation bar that takes the colours of the active look.

## Tools (optional, need Python with Pillow + numpy)
- `python3 tools/make_icon.py` — regenerates the app icon (vector drawables), `play_store/icon_512.png` and the feature graphic.
- `python3 tools/sound_preview.py` — writes `play_store/sound_previews/*.wav`, a Python mirror of the in-app sound recipe, so you can listen on a computer.

## Where to tweak
- Dhikr list and hadith text: `data/Dhikr.kt`
- Bead sound recipe: `ui/SoundSynth.kt` (+ `tools/sound_preview.py`)
- Looks / colours / bead materials: `ui/Theme.kt`
- Floating bar: `ui/FloatingNav.kt` · Name box: `ui/NameField.kt`
- Bead size and perspective: `unit`, `r`, `y0` in `ui/BeadString.kt`
