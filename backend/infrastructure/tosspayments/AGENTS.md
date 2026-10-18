<!-- Parent: ../../AGENTS.md -->

# infrastructure:tosspayments

토스페이먼츠 **벤더 모듈**(`java-library`). 코어 `application-core` 모듈이 소유하는 벤더 SPI `PgProviderGatewayPort`(`com.tastyhouse.application.payment.port.out`)를 `TossPaymentGatewayAdapter`가 구현하고 `provider()`로 코어 소유의 `PgProviderCode.TOSS`(도메인 `PgProvider`와 상수명이 같은 별도 enum — 아래 "포트 반환 타입" 절 참고)를 알린다. 앱이 아니라 PG 채널 모듈 `infrastructure:pg`가 `runtimeOnly`로 조립하며, 결제 건의 `PgProvider`가 `TOSS`면 코어의 라우터(`PgPaymentGatewayRouter`, `application-core`의 `payment/service/`)가 이 어댑터로 넘긴다.

채널·벤더 분리로 `infrastructure:payment`에서 갈라진 벤더 모듈이다. 패키지 루트는 `com.tastyhouse.infrastructure.tosspayments`이고 wire DTO는 루트 패키지에 package-private으로 있으며 `dto/` 하위 패키지는 없다(아래 봉인·가드 목록). ([ADR-0040](../../../docs/adr/0040-package-private-visibility.md))

## 무엇을 소유하는가

```
com.tastyhouse.infrastructure.tosspayments/
├── TossPaymentsModuleConfig.java  @Configuration(proxyBeanMethods = false) + @EnableConfigurationProperties(TossPaymentProperties) — 스캔 없음(앱 ModuleScanConfig가 com.tastyhouse.infrastructure를 스캔). imports 제거로 TossPaymentsModuleAutoConfiguration에서 리네임
├── TossPaymentGatewayAdapter.java            PgProviderGatewayPort 구현, provider()=TOSS
├── TossPaymentClient.java                    결제 승인(confirmPayment)·취소(cancelPayment)·주문번호로 결제 조회(lookupPaymentByOrderId) HTTP 호출 — 동기 RestClient
├── TossPaymentUtils.java                     카드사 코드 매핑·일시 파싱
├── TossPaymentProperties.java                pg.tosspayments.*
├── TossPaymentConfirmRequest.java (package-private, 과거 dto/ 하위)
├── TossPaymentCancelRequest.java (package-private, 과거 dto/ 하위)
└── TossPaymentConfirmResponse.java (package-private, 과거 dto/ 하위)
```

**wire DTO는 이 모듈의 루트 패키지에 package-private으로 있다**(`TossPaymentClient`와 같은 패키지여야 한다). 반환 타입은 `application`이 선언한 `PgConfirmResult`·`PgCancelResult`·`TossPaymentDetail`(`com.tastyhouse.application.payment.port.out`)이며, 토스 응답 → 그 타입으로의 변환은 `TossPaymentGatewayAdapter`가 끝낸다 — `RestClient`·wire DTO 타입이 포트 시그니처로 새어나가지 않는다.

`TossPaymentDetail`(토스 응답 원본)은 아직 `PgConfirmResult.detail`의 타입이고 `TOSS_PAYMENT_RECORD` 원장으로 저장된다. 이 원장의 PG 중립화는 두 번째 벤더 도입 시 함께 판단할 후속 항목이다. 다른 벤더는 그전까지 `detail`을 null로 돌려주고, 도메인 서비스는 null이면 원장 저장을 건너뛴다.

## 포트 반환 타입 — `PgProviderCode`(application 소유) vs `PgProvider`(domain 소유)

`PgProviderGatewayPort.provider()`는 도메인 `PgProvider`를 직접 반환하지 않고, `application`이 새로 선언한 `PgProviderCode`(`com.tastyhouse.application.payment.port.out`, 상수명은 `PgProvider`와 동일 — `TOSS`·`KAKAO`·`NICE`·`KG_INICIS`·`NHN_KCP`·`SETTLE_BANK`)를 반환한다. 소비자 쪽 포트 `PgPaymentGatewayPort`(라우터만 구현, web-application의 `PgPaymentConfirmService`·`PaymentCancelService`와 batch-application의 `PaymentReconcileSchedulerService`가 실제로 호출하는 계약)는 도메인 `PgProvider`의 상수명(`String`)을 받는다.

