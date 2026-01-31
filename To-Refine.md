## nfc anim/components/NFCPaymentAnimation.tsx (Pass 6: Innovation & Chaos)

### 🚀 PRODUCT UNIVERSE (The Feature Creep)
- **WebNFC Reality Bridge:** Why simulate? Use `NDEFReader` to trigger the animation when a *real* physical card touches the phone! "Phygital" experience!
- **Haptic Immersion:** Call `navigator.vibrate([50, 200, 50])` on "success". Make the user *feel* the transaction.
- **Skin Economy:** "Moskvenok" is boring. Add a "Skin Store" for the card. Gold plated, Anime girl, Matrix code rain.
- **Audio Spatialization:** Add `useSound` hooks. "Bleep" for success, a sad trombone for error.

### 👺 SECURITY & GLITCHES (The Red Teamer)
- **Zombie State Injection:** The `sequence` async function has no abortion mechanism. If the component unmounts during `await setTimeout`, `setStage` will fire on a dead component. React memory leak warning guaranteed.
- **RNG Manipulation:** `Math.random()` determines success. In a demo, this is fine. In a "simulation" used for training, this is a bias vector.
- **Layout Thrashing:** `getCardPosition` checks `window.innerWidth` on every render if `stage` allows. A resize event listener loop could DoS the main thread.

### 📜 HERITAGE & DEBT (The Historian)
- **Hardcoded "Moskvenok":** The text is hardcoded. This component is now vendor-locked to Moscow Department of Education. Needs `props.cardLabel`.
- **Magic Coordinates:** `x: -150, y: -350`. What are these? Pixels? Why -350? This smells like "It looked good on my 1080p monitor".
- **Inline Icon Anachronism:** `CardsIcon` is defined inline while `lucide-react` is available. It's a second-class citizen in the codebase.

## app/src/main/java/org/bxkr/octodiary/MainActivity.kt (Pass 6: Innovation & Chaos)

### 🚀 PRODUCT UNIVERSE (The Feature Creep)
- **AI-Driven Battery:** Replace fixed polling with adaptive AI scheduling based on user usage patterns.
- **Mood Themes:** Dynamic "Mood Theme" that changes color based on user's recent grades (Red for 2, Green for 5).
- **Voice Assistant:** "Hey Octo" integration directly in `MainActivity`.

### 👺 SECURITY & GLITCHES (The Red Teamer)
- **Intent Poisoning:** `intent.dataString` is read without scheme/host validation. Potential for malicious deep links.
- **Global State Soup:** `MutableLiveData` globals (`navControllerLive`, etc.) are race conditions waiting to happen.
- **Bitmap OOM:** Manual scaling in `picker` is memory intensive. Although moved to IO, large images could still OOM.

### 📜 HERITAGE & DEBT (The Historian)
- **LiveData in Compose:** Using `MutableLiveData` instead of `StateFlow` is archaic for a Compose app.
- **Dead Code:** `registerNotifier` was a zombie function (Removed).
- **Legacy Activity:** Was `FragmentActivity`, modernized to `ComponentActivity`.