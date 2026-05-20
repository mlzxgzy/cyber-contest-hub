# CCH - Cyber Contest Hub Knowledge Base

**Generated:** 2026-03-19  
**Project:** RuoYi-Vue-Plus + Soybean (CCH - Cyber Contest Hub)

## OVERVIEW

Multi-tenant enterprise management system combining RuoYi-Vue-Plus backend with Soybean Admin frontend.  
**Stack:** Java 17 + Spring Boot 3.5 + Vue 3 + TypeScript + Vite

## STRUCTURE

```
.
├── ruoyi-admin/          # Main Spring Boot entry point
├── ruoyi-common/         # Shared libraries (25+ modules)
├── ruoyi-modules/        # Business modules (system, workflow, job, generator)
├── ruoyi-extend/         # Extensions (monitor, snailjob)
├── cch-system/           # Custom business logic module
└── cch-ui/               # Vue3 frontend monorepo
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Main application | `ruoyi-admin/` | Spring Boot entry: `DromaraApplication.java` |
| Business modules | `ruoyi-modules/` | system, workflow, job, generator |
| Shared utilities | `ruoyi-common/` | core, security, mybatis, redis... |
| Custom logic | `cch-system/` | Domain-specific business logic |
| Frontend | `cch-ui/` | Vue 3 + TypeScript + Naive UI |
| Frontend packages | `cch-ui/packages/` | Monorepo packages (axios, hooks, utils) |
| Monitor | `ruoyi-extend/ruoyi-monitor-admin/` | Spring Boot Admin |
| Job scheduler | `ruoyi-extend/ruoyi-snailjob-server/` | SnailJob server |

## ENTRY POINTS

**Backend:**
- `ruoyi-admin/src/main/java/org/dromara/DromaraApplication.java`

**Frontend:**
- `cch-ui/src/main.ts`
- `cch-ui/package.json` - scripts: dev, build

## CONVENTIONS

### Java (Backend)
- **Indent:** 4 spaces (defined in `.editorconfig`)
- **ORM:** MyBatis-Plus (no XML, annotation-based)
- **Auth:** Sa-Token + JWT
- **DTO/VO/BO:** MapStruct for
- **ID:** Snowflake ID (not auto-increment)

### TypeScript (Frontend)
- **Indent:** 2 spaces for JSON/YAML
- **Style:** ESLint + `@soybeanjs/eslint-config`
- **CSS:** UnoCSS preferred over SCSS
- **State:** Pinia for store management
- **HTTP:** Alova (primary), Axios (legacy)

## KEY TECHNOLOGIES

| Layer | Technology |
|-------|------------|
| Web container | Undertow (not Tomcat) |
| Auth | Sa-Token + JWT |
| ORM | MyBatis-Plus 3.5.14 |
| Redis | Redisson (not Lettuce) |
| DB Pool | HikariCP (not Druid) |
| Excel | FastExcel (Alibaba EasyExcel) |
| Frontend UI | Naive UI 2.43 |
| Build | Vite 7 |

## ANTI-PATTERNS

- **NEVER** use `druid` - HikariCP is the standard
- **NEVER** use `PageHelper` - MyBatis-Plus pagination only
- **NEVER** use `fastjson` - Jackson is required
- **NEVER** use database auto-increment IDs - Snowflake only
- **NEVER** use XML for MyBatis - Annotation-based only
- Skip tests by default: `-DskipTests=true`

## BUILD COMMANDS

```bash
# Backend (Maven)
mvn clean package -DskipTests
mvn clean package -P prod

# Frontend (pnpm)
cd cch-ui
pnpm install
pnpm dev          # Development
pnpm build        # Production build
pnpm lint         # ESLint fix
pnpm typecheck    # TypeScript check
```

## MODULE BOUNDARIES

**ruoyi-common:** Shared across all modules, NEVER import from modules  
**ruoyi-modules:** Business logic, can import from ruoyi-common  
**cch-system:** Custom business, independent module  
**cch-ui:** Frontend monorepo, pnpm workspaces

## NOTES

- Multi-tenant SaaS architecture
- Code generation templates in `docs/template/`
- Workflow engine: Warm-Flow (ruoyi-workflow)
- Job scheduler: SnailJob (not Quartz)
- File storage: Minio + AWS S3 protocol
- Monitor: Spring Boot Admin + SkyWalking
