<!-- Parent: ../AGENTS.md -->

# dashboard/shop

## Purpose
가게 관리 화면. `page.tsx`(Server Component)가 `searchParams` 의 `shopId`·`tab` 을 읽어 기본정보·운영정보·주문정보 3개 탭을 렌더하고, 각 설정 항목은 `_components/*-sheet.tsx` 가 담당한다. 배달지역만 시트가 아니라 전용 라우트(`delivery-area/`)로 분리돼 있다.

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `_components/` | 이 라우트에서만 쓰는 Client Component — 탭 3종, 설정 항목별 Sheet, `setting-row.tsx` |
| `delivery-area/` | 배달지역 설정 전용 라우트. 지도 편집기 + 빠른설정 패널 |
| `change-history/` | 가게 변경이력 조회 전용 라우트 |
| `requests/` | 요청처리 현황 조회 전용 라우트. 목록 + `?requestId=` 상세 시트 |
| `menus/` | 메뉴·옵션 관리. 메뉴판·메뉴 상세·옵션그룹·옵션그룹 합치기·품절숨김 5개 화면 (아래 절) |
| `reviews/` | 리뷰 관리 전용 라우트 |

## 이 라우트군의 공통 컨벤션

`shopId` 는 **동적 세그먼트가 아니라 `?shopId=` searchParams** 다. `delivery-area/`·`change-history/`·`requests/`·`menus/` 가 모두 이 규칙을 따른다.

사이드바(`src/navigation/sidebar/sidebar-items.ts`) 등록은 **가게 관리(`/dashboard/shop`)와 메뉴·옵션 관리(`/dashboard/shop/menus`) 두 항목만** 한다. 나머지(`delivery-area/`·`change-history/`·`requests/`)는 가게 단위 화면이라 가게 관리 화면의 탭·행을 거쳐 진입하며, 상위 `dashboard/AGENTS.md` 의 "화면 추가 시 sidebar-items.ts 에 등록" 규칙보다 이 선례가 우선한다.

**`menus/` 는 그 예외의 예외다.** 사이드바에 없어 URL 이나 메뉴판 내부 링크로만 도달할 수 있었고, PDF 요구서가 전제하는 "메뉴·옵션 관리" 진입 경로가 화면에 없었다 — 가게 설정이 아니라 상시 운영 업무라 별 항목으로 등록했다.

## 상세를 `?requestId=` 로 서버 렌더하는 패턴 (`requests/`)

요청처리 현황의 상세 시트는 클라이언트에서 데이터를 가져오지 않는다. **`src/api/**` 의 repository·service 가 최상단에 `import "server-only";` 를 선언하므로 Client Component 에서 호출할 수 없기 때문**이다. 그래서 `page.tsx` 가 `?requestId=` 를 읽어 상세와 문의 스레드를 `Promise.all` 로 함께 조회한 뒤 뷰에 내려주고, 뷰는 그 값이 있을 때만 Sheet 를 연다.

시트를 열고 닫는 것도 상태가 아니라 URL 조작이다(`params.set/delete("requestId")`). 부수 효과로 **새 탭·뒤로가기·링크 공유가 그대로 동작**한다.

되돌리려 하기 전에: 상세를 클라이언트 fetch 로 바꾸려면 `api/` 레이어의 `server-only` 경계를 허무는 별도 판단이 필요하다. 시트 하나 때문에 그 경계를 열지 않는다.

## 실패 계층 분리 (`change-history/`·`requests/`)

조회 화면의 실패는 **전부 `error.tsx` 로 보내지 않는다.** 화면이 성립하는지에 따라 층을 나눈다.

| 실패 | 처리 |
|---|---|
| 가게 목록·카탈로그 조회 실패 | `throw` → `error.tsx` (필터를 만들 수 없어 화면 자체가 성립하지 않음) |
| 목록 조회 실패 | throw 하지 않고 `items=undefined` + `errorCode` 를 뷰에 넘겨 **필터바를 살린다** |
| 상세 조회 실패 | 시트를 열지 않고 인라인 안내만 띄운다 |

목록만 실패했는데 화면 전체를 에러로 덮으면 사용자가 필터를 고쳐 재시도할 방법이 사라진다.

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

## menus/ — 메뉴·옵션 관리

| 라우트 | 화면 |
|---|---|
| `menus/` | 메뉴판 관리(메뉴그룹 + 메뉴, 드래그 정렬) |
| `menus/[productId]/` | 메뉴 상세. 기본정보·이미지·채식·노출기간·옵션그룹 연결을 Sheet 로 나눠 담는다 |
| `menus/option-groups/` | 옵션그룹 관리(그룹·옵션 CRUD, 옵션 드래그 정렬) |
| `menus/option-groups/merge/` | 옵션그룹 합치기(추천 묶음 / 직접 선택 → 기준 선택 → diff → 실행) |
| `menus/availability/` | 품절·숨김 일괄 설정 |

### 메뉴 탭 / 옵션 탭은 라우트를 가른다

