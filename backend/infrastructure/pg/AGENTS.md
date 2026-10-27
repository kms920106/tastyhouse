<!-- Parent: ../../AGENTS.md -->

# infrastructure:pg

결제 **PG(결제대행사) 채널 스타터**(`java-library`). 여기서 `pg`는 PostgreSQL이 아니라 **Payment Gateway**다 — 도메인 어휘 `PgProvider`·`PgPaymentGatewayPort`·`PgOrderId`와 같은 뜻이다. **자바 코드가 없다** — 의존 선언(`build.gradle`)과 설정 진입점(`application-pg.yml`) 둘뿐이며, 형태는 `infrastructure:file-storage`·`infrastructure:oauth`·`infrastructure:mail`·`infrastructure:sms`와 같다. 벤더(토스페이먼츠 등)를 `runtimeOnly`로 조립해 web-api·batch-module에 노출하고, 여러 벤더가 **동시에 공존**할 수 있도록 하는 PG 라우터 빈(`PgPaymentGatewayRouter`)의 등록은 코어 `application-core` 모듈이 맡는다. PG 호출 구현은 이 모듈이 아니라 벤더 모듈(`infrastructure:tosspayments`)에 있다. ([결정 0061](../../../docs/domain/payment.md#decision-0061))

## 왜 `payment`가 아니라 `pg`인가

- `payment`는 이미 `domain/payment`·`application/payment`·`webapi/payment`에서 **결제 도메인**을 뜻한다. 이 모듈은 결제 도메인이 아니라 PG 연동 창구다.
- 다른 채널 스타터는 연동 대상 채널 이름이다(`mail`·`sms`·`file-storage`). 결제의 채널은 PG다.

## 무엇을 소유하는가

```
backend/infrastructure/pg/
  build.gradle                                       runtimeOnly tosspayments (한 줄)
  AGENTS.md
  src/main/resources/application-pg.yml               벤더 yml 중첩 import만
```

`compileJava`는 NO-SOURCE로 넘어가고 jar에는 yml만 실린다. `META-INF/spring/...AutoConfiguration.imports`도 없다 — 등록할 빈이 없기 때문이다. `find backend/infrastructure/pg -name "*.java" -not -path "*/build/*"`는 아무것도 반환하지 않는다(자바 코드 0개).

## 이 모듈이 하는 일 — 조립과 설정 두 가지만

- **조립**: `build.gradle`의 `runtimeOnly project(':infrastructure:tosspayments')` 한 줄이 기본 벤더를 web-api·batch-module의 runtimeClasspath에 싣는다.
- **설정**: `application-pg.yml`이 벤더 yml을 중첩 import한다(아래 §yml).

빈 등록은 조립 대상(tosspayments 등)의 `@Configuration`이 `PgProviderGatewayPort` 구현체를, 코어 `application-core`의 라우터 클래스 `PgPaymentGatewayRouter`가 `@Service` 스캔으로 각각 담당한다 — 이 모듈은 어느 쪽도 하지 않는다.

## 라우터 등록은 어디인가 — `web-application`의 `PgPaymentGatewayRouter`

> 제목은 결정 0026·0034·0036이 인용하므로 남겨 둔 이름이다. 라우터는 `application-core`에 있다.

라우터는 **코어 `application-core` 모듈**의 `payment/service/PgPaymentGatewayRouter`에 있고 **`@Service`**를 단다. 코어는 4앱 모두에 실리므로 라우터 빈도 4앱 모두에 뜬다. 벤더 어댑터는 이 모듈을 의존하는 web-api·batch-module에만 있으므로, admin·ceo에서는 라우터가 **빈 목록**으로 생성되고 어떤 PG도 지원하지 않는다(admin·ceo에는 라우터를 주입하는 빈도 없다). 라우터가 다루는 포트 `PgPaymentGatewayPort`·`PgProviderGatewayPort`·`PgCancelResult`·`PgCancelFailure`·`PgCancelLookupResult`·`PgCancelLookupStatus`·`PgProviderCode`와 `PgConfirmResult`·`PgConfirmFailure`·`PgPaymentLookupResult`·`PgPaymentLookupStatus`·`TossPaymentDetail`이 모두 코어에 있다(패키지는 모두 `com.tastyhouse.application.payment.port.out`). 코어로 둔 이유는 web(승인·취소)과 batch(주기 대사) **두 앱이 쓰기 때문**이다 — 2개 앱 이상이 쓰는 계약은 코어 소유다. 빈 이름은 `pgPaymentGatewayRouter`다. ([결정 0061](../../../docs/domain/payment.md#decision-0061))

```java
// application/payment/service/PgPaymentGatewayRouter.java (application-core)
@Service
class PgPaymentGatewayRouter implements PgPaymentGatewayPort {

    public PgPaymentGatewayRouter(List<PgProviderGatewayPort> gateways) {
        // ...
    }
}
```

**이 모듈은 그 등록에 관여하지 않는다.** 벤더(tosspayments 등)를 조립해 `PgProviderGatewayPort` 구현체를 web-api·batch-module의 클래스패스에 올리는 것까지가 이 모듈의 일이다. `provider()` 반환 타입은 도메인 `PgProvider`가 아니라 `application` 소유 `PgProviderCode`이고 `.name()` 변환은 라우터가 수행한다(`../tosspayments/AGENTS.md`의 "포트 반환 타입" 절 참고).

## 다른 채널 스타터와의 관계

`pg`도 코드가 없는 스타터이므로 `file-storage`·`oauth`·`mail`·`sms`와 형태가 같다. 차이는 배타 선택 방식뿐이다: `pg`는 벤더가 배타 선택 없이 **공존**하고(`oauth`와 동형), `mail`·`sms`·`file-storage`는 `provider` 키로 벤더 하나만 배타적으로 켠다.

## 포트 2단 구조 — 벤더는 공존하고 앱은 하나만 주입한다

두 포트(`PgPaymentGatewayPort`·`PgProviderGatewayPort`)는 코어 `application-core` 소유다(web·batch가 함께 쓰는 SPI).

| 계약 (`com.tastyhouse.application.payment.port.out`) | 누가 구현하나 | 누가 주입하나 |
|---|---|---|
| `PgProviderGatewayPort` — `provider()`(반환 타입 `PgProviderCode`) + 승인·취소·조회(`lookupPayment(pgOrderId)`)·취소 확인 조회(`lookupCancellation(pgOrderId, paymentKey)`) | 벤더 어댑터(`TossPaymentGatewayAdapter` 등), 벤더마다 하나 | 라우터(`List`로) |
| `PgPaymentGatewayPort` — `supports(String pgProvider)`(도메인 `PgProvider`의 상수명) + 승인·취소·조회(첫 인자 `String pgProvider`) | **라우터 `PgPaymentGatewayRouter` 하나뿐** | web `PgPaymentConfirmService`·`PaymentCancelService`, batch `PaymentReconcileSchedulerService` |

- 벤더가 둘 이상 떠도 `PgPaymentGatewayPort` 구현은 라우터 하나라 **빈 모호성이 없다.** 벤더 어댑터가 `PgPaymentGatewayPort`를 직접 구현하게 하지 않는다 — 두 번째 벤더가 들어오는 순간 `NoUniqueBeanDefinitionException`으로 web-api가 뜨지 않는다.
- 라우터는 `application-core`의 `payment/service/PgPaymentGatewayRouter`에 있다 — 이 라우터가 다루는 계약(`PgProviderGatewayPort`·`PgConfirmResult`·`PgCancelResult` 등)이 application 계층 소유이므로 계약과 같은 계층에 둔다. 같은 `provider()`를 반환하는 벤더가 둘이면 생성자가 `IllegalStateException`으로 기동을 멈추고, 미등록 PG로 승인·취소·조회를 요청하면 `ApplicationException(ApplicationErrorCode.PG_PROVIDER_UNSUPPORTED)`를 던진다.
- 취소는 `supports`로 먼저 묻는다. 결제 완료 경로는 PG 승인(`POST /api/payments/v1/toss/confirm`)과 현장결제 완료뿐이지만, 담당 벤더가 없는 PG 이름으로 완료된 결제가 과거 데이터에 남아 있을 수 있다. 그런 결제는 PG 취소 없이 DB만 취소한다(`PaymentCancelService#doCancelPayment`).

## 벤더 추가 절차 (예: 다날)

**web-api·batch-module을 건드리지 않는다.**

1. `infrastructure:danal` 신설 — 패키지 `com.tastyhouse.infrastructure.danal`(모듈 루트 규칙 — `VendorLayerRulesTest#shouldResideInModuleRootPackage`를 함께 둔다), 어댑터가 `application` 소유 `PgProviderGatewayPort`를 구현하고 `provider()`로 새 `PgProviderCode` 상수(`DANAL`)를 반환한다. **도메인 `PgProvider`에도 같은 이름으로 상수를 추가해야 한다**(`../tosspayments/AGENTS.md`의 `EnumCodeConstantsTest`가 두 enum의 상수 집합 일치를 검증한다). 자기 `@Configuration`과 `application-danal.yml`(`pg.danal.*`)을 갖는다.
2. 이 모듈 `build.gradle`에 `runtimeOnly project(':infrastructure:danal')` 한 줄.
3. `application-pg.yml`에 `classpath:application-danal.yml` import 한 줄.
4. `.env`에 벤더 키.

**2번과 3번은 항상 한 쌍이다.** jar만 빼고 import를 남기면 import 대상 파일이 사라져 `ConfigDataResourceNotFoundException`으로 기동이 멈춘다. import만 빼고 jar를 남기면 벤더 yml 값이 없어 어댑터가 빈 설정으로 뜬다. 벤더를 제거할 때도 두 줄을 함께 지운다.

## 어느 앱이 의존하는가

**web-api와 batch-module 둘이다**(둘 다 `runtimeOnly`). web-api는 사용자 결제의 승인·취소·즉시 조회에, batch-module은 결과를 확정하지 못한 결제의 주기 대사(`PaymentReconcileScheduler`)에 쓴다. 클래스패스 존재가 곧 활성화이므로 이 모듈을 붙인 앱에는 벤더 어댑터가 올라오고, 벤더 yml의 `${TOSS_SECRET_KEY}`를 그 앱의 환경 변수(`.env`)로 채워야 한다. admin·ceo는 PG를 호출하지 않으므로 추가하지 않는다 — 라우터는 코어에 있어 거기서도 뜨지만 빈 목록으로 생성된다.

## yml — `application-pg.yml`

벤더 yml(`classpath:application-tosspayments.yml`)을 중첩 `spring.config.import`로 로딩하는 것이 전부다. **`pg.provider` 같은 배타 선택 키를 두지 않는다** — 파일 저장(`file.provider`)·메일(`mail.provider`)·SMS(`sms.provider`)와 달리 결제는 여러 벤더가 동시에 떠야 하기 때문이다. web-api·batch-module의 `application.yml`에는 `classpath:application-pg.yml` 한 줄만 있다.

## Dependencies

- `infrastructure:tosspayments` (runtimeOnly) — 토스 벤더. compileClasspath에는 없다
- `domain`·`application` 의존 없음 — 자바 코드가 없어 도메인·유스케이스 타입을 참조할 일이 없다
- `infrastructure:restclient` 의존 없음 — HTTP는 벤더 모듈이 쓴다

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- 벤더 설정은 `TossPaymentsModuleConfig`(일반 `@Configuration`)라 `--debug` 조건 리포트에 나오지 않는다. 스캔된 라우터·어댑터 빈도 리포트에 나오지 않으므로, 배선 확인은 `--logging.level.org.springframework.beans.factory.support=DEBUG`로 띄워 `Autowiring by type from bean name 'pgPaymentGatewayRouter' via factory method to bean named 'tossPaymentGatewayAdapter'` 로그를 본다. ([결정 0038](../../AGENTS.md#decision-0038))
- 결제 실패는 `BusinessException`(에러코드는 던지는 모듈의 enum — 코어 라우터가 던지는 `PG_PROVIDER_UNSUPPORTED`는 `ApplicationErrorCode`)으로 표현한다. 전용 예외 타입과 모듈별 `@ExceptionHandler`를 추가하지 않는다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 이 모듈에 코드를 넣지 않는다

**대상**: `backend/infrastructure/pg/` 전체(`src/main/java` 부재가 정상)

이 모듈은 코드 없는 스타터다(`../file-storage/AGENTS.md`·`../oauth/AGENTS.md`와 같은 판단). **`PgModuleAutoConfiguration`·`PgGatewayConfig` 같은 등록 코드를 두지 않는다** — 라우터 등록은 `application-core`의 `@Service`가 한다.

### 벤더 어댑터에 `@ConditionalOnProperty`를 붙이지 않는다

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/TossPaymentGatewayAdapter.java`

메일·SMS·파일 저장 벤더처럼 provider 조건으로 배타 선택하면 결제가 다시 "한 번에 한 PG" 구조가 된다. 벤더 선택은 조건이 아니라 이 모듈의 `build.gradle` 조립과 결제 건의 `PgProvider`로 한다.

### 라우터 등록에 `@ConditionalOnBean`을 쓰지 않는다

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/payment/service/PgPaymentGatewayRouter.java` → 클래스 애노테이션(`@Service`)

사용자 설정 사이의 등록 순서에 따라 조건이 거짓이 되어 라우터가 조용히 빠질 수 있다(`../mail/AGENTS.md`와 같은 판단). 벤더가 0개여도 빈 목록으로 라우터가 생성되고, 첫 결제에서 `PG_PROVIDER_UNSUPPORTED`로 드러난다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 진입 설정이 벤더 클래스를 참조하지 않는다, 벤더는 이 모듈을 의존하지 않는다

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/TossPaymentsModuleConfig.java`

채널이 벤더 클래스를 컴파일 참조하면 `runtimeOnly` 조립이 불가능해지므로 벤더 프로퍼티 등록(`@EnableConfigurationProperties(TossPaymentProperties.class)`)은 벤더의 `TossPaymentsModuleConfig`가 한다. 반대로 벤더가 이 모듈을 `implementation`으로 의존하면 채널 ↔ 벤더 순환이다. 벤더가 채널 값을 써야 하면 프로퍼티 키로만 읽는다.
