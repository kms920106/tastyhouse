# App Router 페이지 작성 가이드 (App Directory Guide)

> 이 문서는 `src/app/` 하위에서 코드를 작성·수정할 때 Claude(AI)가 참고하는 표준 가이드입니다.
> **`src/app/places/[id]/(detail)/`** 가 현재 프로젝트의 Best Practice 레퍼런스이며, 본 가이드는 해당 디렉토리 패턴을 기반으로 작성되었습니다.

---

## 1. 개요

### 1.1. `src/app/`의 역할

- `src/app/`은 Next.js 15 App Router의 **라우팅 계층**입니다.
- `page.tsx` / `layout.tsx` / `loading.tsx` / `error.tsx` / `not-found.tsx` / `route.ts` 등 Next.js가 약속한 파일만 라우팅 역할을 담당합니다.
- **페이지의 실제 UI와 로직은 `_components/` 하위 컴포넌트에 위임**합니다. `page.tsx`는 params/searchParams를 해석하고 루트 컴포넌트를 렌더링하는 얇은 진입점입니다.
- 데이터 페칭은 Server Component에서 repository를 직접 호출하거나, Client Component에서 domain hook을 통해 TanStack Query로 수행합니다.

### 1.2. 왜 이 가이드가 필요한가

- `page.tsx`에 UI 로직과 데이터 페칭이 섞이면 테스트와 재사용이 어려워집니다.
- Server Component / Client Component 경계를 명확히 하지 않으면 `server-only` 코드가 클라이언트 번들로 유출됩니다.
- `params`와 `searchParams`가 Next.js 15부터 `Promise`로 바뀐 것을 인지하지 못하면 런타임 에러가 발생합니다.
- Suspense 경계와 Skeleton 배치 기준이 없으면 로딩 UX가 불일치합니다.

### 1.3. 적용 범위

- **신규 페이지**: 반드시 이 가이드를 따릅니다.
- **기존 페이지**: 강제 일괄 마이그레이션 대상이 아닙니다. 기존 파일을 손댈 때 점진적으로 정리합니다.

---

## 2. 파일 구조

### 2.1. 라우트 세그먼트 구조

```
src/app/
├── layout.tsx                        # Root Layout — QueryProvider, 폰트, AppToaster
├── error.tsx                         # 전역 에러 바운더리 ('use client' 필수)
├── (with-footer)/                    # Route Group: 레이아웃 구성용 (URL 무영향)
│   ├── layout.tsx
│   └── (with-sidebar)/
│       ├── layout.tsx
│       └── (home)/page.tsx
├── places/
│   ├── [id]/
│   │   ├── (detail)/                 # Route Group: URL 무영향 그룹화
│   │   │   ├── page.tsx
│   │   │   └── _components/          # 이 라우트 전용 컴포넌트
│   │   └── order/
│   │       ├── menus/(list)/
│   │       ├── cart/
│   │       ├── checkout/
│   │       └── method/
└── api/                              # Route Handlers (route.ts)
```

### 2.2. 파일 역할 한눈에 보기

| 파일 | 필수 여부 | 역할 |
| --- | --- | --- |
| `page.tsx` | 라우트당 필수 | params/searchParams 파싱 → 루트 컴포넌트 렌더링 |
| `layout.tsx` | 공유 레이아웃이 필요할 때 | children wrap, 레이아웃 UI |
| `loading.tsx` | 선택 | 라우트 전환 시 전체 페이지 Skeleton |
| `error.tsx` | 선택 | 라우트 레벨 에러 바운더리 |
| `not-found.tsx` | 선택 | `notFound()` 호출 시 렌더링 |
| `route.ts` | API 라우트 | HTTP 핸들러 (`GET`, `POST`, ...) |
| `_components/` | 거의 항상 | page가 위임하는 실제 컴포넌트들 |
| `_lib/` | 선택 | 해당 라우트 전용 순수 유틸·헬퍼 |

### 2.3. `page.tsx` 골격

```tsx
import type { Metadata } from 'next'
import XxxPage from './_components/XxxPage'

interface Props {
  params: Promise<{ id: string }>
  searchParams: Promise<{ tab?: string }>
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { id } = await params
  // 필요 시 repository 직접 호출
  return { title: '...', openGraph: { ... } }
}

export default async function Page({ params, searchParams }: Props) {
  const [{ id }, { tab }] = await Promise.all([params, searchParams])
  return <XxxPage id={Number(id)} tab={tab} />
}
```

- ✅ `params`와 `searchParams`는 반드시 `Promise<...>`로 선언 (Next.js 15 필수)
- ✅ 여러 값이 필요하면 `Promise.all([params, searchParams])`로 병렬 await
- ✅ 타입 변환(`Number(id)`, 파싱)은 `page.tsx`에서 처리 후 컴포넌트에 전달
- ❌ page.tsx에 JSX 로직 직접 작성 금지 → `_components/XxxPage.tsx`로 위임

### 2.4. `layout.tsx` 골격

```tsx
interface Props {
  children: React.ReactNode
}

export default function Layout({ children }: Props) {
  return <div className="min-h-screen">{children}</div>
}
```

- ✅ 레이아웃 고유 UI(래퍼 div, 헤더·푸터 고정 등)만 포함
- ✅ Route Group layout은 children pass-through만 해도 무방 (`<>{children}</>`)
- ✅ **동적 세그먼트 값 검증의 일원화는 허용**: `[param]/layout.tsx`에서 세그먼트 값을 파싱·검증하고 유효하지 않으면 `redirect()`하는 패턴은 권장. 하위 page들이 동일한 파싱/폴백을 중복하지 않게 한다. (예: `order/[orderMethod]/layout.tsx`가 `parseOrderMethodType` 검증 후 `ORDER_METHOD`로 redirect)
- ❌ 데이터 페칭, 비즈니스 로직 금지 (단 위의 순수 파라미터 검증은 예외 — `await fetch`/repository 호출은 여전히 금지)