라우터 `PgPaymentGatewayRouter`(`application-core`의 `payment/service/`)가 등록 시점에 `PgProviderCode.name()` → `PgProvider.valueOf(name)`으로 변환해 두 enum을 잇는다. 이 변환이 어긋나지 않는지는 `application`의 `architecture/EnumCodeConstantsTest`가 상수 집합 일치를 검증한다. 벤더가 새 PG사를 추가하면 `PgProviderCode`와 `PgProvider` 양쪽에 같은 이름으로 상수를 추가해야 하며, 한쪽만 추가하면 이 테스트가 실패한다.

## 어느 앱이 의존하는가

앱은 직접 의존하지 않는다. web-api·batch-module → `infrastructure:pg`(runtimeOnly) → 이 모듈(runtimeOnly)로 전이된다. 벤더 추가·제거 절차는 `../pg/AGENTS.md`.

## yml — `application-tosspayments.yml`

```yaml
pg:
  tosspayments:
    secret-key: ${TOSS_SECRET_KEY}
    base-url: https://api.tosspayments.com
    confirm-path: /v1/payments/confirm
    lookup-path: /v1/payments/orders/{orderId}
```

채널의 `application-pg.yml`이 중첩 import로 로딩한다. 키 접두어는 `{채널}.{벤더}`(`sms.solapi.*`·`file.firebase.*` 선례)이다. 환경변수 `TOSS_SECRET_KEY`는 그대로이고 `.env`로 주입한다(코드 하드코딩 금지). 취소 경로 `cancel-path`는 `TossPaymentProperties`의 기본값(`/v1/payments/{paymentKey}/cancel`)을 쓴다.

`@ConfigurationProperties` 바인딩은 해석하지 못한 placeholder를 문자열 그대로 넣으므로 `TOSS_SECRET_KEY`가 없어도 기동은 성공한다. 키 누락은 첫 승인 호출에서 토스의 인증 오류로 드러난다.

## Dependencies

- `infrastructure:restclient` (implementation) — Boot `RestClient.Builder` customizer. 토스 API 호출은 **동기 `RestClient`**다.
- `application-core` (implementation) — 구현하는 `PgProviderGatewayPort`와 그 계약 타입(`PgCancelResult`·`PgProviderCode`·`PgConfirmResult`·`PgConfirmFailure`·`PgPaymentLookupResult`·`PgPaymentLookupStatus`·`TossPaymentDetail`)이 모두 코어 소유다. 패키지는 모두 `com.tastyhouse.application.payment.port.out`. `:web-application`을 의존하지 않는다 — 이 모듈은 web·batch 두 앱에 조립되므로, 앱 모듈을 의존하면 batch 클래스패스에 web-application이 실려 web 빈 전체가 batch에 뜬다([ADR-0061](../../../docs/adr/0061-pg-ports-to-core-batch-reconcile.md)). `:domain` 의존은 없다 — 이 모듈은 도메인 `PgProvider`를 참조하지 않는다(위 "포트 반환 타입" 절 참고)
- `infrastructure:pg`를 의존하지 않는다(순환 방지)
- 테스트: `TossPaymentClientTest`(11건)·`TossPaymentGatewayAdapterTest`(11건) — `MockRestServiceServer.bindTo(RestClient.builder())` 기반. `TossPaymentProperties`를 생성자로 직접 만들므로 프로퍼티 컴포넌트를 추가하면 두 테스트의 `setUp`도 함께 고친다

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **`@ConditionalOnProperty`를 붙이지 않는다** — 결제 벤더는 배타 선택이 아니라 공존한다(`../pg/AGENTS.md`의 봉인 항목).
- **결제 실패는 예외가 아니라 결과로 돌려준다** — `TossPaymentClient`는 HTTP 오류·무응답을 `code`/`message`가 채워진 응답 객체로 바꾸고, 어댑터가 `PgConfirmResult.success=false`·`PgCancelResult.success=false`로 옮긴다. 성공·실패 판정과 `BusinessException` 번역은 application(`PgPaymentConfirmService`·`PaymentCancelService`)이 한다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.infrastructure.tosspayments` 봉인

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/`

