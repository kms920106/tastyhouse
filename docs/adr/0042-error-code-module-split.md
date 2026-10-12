# ADR-0042: 에러코드·예외를 그것을 던지는 가장 안쪽 모듈이 소유하도록 분할한다

- 상태: 채택
- 결정 커밋: `b188384ee` (2026-10-05)
- 대체한 결정: [ADR-0025](0025-presentation-domain-cut.md)(ErrorContracts 미러링 부분)

## 맥락

- **에러코드**: HTTP 상태·`errorCode` 문자열·메시지를 묶은 enum 상수입니다. 프론트는 `errorCode`로 분기하므로 이 값은 **wire 계약**(바꾸면 클라이언트가 깨지는 약속)입니다.
- **`BusinessException`**: 에러코드를 담아 던지는 예외이고, 표현 계층이 이것을 `ProblemDetail` 응답으로 바꿉니다.

결정 전에는 단일 enum `domain.exception.ErrorCode`(442개)가 모든 에러를 담았습니다. 실측해 보니 domain 모듈이 도메인 불변식이 아닌 유스케이스 실패(조회 실패·인가·중복) 289개와, 앱 하나만 쓰는 코드 180개까지 소유하고 있었습니다. 변경 이유가 다른 것이 한 파일에 모여 있었고, 앱 모듈 재분할([ADR-0036](0036-app-marker-removal.md))로 얻은 "다른 앱 것을 쓰면 컴파일 에러" 게이트가 에러코드에만 적용되지 않았습니다.

## 이전 규칙

### backend/CLAUDE.md — 예외·에러코드 소유 규칙 (`ErrorCodeSpec` 공통 계약 + `BusinessException` 단일 계층)

- **카탈로그는 하나**: `ErrorCode`를 도메인(컨텍스트)별로 쪼개지 않았습니다. "상수 230여 개의 단일 카탈로그"로 서술됐습니다.
- **`ErrorCodeSpec`**: `domain.exception`의 순수 Java 인터페이스였습니다. 구현체는 `ErrorCode` 하나뿐이었고, "필요하면 domain이 모듈별 에러 카탈로그를 다시 호스트할 수 있는 확장점"으로 유지했습니다.
- **새 예외 타입을 만들지 않습니다**: 생성자를 위임만 하는 서브클래스를 금지했습니다. 근거는 과거 `ExternalApiException`이 생성자 위임뿐인 빈 서브클래스였다는 점이었습니다.
- **외부 연동 실패 코드도 단일 카탈로그 `ErrorCode`가 소유한다**: 원래는 외부 연동 전용 `ExternalApiErrorCode`(`infrastructure:http-client` 소유)가 따로 있었습니다. 그 탓에 admin-api·ceo-api가 502로 의도된 외부 연동 실패를 500으로 응답하는 결함이 있었습니다. 그래서 `ExternalApiException`·`ExternalApiErrorCode`를 삭제하고, 상수 5개를 code·상태(502)를 유지한 채 `ErrorCode`로 옮겼습니다(`b02f83ecc`).
- **표현 계층이 직접 쓰는 에러코드 상수는 `ErrorContracts`가 미러링한다**([ADR-0025](0025-presentation-domain-cut.md) 덩어리 01): `application.shared.error.ErrorContracts`가 문자열 미러 3개를 제공하고 `ErrorContractsConsistencyTest`가 일치를 검사했습니다. 필터는 `ErrorContracts.authRequired()`·`accessDenied()`를 썼습니다.
- **`ErrorCodeConventionTest`**(`domain` 순수 단위 테스트)가 카탈로그 규약을 지켰습니다.
- `ResourceNotFoundException`은 domain에 있었습니다(domain은 이것을 던진 적이 없습니다).
- domain·application 코드를 함께 던지는 메서드의 당시 예: `web-application`의 `payment/service/PaymentQueryService.java`가 `validateOwnership`을 오버로드 2개로 나눠 가졌습니다.

### backend/CLAUDE.md — 외부 연동 실패 코드 / `ErrorCodeSpec` 유지 근거 (restclient 문서에서 이관된 이력 포함)

