<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# shadcn

## Purpose
shadcn/ui로 생성한 원본 UI 컴포넌트입니다. Radix UI 프리미티브 위에 Tailwind 스타일을 입힌 저수준 빌딩 블록으로, 대부분 상위 `ui/`의 `App*` 래퍼를 통해 간접 사용됩니다.

## Key Files
| File | Description |
|------|-------------|
| `button.tsx` / `input.tsx` / `textarea.tsx` | 기본 폼 요소 |
| `dialog.tsx` / `drawer.tsx` / `sheet.tsx` / `popover.tsx` / `tooltip.tsx` | 오버레이 |
| `tabs.tsx` | 탭 (`app/CLAUDE.md`의 Tabs 패턴 참조) |
| `accordion.tsx` / `command.tsx` / `native-select.tsx` | 컴포지트 입력 |
| `badge.tsx` / `progress.tsx` / `separator.tsx` / `skeleton.tsx` / `spinner.tsx` | 표시 요소 |
| `sidebar.tsx` | 사이드바 프리미티브 |

## For AI Agents

### Working In This Directory
- 이 디렉토리 파일은 **shadcn CLI가 생성**한 것입니다. 가능하면 직접 수정보다 상위 `ui/App*` 래퍼에서 커스터마이즈하세요.
- shadcn 기본값 재정의가 필요하면 사용처에서 className으로 덮어씁니다 (예: Tabs의 `mt-0`, `border-0 shadow-none`).
- 설정: 루트 `components.json`

## Dependencies

### Internal
- `@/lib/utils` (`cn`)

### External
- `@radix-ui/*`, `class-variance-authority`, `cmdk`, `vaul`, `lucide-react`

<!-- MANUAL: -->