옛 위치 `external.pg.toss`로 되돌리지 않는다 — 채널 모듈 `infrastructure:pg`의 스캔에 동반 등록된다. 이 모듈의 루트는 `com.tastyhouse.infrastructure.tosspayments`이고 main 클래스는 전부 그 아래에 있어야 한다 — `backend/infrastructure/tosspayments/src/test/java/com/tastyhouse/infrastructure/tosspayments/architecture/VendorLayerRulesTest.java` → `shouldResideInModuleRootPackage`이 강제한다. ([ADR-0037](../../../docs/adr/0037-infrastructure-package-root.md))

### 어댑터는 `PgPaymentGatewayPort`를 직접 구현하지 않는다

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/TossPaymentGatewayAdapter.java`

`PgPaymentGatewayPort`의 구현은 라우터 하나여야 한다. 벤더가 그것을 구현하면 두 번째 벤더가 들어오는 순간 application의 단일 주입이 모호해져 web-api·batch-module 기동이 실패한다.

### 최상위 클래스에 `public`을 붙이지 않는다 (package-private 적용)

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/` 의 모든 최상위 타입 · 가드 `backend/infrastructure/tosspayments/src/test/java/com/tastyhouse/infrastructure/tosspayments/architecture/VendorLayerRulesTest.java` → `topLevelClassesShouldNotBePublic`

이 모듈의 최상위 타입은 전부 package-private이다(허용 목록 없음). 앱이 이 모듈을 타입 이름으로 부르지 않고 `ModuleScanConfig`의 문자열 스캔으로만 조립하며, 소비자는 `application`이 소유한 포트로만 주입받기 때문이다. `public`을 붙이면 다른 모듈이 구현에 직접 결합할 수 있게 되므로 가드가 `build/classes/java/main`의 최상위 클래스를 검사해 빌드를 실패시킨다. 생성자·메서드의 `public`은 유지한다.

외부 API wire DTO(`TossPaymentConfirmRequest`·`TossPaymentCancelRequest`·`TossPaymentConfirmResponse`)도 예외가 아니다. **`{루트}.dto` 하위 패키지로 되돌리지 않는다** — package-private 타입은 같은 패키지에서만 보이므로, 그 DTO를 쓰는 Client와 같은 루트 패키지에 있어야 한다. Jackson은 별도 가시성 설정 없이(기본 `CAN_OVERRIDE_ACCESS_MODIFIERS`) package-private record를 (역)직렬화한다. `TossPaymentClientTest`가 역직렬화를 확인한다. 근거와 전체 범주는 `backend/CLAUDE.md`의 "접근 제어자 규칙 (내부 구현은 package-private)" 절.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 토스 승인 응답의 에러 필드는 성공 응답과 한 타입에 담긴다

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/TossPaymentConfirmResponse.java`

승인 응답 wire DTO 안에 **에러 응답 필드가 함께 선언돼 있다.** 토스가 성공·실패를 같은 엔드포인트에서 돌려주기 때문이며, 실패 판별은 `TossPaymentGatewayAdapter`가 수행한다. 이 필드들을 별도 DTO로 떼어내면 어댑터가 응답 본문을 두 번 역직렬화해야 하므로 분리 대상이 아니다.

### 승인 4xx 응답은 토스 본문의 code·message를 보존한다

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/TossPaymentClient.java` → `confirmPayment` · `readErrorBody`, `.../TossPaymentGatewayAdapter.java` → `toFailure`

토스는 승인 거절을 HTTP 4xx로 돌려주고 RestClient는 이를 `RestClientResponseException`으로 던진다. 이 분기는 `e.getResponseBodyAs(TossPaymentConfirmResponse.class)`로 본문을 읽어 토스의 code·message를 그대로 돌려준다. code를 `PG_API_ERROR`로 덮어쓰면 동시 중복 승인의 `ALREADY_PROCESSED_PAYMENT`를 어댑터가 알아볼 수 없기 때문이다.