- 외부 연동 상수 5개(`SMS_SEND_NO_RESPONSE`·`SMS_SEND_FAILED`·`SMS_SEND_API_ERROR`·`MAIL_SEND_FAILED`·`ADMIN_DONG_BOUNDARY_FETCH_FAILED`)는 도메인 `ErrorCode`(`backend/domain/src/main/java/com/tastyhouse/domain/exception/ErrorCode.java`)에 있었고, 어댑터(`SolapiSmsClient`·`JavaMailAdapter`·`SesMailSender`·`SnsSmsSender`·`AdminDongBoundaryClient`)가 `new BusinessException(ErrorCode.X[, cause])`를 직접 던졌습니다.
- `ErrorCodeSpec`을 폐지하지 않은 당시 이유: `BusinessException`의 필드·생성자·`getErrorCode()`가 `ErrorCodeSpec` 타입이어야 하고(`ErrorCode`로 좁히면 API 변경), `ErrorCodeConventionTest`의 계약 검증 케이스 1건도 필요했습니다. 두 `GlobalExceptionHandler`는 `e.getErrorCode().getCode()`/`getHttpStatusCode()`만 호출해 구체 타입을 몰랐습니다.

### backend/CLAUDE.md — DTO 조립 규칙 (Command 가드)

Command record의 검증 가드는 `BusinessException(ErrorCode.INVALID_INPUT)`(domain)을 던졌습니다. "예외는 계층 칸이 없는 횡단 관심사"라는 이유로 `commandRecordsShouldBeBoundaryTyped`·`BatchSchedulerRulesTest#inboundPortsShouldBeBoundaryTyped`가 `domain.exception..`을 carve-out으로 허용했습니다.

### 모듈 AGENTS.md

restclient 문서에 "예외·에러코드를 두지 않는 이유" 절이 있었습니다. bbq·firebase·admdongkor 등 벤더 문서는 실패 코드를 단일 `ErrorCode` 소유로 서술했습니다. 예를 들어 bbq의 `RemoteImageDownloader`는 과거 `BusinessException(ErrorCode.FILE_SIZE_EXCEEDED)`를 직접 던졌고, admdongkor는 `BusinessException(ErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED)`를 던진다고 서술돼 있었습니다. web-api 문서는 401도 `BusinessException`에 `ErrorCode.AUTH_*`를 담는다고 적었습니다.

- `backend/infrastructure/restclient/AGENTS.md` — 어댑터 작성 규칙: "에러는 도메인 `BusinessException(ErrorCode.X[, cause])`을 직접 던진다. 모듈마다 예외 타입을 새로 만들고 전역 핸들러에 `@ExceptionHandler`를 추가하지 않는다. `RestClientResponseException`을 catch해 이 형태로 번역한다"였습니다.
- `backend/infrastructure/bbq/AGENTS.md` — Internal: `domain`을 예외 계약(`BusinessException`·`ErrorCode`)용으로 `implementation` 의존한다고 적었습니다.
- `backend/infrastructure/firebase/AGENTS.md` — Internal: `FirebaseFileStorage#delete`가 `FILE_DELETE_FAILED`를 던지는 대신 `FileDeleteResult`를 반환한다고 서술하며, 그 상수가 단일 `ErrorCode`에 남아 있었습니다.
- `backend/infrastructure/redis/AGENTS.md` — rate limit 응답은 단일 `ErrorCode.RATE_LIMIT_EXCEEDED`(덩어리 01 이후 `ErrorContracts#rateLimit`)로 조립된다고 적었습니다.
- `backend/api-common-module/AGENTS.md` — `RateLimitException` 항목: `handleRateLimitException`이 `ErrorContracts#rateLimit`의 code·message로 응답을 조립했습니다. 필터·핸들러가 쓰는 에러 상수의 출처는 `ErrorContracts`(`rateLimit()`·`accessDenied()`·`authRequired()`)였습니다.
- `backend/security-module/AGENTS.md` — `JwtAuthenticationEntryPoint`/`JwtAccessDeniedHandler`의 401·403 출처는 `ErrorContracts.authRequired()`/`accessDenied()`였고, 이 모듈의 의존은 `application`뿐이었습니다.
- `backend/domain/AGENTS.md` — 카탈로그 가드는 `backend/domain/src/test/java/com/tastyhouse/domain/exception/ErrorCodeConventionTest.java`였습니다. 에러 카탈로그 규약 표에는 `INVALID_INPUT`(400, Command compact constructor 가드 전용)과 `ENTITY_NOT_FOUND`(fallback — 도메인별 전용 `*_NOT_FOUND`가 있으면 그쪽을 쓴다)가 domain 상수로 있었습니다.
- `backend/application-core/AGENTS.md` — `commandRecordsShouldBeBoundaryTyped`의 carve-out은 덩어리 01 이후 2건(`domain.exception..`·`MultipartFile`)이었고, `domain.exception..`은 "Command가 `application`에 살아 여전히 domain을 보고, compact constructor 가드가 `BusinessException`을 던진다"는 이유로 유지됐습니다. batch 엄격판 `BatchSchedulerRulesTest#inboundPortsShouldBeBoundaryTyped`는 `com.tastyhouse.domain.exception` 하나만 carve-out으로 허용했습니다. 앱 네임스페이스 View 표의 `product/port/out/ProductAvailabilityChangeView.java`는 domain `product/model`의 `ProductAvailabilityChangeResult`와 짝을 이뤘습니다.

