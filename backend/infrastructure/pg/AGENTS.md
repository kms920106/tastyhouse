<!-- Parent: ../../AGENTS.md -->

# infrastructure:pg

결제 **PG(결제대행사) 채널 스타터**(`java-library`). 여기서 `pg`는 PostgreSQL이 아니라 **Payment Gateway**다 — 도메인 어휘 `PgProvider`·`PgPaymentGatewayPort`·`PgOrderId`와 같은 뜻이다. **자바 코드가 없다** — 의존 선언(`build.gradle`)과 설정 진입점(`application-pg.yml`) 둘뿐이며, 형태는 `infrastructure:file-storage`·`infrastructure:oauth`·`infrastructure:mail`·`infrastructure:sms`와 같다. 벤더(토스페이먼츠 등)를 `runtimeOnly`로 조립해 web-api에 노출하고, 여러 벤더가 **동시에 공존**할 수 있도록 하는 PG 라우터 빈(`PgPaymentGatewayRouter`)의 등록은 지금 `application` 모듈이 맡는다. PG 호출 구현은 이 모듈이 아니라 벤더 모듈(`infrastructure:tosspayments`)에 있다.

옛 `infrastructure:payment`(토스 어댑터를 통째로 담던 모듈)를 채널·벤더로 나누며(2026-09-26) 이 이름으로 리네임됐다. 당시엔 이 모듈이 라우터 등록 코드(`PgGatewayConfig`)를 갖고 있었으나, **채널·벤더 포트 이관 프로그램("chunk 02-vendor-ports")으로 그 코드가 삭제되고 이 모듈은 코드 없는 스타터가 됐다** — 아래 "무엇이 바뀌었는가" 절 참고.

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

`compileJava`는 NO-SOURCE로 넘어가고 jar에는 yml만 실린다. `META-INF/spring/...AutoConfiguration.imports`도 없다 — 등록할 빈이 없기 때문이다.

## 무엇이 바뀌었는가 (chunk 02-vendor-ports)

**이 모듈은 과거 라우터 등록 코드를 가진 채널 모듈이었다. 그 코드는 삭제됐고, 지금은 `infrastructure:file-storage`·`infrastructure:oauth`와 같은 부류의 코드 없는 스타터다.**

| 항목 | before | after |
|---|---|---|
| `PgModuleAutoConfiguration.java` | `@AutoConfiguration` + `@ComponentScan("com.tastyhouse.external.pg")` | **삭제** |
| `config/PgGatewayConfig.java` | `@Bean PgPaymentGatewayRouter(List<PgProviderGatewayPort>)` 등록 | **삭제** — 등록 책임이 `application`으로 이동 |
| `META-INF/spring/...AutoConfiguration.imports` | 자기 등록 | **삭제** |
| `build.gradle` | `implementation project(':domain')` + `runtimeOnly tosspayments` | `runtimeOnly project(':infrastructure:tosspayments')` **한 줄만** |
| `PgPaymentGatewayRouter` | `domain/payment/service/`의 순수 POJO, `PgGatewayConfig`가 `@Bean` 등록 | **`application/payment/service/`로 이동**(더 이상 domain 소유가 아니다), 등록은 ~~`application`의 `PgRouterConfig`(`@WebApp`) 담당~~ 클래스에 붙은 `@WebApp` 마커(스캔 등록 — `PgRouterConfig`는 application `*ServiceConfig` 삭제로 없어졌다). `provider()` 반환 타입도 도메인 `PgProvider`가 아니라 `application` 소유 `PgProviderCode`로 바뀌었다(`.name()` 변환은 라우터가 수행 — `../tosspayments/AGENTS.md`의 "포트 반환 타입" 절 참고) |

