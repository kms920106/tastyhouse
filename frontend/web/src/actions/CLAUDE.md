# Server Actions 작성 가이드 (Server Actions Guide)

> 이 문서는 `src/actions/` 하위에서 코드를 작성·수정할 때 Claude(AI)가 참고하는 표준 가이드입니다.
> **`src/actions/place.ts`** 가 현재 프로젝트의 Best Practice 레퍼런스이며, 본 가이드는 place 패턴을 기반으로 작성되었습니다.

---

## 1. 개요

### 1.1. server action의 역할

- `src/actions/`의 파일은 **client component ↔ server-only 도메인** 사이를 잇는 얇은 경계 어댑터입니다.
- 모든 파일은 첫 줄이 `'use server'`이며, 내부에서 `@/domains/[domain]`의 repository/service를 호출해 결과를 반환합니다.
- 도메인 로직(가공·조합·HTTP 호출)은 server action에 들어오지 않습니다 — 모두 도메인 layer가 책임집니다.
- client component가 TanStack Query를 통해 데이터를 조회할 때는 `src/domains/[domain]/[domain].hook.ts`의 custom hook이 server action을 `queryFn`으로 호출합니다. **hook → server action → repository** 순서로 단방향 의존합니다.

### 1.2. 왜 이 가이드가 필요한가

- 단순 wrapper(`place.ts`, `banner.ts`)와 cookie/redirect를 포함한 인증 액션(`auth.ts`, `signup.ts`)이 같은 폴더에 섞여 있어 새 액션을 어떤 형태로 작성해야 할지 기준이 모호합니다.
- 동일 도메인에서도 어떤 곳은 barrel(`@/domains/[domain]`)로, 어떤 곳은 명시 경로(`@/domains/[domain]/[domain].repository`)로 repository를 가져와 import 규칙이 일관되지 않습니다.
- `revalidatePath` 호출 조건과 위치가 통일되어 있지 않습니다.

### 1.3. 적용 범위

- **신규 액션**: 반드시 이 가이드를 따릅니다.
- **기존 액션**: 강제 일괄 마이그레이션 대상이 아닙니다. 기존 파일을 손댈 때 점진적으로 정리합니다.

---

## 2. 파일 구조

### 2.1. 위치 및 파일명

- 모든 server action은 `src/actions/[domain].ts`에 위치합니다.
- **1 도메인 = 1 파일** 원칙. (한 파일이 너무 커지는 사례가 등장하면 그때 가이드를 개정합니다.)
- 파일명은 도메인 이름과 일치해야 합니다 (`place.ts`, `auth.ts`, `member.ts`).

### 2.2. 파일 골격

```typescript
'use server'

import { ... } from '@/domains/[domain]'                           // 타입/DTO
import { [domain]Repository } from '@/domains/[domain]/[domain].repository'
// (선택) import { [domain]Service } from '@/domains/[domain]/[domain].service'
// (선택) import { revalidatePath } from 'next/cache'
// (선택) import { redirect } from 'next/navigation'
// (선택) import { cookies } from 'next/headers'

export async function getXxx(...) { ... }
export async function createXxx(...) { ... }
```

- ✅ 첫 줄은 항상 `'use server'`
- ✅ 모든 export는 `async function`
- ❌ 동기 함수, 클래스, 상수 export 금지

---

## 3. 책임 매트릭스

| 책임 | server action에서 처리 | 처리 금지 |
| --- | --- | --- |
| HTTP 호출 (`api.get` 등) | | ❌ → repository로 |
| 데이터 가공 (코드↔한국어, 여러 API 조합) | | ❌ → service로 |
| `response.data ?? []` 같은 단순 unwrap | ✅ (호출자 편의용) | |
| `revalidatePath` (mutation 성공 후) | ✅ | |
| `redirect` (인증/완료 페이지 이동) | ✅ | |
| `cookies()` 조작 | ✅ (인증 토큰 한정) | |
| FormData 파싱 + 간단한 입력 검증 | ✅ (`useActionState` 진입점) | |
| React state, UI 렌더링 | | ❌ |
| 도메인 비즈니스 로직 | | ❌ → service로 |

