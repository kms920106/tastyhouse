<!-- Parent: ../../AGENTS.md -->

# infrastructure:payment

토스페이먼츠 PG 연동을 소유하는 어댑터 모듈(`java-library`). `infrastructure:external` 7모듈 분리(챕터 01)로 코어에서 떨어져 나왔고, **자바 패키지 `com.tastyhouse.external.payment..`는 불변**이다(코어 스캔 범위 밖이라 동반 스캔 위험이 없다).

## 무엇을 소유하는가

```
com.tastyhouse.external.payment/
├── PaymentModuleAutoConfiguration.java  진입점 — 챕터 02로 PaymentModuleConfig에서 리네임 + @AutoConfiguration, 자기 등록(의존 선언 = 활성화)
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

**web-api 하나뿐이다.** 결제는 사용자 앱에서만 일어나므로 admin-api·ceo-api·batch-module은 이 모듈을 의존하지 않는다(챕터 02 이후로는 의존 선언이 곧 활성화이므로 `@Import` 여부는 무관하다). 분리 전에는 세 앱 모두 토스 연동 코드를 클래스패스에 얹고 있었다.

## 진입 설정과 스캔 범위

`PaymentModuleAutoConfiguration`(챕터 02 — `@AutoConfiguration(proxyBeanMethods = false)`, `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록)이 `@ComponentScan("com.tastyhouse.external.payment")` + `@EnableConfigurationProperties(TossPaymentProperties.class)`를 갖는다. **provider 조건(`@ConditionalOnProperty`)이 없다** — PG는 파일·메일·SMS와 달리 대안 구현이 없어 선택 축 자체가 존재하지 않는다.

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
- `domain` (implementation) — 구현하는 `PgPaymentGateway` 포트와 그 반환 타입(`payment/port/dto/`), 예외 계약

### External
- `spring-boot-starter-webflux` — 토스 API 호출(`WebClient`). Jackson도 이것이 전이로 제공한다.

**`application`에 의존하지 않는다** — 이 모듈이 구현하는 계약은 도메인 포트 하나뿐이고 아웃바운드 SPI가 없다(oauth·crawling과 다른 점).

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **빈 배선 (챕터 02 개정)**: web-api만 `implementation project(':infrastructure:payment')`를 선언한다(챕터 02 — `runtimeOnly`). `PaymentModuleAutoConfiguration`이 클래스패스 존재만으로 자동 등록되므로 `@Import`는 없다. **의존 선언 자체가 활성화**이므로, 다른 앱에 실수로 의존을 추가하면 `PgPaymentGateway` 빈이 뜨는 것이 아니라(설정값이 web-api에만 있어) 오히려 그 앱에서 프로퍼티 바인딩 실패로 부팅이 깨질 수 있다.
- **결제 실패는 `ExternalApiException`으로 던진다** — 전용 예외 타입과 모듈별 `@ExceptionHandler`를 추가하지 않는다. 응답 `code`는 wire 계약이므로 기존 값을 바꾸지 않는다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.external.payment..` 봉인

**대상**: `backend/infrastructure/payment/src/main/java/com/tastyhouse/external/payment/`

외부 연동 7모듈 공통 규칙이다. `com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 `@ComponentScan("com.tastyhouse.infrastructure")`가 통째로 스캔해 **빈 스캔 범위가 어긋나 admin-api·ceo-api·batch-module의 부팅이 깨진다.** 모듈 디렉터리와 패키지 이름이 어긋나 보인다는 이유로 정리하지 않는다. 상세는 `../external/AGENTS.md`.

### 다른 앱에 이 모듈 의존을 추가하지 않는다

**대상**: `backend/infrastructure/payment/src/main/java/com/tastyhouse/external/payment/PaymentModuleAutoConfiguration.java`

결제는 사용자 앱에서만 일어나므로 web-api만 이 모듈을 의존한다. **클래스패스 존재만으로 활성화되므로, 다른 앱에 의존을 추가하면 그 앱에도 PG 빈이 올라온다**(설정값은 web-api에만 있어 프로퍼티 바인딩 실패로 부팅이 깨질 수 있다). 위 "빈 배선" 항목과 같은 사실이며, 여기서는 가드로서 다시 못박는다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 토스 승인 응답의 에러 필드는 성공 응답과 한 타입에 담긴다

**대상**: `backend/infrastructure/payment/src/main/java/com/tastyhouse/external/payment/toss/dto/TossPaymentConfirmResponse.java`

승인 응답 wire DTO 안에 **에러 응답 필드가 함께 선언돼 있다.** 토스가 성공·실패를 같은 엔드포인트에서 돌려주기 때문이며, 실패 판별과 `ExternalApiException` 번역은 `TossPaymentGatewayAdapter`가 수행한다. 이 필드들을 별도 DTO로 떼어내면 어댑터가 응답 본문을 두 번 역직렬화해야 하므로 분리 대상이 아니다.
