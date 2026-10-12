# ADR-0034: application *ServiceConfig를 지우고 도메인 서비스 클래스에 앱 마커만 단다

- 상태: 대체됨(→ [ADR-0036](0036-app-marker-removal.md))
- 결정 커밋: `251c74c06` (2026-10-02)
- 대체한 결정: [ADR-0026](0026-vendor-ports-to-application.md), [ADR-0027](0027-domain-ports-to-application.md)

## 맥락

덩어리 02([ADR-0026](0026-vendor-ports-to-application.md))와 03a([ADR-0027](0027-domain-ports-to-application.md))는 포트를 주입받는 도메인 서비스를 domain에서 application으로 옮겼습니다. 등록 방식은 **"마커 없는 POJO + 마커 붙은 `@Configuration`의 `@Bean`"**이었습니다. 서비스 클래스에는 애노테이션이 없고, `<ctx>/config/<Ctx>ServiceConfig`가 `new`로 만들어 등록했습니다.

- **앱 마커**: 당시 application 한 모듈 안에서 클래스의 앱 소속을 나타내던 애노테이션 `@WebApp`/`@AdminApp`/`@CeoApp`/`@BatchApp`/`@SharedApp`입니다. 각 앱은 자기 마커와 `@SharedApp`만 골라 스캔했습니다([ADR-0014](0014-package-flattening-app-markers.md)).
- **도메인 서비스**: `<ctx>/service/`에 있으면서 `*CommandService`/`*QueryService`가 아닌 서비스입니다. 트랜잭션을 열지 않고 여러 애그리거트·포트를 묶어 도메인 불변식을 지킵니다.

이 방식에는 문제가 두 가지 있었습니다.

- 서비스 하나를 추가할 때 클래스와 config의 `@Bean`, 두 곳을 고쳐야 했습니다.
- `@SharedApp` config가 등록했기 때문에 **한 앱만 쓰는 빈도 4앱 전부에 떴습니다.** web·admin·ceo·batch 어디에도 주입처가 없는 빈이 컨텍스트에 상주했습니다.

## 이전 규칙

### backend/application-core/AGENTS.md — 덩어리 02/03a

