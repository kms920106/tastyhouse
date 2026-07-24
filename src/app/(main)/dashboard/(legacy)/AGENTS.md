<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# (legacy)

## Purpose
Route group holding the **v1 (legacy)** dashboard variants kept for reference/comparison against the current designs. The `(legacy)` group name does not appear in URLs; each child is a normal route with its own `page.tsx` and colocated `_components/`.

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `default-v1/` | Legacy default dashboard (incl. `_components/proposal-sections-table/`) |
| `crm-v1/` | Legacy CRM dashboard (incl. `_components/recent-leads-table/`) |
| `finance-v1/` | Legacy finance dashboard (incl. `_components/kpis/`) |
| `analytics-v1/` | Legacy analytics dashboard |

## For AI Agents

### Working In This Directory
- These are **superseded** screens. Prefer building on the current dashboards (`dashboard/default`, `dashboard/crm`, etc.); only touch these for parity or before removal.
- Same colocation conventions as the active routes (`page.tsx` + `_components/`).

## Dependencies

### External
- `@tanstack/react-table`, `recharts`

### Internal
- `src/components/ui/*`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
