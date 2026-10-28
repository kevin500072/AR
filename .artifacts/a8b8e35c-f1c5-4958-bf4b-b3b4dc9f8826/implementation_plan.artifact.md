# Implementation Plan - CognoKinet Comprehensive Refactoring & Fixes

This plan outlines the steps to resolve all critical and important flaws, improve game coherence, design, accessibility, and clean up technical debt in the CognoKinet Kotlin Multiplatform app.

## User Review Required

> [!IMPORTANT]
> - **Teacher PIN Security**: The backdoor `1234` will be removed. Initial setup will prompt for a custom 4-digit PIN, stored securely (hashed).
> - **Auto-approval / Debug simulation**: The "Aprobar Automáticamente" button will be removed or restricted behind a teacher PIN dialog to prevent student cheating.
> - **Data Persistence**: We will implement JSON-based local serialization (or multiplatform storage) so profiles, progress, inventory, avatars, and requests persist across app restarts and screen rotations.

## Open Questions

- *None at this stage; all requirements are specified by the user.*

## Proposed Changes

### Core Security & Rules (C2, C3, C4)
#### [MODIFY] [CognokinetNavigation.kt](file:///C:/Users/kevin_ddiow5b/AndroidStudioProjects/AR/shared/src/commonMain/kotlin/com/example/ar/cognokinet/CognokinetNavigation.kt)
- Fix `navigateTo` guard: allow `TEACHER_DASHBOARD`, `HOME`, and `STUDENT_PROFILE` without an active student profile.
- Remove backdoor `1234` from teacher login.
- Implement proper PIN storage and validation.

### Data Model & State Management (I6, I7, I8, C7, I3, I4, I1)
#### [MODIFY] [StudentModel.kt](file:///C:/Users/kevin_ddiow5b/AndroidStudioProjects/AR/shared/src/commonMain/kotlin/com/example/ar/cognokinet/StudentModel.kt)
- Move inventory and avatar config into `StudentProgress` per student profile.
- Use `mutableStateMapOf` for progress map to ensure Compose observes changes.
- Implement monotonic unique IDs for profiles and requests to prevent collisions.
- Cascade delete profile data (progress, inventory, avatar, requests).

### Persistence & Storage (C1, I12)
#### [MODIFY] [CognokinetAppState.kt](file:///C:/Users/kevin_ddiow5b/AndroidStudioProjects/AR/shared/src/commonMain/kotlin/com/example/ar/cognokinet/CognokinetAppState.kt) (or within navigation/app state)
- Add serialization for state saving/loading.
#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/kevin_ddiow5b/AndroidStudioProjects/AR/androidApp/src/main/AndroidManifest.xml)
- Set `android:allowBackup="false"`.
#### [MODIFY] [androidApp/build.gradle.kts](file:///C:/Users/kevin_ddiow5b/AndroidStudioProjects/AR/androidApp/build.gradle.kts)
- Enable `isMinifyEnabled = true` for release builds.

### Screen Logic & Real Data (C5, C6, I5, I11, I2)
#### [MODIFY] [ResultsScreen.kt](file:///C:/Users/kevin_ddiow5b/AndroidStudioProjects/AR/shared/src/commonMain/kotlin/com/example/ar/cognokinet/ResultsScreen.kt)
- Accept real `SessionResult` from appState. Remove hardcoded dummy values. Use `levelForXp` and `XP_PER_LEVEL`.
#### [MODIFY] [MapScreen.kt](file:///C:/Users/kevin_ddiow5b/AndroidStudioProjects/AR/shared/src/commonMain/kotlin/com/example/ar/cognokinet/MapScreen.kt)
- Fix "You are here" marker (`completed + 1`). Handle locked station clicks (show modal or disable).
#### [MODIFY] [RetoDemoScreen.kt](file:///C:/Users/kevin_ddiow5b/AndroidStudioProjects/AR/shared/src/commonMain/kotlin/com/example/ar/cognokinet/RetoDemoScreen.kt)
- Lock/unlock chips based on `completedStationIds`. Remove or secure auto-simulation.
#### [MODIFY] [TeacherScreen.kt](file:///C:/Users/kevin_ddiow5b/AndroidStudioProjects/AR/shared/src/commonMain/kotlin/com/example/ar/cognokinet/TeacherScreen.kt)
- Support request status ("APROBADO", "REPETIR", "PENDIENTE") and display student results.
#### [MODIFY] [ShopScreen.kt](file:///C:/Users/kevin_ddiow5b/AndroidStudioProjects/AR/shared/src/commonMain/kotlin/com/example/ar/cognokinet/ShopScreen.kt)
- Fix purchase validation and error messaging.
#### [MODIFY] [StudentProfileScreen.kt](file:///C:/Users/kevin_ddiow5b/AndroidStudioProjects/AR/shared/src/commonMain/kotlin/com/example/ar/cognokinet/StudentProfileScreen.kt)
- Uncheck consent by default, validate age range (3-18).

### Design, Accessibility & Technical Debt (D1-D7, T1-T7)
- Unify typography (minimum 12sp secondary, 14sp body), touch targets (minimum 48dp).
- Remove orphan `game/` folder if present, clean up template files.
- Add comprehensive unit tests in `commonTest`.

## Verification Plan

### Automated Tests
- Run `./gradlew test` (or host test tasks) to verify unit tests for rewards, level calculation, badge evaluation, station unlocking, and ID generation.
- Run `./gradlew build` to ensure successful compilation across all targets.

### Manual Verification
- Deploy and verify profile creation, map progression, teacher dashboard access with PIN, and reward distribution.