- `file`·`payment`는 `@SharedApp`로, `mail`·`sms`는 `@WebApp`으로 등록했습니다. `PaymentConfirmationService`는 PG 결제 승인이 web에서만 일어나지만, 리스너 배치 원칙처럼 **"언젠가 다른 앱이 같은 유스케이스를 트리거해도 안전해야 한다"**는 이유로 `@SharedApp`을 유지했습니다.
- 등록 클래스는 `FileServiceConfig`(`@SharedApp`), `MailServiceConfig`·`SmsServiceConfig`(`@WebApp`), `PaymentServiceConfig`(`@SharedApp`), `PgRouterConfig`(`@WebApp`)였습니다.
- 03a는 같은 방식을 나머지 전부에 반복했습니다. persistence의 `<Ctx>DomainConfig` 18개를 `<Ctx>ServiceConfig`(`@Configuration(proxyBeanMethods = false) @SharedApp`)로 옮겼고, `@Bean` 메서드 이름은 그대로 뒀습니다. 이벤트 발행기는 `SharedEventConfig`(`@SharedApp`)의 `@Bean domainEventPublisher`였습니다.
- 02의 마커 배정은 "발송 포트 구현이 web에만 있다"는 기존 배치 기준(당시 `persistence/AGENTS.md`, 지금 `backend/infrastructure/jpa/AGENTS.md`의 "구현이 일부 앱에만 있는가" 판정)을 승계했습니다. `file`은 4앱 전부가 `FileStoragePort` 구현(firebase/aws-s3)을 가지므로 공유였고, `PgPaymentGatewayRouter`만 web 전용 채널(`infrastructure:pg`가 web-api에만 조립)이라 `@WebApp`인 `PgRouterConfig`가 따로 등록했습니다.
- 다섯 설정의 `@Bean`은 `fileUploadService`·`mailVerificationService`·`smsVerificationService`·`paymentConfirmationService`·`pgPaymentGatewayRouter`(`List<PgProviderGatewayPort>`를 주입받아 조립)였습니다. 이 다섯이 `@SharedApp`/마커 설정의 "리스너 외 첫 사용처"였고, `LayerRulesTest#sharedConfigsShouldOnlyDeclareUnmarkedBeans`가 처음으로 실제 대상(`file`·`payment`의 두 `@SharedApp` 설정)을 가졌습니다.
- 02로 `FileDomainConfig`·`PaymentDomainConfig`의 관련 빈은 `infrastructure:persistence`에서 삭제됐고, `PaymentDomainConfig`에는 `paymentCancellationService` 하나만 남았습니다. 03a로 그것도 서비스와 함께 와 `PaymentServiceConfig`에 합쳐져 persistence의 `*DomainConfig`는 0개가 됐습니다. 판정 기준은 "외부 연동 포트인가"가 아니라 "이 서비스가 지금 어디 있는가"였습니다.
- 03a의 18개 이관은 17개를 `git mv` + 개명, `PaymentDomainConfig`분을 기존 `PaymentServiceConfig`에 병합한 것이었습니다. `@Bean` 메서드 이름을 유지한 이유는 빈 이름이 바뀌면 이름 기반 주입이 깨지기 때문이었습니다. 예외는 발행기 하나로, `@Component` 기본 이름 `springDomainEventPublisher`가 `@Bean domainEventPublisher`로 바뀌었고 그 이름을 `@Qualifier`로 참조하는 곳은 0건이었습니다. domain에 남은 순수 서비스(`CupDepositPolicy`·`ProductExposureCalculator`·`ShopDeliveryTipCalculator` 등)의 `@Bean`도 이 설정들이 가졌습니다(클래스는 domain, 등록은 application).
- `SpringDomainEventPublisher`는 persistence `shared/event/`의 마커 없는 `@Component`였는데, 03a로 `@Component`를 떼고 `SharedEventConfig`의 `@Bean`이 됐습니다. persistence에 두면 persistence가 `port.out` 밖의 application 타입을 봐야 해 `shouldNotDependOnApiModules`에 걸리고, application 설정이 persistence 구현을 `new`할 수는 없었기 때문입니다.
- 서비스 연결 어댑터 `ShopRequestIndexSyncAdapter`·`ReplyPhraseProhibitedWordValidatorAdapter`는 `shop/service/` POJO로 옮겨 `ShopServiceConfig`가 같은 빈 이름으로 `@Bean` 등록했습니다. 금칙어 캐시 데코레이터 `CachingProhibitedWordPersistencePort`(현재 `CachingProhibitedWordLoadPort`)는 빈이 아니고 `ShopServiceConfig#prohibitedWordValidator`가 `new`로 감쌌습니다. 03b의 `StorePriceVerificationAdapter`도 `shop/config/ShopServiceConfig#storePriceVerificationAdapter`가 `@Bean`으로 등록했습니다(이 결정 후 `@SharedApp` 마커만 단 도메인 서비스). `PaymentConfirmationService`는 `PaymentServiceConfig`가 `@SharedApp`으로 등록했습니다.
- persistence domain 재허용 이후 `<Ctx>ServiceConfig`에는 Store가 아닌 빈(도메인 서비스·어댑터·정책 record)만 남아 있었습니다. 이 결정으로 Store 빈만 갖던 `Admin`·`Banner`·`Event`·`Notice`·`Partnership`·`Region`의 `*ServiceConfig`는 파일째 삭제됐고, `@WebApp`이던 `MailServiceConfig`·`SmsServiceConfig`가 등록하던 `MailVerificationPersistencePort`·`SmsVerificationPersistencePort` 구현은 `@Repository` 스캔으로 전 앱에 뜨게 됐습니다(03b 이전과 같은 상태이고 admin·ceo·batch에는 주입처가 없습니다).
- persistence에서 옮겨 온 설계 근거 절들은 대상이 `<Ctx>ServiceConfig`(구 `<Ctx>DomainConfig`)였습니다. 삭제 후에는 "같은 이름의 서비스 클래스"(`@Bean` 메서드명 = 클래스명 첫 글자 소문자 = 지금의 빈 이름)로 읽습니다. `ShopServiceConfig`의 `prohibitedWordValidator`·`shopNextOpenTimeCalculator`와 `ProductServiceConfig`의 `cupDepositPolicy`는 `SharedBeanConfig`의 `@Bean`으로 옮겼습니다(`cupDepositPolicy`는 여전히 단일 빈이라 "같은 인스턴스" 근거가 유효합니다).