**"채널 모듈이 벤더를 조립하는 쪽이라 라우터를 등록한다"던 과거 규칙(아래 §역사 참고)은 이 모듈에서는 번복됐다** — 라우터 등록이 애초에 이 모듈의 자리가 아니게 재설계됐기 때문이다. `find backend/infrastructure/pg -name "*.java" -not -path "*/build/*"`는 아무것도 반환하지 않는다(자바 코드 0개, 확인됨).

## 지금 이 모듈이 하는 일 — 조립과 설정 두 가지만

- **조립**: `build.gradle`의 `runtimeOnly project(':infrastructure:tosspayments')` 한 줄이 기본 벤더를 web-api의 runtimeClasspath에 싣는다.
- **설정**: `application-pg.yml`이 벤더 yml을 중첩 import한다(아래 §yml).

빈 등록은 이제 조립 대상(tosspayments 등)의 auto-configuration이 `PgProviderGatewayPort` 구현체를, `web-application`의 라우터 클래스 `PgPaymentGatewayRouter`가 `@Service` 스캔으로(앱 마커 제거 전에는 `application` + `@WebApp` 마커) 각각 담당한다 — 이 모듈은 어느 쪽도 하지 않는다.

## 라우터 등록은 이제 어디인가 — `application`의 `PgPaymentGatewayRouter` 클래스 마커

> **(번복됨 — 앱 마커 제거)** 아래 "클래스에 `@WebApp` 마커만 붙고(`@Service`는 달지 않는다)"는 번복됐다. 지금 라우터는 **`web-application` 모듈**의 `payment/service/PgPaymentGatewayRouter`에 있고 **`@Service`**를 단다. web-api만 `web-application`을 의존하므로 결제 기능이 없는 admin·ceo·batch에는 여전히 이 빈이 뜨지 않는다 — 한정하는 수단이 마커에서 모듈 경계로 바뀌었을 뿐이다. 라우터가 다루는 포트 `PgPaymentGatewayPort`·`PgProviderGatewayPort`·`PgCancelResult`·`PgProviderCode`도 `web-application` 소유이고, `PgConfirmResult`·`TossPaymentDetail`은 코어 `application`에 있다(패키지는 모두 `com.tastyhouse.application.payment.port.out`). 빈 이름 `pgPaymentGatewayRouter`는 그대로다. 아래 코드 블록은 그 시점 기록이다.

**(번복됨 — application `*ServiceConfig` 삭제)** 아래 첫 코드 블록(before)의 `PgRouterConfig`는 삭제됐다. 지금은 라우터 클래스 자체에 `@WebApp` 마커만 붙고(`@Service`는 달지 않는다 — "마커만 = 도메인 서비스" 컨벤션), 마커 기반 컴포넌트 스캔이 생성자에 `List<PgProviderGatewayPort>`를 주입해 등록한다. 빈 이름 `pgPaymentGatewayRouter`와 등록 앱(web)은 그대로다.

```java
// after (현행) — application/payment/service/PgPaymentGatewayRouter.java
@WebApp
public class PgPaymentGatewayRouter implements PgPaymentGatewayPort {

    public PgPaymentGatewayRouter(List<PgProviderGatewayPort> gateways) {
        // ...
    }
}
```

```java
// before (02-vendor-ports ~ ServiceConfig 삭제 전, 삭제됨)
@Configuration(proxyBeanMethods = false)
@WebApp
public class PgRouterConfig {

    @Bean
    public PgPaymentGatewayRouter pgPaymentGatewayRouter(List<PgProviderGatewayPort> gateways) {
        return new PgPaymentGatewayRouter(gateways);
    }
}
```

~~`PgPaymentGatewayRouter`는 `application/payment/service/`의 annotation-free POJO이고, `application`의 `payment/config/PgRouterConfig`(`@WebApp`)가 `List<PgProviderGatewayPort>`를 주입받아 `@Bean`으로 등록한다.~~ 지금은 라우터 클래스의 `@WebApp` 마커가 web-api에서만 스캔되므로, 결제 기능이 없는 admin·ceo·batch에는 이 빈이 뜨지 않는다.