### 2.5. `_components/` 내부 파일 분류

```
[Feature]Page.tsx          # 페이지 루트 — 섹션 조합 (Server Component)
[Feature]Header.tsx        # 섹션 단위 조합 컴포넌트
[Feature]Server.tsx        # 데이터 페칭 담당 Server Component
[Feature]Client.tsx        # 사용자 상호작용 담당 ('use client')
[Feature]Fetcher.tsx       # TanStack Query hook으로 데이터 페칭 (Client Component)
[Feature]Skeleton.tsx      # Suspense fallback / 로딩 UI
[Feature]Section.tsx       # Suspense + Server 조합을 감싸는 section 래퍼
```

---

## 3. 책임 매트릭스

| 책임 | `src/app/`에서 처리 | 처리 금지 |
| --- | --- | --- |
| params / searchParams 파싱 및 타입 변환 | ✅ `page.tsx` | |
| `generateMetadata` 작성 | ✅ `page.tsx` | |
| 라우트 레벨 레이아웃 | ✅ `layout.tsx` | |
| 인증 여부 확인 후 redirect | ✅ `page.tsx` 또는 `XxxServer.tsx` | |
| 데이터 페칭 (Server) | ✅ `[Feature]Server.tsx` (repository 직접 호출) | |
| 데이터 페칭 (Client) | ✅ `[Feature]Fetcher.tsx` (domain hook 사용) | |
| 사용자 상호작용, 이벤트 핸들러 | ✅ `[Feature]Client.tsx` | |
| Suspense + Skeleton 배치 | ✅ `[Feature]Section.tsx` | |
| 에러 분기 렌더링 | ✅ `[Feature]Server.tsx` / `Fetcher.tsx` | |
| HTTP 호출 (`api.get` 등) | | ❌ → repository로 |
| 도메인 가공 로직 | | ❌ → service로 |
| TanStack Query (`useQuery`, `useMutation`) 직접 사용 | | ❌ → domain hook으로 |
| queryKey 직접 선언 | | ❌ → `[domain].hook.ts`로 |
| DTO 타입을 컴포넌트 Props에 직접 사용 | | ❌ → model 타입으로 |

> 한 줄 요약: `page.tsx`는 **파싱 + 위임**만, `_components/`는 **렌더링 + 페칭**을, 비즈니스 로직은 **domains 계층**이 담당합니다.

---

## 4. 표준 패턴

### 4.1. 패턴 A — Server Component에서 데이터 페칭 (정석)

초기 데이터를 SSR로 제공해야 할 때 사용합니다.

```tsx
// PlaceDetailHeaderServer.tsx — Server Component
import { placeRepository } from '@/domains/place/place.repository'

interface Props {
  placeId: number
}

export default async function PlaceDetailHeaderServer({ placeId }: Props) {
  const { error, data } = await placeRepository.getPlaceDetail(placeId)

  if (error) return <HeaderTitle>-</HeaderTitle>
  if (!data) return <HeaderTitle>-</HeaderTitle>

  return <HeaderTitle>{data.name}</HeaderTitle>
}
```

- ✅ `repository`를 직접 호출 (server-only 코드이므로 Server Component에서만 가능)
- ✅ `{ data, error, status }` 구조분해 후 즉시 에러 분기
- ❌ `'use client'` 선언 금지

**언제 사용?**: SEO가 필요하거나 초기 렌더링에 데이터가 필요한 경우.

---

### 4.2. 패턴 B — Suspense + Server Component 조합 (스트리밍)

비동기 Server Component를 Suspense로 감싸 스트리밍 렌더링합니다.

```tsx
// PlaceDetailHeader.tsx — Suspense 조합 (Server Component)
import { Suspense } from 'react'
import PlaceDetailHeaderServer from './PlaceDetailHeaderServer'
import { PlaceDetailHeaderSkeleton } from './PlaceDetailHeaderSkeleton'

export default function PlaceDetailHeader({ placeId }: { placeId: number }) {
  return (
    <Header>
      <Suspense fallback={<PlaceDetailHeaderSkeleton />}>
        <PlaceDetailHeaderServer placeId={placeId} />
      </Suspense>
    </Header>
  )
}
```

- ✅ 모든 비동기 Server Component는 반드시 `<Suspense fallback={<...Skeleton />}>`으로 감싸기
- ✅ Suspense 경계를 분리하면 독립적인 스트리밍이 가능 (느린 데이터가 빠른 데이터를 막지 않음)

**언제 사용?**: 비동기 Server Component가 있는 모든 경우.

---

### 4.3. 패턴 C — 중첩 Suspense (ReactNode prop)

독립적인 로딩이 필요한 컴포넌트를 중첩 Suspense로 조율합니다.

```tsx
// PlaceSummarySection.tsx — 중첩 Suspense 조합
<Suspense fallback={<PlaceSummarySkeleton />}>
  <PlaceSummaryServer
    placeId={placeId}
    bookmarkButton={
      <Suspense fallback={<PlaceBookmarkButtonSkeleton />}>
        <PlaceBookmarkButtonServer placeId={placeId} />
      </Suspense>
    }
  />
</Suspense>
```

- ✅ 외부 Suspense가 내부보다 먼저 resolve → 내부는 별도 스트리밍
- ✅ `ReactNode` prop을 통해 Server Component 간 Suspense 경계를 주입
- **왜 이 구조?**: `PlaceSummaryServer`와 `PlaceBookmarkButtonServer`의 데이터 의존성이 달라 로딩 속도가 다릅니다. 하나의 Suspense로 묶으면 느린 쪽이 빠른 쪽을 block합니다.

**언제 사용?**: 한 컴포넌트 내부에 독립적인 로딩 단위가 2개 이상 존재할 때.

---

### 4.4. 패턴 D — Client Component에서 TanStack Query (Fetcher 패턴)