`_components/menu-tab-bar.tsx` 가 메뉴판과 옵션그룹 화면 상단에 같이 붙는다. **`availability-filter-bar.tsx` 의 `?tab=` searchParam 패턴과 달리 이 탭은 `router.push` 로 경로를 바꾼다** — 두 화면이 별 라우트이고 `option-groups/` 는 합치기 화면의 부모라 유지해야 하기 때문이다. 탭은 진입 경로를 하나로 모으는 용도이고 실체는 여전히 두 라우트다.

`menus/page.tsx` 는 `?tab=option` 을 받으면 `option-groups` 로 **리다이렉트**한다(직접 링크·북마크 호환). 옵션그룹 화면을 메뉴판 안에 복제하지 않는다 — 복제하면 같은 조작이 진입 경로에 따라 갈린다.

### 옵션그룹 합치기 (`merge/`)

- **비가역이다.** 서버에 분리(unmerge) 엔드포인트가 없어 되돌릴 방법이 아예 없으므로, 실행 전 `merge-confirm-dialog.tsx` 가 기준 그룹명·흡수 그룹 수·영향 메뉴 수와 함께 그 사실을 명시한다. 이 경고를 지우지 않는다.
- **흡수될 그룹에만 있는 옵션은 사라진다**(재부모화하지 않는다 — `docs/tasks/backend.md` §2-3). `merge-diff-sheet.tsx` 가 `diffType: "ONLY_IN_CANDIDATE"` 를 `destructive` 배지로 띄우는 이유다.
- 진입 모드는 `?mode=RECOMMENDED|MANUAL`. 추천 카드에서 넘어오면 묶음의 그룹 전체가 선택 상태로 옮겨지고 **기준 선택은 사용자가 한다** — 어떤 이름·연결을 남길지는 서버가 정할 수 없다.
- 합치기 가능 여부는 **서버 사전 검증(`mergeable`/`blockedReason`)이 판정**한다. 프론트가 다시 계산하지 않는다.
- **`blockedReason` 만 예외적으로 프론트에 `errorCode → 문구` 맵을 둔다**(`OPTION_GROUP_MERGE_COPY.BLOCKED_REASON_LABEL`). 에러 응답이 아니라 정상 응답(200)의 필드라 서버가 한국어 문구를 함께 내려주지 않는데, 버튼을 왜 못 누르는지는 화면이 설명해야 한다. 이 앱의 "서버 문구를 그대로 노출" 규칙은 여전히 에러 응답에 대해 유효하다.
- `signature` 는 **불투명 토큰**이다. 구조를 해석하거나 재계산하지 않고 제외 요청에 그대로 되돌려 보낸다(서버가 재계산해 낡은 추천을 거부한다).
- 추천 묶음이 0건이면 옵션그룹 관리 화면의 진입 배너를 **렌더링하지 않는다** — 눌러도 빈 화면인 진입점을 만들지 않는다.

### 일회용컵 보증금 옵션그룹

- **유형(`groupType`)은 등록에서만 정한다.** 서버 `PUT` 이 `groupType` 을 받지 않으므로(유형 전환 시 과거 주문 스냅샷의 해석이 바뀐다) 수정 폼에 선택 UI 를 노출하지 않는다.
- 유형 선택은 가게의 `cupDepositEnabled` 가 `true` 일 때만 보인다 — `false` 면 서버가 `SHOP_CUP_DEPOSIT_NOT_ENABLED` 로 거부하므로 고를 수 있게 두면 저장 실패만 남는다. 이 플래그는 **admin-api 만 토글**한다(환경부 지정 사실).
- 보증금 그룹은 `required=false`, `minSelect=0`, `maxSelect=1` 이 **서버 강제**다. 폼은 입력을 비활성화하는 데 그치지 않고 유형 전환 시 값을 그 값으로 **즉시 맞춘다** — 비활성화만 하면 이전 입력이 남아 `PRODUCT_OPTION_GROUP_DEPOSIT_SELECT_FIXED` 로 거절된다.
- 보증금 옵션의 `additionalPrice` 는 **0** 이다(추가금과 섞으면 비과세 분리가 무너진다). 개인컵 옵션은 `cupCount` 가 없고 `personalCupDiscountAmount` 만 있으며, **보증금이 아니라 상품 할인 축**이다.
- `CUP_DEPOSIT_PER_CUP`(300)은 `constants.ts` 에 있지만 **표시 계산 전용**이다. 금액의 진실원은 서버가 확정하는 `depositAmount` 다.

## For AI Agents

- 새 설정 항목을 추가할 때의 기본값은 여전히 **Sheet** 다. 라우트 분리는 위 4가지 조건(다른 저장 시맨틱·넓은 폭·중첩 회피·제스처 충돌)에 해당할 때만 고려한다.
- 사용자 문구는 `@/feature/shop/message` 의 `SHOP_OPERATION_COPY` / `SHOP_MESSAGE` 에서 가져온다. 컴포넌트에 인라인하지 않는다.
- 지도·좌표 관련 규칙은 `src/lib/AGENTS.md` 의 "카카오맵 SDK 컨벤션", 기하 계산은 `src/feature/shop/AGENTS.md` 의 `geo.ts` 항목을 따른다.
