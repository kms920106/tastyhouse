<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# ui

## Purpose
범용 UI 프리미티브 계층입니다. shadcn 기본 컴포넌트(`shadcn/`)를 프로젝트 디자인에 맞춰 감싼 `App*` 래퍼, 폼 필드, 다이얼로그/모달, 이미지·아이콘 유틸리티 컴포넌트를 제공합니다. 도메인 지식이 없는 순수 프레젠테이션 컴포넌트만 둡니다.

## Key Files
| File | Description |
|------|-------------|
| `App*Button.tsx` | 버튼 변형 (Primary, Outline, Full, Submit) |
| `App*Input*.tsx` | 입력 필드 (Text, Email, Password, Phone, Number, Amount) |
| `AppFormField.tsx` / `FormCheckbox.tsx` | 폼 필드 래퍼 |
| `App*Dialog.tsx` / `Modal.tsx` / `ConfirmModal.tsx` | 다이얼로그·모달 |
| `AppToaster.tsx` | react-hot-toast 토스트 컨테이너 (Root layout에서 사용) |
| `FetchErrorState.tsx` / `ErrorStateSection.tsx` | 에러 표시 (페칭 실패 UI) |
| `Rating*.tsx` / `Avatar.tsx` / `HashTag*.tsx` | 평점·아바타·태그 표시 |
| `Image*.tsx` / `PhotoUploader.tsx` | 이미지 컨테이너·스와이퍼·라이트박스·업로더 |
| `Icon.tsx` / `icon-registry.ts` / `icon-helpers.ts` | 아이콘 레지스트리·헬퍼 |
| `HtmlContent.tsx` | sanitize된 HTML 렌더링 (약관·공지 본문) |
| `BorderedSection.tsx` / `SectionStack.tsx` / `Divider.tsx` | 레이아웃 섹션 |
| `StickyFooter.tsx` / `SideBar*.tsx` | 고정 푸터·사이드바 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `shadcn/` | shadcn/ui 원본 컴포넌트 (see `shadcn/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- 도메인 데이터·페칭·비즈니스 로직 금지 — 순수 프레젠테이션만
- shadcn 컴포넌트를 직접 쓰기보다 `App*` 래퍼가 있으면 그것을 사용
- `BorderedSection` 중첩 주의 (부모가 이미 감싸면 자식에서 중복 금지 — `app/CLAUDE.md` §4.10)
- className은 `tailwind-merge`/`clsx`(`@/lib/utils`의 `cn`)로 병합

## Dependencies

### Internal
- `@/components/ui/shadcn/*`, `@/lib/utils` (`cn`), `@/lib/sanitize` (HtmlContent)

### External
- `class-variance-authority`, `tailwind-merge`, `clsx`, `lucide-react`, `react-icons`, `react-hot-toast`, `swiper`, `yet-another-react-lightbox`

<!-- MANUAL: -->
