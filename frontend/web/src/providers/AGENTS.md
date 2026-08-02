<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# providers

## Purpose
앱 전역 React Context Provider를 둡니다. Root layout(`app/layout.tsx`)에서 children을 감쌉니다.

## Key Files
| File | Description |
|------|-------------|
| `QueryProvider.tsx` | TanStack Query `QueryClientProvider` (`'use client'`). 전역 QueryClient 설정 제공 |

## For AI Agents

### Working In This Directory
- Provider는 `'use client'` 컴포넌트
- 전역 QueryClient 기본 옵션(staleTime, retry 등) 변경은 `QueryProvider.tsx`에서
- 새 전역 Provider 추가 시 `app/layout.tsx`의 트리에 연결

## Dependencies

### External
- `@tanstack/react-query`

<!-- MANUAL: -->