### backend/application-core/AGENTS.md — `ServiceContextBoundaryTest` (03a 시점 검사 대상)

- 검사 대상은 `com.tastyhouse.application.<ctx>..service..`의 최상위 클래스 중 `@Service`/`@Component`/`@Configuration`이 없고 이름이 `*CommandService`/`*QueryService`로 끝나지 않는 것이었습니다. 즉 스테레오타입 없는 도메인 서비스와 그 협력 record·유틸이었고, 하한은 `domainServicesShouldExist` ≥ 71이었습니다. 이 결정 후에도 마커는 스테레오타입이 아니어서 대상은 그대로였습니다(봉인 15, 순환 봉인 1 불변).

### backend/CLAUDE.md — 공존형 채널 — PG 라우터

- 라우터 `PgPaymentGatewayRouter`는 `application/payment/service/`의 마커 없는 POJO였습니다. `application/payment/config/PgRouterConfig`(`@WebApp`)가 `List<PgProviderGatewayPort>`를 주입받아 `@Bean`으로 조립했습니다.

### backend/infrastructure/{pg,mail,sms}/AGENTS.md

- `MailVerificationService`·`SmsVerificationService`는 annotation-free POJO였습니다. `application`의 `MailServiceConfig`·`SmsServiceConfig`(`@WebApp`)가 `@Bean`으로 등록했으므로, 발송 기능이 없는 admin·ceo·batch에는 이 빈이 뜨지 않았습니다.
- `MailVerificationService`는 생성자로 `MailSenderPort`·`MemberLoadPort`·`MailVerificationLoadPort`·`MailVerificationSavePort`·`DomainEventPublisher`를, `SmsVerificationService`는 `SmsSenderPort`·`SmsVerificationLoadPort`·`SmsVerificationSavePort`·`DomainEventPublisher`를 받았습니다. 과거 채널 모듈이 지키던 "포트 구현이 web에만 있다"는 제약이 이때 `application` 쪽 마커로 표현됐습니다.
- 이 결정 후 세 클래스(`application/mail/service/MailVerificationService`·`application/sms/service/SmsVerificationService`·`application/payment/service/PgPaymentGatewayRouter`)는 `@WebApp` 마커만(`@Service` 없이) 달고 web-api의 마커 스캔으로 등록됐습니다. 빈 이름(`mailVerificationService`·`smsVerificationService`·`pgPaymentGatewayRouter`)과 등록 앱(web)은 그대로였고, 발송·결제 포트를 받는 마커 클래스가 `@WebApp`이 아니면 `AppIsolationTest#appRestrictedPortDependentsShouldBelongToThatApp`이 빌드를 실패시켰습니다. 채널 모듈은 이때도 등록에 관여하지 않고 벤더 구현을 web-api 클래스패스에 올리는 일만 했습니다.
- `sms/AGENTS.md`의 "인증 발급이 발송까지 원자적인 이유"는 등록 위치 이력을 `SmsDomainConfig`(`infrastructure:sms`) → `SmsServiceConfig`(02) → 클래스의 `@WebApp` 마커(이 결정)로 적었고, 설계 근거(같은 번호의 기존 미완료 인증 만료, 발송 누락 방지를 위한 `SmsSenderPort` 직접 주입)는 바뀌지 않았다고 기록했습니다.

