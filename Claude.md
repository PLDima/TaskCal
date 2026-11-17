# Project Overview

This is an Android application for tracking tasks and physical activity and losing weight.
Built with Kotlin and Jetpack Compose.


# Architecture

The project follows a layered architecture:
- UI — presentation and user interaction.
- Domain — business logic and domain models.
- Data — data sources and persistence.
- DI - all dependency injection related modules, files or providers.

Each layer is a package under `inc.dimasic.taskcal`:

```
ui/
  screens/           screens, ViewModels, and the UI models they map to
  theme/
domain/
  model/             entities and value objects — Task, FoodEntry, Priority, DateRange
  usecase/           use cases, plus pure domain rules (PeriodRanges, GoalProgress, TaskRecurrence)
  usecase/model/     what use cases return — summaries, list items, chart points
data/
  repository/        repositories
  model/             shapes that only exist in storage
  datastore/  healthconnect/
```

Dependencies point inwards: UI depends on Domain, Domain depends on Data. A repository never
reaches back into Domain or UI, and nothing in Domain imports from `ui`.

Tasks and Activities are the reference implementations — follow them when in doubt.

# Rules

- Follow existing project conventions.
- Prefer existing patterns over introducing new ones.
- Do not add dependencies unless necessary.
- Do not add unnecessary data classes.
- Keep core business logic in the Domain layer.
- UI and Data layers should not contain domain decision-making logic.
  - Repositories store and return data, nothing more. What dates a period covers, how things are
    grouped or ordered, and how a value measures against a goal are Domain decisions.
- UI layer should not contain domain models.
  - Composables may render Domain *enums* they select from — `Priority`, `RecurrenceType`,
    `HealthConnectStatus`, the period enums. Duplicating those as UI enums would add a type and
    an identity mapping without adding information. Composables may not hold Domain entities.
- The UI layer should use screen-specific UI models mapped from Domain models.
  - Add one when the mapping does real work: wording a date as "Tomorrow", flattening a state to
    a boolean, pre-formatting a macro line. When a Domain model is already field-for-field what
    the screen renders, pass it through instead.
- Map Domain models to UI models in the UI layer.
  - In the ViewModel — the one place allowed to see both sides.
- Do not expose Data models outside the Data layer.
- Prefer interfaces for abstractions and direct implementations when appropriate.
  - The repositories are concrete while they are in-memory mocks. Add interfaces when a real
    data source arrives.
- Keep each non-private data class in a separate file.

# Testing

Unit tests mirror the main source tree under `app/src/test/java`. Pure domain rules are the ones
worth covering — see `TaskRecurrenceTest` for recurrence expansion and `ActivitiesUseCaseTest`
for aggregation. `firstValue()` in `FlowTestSupport.kt` takes the first value off a flow, so a
use case can be driven with `flowOf(...)` inputs without a coroutine test dependency.

The test fixtures build their data relative to `LocalDate.now()`, so assert on invariants and
relationships rather than fixed numbers. In-memory DAO fakes live in `test/.../data/database`, so
a use case can be driven through its real repository without Room.

Java is not on `PATH`; build with the Android Studio JBR:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:assembleDebug :app:testDebugUnitTest
```