탭 전환, 무한스크롤 등 클라이언트 사이드 페칭이 필요할 때 사용합니다.

```tsx
// PlaceMenuListFetcher.tsx — Client Component + domain hook
'use client'

import { usePlaceMenus } from '@/domains/place/place.hook'
import { PlaceMenuListSkeleton } from './PlaceMenuListSkeleton'

export default function PlaceMenuListFetcher({ placeId }: { placeId: number }) {
  const { data, isLoading, error } = usePlaceMenus(placeId)

  if (isLoading) return <PlaceMenuListSkeleton />
  if (error) return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  if (!data?.data) return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('메뉴')} />

  return <PlaceMenuList menus={data.data} />
}
```

- ✅ 파일명 suffix `Fetcher` — Client Component가 로딩/에러 상태를 직접 관리함을 명시
- ✅ domain hook만 사용 (`@tanstack/react-query` 직접 import 금지)
- ✅ `isLoading → error → empty → 데이터` 순서로 분기
- ❌ `useQuery`, `useMutation`을 컴포넌트 안에서 직접 선언 금지

**언제 사용?**: 탭 전환, 무한스크롤, 사용자 인터랙션 후 데이터 갱신 등 클라이언트 사이드 페칭.

---

### 4.5. 패턴 E — Server Component에서 에러 분기

```tsx
// PlaceSummaryServer.tsx
const { error, status, data } = await placeRepository.getPlaceDetail(placeId)

if ((error && status === 404) || !data) {
  return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('기본 정보')} />
}
if (error) {
  return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
}
```

- ✅ 404 / 데이터 없음은 별도 분기 (먼저 체크)
- ✅ 일반 에러는 `COMMON_ERROR_MESSAGES.API_FETCH_ERROR` 사용
- ✅ `notFound()`는 완전한 404 페이지가 필요할 때만 (아니면 `FetchErrorState` 인라인 처리)
- ❌ `try/catch`로 에러 삼키기 금지 — repository가 이미 `{ error }` 형태로 반환

---

### 4.6. 패턴 F — 인증 체크 후 redirect (page.tsx 레벨)

페이지 전체를 인증이 필요로 할 때 `page.tsx`에서 처리합니다.

```tsx
// page.tsx — 인증 필요 페이지
export default async function Page({ params }: Props) {
  const [{ id }, isLoggedIn] = await Promise.all([params, getIsLoggedIn()])

  if (!isLoggedIn) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  return <OrderMethodPage placeId={Number(id)} />
}
```

- ✅ `getIsLoggedIn()`과 `params`를 `Promise.all`로 병렬 처리
- ✅ 인증 실패 시 `redirect()` — 이후 코드는 실행되지 않음
- ❌ `isLoggedIn` 조건 분기를 JSX에서 처리하지 않기 (가능하면 redirect)

**컴포넌트 레벨 인증 체크**: 특정 버튼·섹션만 인증이 필요할 때는 `XxxServer.tsx`에서 처리합니다.

```tsx
// PlaceBookmarkButtonServer.tsx — 컴포넌트 레벨 인증 체크
const isLoggedIn = await getIsLoggedIn()
if (!isLoggedIn) {
  return <Link href={PAGE_PATHS.AUTH_LOGIN}>...</Link>
}
const { data } = await placeRepository.getPlaceBookmark(placeId)
```

---

### 4.7. 패턴 G — generateMetadata

```tsx
export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { id } = await params
  const { data } = await placeRepository.getPlaceDetail(Number(id))

  if (!data) return {}

  return {
    title: data.name,
    description: `${data.name} | ${data.roadAddress ?? ''}`,
    openGraph: {
      title: data.name,
      ...(data.thumbnailUrl && { images: [data.thumbnailUrl] }),
    },
  }
}
```

- ✅ `params`를 `await` 후 사용 (Promise)
- ✅ 데이터 없으면 `return {}` (빈 메타데이터 반환)
- ✅ `metadataBase`는 Root layout에서 전역 설정 — 페이지별 재선언 불필요
- ✅ 여러 repository가 필요하면 `Promise.all`로 병렬 호출

---

### 4.8. 패턴 H — searchParams 파싱

```tsx
export default async function Page({ searchParams }: Props) {
  const { stationId: stationIdParam, foodTypes: foodTypesParam } = await searchParams

  const stationId = stationIdParam ? Number(stationIdParam) : undefined
  const foodTypes = foodTypesParam?.split(',').filter(Boolean) as PlaceFoodType[] | undefined

  return <PlacePage stationId={stationId} foodTypes={foodTypes} />
}
```

- ✅ `searchParams`는 반드시 `await`
- ✅ 타입 변환 / 파싱은 `page.tsx`에서 완료 후 컴포넌트에 전달 (컴포넌트 내부에서 `searchParams` 파싱 금지)
- ✅ 쉼표 구분 배열은 `.split(',').filter(Boolean)` 패턴

---

### 4.9. 패턴 I — Tab searchParam 파싱

탭 상태를 URL searchParam(`?tab=xxx`)으로 관리할 때의 표준 패턴입니다.

```tsx
// XxxTabs.tsx — Tab 타입은 반드시 Tabs 컴포넌트 파일에서 export
export type ReviewTab = 'all' | 'following'

// page.tsx — 파서 함수로 안전하게 파싱
import type { ReviewTab } from './_components/ReviewTabs'

const REVIEW_TAB_VALUES: ReviewTab[] = ['all', 'following']

function parseReviewTab(value: string | undefined): ReviewTab {
  return REVIEW_TAB_VALUES.includes(value as ReviewTab) ? (value as ReviewTab) : 'all'
}

export default async function Page({ searchParams }: Props) {
  const { tab } = await searchParams
  const initialTab = parseReviewTab(tab)
  return <ReviewPage tab={initialTab} />
}
```

