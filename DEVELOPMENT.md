# Development

## Notes on development methodology

**This project was completely vibe-coded**, 100%, with the help of Claude Code and Opus 4.5. There it is, I said it, the shame is now gone.

I am pretty skeptical on LLM-generated code and this was an experiment to learn how it actually was *vibe-coding* something from scratch.

Turns out it's pretty awesome and easy to follow the happy path for a stock, modern Android app which just displays data consumed from a JSON REST API. I'm a DevOps guy and I have zero mobile development skills, so achieving this can be considered pretty awesome in my book. But I'm pretty sure in the eyes of a skilled Kotlin Android developer, this code might induce different feelings.

At the moment I totally depend on CC to maintain the app, but I would like to take this exercise as an excuse to learn more about this ecosystem, beside learning the AI-powered ecosystem as well.

### Project Structure

```
matedroid/
├── app/src/main/java/com/matedroid/
│   ├── data/           # Data layer (API, repository, local storage)
│   ├── domain/         # Domain layer (models, use cases)
│   ├── ui/             # UI layer (screens, components, theme)
│   └── di/             # Dependency injection modules
├── gradle/             # Gradle wrapper and version catalog
├── util/               # Utility scripts
├── ASSETS.md           # Tesla car image asset documentation
└── PLAN.md             # Detailed implementation plan
```

### Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp + Moshi
- **Local Storage**: DataStore
- **Charts**: Vico
- **Maps**: osmdroid (OpenStreetMap)
