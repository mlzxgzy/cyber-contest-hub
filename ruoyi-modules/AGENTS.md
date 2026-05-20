# ruoyi-modules

## OVERVIEW
The ruoyi-modules directory hosts four business-oriented modules forming the core of the system: ruoyi-system, ruoyi-workflow, ruoyi-job, and ruoyi-generator. Each module encapsulates its own domain logic with a REST API surface and a clear service layer boundary.

## MODULES
| Module | Purpose |
|---|---|
| ruoyi-system | Core domain for users, roles, departments, menus, tenants, and OSS governance |
| ruoyi-workflow | Workflow engine integration and process orchestration (Warm-Flow) |
| ruoyi-job | Distributed job scheduling and execution (SnailJob) |
| ruoyi-generator | Code generation scaffolding for rapid module bootstr |

## PATTERNS
- Controllers use @RestController to expose JSON endpoints for domain resources
- Service layer implements the standard interface + implementation pattern; services annotated with @Service
- Each request flows from controller to service, and service to repository/DAO as needed
- Transactions are managed at the service level with @Transactional where persistence occurs
- DTO/VO/BO are handled consistently to keep controllers thin and stable
- Error handling follows a uniform pattern to provide predictable responses across modules

## NOTES
- Domain boundaries are respected; modules should not leak infrastructure details into business logic
- Dependencies are primarily to ruoyi-common; keep module internals isolated
- Naming, test patterns, and response contracts are aligned across all modules
- Example: system endpoints for users, roles, and tenants follow standard REST conventions
- The generator module exposes templates for new modules, Controllers, Services, and DTOs to accelerate onboarding