### CLAUDE.md — 리포지토리 구조 (infrastructure·외부 연동 문단)

- 02 이후 서술은 "`pg`·`mail`·`sms`의 등록은 `application`의 마커 붙은 `@Configuration`(`PgRouterConfig`·`MailServiceConfig`·`SmsServiceConfig`, 전부 `@WebApp`)이 맡는다"였고, 이 결정 후에는 "세 서비스 클래스에 `@WebApp` 마커만(`@Service` 없이) 붙어 마커 스캔으로 web에만 등록된다(상세는 `backend/CLAUDE.md`의 앱 마커 규칙 절)"로 바뀌었습니다. 이후 ADR-0036으로 다시 `web-application`의 `@Service`가 됐습니다.

### backend/AGENTS.md — Subdirectories / Module Dependency Graph

- `infrastructure/jpa/` 행은 도메인 서비스 빈 등록과 `SpringDomainEventPublisher`가 03a로 `application`의 `<ctx>/config/<Ctx>ServiceConfig`·`shared/event`로 이동했다고 적었고, 이 결정 후에는 "서비스 클래스의 앱 마커 + `shared/config/SharedBeanConfig`"로 고쳐 적었습니다.
- `infrastructure/pg/`·`mail/`·`sms/` 행과 그래프 아래 목록은 `PgGatewayConfig`/`MailDomainConfig`/`SmsDomainConfig`가 02/03a로 삭제되고, 그 이관처였던 `PgRouterConfig`·`MailServiceConfig`·`SmsServiceConfig`도 이 결정으로 없어졌다고 기록했습니다.

### backend/AGENTS.md — Purpose

- 도메인 서비스는 순수 POJO이고, 빈 등록은 컨텍스트별 `<Ctx>DomainConfig`(이후 `<Ctx>ServiceConfig`)가 담당한다고 적었습니다.

## 결정

`<ctx>/config/*ServiceConfig` 22개와 `payment/config/PgRouterConfig`를 전부 삭제합니다. 그 설정들이 만들던 도메인 서비스 78개는 **클래스에 앱 마커 하나만** 달고(`@Service`는 달지 않음) 마커 기반 컴포넌트 스캔으로 등록합니다.

```java
// before — application/bug/config/BugServiceConfig.java (삭제됨)
@Configuration(proxyBeanMethods = false)
@SharedApp
public class BugServiceConfig {
    @Bean
    public BugReportRegistrationService bugReportRegistrationService(
        BugReportPersistencePort bugReportPersistencePort,
        BugReportImagePersistencePort bugReportImagePersistencePort
    ) {
        return new BugReportRegistrationService(bugReportPersistencePort, bugReportImagePersistencePort);
    }
}
```

```java
// after — config 없음
@WebApp
public class BugReportRegistrationService { ... }
```

| 항목 | before | after |
|---|---|---|
| 등록 config | `*ServiceConfig` 22개 + `PgRouterConfig` + `SharedEventConfig`(`@Bean` 88개) | `shared/config/SharedBeanConfig` 1개(`@Bean` 10개, `SharedEventConfig`를 리네임) |
| 도메인 서비스 78개 | 마커 없는 POJO, config가 `new` | 클래스에 앱 마커만, 스캔 등록. 빈 이름 = 옛 `@Bean` 메서드명(실측 전부 일치) |
| 마커 분포 | `@SharedApp` config 20개 + `SharedEventConfig`, `@WebApp` config 3개 | `@WebApp` 17 · `@AdminApp` 3 · `@CeoApp` 21 · `@BatchApp` 2 · `@SharedApp` 35 |
| 마커 배정 기준 | "언젠가 다른 앱이 트리거해도 안전하도록" 미리 `@SharedApp` | **현재 소비 앱 집합** — 한 앱이면 그 앱 마커, 둘 이상이거나 `@SharedApp` 빈이 쓰면 `@SharedApp` |
| 한 앱 전용 빈 | 4앱 전부에 뜸 | 소비 앱에서만 뜸 |
| 애노테이션을 달 수 없는 빈 | 각 config | `SharedBeanConfig`에 잔류 — `domainEventPublisher`, domain 계산기·정책 7개, `shopDeliveryTipRangePolicy`, `prohibitedWordValidator` |

