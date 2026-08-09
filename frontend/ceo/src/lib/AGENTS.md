<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# lib

## Purpose
Shared utilities and the client-side persistence/preferences engine. Holds the `cn()` class-name helper, browser storage wrappers, the font registry, and the theme/layout preferences logic that powers the customizable dashboard chrome.

## Key Files
| File | Description |
|------|-------------|
| `utils.ts` | `cn()` (clsx + tailwind-merge), `getInitials()`, and `formatCurrency()` (Intl-based) |
| `cookie.client.ts` | Client-side cookie read/write helpers (used to persist preferences server-readably) |
| `local-storage.client.ts` | Client-side localStorage read/write helpers |
| `env.ts` | Zod-validated client env accessors. Currently only the Kakao Maps key (`getKakaoMapAppKey()`) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `fonts/` | Font registry — central declaration of the app's fonts (see `fonts/AGENTS.md`) |
| `kakao/` | Kakao Maps SDK — `types.ts` (the app's single global type declaration) + `loader.ts` (module-scope Promise singleton) |
| `preferences/` | Theme + layout preference types, defaults, storage, and utilities (see `preferences/AGENTS.md`) |

## 카카오맵 SDK 컨벤션

배달지역 편집기(`dashboard/shop/delivery-area`)가 유일한 사용처지만, 규칙은 앱 전체에 적용된다.

- **타입은 `kakao/types.ts` 한 벌만 선언한다.** `window.kakao` 를 컴포넌트마다 재선언하면 같은 SDK를 서로 다른 타입으로 보게 되어, 한쪽만 고친 시그니처가 조용히 어긋난다(web 앱이 `KakaoMap.tsx`/`ShopMap.tsx`에서 겪고 있는 문제 — 답습하지 않는다).
- **로드는 `loadKakaoMaps()` 하나로만 한다.** 모듈 스코프 Promise 싱글턴이라 동시에 여러 번 불러도 `<script>` 는 한 번만 꽂힌다. 실패한 Promise 는 캐시하지 않아 재시도할 수 있다. `next/script` 의 `<Script onReady>` 대신 명령형 로더를 쓰는 이유는 "지도 생성 → 데이터 fetch → 오버레이 attach" 의 순차 의존을 `await` 로 직렬화하기 위해서다.
- **`env.ts` 는 모듈 로드 시점에 throw 하지 않는다.** `getKakaoMapAppKey()` 는 키가 없으면 `null` 을 반환하고, 호출부가 지도 대신 폴백 UI 를 렌더한다. 키 하나가 대시보드 전체를 500 으로 떨어뜨리면 안 된다. `NEXT_PUBLIC_API_URL` 은 `client.ts` 의 `?? ""` 폴백 계약이 있으므로 이 모듈로 끌어오지 않는다.
- SDK URL 에 **`?libraries=services` 를 붙이지 않는다.** 가게 좌표는 서버가 `ShopBasicInfo.latitude/longitude` 로 내려주므로 지오코딩이 필요 없다.
- 지도에 얹는 캔버스는 `AbstractOverlay` 상속 대신 **지도 컨테이너의 형제로 절대배치**한다. 그리기 모드에서는 지도 드래그가 꺼져 있어 좌표 동기화가 어긋날 창이 없고, 오버레이 수명주기(`onAdd`/`draw`/`onRemove`)를 React 수명주기와 이중으로 관리하지 않아도 된다.

## For AI Agents

### Working In This Directory
- `*.client.ts` files contain browser-only APIs (cookies/localStorage) — import them only from client components or client-side code paths.
- Always compose Tailwind classes with `cn()` so conflicting utilities merge correctly.

### Common Patterns
- Pure, dependency-light helpers; no React in `utils.ts`.

## Dependencies

### External
- `clsx`, `tailwind-merge` (in `utils.ts`)

### Internal
- `preferences/` is consumed by `src/stores/preferences/` and the dashboard sidebar/layout controls.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