> 한 줄 요약: server action은 **호출 → (선택) 캐시/쿠키/리다이렉트** 외에는 아무것도 하지 않습니다.

---

## 4. Import 규칙

```typescript
// ✅ 권장
import { reviewRepository, ReviewLatestQuery } from '@/domains/review'      // ❌ 비권장 — repository는 명시 경로
import type { PlaceMapMarkerResponse } from '@/domains/place'               // ✅ 타입은 barrel
import { placeRepository } from '@/domains/place/place.repository'          // ✅ repository는 명시 경로
import { placeService } from '@/domains/place/place.service'                // ✅ service는 명시 경로

import { revalidatePath } from 'next/cache'
import { redirect } from 'next/navigation'
import { cookies } from 'next/headers'
```

- **타입 / DTO / constants**: barrel(`@/domains/[domain]`)에서 import 가능
- **repository / service**: 항상 명시 경로(`@/domains/[domain]/[domain].repository`) 사용
  - **이유**: barrel은 client-safe layer만 export합니다 (`src/domains/CLAUDE.md` 참고). repository/service는 `import 'server-only'`이므로 명시 경로가 의도를 더 명확히 드러냅니다.
- **단, DTO/Request 타입을 server action 파라미터 타입으로 그대로 노출하는 것은 금지** — 8.8 참조. barrel import는 함수 내부 가공/타입 보강용으로만 사용합니다.

> **기존 코드 안내**: [review.ts:3-9](review.ts)는 barrel로 repository를 가져오지만 — 이는 점진 정리 대상입니다. 신규 작업에서는 명시 경로를 사용하세요.

---

## 5. 4가지 표준 패턴

### 5.1. 패턴 A — 순수 read wrapper (정석)

repository 응답을 그대로 반환하는 가장 단순한 형태입니다. 호출자가 `{ data, error }`를 직접 다룹니다.

```typescript
'use server'

import { placeRepository } from '@/domains/place/place.repository'

export async function getLatestPlaces({ page, size }: { page: number; size: number }) {
  return placeRepository.getLatestPlaces({ page, size })
}

export async function getPlaceInfo(placeId: number) {
  return placeRepository.getPlaceInfo(placeId)
}
```

- ✅ 한 줄짜리 wrapper
- ✅ 반환 타입은 repository에서 자동 추론 (명시 불필요)
- ❌ `try/catch`, 로깅, 변환 금지

**언제 사용?**: 단순 조회. server action의 70% 이상이 이 패턴입니다.

---

### 5.2. 패턴 B — unwrap된 read wrapper

호출자가 항상 배열·객체를 기대할 때, `response.data ?? []`로 unwrap해서 반환합니다.

```typescript
import { PlaceMapMarkerResponse } from '@/domains/place'
import { placeRepository } from '@/domains/place/place.repository'

export async function getMapMarkers(params: {
  latitude: number
  longitude: number
}): Promise<PlaceMapMarkerResponse[]> {
  const response = await placeRepository.getMapMarkers(params)
  return response.data ?? []
}
```

- ✅ 반환 타입을 `Promise<T[]>` 또는 `Promise<T | null>`로 명시
- ✅ unwrap은 `?? []` / `?? null` 정도의 단순 fallback만
- ❌ `.map(...)`, `.filter(...)` 같은 가공 금지 → service.ts로

**언제 사용?**: 호출자가 에러 분기를 신경 쓰지 않고 데이터만 받기를 원할 때 (지도 마커, 정책 본문 등).

---

### 5.3. 패턴 C — mutation + revalidate

mutation 성공 시에만 캐시를 무효화합니다.

```typescript
import { reviewRepository, CommentCreateRequest } from '@/domains/review'
import { revalidatePath } from 'next/cache'

export async function createComment(reviewId: number, request: CommentCreateRequest) {
  const result = await reviewRepository.createReviewComment(reviewId, request)

  if (!result.error && result.data) {
    revalidatePath(`/reviews/${reviewId}`)
  }

  return result
}
```

