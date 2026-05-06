Status Report: TalentCircle Backend
Date: May 5th, 2026

Responsibles: Backend Team

Status: Technical integration completed and build validated.

1. Model Development (Marcelo's Contribution)
The initial domain logic has been consolidated through the creation of entities and enumerations that define the system's data structure:

Entity Mapping: Implementation of core classes: User, Community, CommunityPost, ChannelDraft, and WeeklyDigest.

Attribute Definition: Configuration of states and roles using Enums (Role, DigestStatus, DraftStatus) to standardize business logic.

2. Infrastructure and Persistence (Personal Contribution)
The database engine and layered architecture setup were completed:

DB Configuration: Stabilization of the local PostgreSQL 15 instance and linking with the talent schema.

Properties Management: Parameterization of the application.properties file for Spring Boot integration.

Data Access Layer (Repository): Implementation of UserRepository for persistence management.

Service Layer: Development of UserService to centralize business logic.

Controller Layer: Creation of UserController for API endpoint exposure.

3. Conflict Resolution and Integration
Environment Synchronization: Successful configuration of environment variables for JDK 21 (Eclipse Adoptium).

Merge Strategy: Branch fusion using the 'ort' strategy, integrating services with domain entities.

Jakarta Standardization: Validation of jakarta.persistence usage for compatibility with Spring Boot 3.x.

4. Build Status
Result: BUILD SUCCESS.

Processed Files: 12 source files correctly compiled under JDK 21.

Repository: Changes synchronized in the feature/backend-setup branch.

5. Technical Documentation & Developer Guide
Environment Setup
To maintain consistent environments between team members, we are using a Template-based Configuration.
•	Local Properties: The application.properties file is ignored by Git to avoid credential conflicts.
•	Instructions: New contributors must copy src/main/resources/application.properties.example to src/main/resources/application.properties and fill in their local database credentials.
•	Key Variables: The system is prepared to use environment variables for production: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, and JWT_SECRET.
Infrastructure (Docker)
We have containerized the database layer to ensure all developers use the same engine version.
•	Engine: PostgreSQL 15.
•	Quick Start: Run docker-compose up -d from the /Backend directory to launch the database instance.
•	Database Schema: The application is configured to connect to the talent schema.
Project Architecture
The backend follows a Standard Layered Architecture to ensure separation of concerns:
1.	Controller Layer: Handles REST API endpoints (UserController).
2.	Service Layer: Contains business logic (UserService).
3.	Repository Layer: Manages data persistence via Spring Data JPA (UserRepository).
4.	Entity Layer: Defines the domain models and database mappings.
Tech Stack Summary
•	Java Version: 21 (Eclipse Adoptium).
•	Framework: Spring Boot 4.0.6
•	Build Tool: Maven.
•	Persistence: Hibernate / Jakarta Persistence.
