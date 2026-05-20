# cch-ui

## OVERVIEW
cch-ui is a Vue 3 + TypeScript + Vite monorepo managed with pnpm workspaces. It powers the frontend of the Cyber Contest Hub with a modern Vue stack. UI toolkit is Naive UI 2.43, styling via UnoCSS, and state via Pinia. Build tooling uses Vite 7.

## STRUCTURE
The repo follows a two-part layout: a src/ app directory for the UI and a packages/ workspace for shared libraries.
- Main application code lives under src/, including layouts, pages, components, store setup, and routing glue
- src/layouts, src/pages, src/components, and src/store are organized by feature and UI concerns
- Packages are under packages/ and consumed by the app and other packages
- pnpm workspaces link packages/ with root dependencies and enable hoisting

## PACKAGES
| Package | Purpose |
|---|---|
| axios | HTTP client wrappers and adapters |
| alova | Data fetching and API integration layer |
| hooks | Reusable Vue composition API utilities |
| utils | Shared helpers and utilities used across the UI |
| materials | Design assets, tokens, and UI utilities |
| color | Color tokens and theming resources |
| scripts | Development and build helpers for the monorepo |
| uno-preset | UnoCSS presets for consistent styling |

## COMMANDS
- Development: pnpm dev
- Production: pnpm build
- Lint: pnpm lint
- Type checking: pnpm typecheck
- Install deps: pnpm install -w

## CONVENTIONS
- UnoCSS is preferred for styling; keep class names simple and meaningful
- Use PascalCase for Vue components and organize by feature
- Pinia stores live in src/store; type-safe hooks in hooks package when possible
- TypeScript used throughout; keep close to usage sites
- Packages in packages/ are shared libraries consumed by the UI and other apps

## NOTES
- This directory is the frontend monorepo entry point for the CCH project
- Packages are built and consumed through the workspace; changes propagate via the monorepo tooling
- The monorepo uses pnpm workspaces and aligns with the CCH UI tech stack (Vue 3, TS, Vite)
