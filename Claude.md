# Project Overview

This is an Android application for tracking tasks and physical activity and losing weight.
Built with Kotlin and Jetpack Compose.


# Architecture

The project follows a layered architecture:
- UI — presentation and user interaction.
- Domain — business logic and domain models.
- Data — data sources and persistence.

# Rules

- Follow existing project conventions.
- Prefer existing patterns over introducing new ones.
- Do not add dependencies unless necessary.
- Do not add unnecessary data classes.
- Keep core business logic in the Domain layer. 
- UI and Data layers should not contain domain decision-making logic.
- UI layer should not contain domain models. 
- The UI layer should use screen-specific UI models mapped from Domain models. 
- Map Domain models to UI models in the UI layer.
- Do not expose Data models outside the Data layer.
- Prefer interfaces for abstractions and direct implementations when appropriate.
- separate file for non-private data classes