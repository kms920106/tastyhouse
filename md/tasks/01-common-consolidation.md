# 작업지시서 01 — 모듈 간 common 패키지 정리

## 배경 (왜)

`web-api`와 `admin-api`는 서로 독립된 프레젠테이션 모듈로, 각자 `common/` 패키지에 API 응답 래퍼·페이징 관련 타입을 두고 있다. 그런데 조사 결과 이 타입들이 **패키지 선언 줄만 다르고 내용은 완전히 동일한(byte-identical) 중복**으로 확인되었다. 또한 예외 처리·보안 설정의 파일 위치가 두 모듈 사이에서 서로 다른 방식으로 조직되어 있어, 같은 역할의 코드를 찾을 때 모듈마다 다른 곳을 뒤져야 하는 비일관성이 있다.

이 프로젝트는 "모듈 간 공유 모듈 없음" 관례를 갖고 있다(루트 `CLAUDE.md`의 "페이징 응답 공용 제네릭 래퍼 규칙" 절 참고 — `ApiResponse<T>`/`PageRequest`가 이미 `adminapi.common`·`webapi.common`에 모듈별로 중복 배치된 선례를 따라 `PaginationResponse<T>`도 두 모듈에 각각 둔다고 명시). 즉 현재의 중복은 **의도된 설계**다. 이 작업지시서의 목적은 중복을 무조건 없애는 것이 아니라, (1) 이 관례를 유지할지 재검토할지 결정하고, (2) 결정과 무관하게 존재하는 위치·네이밍 불일치는 정리하는 것이다.

## 현재 상태 (근거)

### 완전 중복 확인된 타입 (byte-identical, 패키지 선언만 다름)

- `admin-api/src/main/java/com/tastyhouse/adminapi/common/ApiResponse.java` ↔ `web-api/src/main/java/com/tastyhouse/webapi/common/ApiResponse.java`
- `admin-api/.../adminapi/common/PageRequest.java` ↔ `web-api/.../webapi/common/PageRequest.java`
- `admin-api/.../adminapi/common/PaginationResponse.java` ↔ `web-api/.../webapi/common/PaginationResponse.java`

### 위치 불일치

| 역할 | admin-api | web-api |
|---|---|---|
| GlobalExceptionHandler | `adminapi/common/GlobalExceptionHandler.java` | `webapi/exception/GlobalExceptionHandler.java` |
| SecurityConfig | `adminapi/config/security/SecurityConfig.java` | `webapi/config/SecurityConfig.java` |
| 공개 경로(permit-all) 관리 | `SecurityConfig` 내부 인라인 배열(`PUBLIC_PATHS`) | 별도 상수 클래스 `webapi/config/PublicPaths.PATTERNS` |

admin-api는 `common/`에 `FileResponse.java`와 `GlobalExceptionHandler.java`를 함께 두고, web-api는 예외 관련 타입(`GlobalExceptionHandler`, `RateLimitException`, `UnauthorizedException`)을 별도 `exception/` 패키지로 분리해두는 등 패키지 분류 기준 자체가 모듈 간 다르다.

## 작업 지시

### 1단계 — 정책 결정 (코드 수정 전 필수)

다음 두 선택지 중 하나를 결정한다. 결정 근거를 이 문서 하단 "결정 로그" 절에 기록한다.

- **선택지 A (현행 관례 유지)**: 완전 중복은 그대로 두되(각 모듈이 독립적으로 빌드/배포 가능해야 한다는 헥사고날 경계 원칙 유지), 위치·네이밍만 통일한다. `GlobalExceptionHandler`는 두 모듈 모두 `exception/` 패키지로, `SecurityConfig`는 두 모듈 모두 `config/security/`로, 공개 경로는 두 모듈 모두 별도 상수 클래스(`PublicPaths`)로 통일.
- **선택지 B (공유 모듈 신설)**: `web-common` 같은 신규 Gradle 모듈을 만들어 `ApiResponse`/`PageRequest`/`PaginationResponse`/`GlobalExceptionHandler` 공통 부분을 옮기고, web-api·admin-api가 이를 `implementation`한다. 단, GlobalExceptionHandler는 모듈별로 처리해야 할 예외 타입이 다를 수 있으므로(web은 `RateLimitException` 처리 필요, admin은 불필요) 완전 통합이 가능한지 먼저 두 파일을 diff하여 확인한다.