**마커 배정의 고정점**: `@SharedApp` 빈이 의존하는 빈은 전부 `@SharedApp`이거나 `SharedBeanConfig`의 마커 없는 빈이어야 합니다. 이 제약을 반복 적용해 고정점까지 올립니다. 예를 들어 `ReviewBlindRequestService`가 Shared이면 `ReviewLifecycleService`도 Shared입니다. `PaymentConfirmationService`는 `@SharedApp` 리스너 `PaymentEventListener`가 쓰므로 `@SharedApp`으로 남았고, `PaymentCancellationService`는 소비자가 web뿐이라 `@WebApp`으로 좁혀졌습니다.

ArchUnit 규칙도 바꿨습니다.

| 규칙 | before | after |
|---|---|---|
| `LayerRulesTest#sharedAppOnlyOnListeners` | 리스너 ∨ `..config..`의 `@Configuration` | 위 두 경우 ∨ 마커-only 도메인 서비스 |
| `LayerRulesTest#markerOnlyClassesShouldBeDomainServices` | 없음 | 신설 — 마커-only 클래스는 도메인 서비스 위치에만 둔다(하한 78) |
| `AppIsolationTest#appsShouldNotDependOnEachOther` | 마커 조합 전부 금지 | 앱 마커 → `@SharedApp` `..service..`만 허용(공유 커널 단방향) |
| `AppIsolationTest#constructorDependenciesShouldBeVisibleToApp` | 없음 | 신설 — 생성자 의존이 같은 앱 또는 `@SharedApp`에서 보이는지 검사(마커 누락·오배정을 둘 다 잡음) |
| `AppIsolationTest#appRestrictedPortDependentsShouldBelongToThatApp` | 없음 | 신설 — web 전용 포트(`MailSenderPort`·`SmsSenderPort`·`PgProviderGatewayPort`·`SocialOAuthClientPort`)는 `@WebApp`, batch 전용 포트(`BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort`)는 `@BatchApp`만 받는다 |
| `ServiceContextBoundaryTest` | 스테레오타입 없는 `..service..` POJO | 변경 없음 — 마커는 스테레오타입이 아니므로 계속 검사 대상 |
| `AppIsolationTest#sharedBeansShouldNotDependOnWebOnlyServices` | 이름 목록 4개 | 유지(중복 방어) |
| `AppIsolationTest#beansShouldHaveExactlyOneAppMarker` | `@Service`/`@Component`만 | 마커-only 도메인 서비스 포함(마커 2개 부착을 잡는다) |
| `AppIsolationTest#markerBeanCounts` | `@WebApp` ≥60 · `@AdminApp` ≥55 · `@CeoApp` ≥95 · `@BatchApp` ≥12 · `@SharedApp` ≥12 | 마커-only 서비스 포함, `@WebApp` ≥83 · `@AdminApp` ≥65 · `@CeoApp` ≥122 · `@BatchApp` ≥15 · `@SharedApp` ≥47 |
| `LayerRulesTest#sharedConfigsShouldOnlyDeclareUnmarkedBeans` | `@SharedApp` config 21개(`*ServiceConfig` 20 + `SharedEventConfig`) 대상 | `SharedBeanConfig` 1개 대상으로 계속 통과 |

규칙 세부(원문 기록):