- **4xx만 대상이다**: 5xx·알 수 없는 상태 코드는 본문에 code가 있어도 `PG_SERVER_ERROR`로 만들고 ERROR 로그(스택 포함)를 남긴다. 4xx는 정상 흐름의 거절(중복 승인 포함)이라 스택 없이 WARN으로 남긴다. `TossPaymentClientTest#confirmPaymentServerError`가 5xx 경로를 지킨다.
- **폴백**: 4xx 본문이 JSON이 아니거나(`UnknownContentTypeException`) 깨졌거나(`RestClientException`) code가 비어 있으면 `PG_UNREADABLE_RESPONSE` 응답을 만든다. 이 응답은 **결과 불명**이다. 토스 문서는 실패 시 `{code, message}` JSON 에러 객체가 돌아온다고 하므로(토스 에러 코드 문서. 408·429는 그 표에 없다), 해석할 수 없는 4xx는 토스의 정상 에러 응답이 아니다. 중간 인프라(프록시·로드밸런서·CDN)의 응답일 수도 있고, 토스가 직접 보낸 비JSON 응답일 수도 있다(라이브 환경은 요청이 과다하면 `429 Too Many Requests`가 올 수 있다고 거래 조회 문서에 적혀 있다. 테스트 환경 실측에서 요청 제한(분당 100건 초과)에 걸린 429는 `server: envoy`가 보낸 JSON 에러 객체 `{"code":"TOO_MANY_REQUESTS"}`였고 이 코드는 에러 코드 문서에 없다. 본문을 읽을 수 있으므로 이 429는 `PG_UNREADABLE_RESPONSE`가 아니라 토스 code 그대로 `REJECTED`로 번역된다). 출처를 알 수 없으므로 토스가 승인을 처리했는지 알 수 없고, 이를 거절로 확정하면 돈은 나갔는데 결제가 FAILED가 될 수 있다. 진짜 토스 거절인데 본문만 깨진 경우에도 조회가 `NOT_APPROVED`를 돌려줘 같은 실패에 도달하므로, 손해는 조회 1회만큼의 지연이다([ADR-0063](../../../docs/adr/0063-pg-confirm-unreadable-4xx-unknown.md)).
- **결과 불명 코드 4개**: 5xx는 `PG_SERVER_ERROR`, 호출 예외(타임아웃 `ResourceAccessException` 포함)는 `SYSTEM_ERROR`, 본문 없는 성공 응답은 `UNKNOWN_ERROR`, 해석할 수 없는 4xx는 `PG_UNREADABLE_RESPONSE`다. 상수는 `TossPaymentClient`의 package-private `static final` 필드이고 어댑터가 같은 패키지에서 참조한다. 이들을 거절 코드와 같은 값으로 두면 어댑터가 "토스가 거절함"과 "토스가 처리했는지 모름"을 구분할 수 없다.
- **`PG_API_ERROR`는 취소 전용이다**: `cancelPayment`의 HTTP 오류 덮어쓰기에만 쓴다. 승인·조회 경로는 이 코드를 만들지 않는다.
- **어댑터 번역**: `toFailure`가 `ALREADY_PROCESSED_PAYMENT`를 `PgConfirmFailure.ALREADY_PROCESSED`로, `PG_SERVER_ERROR`·`SYSTEM_ERROR`·`UNKNOWN_ERROR`·`PG_UNREADABLE_RESPONSE`를 `UNKNOWN`으로, 나머지 실패(토스 JSON 본문의 4xx code)는 `REJECTED`로 번역한다. 로그도 이 번역을 따른다 — `confirmPayment`는 `ALREADY_PROCESSED`면 WARN, 그 밖의 실패는 ERROR로 남긴다. 중복 승인은 정상 흐름이라 클라이언트의 4xx WARN과 심각도를 맞추지 않으면 ERROR 기준 알림의 노이즈가 된다. 토스 문자열은 이 모듈 밖으로 나가지 않는다(소비 측 근거는 `application-core/AGENTS.md`의 PG 승인·실패 반영 항목).
- **사용자 응답**(토스 JSON 본문이 있는 4xx에 한함 — 해석할 수 없는 4xx는 결과 불명 흐름을 탄다): 승인 실패 메시지는 토스 원문이고, 에러코드는 `PAYMENT_APPROVAL_FAILED`·HTTP 상태는 400이다. `TOSS_PAYMENT_RECORD`의 `failureCode`·`failureMessage`에도 원래 code가 남는다.
- `cancelPayment`는 이 처리 대상이 아니며 기존 `PG_API_ERROR` 덮어쓰기를 유지한다.