**권장**: 선택지 A. 이유는 (1) 프로젝트가 이미 이 관례를 `PaginationResponse` 도입 시점에 명시적으로 채택했고, (2) 두 모듈은 배포 단위가 다른 별도 애플리케이션(관리자용/일반용)이라 완전한 컴파일 독립성이 실익이 있다. 선택지 B로 갈 경우 신규 모듈이 늘어나 `settings.gradle`·양쪽 `build.gradle`·CI(부재하지만 향후 대비)에 영향이 커진다.

### 2단계 — 위치·네이밍 통일 (선택지 A 기준)

1. `web-api/src/main/java/com/tastyhouse/webapi/exception/GlobalExceptionHandler.java`를 기준으로, admin-api의 `adminapi/common/GlobalExceptionHandler.java`를 `adminapi/exception/GlobalExceptionHandler.java`로 이동.
2. `admin-api/src/main/java/com/tastyhouse/adminapi/config/security/SecurityConfig.java`를 기준으로, web-api의 `webapi/config/SecurityConfig.java`를 `webapi/config/security/SecurityConfig.java`로 이동.
3. admin-api의 `SecurityConfig` 내부 인라인 `PUBLIC_PATHS` 배열을 web-api의 `PublicPaths` 패턴을 따라 `adminapi/config/security/PublicPaths.java` 상수 클래스로 추출.
4. admin-api `common/FileResponse.java`는 응답 DTO이므로 `file/response/FileResponse.java`로 이동(도메인 접두어 규칙에도 부합 — 이미 `File` 접두어 보유하니 위치만 이동).
5. 이동 후 두 모듈 모두 컴파일 확인(단, gradle build 테스트는 진행하지 않음 — CLAUDE.md 규칙).

### 3단계 — 문서 갱신

루트 `CLAUDE.md`의 "페이징 응답 공용 제네릭 래퍼 규칙" 절 하단에, 이번 정리로 `GlobalExceptionHandler`/`SecurityConfig`/공개경로 관리 위치도 두 모듈이 통일되었음을 한 줄 추가한다(신규 컨벤션이 생긴 것은 아니고 기존 산발적 상태를 규칙에 맞게 정렬한 것이므로 "규칙 변경"이 아니라 "적용 확인" 성격으로 서술).

## 완료 기준

- [ ] 1단계 정책 결정이 이 문서에 기록됨
- [ ] `GlobalExceptionHandler`, `SecurityConfig`, 공개 경로 관리 클래스의 패키지 위치가 web-api/admin-api 양쪽에서 동일한 상대 경로를 가짐
- [ ] `FileResponse`가 `file/response/`로 이동
- [ ] 이동한 클래스들의 import 경로가 프로젝트 import 순서 규칙(java → javax → 서드파티 → `com.tastyhouse.*`)을 따름
- [ ] CLAUDE.md에 한 줄 갱신(신규 규칙 아님, 기존 규칙 적용 확인 문구)

## 주의사항

- **완전 중복 타입(`ApiResponse`/`PageRequest`/`PaginationResponse`) 자체는 선택지 A를 택했다면 건드리지 않는다** — 이것은 버그가 아니라 이 프로젝트의 명시적 설계 결정이다(CLAUDE.md 참고).
- 파일 이동 시 참조하는 모든 import 문을 함께 갱신해야 한다. IDE의 안전한 rename/move 기능을 우선 사용할 것.
- `GlobalExceptionHandler` 이동 시 admin-api와 web-api가 처리하는 예외 타입 목록이 서로 다를 수 있으므로(web은 `RateLimitException`도 처리), 파일 내용 자체는 옮기지 말고 위치만 이동한다.
- NO_COMMIT_OR_ROLLBACK — 작업 완료 후 커밋은 직접 하지 말고 추천 커밋 메시지만 제시한다. 형식: `refactor(common): {요약}` (예: `refactor(common): 예외처리·보안설정 패키지 위치를 모듈 간 통일`).