- ✅ Tab 타입(`XxxTab`)은 해당 **Tabs 컴포넌트 파일**(`XxxTabs.tsx`)에서 `export type`으로 정의
- ✅ `page.tsx`와 Page 컴포넌트는 Tabs 파일에서 import (`@/domains/` 금지)
- ✅ **하위 컴포넌트도 동일 규칙 적용**: Tabs 컴포넌트 하위의 Server Component(`XxxServer.tsx`, `XxxList.tsx` 등)도 `@/domains/`가 아닌 Tabs 파일에서 `import type { XxxTab }`
- ✅ 유효값 배열(`FOO_TAB_VALUES`) + 파서 함수(`parseFooTab`) 패턴으로 `as` 단언 최소화
- ✅ 파서 함수 fallback은 해당 탭의 기본값(첫 번째 탭)으로 설정
- ✅ **prop명 규칙**: `page.tsx` → `XxxPage` → `XxxTabs` 모든 계층에서 `tab`으로 통일
- ✅ **도메인 타입 잔류**: `XxxTab`과 값이 동일한 도메인 타입(예: `RankPeriod`)이 있어도 삭제하지 않는다 — repository/service 레이어에서 여전히 참조할 수 있음
- ✅ **도메인 API 타입 매핑**: 하위 컴포넌트에서 `Record<XxxTab, DomainApiType>` 형태의 매핑이 필요하면 `XxxTab`을 키 타입으로 사용 (예: `Record<RankTab, RankType>`)
- ❌ `(tab || 'default') as XxxTab` 직접 단언 금지 — 유효하지 않은 값이 통과됨
- ❌ Tab 타입을 도메인(`src/domains/`)이나 Page 컴포넌트(`XxxPage.tsx`)에 정의 금지

---

### 4.10. 패턴 K — Tabs 컴포넌트 작성

shadcn/ui `<Tabs>`를 사용하는 컴포넌트의 표준 작성 패턴입니다.

**Best Practice 레퍼런스**: `src/app/search/result/_components/SearchResultTabs.tsx`

```tsx
// XxxTabs.tsx — Best Practice 구조
'use client'

export type XxxTab = 'all' | 'menu' | 'review'

const TABS: { label: string; value: XxxTab }[] = [
  { label: '전체', value: 'all' },
  { label: '메뉴', value: 'menu' },
  { label: '리뷰', value: 'review' },
]

export default function XxxTabs({ tab }: { tab: XxxTab }) {
  const { handleTabChange } = useTabNavigation()

  return (
    <Tabs value={tab} onValueChange={handleTabChange} className="gap-0">
      <TabsList className="sticky top-0 w-full h-[50px] p-0 rounded-none bg-white z-40 border-0 shadow-none">
        {TABS.map(({ label, value }) => (
          <TabsTrigger
            key={value}
            value={value}
            className="flex-1 h-full text-sm leading-[14px] text-foreground/40 border-0 border-b border-line rounded-none shadow-none cursor-pointer data-[state=active]:text-main data-[state=active]:font-bold data-[state=active]:shadow-none data-[state=active]:border-b-[1.5px] data-[state=active]:border-main"
          >
            {label}
          </TabsTrigger>
        ))}
      </TabsList>
      <TabsContent value="all" className="mt-0">...</TabsContent>
      <TabsContent value="menu" className="mt-0">...</TabsContent>
      <TabsContent value="review" className="mt-0">...</TabsContent>
    </Tabs>
  )
}
```

#### TabsTrigger 규칙

- ✅ **TABS 배열 + `map()`**: 동일한 className·라벨을 반복 선언하지 않는다
- ✅ **className 인라인 직접 작성**: `TAB_TRIGGER_CLASS` 같은 상수 변수 금지
- ✅ **`border-b-[1.5px]`**: active 탭 밑줄 두께는 `border-b-2` 아닌 `border-b-[1.5px]`
- ✅ **`key={value}`**: map() 시 반드시 지정
- ✅ **탭별 className이 다를 때**: `index === 0 ? '...' : '...'` 인라인 삼항으로 처리 (상수 변수 미사용)
- ❌ `TAB_TRIGGER_CLASS`, `TAB_CONTENT_CLASS` 등 className 상수 선언 금지

#### TabsList 규칙

- ✅ **shadcn 기본값 재정의**: `border-0 shadow-none` 반드시 포함
- ✅ **sticky 탭바**: `sticky top-0 z-40` (스크롤 시 탭바 고정이 필요한 경우)
- ✅ **전체 너비**: `w-full`

#### TabsContent 규칙

- ✅ **각 탭 콘텐츠가 다르므로 하드코딩 유지** (map() 일반화 안 함)
- ✅ **`<Tabs>` 직속 자식으로 배치**: `sticky` 래퍼 div 바깥에 위치해야 함
- ✅ **`className="mt-0"` 필수**: shadcn 기본값이 `mt-2`이므로 모든 `TabsContent`에 반드시 지정
- ❌ sticky 포지션을 가진 부모 div 안에 `TabsContent` 배치 금지 (콘텐츠가 sticky 영역에 고정되는 버그 발생)
- ❌ `className="mt-0"` 생략 금지 — 탭 콘텐츠 위에 의도치 않은 상단 간격이 생김

#### 탭-API 타입 매핑 상수 규칙

탭 값을 API enum으로 변환하는 `Record` 매핑 상수가 필요할 때의 네이밍·위치 규칙입니다.

```tsx
// ✅ 권장 — SCREAMING_SNAKE_CASE, [DOMAIN]_TAB_TYPE_MAP 패턴
const REVIEW_TAB_TYPE_MAP: Record<ReviewTab, ReviewType> = {
  all: 'ALL',
  following: 'FOLLOWING',
}

const RANK_TAB_TYPE_MAP: Record<RankTab, RankType> = {
  all: 'ALL',
  monthly: 'MONTHLY',
}

// ❌ 금지 — camelCase 상수
const reviewTypeMap: Record<ReviewTab, ReviewType> = { ... }

// ❌ 금지 — 입력 키와 타입명이 불일치 (키 타입이 RankTab인데 PERIOD 사용)
const RANK_PERIOD_TO_TYPE: Record<RankTab, RankType> = { ... }
```