- ✅ **성공 분기**(`!result.error && result.data`)에서만 `revalidatePath` 호출
- ✅ 결과는 `{ data, error }` 그대로 반환 — 호출자가 토스트/모달로 처리
- ✅ 동적 path는 템플릿 리터럴 사용 (`/reviews/${reviewId}`)
- ❌ `try/catch`로 에러를 삼키지 않기 — repository가 이미 `{ error }` 형태로 반환합니다
- ❌ 무조건 `revalidatePath` 호출 금지

**언제 사용?**: 댓글/리뷰 작성, 좋아요 토글 등 사용자가 변경한 데이터가 다시 보여야 하는 경우.

---

### 5.4. 패턴 D — FormData + cookies + redirect (`useActionState` 진입점)

폼 제출에서 사용하는 가장 복잡한 형태. discriminated union 결과 타입이 필수입니다.

```typescript
import { authRepository } from '@/domains/auth/auth.repository'
import type { LoginResult } from '@/domains/auth/auth.types'
import { AUTH_COOKIE_KEYS } from '@/lib/auth-config'
import { revalidatePath } from 'next/cache'
import { cookies } from 'next/headers'
import { redirect } from 'next/navigation'

function validateLoginInput(username: string, password: string): string | null {
  if (!username?.trim()) return '아이디를 입력해 주세요.'
  if (!password?.trim()) return '비밀번호를 입력해 주세요.'
  return null
}

export async function loginFormAction(
  _prevState: LoginResult | null,
  formData: FormData,
): Promise<LoginResult> {
  const username = formData.get('username')?.toString() ?? ''
  const password = formData.get('password')?.toString() ?? ''

  const validationError = validateLoginInput(username, password)
  if (validationError) {
    return { success: false, error: validationError }
  }

  const { data, error } = await authRepository.login({ username, password })
  if (error || !data) {
    return { success: false, error: '아이디 또는 비밀번호가 올바르지 않습니다.' }
  }

  const cookieStore = await cookies()
  const baseOptions = {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax' as const,
    path: '/',
  }
  cookieStore.set(AUTH_COOKIE_KEYS.ACCESS_TOKEN, data.accessToken, baseOptions)
  cookieStore.set(AUTH_COOKIE_KEYS.REFRESH_TOKEN, data.refreshToken, baseOptions)

  revalidatePath('/')
  redirect('/')
}
```

- ✅ 함수명 suffix `FormAction` (`loginFormAction`, `signupFormAction`)
- ✅ 시그니처: `(_prevState, formData) => Promise<Result>`
- ✅ 결과 타입은 **discriminated union 강제**:

```typescript
export type LoginResult =
  | { success: true }
  | { success: false; error: string }
```

- ✅ 검증 로직이 길어지면 같은 파일에 private helper로 분리 (export 금지)
- ✅ 쿠키 옵션은 helper로 묶기 (`setAuthCookies` 패턴, [auth.ts:122-132](auth.ts))
- ❌ `prevState` 사용 안 해도 시그니처에는 반드시 포함 (`useActionState` 호환)
- ❌ 결과 타입을 `any`로 두기 금지

**언제 사용?**: `useActionState` 훅을 쓰는 폼(로그인, 회원가입, 비밀번호 재설정).

---

### 5.5. 패턴 E — hook의 queryFn으로 사용 (TanStack Query 연동)

client component에서 `useQuery`를 사용할 때 server action을 `queryFn`으로 연결하는 표준 패턴입니다.
hook 파일은 `src/domains/[domain]/[domain].hook.ts`에 위치합니다.

```typescript
// src/domains/place/place.hook.ts
'use client'

import { getPlaceMenus } from '@/actions/place'   // ← server action import
import { useQuery } from '@tanstack/react-query'

export const placeQueryKeys = {
  menus: (placeId: number) => ['place', placeId, 'place-detail-menus'] as const,
}

export function usePlaceMenus(placeId: number) {
  return useQuery({
    queryKey: placeQueryKeys.menus(placeId),
    queryFn: () => getPlaceMenus(placeId),   // ← server action을 queryFn으로 호출
  })
}
```

