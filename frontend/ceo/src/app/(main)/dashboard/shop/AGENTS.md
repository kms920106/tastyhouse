<!-- Parent: ../AGENTS.md -->

# dashboard/shop

## Purpose
가게 관리 화면. `page.tsx`(Server Component)가 `searchParams` 의 `shopId`·`tab` 을 읽어 기본정보·운영정보·주문정보 3개 탭을 렌더하고, 각 설정 항목은 `_components/*-sheet.tsx` 가 담당한다. 배달지역만 시트가 아니라 전용 라우트(`delivery-area/`)로 분리돼 있다.

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `_components/` | 이 라우트에서만 쓰는 Client Component — 탭 3종, 설정 항목별 Sheet, `setting-row.tsx` |
| `delivery-area/` | 배달지역 설정 전용 라우트. 지도 편집기 + 빠른설정 패널 |

## 배달지역이 별도 라우트인 이유

다른 설정 항목은 전부 Sheet 인데 배달지역만 라우트다. 되돌리려 하기 전에 아래를 읽는다.

1. **저장 시맨틱이 다르다.** 다른 시트는 행 단위 즉시 저장(`handleAdd` → 액션 → `reload()`)이지만, 편집기는 조작을 draft 에 모아 두고 "저장"에서 한 번에 커밋한다. 한 컨테이너에 섞으면 "저장을 안 눌렀는데 이미 반영된 것"과 아닌 것을 사용자가 구분할 수 없다.
2. **폭이 부족하다.** Sheet 는 `sm:max-w-lg`(512px)라 지도 편집·확대 확인이 되지 않는다.
3. **중첩이 깊어진다.** 배달지역 조정 신청 시트가 이미 중첩돼 있어, 여기에 편집기를 얹으면 Radix Dialog 3중이 된다 — 포커스 트랩·ESC 전파·스크롤 락이 꼬인다.
4. **제스처가 충돌한다.** 지도 pan/zoom + 브러시 드래그가 Sheet(`vaul`)의 드래그 처리와 이벤트 소유권을 다툰다.

진입점은 그대로 **운영정보 탭의 "배달가능지역" 행**이며, `router.push("/dashboard/shop/delivery-area?shopId=…")` 로 이동한다. `shopId` 는 동적 세그먼트가 아니라 `searchParams` 다 — 이 라우트군의 확립된 컨벤션이다. 사이드바에는 노출하지 않는다(탭 경유 진입 전용).

## delivery-area 편집기 구조

| 파일 | 역할 |
|------|------|
| `delivery-area-editor.tsx` | `"use client"` 셸. draft 소유, 저장 오케스트레이션 |
| `delivery-area-map.tsx` | 카카오 지도 인스턴스 + 캔버스. `next/dynamic` `{ ssr: false }` |
| `delivery-area-canvas-overlay.ts` | 캔버스 렌더러. **비 React 순수 TS** |
| `use-delivery-area-draft.ts` | 델타 기반 undo/redo reducer |
| `use-delivery-area-draft-storage.ts` | localStorage 임시 저장·복원 |
| `use-brush-paint.ts` | Pointer Events 전담 |
| `use-admin-dong-boundaries.ts` | 뷰포트 기반 경계 로드·캐시 |
| `delivery-area-quick-panel.tsx` | 반경 / 행정동 선택 탭 + 선택 목록 + 조정 신청 진입 |

### draft 커밋 시맨틱

- **baseline** = SSR 로 읽은 배달가능지역 목록 + 저장된 도형. **draft** = 도형(`rings`) + 직접 고른 행정동 `Set`.
- draft 는 `MANUAL` 행만 다룬다. `POLYGON` 행은 도형이 바뀌면 서버가 다시 만들므로 개별 토글 대상이 아니다(목록에 "지도" 배지로 구분 표시).
- 히스토리는 스냅샷이 아니라 **델타**다. 정점 5000개 도형을 50단계 복사하면 메모리가 수십 MB 로 불어난다. 되돌리기는 base 부터 액션을 재생해 만든다.
- **되돌리기 1단위는 스트로크**(pointerdown~pointerup). 동 단위로 쪼개면 한 획을 여러 번 눌러 지워야 해서 쓸모가 없다.
- 반경 적용(#7 `POST .../delivery-areas/radius`)은 **쓰지 않는다.** 서버에 즉시 반영하면 되돌릴 수 없으므로, 반경 결과도 draft 에 넣고 저장 시 bulk 로 함께 커밋한다.
- 저장 순서는 **도형 → bulk 추가 → bulk 삭제**다. 추가를 삭제보다 먼저 해야 중간에 배달지역이 0건이 되는 창이 생기지 않는다(0건은 "전 지역 배달"로 해석된다).
- 저장 직전 **#12 preview** 로 `blockedAdminDongs`(배달팁이 걸려 닫을 수 없는 동)를 확인해 409 를 맞기 전에 안내한다.

### 접근성

**지도는 키보드로 칠할 수 없다.** 따라서 검색 + 트리가 완전한 대체 경로여야 하며 같은 화면에 항상 존재한다 — 지도는 "더 빠른 길"이지 "유일한 길"이 아니다. 지도 키가 없거나 SDK 로드가 실패해도 이 경로는 동작한다. 선택/미선택은 색만으로 구분하지 않고 채움 + 굵은 외곽선 + 목록 체크 3중으로 표현하며, 배달팁 잠금은 파선으로 구분한다.

## For AI Agents

- 새 설정 항목을 추가할 때의 기본값은 여전히 **Sheet** 다. 라우트 분리는 위 4가지 조건(다른 저장 시맨틱·넓은 폭·중첩 회피·제스처 충돌)에 해당할 때만 고려한다.
- 사용자 문구는 `@/feature/shop/message` 의 `SHOP_OPERATION_COPY` / `SHOP_MESSAGE` 에서 가져온다. 컴포넌트에 인라인하지 않는다.
- 지도·좌표 관련 규칙은 `src/lib/AGENTS.md` 의 "카카오맵 SDK 컨벤션", 기하 계산은 `src/feature/shop/AGENTS.md` 의 `geo.ts` 항목을 따른다.
