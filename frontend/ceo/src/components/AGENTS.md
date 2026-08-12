<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# components

## Purpose
App-wide shared components. Contains the generated **shadcn/ui** primitive library (`ui/`) plus a few composite shared components used across multiple routes.

## Key Files
| File | Description |
|------|-------------|
| `date-range-picker.tsx` | Shared date-range picker (built on the calendar/popover primitives) |
| `simple-icon.tsx` | Renders brand icons from the `simple-icons` package |
| `status-badge.tsx` | Renders a request/application status as a `Badge` with a code→variant mapping |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `ui/` | shadcn/ui primitive components (see `ui/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- Put a component here only when it is shared across multiple routes; otherwise colocate it in the route's `_components/` folder.
- Compose styles with `cn()` from `src/lib/utils.ts`.

### Common Patterns
- Composite shared components build on `ui/` primitives rather than re-implementing them.

### `status-badge.tsx` — 승격 근거와 라벨 규칙

원래 `dashboard/shop/_components/delivery-area-adjustment-sheet.tsx` 의 로컬 `statusBadgeVariant` 함수였고, 요청처리 현황 화면이 두 번째 사용처가 되면서 "2곳 이상이면 승격" 규칙에 따라 여기로 옮겼다.

**라벨은 매핑하지 않고 `label` prop 으로 받는다.** 상태 코드의 한글화는 서버 카탈로그(`statusDescription`)의 몫이고, 이 컴포넌트가 아는 것은 "어떤 상태를 어떤 색으로 보일지"뿐이다. 덕분에 백엔드에 상태가 추가돼도 기본값(`outline`)으로 안전하게 떨어진다.

variant 표에 `APPROVED` 와 `COMPLETED` 가 **둘 다** 있는 이유: 통합 상태(`docs/tasks/backend.md` 2-2)는 완료를 `APPROVED` 로 접어 넣지만, 배달지역 조정의 원본 enum 은 여전히 `COMPLETED` 를 쓴다. 원본 enum 을 그대로 쓰는 조정 신청 시트도 같은 표를 공유해야 하므로 두 코드를 같은 색으로 둔다.

## Dependencies

### External
- `simple-icons`, `lucide-react`, `react-day-picker` / `date-fns` (date picker)

### Internal
- `src/components/ui/`, `src/lib/utils.ts`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