```typescript
// src/app/places/[id]/(detail)/_components/PlaceMenuListFetcher.tsx
'use client'

import { usePlaceMenus } from '@/domains/place/place.hook'

export default function PlaceMenuListFetcher({ placeId }: { placeId: number }) {
  const { data, isLoading, error } = usePlaceMenus(placeId)
  // ...
}
```

- ✅ hook의 `queryFn`에서 server action을 직접 호출
- ✅ server action은 기존 패턴 A/B 그대로 유지 — hook을 위해 변경할 필요 없음
- ❌ hook에서 repository/service 직접 import 금지 (`server-only` 코드)

**언제 사용?**: client component에서 데이터를 조회하고 로딩/에러 상태를 관리해야 할 때.
Server Component에서 `await`으로 처리할 수 있다면 server action을 직접 호출하는 것을 우선합니다.

---

## 6. Naming Convention

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 파일명 | `[domain].ts` | `place.ts`, `auth.ts`, `member.ts` |
| 조회 함수 | `get` + 리소스 | `getLatestPlaces`, `getMemberMe` |
| 생성/수정/삭제 함수 | `create` / `update` / `toggle` / `delete` + 리소스 | `createComment`, `toggleReviewLike`, `withdrawMember` |
| FormData 액션 | 동작 + `FormAction` suffix | `loginFormAction`, `signupFormAction` |
| 결과 타입 | `[Action]Result` (discriminated union) | `LoginResult`, `SignupResult`, `PhoneLoginResult` |
| private helper | 동사 + 명사, export 안 함 | `validateLoginInput`, `setAuthCookies` |

---

## 7. 시나리오

### 시나리오 A — 신규 도메인 `coupon`에 read action 추가

**전제**: `src/domains/coupon/`이 7-Layer 패턴(`src/domains/CLAUDE.md` 참고)으로 이미 존재.

**1단계 — `src/actions/coupon.ts` 생성**

```typescript
'use server'

import { couponRepository } from '@/domains/coupon/coupon.repository'

export async function getCoupons({ page, size }: { page: number; size: number }) {
  return couponRepository.getCoupons({ page, size })
}

export async function getCouponDetail(couponId: number) {
  return couponRepository.getCouponDetail(couponId)
}
```

→ 패턴 A 그대로. 추가 가공 없음.

---

### 시나리오 B — mutation에 캐시 무효화 추가

**요구사항**: 장소 북마크 토글 후 장소 상세 페이지를 갱신하고 싶다.

**Before** (패턴 A):

```typescript
export async function togglePlaceBookmark(placeId: number) {
  return placeRepository.togglePlaceBookmark(placeId)
}
```

**After** (패턴 C로 전환):

```typescript
import { revalidatePath } from 'next/cache'

export async function togglePlaceBookmark(placeId: number) {
  const result = await placeRepository.togglePlaceBookmark(placeId)

  if (!result.error && result.data) {
    revalidatePath(`/places/${placeId}`)
  }

  return result
}
```

- 동적 path는 템플릿 리터럴
- 성공 분기에서만 호출 (실패 시 캐시를 비우면 안 됨)

---

### 시나리오 C — 회원가입 같은 form 액션 추가

**요구사항**: 휴대폰 인증 완료 후 회원 정보를 수집하는 폼.

**1단계 — `src/domains/auth`에 DTO/타입 정의** (도메인 가이드 참고)

**2단계 — `src/actions/signup.ts`에 패턴 D 액션 추가**