- ✅ **네이밍 패턴**: `[DOMAIN]_TAB_TYPE_MAP` — SCREAMING_SNAKE_CASE + 키 타입명(`TAB`) + 역할(`TYPE_MAP`)
- ✅ **키 타입과 일치**: 키 타입이 `RankTab`이면 `RANK_TAB_TYPE_MAP` (도메인 타입명 `RankPeriod` 사용 금지)
- ✅ **선언 위치**: 해당 상수를 사용하는 각 파일의 모듈 스코프에 선언
- ❌ **`'use client'` 파일에서 export 후 Server Component에서 import 금지**: `'use client'` 파일의 상수(값)를 Server Component가 import하면 번들 경계 위반 — 같은 상수가 여러 Server Component에 필요하더라도 각 파일에 중복 선언하는 것이 올바름

#### Tabs 컴포넌트 prop명

URL-driven 탭 패턴에서 prop명은 모든 계층에서 `tab`으로 통일합니다:

| 전달 방향 | prop명 | 이유 |
|-----------|--------|------|
| `page.tsx` → `XxxPage` | `tab` | page가 파싱한 현재 값 |
| `XxxPage` → `XxxTabs` | `tab` | URL-driven controlled 컴포넌트이므로 모든 계층에서 통일 |

```tsx
// page.tsx
return <RankPage tab={initialTab} />

// RankPage.tsx → RankMemberSection → RankMemberTabs
<RankMemberTabs tab={tab} ... />
```

> `page.tsx` 내부 파싱 결과를 담는 지역변수명(`initialTab`)은 유지해도 무방합니다. 외부로 전달하는 prop명만 `tab`으로 통일합니다.

---

#### 아이콘 탭 (이미지 on/off 전환)

탭 라벨 대신 아이콘 이미지를 사용하는 탭은 `tab` prop이 아닌 `currentTab` local state로 아이콘 상태를 결정합니다.

```tsx
// ✅ 권장 — currentTab state로 아이콘 즉시 반응
const [currentTab, setCurrentTab] = useState<XxxTab>(tab)

const handleChange = (value: string) => {
  setCurrentTab(value as XxxTab)
  handleTabChange(value)
}

// 아이콘 src: currentTab 기준, <Tabs value={currentTab}>
src={`/images/${iconBase}-${currentTab === value ? 'on' : 'off'}.png`}
```

```tsx
// ❌ 금지 — tab 기준이면 URL 변경 → re-render 전까지 아이콘 미반영
src={`/images/${iconBase}-${tab === value ? 'on' : 'off'}.png`}
```

#### 탭 1개 컴포넌트

탭이 1개뿐이면 `onValueChange`는 발동될 일이 없으므로 생략합니다. 불필요한 조건식(ex. `tab === 'reviews' ? 'on' : 'off'`)도 제거하고 하드코딩합니다.

```tsx
// 탭 1개 — onValueChange 불필요, 아이콘 상태 하드코딩
<Tabs value={tab} className="gap-0">
  <TabsTrigger value="reviews" ...>
    <Image src="/images/mypage/icon-review-on.png" ... />
  </TabsTrigger>
</Tabs>
```

#### `BorderedSection` 중첩 주의

부모가 이미 `<BorderedSection>`으로 컴포넌트를 감싸고 있으면, 자식 컴포넌트 내부에 `<BorderedSection>`을 추가로 중첩하지 않습니다.

```tsx
// ❌ 이중 중첩 — border-y가 2겹 적용됨
<BorderedSection>           {/* 부모 */}
  <ChildComponent>
    <BorderedSection>       {/* 중복! */}
      <OptionTabs />
    </BorderedSection>
  </ChildComponent>
</BorderedSection>

// ✅ 부모의 BorderedSection 하나만 유지
<BorderedSection>
  <ChildComponent>
    <OptionTabs />
  </ChildComponent>
</BorderedSection>
```

---

### 4.11. 패턴 J — Route Handler (`api/` route.ts)

```tsx
// src/app/api/auth/callback/kakao/route.ts
export async function GET(request: NextRequest) {
  const { searchParams } = request.nextUrl
  const code = searchParams.get('code')

  if (!code) {
    return NextResponse.redirect(new URL(PAGE_PATHS.AUTH_LOGIN, request.url))
  }

  const { data, error } = await authRepository.kakaoLogin({ code })

  if (error || !data) {
    return NextResponse.redirect(loginFailedUrl)
  }

  // 쿠키 설정 후 redirect
  return NextResponse.redirect(new URL(PAGE_PATHS.HOME, request.url))
}
```

- ✅ 핸들러명은 HTTP 메서드 대문자 (`GET`, `POST`, `DELETE` 등)
- ✅ repository 직접 호출 가능 (server-only 환경)
- ✅ 에러 시 `NextResponse.redirect` 또는 `NextResponse.json({ error })` 반환
- ❌ UI 렌더링, JSX 금지

---

### 4.10. 패턴 J — loading.tsx (라우트 레벨 Skeleton)

```tsx
// loading.tsx — 라우트 전환 시 자동 렌더링
export default function Loading() {
  return (
    <section className="min-h-screen flex flex-col">
      <Header>
        <HeaderLeft><BackButton /></HeaderLeft>
        <HeaderCenter><Skeleton className="w-20 h-4" /></HeaderCenter>
      </Header>
      {/* 페이지 구조와 동일한 Skeleton 배치 */}
    </section>
  )
}
```

- ✅ 페이지 전체 구조(Header + 콘텐츠)를 Skeleton으로 모방
- ✅ `Skeleton` 컴포넌트(`@/components/ui/shadcn/skeleton`) 사용
- **언제 추가?**: 다단계 플로우(주문, 결제)처럼 페이지 전환이 눈에 띄는 경우에 추가. 단순 페이지는 Suspense Skeleton으로 충분합니다.