### docs/domain/ceo.md — 점주 이력 조회 보관 기간

보관 기간 위반 에러코드 4개는 단일 `ErrorCode`가 소유했습니다(지금은 ceo 전용이라 `CeoErrorCode`).

## 결정

에러코드와 예외는 그것을 던지는 가장 안쪽 모듈이 소유합니다.

| 항목 | before | after |
|---|---|---|
| domain 에러코드 | `domain.exception.ErrorCode` 442개 | `domain.exception.DomainErrorCode` 153개 |
| core 에러코드 | 없음 | `application.shared.exception.ApplicationErrorCode` 84개 |
| 앱 에러코드 | 없음 | 같은 패키지(split package) `WebErrorCode` 86 · `AdminErrorCode` 28 · `CeoErrorCode` 65 · `BatchErrorCode` 1 — 각 `{앱}-application` 모듈 |
| api 에러코드 | `ErrorContracts`(문자열 미러 3개) | `apicommon.exception.ApiErrorCode`(`RATE_LIMIT_EXCEEDED`·`ACCESS_DENIED`·`AUTH_REQUIRED`). `ErrorContracts` 삭제 |
| infrastructure 에러코드 | 없음 | 없음 — 벤더는 실패를 결과 record로 돌려주고 application이 번역한다 |
| 기반 예외 | `BusinessException`(구체, public 생성자) | `abstract BusinessException`(protected 생성자 3개, `getErrorCode(): ErrorCodeSpec`) |
| 계층별 예외 | `BusinessException` 직접 | `DomainException`(`DomainErrorCode`만) · `ApplicationException`(`ApplicationErrorCodeSpec`만) |
| `ResourceNotFoundException` | domain | `application.shared.exception`, `extends ApplicationException` |
| 죽은 상수 | 25개(던지는 곳 0) | 23개 삭제, `ACCESS_DENIED`·`RATE_LIMIT_EXCEEDED`는 `ApiErrorCode`로 이동 |
| Command 가드 | `BusinessException(ErrorCode.INVALID_INPUT)`, `domain.exception..` carve-out | `ApplicationException(ApplicationErrorCode.INVALID_INPUT)`, carve-out 제거 — `domain..` 전면 금지 |
| 규약 가드 | `ErrorCodeConventionTest`(domain) | `ErrorCatalogConventionTest`(7개 카탈로그) + `ErrorCatalogSnapshotTest` |

## 근거

- **변경 이유와 컴파일 게이트**: 어느 계층·어느 앱의 실패인가가 변경 이유입니다. 컨텍스트 축(주문·쿠폰별)으로 나누는 것은 여전히 금지이며, 이번 분할은 계층·모듈 축입니다.
- **계층별 예외는 "빈 서브클래스"가 아닙니다**: `DomainException`과 `ApplicationException`은 받는 코드 타입이 달라 "도메인이 앱 코드를 던짐", "web 서비스가 ceo 코드를 던짐"이 컴파일 에러가 됩니다. 과거 `ExternalApiException`은 생성자 위임뿐이고 타입 차이가 없었습니다. "새 예외 타입을 만들지 않는다" 항목은 이 차이를 근거로 뒤집혔습니다.
- `INVALID_INPUT`은 application 모듈들만 던지는 유스케이스 실패라 `ApplicationErrorCode`로 옮겨졌습니다. 그 결과 Command의 `domain.exception` carve-out을 받던 참조가 0건이 되어 규칙을 강화했습니다.

## 결과·감수한 손실