```typescript
import { authRepository } from '@/domains/auth/auth.repository'
import { redirect } from 'next/navigation'

export type SignupResult = { success: false; error: string }

function validateSignupInput(email: string, password: string): string | null {
  if (!email?.includes('@')) return '이메일을 확인해 주세요.'
  if (password.length < 8) return '비밀번호는 8자 이상이어야 합니다.'
  return null
}

export async function signupFormAction(
  _prevState: SignupResult | null,
  formData: FormData,
): Promise<SignupResult> {
  const email = formData.get('email')?.toString() ?? ''
  const password = formData.get('password')?.toString() ?? ''

  const validationError = validateSignupInput(email, password)
  if (validationError) return { success: false, error: validationError }

  const { error } = await authRepository.signup({ username: email, password })
  if (error) return { success: false, error: '회원가입에 실패했습니다. 다시 시도해 주세요.' }

  redirect('/auth/signup/complete')
}
```

- 검증 helper는 같은 파일 안 private 함수
- 성공 시 `redirect`로 종료 (반환문 없음 — `redirect`는 throw)
- 실패 결과는 discriminated union으로 명시

---

### 시나리오 D — 두 도메인 조합이 필요할 때

**요구사항**: 회원가입 페이지에서 약관 + 개인정보처리방침 + 전자금융거래 약관 본문 3개를 한 번에 가져오기.

**❌ 잘못된 방식**: server action에서 `Promise.all`로 조합 + 변환

```typescript
// 도메인 로직이 server action에 새는 형태 — 지양
export async function getSignupPolicies() {
  const [terms, privacy, finance] = await Promise.all([
    policyRepository.getLatestTermsOfService(),
    policyRepository.getLatestPrivacyPolicy(),
    policyRepository.getLatestElectronicFinancialTransactions(),
  ])
  return { terms: terms.data?.content ?? '', privacy: privacy.data?.content ?? '', finance: ... }
}
```

**✅ 올바른 방식 1**: 단순 단일 호출은 그대로 server action에서 패턴 B로 노출

```typescript
// signup.ts
export async function fetchTermsOfServiceContent(): Promise<string> {
  const { data } = await policyRepository.getLatestTermsOfService()
  return data?.content ?? ''
}
```

→ 호출자(컴포넌트)가 필요한 만큼만 await ([signup.ts:15-33](signup.ts) 참고)

**✅ 올바른 방식 2**: 진짜 조합/가공 로직이라면 `policyService.ts`에 메서드를 만들고 server action은 그대로 호출

```typescript
// src/domains/policy/policy.service.ts
export const policyService = {
  async getSignupPolicies() {
    const [terms, privacy, finance] = await Promise.all([...])
    return { ... }
  },
}

// src/actions/signup.ts
import { policyService } from '@/domains/policy/policy.service'

export async function getSignupPolicies() {
  return policyService.getSignupPolicies()
}
```

> **판단 기준**: 단순 호출 묶음이면 server action에서 OK. `.data?.x ?? y` 변환이 한 번이라도 들어가면 service.ts로 옮기는 것을 검토하세요.

---

## 8. 안티패턴 (지양)

### 8.1. server action 안에서 `api.get(...)` 직접 호출 (금지)

```typescript
// ❌ 지양
import { api } from '@/lib/api'

export async function getXxx() {
  return api.get('/api/xxx')   // ← repository를 거쳐야 함
}
```

→ 반드시 repository 경유.

---

### 8.2. server action 안에서 데이터 가공 (지양)

```typescript
// ❌ 지양
export async function getActivePlaces() {
  const response = await placeRepository.getLatestPlaces({ page: 0, size: 10 })
  return response.data?.filter((p) => p.rating >= 4.0) ?? []   // ← 가공 NO
}
```

→ 가공은 service.ts로. server action에서는 `?? []` 정도의 단순 unwrap만 허용.

---

### 8.3. 무조건 `revalidatePath` 호출 (지양)

```typescript
// ❌ 지양
export async function createComment(reviewId: number, request: CommentCreateRequest) {
  const result = await reviewRepository.createReviewComment(reviewId, request)
  revalidatePath(`/reviews/${reviewId}`)   // ← 실패해도 캐시를 비움
  return result
}
```

→ 성공 분기(`!result.error && result.data`)에서만 호출.

---

### 8.4. barrel을 통해 repository import (지양)

