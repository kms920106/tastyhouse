# ADR-0003: 3개 api 모듈에 똑같이 복제된 플럼빙은 api-common-module이 단독 소유한다

- 상태: 채택
- 결정 커밋: `f15e0b786` (2026-08-02)
- 대체한 결정: —

## 맥락

`web-api`·`admin-api`·`ceo-api`는 각자 HTTP 응답 래퍼(`ApiResponse<T>`), 페이징 요청·응답(`PageRequest`·`PaginationResponse<T>`), 파일 업로드 어댑터(`FileService`) 같은 **플럼빙**을 갖고 있었다.

- **플럼빙(plumbing)**: 비즈니스 규칙과 무관하게 요청·응답을 실어 나르는 배관 코드.

전수 diff를 해 보니 이 파일들은 **package 선언 1줄만 다른 완전 복제**였다. 한편 복제를 방치한 결과 세 모듈의 `FileService`는 이미 서로 다르게 갈라져 있었다.

## 이전 규칙

### `backend/CLAUDE.md` — 페이징 응답 공용 제네릭 래퍼 규칙 (`PaginationResponse<T>`)

"모듈별로 각각 둠" — `ApiResponse<T>`/`PageRequest`가 모듈별로 중복 배치된 선례를 따라 `PaginationResponse<T>`도 각 모듈에 둔다(`a297ab48b`, 2026-07-17 `PaginationResponse` 통합 당시 관례). reference 구현도 `admin-api`/`web-api` 공통 `common/PaginationResponse.java`였다.

- reference 구현이 가리키던 페이징 조회 메서드는 당시 이름 `NoticeQueryService#getNotices`·`EventService#getEvents`·`OrderService#getOrderList` 등이었다(지금은 admin `NoticeManagementListQueryService#getNotices`, web `NoticeListQueryService#getNoticeList` 등).
- 통합 직후의 "적용 확인" 문구는 `SecurityConfig`(인증 주체·필터체인·인가 정책이 모듈마다 다름)와 `PublicPaths.PATTERNS`(web은 18줄, admin/ceo는 3줄로 공개 경로 목록이 전혀 다름)를 내용이 실제로 다른 정책 파일로 보고 모듈별로 두되, 위치·패키지 이름만 `config/security/`로 통일한다고 적었다.

### `backend/CLAUDE.md` — api 모듈 공용 플럼빙 소유 규칙 (머리말)

과거 관례는 "모듈별로 각각 둠"이었고 근거는 **모듈 독립성**(소비자별 계약 차이를 보호한다)이었다.

### `backend/api-common-module/AGENTS.md` — Purpose

과거 루트 CLAUDE.md의 관례는 "모듈별로 각각 둠"이었다.

### `backend/api-common-module/AGENTS.md` — Subdirectories

- 한때 `src/main/java/com/tastyhouse/apicommon/file/` 행이 있었다 — `FileService`를 "`MultipartFile`을 도메인 `FileUploadCommand`로 바꾸는 얇은 업로드 어댑터(조회·URL 변환 책임 없음)"로 설명했다.
- `src/main/java/com/tastyhouse/apicommon/shop/response/` 행도 있었다 — admin↔ceo 바이트 동일이던 shop 응답 record 3종(`ShopBreakTimeResponse`·`ShopBusinessHourResponse`·`ShopHygieneBadgeResponse`)을 담는다고 적었다. 두 패키지 모두 소스 트리에 없어 행이 삭제됐다.

### `backend/api-common-module/AGENTS.md` — 스캔 주의

`FileService`는 조건부 등록 전환 대상이 아니라 `@Service` 컴포넌트 스캔으로 등록된다고 적었다 — admin/ceo/web 전부 파일 업로드가 필요해 앱별 차이가 없다는 이유였다.

## 결정

내용이 **완전히 동일한(= package 선언 줄만 다른)** 비도메인 플럼빙은 복제하지 않고 신설 `api-common-module`(`com.tastyhouse.apicommon`)이 단독 소유한다. 동일함이 확인된 것만 통합한다.