- `sharedAppOnlyOnListeners`의 "마커-only 도메인 서비스"는 `..service..`에 있고, 이름이 `*CommandService`/`*QueryService`가 아니며, `port.in` UseCase 구현체가 아닌 클래스입니다. `markerOnlyClassesShouldBeDomainServices`는 리스너가 `@Component`, 설정이 `@Configuration`을 함께 단다는 전제로 마커-only 클래스의 위치를 강제했습니다.
- `appsShouldNotDependOnEachOther`의 before는 마커 5×4=20조합 전부 금지였습니다. after에도 앱 ↔ 앱, `@SharedApp` → 앱, 앱 → `@SharedApp` 리스너·설정은 계속 금지했습니다.
- `constructorDependenciesShouldBeVisibleToApp`: 마커 M 클래스(스테레오타입 유무 무관)의 생성자 파라미터 타입 T(제네릭 인자 포함)가 `com.tastyhouse.{application,domain}..`이면 (i) M/`@SharedApp` `@Configuration`의 `@Bean` 반환 타입이거나, (ii) 구체 클래스로서 M/`@SharedApp` 마커를 갖거나, (iii) 인터페이스로서 application 안 구현체 중 하나가 M/`@SharedApp`이거나, (iv) application 안에 구현체가 없어야(persistence·벤더 구현) 했습니다. 검사 의존 수 하한은 700이었습니다. 후속 보강으로 마커 `@Configuration`의 `@Bean` 메서드 파라미터도 그 설정의 마커 기준으로 같은 판정을 받게 했고(하한 ≥ 3), 컬렉션이 아닌 인터페이스 의존의 주입 후보가 2개 이상이면 모호로 실패시켰으며, 구현체 집계에서 데코레이터(그 인터페이스를 생성자로 받는 클래스)·abstract를 뺐습니다.
- 마커 분포 합계는 `@WebApp` 17 · `@AdminApp` 3 · `@CeoApp` 21 · `@BatchApp` 2 · `@SharedApp` 35 = 78이었습니다. `SharedBeanConfig`는 `SharedEventConfig`에서 `git mv`로 리네임했습니다. 잔류 `@Bean`의 domain 계산기·정책 7개는 `productExposureCalculator`·`cupDepositPolicy`·`storePriceBadgePolicy`·`shopOperatingStatusCalculator`·`shopDeliveryTipCalculator`·`shopNextOpenTimeCalculator`·`scheduledOrderSlotCalculator`였고, `domainEventPublisher`는 인터페이스 타입으로, `shopDeliveryTipRangePolicy`는 상수·람다로 조립하는 record로, `prohibitedWordValidator`는 `CachingProhibitedWordPersistencePort`로 감싸 조립했습니다.
- 고정점 예시에는 `ShopLifecycleService`(Shared) → `ShopImageApprovalService`·`ShopCeoAssignmentRecorder`도 Shared가 있었습니다. 용어상 앱 오케스트레이터는 `*CommandService`/`*QueryService`(batch는 `*SchedulerService`)로 `@Service` + 앱 마커를 달고 UseCase를 구현하며 트랜잭션 경계를 가졌고, "소비 앱 집합"은 생성자 주입을 따라 올라가 최종적으로 그 빈을 쓰는 앱의 집합이었습니다.

## 근거

- config를 없애지 못한 원인은 규칙 3개였습니다.
  - `sharedAppOnlyOnListeners`: `@SharedApp`은 리스너·config 전용
  - `appsShouldNotDependOnEachOther`: 앱 → `@SharedApp` 의존 금지
  - `ServiceContextBoundaryTest`: 스테레오타입이 붙으면 검사 대상에서 조용히 빠짐

  앞의 둘은 "앱 → 공유 도메인 서비스 단방향 허용(공유 커널)"으로 개정했습니다. 셋째는 `@Service`를 달지 않는 것으로 피했습니다.
