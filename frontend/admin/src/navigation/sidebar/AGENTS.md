<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# sidebar

## Purpose
Source-of-truth data structure for the dashboard sidebar menu.

## Key Files
| File | Description |
|------|-------------|
| `sidebar-items.ts` | Exports the nav item tree and the `NavMainItem` / `NavSubItem` interfaces. Each item has `title`, `url`, optional Lucide `icon`, nested `subItems`, and flags `comingSoon` / `newTab` / `isNew` |

## For AI Agents

### Working In This Directory
- To add a dashboard route to the sidebar, append an item here with the matching `url` (Lucide icon imported from `lucide-react`).
- Use `comingSoon: true` for placeholder routes (e.g. Calendar) and `isNew: true` to badge new entries — the renderer reads these flags.

### Common Patterns
- Pure data + interfaces; no rendering logic. Icons are `LucideIcon` references, not strings.

## Dependencies

### External
- `lucide-react` (icon components)

### Internal
- Rendered by `src/app/(main)/dashboard/_components/sidebar/nav-main.tsx` and related nav components.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
