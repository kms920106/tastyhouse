<!-- Parent: ../../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# sidebar (dashboard chrome)

## Purpose
The dashboard's sidebar and its controls — the shared navigation chrome rendered by `dashboard/layout.tsx`. Renders the nav tree from `src/navigation/sidebar/sidebar-items.ts` and exposes theme/layout customization.

## Key Files
| File | Description |
|------|-------------|
| `app-sidebar.tsx` | Top-level sidebar composition (assembles the sections below) |
| `nav-main.tsx` | Renders the main nav items/sub-items from `sidebar-items.ts` (handles `comingSoon`/`isNew`/`newTab` flags) |
| `nav-secondary.tsx` | Secondary nav section (e.g. settings/help links) |
| `nav-documents.tsx` | "Documents"-style nav section |
| `nav-user.tsx` | User profile menu at the sidebar footer |
| `account-switcher.tsx` | Account/tenant switcher (multi-tenant placeholder) |
| `theme-switcher.tsx` | Color-preset + light/dark mode switcher (writes to preferences store) |
| `layout-controls.tsx` | Layout option controls (sidebar variant, content width, etc.) |
| `search-dialog.tsx` | Command-palette / search dialog (`cmdk`) |
| `sidebar-support-card.tsx` | Promotional/support card shown in the sidebar |

## For AI Agents

### Working In This Directory
- Nav content is **data-driven** — to add/remove menu entries, edit `src/navigation/sidebar/sidebar-items.ts`, not these components.
- `theme-switcher.tsx` and `layout-controls.tsx` call setters on the Zustand preferences store (`src/stores/preferences/`), persisted via `src/lib/preferences/`.
- Built on the `src/components/ui/sidebar.tsx` primitive; responsive behavior uses `src/hooks/use-mobile.ts`.

## Dependencies

### External
- `cmdk` (search), `lucide-react`, `next-themes`

### Internal
- `src/navigation/sidebar/sidebar-items.ts`, `src/components/ui/sidebar.tsx`, `src/stores/preferences/`, `src/lib/preferences/`, `src/hooks/`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
