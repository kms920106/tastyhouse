<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# styles

## Purpose
전역 스타일 진입점입니다.

## Key Files
| File | Description |
|------|-------------|
| `globals.css` | Tailwind CSS 4 진입점 — 디자인 토큰(CSS 변수), 테마, 전역 base 스타일. `app/layout.tsx`에서 import |

## For AI Agents

### Working In This Directory
- 색상·간격 등 디자인 토큰은 여기 CSS 변수로 정의 (예: `--main`, `--line`, `--foreground`)
- 컴포넌트 스타일은 Tailwind 유틸리티 클래스로 작성 — 전역 CSS 남용 지양
- PostCSS 설정: 루트 `postcss.config.mjs` (`@tailwindcss/postcss`)

## Dependencies

### External
- `tailwindcss` 4, `tw-animate-css`

<!-- MANUAL: -->
