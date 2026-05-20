# ruoyi-admin

## OVERVIEW
Main Spring Boot backend entry for the RuoYi-Vue-Plus project. Boots the admin API layer and ties to the frontend via REST.

## STRUCTURE
- Project root contains Dockerfile, pom.xml, src/ and target/.
- Core sources live under src/main/java and resources under src/main/resources.
- This module is the backend entry point; frontend assets are in the parent/frontend module.

## ENTRY POINT
- Main class: DromaraApplication.java located at ruoyi-admin/src/main/java/org/dromara/DromaraApplication.java
- This class starts the Spring Boot application and wires the core modules.

## CONFIGURATION
- Core configuration lives in src/main/resources/application.yml (or application-<env>.yml)
- application.yml contains Spring Boot settings, data source, Redis, and security-related sections
- Logging configuration may live in src/main/resources/logback-spring.xml (if present)
- Environment-specific override files (e.g., application-dev.yml, application-prod.yml) may exist to tailor profiles
- Authentication uses Sa-Token + JWT; related settings are declared in the environment-specific or main YAML files

## NOTES
- Built with Maven; module root contains pom.xml and relies on the parent multi-module build
- Docker packaging uses the module's Dockerfile at the repo root for containerized runs
- Ensure Java version aligns with Spring Boot 3.x requirements (JDK 17+)
- Ports, data sources, and external service endpoints are controlled via application.yml and active Spring profiles
- This AGENTS.md should remain module-specific and not duplicate content from the parent AGENTS.md