**이 모듈은 그 등록에 관여하지 않는다.** 벤더(tosspayments 등)를 조립해 `PgProviderGatewayPort` 구현체를 web-api의 클래스패스에 올리는 것까지가 이 모듈의 일이다.

## 채널 스타터와 파일 저장 스타터의 관계 (역사 — 지금은 형태가 같다)

과거 문서는 이 모듈을 "DomainConfig 대신 라우터를 등록하는 공존형 채널 모듈"로 구분했다. 그 구분은 **소멸했다** — 지금은 `pg`도 코드가 없는 스타터이므로 `file-storage`·`oauth`·`mail`·`sms`와 형태가 같다. 남은 차이는 배타 선택 방식뿐이다: `pg`는 벤더가 배타 선택 없이 **공존**하고(`oauth`와 동형), `mail`·`sms`·`file-storage`는 `provider` 키로 벤더 하나만 배타적으로 켠다.

## 포트 2단 구조 — 벤더는 공존하고 앱은 하나만 주입한다 (지금은 둘 다 `application` 소유)

> **(앱 마커 제거 후 갱신)** 두 포트(`PgPaymentGatewayPort`·`PgProviderGatewayPort`)는 지금 `web-application` 소유다(web 전용 SPI). 아래 "`application` 소유"는 "application 계층 소유"로 읽고, 모듈은 `web-application`이다.

| 계약 (`com.tastyhouse.application.payment.port.out`) | 누가 구현하나 | 누가 주입하나 |
|---|---|---|
| `PgProviderGatewayPort` — `provider()`(반환 타입 `PgProviderCode`) + 승인·취소 | 벤더 어댑터(`TossPaymentGatewayAdapter` 등), 벤더마다 하나 | 라우터(`List`로) |
| `PgPaymentGatewayPort` — `supports(PgProvider)`(도메인 `PgProvider`) + 승인·취소(첫 인자 `PgProvider`) | **라우터 `PgPaymentGatewayRouter` 하나뿐** | application `PgPaymentConfirmService`·`PaymentCancelService` |

- 벤더가 둘 이상 떠도 `PgPaymentGatewayPort` 구현은 라우터 하나라 **빈 모호성이 없다.** 벤더 어댑터가 `PgPaymentGatewayPort`를 직접 구현하게 되돌리지 않는다 — 두 번째 벤더가 들어오는 순간 `NoUniqueBeanDefinitionException`으로 web-api가 뜨지 않는다.
- 라우터는 Spring을 모르는 순수 POJO이고 `application/payment/service/PgPaymentGatewayRouter`에 있다(과거엔 `domain`에 있었으나 chunk 02-vendor-ports로 `application`으로 이동했다 — `PgProviderGatewayPort`·`PgConfirmResult`·`PgCancelResult` 등 이 라우터가 다루는 계약 자체가 `application` 소유이므로, 계약과 같은 모듈에 두는 것이 자연스럽다). ~~`application`의 `PgRouterConfig`가 `@Bean`으로 등록한다.~~ 지금은 클래스에 `@WebApp` 마커만 달아 스캔으로 등록된다(Spring 애노테이션이 아니라 프로젝트 마커라 "Spring을 모르는" 성질은 유지된다). 같은 `provider()`를 반환하는 벤더가 둘이면 생성자가 `IllegalStateException`으로 기동을 멈추고, 미등록 PG로 승인·취소를 요청하면 `BusinessException(ErrorCode.PG_PROVIDER_UNSUPPORTED)`를 던진다.
- 취소는 `supports`로 먼저 묻는다. PG 콜백 경로(`POST /api/payments/v1/confirm`)는 요청 본문의 아무 PG명으로나 결제를 완료시킬 수 있어서, 담당 벤더가 없는 PG의 완료 결제가 존재한다. 그런 결제는 지금처럼 PG 취소 없이 DB만 취소한다(`PaymentCancelService#doCancelPayment`). 이 정책이 옳은지는 콜백 엔드포인트 정리와 함께 판단할 후속 항목이다.