---

## 5. Naming Convention

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 라우트 파일 | Next.js 약속 파일명 고정 | `page.tsx`, `layout.tsx`, `loading.tsx` |
| Route Group | 소문자 괄호 | `(with-footer)`, `(detail)`, `(list)` |
| 동적 세그먼트 | `[param]` | `[id]`, `[menuId]` |
| 페이지 루트 컴포넌트 | `[Feature]Page` | `PlaceDetailPage`, `OrderMethodPage` |
| 데이터 페칭 Server | `[Feature]Server` | `PlaceDetailHeaderServer`, `PlaceSummaryServer` |
| Client 상호작용 | `[Feature]Client` | `PlaceBookmarkButtonClient`, `ShareButtonClient` |
| TanStack Query Fetcher | `[Feature]Fetcher` | `PlaceMenuListFetcher`, `PlaceInfoDetailFetcher` |
| Skeleton | `[Feature]Skeleton` | `PlaceDetailHeaderSkeleton`, `PlaceSummarySkeleton` |
| Section 조합 | `[Feature]Section` | `PlaceSummarySection`, `PlaceMenuListSection` |
| 라우트 전용 유틸 | `_lib/[name].ts` | `_lib/socialLoginHandlers.ts` |
| Props 인터페이스 | `interface Props { ... }` | 파일 로컬 선언, export 불필요 |
| Tab 타입 | `[Feature]Tab` — Tabs 컴포넌트 파일에서 export | `ReviewTab`, `MemberFollowTab`, `PlaceTab` |

**파일명 접미사 의사결정 기준**:

- 데이터를 `await`으로 페칭하는 Server Component → `Server`
- `'use client'` + domain hook으로 페칭 → `Fetcher`
- `'use client'` + 인터랙션만 → `Client`
- Suspense + Server(들) 조합 → `Section` 또는 접미사 없음
- `page.tsx`에서 받은 props를 배분하는 서버 루트 → `Page`

---

## 6. Route Groups 활용 기준

| 패턴 | 용도 | 예시 |
| --- | --- | --- |
| `(레이아웃명)` | 같은 레이아웃을 공유하는 페이지 그룹화 | `(with-footer)`, `(with-sidebar)` |
| `(detail)`, `(list)` | URL에 영향 없이 파일 구조를 논리적으로 정리 | `places/[id]/(detail)/page.tsx` |
| `(page)` | 하위에 `layout.tsx` + `page.tsx`를 분리할 때 | `auth/signup/(page)/` |

- ✅ `layout.tsx`를 라우트 그룹에 추가하면 해당 그룹 내 페이지에만 레이아웃 적용
- ✅ URL이 같은 두 페이지가 다른 레이아웃을 써야 할 때 Route Group으로 분리
- ❌ 디렉토리 정리용으로만 Route Group을 남용하지 않기 — 의미 있는 그룹화에만 사용

---

## 7. 안티패턴 (지양)

### 7.1. page.tsx에 JSX 로직 직접 작성 (지양)

```tsx
// ❌ 지양
export default async function Page({ params }: Props) {
  const { id } = await params
  const { data } = await placeRepository.getPlaceDetail(Number(id))
  return (
    <div>
      <h1>{data?.name}</h1>
      {/* JSX 로직이 page.tsx에 직접... */}
    </div>
  )
}

// ✅ 권장 — _components로 위임
export default async function Page({ params }: Props) {
  const { id } = await params
  return <PlaceDetailPage placeId={Number(id)} />
}
```

---

### 7.2. params / searchParams를 Promise 없이 사용 (금지)

```tsx
// ❌ 금지 — Next.js 15에서 런타임 에러 발생
export default async function Page({ params }: { params: { id: string } }) {
  const { id } = params   // ← await 없음
}

// ✅ 권장
export default async function Page({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params
}
```

---

### 7.3. _components에서 @tanstack/react-query 직접 import (금지)

```tsx
// ❌ 금지 — domain hook 정책 우회, repository는 server-only라 빌드 에러 발생
'use client'
import { useQuery } from '@tanstack/react-query'
import { placeRepository } from '@/domains/place/place.repository'

// ✅ 권장 — domain hook 사용
import { usePlaceMenus } from '@/domains/place/place.hook'
```

---

### 7.4. DTO 타입을 컴포넌트 Props에 직접 사용 (지양)

```tsx
// ❌ 지양 — API 타입이 컴포넌트에 노출됨
import type { PlaceInfoResponse } from '@/domains/place'

interface Props {
  placeInfo: PlaceInfoResponse   // ← Response suffix가 붙은 DTO 타입
}

// ✅ 권장 — model 타입 사용 (src/domains/CLAUDE.md 참조)
import type { PlaceInfo } from '@/domains/place'

interface Props {
  placeInfo: PlaceInfo
}
```

---

### 7.5. 비동기 Server Component를 Suspense 없이 사용 (지양)

```tsx
// ❌ 지양 — 부모가 렌더링 완료될 때까지 전체 페이지 block
export default function PlaceDetailPage({ placeId }: Props) {
  return (
    <div>
      <PlaceDetailHeaderServer placeId={placeId} />  {/* Suspense 없음 */}
    </div>
  )
}

// ✅ 권장
<Suspense fallback={<PlaceDetailHeaderSkeleton />}>
  <PlaceDetailHeaderServer placeId={placeId} />
</Suspense>
```

---

### 7.6. Server Component에서 이벤트 핸들러 사용 (금지)

```tsx
// ❌ 금지 — Server Component는 이벤트 핸들러 불가 (런타임 에러)
export default async function PlaceDetailPage({ placeId }: Props) {
  const handleClick = () => { ... }
  return <button onClick={handleClick}>...</button>
}

// ✅ 권장 — 인터랙션이 필요하면 Client Component로 분리
```

