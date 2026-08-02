<!-- Generated: 2026-06-16 | Updated: 2026-06-22 -->

# tastyhouse-admin

## Purpose
A modern admin dashboard starter built with Next.js 16 (App Router), TypeScript, Tailwind CSS v4, and shadcn/ui. It ships multiple prebuilt dashboards (Default, CRM, Finance, Analytics, Productivity, E-commerce, Academy, Logistics), authentication layouts, chat/mail pages, and customizable theme presets with light/dark modes and layout controls. Synced from the `next-shadcn-admin-dashboard` template and rebranded as **Tastyhouse Admin**.

## Key Files
| File | Description |
|------|-------------|
| `package.json` | Dependencies, scripts, and template provenance. Dev/start server runs on port **3010** |
| `next.config.mjs` | Next.js configuration |
| `tsconfig.json` | TypeScript config (app); path alias `@/*` → `src/*` |
| `tsconfig.scripts.json` | Separate TS config used by the `generate:presets` script |
| `biome.json` | Biome linter/formatter config (replaces ESLint + Prettier) |
| `components.json` | shadcn/ui CLI config (component install paths, style, aliases) |
| `postcss.config.mjs` | PostCSS config for Tailwind v4 |
| `next-env.d.ts` | Next.js generated type declarations |
| `README.md` | Project overview, feature list, getting-started |
| `CONTRIBUTING.md` | Contribution flow and where-to-contribute map |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/` | All application source code (see `src/AGENTS.md`) |
| `media/` | Static screenshots/assets used by the README |

## For AI Agents

### Working In This Directory
- This is a **template/starter**: data is mock (see `src/api/`), there is no real backend or database. Auth screens are UI-only.
- Package manager is **npm** (`package-lock.json` present). After editing `package.json`, run `npm install`.
- The repo was synced from an upstream template (`template` field in `package.json`). Preserve the colocation architecture when adding features.

### Testing Requirements
- There is no test suite. Validate changes with the linter and a build:
  - `npm run check` — Biome lint + format check
  - `npm run check:fix` — auto-fix
  - `npm run build` — full Next.js production build (the real correctness gate)
- Husky pre-commit hooks run `biome check --write` on staged JS/TS files via lint-staged; a failing check blocks the commit.

### Common Patterns
- **Colocation architecture**: each route folder keeps its own `page.tsx` and a `_components/` folder for components used only by that route. Shared UI/hooks/config live at the `src/` top level.
- TypeScript strict; prefer explicit types over `any`.
- Follow shadcn/ui + Tailwind v4 conventions. Compose class names with `cn()` from `src/lib/utils.ts`.
- Conventional commit prefixes (`feat:`, `fix:`, `chore:`).

## Dependencies

### External (key)
- `next` 16, `react` 19, `react-dom` 19 — framework (App Router, React Compiler enabled via `babel-plugin-react-compiler`)
- `tailwindcss` v4, `@tailwindcss/postcss`, `tw-animate-css` — styling
- `radix-ui` / `@base-ui/react`, `class-variance-authority`, `clsx`, `tailwind-merge` — shadcn/ui primitives
- `zustand` — client state; `react-hook-form` + `zod` + `@hookform/resolvers` — forms/validation
- `@tanstack/react-table` — data tables; `@dnd-kit/*` — drag-and-drop (kanban)
- `recharts`, `d3-geo`, `topojson-client` — charts and maps
- `lucide-react`, `simple-icons` — icons; `next-themes` — theme switching; `sonner` — toasts
- `@biomejs/biome`, `husky`, `lint-staged`, `ts-node` — tooling

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
