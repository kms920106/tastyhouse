# P12. api 모듈 3중 중복 통합 + 포트 부재 경계 정리

## 배경

동일 클래스명이 web∩admin 73개, admin∩ceo 34개, 3모듈 공통 20개. diff가 package 선언 1줄뿐인 완전 복제가 다수다. CLAUDE.md는 "소비자가 다르면 통합하지 않는다"고 하지만, 그 근거(과잉 노출 방지)는 필드 셋이 동일한 사례엔 성립하지 않는다. JWT를 security-module로 통합한 선례가 이미 있다.

## 문제 상세

### 12-1. 플럼빙 완전 복제 (admin↔ceo, diff 라인 수)

| 파일 | diff |
|---|---|
| `exception/GlobalExceptionHandler.java` (93행) | 1줄(package) |
| `common/ApiResponse.java` (47행) | 1줄 |
| `config/security/PublicPaths.java` | 1줄 |
| `common/PaginationResponse.java` | 1줄 |
| `auth/AuthService.java` (50행) | 5줄 |
| `auth/AuthApiController.java` (50행) | 8줄 |
| `config/jwt/service/TokenService.java` (101행) | 14줄 |

`FileService`도 3모듈에 각각(96/76/73행) 존재.

### 12-2. 도메인 read model 복제

- shop: `admin-api/.../shop/response/` ↔ `ceo-api/.../shop/response/` — `ShopBreakTimeResponse`(1줄), `ShopBusinessHourResponse`(1줄), `ShopHygieneBadgeResponse`(1줄), `ShopAmenityResponse`(2줄), `ShopListItemResponse`(2줄), `ShopDetailResponse`(17줄). `ShopContentBoardCommandService`/`QueryService`, `ShopHygieneBadgeQueryService`도 양쪽 존재.
- order: `web-api/.../order/response/OrderDetailResponse.java` ↔ admin — diff가 package 1줄 + `@Schema` example 1줄. `OrderProductOptionResponse`는 package만 다름. `OrderQueryService`(170행 vs 182행)의 private 매퍼 통째 재작성.

### 12-3. 포트 부재 경계 (관련 정리)

- OAuth: `web-api/.../auth/kakao/KakaoSocialLoginService.java` → `external.oauth.kakao.KakaoOAuthClient` + 응답 DTO 직접 의존 (naver/facebook/apple 4종 동일). 크롤링: `batch-module/.../crawling/bbq/BbqService.java` → `external.crawling.bbq.BbqApiClient` + DTO 3종.
- 역방향 누수: `external-api/.../oauth/kakao/KakaoUserInfoResponse.java:5`·`naver/NaverUserInfoResponse.java:5`가 도메인 enum `MemberGender`를 직접 사용 — 외부 응답 DTO가 도메인 타입 보유.
- `security-module/.../ratelimit/RateLimitException.java:3`이 `domain.exception.ErrorCode`에 결합.

## 작업 지시

**주의: CLAUDE.md의 "모듈별로 각각 둠" 관례(ApiResponse/PageRequest/PaginationResponse 명시)와 정면 충돌하는 작업이다. 실행 전 사용자에게 범위를 체크리스트로 질문한다:**

> 어디까지 통합할까요? (다중 선택 가능)
> - [ ] **A. 플럼빙 공유 모듈화** — GlobalExceptionHandler/ApiResponse/PaginationResponse/PublicPaths/TokenService/AuthService·Controller를 공유 모듈(신설 또는 security-module)로. CLAUDE.md "모듈별 각각 둠" 관례 개정 필요.
> - [ ] **B. shop·order response/서비스 중복만 정리** — admin↔ceo의 shop read model을 한쪽 소유+의존으로(또는 공유 모듈), diff 17줄짜리는 필드 차이 확인 후 분리 유지.
> - [ ] **C. 역방향 누수만 수술** — `KakaoUserInfoResponse`류의 도메인 enum 제거(String 수신 후 web-api Service에서 승격), `RateLimitException`의 ErrorCode 결합 해소, admin/ceo 미사용 external-api 의존 제거(P10과 중복 — 담당 확인).
> - [ ] **D. OAuth 포트화** — domain-module에 `SocialProfilePort`류 포트 신설, external-api가 구현, web-api는 포트 의존. (비용 큼 — 소셜 4종 전면 수정)
> - [ ] **E. 현상 유지 + 문서화만** — 중복을 인정하고 CLAUDE.md에 "복제 허용 목록"으로 명시.

**선택된 범위 실행 시 공통 원칙**:
1. 통합 대상은 "필드 셋·동작이 실제로 동일한 것"만. diff가 있는 것(`ShopDetailResponse` 17줄, `TokenService` 14줄)은 차이가 소비자별 계약 차이인지 우연인지 먼저 판정하고, 계약 차이면 통합하지 않는다.
2. 공유 모듈 신설 시 CLAUDE.md "모듈 경계 규칙"(도메인 포트 없는 공유 기술은 별도 공유 모듈, `implementation` 의존, bootJar 비활성 `java-library`) 및 security-module 선례를 따른다.
3. 관례 개정이 생기면 루트 CLAUDE.md·해당 모듈 AGENTS.md를 함께 갱신하고 커밋 메시지 본문에 명시한다.

## 수용 기준

- [ ] 사용자 범위 결정이 기록됨
- [ ] 결정 범위 내에서: diff 1줄짜리 완전 복제가 통합되거나, 유지 근거가 문서화됨
- [ ] 역방향 누수(external DTO의 도메인 enum) 선택 시: `com.tastyhouse.domain` import가 external-api 응답 DTO에서 0건
- [ ] HTTP 응답 계약 무변경 (Swagger 스키마 전후 대조)
- [ ] 전 모듈 컴파일·테스트 통과 (verify-without-gradle)

## 주의사항

- **가장 결정 의존적인 태스크** — 사용자 선택 없이 통합을 강행하지 말 것. CLAUDE.md가 명시적으로 "모듈별 각각 둠"이라 한 항목(ApiResponse 등)은 관례 개정 승인 없이는 건드리지 않는다.
- admin과 ceo는 인증 주체(adminId/ceoId)·시크릿이 다르다 — Auth/Token 통합 시 JWT 시크릿 분리(`JWT_SECRET_ADMIN` 등)와 principal 차이를 보존해야 한다(security-module의 파라미터형 POJO 선례 참고).
- P3가 web-api auth 계열을 만진다 — Auth 통합(A안) 선택 시 P3 완료 후 착수.