- **wire 계약 불변**: HTTP status·`errorCode`·message는 `ErrorCatalogSnapshotTest`가 분할 전 스냅샷(`application-core/src/test/resources/error-catalog-before.tsv`)과 419행 전수 비교로 보증합니다.
- 생성자를 위임만 하고 받는 타입이 같은 서브클래스는 여전히 만들지 않습니다. 전용 `@ExceptionHandler`를 모듈마다 추가하지 않는다는 규칙도 그대로입니다.
- 필터 단계의 참조는 `ErrorContracts.authRequired()`·`accessDenied()` → `ApiErrorCode.AUTH_REQUIRED`·`ACCESS_DENIED`로 바뀌었고 값은 같습니다.
- `ProductAvailabilityFailure`·`ChangeResult`는 ceo-application으로 옮겼습니다. `ProductAvailabilityFailure`는 throw하지 않고 응답으로 변환되는 record라 `ErrorCodeSpec` 컴포넌트를 쓰는 봉인 예외 1건입니다.
- `ErrorContractsConsistencyTest`는 `ErrorResponsesTest`로, domain의 `ErrorCodeConventionTest`는 `ErrorCatalogConventionTest`로 대체됐습니다. `AUTH_REQUIRED` 미러의 일치는 `ErrorCatalogConventionTest#mirroredCodesStayIdentical`이 보증합니다.
- 대가로 `security-module`이 `ApiErrorCode`를 읽으려고 `api-common-module`을 의존하게 됐습니다(`security-module → api-common-module`). `security-module` `LayerRulesTest#shouldNotDependOnDomain`의 `.because`도 갱신됐습니다.
- `ENTITY_NOT_FOUND`는 호출부가 없어 삭제됐고, firebase의 `FILE_DELETE_FAILED`도 단일 `ErrorCode`와 함께 사라져 어느 enum에도 없습니다. bbq는 `domain` 의존이 없어졌습니다(`restclient`·`batch-application`만).
- 오버로드 당시 예였던 `PaymentQueryService#validateOwnership`은 이후 유스케이스 분리([ADR-0043](0043-usecase-per-service.md))로 `PaymentDetailQueryService`(`ApplicationErrorCode.PAYMENT_ACCESS_DENIED`)와 `PaymentByOrderQueryService`(`DomainErrorCode.ORDER_ACCESS_DENIED`)가 각자 하나씩 던지게 되어 오버로드가 필요 없어졌습니다.
- 같은 커밋에서, 문서가 안내하던 `backend/import_order.py`가 저장소에 커밋된 적이 없다는 사실을 정정하고 안내를 지웠습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · 예외·에러코드 소유 규칙 (`ErrorCodeSpec` 공통 계약 + `BusinessException` 단일 계층) · 제목은 앵커 보존용
- `backend/CLAUDE.md` · 예외·에러코드 소유 규칙 > 현재 규칙 — 에러코드·예외는 소유 모듈별로 나뉜다 · 정본
- `backend/CLAUDE.md` · 예외·에러코드 소유 규칙 > 에러코드·예외 세부 규칙 · `ErrorCodeSpec`·예외 타입·`ResourceNotFoundException`·필터 단계 errorCode
- `backend/CLAUDE.md` · DTO 조립 규칙 (`new` 직접 호출 지양) · Command 가드는 `ApplicationException(INVALID_INPUT)`
- `CLAUDE.md` · 리포지토리 구조 · 외부 연동 실패 코드는 그것을 번역하는 앱의 application 에러코드가 소유
- `backend/api-common-module/AGENTS.md` · 봉인·가드 목록 > `ApiErrorCode`는 `application`이 아니라 이 모듈이 소유하고, `AUTH_REQUIRED`는 미러다
- `backend/application-core/AGENTS.md` · "`ErrorResponses` — 정적 유틸이고, cause를 따라가지 않으며, AOP로 하지 않는다" / "패키지 구조 (챕터 03으로 평탄화 — 도메인 아래에 앱별 폴더가 없다)" · `application.shared.exception`
- `backend/infrastructure/restclient/AGENTS.md` · 예외·에러코드를 두지 않는 이유
- `backend/domain/AGENTS.md` · 코드 주석에서 이관된 설계 근거 > 에러 카탈로그 규약 — 그룹 주석에서 이관
- `backend/infrastructure/bbq/AGENTS.md` · "주의" · 실패 코드 소유 서술
- `backend/infrastructure/firebase/AGENTS.md` · "주의" · 같은 취지
- `backend/infrastructure/admdongkor/AGENTS.md` · "수집 방식" · 같은 취지
- `backend/{web,admin,ceo}-api/AGENTS.md` · "Working In This Directory" / "Dependencies" > "Internal" · 같은 취지
- `docs/domain/ceo.md` · 점주 이력 조회 보관 기간 · 에러코드 카탈로그 참조
