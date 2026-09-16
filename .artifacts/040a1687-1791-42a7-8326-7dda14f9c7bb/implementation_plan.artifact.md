# Fix Gradle Sync Error: Duplicate 'kotlin' Extension

The project is failing to sync with the error `Cannot add extension with name 'kotlin', as there is an extension already registered with that name`. This typically occurs due to version mismatches or duplicate plugin applications in modern Gradle/Kotlin environments (Kotlin 2.0+ and AGP 8.x/9.x).

## User Review Required

> [!NOTE]
> I am updating the versions of AGP, Kotlin, Hilt, and KSP to the latest stable versions available in your environment. This is recommended to resolve internal plugin conflicts present in older preview versions.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/MarianMaier/Android Studio/CyklusCalk/gradle/libs.versions.toml)
- Update `agp` to `9.4.0`
- Update `kotlin` to `2.4.20`
- Update `hilt` to `2.60.1`
- Update `ksp` to `2.4.20-1.0.30` (matching the Kotlin version)

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/MarianMaier/Android Studio/CyklusCalk/app/build.gradle.kts)
- Ensure the `plugins` block order follows the standard: `android.application`, `kotlin.android`, then others.
- Migrate `kotlinOptions` to the modern `compilerOptions` to align with Kotlin 2.0+ standards.

## Verification Plan

### Automated Tests
- Run `gradle_sync` to verify the error is resolved.
- Run `app:assembleDebug` to verify the build process.

### Manual Verification
- None required beyond a successful sync and build.