- **`@Service`를 달지 않은 이유**: `ServiceContextBoundaryTest`는 `@Service`/`@Component`가 붙은 클래스를 대상에서 뺍니다. 달면 컨텍스트 경계 검사가 빌드 실패 없이 사라집니다. 반대로 필터를 넓혀 `@Service`까지 검사하면, 기존 오케스트레이터 37개 중 13개 이상이 봉인 목록에 없는 위반이라 실패합니다. 그래서 "마커만 = 도메인 서비스, `@Service` + 마커 = 앱 오케스트레이터"로 갈랐습니다.
- **"현재 소비자" 기준을 택한 이유**: 미리 넓혀 두면 쓰지 않는 앱에 빈을 띄우는 비용만 생깁니다. 다른 앱이 필요해지면 그때 마커를 `@SharedApp`으로 올리면 됩니다. 올리지 않고 주입하면 `constructorDependenciesShouldBeVisibleToApp`이 빌드에서 잡습니다. `PaymentCancellationService`의 소비자는 `PaymentRefundRequestService`(당시 `PaymentCommandService`)·`PaymentCancellationExecutor`로 모두 web이었습니다. 리스너 자체가 4앱 공통인 이유(이벤트는 어느 앱이 발행하든 처리돼야 한다)는 리스너에만 적용됩니다.
- **4앱 공통 등록을 버린 이유**: `*ServiceConfig`는 03a에서 persistence `*DomainConfig`의 등록 범위(4앱 전부)를 지키려고 만든 것이었습니다. 그 대가로 한 앱 전용 빈 44개가 쓰지 않는 앱에도 떴습니다. 앱 마커가 이미 "어느 앱에 뜨는가"를 표현하는 유일한 수단이므로, 서비스 자신이 마커를 갖는 것이 정보가 한 곳에 사는 형태라고 봤습니다.
- **앱 → 공유만 허용하고 공유 → 앱은 막은 이유**: `@SharedApp` 빈은 4앱 전부에 뜨는데, 앱 전용 빈을 주입하면 나머지 세 앱에서 그 빈을 찾지 못해 기동이 실패합니다. 반대 방향은 공유 빈이 어느 앱에나 있으므로 항상 안전합니다. 허용 범위는 `..service..`의 공유 도메인 서비스로만 열었습니다.
- **고정점 계산이 필요한 이유**: 한 서비스를 `@SharedApp`으로 올리면 그 의존 서비스도 따라 올라가야 합니다. 새 서비스를 추가하거나 소비자가 바뀌면 이 전파를 다시 따라가야 하고, 누락하면 `constructorDependenciesShouldBeVisibleToApp`이 위반 경로를 이름으로 보여 줬습니다.
- **domain 계산기 7개에 마커를 달 수 없었던 이유**: domain 모듈은 production 의존이 0개(spring-free)이고 마커 애노테이션은 application 소유라 domain이 볼 수 없습니다. 그래서 `SharedBeanConfig`의 `@Bean`이 남았습니다.

## 결과·감수한 손실

