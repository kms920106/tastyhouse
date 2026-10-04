<!-- Parent: ../../AGENTS.md -->

# infrastructure:tosspayments

토스페이먼츠 **벤더 모듈**(`java-library`). `web-application` 모듈(앱 마커 제거 전에는 `application`)이 소유하는 벤더 SPI `PgProviderGateway`(`com.tastyhouse.application.payment.port.out`)를 `TossPaymentGatewayAdapter`가 구현하고 `provider()`로 `web-application` 소유의 `PgProviderCode.TOSS`(도메인 `PgProvider`와 상수명이 같은 별도 enum — 아래 "포트 반환 타입" 절 참고)를 알린다. 앱이 아니라 PG 채널 모듈 `infrastructure:pg`가 `runtimeOnly`로 조립하며, 결제 건의 `PgProvider`가 `TOSS`면 채널의 라우터(`PgPaymentGatewayRouter`, 지금은 `application/payment/service/`에 있다)가 이 어댑터로 넘긴다.

옛 `infrastructure:payment`의 `toss/` 패키지를 채널·벤더 분리(2026-09-26)로 옮겨 신설됐다. 패키지는 `external.payment.toss` → `com.tastyhouse.external.tosspayments`로 옮겼다 — 채널 모듈의 `@ComponentScan("com.tastyhouse.external.pg")`에 동반 스캔되지 않게 형제 패키지에 둔다. 이후 infrastructure 패키지 루트 통일로 루트가 `com.tastyhouse.infrastructure.tosspayments`가 됐다(`dto/` 구성은 불변).

## 무엇을 소유하는가

```
com.tastyhouse.infrastructure.tosspayments/
├── TossPaymentsModuleAutoConfiguration.java  @AutoConfiguration + @ComponentScan(이 패키지) + @EnableConfigurationProperties(TossPaymentProperties)
├── TossPaymentGatewayAdapter.java            PgProviderGateway 구현, provider()=TOSS
├── TossPaymentClient.java                    결제 승인(confirmPayment)·취소(cancelPayment) HTTP 호출 — 동기 RestClient
├── TossPaymentUtils.java                     카드사 코드 매핑·일시 파싱
├── TossPaymentProperties.java                pg.tosspayments.*
└── dto/
    ├── TossPaymentConfirmRequest.java
    ├── TossPaymentCancelRequest.java
    └── TossPaymentConfirmResponse.java
```

**wire DTO(`dto/`)는 이 모듈에 잔류한다.** 반환 타입은 `application`이 선언한 `PgConfirmResult`·`PgCancelResult`·`TossPaymentDetail`(`com.tastyhouse.application.payment.port.out`, 과거 `domain/payment/port/dto/`에서 이관됨)이며, 토스 응답 → 그 타입으로의 변환은 `TossPaymentGatewayAdapter`가 끝낸다 — `RestClient`·wire DTO 타입이 포트 시그니처로 새어나가지 않는다.

`TossPaymentDetail`(토스 응답 원본)은 아직 `PgConfirmResult.detail`의 타입이고 `TOSS_PAYMENT_RECORD` 원장으로 저장된다. 이 원장의 PG 중립화는 두 번째 벤더 도입 시 함께 판단할 후속 항목이다. 다른 벤더는 그전까지 `detail`을 null로 돌려주고, 도메인 서비스는 null이면 원장 저장을 건너뛴다.

## 포트 반환 타입 — `PgProviderCode`(application 소유) vs `PgProvider`(domain 소유)

`PgProviderGateway.provider()`는 도메인 `PgProvider`를 직접 반환하지 않고, `application`이 새로 선언한 `PgProviderCode`(`com.tastyhouse.application.payment.port.out`, 상수명은 `PgProvider`와 동일 — `TOSS`·`KAKAO`·`NICE`·`KG_INICIS`·`NHN_KCP`·`SETTLE_BANK`)를 반환한다. 소비자 쪽 포트 `PgPaymentGateway`(라우터만 구현, web-api·`PaymentCommandService`가 실제로 호출하는 계약)는 그대로 도메인 `PgProvider`를 쓴다.

라우터 `PgPaymentGatewayRouter`(지금은 `domain`이 아니라 `web-application`의 `payment/service/`에 있다 — 앱 마커 제거 전에는 `application`)가 등록 시점에 `PgProviderCode.name()` → `PgProvider.valueOf(name)`으로 변환해 두 enum을 잇는다. 이 변환이 어긋나지 않는지는 `application`의 `architecture/EnumCodeConstantsTest`가 상수 집합 일치를 검증한다. 벤더가 새 PG사를 추가하면 `PgProviderCode`와 `PgProvider` 양쪽에 같은 이름으로 상수를 추가해야 하며, 한쪽만 추가하면 이 테스트가 실패한다.

