# CyklusCalk Project Walkthrough

This document summarizes the current progress of the **CyklusCalka** project, an Android application designed for cycle tracking and daily record management.

## 🛠 Tech Stack

The project leverages modern Android development tools and libraries:

-   **Jetpack Compose**: Used for building a modern, declarative UI.
-   **Hilt**: Handles Dependency Injection (DI) for better modularity and testability.
-   **Room**: Provides an abstraction layer over SQLite for local data storage.
-   **Gson**: Utilized for JSON serialization, specifically for complex data types in the database.
-   **java.time (Java 8 Date/Time API)**: Used for robust date handling (requires `minSdk 26`).

## 📁 Data Layer Design

The core data model is the [DayRecord](file:///C:/Users/MarianMaier/Android Studio/CyklusCalk/app/src/main/java/com/example/cykluscalk/data/DayRecord.kt) entity.

### JSON Tag Serialization
To store a list of tags (strings) in a relational database, the project uses [Converters](file:///C:/Users/MarianMaier/Android Studio/CyklusCalk/app/src/main/java/com/example/cykluscalk/data/Converters.kt). These Room `TypeConverters` use **Gson** to:
-   Serialize `List<String>` into a JSON string for storage.
-   Deserialize JSON strings back into `List<String>` when reading from the database.

```kotlin
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String = Gson().toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = ...
}
```

## 📱 UI Implementation

The user interface is centered around the [DayRecordScreen](file:///C:/Users/MarianMaier/Android Studio/CyklusCalk/app/src/main/java/com/example/cykluscalk/ui/DayRecordScreen.kt), which allows users to view and edit data for specific dates.

### Auto-saving Notes and Tags
The application implements an **auto-save** pattern to ensure data persistence without requiring a manual "Save" button:
-   **Notes**: As the user types in the `OutlinedTextField`, the `onValueChange` callback triggers `viewModel.saveNote(text)`, which persists the changes immediately via coroutines.
-   **Tags**: Toggling symptoms or moods using `FilterChip` components immediately updates the database through `viewModel.toggleTag(tag)`.

## ⚙️ Configuration Changes

-   **Minimum SDK**: The `minSdk` has been increased to **26** (Android 8.0) to natively support the `java.time` library without needing API desugaring.