- HTTP·DB 동작과 빈 이름은 바뀌지 않았습니다. 한 앱 전용 빈이 다른 앱 컨텍스트에서 사라지는 것은 동작 변경이지만, 주입처가 없는 앱에서 사라질 뿐이라 기능 차이는 없습니다.
- 반증 probe 7종을 임시 클래스로 만들어 각각 실패하는 것을 확인하고 지웠습니다. (a) `@SharedApp` 서비스가 `@WebApp` 서비스 주입, (b) 마커-only 클래스를 `..service..` 밖에 둠, (c) `@WebApp` 클래스가 구현체가 `@CeoApp`뿐인 인터페이스 주입, (d) 마커 2개 부착, (e) 마커 없는 `..service..` POJO를 `@WebApp` 서비스에 주입(마커 누락), (f) `MailSenderPort`를 받는 클래스에 `@SharedApp`, (g) `List<PgProviderGatewayPort>`를 받는 클래스에 `@SharedApp`입니다. `backend`에서 `./gradlew build`가 통과했고, `grep -rl '@Bean' application-core/src/main/java`는 `SharedBeanConfig` 1개만 반환했습니다.
- 사각지대도 남았습니다. 4앱 `java -jar` 기동(`Started *ApiApplication` 마커) 확인은 별도 검증 세션의 몫으로 남겼습니다(`contextLoads`는 빈 껍데기라 대신할 수 없습니다). `List<I>` 주입(`PgPaymentGatewayRouter`)은 구현체가 0개여도 기동되므로 규칙이 잡지 못합니다.
- 채널 모듈 `infrastructure:pg`는 코드가 전부 사라져 `runtimeOnly project(':infrastructure:tosspayments')` 한 줄만 남은 조립 스타터가 됐습니다.
- [ADR-0036](0036-app-marker-removal.md)이 이 결정을 대체했습니다. "config 삭제"와 "`SharedBeanConfig`에는 애노테이션을 달 수 없는 빈만 둔다"는 유지됐습니다. 반면 "마커만 달고 `@Service`는 달지 않는다"는 반대로 바뀌어, 지금은 도메인 서비스가 `@Service`를 달고 모듈 위치로 소속을 표현합니다.

## 영향받은 문서

- `backend/application-core/AGENTS.md` · `application \`*ServiceConfig\`를 두지 않는다 — 도메인 서비스는 \`@Service\`로 스캔 등록` · 도메인 서비스를 `new` + `@Bean`으로 등록하는 config를 두지 않는다
- `backend/application-core/AGENTS.md` · `도메인 서비스를 \`@Bean\`으로 등록하지 않는다 — \`*ServiceConfig\`를 되살리지 않는다` · `SharedBeanConfig`에는 애노테이션을 달 수 없는 빈만 둔다
- `backend/application-core/AGENTS.md` · `도메인 서비스를 둘 모듈은 소비 앱 집합으로 정한다 — 공유 커널 단방향` · 소비 앱 집합 기준과 고정점 계산(지금은 모듈 선택 기준)
- `backend/application-core/AGENTS.md` · `덩어리 02/03a — 벤더 포트·서비스 4종을 \`domain\`에서 이관` / `덩어리 03a — 나머지 포트·도메인 서비스·이벤트 발행기를 \`domain\`에서 이관` · 등록 방식 서술의 현행 자리
- `backend/CLAUDE.md` · `공존형 채널 — PG 라우터 (\`infrastructure:pg\`)` · 라우터 등록 위치
- `backend/CLAUDE.md` · `앱 마커 규칙 (챕터 03 — 스캔이 패키지에서 애노테이션으로)` · 폐기 표기만 남은 절
- `backend/infrastructure/pg/AGENTS.md` · `라우터 등록은 어디인가 — \`web-application\`의 \`PgPaymentGatewayRouter\`` · 이 모듈은 등록에 관여하지 않는다
- `backend/infrastructure/mail/AGENTS.md` · `도메인 서비스 등록은 어디인가 — \`web-application\`의 \`MailVerificationService\`` · 같은 취지
- `backend/infrastructure/sms/AGENTS.md` · `도메인 서비스 등록은 어디인가 — \`web-application\`의 \`SmsVerificationService\`` / `인증 발급이 발송까지 원자적인 이유` · 같은 취지
- `backend/AGENTS.md` · `Purpose` / `Subdirectories` / `Module Dependency Graph` · 도메인 서비스 등록 위치
- `backend/domain/AGENTS.md` · `Working In This Directory` / `모듈 전역 — 프레임워크-프리와 명시적 저장` · domain 계산기는 `SharedBeanConfig`가 등록한다
- `backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md` · `Key Files / Cross-cutting packages` / `Working In This Directory` · 같은 취지
- `CLAUDE.md` · `리포지토리 구조` · 채널 모듈이 코드 없는 스타터라는 서술
