<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# scripts

## Purpose
Build/dev-time scripts run outside the Next.js app. Notably, generation of CSS theme presets.

## Key Files
| File | Description |
|------|-------------|
| `generate-theme-presets.ts` | Generates the CSS files under `src/styles/presets/`. Run via `npm run generate:presets` (uses `ts-node` with `tsconfig.scripts.json`) |
| `theme-boot.tsx` | Inline theme bootstrap snippet injected early in the document to apply the saved theme/mode before hydration (prevents flash of incorrect theme) |

## For AI Agents

### Working In This Directory
- `generate-theme-presets.ts` runs under `tsconfig.scripts.json` (Node/ts-node), not the app's `tsconfig.json` — keep it Node-compatible.
- After changing preset definitions, regenerate with `npm run generate:presets` and commit the updated `src/styles/presets/*.css`.

## Dependencies

### External
- `ts-node` (via the `generate:presets` script)

### Internal
- Outputs to `src/styles/presets/`.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