## 어느 앱이 의존하는가

앱은 직접 의존하지 않는다. web-api → `infrastructure:pg`(runtimeOnly) → 이 모듈(runtimeOnly)로 전이된다. 벤더 추가·제거 절차는 `../pg/AGENTS.md`.

## yml — `application-tosspayments.yml`

```yaml
pg:
  tosspayments:
    secret-key: ${TOSS_SECRET_KEY}
    base-url: https://api.tosspayments.com
    confirm-path: /v1/payments/confirm
```

채널의 `application-pg.yml`이 중첩 import로 로딩한다. 키 접두어는 `{채널}.{벤더}`(`sms.solapi.*`·`file.firebase.*` 선례)이며 분리 전 `payment.toss.*`에서 바뀌었다. 환경변수 `TOSS_SECRET_KEY`는 그대로이고 `.env`로 주입한다(코드 하드코딩 금지). 취소 경로 `cancel-path`는 `TossPaymentProperties`의 기본값(`/v1/payments/{paymentKey}/cancel`)을 쓴다.

`@ConfigurationProperties` 바인딩은 해석하지 못한 placeholder를 문자열 그대로 넣으므로 `TOSS_SECRET_KEY`가 없어도 기동은 성공한다. 키 누락은 첫 승인 호출에서 토스의 인증 오류로 드러난다.

## Dependencies

- `infrastructure:restclient` (implementation) — Boot `RestClient.Builder` customizer. 토스 API 호출은 **동기 `RestClient`**다.
- `web-application` (implementation, 앱 마커 제거로 `:application`에서 변경) — 구현하는 `PgProviderGateway`·`PgCancelResult`·`PgProviderCode`는 `web-application` 소유이고, `PgConfirmResult`·`TossPaymentDetail`은 코어 `application` 소유다(`web-application`이 `api project(':application')`로 함께 노출한다). 패키지는 모두 `com.tastyhouse.application.payment.port.out`. `:domain` 의존은 없다 — 이 모듈은 도메인 `PgProvider`를 참조하지 않는다(위 "포트 반환 타입" 절 참고)
- `infrastructure:pg`를 의존하지 않는다(순환 방지)
- 테스트: `TossPaymentClientTest`(4건) — `MockRestServiceServer.bindTo(RestClient.builder())` 기반

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **`@ConditionalOnProperty`를 붙이지 않는다** — 결제 벤더는 배타 선택이 아니라 공존한다(`../pg/AGENTS.md`의 봉인 항목).
- **결제 실패는 예외가 아니라 결과로 돌려준다** — `TossPaymentClient`는 HTTP 오류·무응답을 `code`/`message`가 채워진 응답 객체로 바꾸고, 어댑터가 `PgConfirmResult.success=false`·`PgCancelResult.success=false`로 옮긴다. 성공·실패 판정과 `BusinessException` 번역은 application(`PaymentCommandService`)이 한다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.external.tosspayments` 봉인

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/`

`external.pg.toss`로 옮기면 채널 모듈 `infrastructure:pg`의 스캔에 동반 등록된다. ~~`com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 통째 스캔에 걸려 admin·ceo·batch 부팅이 깨진다.~~ **(번복됨 — infrastructure 패키지 루트 통일)** persistence가 자기 루트 `com.tastyhouse.infrastructure.persistence`만 스캔하게 되면서 이 제약이 사라졌다. 지금 이 모듈의 루트는 `com.tastyhouse.infrastructure.tosspayments`이고 main 클래스는 전부 그 아래에 있어야 한다 — `backend/infrastructure/tosspayments/src/test/java/com/tastyhouse/infrastructure/tosspayments/architecture/VendorLayerRulesTest.java` → `shouldResideInModuleRootPackage`이 강제한다.

### 어댑터는 `PgPaymentGateway`를 직접 구현하지 않는다

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/TossPaymentGatewayAdapter.java`

`PgPaymentGateway`의 구현은 라우터 하나여야 한다. 벤더가 그것을 구현하면 두 번째 벤더가 들어오는 순간 application의 단일 주입이 모호해져 web-api 기동이 실패한다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 토스 승인 응답의 에러 필드는 성공 응답과 한 타입에 담긴다

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/dto/TossPaymentConfirmResponse.java`

승인 응답 wire DTO 안에 **에러 응답 필드가 함께 선언돼 있다.** 토스가 성공·실패를 같은 엔드포인트에서 돌려주기 때문이며, 실패 판별은 `TossPaymentGatewayAdapter`가 수행한다. 이 필드들을 별도 DTO로 떼어내면 어댑터가 응답 본문을 두 번 역직렬화해야 하므로 분리 대상이 아니다.