## 벤더 추가 절차 (예: 다날)

**web-api를 건드리지 않는다.**

1. `infrastructure:danal` 신설 — 패키지 `com.tastyhouse.infrastructure.danal`(모듈 루트 규칙 — `VendorLayerRulesTest#shouldResideInModuleRootPackage`를 함께 둔다), 어댑터가 `application` 소유 `PgProviderGatewayPort`를 구현하고 `provider()`로 새 `PgProviderCode` 상수(`DANAL`)를 반환한다. **도메인 `PgProvider`에도 같은 이름으로 상수를 추가해야 한다**(`../tosspayments/AGENTS.md`의 `EnumCodeConstantsTest`가 두 enum의 상수 집합 일치를 검증한다). 자기 auto-configuration과 `application-danal.yml`(`pg.danal.*`)을 갖는다.
2. 이 모듈 `build.gradle`에 `runtimeOnly project(':infrastructure:danal')` 한 줄.
3. `application-pg.yml`에 `classpath:application-danal.yml` import 한 줄.
4. `.env`에 벤더 키.

**2번과 3번은 항상 한 쌍이다.** jar만 빼고 import를 남기면 import 대상 파일이 사라져 `ConfigDataResourceNotFoundException`으로 기동이 멈춘다. import만 빼고 jar를 남기면 벤더 yml 값이 없어 어댑터가 빈 설정으로 뜬다. 벤더를 제거할 때도 두 줄을 함께 지운다.

## 어느 앱이 의존하는가

**web-api 하나뿐이다**(`runtimeOnly`). 클래스패스 존재가 곧 활성화이므로 다른 앱에 이 모듈을 추가하면 벤더 어댑터가 그 앱에도 올라온다(라우터 등록은 `web-application` 모듈에 있는 `PgPaymentGatewayRouter`가 이미 web-api에 한정하지만 — 앱 마커 제거 전에는 `@WebApp` 마커가 한정했다 — 벤더 어댑터 자체는 이 모듈의 조립을 따라간다). 결제는 사용자 앱에서만 일어나므로 추가하지 않는다.

## yml — `application-pg.yml`

벤더 yml(`classpath:application-tosspayments.yml`)을 중첩 `spring.config.import`로 로딩하는 것이 전부다. **`pg.provider` 같은 배타 선택 키를 두지 않는다** — 파일 저장(`file.provider`)·메일(`mail.provider`)·SMS(`sms.provider`)와 달리 결제는 여러 벤더가 동시에 떠야 하기 때문이다. web-api `application.yml`에는 `classpath:application-pg.yml` 한 줄만 있다.

## Dependencies

- `infrastructure:tosspayments` (runtimeOnly) — 토스 벤더. compileClasspath에는 없다
- `domain`·`application` 의존 없음 — 자바 코드가 없어 도메인·유스케이스 타입을 참조할 일이 없다(과거엔 `PgGatewayConfig`가 요구하는 타입들 때문에 `domain`을 `implementation`으로 의존했으나, 그 설정 클래스가 `application`으로 이동하며 이 모듈의 의존도 사라졌다)
- `infrastructure:restclient` 의존 없음 — HTTP는 벤더 모듈이 쓴다

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- ~~벤더 auto-configuration(`TossPaymentsModuleAutoConfiguration`)은 조건이 없어 `--debug` 리포트의 "Unconditional classes" 절에 나온다.~~ **(번복됨 — imports 제거)** 벤더 설정은 이제 `TossPaymentsModuleConfig`(일반 `@Configuration`)라 `--debug` 조건 리포트에 나오지 않는다. 스캔된 라우터·어댑터 빈은 리포트에 나오지 않으므로, 배선 확인은 `--logging.level.org.springframework.beans.factory.support=DEBUG`로 띄워 `Autowiring by type from bean name 'pgPaymentGatewayRouter' via factory method to bean named 'tossPaymentGatewayAdapter'` 로그를 본다.
- 결제 실패는 `BusinessException`(에러코드는 던지는 모듈의 enum — 단일 `ErrorCode`는 삭제됨, `PG_PROVIDER_UNSUPPORTED` 등은 `WebErrorCode`)으로 표현한다. 전용 예외 타입과 모듈별 `@ExceptionHandler`를 추가하지 않는다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 이 모듈에 코드를 넣지 않는다 (번복됨 — 과거엔 라우터 등록 코드를 가진 채널 모듈이었다)

