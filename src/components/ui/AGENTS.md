<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# ui

## Purpose
The **shadcn/ui** primitive component library — copy-in, fully-owned components built on Radix/Base UI primitives, `class-variance-authority`, and Tailwind v4. These are the building blocks for every screen in the app (~55 components).

## Key Files
A representative selection (each file is one primitive):

| File | Description |
|------|-------------|
| `button.tsx` | Button with `cva` variants |
| `card.tsx` | Card container parts |
| `dialog.tsx`, `sheet.tsx`, `drawer.tsx` | Overlays (modal, side panel, bottom drawer via `vaul`) |
| `dropdown-menu.tsx`, `context-menu.tsx`, `menubar.tsx`, `navigation-menu.tsx` | Menu primitives |
| `select.tsx`, `native-select.tsx`, `combobox.tsx`, `command.tsx` | Selection / command palette (`cmdk`) |
| `table.tsx` | Table primitives (paired with `@tanstack/react-table` in features) |
| `chart.tsx` | Recharts wrapper/theming |
| `sidebar.tsx` | Sidebar primitive used by the dashboard chrome |
| `form` parts: `field.tsx`, `label.tsx`, `input.tsx`, `textarea.tsx`, `checkbox.tsx`, `radio-group.tsx`, `switch.tsx`, `slider.tsx`, `input-otp.tsx` | Form controls |
| `sonner.tsx` | Toast host (`sonner`) |
| `calendar.tsx` | Calendar (`react-day-picker`) |
| `carousel.tsx` | Carousel (`embla-carousel-react`) |
| `resizable.tsx` | Resizable panels (`react-resizable-panels`) |

…plus `accordion`, `alert`, `alert-dialog`, `aspect-ratio`, `avatar`, `badge`, `breadcrumb`, `button-group`, `collapsible`, `direction`, `empty`, `hover-card`, `input-group`, `item`, `kbd`, `pagination`, `popover`, `progress`, `scroll-area`, `separator`, `skeleton`, `spinner`, `tabs`, `toggle`, `toggle-group`, `tooltip`.

## For AI Agents

### Working In This Directory
- These are **owned source files**, not an npm package — prefer adding new primitives via the shadcn CLI (`components.json` config) so they match conventions, then customize.
- Keep variant logic in `cva` definitions; merge classes with `cn()` from `src/lib/utils.ts`.
- Avoid one-off business logic here; primitives stay generic. Feature behavior belongs in route `_components/`.

### Common Patterns
- `forwardRef` + Radix/Base UI `asChild` composition.
- Variant props defined above the component via `class-variance-authority`.

## Dependencies

### External
- `radix-ui` / `@base-ui/react`, `class-variance-authority`, `clsx`, `tailwind-merge`
- Per-component: `cmdk`, `vaul`, `sonner`, `recharts`, `react-day-picker`, `embla-carousel-react`, `react-resizable-panels`, `input-otp`, `lucide-react`

### Internal
- `src/lib/utils.ts` (`cn`)

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
