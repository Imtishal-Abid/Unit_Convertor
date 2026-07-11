
# Unit Converter

A simple, fast unit converter app with a splash screen and customizable settings.

## Features

- **Length** — convert between meters (m) and centimeters (cm)
- **Weight** — convert between kilograms (kg) and grams (g)
- **Temperature** — convert between Celsius (°C) and Fahrenheit (°F)
- **Splash screen** — clean loading screen shown on app launch
- **Settings**
  - Toggle between light and dark theme
  - Clear conversion history

## Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for reference)
5. Remove this line from the app's `build.gradle.kts` file before creating a release build: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device

## View in AI Studio

[Open this app in AI Studio](https://ai.studio/apps/6c3ae99e-e556-45ee-a0e0-fa658ab46aeb)

## Roadmap

- [ ] Add more length/weight units (km, mm, lb, oz)
- [ ] Persist theme preference and history across sessions
- [ ] Add unit conversion for volume and area
