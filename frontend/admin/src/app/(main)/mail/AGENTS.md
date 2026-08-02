<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# mail

## Purpose
Standalone mail/email page with its own layout and colocated components. Renders an inbox-style UI from mock data, typically using resizable panels.

## Key Files
| File | Description |
|------|-------------|
| `page.tsx` | Mail route page |
| `layout.tsx` | Mail-specific layout |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `_components/` | Colocated mail UI components (mail list, reading pane, toolbar, etc.) |

## For AI Agents

### Working In This Directory
- Components used only by mail live in `_components/`.
- Data is mock/local — no mail backend.

## Dependencies

### External
- `react-resizable-panels` (split panes), via `src/components/ui/resizable.tsx`

### Internal
- `src/components/ui/*`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