### 주문번호로 결제 조회 — 결과 불명 대사용

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/TossPaymentClient.java` → `lookupPaymentByOrderId` · `translateResponseError`, `.../TossPaymentGatewayAdapter.java` → `lookupPayment` · `approvedResult`, `.../TossPaymentProperties.java` → `lookupPath`

승인 결과가 `UNKNOWN`이거나 `ALREADY_PROCESSED`면 application(`PgPaymentConfirmService`)이 `PgProviderGatewayPort#lookupPayment`로 토스 `GET /v1/payments/orders/{orderId}`를 부른다. 응답은 Payment 객체라 wire DTO `TossPaymentConfirmResponse`를 재사용하고, 오류 변환은 승인과 같은 `translateResponseError` 한 곳이 한다(4xx 본문 code 보존, 해석할 수 없는 4xx `PG_UNREADABLE_RESPONSE`, 5xx `PG_SERVER_ERROR`, 예외 `SYSTEM_ERROR`, 빈 응답 `UNKNOWN_ERROR`). 경로의 `{orderId}`는 RestClient URI 템플릿 변수로 넘겨 인코딩을 맡긴다.

| 토스 응답 | `PgPaymentLookupStatus` | `PgConfirmResult.failure` |
|---|---|---|
| `status = DONE` | `APPROVED` | `null`(성공 결과 — 승인과 같은 `approvedResult`로 조립) |
| `status = ABORTED`·`EXPIRED`·`CANCELED`, 또는 4xx code `NOT_FOUND_PAYMENT` | `NOT_APPROVED` | `REJECTED` |
| `status = READY`·`IN_PROGRESS`(승인 직후 아직 전이 전이거나 승인 처리 중일 수 있음), `PARTIAL_CANCELED`·`WAITING_FOR_DEPOSIT` 등 예상 밖 상태, `NOT_FOUND_PAYMENT`가 아닌 오류(5xx·예외·빈 응답·그 밖의 4xx) | `UNKNOWN` | `UNKNOWN` |

