# 코드 컨벤션

## 타입 네이밍 규칙

### Response 접미사 사용 기준

타입 이름에 `Response` 접미사를 붙일지 여부는 **타입의 사용 목적과 범위**에 따라 결정합니다.

#### ✅ Response를 붙이는 경우

**API 엔드포인트의 응답 데이터와 직접 매핑되는 타입**일 때 사용합니다.

**특징:**

- API 스펙과 1:1 대응
- `ApiResponse<T>`의 `data` 필드 타입으로 사용
- 특정 엔드포인트 전용 타입
- Request/Response 쌍이 명확한 경우

**예시:**

```typescript
// API: GET /api/places/{id}/summary
export type PlaceSummaryResponse = {
  id: number
  name: string
  roadAddress: string
  lotAddress: string
  rating: number
}

// API: POST /api/places/{id}/bookmark
export type PlaceBookmarkResponse = {
  bookmarked: boolean
}

// Request/Response 쌍
export type CommentCreateRequest = {
  content: string
}

export type CommentCreateResponse = {
  id: number
  reviewId: number
  // ...
}
```

#### ❌ Response를 붙이지 않는 경우

**도메인 모델이나 재사용 가능한 비즈니스 로직 타입**일 때 사용합니다.

**특징:**

- 여러 API에서 공통으로 사용
- 컴포넌트 props, 상태 관리 등에서도 활용
- 도메인 엔티티나 비즈니스 모델
- `ListItem`, `Detail` 등의 접미사로 용도 명시

**예시:**

```typescript
// 여러 목록 API에서 공통 사용
export type PlaceListItem = {
  id: number
  name: string
  stationName: string
  rating: number
  reviewCount: number
  // ...
}

// 도메인 모델
export type ReviewDetail = {
  id: number
  placeId: number
  content: string
  totalRating: number
  // ...
}

// 컴포넌트에서도 사용되는 타입
export type MemberProfile = {
  id: number
  userName: string
  userProfileImage: string | null
  // ...
}
```

### 판단 기준 체크리스트

다음 질문에 답하여 타입 네이밍을 결정하세요:

1. **이 타입이 특정 API 엔드포인트의 응답 구조와 직접 대응하나요?**
   - ✅ 예 → `Response` 접미사 사용
   - ❌ 아니오 → 다음 질문으로

2. **이 타입이 여러 곳(다른 API, 컴포넌트, 상태 관리)에서 재사용되나요?**
   - ✅ 예 → `Response` 접미사 없이 사용
   - ❌ 아니오 → 다음 질문으로

3. **이 타입이 도메인 엔티티나 비즈니스 모델을 표현하나요?**
   - ✅ 예 → `Response` 접미사 없이 사용
   - ❌ 아니오 → `Response` 접미사 사용 고려

### 네이밍 패턴 요약

| 타입 용도           | 네이밍 패턴                        | 예시                                            |
| ------------------- | ---------------------------------- | ----------------------------------------------- |
| API 응답 전용       | `{Entity}{Action}Response`         | `PlaceSummaryResponse`, `ReviewLikeResponse`    |
| Request/Response 쌍 | `{Entity}{Action}Request/Response` | `CommentCreateRequest`, `CommentCreateResponse` |
| 도메인 모델         | `{Entity}` 또는 `{Entity}{Detail}` | `Place`, `ReviewDetail`                         |
| 목록 아이템         | `{Entity}ListItem`                 | `PlaceListItem`, `PlaceBestListItem`            |
| 통계/집계 데이터    | `{Entity}{Type}`                   | `PlaceReviewStatistics`                         |

### 실제 코드 예시

**올바른 사용 예시:**

```typescript
// ✅ API 응답 전용 - Response 접미사 사용
export type PlaceSummaryResponse = { ... }
export type PlaceBookmarkResponse = { ... }

// ✅ 도메인 모델 - Response 접미사 없음
export type PlaceListItem = { ... }
export type ReviewDetail = { ... }

// ✅ Request/Response 쌍 - 둘 다 명시
export type CommentCreateRequest = { ... }
export type CommentCreateResponse = { ... }
```

**주의해야 할 경우:**

```typescript
// ⚠️ 여러 API에서 사용되는 경우 - Response 제거 고려
// 만약 PlaceReviewListItem이 여러 엔드포인트에서 사용된다면
export type PlaceReviewListItem = { ... }  // Response 없이

// ⚠️ 단일 엔드포인트 전용이라면
export type PlaceReviewListItemResponse = { ... }  // Response 포함
```

### 일관성 유지 원칙

1. **같은 패턴의 타입은 일관되게 네이밍**
   - 모든 API 응답 타입에 `Response` 접미사 사용
   - 모든 도메인 모델은 접미사 없이 사용

2. **팀 내 합의된 규칙 준수**
   - 새로운 타입 추가 시 기존 패턴 확인
   - 예외가 필요한 경우 팀과 논의 후 결정

3. **명확성 우선**
   - 타입 이름만 봐도 용도를 알 수 있어야 함
   - 모호한 경우 주석으로 용도 명시

---

## 데이터 페칭 규칙

### Next.js `fetch` vs TanStack Query `useQuery` 사용 기준

데이터를 가져오는 방법은 **렌더링 주체(서버 vs 클라이언트)** 와 **데이터의 성격** 에 따라 결정합니다.

---

### Next.js `fetch` — 서버에서 데이터를 가져올 때

**Server Component** 또는 **Route Handler** 에서 사용합니다.

#### ✅ 사용하는 경우