---

### 7.7. _components/에 공용 컴포넌트 배치 (지양)

```
// ❌ 지양 — 여러 라우트에서 공용으로 쓰는 컴포넌트를 특정 라우트 _components에 배치
src/app/places/[id]/(detail)/_components/FetchErrorState.tsx

// ✅ 권장 — 공용 컴포넌트는 src/components/에 위치
src/components/ui/FetchErrorState.tsx
```

---

### 7.8. searchParams를 `await Promise.resolve()`로 우회 (금지)

```tsx
// ❌ 금지 — Promise가 아닌 타입 + 억지 await로 우회
export default async function Page({ searchParams }: { searchParams: { tab?: string } }) {
  const resolved = await Promise.resolve(searchParams)
  const tab = resolved.tab

// ✅ 권장 — 타입을 올바르게 선언하고 직접 await
interface Props {
  searchParams: Promise<{ tab?: string }>
}
export default async function Page({ searchParams }: Props) {
  const { tab } = await searchParams
```

---

### 7.9. Tabs 관련 안티패턴

```tsx
// ❌ className 상수 선언 금지
const TAB_TRIGGER_CLASS = 'flex-1 h-full ...'
const TAB_CONTENT_CLASS = 'mt-0 flex flex-col ...'
<TabsTrigger className={TAB_TRIGGER_CLASS}>...</TabsTrigger>

// ✅ 인라인 직접 작성
<TabsTrigger className="flex-1 h-full ...">...</TabsTrigger>
```

```tsx
// ❌ TabsTrigger 하드코딩 금지 (className이 동일한 경우)
<TabsTrigger value="all" className="...">전체</TabsTrigger>
<TabsTrigger value="menu" className="...">메뉴</TabsTrigger>

// ✅ TABS 배열 + map()
const TABS = [{ label: '전체', value: 'all' }, { label: '메뉴', value: 'menu' }]
{TABS.map(({ label, value }) => (
  <TabsTrigger key={value} value={value} className="...">
    {label}
  </TabsTrigger>
))}
```

```tsx
// ❌ TabsContent를 sticky 래퍼 안에 배치 — 스크롤 시 콘텐츠 고정 버그
<div className="sticky top-[60px] ...">
  <TabsList>...</TabsList>
  <TabsContent value="info">...</TabsContent>  // ← 잘못된 위치
</div>

// ✅ sticky 래퍼에는 TabsList만, TabsContent는 Tabs 직속 자식
<Tabs ...>
  <div className="sticky top-[60px] ...">
    <TabsList>...</TabsList>
  </div>
  <TabsContent value="info">...</TabsContent>
</Tabs>
```

```tsx
// ❌ active 탭 밑줄 두께 border-b-2 사용 금지
className="... data-[state=active]:border-b-2 ..."

// ✅ border-b-[1.5px] 사용
className="... data-[state=active]:border-b-[1.5px] ..."
```

```tsx
// ❌ TabsContent에 className="mt-0" 생략 금지 — shadcn 기본값 mt-2가 적용되어 상단 간격 발생
<TabsContent value="info">...</TabsContent>

// ✅ 모든 TabsContent에 mt-0 명시
<TabsContent value="info" className="mt-0">...</TabsContent>
```

```tsx
// ❌ 탭-API 타입 매핑 상수 camelCase 금지, 키 타입명 불일치 금지
const reviewTypeMap: Record<ReviewTab, ReviewType> = { ... }        // camelCase
const RANK_PERIOD_TO_TYPE: Record<RankTab, RankType> = { ... }      // 키 타입 RankTab인데 PERIOD 사용

// ✅ [DOMAIN]_TAB_TYPE_MAP 패턴 — SCREAMING_SNAKE_CASE + 키 타입명과 일치
const REVIEW_TAB_TYPE_MAP: Record<ReviewTab, ReviewType> = { ... }
const RANK_TAB_TYPE_MAP: Record<RankTab, RankType> = { ... }
```

```tsx
// ❌ 'use client' 파일의 매핑 상수를 export해 Server Component에서 import 금지 (번들 경계 위반)
// RankMemberTabs.tsx ('use client')
export const RANK_TAB_TYPE_MAP = { ... }

// RankMyInfo.tsx (Server Component) — import 금지
import { RANK_TAB_TYPE_MAP } from './RankMemberTabs'

// ✅ 각 Server Component 파일에 중복 선언
// RankMyInfo.tsx
const RANK_TAB_TYPE_MAP: Record<RankTab, RankType> = { ... }
// RankMemberList.tsx
const RANK_TAB_TYPE_MAP: Record<RankTab, RankType> = { ... }
```

```tsx
// ❌ Tab 타입을 도메인에서 import하는 하위 컴포넌트 (금지)
// RankMemberList.tsx
import { RankPeriod } from '@/domains/rank'
export default async function RankMemberList({ rankPeriod }: { rankPeriod: RankPeriod }) { ... }

// ✅ Tabs 컴포넌트 파일에서 import
import type { RankTab } from './RankMemberTabs'
export default async function RankMemberList({ tab }: { tab: RankTab }) { ... }
```

---

### 7.10. loading.tsx 없이 다단계 플로우 구성 (지양)

주문 플로우(`method → menus → cart → checkout`)처럼 페이지 전환이 있는 경우 `loading.tsx`를 생략하면 전환 시 빈 화면이 노출됩니다.

---

## 8. 자가 검증 체크리스트

신규/수정 작업 종료 전에 확인하세요.

### page.tsx

- [ ] `params`와 `searchParams`가 `Promise<...>`로 선언되었는가?
- [ ] `params` / `searchParams`를 `await`로 처리했는가?
- [ ] 병렬로 처리할 수 있는 `await`는 `Promise.all`로 묶었는가?
- [ ] JSX 로직이 없고 `_components/XxxPage`로 위임하는가?
- [ ] 인증이 필요한 페이지는 `getIsLoggedIn()` + `redirect()`를 사용하는가?

