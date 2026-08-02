<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# components

## Purpose
App-wide shared components. Contains the generated **shadcn/ui** primitive library (`ui/`) plus a few composite shared components used across multiple routes.

## Key Files
| File | Description |
|------|-------------|
| `date-range-picker.tsx` | Shared date-range picker (built on the calendar/popover primitives) |
| `simple-icon.tsx` | Renders brand icons from the `simple-icons` package |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `ui/` | shadcn/ui primitive components (see `ui/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- Put a component here only when it is shared across multiple routes; otherwise colocate it in the route's `_components/` folder.
- Compose styles with `cn()` from `src/lib/utils.ts`.

### Common Patterns
- Composite shared components build on `ui/` primitives rather than re-implementing them.

## Dependencies

### External
- `simple-icons`, `lucide-react`, `react-day-picker` / `date-fns` (date picker)

### Internal
- `src/components/ui/`, `src/lib/utils.ts`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
