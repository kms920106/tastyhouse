<!-- Parent: ../../AGENTS.md -->

# infrastructure:pg

결제 **PG(결제대행사) 채널 모듈**(`java-library`). 여기서 `pg`는 PostgreSQL이 아니라 **Payment Gateway**다 — 도메인 어휘 `PgProvider`·`PgPaymentGateway`·`PgOrderId`와 같은 뜻이다. 벤더(토스페이먼츠 등)를 `runtimeOnly`로 조립하고, 여러 벤더가 **동시에 공존**할 수 있도록 PG 라우터 빈(`PgPaymentGatewayRouter`)을 등록한다. PG 호출 구현은 이 모듈이 아니라 벤더 모듈(`infrastructure:tosspayments`)에 있다.

옛 `infrastructure:payment`(토스 어댑터를 통째로 담던 모듈)를 채널·벤더로 나누며(2026-09-26) 이 이름으로 리네임됐다. 형태는 `infrastructure:mail`·`infrastructure:sms` 채널 모듈과 같고, 다른 점은 등록하는 것이 DomainConfig가 아니라 **라우터**라는 것이다.

## 왜 `payment`가 아니라 `pg`인가

- `payment`는 이미 `domain/payment`·`application/payment`·`webapi/payment`에서 **결제 도메인**을 뜻한다. 이 모듈은 결제 도메인이 아니라 PG 연동 창구다.
- 다른 채널 모듈은 연동 대상 채널 이름이다(`mail`·`sms`·`file-storage`). 결제의 채널은 PG다.

## 무엇을 소유하는가

```
backend/infrastructure/pg/
  build.gradle                                       implementation domain + runtimeOnly tosspayments
  src/main/java/com/tastyhouse/external/pg/
    PgModuleAutoConfiguration.java                   @AutoConfiguration + @ComponentScan("com.tastyhouse.external.pg")
    config/PgGatewayConfig.java                      @Bean PgPaymentGatewayRouter(List<PgProviderGateway>)
  src/main/resources/
    application-pg.yml                               벤더 yml 중첩 import만
    META-INF/spring/...AutoConfiguration.imports     자기 등록
```

## 포트 2단 구조 — 벤더는 공존하고 앱은 하나만 주입한다

| 계약 (`domain/payment/port/`) | 누가 구현하나 | 누가 주입하나 |
|---|---|---|
| `PgProviderGateway` — `provider()` + 승인·취소 | 벤더 어댑터(`TossPaymentGatewayAdapter` 등), 벤더마다 하나 | 라우터(`List`로) |
| `PgPaymentGateway` — `supports(PgProvider)` + 승인·취소(첫 인자 `PgProvider`) | **라우터 `PgPaymentGatewayRouter` 하나뿐** | application `PaymentCommandService` |

- 벤더가 둘 이상 떠도 `PgPaymentGateway` 구현은 라우터 하나라 **빈 모호성이 없다.** 벤더 어댑터가 `PgPaymentGateway`를 직접 구현하게 되돌리지 않는다 — 두 번째 벤더가 들어오는 순간 `NoUniqueBeanDefinitionException`으로 web-api가 뜨지 않는다.
- 라우터는 Spring을 모르는 순수 POJO라 `domain`(`payment/service/PgPaymentGatewayRouter`)에 있고, 이 모듈이 `@Bean`으로 등록한다. 같은 `provider()`를 반환하는 벤더가 둘이면 생성자가 `IllegalStateException`으로 기동을 멈추고, 미등록 PG로 승인·취소를 요청하면 `BusinessException(ErrorCode.PG_PROVIDER_UNSUPPORTED)`를 던진다.
- 취소는 `supports`로 먼저 묻는다. PG 콜백 경로(`POST /api/payments/v1/confirm`)는 요청 본문의 아무 PG명으로나 결제를 완료시킬 수 있어서, 담당 벤더가 없는 PG의 완료 결제가 존재한다. 그런 결제는 지금처럼 PG 취소 없이 DB만 취소한다(`PaymentCommandService#doCancelPayment`). 이 정책이 옳은지는 콜백 엔드포인트 정리와 함께 판단할 후속 항목이다.

## 벤더 추가 절차 (예: 다날)

**web-api를 건드리지 않는다.**

1. `infrastructure:danal` 신설 — 패키지 `com.tastyhouse.external.danal`, 어댑터가 `PgProviderGateway`를 구현하고 `provider()`로 새 `PgProvider` 상수(`DANAL`)를 반환한다. 자기 auto-configuration과 `application-danal.yml`(`pg.danal.*`)을 갖는다.
2. 이 모듈 `build.gradle`에 `runtimeOnly project(':infrastructure:danal')` 한 줄.
3. `application-pg.yml`에 `classpath:application-danal.yml` import 한 줄.
4. `.env`에 벤더 키.

