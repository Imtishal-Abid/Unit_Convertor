<div align="center">
<h1>📐 Unit Converter</h1>
<p>A simple, elegant Android app to convert length, weight, and temperature — with theme switching and history management.</p>
<p>
<img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Platform" />
<img src="https://img.shields.io/badge/Built%20with-Android%20Studio-blue?logo=androidstudio&logoColor=white" alt="Built with" />
<img src="https://img.shields.io/badge/Deployed%20via-Google%20AI%20Studio-orange" alt="AI Studio" />
</p>
</div>

##  Features

CategoryConversion📏 LengthMeters (m) ↔ Centimeters (cm)⚖️ WeightKilograms (kg) ↔ Grams (g)🌡️ TemperatureCelsius (°C) ↔ Fahrenheit (°F)


🚀 Splash Screen — smooth branded loading screen on launch
⚙️ Settings

🌗 Switch between Light and Dark theme
🗑️ Clear conversion history in one tap
## 📱 Screenshots

<div align="center">
  <i><img width="314" height="599" alt="unit1" src="https://github.com/user-attachments/assets/85b72b92-071c-4247-99f3-bc1a47921033" />
</i>
</div>


## Tech Stack

<div align="center">
<p>
<img src="https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
<img src="https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white" alt="Gradle" />
<img src="https://img.shields.io/badge/Android%20Studio-3DDC84?logo=androidstudio&logoColor=white" alt="Android Studio" />
<img src="https://img.shields.io/badge/Google%20AI%20Studio-orange?logo=google&logoColor=white" alt="Google AI Studio" />
</p>
</div>

## 🚀 Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Clone this repository
   ```bash
   git clone https://github.com/Imtishal-Abid/<Unit_Convertor>.git
   ```
2. Open **Android Studio** → **Open** → select the project folder
3. Let Android Studio sync and resolve dependencies
4. Create a `.env` file in the project root and add your Gemini API key:
   ```
   GEMINI_API_KEY=your_api_key_here
   ```
   *(see `.env.example` for reference)*
5. Before building a **release** version, remove this line from `build.gradle.kts`:
   ```kotlin
   signingConfig = signingConfigs.getByName("debugConfig")
   ```
6. Run the app on an emulator or a physical device ▶️

---

## 🗺️ Roadmap

- [ ] Add more units (km, mm, lb, oz)
- [ ] Persist theme & history across sessions
- [ ] Add volume and area conversions

---
<div align="center">
  Made with ❤️ by <b>Imtishal Abid</b>
</div>
