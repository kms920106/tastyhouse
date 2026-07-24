<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# chat

## Purpose
Standalone chat page with its own layout and colocated components. Renders a chat UI from mock data.

## Key Files
| File | Description |
|------|-------------|
| `page.tsx` | Chat route page |
| `layout.tsx` | Chat-specific layout |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `_components/` | Colocated chat UI components (conversation list, message thread, composer, etc.) |

## For AI Agents

### Working In This Directory
- Components used only by chat live in `_components/`; promote to `src/components/` only if reused elsewhere.
- Data is mock/local — no realtime backend.

## Dependencies

### Internal
- `src/components/ui/*`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