### layout.tsx

- [ ] 레이아웃 UI와 children 전달만 담당하는가?
- [ ] 데이터 페칭이 없는가?

### _components

- [ ] 비동기 Server Component는 `<Suspense fallback={<...Skeleton />}>`으로 감쌌는가?
- [ ] Client Component 파일 최상단에 `'use client'`가 있는가?
- [ ] TanStack Query를 직접 import하지 않고 domain hook을 사용하는가?
- [ ] Fetcher 패턴에서 `isLoading → error → empty → 데이터` 순서로 분기했는가?
- [ ] Server Component 에러 분기에서 404와 일반 에러를 구분했는가?
- [ ] Props에 `*Response`, `*Request`, `*Query` suffix DTO 타입을 사용하지 않았는가?
- [ ] `_components/`에 공용 컴포넌트가 아닌 이 라우트 전용 컴포넌트만 있는가?

### 파일명 · 구조

- [ ] 데이터 페칭 Server Component는 `Server` suffix를 사용했는가?
- [ ] Client 상호작용 컴포넌트는 `Client` suffix를 사용했는가?
- [ ] TanStack Query 페칭 컴포넌트는 `Fetcher` suffix를 사용했는가?
- [ ] Skeleton 컴포넌트는 `Skeleton` suffix를 사용했는가?

### Tabs 컴포넌트

- [ ] TabsTrigger를 TABS 배열 + `map()`으로 선언했는가?
- [ ] `TAB_TRIGGER_CLASS`, `TAB_CONTENT_CLASS` 등 className 상수를 사용하지 않았는가?
- [ ] TabsList에 `border-0 shadow-none`이 포함되어 있는가?
- [ ] active 탭 밑줄이 `border-b-[1.5px]`인가? (`border-b-2` 사용 금지)
- [ ] 모든 `TabsContent`에 `className="mt-0"`이 있는가? (shadcn 기본값 `mt-2` 재정의 필수)
- [ ] `TabsContent`가 sticky 래퍼 div 바깥(`<Tabs>` 직속 자식)에 위치하는가?
- [ ] `XxxTabs` prop명이 `tab`인가? (`page.tsx` → `XxxPage` → `XxxTabs` 모든 계층 `tab`으로 통일)
- [ ] 아이콘 탭이라면 `<Tabs value={currentTab}>`이고 on/off 상태를 `currentTab` state 기반으로 결정하는가?
- [ ] 탭이 1개뿐이라면 `onValueChange`를 생략했는가?
- [ ] `BorderedSection`이 부모에서 이미 감싸고 있다면 자식에서 중첩하지 않았는가?
- [ ] 탭-API 타입 매핑 상수명이 `[DOMAIN]_TAB_TYPE_MAP` 패턴(SCREAMING_SNAKE_CASE)인가?
- [ ] 매핑 상수의 키 타입명이 실제 Tab 타입명과 일치하는가? (`RankTab` → `RANK_TAB_TYPE_MAP`)
- [ ] `'use client'` 파일의 매핑 상수를 Server Component에서 import하고 있지 않은가?

### Route Handler

- [ ] 핸들러명이 HTTP 메서드 대문자인가? (`GET`, `POST`, ...)
- [ ] JSX / React import가 없는가?
- [ ] repository를 통해 API를 호출했는가?

---

## 9. 빠른 참조 (Quick Reference)

> **Best Practice 레퍼런스**: [`src/app/places/[id]/(detail)/`](places/[id]/(detail)/)
>
> 새 페이지를 만들거나 기존 페이지를 수정할 때 항상 places detail 디렉토리를 먼저 보세요.

**컴포넌트 유형별 파일 매핑**:

| 유형 | 대표 파일 |
| --- | --- |
| page.tsx (params + generateMetadata) | [places/\[id\]/(detail)/page.tsx](places/[id]/(detail)/page.tsx) |
| XxxPage (섹션 조합) | [PlaceDetailPage.tsx](<places/[id]/(detail)/_components/PlaceDetailPage.tsx>) |
| Server (데이터 페칭) | [PlaceSummaryServer.tsx](<places/[id]/(detail)/_components/PlaceSummaryServer.tsx>) |
| Client (인터랙션) | [PlaceBookmarkButtonClient.tsx](<places/[id]/(detail)/_components/PlaceBookmarkButtonClient.tsx>) |
| Fetcher (TanStack Query) | [PlaceMenuListFetcher.tsx](<places/[id]/(detail)/_components/PlaceMenuListFetcher.tsx>) |
| Section (Suspense 조합) | [PlaceSummarySection.tsx](<places/[id]/(detail)/_components/PlaceSummarySection.tsx>) |
| 중첩 Suspense (ReactNode prop) | [PlaceSummarySection.tsx](<places/[id]/(detail)/_components/PlaceSummarySection.tsx>) |
| 인증 체크 (컴포넌트 레벨) | [PlaceBookmarkButtonServer.tsx](<places/[id]/(detail)/_components/PlaceBookmarkButtonServer.tsx>) |
| Tabs 컴포넌트 | [SearchResultTabs.tsx](search/result/_components/SearchResultTabs.tsx) |
| Route Handler | [api/payments/tosspayments/success/route.ts](api/payments/tosspayments/success/route.ts) |

**핵심 한 줄 요약**:

- `page.tsx` = `await params` + `await searchParams` + 컴포넌트 위임만
- Server Component = repository 직접 호출 + 에러 분기 + Suspense로 감싸기
- Client Component = domain hook + `isLoading → error → data` 분기
- 데이터 없음: `FetchErrorState` / 전체 없음: `notFound()` / 전역: `error.tsx`
- 의심되면 [`places/[id]/(detail)/`](places/[id]/(detail)/)를 보고 결정
