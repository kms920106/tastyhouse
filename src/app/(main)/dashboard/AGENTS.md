<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-22 -->

# dashboard

## Purpose
The authenticated dashboard area. `layout.tsx` provides the shared chrome (sidebar, header, content frame) wrapped around every dashboard feature route. Each feature is its own route folder with a `page.tsx` and a colocated `_components/` folder. A `(legacy)` route group holds older v1 dashboard variants.

## Key Files
| File | Description |
|------|-------------|
| `layout.tsx` | Dashboard shell — renders the sidebar (`_components/sidebar`), header, and preference-driven layout; wraps all child routes |
| `page.tsx` | Default dashboard landing (`/dashboard`) |

## Subdirectories

### Shared
| Directory | Purpose |
|-----------|---------|
| `_components/` | Shared dashboard chrome — primarily the `sidebar/` (see `_components/sidebar/AGENTS.md`) |
| `[...not-found]/` | Catch-all 404 within the dashboard area |
| `(legacy)/` | Older v1 dashboard variants, grouped out of the URL (see `(legacy)/AGENTS.md`) |

### Feature routes (each: `page.tsx` + `_components/`)
| Directory | Purpose |
|-----------|---------|
| `default/` | Default dashboard (overview KPIs, recent customers table) |
| `crm/` | CRM dashboard (KPI cards, pipeline activity, opportunities table, task reminders) |
| `finance/` | Finance dashboard |
| `analytics/` | Analytics dashboard |
| `productivity/` | Productivity dashboard |
| `ecommerce/` | E-commerce dashboard (recent orders table) |
| `academy/` | Academy/learning dashboard |
| `logistics/` | Logistics dashboard |
| `invoice/` | Invoice page |
| `kanban/` | Kanban board (drag-and-drop via `@dnd-kit`) |
| `notices/` | Notices management (CRUD over `src/api/notice/` via `src/feature/notice/` Server Actions; table, detail/form sheets, delete dialog, with `loading.tsx`/`error.tsx`) |
| `users/` | Users management (table over `src/api/users.ts`) |
| `roles/` | Roles management (RBAC; roles table) |
| `chat/` | Chat screen within the dashboard shell |
| `mail/` | Mail screen within the dashboard shell |
| `coming-soon/` | Placeholder page for unreleased features (e.g. Calendar) |

## For AI Agents

### Working In This Directory
- **Adding a dashboard screen**: create `dashboard/<name>/page.tsx`, colocate UI in `dashboard/<name>/_components/`, then register it in `src/navigation/sidebar/sidebar-items.ts` so it appears in the sidebar.
- Shared chrome belongs in `_components/sidebar/`; per-feature widgets stay in that feature's `_components/`.
- Data tables use `@tanstack/react-table` with the `src/components/ui/table.tsx` primitive; charts use `recharts` via `src/components/ui/chart.tsx`. Several routes contain a nested `_components/<feature>-table/` folder holding the table's columns/definition.
- Use `coming-soon/` (or the `comingSoon` flag in sidebar items) for not-yet-built screens.

### Common Patterns
- One route folder per feature; `page.tsx` composes section components from `_components/`.
- All data is mock/static; wire real data via Server Actions (`src/server/`) when integrating a backend.

## Dependencies

### External
- `@tanstack/react-table`, `recharts`, `@dnd-kit/*` (kanban), `d3-geo`/`topojson-client` (maps), `lucide-react`

### Internal
- `src/navigation/sidebar/sidebar-items.ts`, `src/components/ui/*`, `src/stores/preferences/`, `src/api/`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
