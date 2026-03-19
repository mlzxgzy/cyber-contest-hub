# cch-system

## OVERVIEW
cch-system is a custom domain module for CCH (Cyber Contest Hub). It encapsulates core business rules and domain logic used across the system. The module houses the domain model, domain services, data mappers, and orchestration tasks.

## STRUCTURE
- com.kdajv.cch.container: lifecycle and orchestration companions for domain ops
- com.kdajv.cch.controller: REST/internal endpoints for domain flows
- com.kdajv.cch.domain: core domain models and aggregates
- com.kdajv.cch.mapper: DTO-entity mappers for boundary crossing
- com.kdajv.cch.service: domain services implementing business rules
- com.kdajv.cch.task: asynchronous or scheduled tasks related to domain processes

## NOTES
- All code sits under the custom package com.kdajv.cch
- Follows standard Spring Boot module structure
- Align with project's Spring Boot conventions and package naming
- Use domain-driven design patterns: entities in domain, services orchestrating invariants
- Mappers translate between domain objects and DTOs used by controllers or schedulers
- Tasks represent async work; containers manage their lifecycle and dependencies
- Designed to be reused by other CCH modules via dependency injection and clear boundaries