- 페이지 최초 진입 시 필요한 **초기 데이터** (SEO 대상 콘텐츠 포함)
- 로그인 여부와 무관하게 **공개된 정적/준정적 데이터** (공지사항, 랭킹, 장소 상세 등)
- 사용자 인터랙션 없이 **한 번만 불러오면 되는 데이터**
- 빌드 타임 또는 ISR(Incremental Static Regeneration) 캐싱이 필요한 경우

#### 특징

- 서버에서 실행되므로 번들 크기에 영향 없음
- Next.js의 fetch 캐시(`cache`, `next.revalidate`) 활용 가능
- 클라이언트에 로딩 스피너 없이 완성된 HTML 전달 가능
- 실시간성이 낮고 변경 빈도가 적은 데이터에 적합

#### 예시

```typescript
// app/places/[id]/page.tsx — Server Component
async function PlaceDetailPage({ params }: { params: { id: string } }) {
  const place = await fetchPlaceDetail(params.id)  // 서버에서 직접 호출

  return <PlaceDetail place={place} />
}

// 캐시 제어
const res = await fetch(`${API_URL}/places/${id}`, {
  next: { revalidate: 60 },  // 60초마다 재검증
})

// 캐시 없이 항상 최신 데이터
const res = await fetch(`${API_URL}/notices`, {
  cache: 'no-store',
})
```

---

### TanStack Query `useQuery` — 클라이언트에서 데이터를 가져올 때

**Client Component** 에서 사용합니다.

#### ✅ 사용하는 경우

- **사용자 인터랙션에 따라 데이터가 바뀌는 경우** (탭 전환, 필터, 무한 스크롤)
- **인증된 사용자 전용 데이터** (내 프로필, 내 주문 내역, 북마크 여부 등)
- **실시간성이 중요한 데이터** (좋아요 수, 재고 상태 등)
- **낙관적 업데이트(Optimistic Update)** 가 필요한 경우
- 동일 데이터를 **여러 컴포넌트에서 공유**하고 중복 요청을 막아야 하는 경우

#### 특징

- 클라이언트 캐시(`staleTime`, `gcTime`)로 중복 요청 방지
- `isFetching`, `isLoading` 등 세밀한 로딩 상태 관리
- `useMutation` + `invalidateQueries` 로 서버 상태와 즉시 동기화
- 자동 재시도, 포커스 시 재검증 등 UX 친화적 기능 내장

#### 예시

```typescript
// 인증된 사용자 데이터
function MyOrderList() {
  const { data, isLoading } = useQuery({
    queryKey: ['orders', 'my'],
    queryFn: fetchMyOrders,
  })
  // ...
}

// 필터/검색 — 파라미터가 바뀔 때마다 재요청
function PlaceSearchResult({ keyword }: { keyword: string }) {
  const { data } = useQuery({
    queryKey: ['places', 'search', keyword],
    queryFn: () => searchPlaces(keyword),
    enabled: keyword.length > 0,
  })
  // ...
}

// 무한 스크롤
function ReviewInfiniteList({ placeId }: { placeId: number }) {
  const { data, fetchNextPage } = useInfiniteQuery({
    queryKey: ['reviews', placeId],
    queryFn: ({ pageParam }) => fetchReviews(placeId, pageParam),
    getNextPageParam: (lastPage) => lastPage.nextCursor,
    initialPageParam: null,
  })
  // ...
}
```

---

### 판단 기준 한눈에 보기

| 질문                                      | fetch (서버) | useQuery (클라이언트) |
| ----------------------------------------- | :----------: | :-------------------: |
| 페이지 첫 로드 시 필요한 초기 데이터인가? |      ✅      |                       |
| SEO가 필요한 콘텐츠인가?                  |      ✅      |                       |
| 로그인 없이 접근 가능한 공개 데이터인가?  |      ✅      |                       |
| 사용자 인터랙션으로 데이터가 바뀌는가?    |              |          ✅           |
| 로그인 후에만 볼 수 있는 데이터인가?      |              |          ✅           |
| 여러 컴포넌트가 같은 데이터를 쓰는가?     |              |          ✅           |
| 낙관적 업데이트가 필요한가?               |              |          ✅           |

---

### 서버 + 클라이언트 조합 패턴

초기 데이터는 서버에서, 이후 갱신은 클라이언트에서 담당하는 패턴입니다.  
**초기 로딩 없이 바로 데이터를 보여주면서, 이후 인터랙션도 처리해야 할 때** 사용합니다.

```typescript
// Server Component — 초기 데이터 준비
async function PlaceDetailPage({ params }: { params: { id: string } }) {
  const initialData = await fetchPlaceDetail(params.id)

  return <PlaceDetailClient initialData={initialData} placeId={params.id} />
}

// Client Component — 이후 상호작용 처리 (북마크, 좋아요 등)
'use client'
function PlaceDetailClient({ initialData, placeId }: Props) {
  const { data } = useQuery({
    queryKey: ['place', placeId],
    queryFn: () => fetchPlaceDetail(placeId),
    initialData,  // 서버 데이터를 초기값으로 주입
  })
  // ...
}
```

---

### 절대 하지 말아야 할 것

```typescript
// ❌ Server Component에서 useQuery 사용 — 런타임 에러
async function PlacePage() {
  const { data } = useQuery({ ... })  // 서버에서는 실행 불가
}

// ❌ Client Component에서 async/await fetch를 직접 useEffect로 호출
'use client'
function MyComponent() {
  useEffect(() => {
    fetch('/api/data').then(...)  // 로딩/에러/캐시 모두 직접 관리해야 함 — useQuery 사용
  }, [])
}

// ❌ 인증 토큰이 필요한 요청을 Server Component의 fetch로 처리하면서 캐시 설정
const res = await fetch(`${API_URL}/my/orders`, {
  next: { revalidate: 60 },  // 다른 사용자의 데이터가 캐시될 위험 있음
})
```