- **`READY`·`IN_PROGRESS`를 미승인으로 보지 않는다** — 승인 요청 직후 토스 상태가 아직 `READY`에서 전이하지 않았거나, 버튼 연타의 첫 요청이 아직 승인 중일 수 있다. 미승인으로 보면 돈이 나간 결제를 FAILED로 만든다. 토스는 승인되지 않은 결제를 결국 `EXPIRED`로 확정하고, 만료된 결제는 404가 아니라 200 `EXPIRED`로 조회되므로 batch 주기 대사가 그때 실패로 정리한다. 시점이 둘이다. 승인 가능 시한은 인증 후 약 10분이고(웹훅 문서), 조회 상태가 `EXPIRED`로 바뀌는 것은 결제 유효 시간 30분 이후다(코어 API 문서). 테스트 환경 실측은 다음과 같다. 승인 전 결제(`READY`·`IN_PROGRESS`)는 주문번호 조회에서 모두 200으로 보였다. 인증 후 11분 이상 지난 `IN_PROGRESS` 결제에 승인을 호출하면 400 `EXPIRED_PAYMENT`("결제가 만료되었습니다. 다시 시도해주세요.", 토스 에러 코드 문서에 없는 코드)가 오고 그 호출로 결제가 `ABORTED`(`failure.code = EXPIRED_PAYMENT`)로 바뀐다. 승인을 호출하지 않으면 승인 가능 시한이 지나도 조회는 `IN_PROGRESS`로 남다가 결제 생성 후 약 33분(30분에 처리 지연 몇 분)에 `EXPIRED`가 되고, 그 뒤 승인은 404 `NOT_FOUND_PAYMENT_SESSION`이다. 표본은 결제 1~2건이고, 라이브 환경의 시점은 확인되지 않았다. `EXPIRED_PAYMENT`는 `toFailure`에서 `REJECTED`로 번역된다. `TossPayments-Test-Code`로 재현한 승인 5xx(`COMMON_ERROR`, `FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING`)는 결제를 `IN_PROGRESS`로 남겼고 그 뒤 정상 승인이 `DONE`으로 성공했다(재현은 실제 처리 중 장애와 다를 수 있다). 같은 키·같은 결제의 재요청은 `Idempotency-Key`가 같으면 첫 응답과 같은 200, 다르거나 없으면 400 `ALREADY_PROCESSED_PAYMENT`였다. **토스는 실패 응답도 키별로 캐시해 재생한다**: 키 K로 `orderId`만 틀린 승인(403 `FORBIDDEN_REQUEST`, 결제는 `IN_PROGRESS` 유지) 뒤 같은 K로 올바른 승인을 보내면 본문이 달라도 같은 403이 재생됐고, 다른 키로 보내면 `DONE`이었다. 그래서 결제별로 고정된 키(예: `pgOrderId`·`paymentKey`에서 결정적으로 만든 키)를 승인에 붙이면, 한 번 실패 응답을 받은 결제는 그 키의 유효기간(15일) 동안 승인할 수 없게 된다 — 현재 승인 요청에 멱등키를 붙이지 않는 이유다. 도입하려면 실패 뒤 재시도마다 다른 키를 쓰는 설계가 필요하다.
- **`CANCELED`는 미승인이다** — 결제 전액이 취소돼 돈이 돌아간 상태라 우리 결제를 FAILED로 정리해도 돈이 어긋나지 않는다.
- **그 밖의 예상 밖 상태는 `UNKNOWN`이다** — `PARTIAL_CANCELED`(승인 뒤 일부만 취소)·가상계좌 입금 대기처럼 승인·미승인 어느 쪽으로 단정해도 틀릴 수 있는 상태는 사람이 보게 남긴다.
- **금액은 비교하지 않는다** — 어댑터는 DB 금액을 모른다. `totalAmount`를 그대로 실어 보내고 application의 `PaymentConfirmationService#reconcileApproved`가 판정한다.
- **404는 `NOT_FOUND_PAYMENT`만 미승인이다** — 토스 에러 코드 문서(https://docs.tosspayments.com/reference/error-codes)의 "결제 조회" 절에 있는 404는 `NOT_FOUND_PAYMENT`("존재하지 않는 결제 정보")와 `NOT_FOUND`("존재하지 않는 정보") 둘이다. 뒤의 것은 무엇이 없는지 특정하지 않아 `UNKNOWN`으로 둔다. `NOT_FOUND_PAYMENT_SESSION`은 승인 API의 코드라 조회에서 기대하지 않는다. 승인 전에 이탈한 주문을 조회하면 404가 오는지 200 `READY`·`IN_PROGRESS`가 오는지는 문서로 확정되지 않았다 — 404면 "결제 없음 또는 승인 전 이탈"로 묶어 미승인, 200이면 위 표대로 `UNKNOWN`이라 어느 쪽이든 안전하다.
- **응답의 `orderId`가 요청과 다르면 상태와 관계없이 `UNKNOWN`이다** — 토스 보안 체크리스트가 조회 응답의 `orderId`·`totalAmount`·`status` 대조를 요구한다. 다른 주문의 응답으로 우리 결제를 완료시키지 않도록 errorCode `ORDER_ID_MISMATCH`로 돌려주고 ERROR를 남긴다. 대상: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/TossPaymentGatewayAdapter.java` → `lookupPayment`.
- 결정 경위: [ADR-0060](../../../docs/adr/0060-pg-confirm-unknown-lookup.md).