**대상**: `backend/infrastructure/pg/` 전체(`src/main/java` 부재가 정상)

이 모듈은 원래 `PgGatewayConfig`(라우터 등록)를 가진 채널 모듈이었으나(chunk 02-vendor-ports 이전), 라우터 등록이 `application`(당시 `PgRouterConfig`, 이후 라우터 클래스의 `@WebApp` 마커, 앱 마커 제거 후에는 `web-application`의 `@Service`)으로 이관되며 코드 없는 스타터가 됐다(`../file-storage/AGENTS.md`·`../oauth/AGENTS.md`와 같은 판단). **`PgModuleAutoConfiguration`·`PgGatewayConfig`를 되살리지 않는다** — 라우터 등록은 `application`의 일이다.

### 벤더 어댑터에 `@ConditionalOnProperty`를 붙이지 않는다

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/TossPaymentGatewayAdapter.java`

메일·SMS·파일 저장 벤더처럼 provider 조건으로 배타 선택하면 결제가 다시 "한 번에 한 PG" 구조가 된다. 벤더 선택은 조건이 아니라 이 모듈의 `build.gradle` 조립과 결제 건의 `PgProvider`로 한다.

### 라우터 등록에 `@ConditionalOnBean`을 쓰지 않는다

**대상**: `backend/web-application/src/main/java/com/tastyhouse/application/payment/service/PgPaymentGatewayRouter.java` → 클래스 애노테이션(`@Service`). (앱 마커 제거로 앵커를 옮겼다 — 과거 `backend/application/src/main/java/com/tastyhouse/application/payment/service/PgPaymentGatewayRouter.java` → `@WebApp`) ~~`backend/application/src/main/java/com/tastyhouse/application/payment/config/PgRouterConfig.java` → `pgPaymentGatewayRouter`~~ (번복됨 — application `*ServiceConfig` 삭제로 이 파일이 없어져 앵커를 옮겼다)

사용자 설정 사이의 등록 순서에 따라 조건이 거짓이 되어 라우터가 조용히 빠질 수 있다(`../mail/AGENTS.md`와 같은 판단). 벤더가 0개여도 빈 목록으로 라우터가 생성되고, 첫 결제에서 `PG_PROVIDER_UNSUPPORTED`로 드러난다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 진입 설정이 벤더 클래스를 참조하지 않는다, 벤더는 이 모듈을 의존하지 않는다

**대상**: `backend/infrastructure/tosspayments/src/main/java/com/tastyhouse/infrastructure/tosspayments/TossPaymentsModuleConfig.java`(imports 제거 전 `TossPaymentsModuleAutoConfiguration.java`)

옛 `PaymentModuleAutoConfiguration`은 `@EnableConfigurationProperties(TossPaymentProperties.class)`를 들고 있었다. 채널이 벤더 클래스를 컴파일 참조하면 `runtimeOnly` 조립이 불가능해지므로 그 등록은 벤더의 `TossPaymentsModuleAutoConfiguration`(imports 제거 후 `TossPaymentsModuleConfig`)으로 옮겼다. 반대로 벤더가 이 모듈을 `implementation`으로 의존하면 채널 ↔ 벤더 순환이다. 벤더가 채널 값을 써야 하면 프로퍼티 키로만 읽는다.