**2번과 3번은 항상 한 쌍이다.** jar만 빼고 import를 남기면 import 대상 파일이 사라져 `ConfigDataResourceNotFoundException`으로 기동이 멈춘다. import만 빼고 jar를 남기면 벤더 yml 값이 없어 어댑터가 빈 설정으로 뜬다. 벤더를 제거할 때도 두 줄을 함께 지운다.

## 어느 앱이 의존하는가

**web-api 하나뿐이다**(`runtimeOnly`). 클래스패스 존재가 곧 활성화이므로 다른 앱에 이 모듈을 추가하면 벤더 어댑터와 라우터가 그 앱에도 올라온다. 결제는 사용자 앱에서만 일어나므로 추가하지 않는다.

## yml — `application-pg.yml`

벤더 yml(`classpath:application-tosspayments.yml`)을 중첩 `spring.config.import`로 로딩하는 것이 전부다. **`pg.provider` 같은 배타 선택 키를 두지 않는다** — 파일 저장(`file.provider`)·메일(`mail.provider`)·SMS(`sms.provider`)와 달리 결제는 여러 벤더가 동시에 떠야 하기 때문이다. web-api `application.yml`에는 `classpath:application-pg.yml` 한 줄만 있다.

## Dependencies

- `domain` (implementation) — `PgGatewayConfig`가 등록하는 `PgPaymentGatewayRouter`와 그 입력 `PgProviderGateway`
- `infrastructure:tosspayments` (runtimeOnly) — 토스 벤더. compileClasspath에는 없다
- `infrastructure:restclient` 의존 없음 — HTTP는 벤더 모듈이 쓴다

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- `PgModuleAutoConfiguration`·`TossPaymentsModuleAutoConfiguration`은 조건이 없어 `--debug` 리포트의 "Unconditional classes" 절에 나온다. 스캔된 라우터·어댑터 빈은 리포트에 나오지 않으므로, 배선 확인은 `--logging.level.org.springframework.beans.factory.support=DEBUG`로 띄워 `Autowiring by type from bean name 'pgPaymentGatewayRouter' via factory method to bean named 'tossPaymentGatewayAdapter'` 로그를 본다.
- 결제 실패는 도메인 `BusinessException(ErrorCode.X)`으로 표현한다. 전용 예외 타입과 모듈별 `@ExceptionHandler`를 추가하지 않는다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 벤더 어댑터에 `@ConditionalOnProperty`를 붙이지 않는다

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/external/tosspayments/TossPaymentGatewayAdapter.java`

메일·SMS·파일 저장 벤더처럼 provider 조건으로 배타 선택하면 결제가 다시 "한 번에 한 PG" 구조가 된다. 벤더 선택은 조건이 아니라 이 모듈의 `build.gradle` 조립과 결제 건의 `PgProvider`로 한다.

### 라우터 등록에 `@ConditionalOnBean`을 쓰지 않는다

**대상**: `backend/infrastructure/pg/src/main/java/com/tastyhouse/external/pg/config/PgGatewayConfig.java` → `pgPaymentGatewayRouter`

사용자 설정 사이의 등록 순서에 따라 조건이 거짓이 되어 라우터가 조용히 빠질 수 있다(`../mail/AGENTS.md`와 같은 판단). 벤더가 0개여도 빈 목록으로 라우터가 생성되고, 첫 결제에서 `PG_PROVIDER_UNSUPPORTED`로 드러난다.

### 진입 설정은 자기 패키지만 스캔한다, 벤더는 형제 패키지에 둔다

**대상**: `backend/infrastructure/pg/src/main/java/com/tastyhouse/external/pg/PgModuleAutoConfiguration.java`

스캔 범위는 `com.tastyhouse.external.pg`다. 벤더를 이 하위(`external.pg.toss`)에 두면 이 스캔에 동반 등록되어 벤더 선택이 `build.gradle` 조립이 아니라 채널 모듈 존재로 결정된다. 벤더는 `external.tosspayments`처럼 형제 패키지에 둔다. 패키지 루트를 `com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 통째 스캔에 걸려 admin·ceo·batch 부팅이 깨진다(`../restclient/AGENTS.md`).

### 진입 설정이 벤더 클래스를 참조하지 않는다, 벤더는 이 모듈을 의존하지 않는다

**대상**: `backend/infrastructure/pg/src/main/java/com/tastyhouse/external/pg/PgModuleAutoConfiguration.java`

옛 `PaymentModuleAutoConfiguration`은 `@EnableConfigurationProperties(TossPaymentProperties.class)`를 들고 있었다. 채널이 벤더 클래스를 컴파일 참조하면 `runtimeOnly` 조립이 불가능해지므로 그 등록은 벤더의 `TossPaymentsModuleAutoConfiguration`으로 옮겼다. 반대로 벤더가 이 모듈을 `implementation`으로 의존하면 채널 ↔ 벤더 순환이다. 벤더가 채널 값을 써야 하면 프로퍼티 키로만 읽는다.