```typescript
// ❌ 지양 — barrel은 repository를 export하지 않음 (빌드 에러 위험)
import { placeRepository } from '@/domains/place'

// ✅ 권장
import { placeRepository } from '@/domains/place/place.repository'
```

> 도메인 가이드(`src/domains/CLAUDE.md` 섹션 3.1)에 따라 `index.ts`는 client-safe layer만 export합니다.

---

### 8.5. FormData 액션의 결과 타입을 암묵적으로 두기 (지양)

```typescript
// ❌ 지양
export async function loginFormAction(_prevState: any, formData: FormData) {
  // ...
  return { success: false, error: '...' }
}
```

→ discriminated union으로 명시:

```typescript
export type LoginResult = { success: true } | { success: false; error: string }

export async function loginFormAction(
  _prevState: LoginResult | null,
  formData: FormData,
): Promise<LoginResult> { ... }
```

---

### 8.6. 한 파일에 여러 도메인 액션 섞기 (지양)

```typescript
// ❌ 지양 — src/actions/misc.ts에 여러 도메인 함수 모음
export async function getBanners() { ... }
export async function getNotices() { ... }
```

→ 도메인별로 파일 분리 (`banner.ts`, `notice.ts`).

---

### 8.7. `try/catch`로 에러 삼키기 (지양)

```typescript
// ❌ 지양 — repository가 이미 { error } 형태로 반환하는데 또 감쌈
export async function createComment(reviewId: number, request: CommentCreateRequest) {
  try {
    return await reviewRepository.createReviewComment(reviewId, request)
  } catch (e) {
    return null   // ← 호출자가 에러를 알 수 없음
  }
}
```

→ repository 응답을 그대로 반환. 예외적으로 `logout()`처럼 redirect 전 cleanup이 필요한 경우에만 try/catch 허용 ([auth.ts:76-90](auth.ts)).

---

### 8.8. 도메인 DTO / 공용 타입을 server action 파라미터 타입으로 사용 (지양)

server action의 파라미터 타입은 **인라인 객체 타입**으로 선언합니다. `@/domains/[domain]`의 Request/DTO 타입이나 `@/types/common`의 공용 타입(`PaginationParams` 등)을 server action의 파라미터 타입으로 그대로 노출하지 않습니다.

```typescript
// ❌ 지양 — 도메인 DTO를 파라미터 타입으로 노출
import { SocialSignUpRequest } from '@/domains/auth/auth.dto'
import { PaginationParams } from '@/types/common'

export async function socialSignUpAction(request: SocialSignUpRequest) { ... }
export async function getLatestPlaces(params: PaginationParams) { ... }
```

```typescript
// ✅ 권장 — 인라인 타입으로 직접 선언
export async function socialSignUpAction({
  username,
  password,
  phoneVerifyToken,
}: {
  username: string
  password: string
  phoneVerifyToken: string
}) {
  return authRepository.signUpSocial({ username, password, phoneVerifyToken })
}

export async function getLatestPlaces({ page, size }: { page: number; size: number }) {
  return placeRepository.getLatestPlaces({ page, size })
}
```

- DTO/공용 타입은 도메인 layer(repository/service)의 경계 타입입니다. server action은 client component와의 또 다른 경계이므로, 두 경계를 분리하기 위해 파라미터를 인라인으로 선언합니다.
- 필드 수와 무관하게 인라인을 유지합니다 (필드가 5개 이상이라도 동일). 호출자가 시그니처만 보고 파라미터 구조를 바로 파악할 수 있는 이점이 더 큽니다.
- DTO 타입을 import해서 함수 **내부**에서 payload를 조립·repository로 그대로 전달하는 용도는 허용됩니다. 금지되는 것은 **파라미터 타입으로의 노출**뿐입니다.
- **이유**: server action이 도메인 DTO에 직접 의존하면 (1) DTO 필드 추가/이름 변경이 client component까지 전파되고, (2) 호출자가 server action 시그니처만 보고는 어떤 필드를 넘겨야 할지 알 수 없어 항상 도메인 파일을 열어봐야 합니다. 또한 Best Practice 레퍼런스인 [place.ts:19](place.ts)도 동일 원칙으로 작성되어 있습니다.

