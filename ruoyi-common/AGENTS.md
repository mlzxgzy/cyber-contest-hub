# ruoyi-common

## OVERVIEW
ruoyi-common provides shared infrastructure and abstractions used by all ruoyi modules. It hosts core utilities, security scaffolds, data access patterns, web helpers and data handling layers. The goal is a consistent foundation for faster, safer module development.

## MODULE GROUPS
| Group | Modules | Purpose |
|---|---|---|
| Core & Utilities | ruoyi-common-core, ruoyi-common-tenant, ruoyi-common-translation | Foundational utilities, multi-tenant scaffolding, data and translation |
| Security & Identity | ruoyi-common-security | Authentication/authorization, Sa-Token integration, token hooks |
| Persistence & Caching | ruoyi-common-mybatis, ruoyi-common-redis | ORM/config, data access repositories, caching abstractions |
| Web & API Utilities | ruoyi-common-web | Web filters, request/response utilities, web helpers |
| Data Privacy & Crypto | ruoyi-common-sensitive, ruoyi-common-encrypt | Data masking, encryption/decryption primitives |

## USAGE
- Add the needed common modules as dependencies to your module
- For core utilities and multi-tenancy: ruoyi-common-core, ruoyi-common-tenant, ruoyi-common-translation
- For security: ruoyi-common-security
- For database access or caching: ruoyi-common-mybatis and ruoyi-common-redis
- For web helpers: ruoyi-common-web
- For data privacy and crypto helpers: ruoyi-common-sensitive and ruoyi-common-encrypt

## RULES
- Dependency direction: modules depend on ruoyi-common, not the other way around
- Avoid circular dependencies and keep common as a stable foundation
- New domain logic belongs in modules that depend on common, not within common itself

## NOTES
- This file describes shared contracts and conventions for ruoyi-common
- Tests and linting should validate integration points between common and consumer modules