| 항목 | before | after |
|---|---|---|
| `ApiResponse`·`PaginationResponse`·`PageRequest` | 3모듈 각각 | `api-common-module` 단독 |
| `FileService` | 3모듈 각각(드리프트) | `api-common-module`(`apicommon.file`) — 이후 소멸(아래) |
| `GlobalExceptionHandler` | admin·ceo 각각 | `api-common-module`. web-api는 자체 핸들러 유지 || 내용이 다른 정책 파일(`SecurityConfig`·`PublicPaths`·`TokenService`·`AuthService`) | 모듈별 | 모듈별 유지(위치·패키지 이름만 `config/security/`로 통일) |
| 계약이 다른 응답 record(`ShopDetailResponse` 등) | 모듈별 | 모듈별 유지 |

## 근거

- 관례가 보호하려던 "소비자별 계약 차이"가 전수 diff 결과 실재하지 않았다.
- 계약이 같은 것을 복제하면 한쪽만 고쳐지는 드리프트가 생긴다(`FileService` 선례).
- 이 모듈은 domain 어댑터가 아니라 presentation 공유 유틸이므로 `security-module` 선례대로 `implementation`으로 노출하고 `bootJar`를 끈다.

## 결과·감수한 손실

- 통합 파일은 원본 대비 package 선언 외 차이가 0줄이라 Swagger 스키마와 HTTP 응답 계약은 바뀌지 않았다(커밋 메시지).
- `FileService`는 이후 계층 재배치로 소멸했다 — 표현 모듈이 `@Transactional` 경계를 갖는 것이 문제여서 application 유스케이스(처음에는 `application`의 `FileUpload*CommandService`, 지금은 앱 모듈의 `MemberFileUploadService`·`FileManagementUploadService`·`FileOwnerUploadService`)로 내려갔고 `apicommon.file` 패키지는 없다.
- 커밋은 admin↔ceo 바이트 동일이던 shop 응답 3종(`ShopBreakTimeResponse`·`ShopBusinessHourResponse`·`ShopHygieneBadgeResponse`)도 통합 대상으로 적었으나, 챕터 09에서 확인한 결과 세 record는 `api-common-module`에 존재한 적이 없고 admin·ceo가 각자 소유한다(문서 드리프트 — admin은 챕터 06, ceo는 챕터 09로 각 api 모듈의 `adapter/in/web/response/`에 있다). 필드가 같은 것은 중복이 아니라 우연히 일치한 앱별 응답 계약이다.
- `backend/CLAUDE.md`의 "통합된 것" 표는 `ProblemDetails`(`apicommon.exception`)도 적었다 — web-api 핸들러와 공용 핸들러의 `problemDetail(...)` private 헬퍼가 바이트 동일 복제라, web-api도 이것만은 공유한다.
- web-api와 공용 `GlobalExceptionHandler`의 충돌 해소 방식은 스캔 범위 → 조건부 `@Bean`([ADR-0017](0017-library-auto-configuration.md)) → 다시 스캔 범위([ADR-0038](0038-autoconfiguration-imports-removal.md))로 바뀌었다.

## 영향받은 문서

- `backend/CLAUDE.md` · "페이징 응답 공용 제네릭 래퍼 규칙 (`PaginationResponse<T>`)" · `PaginationResponse`는 `api-common-module` 단독 소유
- `backend/CLAUDE.md` · "api 모듈 공용 플럼빙 소유 규칙 (`api-common-module`)" > "통합된 것 (`api-common-module` 소유)" · "복제를 유지하는 것 (허용 목록 — 통합 금지)" · 완전 동일한 것만 통합
- `backend/AGENTS.md` · "Module Dependency Graph" · api 모듈 공용 플럼빙은 `api-common-module`이 단독 소유
- `backend/api-common-module/AGENTS.md` · "Purpose" · 모듈 신설 이유
- `backend/admin-api/AGENTS.md` · "봉인·가드 목록" > "`ShopBusinessHourResponse` / `ShopBreakTimeResponse` / `ShopHygieneBadgeResponse` — 필드가 같아도 ceo·공유 모듈로 통합하지 않는다" · shop 응답 3종은 앱별 계약
