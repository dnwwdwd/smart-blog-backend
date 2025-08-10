# Project Summary

## Overview of Languages, Frameworks, and Main Libraries Used
This project is primarily developed using **Java** and utilizes the **Spring Framework** for building the application. The project also incorporates **MyBatis** for data access and object-relational mapping, as indicated by the presence of MyBatis configuration files and mapper XML files. The project is built using **Maven**, as evidenced by the `pom.xml` file and the Maven wrapper scripts (`mvnw` and `mvnw.cmd`).

## Purpose of the Project
The purpose of this project appears to be the development of a knowledge base application, likely aimed at managing user interactions, messages, and possibly other knowledge-related functionalities. The presence of controllers, services, and data transfer objects (DTOs) suggests that the application facilitates user management and communication features.

## List of Build/Configuration/Project Files
- **Build Files:**
  - `/mvnw`
  - `/mvnw.cmd`
  - `/pom.xml`

- **SQL Files:**
  - `/sql/create_table.sql`

## Source Files Directory
The source files can be found in the following directory:
- `/src/main/java/com/hjj/knowledgebase`

## Documentation Files Location
Documentation files are located in the following directory:
- `/src/main/resources/META-INF`
  - `additional-spring-configuration-metadata.json`
  - `application.yml`

## Additional Notes
- The project structure follows standard Maven conventions, with separate directories for main application code, resources, and test code.
- The presence of various packages such as `controller`, `service`, `mapper`, and `exception` indicates a well-organized codebase, adhering to common design patterns in Java development.