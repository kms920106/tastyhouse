<!-- Parent: ../../AGENTS.md -->

# infrastructure:payment

토스페이먼츠 PG 연동을 소유하는 어댑터 모듈(`java-library`). `infrastructure:external` 7모듈 분리(챕터 01)로 코어에서 떨어져 나왔고, **자바 패키지 `com.tastyhouse.external.payment..`는 불변**이다(코어 스캔 범위 밖이라 동반 스캔 위험이 없다).

## 무엇을 소유하는가

```
com.tastyhouse.external.payment/
├── PaymentModuleConfig.java          진입점 — 쓰는 앱이 @Import 한다
└── toss/
    ├── TossPaymentGatewayAdapter.java  도메인 포트 PgPaymentGateway 구현
    ├── TossPaymentClient.java          결제 승인(confirmPayment)·취소(cancelPayment) HTTP 호출
    ├── TossPaymentUtils.java           서명·인코딩 등 보조 유틸
    ├── TossPaymentProperties.java      payment.toss.* 프로퍼티
    └── dto/
        ├── TossPaymentConfirmRequest.java
        ├── TossPaymentCancelRequest.java
        └── TossPaymentConfirmResponse.java
```

**wire DTO(`dto/`)는 이 모듈에 잔류한다.** 포트 `PgPaymentGateway`의 반환 타입은 도메인이 선언한 `PgConfirmResult`·`PgCancelResult`·`TossPaymentDetail`이며, 토스 응답 → 그 타입으로의 변환은 `TossPaymentGatewayAdapter`가 끝낸다 — WebClient·wire DTO 타입이 포트 시그니처로 새어나가지 않는다.

## 어느 앱이 의존하는가

**web-api 하나뿐이다.** 결제는 사용자 앱에서만 일어나므로 admin-api·ceo-api·batch-module은 이 모듈을 의존하지도 `@Import` 하지도 않는다. 분리 전에는 세 앱 모두 토스 연동 코드를 클래스패스에 얹고 있었다.

## 진입 설정과 스캔 범위

`PaymentModuleConfig`(`@Configuration(proxyBeanMethods = false)`)가 `@ComponentScan("com.tastyhouse.external.payment")` + `@EnableConfigurationProperties(TossPaymentProperties.class)`를 갖는다. **provider 조건(`@ConditionalOnProperty`)이 없다** — PG는 파일·메일·SMS와 달리 대안 구현이 없어 선택 축 자체가 존재하지 않는다.

## yml — `application-payment.yml`

**web-api만** `spring.config.import`로 로딩한다.

```yaml
payment:
  toss:
    secret-key: ${TOSS_SECRET_KEY}
    base-url: https://api.tosspayments.com
    confirm-path: /v1/payments/confirm
```

`TOSS_SECRET_KEY`는 `.env`로 주입한다(코드 하드코딩 금지). 분리 전 `config/application-payment.yml`의 내용 그대로이며, 달라진 것은 파일 위치와 로딩 경로(허브 `application-external.yml` 경유 → 앱이 직접 import)뿐이다.

## Dependencies

### Internal
- `infrastructure:external` (implementation) — `WebClient.Builder`(`WebClientConfig`), `ExternalApiException`/`ExternalApiErrorCode`
- `domain-module` (implementation) — 구현하는 `PgPaymentGateway` 포트와 그 반환 타입(`payment/port/dto/`), 예외 계약

### External
- `spring-boot-starter-webflux` — 토스 API 호출(`WebClient`). Jackson도 이것이 전이로 제공한다.

**`application`에 의존하지 않는다** — 이 모듈이 구현하는 계약은 도메인 포트 하나뿐이고 아웃바운드 SPI가 없다(oauth·crawling과 다른 점).

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **빈 배선**: web-api만 `@Import(PaymentModuleConfig.class)` 한다. 빠뜨리면 `PaymentCommandService`가 `PgPaymentGateway` 빈을 찾지 못해 **기동 시** 실패한다.
- **결제 실패는 `ExternalApiException`으로 던진다** — 전용 예외 타입과 모듈별 `@ExceptionHandler`를 추가하지 않는다. 응답 `code`는 wire 계약이므로 기존 값을 바꾸지 않는다.