> **기존 코드 안내**: 일부 기존 파일([auth.ts](auth.ts), [bug-report.ts](bug-report.ts), [member.ts](member.ts), [order.ts](order.ts), [partnership.ts](partnership.ts), [payment.ts](payment.ts), [phone-verification.ts](phone-verification.ts), [review.ts](review.ts), [banner.ts](banner.ts), [follow.ts](follow.ts) 등)은 도메인 DTO/`PaginationParams`를 파라미터로 받지만 점진 정리 대상입니다. 신규 작업에서는 인라인 타입을 사용하고, 기존 파일을 수정할 때 함께 정리하세요.

---

## 9. 자가 검증 체크리스트

신규/수정 작업 종료 전에 확인하세요.

### 파일 구성

- [ ] 첫 줄이 `'use server'`인가?
- [ ] 모든 export가 `async function`인가?
- [ ] 파일명이 `[domain].ts` 규칙을 따르는가?
- [ ] 한 파일에 단일 도메인의 액션만 들어 있는가?

### Import

- [ ] repository/service를 명시 경로(`@/domains/[domain]/[domain].repository`)로 import했는가?
- [ ] 타입/DTO/constants는 barrel(`@/domains/[domain]`)에서 가져왔는가?
- [ ] server action 파라미터 타입을 도메인 DTO/Request나 `PaginationParams` 대신 인라인 객체 타입으로 선언했는가? (8.8 참조)
- [ ] DTO를 import한 경우, 파라미터 타입 노출이 아니라 함수 내부 payload 조립 용도로만 사용하는가?

### 책임 분리

- [ ] `api.get/post/...` 직접 호출이 없는가?
- [ ] `.map(...)`, `.filter(...)` 같은 가공 로직이 없는가? (단순 `?? []` 제외)
- [ ] `revalidatePath`가 성공 분기에서만 호출되는가?
- [ ] cookies 조작은 인증 액션에서만 일어나는가?

### 명명 / 타입

- [ ] 함수명이 `get / create / update / toggle / delete` + 리소스 패턴인가?
- [ ] FormData 액션이라면 suffix가 `FormAction`이고 시그니처가 `(_prevState, formData)`인가?
- [ ] FormData 액션의 결과 타입이 discriminated union으로 명시되었는가?

### hook 연동 (해당하는 경우)

- [ ] client component에서 useQuery를 직접 작성하지 않고 `[domain].hook.ts`의 custom hook을 사용했는가?
- [ ] hook의 queryFn이 이 server action을 호출하는가? (hook ↔ action 연결 확인)

---

## 10. 빠른 참조 (Quick Reference)

> **Best Practice 레퍼런스**: [`src/actions/place.ts`](place.ts)
>
> 새 server action을 만들거나 기존 파일을 수정할 때 항상 place.ts를 먼저 보세요.

**파일별 패턴 매핑**:

| 패턴 | 대표 파일 |
| --- | --- |
| A — 순수 read wrapper | [place.ts](place.ts), [banner.ts](banner.ts), [notice.ts](notice.ts), [faq.ts](faq.ts) |
| B — unwrap된 read wrapper | [place.ts:7-13](place.ts) (`getMapMarkers`), [signup.ts:15-33](signup.ts) (`fetch*Content`) |
| C — mutation + revalidate | [review.ts](review.ts), [rank.ts](rank.ts) |
| D — FormData + cookies + redirect | [auth.ts](auth.ts), [signup.ts](signup.ts) |

**핵심 한 줄 요약**:

- server action = `'use server'` + repository/service 호출 + (선택) `revalidatePath` / `redirect` / `cookies`
- HTTP는 repository, 가공은 service, server action은 **얇게**
- 단순 read는 패턴 A, 배열 fallback은 패턴 B, mutation은 패턴 C, FormData는 패턴 D
- 의심되면 [place.ts](place.ts)를 보고 결정
