# application-core

**application 계층의 코어 모듈.** application 계층은 앱 마커 제거 이후 **5개 Gradle 모듈**로 나뉜다 — 이 코어 `application-core`와 앱 모듈 `web-application`·`admin-application`·`ceo-application`·`batch-application`(각 모듈의 `AGENTS.md` 참고). 자바 패키지는 5모듈 모두 `com.tastyhouse.application.<도메인>.{port.in, port.out, service, listener}` 하나를 나눠 쓴다(split package). **이 코어에는 "2개 앱 이상이 쓰는 것"만 산다** — 공유 도메인 서비스, 도메인 이벤트 리스너 12종(`<ctx>/listener/`), `shared/**`(`SharedBeanConfig` 포함), **앱 전용 SPI 포트를 뺀 모든 `port.out` 계약**(읽기 계약·write 포트·Command 반환 Result). UseCase 인터페이스·Command record·유스케이스 서비스(유스케이스당 서비스 1개 — 분리 전에는 `*CommandService`/`*QueryService` 쌍)·앱 전용 도메인 서비스·앱 전용 SPI 포트는 앱 모듈에 있다. **앱 소속은 마커 애노테이션이 아니라 Gradle 모듈이 표현한다** — 상세는 아래 [앱 모듈 재분리](#앱-모듈-재분리--앱-소속은-gradle-모듈이-표현한다). 이 문서는 5모듈 공통 규칙의 정본이기도 하다(앱 모듈 `AGENTS.md`는 요약만 둔다).

컨트롤러(`<ctx>/adapter/in/web/`)·`request/`·`response/`·config·security 정책·전역 예외 핸들러와 부트스트랩은 각 api 모듈(`web-api`·`admin-api`·`ceo-api`·`batch-module`)에 남아 있다.

## 앱 모듈 재분리 — 앱 소속은 Gradle 모듈이 표현한다

**앱 소속은 애노테이션이 아니라 클래스가 들어 있는 Gradle 모듈이 표현한다.** 앱 마커 애노테이션(`@WebApp`/`@AdminApp`/`@CeoApp`/`@BatchApp`/`@SharedApp`)은 없다 — 되살리지 않는다(아래 "봉인·가드 목록"의 "앱 모듈 경계 가드"). 자바 패키지는 5모듈이 하나를 나눠 쓴다(아래 [패키지 평탄화](#패키지-평탄화-챕터-03)). 경위는 [ADR-0036](../../docs/adr/0036-app-marker-removal.md).

**용어 풀이**

- **코어**: Gradle 모듈 `application-core`(자바 패키지는 앱 모듈과 같은 `com.tastyhouse.application`). 2개 앱 이상이 쓰는 것과 리스너·`@Configuration`·`port.out` 계약을 담는다.
- **앱 모듈**: `{web,admin,ceo,batch}-application`. 평면 이름의 `java-library`이고 `api project(':application-core')`으로 코어를 노출한다. 앱 하나만 쓰는 빈·UseCase·Command·도메인 서비스·SPI 포트를 담는다.
- **컴파일 게이트**: "클래스패스에 없어서 아예 import할 수 없다"로 막는 것. ArchUnit은 위반을 테스트 시점에 잡지만 컴파일 게이트는 코드가 애초에 컴파일되지 않는다.
- **split package**: 같은 자바 패키지를 여러 모듈이 나눠 갖는 것. `security-core`/`security-module`(`com.tastyhouse.security..`) 선례가 있다.

**왜 마커가 아니라 모듈인가**

- 마커는 **컴파일러가 모르는 약속**이다. 잘못 달거나 빠뜨려도 빌드가 통과하므로 ArchUnit 규칙으로 메워야 하고, 규칙이 많을수록 술어의 사각지대도 많아진다. 모듈은 다른 앱의 타입을 쓰는 순간 컴파일 에러가 난다.
- **앱 모듈끼리는 어떤 경로로도 서로를 보지 못한다.** 영속 어댑터(`infrastructure:jpa`·`infrastructure:mybatis`)가 application에서 import하는 것은 `..port.out..`뿐이고 그 계약이 전부 코어에 있으므로 **영속 어댑터는 코어 하나만 의존**한다. 그래서 앱 모듈을 나누면 컴파일 게이트가 실제로 동작한다. **`port.out` 계약을 앱 모듈로 옮기지 않는 이유가 이것이다** — 계약이 앱 모듈에 생기면 영속 어댑터가 그 앱 모듈을 의존해야 하고, 그 순간 영속 어댑터를 거쳐 앱 모듈들이 서로를 보게 된다.

**현재 구성**

| 항목 | 구성 |
|---|---|
| application 계층 모듈 | 코어 `application-core` + 앱 모듈 `{web,admin,ceo,batch}-application` 4개 |
| 앱 소속 표현 | 클래스가 들어 있는 Gradle 모듈 |
| 자바 패키지 | 5모듈 모두 `com.tastyhouse.application..`(split package) — 클래스를 모듈 사이로 옮겨도 import가 안 바뀐다 |
| 빈 선언 | 전부 `@Service`(또는 `@Component`) |
| 유스케이스 서비스 vs 도메인 서비스 구분 | **`port.in` UseCase를 구현하면 유스케이스 서비스, 아니면 도메인 서비스**(구조로 판정) |
| 앱 스캔(`ApplicationLayerScanConfig`) | `@Configuration(proxyBeanMethods = false) @ComponentScan(basePackages = "com.tastyhouse.application")` — 기본 필터, 필터 없음. 클래스패스(코어 + 자기 앱 모듈)가 스캔 범위를 정한다 |
| 실행 앱 의존 | `implementation project(':application-core')` + `implementation project(':{앱}-application')` |
| 어댑터 의존 | `{kakao,naver,apple,facebook}-oauth`·`javamail`·`solapi`·`aws-ses`·`aws-sns` → `:web-application`, `bbq`·`admdongkor` → `:batch-application`. `jpa`·`mybatis`·`firebase`·`aws-s3`·`tosspayments`·`security-module`·`api-common-module` → `:application-core`. 조립 스타터 `persistence`는 `jpa`·`mysql`만 `runtimeOnly`로 묶고 application 모듈을 의존하지 않는다 |

**앱 전용 SPI 포트(앱 모듈 소유)** — 구현(벤더)이 그 앱에만 조립되는 포트는 그 앱 모듈이 갖는다. 코어가 이 포트를 import하면 **컴파일 에러**다(반증 확인).

- `web-application`: `emailverification.port.out.{MailSenderPort,MailSendResult}`, `phoneverification.port.out.{SmsSenderPort,SmsSendResult,SmsSendFailure}`, `auth.port.out.{SocialOAuthClientPort,SocialAuthorization,SocialCredential,SocialOAuthResult,SocialOAuthFailure,SocialProfile,SocialProvider}`
- `ceo-application`: `ceo.port.out.ReplyPhraseTextValidatorPort` — 구현(`shop/service/ReplyPhraseProhibitedWordValidatorAdapter`)과 유일한 소비자가 모두 ceo라 코어에서 옮겨왔다. 이 포트는 구현이 infrastructure가 아니라 같은 앱 모듈에 있다는 점이 다른 SPI와 다르다
- `batch-application`: `crawling.bbq.port.out.*`(`BbqMenuPort`·`RemoteImagePort`·DTO), `region.port.out.{AdminDongBoundaryPort,AdminDongBoundaryFetchResult,AdminDongBoundarySource,BoundaryCoordinate,BoundaryRing}`

**PG SPI는 앱 전용이 아니다** — `payment.port.out.{PgProviderGatewayPort,PgPaymentGatewayPort,PgCancelResult,PgProviderCode}`와 라우터 `payment/service/PgPaymentGatewayRouter`는 web(승인·취소·즉시 조회)과 batch(주기 대사) 두 앱이 쓰므로 이 모듈(코어)에 있다. 구현 벤더 `infrastructure:tosspayments`는 `infrastructure:pg`를 조립한 web·batch에만 실리므로, admin·ceo에서는 라우터가 `List` 주입으로 빈 목록이 되어 어떤 PG도 지원하지 않는다(admin·ceo에는 라우터를 주입하는 빈도 없다). [ADR-0061](../../docs/adr/0061-pg-ports-to-core-batch-reconcile.md)

**새 클래스를 어디에 두나**

1. 쓰는 앱이 하나 → 그 `{앱}-application`. UseCase·Command·유스케이스 서비스(`{도메인}{동작}Service`/`{도메인}{관점}QueryService`, batch는 `*SchedulerService`)는 언제나 앱 모듈이다(코어의 UseCase는 0개). 새 연산은 포트 1개(추상 메서드 1개) + 서비스 1개(포트명의 `UseCase`→`Service`)로 만든다 — 아래 "봉인·가드 목록"의 "유스케이스 서비스 1:1 규칙 4종".
2. 2개 앱 이상이 쓰는 도메인 서비스 → 코어. 두 번째 앱이 생기는 시점에 코어로 옮긴다(패키지가 같아 import 불변).
3. 빈이면 언제나 `@Service`/`@Component`를 단다.
4. 리스너(`@TransactionalEventListener`)와 `@Configuration`은 **코어에만**. `@Configuration`이 스테레오타입 클래스를 `@Bean`으로 다시 등록하지 않는다(스캔과 이중 등록).
5. `port.out` 계약은 코어. 예외는 위 앱 전용 SPI 포트.
6. 코어 빈은 구현체가 앱 모듈에만 있는 application 인터페이스를 주입받지 않는다.

**가드** — 정본 목록은 아래 [봉인·가드 목록](#봉인가드-목록)의 "앱 모듈 경계 가드" 항목.

| 대상 | 가드 |
|---|---|
| 앱 간 수평 의존 · 앱 전용 포트 소비자 | 컴파일 게이트(다른 앱 모듈이 클래스패스에 없다) + 각 앱 `ApplicationModuleClasspathTest` |
| 코어에 둘 것·두지 않을 것 | `LayerRulesTest#listenersAndConfigsShouldResideInCore`·`#coreShouldNotContainUseCasesOrOrchestrators`·`#coreBeansShouldOnlyDependOnCoreVisibleTypes`·`#configurationsShouldNotRegisterStereotypedClasses` |
| batch 선별 | `commandRecordsShouldBeBoundaryTyped`의 batch 제외와 `BatchSchedulerRulesTest` 대상 모두 출처 모듈(`batch-application`)로 판정 |
| 모듈별 개수 anchor | `RuleAnchorTest#moduleBeanCounts`(web ≥83 · admin ≥65 · ceo ≥122 · batch ≥15 · core ≥47)·`#moduleUseCaseCounts`(web ≥185 · admin ≥207 · ceo ≥188 · batch =8 · core =0) |
| 도메인 서비스 경계 대상 | `ServiceContextBoundaryTest#domainServices()` — 구조 조건 − `EXCLUDED_COLLABORATORS` 21개, 하한은 `domainServicesShouldExist`, 짝 `excludedCollaboratorsShouldNotBeStale` |
| 앱 부트스트랩 스캔 | testFixtures `ApplicationLayerScanAssertions#assertScansApplicationLayerWithoutFilters`·`#assertLoadsOnlyOwnApplicationModule(appModule)` |
| 출처 모듈 판정 | testFixtures `com.tastyhouse.architecture.ModuleOrigin`(`ModuleOrigin.from(module)` 술어) + `ModuleOriginTest` |
| FQCN 중복 | `SplitPackageUniquenessTest` |

- **아키텍처 테스트는 이 코어 모듈의 테스트에 둔다.** `application-core/build.gradle`이 `testImplementation project(':{web,admin,ceo,batch}-application')` 4줄을 가져 ArchUnit이 5모듈을 모두 본다. test → main 방향이라 Gradle 순환이 아니다.
- **공유 테스트 더블 20개**(`Fake*`/`Stub*`/`Recording*`/`LogCapture`)는 코어 testFixtures의 `com.tastyhouse.testsupport.<ctx>..`에 있다. 앱 모듈의 테스트도 이 더블을 써야 하기 때문이다(그래서 public이다). 새 최상위 import 세그먼트 `testsupport`는 4순위다(`ImportOrderConventionTest.TOP_SEGMENT_RANK`, `backend/CLAUDE.md`의 import 순서 표).
- 새 규칙은 전부 위반 probe로 실패를 확인했다(코어가 `MailSenderPort`를 import하면 컴파일 에러인 것도 확인).

## 앱 축 통합 (챕터 01) — 폐기됨

폐기됨. application 계층을 앱별 모듈 없이 `application` 하나로 두던 구조는 다시 앱 모듈 4개로 나뉘었다. 현재 규칙은 [앱 모듈 재분리](#앱-모듈-재분리--앱-소속은-gradle-모듈이-표현한다). 경위는 [ADR-0013](../../docs/adr/0013-application-module-merge.md)·[ADR-0036](../../docs/adr/0036-app-marker-removal.md).

## 패키지 평탄화 (챕터 03)

**application 5모듈은 자바 패키지 `com.tastyhouse.application` 하나를 나눠 쓴다(split package).** 앱별 패키지(`com.tastyhouse.{web|admin|ceo|batch}application`)는 없다. 앱 소속은 패키지가 아니라 클래스가 들어 있는 Gradle 모듈이 표현한다(위 [앱 모듈 재분리](#앱-모듈-재분리--앱-소속은-gradle-모듈이-표현한다)). 경위는 [ADR-0014](../../docs/adr/0014-package-flattening-app-markers.md).

- **왜 하나로 두나**: 앱별 패키지가 있으면 ArchUnit 슬라이스 규칙·import 정렬 규칙이 "접두어가 겹치는 4개 패키지"를 특별 취급해야 하고, 그 특별 취급 자체가 문서·규칙의 복잡도가 된다. 패키지가 하나면 클래스를 모듈 사이로 옮겨도 import가 바뀌지 않는다.
- **패키지만 봐서는 앱 소속을 알 수 없다**: `com.tastyhouse.application.notice.service`의 클래스가 어느 앱 것인지는 그 파일이 어느 모듈(`{앱}-application` / 코어) 아래 있는지로 판단한다.
- **같은 FQCN을 두 모듈에 만들지 않는다**: 컴파일은 통과하고 클래스패스 순서로 한쪽이 조용히 가려진다. `SplitPackageUniquenessTest`가 5모듈 소스 경로의 중복 상대 경로를 실패시킨다(아래 "봉인·가드 목록"의 "앱 모듈 경계 가드").
- **`<ctx>/port/out`은 "이 도메인의 모든 아웃바운드 계약"이다.** 읽기 계약(`QueryPort`·`Result`·`SearchCondition`) + 아웃바운드 SPI(`SocialOAuthClientPort`·`BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort`) + **명령 유스케이스 서비스가 반환하는 Result/View record**가 함께 산다.
- **그래서 CQRS 교차 주입 규칙은 이름으로 대상을 고른다**: `commandServicesShouldNotDependOnQueryPorts`의 금지 대상은 패키지(`..port.out..`)가 아니라 이름(`*QueryPort`·`*QueryService`·`*QueryUseCase`)이다. 패키지 술어를 쓰면 `port.out`에 사는 Command 반환 record를 import하는 명령 서비스가 정당한 반환 타입인데도 위반으로 잡힌다. 같은 이유로 api 3모듈의 `controllersShouldNotDependOnQueryPorts`도 이름 기준이다.

## 덩어리 02/03a — 벤더 포트·서비스 4종을 `domain`에서 이관

**파일·메일·SMS·결제 4개 컨텍스트의 아웃바운드 포트와, 그것을 쓰는 도메인 서비스 4개가 `domain`을 떠나 이 모듈로 옮겨왔다.** 소셜 로그인 SPI(`auth.port.out`)·BBQ 크롤링 포트가 이미 이 모듈에 있던 것과 같은 형태로, "벤더 무관 계약을 `domain`이 갖고 외부 연동 모듈이 구현한다"는 배치를 "계약을 유스케이스 계층이 갖고 외부 연동 모듈이 구현한다"로 확장한 것이다.

| 컨텍스트 | 이관된 포트 | 이관된 서비스 | 이전 위치 |
|---|---|---|---|
| file | `file/port/out/FileStoragePort`(+`FileDeleteResult`) | `file/service/FileUploadService`(+`FileUploadCommand`) | `domain.file.port.FileStoragePort`, `domain.file.service.FileUploadService` |
| emailverification | `emailverification/port/out/MailSenderPort`(+`MailSendResult`) | `emailverification/service/MailVerificationService`(+package-private `MailVerificationMessage`) | `domain.mail.port.MailSender`, `domain.mail.service.MailVerificationService` |
| phoneverification | `phoneverification/port/out/SmsSenderPort`(+`SmsSendResult`, enum `SmsSendFailure{NO_RESPONSE,FAILED,API_ERROR}`) | `phoneverification/service/SmsVerificationService` | `domain.sms.port.SmsSender`, `domain.sms.service.SmsVerificationService` |
| payment | `payment/port/out/{PgPaymentGatewayPort,PgProviderGatewayPort}` + `PgConfirmResult`·`PgConfirmFailure`·`PgCancelResult`·`PgPaymentLookupResult`·`PgPaymentLookupStatus`·`TossPaymentDetail` + 신설 enum `PgProviderCode` | `payment/service/{PaymentConfirmationService,PgPaymentGatewayRouter}`(+`PgConfirmationTarget`) | `domain.payment.port.{PgPaymentGatewayPort,PgProviderGatewayPort}`, `domain.payment.service.{PaymentConfirmationService,PgPaymentGatewayRouter}` |

**왜 서비스까지 옮겼나 — 포트만으로는 끝나지 않는다.** 이 네 서비스는 순수 POJO라는 점에서 기존 도메인 서비스와 다르지 않지만, 이관된 포트를 생성자로 주입받는다. 포트가 `application`으로 옮겨간 채 서비스만 `domain`에 남으면 `domain`이 `application`의 포트 인터페이스를 참조해야 해 **의존 방향이 뒤집힌다**(안쪽이 바깥쪽을 아는 상태). 그래서 포트를 쓰는 서비스 자체도 함께 옮겼다 — "이 서비스가 도메인 불변식을 오케스트레이션하는가"라는 기존 배치 기준(`domain/AGENTS.md`의 "도메인 서비스(`<ctx>/service/`)는 순수 POJO다" 절)은 여전히 참이지만, **아웃바운드 포트가 domain 밖에 있으면 그 포트를 쓰는 오케스트레이션도 domain 밖에 있어야 한다**는 조건이 우선한다.

- **등록 위치는 현재 소비 앱 집합으로 정한다** — `FileUploadService`(4앱 전부가 `FileStoragePort` 구현 firebase/aws-s3를 가진다)와 `PaymentConfirmationService`(코어 리스너 `PaymentEventListener`가 쓴다)는 코어, `MailVerificationService`·`SmsVerificationService`·`PaymentCancellationService`(소비자가 web뿐)는 `web-application`에 둔다. `PgPaymentGatewayRouter`는 web·batch 두 앱이 쓰므로 코어다. 전부 `@Service` 스캔 등록이고 config 클래스는 없다. "언젠가 다른 앱이 쓸 수 있으니 미리 코어에"는 하지 않는다 — 쓰지 않는 앱에 빈을 띄우는 비용만 있고, 두 번째 앱이 필요해지면 그때 코어로 옮긴다(패키지가 같아 import 불변). 앱 모듈에 둔 빈을 코어 빈이 주입하면 컴파일 에러 또는 `coreBeansShouldOnlyDependOnCoreVisibleTypes`가 잡는다.
- **persistence에는 도메인 서비스 등록 설정(`*DomainConfig`)이 없다.** 도메인 서비스는 그것이 사는 application 모듈의 `@Service` 스캔으로 등록된다. 판정 기준은 "외부 연동 포트인가"가 아니라 "이 서비스가 지금 어디 있는가"다.
- **`PgProviderGatewayPort.provider()`는 domain `PgProvider`가 아니라 이 모듈 신설 enum `PgProviderCode`를 반환한다.** `PgPaymentGatewayPort`(라우터가 구현하는, 소비 측이 호출하는 계약)는 domain `PgProvider`의 상수명(`String`)을 받는다 — 라우터(`PgPaymentGatewayRouter`)가 `PgProviderCode.name()` → `PgProvider.valueOf(...)`로 두 enum을 **상수명으로만** 연결한다. 벤더(`infrastructure:tosspayments`)가 `PgProviderGatewayPort`를 구현하며 domain을 몰라도 되게 하려는 것이 이 우회의 목적이다 — `PgProviderCode`가 `domain`을 참조하지 않으므로 벤더 모듈도 `domain` 의존 없이 채널 어댑터를 만들 수 있다. **두 enum은 상수명·순서가 항상 같아야 하며**, `application-core/src/test/.../architecture/EnumCodeConstantsTest#pgProviderCodeMatchesPgProvider`가 `Enum::name` 배열을 대조해 어긋남을 잡는다. 한쪽에만 상수를 추가하면 이 테스트가 즉시 실패한다(라우터의 `PgProvider.valueOf(code.name())`이 매핑되지 않는 상수에서 `IllegalArgumentException`을 내는 런타임 위험의 컴파일 타임 방어선).
- **`SocialOAuthClientPort` SPI는 예외 없는 `Optional`형 결과를 쓴다** — `exchange`/`fetchProfile`은 예외를 던지지 않고 `SocialOAuthResult<T>`(`value` XOR `failure`인 record, compact constructor가 강제)를 반환한다. 실패는 enum `SocialOAuthFailure{APPLE_ID_TOKEN_INVALID,ACCESS_TOKEN_REJECTED}`로 표현하고, `auth/service/SocialLoginService` 1개가 `.orElseThrow(SocialOAuthFailures::toException)`로 소비한다(같은 유틸에 provider별 임시토큰 만료 코드 매핑 `tempTokenExpired(SocialProvider)`가 있다). `auth/service/SocialOAuthFailures`(정적 유틸)가 `APPLE_ID_TOKEN_INVALID → WebErrorCode.APPLE_ID_TOKEN_INVALID`·`ACCESS_TOKEN_REJECTED → WebErrorCode.SOCIAL_OAUTH_FAILED`로 매핑한다 — 카카오·네이버는 항상 `success(...)`로 감싸고, 실패는 예외가 아니라 값으로 표현한다. 응답 계약(에러코드 문자열·HTTP 상태)은 예외 기반 구현과 같다.
- **`RemoteImagePort.download`도 같은 형태의 결과 record를 반환한다** — `ImageDownloadResult(image, failure)` record(`image` XOR `failure`, compact constructor가 강제)를 반환하고, 실패는 enum `ImageDownloadFailure{EMPTY,SIZE_EXCEEDED}`로 표현한다. 어댑터는 예외를 던지지 않고 실패를 값으로 돌려주며, `BbqService`가 `success()`를 확인해 각각 `ApplicationErrorCode.FILE_EMPTY`/`FILE_SIZE_EXCEEDED`로 번역한다 — 위 `SocialOAuthClientPort`와 같은 패턴이다.
- **`AdminDongBoundaryPort.fetchAll()`도 결과 record를 반환한다** — `AdminDongBoundaryFetchResult(sources, failed)`를 반환하고, `AdminDongBoundarySource`는 domain `GeoPoint`/`GeoRing`을 담지 않는다. 대신 `List<BoundaryRing>`(`BoundaryRing(List<BoundaryCoordinate>)`, `BoundaryCoordinate(double latitude, double longitude)`)라는 이 모듈 소유의 좌표 전용 타입을 담는다 — 어댑터(`infrastructure:admdongkor`)는 원시 좌표만 돌려주고, `GeoRing` 조립(퇴화 링 스킵)·중심점 계산(`InteriorPoint`)·중심점 없는 행 스킵·전량 실패시 `ADMIN_DONG_BOUNDARY_FETCH_FAILED`는 전부 `region/service/AdminDongSchedulerService`(이 모듈)가 수행한다. 어댑터가 domain 기하 타입을 몰라도 되게 하려는 것이 이 분리의 목적이며, 위 `PgProviderCode`·`SocialOAuthFailure`와 같은 "어댑터는 벤더 무관 원시 타입만, 판단은 유스케이스 계층"이라는 원칙의 반복 적용이다.
- **`architecture/EnumCodeConstantsTest`** — 위 `PgProviderCode`↔`PgProvider` 대조. web 전용 서비스(`MailVerificationService`·`SmsVerificationService`)와 그 포트는 `web-application`에 있어 코어 빈은 컴파일 단계에서 볼 수 없다. PG 포트·라우터는 코어에 있다(위 "PG SPI는 앱 전용이 아니다").
- **domain 쪽 정리**: `domain/AGENTS.md`의 `ContextBoundaryTest.SEALED_VIOLATIONS`에서 `MailVerificationService`·`PaymentConfirmationService` 2건이 빠졌다(대상이 domain 밖으로 나갔으므로 봉인 목록도 그 파일의 관할이 아니게 됐다) — 16개 → 14개.

## 덩어리 03a — 나머지 포트·도메인 서비스·이벤트 발행기를 `domain`에서 이관

**포트와 포트를 주입받는 서비스는 전부 이 application 계층에 있다.** `domain`에는 모델·VO·이벤트 타입·포트 없는 순수 계산기/정책·공유 커널·예외만 남는다. 이관은 클래스 위치만 바꿨고 로직은 바꾸지 않았다(HTTP·DB 계약 불변). write 포트 시그니처는 domain 모델(`Optional<Notice> findById(NoticeId)`)이고, persistence는 `:domain`을 `implementation`으로 의존해 그것을 구현한다(아래 [persistence domain 재허용](#persistence-domain-재허용--persistence가-도메인-모델을-직접-쓴다)). 이관된 서비스는 `@Service`로 스캔 등록된다 — 등록용 config는 없다(아래 [application `*ServiceConfig`를 두지 않는다](#application-serviceconfig를-두지-않는다--도메인-서비스는-service로-스캔-등록)). 경위는 [ADR-0027](../../docs/adr/0027-domain-ports-to-application.md).

| 항목 | before | after |
|---|---|---|
| write 포트 106개 + 보조 타입 2개(`AdminDongSyncResult`·`ShopDeliveryTipRegionLookupPort` — 이후 네이밍 전환에서 `ShopDeliveryTipRegionLookupPort`는 쓰기 포트로 분류돼 `writePortsExist`에 포함된다) | `domain/<ctx>/repository/` | **`<ctx>/port/out/write/`** (`member/follow`·`member/referral`처럼 한 겹 더 있으면 경로를 그대로 따른다 — `member/follow/port/out/write/`) |
| 출력 포트 7개 + record 3개 | `domain/<ctx>/port/` | `<ctx>/port/out/` (`product`·`member`·`rank`·`search`·`ceo`) |
| 포트를 주입받는 서비스 71개 | `domain/<ctx>/service/` | `<ctx>/service/` (마커 없는 POJO). 순수 서비스가 이동 서비스를 주입하면 함께 옮겼다(`ShopRiderGuideValidator` — domain은 application을 볼 수 없다). package-private 문구 유틸 `NotificationMessage`도 유일한 사용처 `NotificationService`와 함께 옮겼다(02의 `MailVerificationMessage` 선례) |
| 빈 등록 18개 | `infrastructure:persistence`의 `<ctx>/config/<Ctx>DomainConfig`(4앱 전부 스캔) | 서비스 클래스의 `@Service` — 2개 앱 이상이 쓰면 코어, 하나면 그 `{앱}-application`. **빈 이름은 그대로**(클래스명 첫 글자 소문자 = 옛 `@Bean` 메서드명). domain에 남은 순수 계산기·정책(`CupDepositPolicy`·`ProductExposureCalculator`·`ShopDeliveryTipCalculator` 등)은 domain이 spring-free라 애노테이션을 달 수 없어 `shared/config/SharedBeanConfig`의 `@Bean`으로 등록한다 — 클래스는 domain, 등록은 application |
| 이벤트 발행 포트 `DomainEventPublisher` | `domain/shared/event/` | `shared/event/` |
| 구현 `SpringDomainEventPublisher` | persistence `shared/event/`, `@Component` | `shared/event/`, `@Component` 없음, **`shared/config/SharedBeanConfig`의 `@Bean domainEventPublisher`**(인터페이스 타입으로 등록 — 빈 이름이 `springDomainEventPublisher`에서 `domainEventPublisher`가 됐고, 그 이름을 `@Qualifier`로 참조하는 곳은 0건). persistence에 두면 persistence가 `port.out` 밖의 application 타입을 봐야 해 `shouldNotDependOnApiModules`에 걸린다. 구현이 `ApplicationEventPublisher`에 위임하는 한 줄이라 이 모듈(이미 spring-context 보유)이 자연스럽다 |
| `OptimisticLockConflictException` | `domain/shared/exception/` | **`shared/port/out/`** — persistence `ReservationSlotPersistenceAdapter`이 던지고 web-application `ReservationCreateService`가 잡는다. `port/out` 아래여야 03b에서 persistence가 domain 없이 던질 수 있다. `BusinessException`이 아니므로 응답은 여전히 500이고, 재시도 소진 시 `RESERVATION_SLOT_FULL` 409로 번역되는 흐름도 그대로다 |
| 서비스 연결 어댑터 2개(`ShopRequestIndexSyncAdapter`·`ReplyPhraseProhibitedWordValidatorAdapter`) | persistence `product/persistence`·`ceo/persistence`, `@Component` | **`shop/service/`**, 같은 빈 이름. `ReplyPhraseProhibitedWordValidatorAdapter`는 포트와 유일한 소비자가 ceo라 지금 `ceo-application`에 있다. DB 기술이 아니라 서비스 연결부라 persistence에 두면 infra가 application 서비스를 부르게 된다. **포트를 소유한 쪽(product·ceo)이 아니라 구현 대상 서비스가 사는 `shop`에 둔 이유**: product·ceo 쪽에 두면 어댑터가 shop의 `service`를 직접 참조해 `ServiceContextBoundaryTest` 위반이 되고 봉인 목록은 늘릴 수 없다. shop에 두면 "shop이 product·ceo의 포트(`port.out`)를 구현"하는 허용된 방향만 남는다 |
| 금칙어 캐시 데코레이터 `CachingProhibitedWordLoadPort`(분리 전 `CachingProhibitedWordPersistencePort`) | persistence `shop/persistence` | `shop/service/` (순수 자바 TTL 캐시, 빈 아님). `SharedBeanConfig#prohibitedWordValidator`가 `new`로 감싼다 |

**ArchUnit 변경**

| 규칙 | before | after |
|---|---|---|
| `LayerRulesTest#queryServicesShouldNotDependOnWritePorts` | 대상 `com.tastyhouse.domain..repository..` | `com.tastyhouse.application..port.out.write..` — 바꾸지 않으면 대상 패키지가 사라져 **아무것도 검사하지 않고 통과**한다 |
| `RuleAnchorTest#writePortsExist` | 없음 | **신설** — `..port.out.write..`의 write 포트 인터페이스 하한. 위 규칙이 대상을 잃으면 빌드 실패. 지금 대상은 `*LoadPort`·`*SavePort` ≥ 192(아래 "anchor 하한") |
| `LayerRulesTest#readContractsShouldBeFrameworkFree` | `port.out`은 `java`·`domain`·`port.out`만 참조 | 읽기 계약은 `java..`·`port.out..`만(domain 금지). `port.out.write`는 대상에서 빠지고 `writePortsShouldOnlyDependOnDomainAndPortOut`이 따로 검사한다(아래 [읽기 계약 평탄화](#읽기-계약-평탄화--portout은-domain을-모른다)) |
| `ServiceContextBoundaryTest` | 없음 | **신설** — 아래 절 |
| domain `ContextBoundaryTest` | 봉인 위반 14 · 순환 성분 1 | 봉인 위반 **1**(`DeliveryAreaProjection`) · 순환 봉인 **해제**(성분 0개 → `beFreeOfCycles()` 순수 강제) — `domain/AGENTS.md` |
| persistence `LayerRulesTest#sealedPersistenceToQuery` | 봉인 3건 | **3건 유지** — 포트가 application으로 옮겨가도 어댑터(`*Adapter`)는 persistence에 남아 여전히 `..query..`를 본다 |

### `ServiceContextBoundaryTest` — 도메인 서비스 사이의 컨텍스트 경계

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/ServiceContextBoundaryTest.java`

domain `ContextBoundaryTest`가 도메인 서비스에 걸던 컨텍스트 경계를, 서비스가 옮겨온 이 모듈에서 **같은 구조(봉인 목록 + 짝 테스트)**로 이어서 강제한다. 옮기기만 하고 규칙을 따라 옮기지 않으면 경계가 조용히 사라진다.

- **검사 대상**: `com.tastyhouse.application.<ctx>..service..`에 있는 최상위 클래스 중 **구조 조건**(인터페이스 아님, `@Configuration` 아님, `port.in` UseCase 구현 아님)에 맞고 협력 빈 FQN 목록 `EXCLUDED_COLLABORATORS`에 없는 것 — 즉 도메인 서비스와 그 협력 record·유틸(목록의 근거는 아래 "봉인·가드 목록"의 "앱 모듈 경계 가드"). 유스케이스 서비스(`port.in` 구현)는 원래 컨텍스트를 가로질러 조립하는 계층이라 대상이 아니다. 하한 `domainServicesShouldExist` ≥ 71.
- **컨텍스트 판정**: 패키지 두 번째 세그먼트(`com.tastyhouse.{domain|application}.<ctx>.`). `shared`·`exception`·`architecture`는 컨텍스트가 아니다. `application.member.follow`처럼 03a가 domain 경로를 그대로 옮긴 곳은 `member` 컨텍스트이고, 원래부터 이 모듈에 있던 `application.follow`는 별개 컨텍스트다.
- **금지**: 타 컨텍스트의 domain `model`·`service`, application의 `port.out` 밖 전부(`port.out.write`(구 `repository`)·`service`·`port.in`·`listener`·`config` 등). **허용**: domain `vo`·`event`, application `port.out`(write 제외). 대상 서비스의 **중첩 클래스도 검사**하며 위반은 최상위 클래스 이름으로 모아 봉인 목록과 대조한다(domain `ContextBoundaryTest`의 `topLevelNameOf` 선례). 의존 대상도 최상위 클래스로 접어서 센다. 한계: 컴파일 타임 상수(`static final` 원시값·문자열)는 javac가 인라인해 바이트코드 의존이 남지 않으므로 잡히지 않는다(`NotificationMessage` → `ReviewBlindRequest.BLIND_PERIOD_DAYS`) — domain 테스트와 같은 한계다.
- **봉인 위반 15개** — domain에서 옮겨 온 위반 13개와 `MailVerificationService`·`PaymentConfirmationService`만 담는다(`DeliveryAreaProjection`은 domain 봉인에 남는다). ceo 항목은 `ShopRequestCancellationService`다. **목록은 줄어들기만 한다 — 항목을 추가하지 않는다.** 짝 테스트 `sealedViolationsShouldNotBeStale`(해소된 항목 검출)·`sealedViolationListShouldNotBeEmpty`(다 비면 봉인 장치 제거 지시).
- **봉인 순환 성분 1개** `"order,product,review,shop"` — domain에서 봉인되던 바로 그 성분이 서비스와 함께 옮겨왔다(순환을 만들던 간선이 전부 서비스에서 나왔다). 간선은 대상 서비스에서 나가는 의존만으로 계산하고, 비교 단위는 domain 선례대로 **SCC**다. 짝 테스트 `sealedCyclesShouldNotBeStale`.
- `allowEmptyShould(true)`를 쓰지 않는다 — 위반을 손으로 모으는 테스트라 ArchUnit의 빈 `should()` 문제도 없다.

## persistence domain 재허용 — persistence가 도메인 모델을 직접 쓴다

**write 경로는 서비스 → write 포트(도메인 타입) ← persistence `XxxPersistenceAdapter` 한 단계다.** persistence가 도메인 모델을 직접 다루고, JpaEntity ↔ Domain 변환은 persistence `XxxMapper`가 한다. 읽기 경로(읽기 계약 `port/out`의 `*Result`·`*SearchCondition`, persistence 조회 DAO)는 domain을 모른다(아래 [덩어리 03b](#덩어리-03b--읽기-계약은-domain을-모른다)).

```
Service → XxxLoadPort·XxxSavePort(도메인 타입) ← persistence XxxPersistenceAdapter (XxxMapper: JpaEntity ↔ Domain)
```

**write 경로에 번역 계층(State record·StatePort·Store·StateMapper)을 다시 끼우지 않는다.** 애그리거트 하나를 저장하려고 파일 5개를 거치고 변환이 두 번 일어나는 비용이, "persistence가 domain을 모른다"는 격리보다 크다. persistence는 원래 도메인 포트의 어댑터라 domain을 아는 방향이 의존 규칙에 맞고, `implementation` 의존이면 domain이 앱 컴파일 클래스패스로 새지 않아 presentation의 domain 절단도 유지된다. 경위는 [ADR-0031](../../docs/adr/0031-persistence-domain-reallowed.md).

**엔티티의 `String` 컬럼과 `*Embeddable` 5종은 유지한다**: 조회 DAO 31개 파일이 String 컬럼을 193곳에서 쓴다. enum으로 되돌리면 `Projections.constructor` 투영이 **컴파일은 통과하고 런타임에만** 깨진다. 그래서 String ↔ enum(`valueOf`/`name()`) 변환은 persistence `XxxMapper`의 몫이다.

### 한 컨텍스트의 파일 구성 — `notice`

| 역할 | 파일 |
|---|---|
| 서비스가 주입하는 도메인 타입 write 포트 | `backend/application-core/src/main/java/com/tastyhouse/application/notice/port/out/write/{NoticeLoadPort,NoticeSavePort}.java` |
| persistence 구현 | `backend/infrastructure/jpa/src/main/java/com/tastyhouse/infrastructure/jpa/notice/persistence/NoticeJpaPersistenceAdapter.java`(`@Repository`, `@Primary`, 두 포트를 구현하고 도메인을 반환). notice는 같은 포트를 MyBatis 구현도 가진다(`../infrastructure/mybatis/AGENTS.md`) — 구현이 하나인 기본형은 `banner/persistence/BannerPersistenceAdapter` |
| persistence 매퍼 | `.../notice/persistence/NoticeJpaMapper.java` — JpaEntity ↔ `Notice` |
| 매퍼 테스트 | `backend/infrastructure/jpa/src/test/java/com/tastyhouse/infrastructure/jpa/<ctx>/persistence/XxxMapperTest.java` — Domain→Entity / Entity→Domain **두 방향 따로**(아래) |

`port/out/write/`에는 도메인 타입 write 포트(`*LoadPort`·`*SavePort`)와 `ShopDeliveryTipRegionLoadPort`, 원시 타입만 쓰는 `StationLoadPort`·`AdminDongSyncResult`가 있다. application에는 write 포트 구현을 두지 않는다.

### write 어댑터 작성 규칙

- **`XxxLoadPort`(조회)·`XxxSavePort`(변경)는 `<ctx>/port/out/write/`에 둔다**(분리 전에는 `XxxPersistencePort` 하나, 2026-10-09 Load/Save 분리). 시그니처는 도메인 타입(`Optional<Notice> findById(NoticeId)`)이고, 구현은 persistence `<ctx>/persistence/XxxPersistenceAdapter`가 두 포트를 함께 구현한다(`@Repository`, 4앱 부트스트랩 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔으로 4앱 전부에 뜬다)이다. 이 모듈에는 구현을 두지 않는다.
- **write 포트는 `java..`·`com.tastyhouse.domain..`·`com.tastyhouse.application..port.out..`만 의존한다** — `LayerRulesTest#writePortsShouldOnlyDependOnDomainAndPortOut`. 서비스·UseCase·설정을 참조하면 persistence가 그 타입까지 봐야 해 `shouldNotDependOnApiModules`에 걸린다.
- **변환은 persistence `XxxMapper`가 한다** — `toDomain(entity)`(`reconstitute` 호출)·`toEntity(domain)`·`applyChanges(entity, domain)`. enum은 `valueOf`/`name()`, ID·단일값 VO는 `Xxx.of(...)`/`.value()`, 복합 VO는 persistence 소유 `*Embeddable` 또는 평탄 컬럼으로 바꾼다. nullable enum·VO·FK는 `x == null ? null : ...` 삼항 가드를 **모든 FK에 예외 없이** 둔다(`backend/CLAUDE.md` "ID VO 경계 규칙").
- **도메인 정책을 원시값으로 푸는 일은 PersistenceAdapter가 한다** — 빈 컬렉션 조기 반환, `LinkedHashSet` 수집, 도메인 정책 상수 호출(예: `backend/infrastructure/jpa/src/main/java/com/tastyhouse/infrastructure/jpa/reservation/persistence/ReservationPersistenceAdapter.java`가 `ReservationStatus.blockingStatuses()`를 `name()` 목록으로 풀어 쿼리에 넘긴다). 인터페이스 여럿을 함께 구현할 수 있다(`ShopDeliveryTipPersistenceAdapter`가 배달팁 Load·Save 포트와 `ShopDeliveryTipRegionLoadPort`를 구현).
- **write 어댑터 빈 등록 설정은 없다** — persistence `@Repository` 스캔으로 4앱 전부에 뜬다. 그래서 web 전용인 `MailVerificationPersistencePort`·`SmsVerificationPersistencePort` 구현(분리 후 Load·Save 포트)도 전 앱에 뜨고, admin·ceo·batch에는 주입처가 없다.
- **서비스 테스트의 fake는 도메인 타입 write 포트를 구현한다** — 예: `FakeMailVerificationPersistence`(두 포트를 함께 구현하면 접미어 `Port`를 뗀다, `backend/CLAUDE.md`의 아웃바운드 포트·어댑터 네이밍 규칙).

### 매퍼 테스트는 두 방향으로 나눠 검증한다

도메인 → 엔티티 → 도메인 왕복 비교 하나로는 부족하다. Entity를 거치면 원본의 일부 값이 사라진다 — `XxxJpaEntity.create(...)`는 id를 받지 않고, `BaseEntity`의 `createdAt`/`updatedAt`은 auditing이 채우며 setter가 없다. 그래서 persistence `XxxMapperTest`는 다음처럼 검증한다.

- (a) Domain → Entity: `toEntity` 결과의 컬럼 값(enum `name()`, VO `value()`, Embeddable 필드)을 필드별로 단언한다.
- (b) Entity → Domain: `ReflectionTestUtils.setField`로 id·`createdAt`·`updatedAt`을 채운 엔티티를 만들고 `toDomain` 결과를 단언한다.
- 왕복 비교를 남긴다면 제외 필드는 `ignoringFields("id", "createdAt", "updatedAt")`만 허용한다. 다른 필드를 제외해 테스트를 통과시키지 않는다.
- null 가드 케이스(enum·VO가 null)는 두 방향 모두 유지하고, **같은 타입의 연속 필드는 서로 다른 값으로 채운다**(아래 봉인 항목).

### 가드

| 규칙·테스트 | 지키는 것 |
|---|---|
| persistence `LayerRulesTest#queryShouldNotDependOnDomain` | `..query..` + 봉인 조회 어댑터 3개(`SEALED_PERSISTENCE_TO_QUERY`)는 domain을 모른다 — persistence 모듈 자체는 domain을 의존하므로 빌드 그래프가 막아 주지 않는다(`../infrastructure/jpa/AGENTS.md`) |
| `LayerRulesTest#readContractsShouldBeFrameworkFree` | 읽기 계약(`port.out`에서 `..port.out.write..` 제외)은 `java..`·`port.out..`만 의존 |
| `LayerRulesTest#writePortsShouldOnlyDependOnDomainAndPortOut` | `port.out.write`는 `java..`·`com.tastyhouse.domain..`·`application..port.out..`만 |
| `LayerRulesTest#queryServicesShouldNotDependOnWritePorts` | 조회 유스케이스 서비스는 `..port.out.write..`를 주입하지 않는다 |
| `RuleAnchorTest#writePortsExist` | `port.out.write`의 `*LoadPort`·`*SavePort` ≥ 192 — 위 규칙이 대상을 잃지 않게 한다 |
| persistence `<ctx>/persistence/XxxMapperTest` | 위 두 방향 매퍼 검증 |

## application `*ServiceConfig`를 두지 않는다 — 도메인 서비스는 `@Service`로 스캔 등록

**도메인 서비스는 클래스에 `@Service`를 달고, 그것이 사는 모듈(쓰는 앱이 둘 이상이면 코어, 하나면 그 `{앱}-application`)의 스캔으로 등록된다.** 서비스를 `new`로 만들어 `@Bean`으로 등록하는 `<ctx>/config/*ServiceConfig`·`PgRouterConfig` 같은 설정은 없다. 경위는 [ADR-0034](../../docs/adr/0034-service-config-removal.md)·[ADR-0036](../../docs/adr/0036-app-marker-removal.md).

**용어 풀이**

- **도메인 서비스**: `<ctx>/service/`에 있으면서 `port.in` UseCase를 구현하지 않는 서비스. 트랜잭션을 열지 않고(`@Transactional` 없음) 여러 애그리거트·포트를 묶어 도메인 불변식을 지킨다.
- **유스케이스 서비스**: `port.in` UseCase를 정확히 1개 구현하는 서비스(batch는 `*SchedulerService`). 트랜잭션 경계를 갖는다.
- **소비 앱 집합**: 생성자 주입을 따라 올라가 "최종적으로 이 빈을 쓰는 앱"의 집합. 모듈을 정하는 기준이다(아래 "코드 주석에서 이관된 설계 근거"의 "도메인 서비스를 둘 모듈은 소비 앱 집합으로 정한다" 절).

**config로 등록하지 않는 이유**

- 서비스 하나를 추가할 때 파일 두 곳(클래스 + config의 `@Bean`)을 고쳐야 한다.
- 4앱 공통 config가 등록하면 **한 앱만 쓰는 빈도 4앱 전부에 뜬다**(주입처가 없는 빈이 컨텍스트에 상주).
- 스캔과 `@Bean`이 같은 클래스를 함께 등록하면 기동이 실패한다 — `LayerRulesTest#configurationsShouldNotRegisterStereotypedClasses`가 막는다.

```java
// web-application/.../bug/service/BugReportRegistrationService.java — config 없음, web만 쓰므로 web-application
@Service
class BugReportRegistrationService { ... }
```

**`shared/config/SharedBeanConfig`(코어)만 `@Bean`을 갖는다** — 클래스에 애노테이션을 달 수 없거나 특수 조립이 필요한 빈 10개다.

| 빈 | 이유 |
|---|---|
| `domainEventPublisher` | 인터페이스 타입으로 등록 |
| domain 계산기·정책 7개(`productExposureCalculator`·`cupDepositPolicy`·`storePriceBadgePolicy`·`shopOperatingStatusCalculator`·`shopDeliveryTipCalculator`·`shopNextOpenTimeCalculator`·`scheduledOrderSlotCalculator`) | domain이 spring-free라 애노테이션을 달 수 없다 |
| `shopDeliveryTipRangePolicy` | 상수·람다로 조립하는 record |
| `prohibitedWordValidator` | `CachingProhibitedWordLoadPort`로 감싸 조립 |

- **새 도메인 서비스를 `@Bean`으로 등록하지 않는다·`*ServiceConfig`를 되살리지 않는다.** `grep -rl '@Bean' application-core/src/main/java`는 `SharedBeanConfig` 1개만 반환해야 한다.
- **도메인 서비스가 `@Service`를 달아도 `ServiceContextBoundaryTest`는 대상을 잃지 않는다** — 술어가 스테레오타입이 아니라 "구조 조건 − `EXCLUDED_COLLABORATORS`"이기 때문이다(아래 "봉인·가드 목록"의 "앱 모듈 경계 가드").
- **알려진 사각지대**: `List<I>` 주입(`PgPaymentGatewayRouter`)은 구현체가 0개여도 기동되므로 규칙이 잡지 못한다. 4앱 기동 확인은 `contextLoads`가 빈 껍데기라 대신할 수 없다.

## 덩어리 03b — 읽기 계약은 domain을 모른다

**읽기 계약(`port/out`의 `{Ctx}QueryPort`·`*Result`·`*SearchCondition`·스펙 record)과 persistence 조회 DAO는 domain 타입을 모른다.** enum은 `String`, ID는 `Long`으로 싣고, 승격·강등·도메인 판정은 QueryService가 한다. write 경로는 도메인 타입을 쓴다(위 [persistence domain 재허용](#persistence-domain-재허용--persistence가-도메인-모델을-직접-쓴다)). 경위는 [ADR-0028](../../docs/adr/0028-persistence-state-layer.md)·[ADR-0031](../../docs/adr/0031-persistence-domain-reallowed.md).

### 읽기 계약 평탄화 — `port/out`은 domain을 모른다

| 항목 | 규칙 |
|---|---|
| `readContractsShouldBeFrameworkFree` | 읽기 계약은 **`java..`·`port.out`만** 의존한다(domain 금지, `port.out.write`는 대상 밖) |
| `*SearchCondition`·`{Ctx}QueryPort` 파라미터의 enum·ID | `String`·`Long`. QueryService가 `OrderStatus.from(orderStatus).name()`·`OrderId.of(id).value()`로 **검증 후 강등**해 넘긴다(잘못된 입력은 400) |
| `*Result`의 enum 라벨(`{field}Description`/`{field}DisplayName`) | **QueryService가 채운다** — DAO는 `Expressions.nullExpression(String.class)`로 자리만 두고, QueryService가 `XxxEnum.valueOf(result.status()).getDescription()`을 Result wither(`withDescriptions(...)` 등)에 넘긴다. 참고: ceo-application `shop/service/ShopChangeHistoryListQueryService` → `shop/port/out/ShopChangeHistoryResult#withDescriptions`, ceo-application `ceo/service/CeoLoginHistoryListQueryService` → `ceo/port/out/CeoLoginHistoryResult` |
| DAO가 enum 상수와 비교 | `.eq(status)` — 비교값을 **포트 인자**로 받고, application(QueryService)이 도메인 enum의 `name()`으로 넘긴다(아래 "enum 비교값 전달 규칙") |

**enum 비교값 전달 규칙** — **조회 DAO는 enum 어휘를 전혀 모른다.** 비교할 값은 application이 도메인 enum의 `name()`으로 만들어 포트 인자로 넘긴다. `port.out`에 도메인 enum을 복제한 문자열 상수 클래스(`XxxCodes`)를 두지 않는다 — 복제본은 도메인 enum과 두 벌이 돼 일치 검사가 따로 필요하고, DAO가 쓰지 않는 상수마다 미사용 경고가 나 억제 어노테이션이 붙으며(`backend/CLAUDE.md`의 `@SuppressWarnings` 지양 규칙), "판매 완료 주문만 센다"·"탈퇴 회원 제외" 같은 **도메인 정책이 persistence에 박힌다.** 경위는 [ADR-0029](../../docs/adr/0029-no-suppress-warnings.md).

| 형태 | 언제 | 예 |
|---|---|---|
| `String`/`Collection<String>` 포트 인자 | 비교·필터 값 | `MemberQueryPort#existsByPhoneNumberAndStatusNot(phoneNumber, excludedStatus)` ← `MemberPhoneAvailabilityQueryService`가 `MemberStatus.DELETED.name()`. |
| 스펙 record 포트 인자 | DAO가 값에 따라 **쿼리 모양을 바꾸는** 곳 | `review/port/out/ReviewSortSpec(byLikeCount, createdAtAscending)` ← `review/service/ReviewSortSpecs.of(ReviewSortType)`, `review/port/out/ShopReviewTabFilter` ← `ShopReviewTabFilters.of(ReviewListTab)`, `product/port/out/ProductExposureWindow(now, todayDayTypes, previousDayDayTypes)` ← `product/service/ProductExposureWindows.now()`/`at(LocalDateTime)`(`DayType#appliesTo(dow, false)` — 공휴일 미판정은 과거 DAO 동작 그대로) |
| 기존 정책 record 컴포넌트 | 요청마다 달라지지 않는 고정값 | `shop/port/out/ShopDeliveryTipRangePolicy`의 `distanceExtraTipType`·`regionExtraTipType` ← `shared/config/SharedBeanConfig#shopDeliveryTipRangePolicy`(구 `ShopServiceConfig`, `DeliveryTipExtraType.DISTANCE/REGION.name()`) |

- **스펙 record를 만드는 유틸은 `<ctx>/service/`의 final class이며, 도메인 enum → 스펙 매핑은 exhaustive switch로 쓴다.** 도메인 상수가 추가되면 컴파일 에러로 드러난다.
- **그 유틸은 자기 컨텍스트(또는 `domain.shared`) 타입만 import한다.** `ServiceContextBoundaryTest`는 `..service..`의 `port.in`을 구현하지 않는 클래스를 도메인 서비스로 보고 타 컨텍스트 `model` 참조를 막는다. 그래서 인기상품 판매 집계의 `OrderStatus.COMPLETED.name()`은 `ProductExposureWindows`가 아니라 `ProductPopularQueryService#findPopularProducts`에서 만든다.
- **write 포트는 도메인 타입 시그니처이고, enum → 원시값 변환은 persistence 쓰기 어댑터 안에 있다**(예: `MailVerificationPersistenceAdapter`가 상태 변경을 `name()`으로 풀어 쿼리에 넘긴다). 그래서 도메인 타입 fake(`FakeMailVerificationPersistence` 등)는 이 변환에 영향을 받지 않는다.
- **`port.out`에 도메인 enum의 복제본을 두지 않는다.** `EnumCodeConstantsTest#portOutShouldNotMirrorDomainEnums`가 `port.out`의 모든 enum을 도메인 enum과 **상수 집합**으로 대조해 막는다. 이름과 무관하게 잡는다. 허용 목록은 값 자체가 벤더 계약인 `PgProviderCode` 하나다. 과거 형태(`public static final String X = "X"` 상수 클래스)의 재발은 `#portOutShouldNotDeclareDomainEnumConstantStrings`가 막는다(도메인 enum 상수명과 같은 이름의 `static final String` 필드 금지).
- **`ShopReviewTabFilter`는 조건을 하나만 켤 수 있다.** compact constructor가 둘 이상이면 `IllegalArgumentException`으로 거부한다. DAO가 앞선 조건만 조용히 적용하는 것을 막는다.
- 리뷰 정렬 fallback이 DB에서 읽은 문자열이면 `ReviewSortType.valueOf`로 승격한다. 알 수 없는 값이면 `IllegalArgumentException`(500)이 나는데, 과거 DAO의 `IllegalStateException`(500)과 응답이 같다.

**함정** — `Map<Enum, X>.get(string)`·`Set<Enum>.contains(string)`·`Objects.equals(enum, string)`은 컴파일되고 항상 `null`/`false`다. 파라미터를 `String`으로 바꾼 뒤에는 그 값을 소비하는 모든 곳을 읽는다(아래 "enum → `String` 강등 후 Object 타입 API" 절).

### persistence에서 이 모듈로 올라온 도메인 판단

조회 DAO는 domain을 볼 수 없으므로(`queryShouldNotDependOnDomain`), 조회 경로의 도메인 정책 호출과 도메인 예외는 이 계층이 맡는다. **DAO는 원자료만 돌려주고, 판정·계산·예외화는 QueryService가 한다.** write 쪽 판단(예: 예약 차단 상태)은 persistence `XxxPersistenceAdapter`가 도메인 타입으로 한다.

| 판단 | 과거 위치 (persistence 조회 DAO) | 현재 위치 (이 계층) |
|---|---|---|
| 컵 보증금 금액 `CupDepositPolicy#depositAmountOf(cupCount)` | `product/query/ProductQueryAdapter`가 주입받아 투영 중 계산 | DAO는 `cupCount`만 싣고, `product/service/ProductOptionDepositAmounts`(package-private 유틸)가 web `ProductOptionsQueryService`·`ProductBatchQueryService`·admin `ProductOptionManagementListQueryService`에서 채운다 |
| 에디터 추천 가게당 상품 수 `EditorChoicePolicy.PRODUCT_LIMIT` | `shop/query/ShopChoiceQueryAdapter`가 상수 직접 참조 | `ShopChoiceQueryPort#findEditorChoices(PageQuery, int productLimit)` 파라미터 — web `ShopEditorChoiceQueryService`·admin `ShopChoiceListManagementQueryService`가 상수를 넘긴다 |
| 배달팁 표기 상한·거리 단위 `DeliveryTipPolicy.EXTRA_TIP_UPPER_BOUND`·`DeliveryTipDistanceUnit#getUnitMeters` | `shop/query/ShopDeliveryTipQueryAdapter`가 domain 상수·enum 직접 참조 | **값 record `shop/port/out/ShopDeliveryTipRangePolicy`**(상한 + 단위명→미터 맵)를 `SharedBeanConfig#shopDeliveryTipRangePolicy`(구 `ShopServiceConfig`)가 domain 값으로 만들어 `@Bean` 등록하고, DAO가 그 빈을 주입받는다 — DAO가 domain 없이 같은 값을 쓰는 형태 |
| 예약 차단 상태 `ReservationStatus.blockingStatuses()` | `reservation/query/ReservationQueryAdapter`가 직접 참조 | `ReservationAvailabilityQueryService`가 `name()` 목록으로 만들어 파라미터로 넘긴다(write 쪽은 persistence `ReservationPersistenceAdapter`가 직접 만든다) |
| 가게 위치 조회 실패 `SHOP_ACCESS_DENIED`·좌표 미등록 `SHOP_DELIVERY_AREA_RADIUS_EXCEEDED` | `shop/query/ShopDeliveryAreaQueryAdapter#findShopLocation`이 `BusinessException` | DAO는 `Optional<ShopLocationResult>`를 돌려주고, 위치 없음(`SHOP_ACCESS_DENIED`)은 호출하는 `ShopDeliveryAreaPolygonDetailQueryService`·`ShopDeliveryAreaPolygonPreviewQueryService`·`ShopDeliveryAreaRadiusQueryService`가 `orElseThrow`로, 좌표 미등록은 `shop/service/ShopDeliveryAreaGeoMapper#requireCoordinates`가 같은 코드·문구로 던진다(응답 불변) |
| 폴리곤·행정동 경계 디코딩 | persistence `shared/query/GeoRingsResolver`(`GeoRingsQueryPort` 구현) | QueryService가 `domain/shared/geo/GeoPolygonTextCodec.decodeRings`를 직접 호출(ceo-application `region/service/AdminDongBoundaryQueryService`·`shop/service/ShopDeliveryAreaPolygonDetailQueryService`·`ShopDeliveryAreaPolygonPreviewQueryService`). `GeoRingsQueryPort`·`GeoRingsResolver`는 삭제 |
| 가게 매장가 인증 플래그 어댑터 `StorePriceVerificationAdapter` | persistence `@Component` | **`shop/service/StorePriceVerificationAdapter`**(코어, `@Service` 스캔 등록) — `ShopLoadPort`·`ShopSavePort`(도메인 `Shop`)와 `ResourceNotFoundException`을 쓰므로 |

**DAO에 도메인 상수가 필요해 보이면** ① 호출부가 파라미터로 넘기거나(`productLimit`·`blockingStatuses`), ② 값 record를 `port/out`에 두고 `shared/config/SharedBeanConfig`가 domain 값으로 `@Bean` 등록한다(`ShopDeliveryTipRangePolicy`). 조회 DAO에 domain을 들이지 않는다(persistence 모듈 자체는 domain을 의존하지만 `..query..`는 `queryShouldNotDependOnDomain`이 막는다).

## 패키지 구조 (챕터 03으로 평탄화 — 도메인 아래에 앱별 폴더가 없다)

아래 트리는 **5모듈을 합친 패키지 모양**이다. `port/in/`·유스케이스 서비스·앱 전용 도메인 서비스·앱 전용 SPI 포트(`SocialOAuthClientPort`·`MailSenderPort`·`SmsSenderPort`·`BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort` 등)는 각 `{앱}-application` 모듈에 있고, 나머지(`shared/**`·공유 도메인 서비스·`port/out` 계약·`listener/`)는 코어 `application-core`에 있다(위 [앱 모듈 재분리](#앱-모듈-재분리--앱-소속은-gradle-모듈이-표현한다)).

```
com.tastyhouse.application/
  │   (루트에는 클래스가 없다 — 스캔은 각 앱 부트스트랩의 중첩 ApplicationLayerScanConfig가 소유한다)
  ├── shared/exception/BatchJobException.java
  ├── shared/exception/{ApplicationErrorCode,ApplicationErrorCodeSpec,ApplicationException,ResourceNotFoundException}.java   코어 소유 코드·앱 계층 예외. split package: `{Web,Admin,Ceo,Batch}ErrorCode`는 각 앱 모듈이 같은 패키지에 둔다
  ├── shared/error/                 표현 계층용 에러 판정 — 빈 아님, 정적 유틸
  │     ├── ErrorDescriptor.java    record(int status, String code, String message)
  │     └── ErrorResponses.java     resolve(Throwable) → Optional<ErrorDescriptor> (최상위가 BusinessException일 때만)
  ├── shared/port/out/CodeLabelResult.java   record(String code, String label) — enum 카탈로그 응답용
  ├── shared/port/out/page/{PageQuery,PageResult}.java
  ├── shared/port/out/OptimisticLockConflictException.java
  ├── shared/event/{DomainEventPublisher,SpringDomainEventPublisher}.java
  ├── shared/config/SharedBeanConfig.java    domainEventPublisher + domain 계산기 7 + shopDeliveryTipRangePolicy + prohibitedWordValidator. @Bean 보유 클래스는 이것 하나
  ├── shared/config/AsyncConfig.java         @EnableAsync — @Async 리스너가 코어에 있어 4앱 공통
  └── <ctx>/
      ├── port/in/                UseCase 인터페이스(추상 메서드 1개) + Command record
      ├── service/                유스케이스 서비스 {도메인}{동작}Service / {도메인}{관점}QueryService(batch는 *SchedulerService),
      │                           implements 포트 정확히 1개
      │                           + 도메인 서비스(포트를 주입받는 서비스) — 둘 다 @Service
      ├── port/out/               이 도메인의 모든 아웃바운드 계약 —
      │                           읽기 계약({Ctx}QueryPort·*Result·*SearchCondition) +
      │                           아웃바운드 SPI(SocialOAuthClientPort 등) + Command 경로 반환 Result/View
      │   │                       + persistence가 쿼리 모양을 바꿀 때 받는 스펙 record(ReviewSortSpec 등 — 도메인 enum 복제본은 두지 않는다)
      │   │                       — Result의 enum 필드는 String(+ {field}Description/DisplayName)
      │   └── write/              write 포트 XxxLoadPort·XxxSavePort. 시그니처는 domain 타입, 구현은 persistence XxxPersistenceAdapter
      └── listener/               도메인 이벤트 리스너(@Component + @TransactionalEventListener(AFTER_COMMIT)) — 코어에만, 10개 컨텍스트에 12종
```

패키지만 봐서는 어느 앱 것인지 알 수 없다 — 클래스가 들어 있는 모듈이 소속을 정한다(위 [패키지 평탄화](#패키지-평탄화-챕터-03)). 컨텍스트별 규모는 앱마다 다르다.

- **web** 컨텍스트 27종: `auth` · `banner` · `bug` · `coupon` · `emailverification` · `event` · `faq` · `follow` · `grade` · `member` · `menureview` · `notice` · `notification` · `order` · `partnership` · `payment` · `phoneverification` · `point` · `policy` · `product` · `rank` · `referral` · `reservation` · `review` · `search` · `shop`.
- **admin** 컨텍스트 19종: `admin` · `auth` · `banner` · `bug` · `ceo` · `coupon` · `event` · `faq` · `file` · `member` · `notice` · `order` · `partnership` · `point` · `policy` · `product` · `rank` · `review` · `shop`.
- **ceo** 컨텍스트 6종: `auth` · `ceo` · `product` · `region` · `review` · `shop`. **컨텍스트 수는 가장 적은데 유스케이스 서비스 수는 많다**(명령 120 · 조회 68 = 188개) — 점주 셀프서비스가 `shop` 하나에 설정 관심사를 대량으로 갖기 때문이다(`ShopBusinessHour*`/`ShopClosedDay*`/`ShopStatus*`/`ShopDeliveryTip*` 등).
- **batch** 잡 슬러그 8종: `grade` · `payment` · `product` · `productsoldout` · `rank` · `region` · `reviewblind` · `search`. 추가로 `crawling/bbq/`(BBQ 크롤링 동기화 + `port/out`에 포트 2종 `BbqMenuPort`·`RemoteImagePort`, 응답 record 4종, 다운로드 결과 record `DownloadedImage`)와 `exception/BatchJobException`이 있다. 잡별 UseCase·트리거 대응표는 `batch-module/AGENTS.md`에 있다.

대형 컨텍스트는 관심사 단위로 서비스를 더 쪼갠다 — 이 관례는 모듈 통합 전과 동일하다.

### 앱 간 동명 클래스는 정상이다

**앱별로 같은 역할의 타입이 따로 존재하는 것은 의도된 중복이다** — 소비자가 다르면 조회 범위·응답 형태가 다르고, 인증은 주체(`Member`·`Admin`·`Ceo`)·ErrorCode·`JWT_SECRET_*`가 앱별로 분리돼 있다. 통합하지 않는다.

**다만 이름까지 같게 두지는 않는다.** 세 앱의 타입이 같은 패키지에 공존하므로 simple name이 앱 간에도 유일해야 한다. `NoticeListQueryService`(web) / `NoticeManagementListQueryService`(admin), `ShopDetailQueryService`(web) / `ShopListManagementQueryService`(admin) / `ShopOwnerListQueryService`·`ShopOwnerDetailQueryService`(ceo), `MemberTokenService` / `AdminTokenService` / `CeoTokenService`처럼 **web은 순수명, admin은 `Management`, ceo는 `Owner`**(인증 타입은 주체명 접두)로 구별한다.

공유되는 것은 `domain`의 도메인 모델·write 포트·도메인 서비스와, 이 모듈 안에서 여러 앱이 함께 쓰는 `{Ctx}QueryPort` 계약이다(write 포트는 이 모듈의 `port/out/write/XxxLoadPort`·`XxxSavePort`, 공유 도메인 서비스는 `<ctx>/service/`에 있다). 그 시그니처를 바꿀 때는 소비 앱 전체를 함께 확인한다.

**앱 간 타입명 충돌 시 `Management`/`Owner` 한정어**를 상시 적용한다 — `Result`·`QueryPort`뿐 아니라 `*UseCase`·`*Service`·`*Command`·협력 빈(`*Reader`·`*View`)까지가 대상이다(규칙 전문과 한정어 삽입 위치는 루트 `backend/CLAUDE.md` 참고). 동명 클래스 **182건의 일괄 개명은 챕터 02에서 완료**했다.

### 읽기 계약을 이 모듈이 소유한다

**읽기 계약(`{Ctx}QueryPort`·`*Result`·`*SearchCondition`)은 전부 이 모듈에 있다** — `src/main/java/com/tastyhouse/application/<ctx>/port/out/`이다. 통합 전 4개 모듈이 나눠 갖던 271개를 같은 트리로 합쳤고(챕터 01, 파일명 충돌 0건), 챕터 04에서 `domain`이 갖고 있던 다중 앱 공유 계약 55개까지 돌아왔다.

**읽기 계약은 코어 한 모듈에만 둔다.** 자바 패키지 `com.tastyhouse.application`은 5모듈이 나눠 쓰지만(split package), 읽기 계약은 앱 모듈에 두지 않으므로 앱 모듈 사이에 계약을 공유하려고 서로를 의존할 일이 없다. 같은 FQCN을 두 모듈에 만드는 것은 `SplitPackageUniquenessTest`가 막는다.

- 구현은 `infrastructure:jpa`(jpa 모듈 분리 전 `infrastructure:persistence`)의 `<ctx>/query/` DAO다. 그 모듈이 `implementation project(':application-core')`으로 이 계약들을 본다.
- **새 읽기 계약은 소비 앱 수를 따지지 않고 이 모듈에 둔다.** 소비 앱이 하나든 셋이든 자리가 같다 — 소유 모듈을 판정하던 절차는 챕터 04와 함께 폐기됐다.
- **프레임워크-프리를 `LayerRulesTest#readContractsShouldBeFrameworkFree`가 지킨다**: 이 모듈은 spring starter를 받으므로 컴파일 게이트가 없다. 읽기 계약이 참조해도 되는 것은 `java..`와 `com.tastyhouse.application..port.out..`뿐이다(domain 금지). `port.out`은 infrastructure가 보는 유일한 application 표면이라, 읽기 계약에 domain 타입이 실리면 조회 DAO가 domain을 알게 된다. enum은 `String`, ID는 `Long`으로 싣고 승격·강등은 QueryService가 한다(위 "덩어리 03b" 절). `port.out.write`(도메인 타입 write 포트)는 이 규칙의 대상이 아니고, `LayerRulesTest#writePortsShouldOnlyDependOnDomainAndPortOut`이 `java..`·`com.tastyhouse.domain..`·`application..port.out..`만 허용한다.

## `response/`는 각 api 모듈로 승격됐다 (챕터 06 · 09 · 10)

**Response record는 api 모듈에 있고, 조립은 Response 자신(`from(XxxResult)`)이 한다**(admin 85 · ceo 105 · web 131, 총 **321개**). 유스케이스는 프레임워크-프리 `*Result`·`PageResult`를 반환하므로 application이 api 패키지를 역참조하지 않는다(`applicationMustNotDependOnAdapters`). 그 결과 이 모듈의 `io.swagger` import와 `com.tastyhouse.apicommon` 참조는 **0건**이며, `applicationShouldNotDependOnSwagger`·`applicationShouldNotDependOnApiCommon`이 그 상태를 고정한다.

**`request/`도 api 모듈에 있다** — Request → Command 매핑은 인바운드 어댑터의 책임이며(완전 매핑 전략), 컨트롤러가 `request.toCommand(...)`로 조립해 넘긴다.

### 표현 계약이 만들 수 없는 값은 이 모듈이 `*View`/`*ViewResult`로 넘긴다

승격 후에도 **application에 남아야 하는 조립**이 있다. 표현 계약(api 모듈)은 도메인 모델·도메인 서비스·아웃바운드 포트를 알 수 없고 시계도 읽지 않아야 하므로, 아래는 이 모듈이 계산해 결과만 넘긴다.

| 남는 이유 | 예 |
|---|---|
| 읽기 포트가 아예 없는 파생(도메인 enum 상수에서 생성) | `GradeInfoResult`(`MemberGrade.values()`) |
| 여러 읽기 포트를 합친 결과 | `PointHistoryViewResult`·`ShopInfoViewResult`(2포트 6쿼리)·`ShopImageStatusResult`·`ProductNutritionViewResult` |
| 도메인 서비스·정책 판정이 필요한 값 | `ShopDetailViewResult`(`ShopOperatingStatusService`)·`ShopPriceBadgeViewResult`(`StorePriceBadgePolicy`)·`ReservationSlotAvailabilityResult`(`SlotPolicy`+시계) |
| 도메인 enum의 **비-accessor 호출**이 필요한 값 | `PaymentCancelResult`(`getMessage()`)·`ProductNutritionView`(`AllergenType.from`)·`ShopRequestListItemViewResult`(`isContractAmending`) |
| 금액 VO 언랩 | `PaymentViewResult`·`PaymentRefundViewResult`(`Money#value()`) |
| 시계 의존 파생 | `MyCouponListItemResult`(`daysRemaining`·`expired`)·`ShopReviewReplyWindow` |
| 다른 컨텍스트에 물어본 값 | `OrderProductViewResult`(`reviewed` — 리뷰 배치 조회로 N+1 회피) |
| 판별 유니온(분기 판정이 도메인 규칙) | `SocialLoginResult`·`SocialLinkResult`·`PhoneLoginResult`(web auth) |
| 도메인 enum `switch` | `ShopReviewSortTypeView` — api 모듈에서 enum을 `switch`하면 바이트코드가 `ordinal()`·`values()`를 호출해 `apiModuleShouldOnlyReadDomainEnums`에 걸린다 *(그 규칙은 덩어리 01로 삭제됐지만 — 지금은 api 모듈 클래스패스에 domain이 없어 `switch` 자체가 컴파일되지 않는다 — 분기를 이 모듈이 맡는다는 결론과 View는 그대로 유효하다)* |

**중첩 `Status` enum은 Result로 함께 복제하되 상수명을 바꾸지 않는다** — 상수 이름이 그대로 JSON 값이라 이름을 바꾸면 API가 바뀐다(`SocialLoginResult.Status`).

**`@JsonInclude`·`@JsonFormat` 같은 jackson 직렬화 어노테이션은 Response 쪽에만 둔다** — 직렬화는 api 모듈에서 일어나므로 Result로 옮기면 무의미해지고, `@JsonFormat` 소실은 날짜 포맷이 조용히 바뀌어 프론트 파싱을 깬다(챕터 10 실측 8인스턴스/6파일).

**반대로 순수 표현 파생은 Response로 내렸다** — 16자리 리뷰번호 0-pad, 문구 표시명 truncate(`CeoReplyPhraseResponse`), 거리별 배달팁 비움 판정. enum → 문자열 강등은 Response가 아니라 Result를 채우는 DAO 투영(`x.status.stringValue()`) 또는 QueryService가 하고, Response는 이미 문자열인 값을 쓴다.

### Command 경로의 반환 Result도 코어 `<ctx>/port/out`에 둔다

명령 유스케이스 서비스가 반환하는 Result/View record(`shop/port/out/ShopDeliveryAreaBulkResult`·`product/port/out/ProductAvailabilityChangeView`)도 읽기 계약과 같은 코어 `<ctx>/port/out`에 있다. `commandServicesShouldNotDependOnQueryPorts`(CQRS 교차 주입 금지)는 금지 대상을 패키지가 아니라 이름(`*QueryPort`·`*QueryService`·`*QueryUseCase`)으로 고르므로 이 record를 반환하는 명령 서비스가 걸리지 않는다(위 [패키지 평탄화](#패키지-평탄화-챕터-03)).

## 앱별 auth 처리

| 앱 | 인증 방식 | application 계층에 있는 것 | api 모듈에 남은 것 |
|---|---|---|---|
| web | 소셜 로그인 SPI + JWT | `web-application`: `auth/token/MemberTokenService` · `auth/security/{CustomUserDetailsService,MemberUserDetails}` · 로그인 유스케이스 서비스(`auth/service/MemberLoginService` 등) | `SecurityConfig` · `PublicPaths` (필터 빈은 `security-module`의 `SecurityModuleConfig`가 등록) |
| admin | `spring-security-core` + JWT | `admin-application`: `auth/token/AdminTokenService` · `auth/security/{AdminUserDetailsService,AdminUserDetails}` | 위 (토큰 저장소 키 접두사는 `security.token-store.key-prefix` 프로퍼티) |
| ceo | `spring-security-core` + JWT | `ceo-application`: `auth/token/CeoTokenService` · `auth/security/{CeoUserDetailsService,CeoUserDetails}` | 위 (동일) |
| batch | 없음 | — | — |

**auth의 결합 대상은 서블릿이 아니라 Spring Security core다.** application 계층의 auth 타입(토큰 서비스·`UserDetailsService`·`UserDetails`)은 `AuthenticationManager`·`SecurityContextHolder`·`UserDetails`와 `security-core`의 `JwtTokenProvider`만 쓰는 **서블릿-프리** 타입이고, 컨트롤러는 원시값(Bearer 토큰 문자열·인가 코드)만 넘긴다. 서블릿 결합 타입은 밖에 있다 — 필터·EntryPoint는 `security-module`이, `SecurityConfig`·`PublicPaths`는 각 api 모듈이 갖는다 — `applicationMustBeServletFree`가 그 경계를 강제한다.

**소셜 로그인은 web에만 있다** — admin·ceo에는 없다.

## ceo 고유 — 소유권·규격 검증이 이 모듈에 있다

`ShopOwnershipValidator`(`shop.ceoId == 로그인 ceoId` 확인, 불일치 시 `ApplicationErrorCode.SHOP_ACCESS_DENIED` 403)와 `ShopImageSpecValidator`/`ProductImageSpecValidator`(이미지 규격)는 **application 계층 협력자**라 이 모듈에 있다. 서블릿 타입을 쓰지 않으므로 `applicationMustBeServletFree`에 걸리지 않으며, 검증기가 쓰는 `javax.imageio.ImageIO`는 java 표준이라 규칙 대상이 아니다.

규격 검증기가 `MultipartFile`을 파라미터로 받는 것은 **업로드 경계 파라미터**로 허용된 형태다(`applicationMustBeServletFree`의 유일한 carve-out). Command record에 담는 것은 `commandRecordsShouldNotHoldMultipartFile`이 별도로 금지한다 — Command에는 업로드 결과 참조(파일 식별자·URL)만 담는다.

## batch 고유 — 왜 `crawling/bbq`가 여기 있나 (챕터 01 §2 판단 기록)

스펙은 "driven 클라이언트면 batch-module 잔류 + 인터페이스 분리"를 원칙으로 했으나, 확인 결과 **`crawling/bbq`는 driven 클라이언트가 아니라 application 계층 코드**였다.

- `BbqProductSyncService`는 `@Service @Transactional`로 **트랜잭션 경계를 소유**하고, 저장 불변식은 도메인 서비스 `ProductRegistrationService`에 위임하며, 동기화 대상 탐색은 `ProductQueryPort`(읽기 포트)로 한다.
- `BbqService`는 오케스트레이션이고, **진짜 driven 클라이언트는 `infrastructure:bbq`에 있다**(`com.tastyhouse.infrastructure.bbq.BbqApiClient`·`com.tastyhouse.infrastructure.bbq.RemoteImageDownloader`). 원격 이미지는 어댑터가 받아오기만 하고(`RemoteImagePort.download` → `DownloadedImage`), 파일 등록(`FileUploadService.upload`)은 `BbqService`가 조율한다.
- `BatchJobException`은 `BbqService`만 던지므로 함께 이동했다.

batch는 CQRS 분리를 쓰지 않는다 — `*CommandService`/`*QueryService`가 0개이고 잡 본문이 `*SchedulerService`에 담기며, 스케줄이 유일한 입력이라 Command record가 없고 인바운드 포트가 전부 `void foo()`다.

## ArchUnit — 아키텍처 테스트 클래스

**아키텍처 테스트는 이 코어 모듈의 테스트(`backend/application-core/src/test/java/com/tastyhouse/application/architecture/`)에 둔다.** importer는 `com.tastyhouse.application`이고, 테스트 클래스패스에 앱 모듈 4개가 있어(`testImplementation project(':{앱}-application')`) **5모듈을 모두 본다.** batch만 골라야 하는 규칙은 testFixtures `ModuleOrigin`으로 판정한 **출처 모듈**(`batch-application`)을 쓴다.

| 클래스 | 내용 |
|---|---|
| `LayerRulesTest` | 5모듈 공통 레이어 규칙. CQRS 교차 주입(`commandServicesShouldNotDependOnQueryPorts`·`queryServicesShouldNotDependOnWritePorts` — 대상은 testFixtures `UseCaseServices#commands()`·`#queries()`, 금지 대상은 이름 기준) · write 포트 분류(`writePortsShouldBeLoadOrSavePort`·`loadPortsShouldOnlyQuery`·`savePortsShouldOnlyMutate`) · 유스케이스 서비스 1:1 규칙 4종(아래 "봉인·가드 목록") · Command·`port.in` 경계 타입(`commandRecordsShouldBeBoundaryTyped`·`commandRecordsShouldNotHoldMultipartFile`·`portInShouldNotDependOnWebPlumbing`·`commandServicesShouldNotDependOnRequestRecords`) · 기술 차단(`shouldNotDependOnQuerydsl`·`shouldNotDependOnInfrastructure`·`applicationShouldNotDependOnPersistenceTechnology`·`applicationMustBeServletFree`·`applicationShouldNotDependOnSwagger`·`applicationShouldNotDependOnApiCommon`) · `applicationMustNotDependOnAdapters` · 계약 순수성(`readContractsShouldBeFrameworkFree`·`writePortsShouldOnlyDependOnDomainAndPortOut`) · 모듈 경계(`listenersAndConfigsShouldResideInCore`·`coreShouldNotContainUseCasesOrOrchestrators`·`coreBeansShouldOnlyDependOnCoreVisibleTypes`·`configurationsShouldNotRegisterStereotypedClasses`·`applicationShouldNotDeclareComponentScan`) · 주입·명명(`servicesShouldDependOnUseCasesNotImplementations`·`useCaseFieldsShouldBeNamedUseCase`) · 가시성(`useCaseImplementationsShouldNotBePublic`·`serviceClassesShouldNotBePublic`·`publicByNecessityShouldStillBePublic`·`listenersAndConfigsShouldNotBePublic`) |
| `BatchSchedulerRulesTest` | batch 출처 클래스 전용 — `applicationServicesShouldNotDependOnWebLayer` · `inboundPortsShouldBeBoundaryTyped`(엄격판) · `schedulerServicesShouldImplementUseCase` + exact anchor(`schedulerServicesExist`·`inboundPortsExist`) |
| `ServiceContextBoundaryTest` | 도메인 서비스 사이의 컨텍스트 경계(봉인 위반 15 · 봉인 순환 1 + 짝 테스트) — 위 "덩어리 03a" 절 |
| `EnumCodeConstantsTest` | `pgProviderCodeMatchesPgProvider`·`socialTempTokenProviderMatchesSocialProvider`(벤더 계약 enum과 domain enum의 상수명·순서 대조) · `portOutShouldNotMirrorDomainEnums`(`port.out`의 모든 enum을 도메인 enum과 상수 집합으로 대조해 복제본을 막는다. `isNotEmpty()`로 공허 통과 방지, 허용 목록 `PgProviderCode`) · `portOutShouldNotDeclareDomainEnumConstantStrings`. **알려진 취약점**: `SocialProvider`는 도메인 `MemberSocialProvider`와 `GOOGLE` 하나만 다르다 — 둘이 같아져 테스트가 실패하면 `SocialProvider`를 허용 목록에 추가하는 것이 올바른 조치다(벤더 계약 enum이지 복제본이 아니다) |
| `OutboundPortNamingRulesTest` | `port.out`의 interface는 이름이 `Port`로 끝난다(앱 모듈 포트 포함) |
| `ExecutorCallerTransactionRulesTest` | `*InNewTx` 메서드를 호출하는 메서드는 그 메서드에도 소속 클래스에도 `@Transactional`이 없어야 한다 + anchor(호출 클래스 ≥4) — 아래 "봉인·가드 목록" |
| `AsyncRulesTest` | `@Async` 메서드가 있으면 `@EnableAsync`가 코어 `shared/config`에 있어야 한다 |
| `ResultWitherComponentOrderTest` | `port/out` Result wither의 컴포넌트 순서 — 아래 "봉인·가드 목록" |
| `SplitPackageUniquenessTest` | 5모듈 사이 FQCN 중복 금지 |
| `RuleAnchorTest` | 공허 통과 자동 검출 — 아래 "anchor 하한" |

- **`queryServicesShouldNotDependOnWritePorts`의 carve-out은 FQN이다** — web `ShopDeliveryTipViewQueryService`(도메인 계산 입력) **1건**이며, **이 목록에 새 항목을 추가하지 않는다**(아래 "봉인·가드 목록").
- **`applicationMustNotDependOnAdapters`의 금지 대상은 4개 api 패키지 전부**다. 어느 앱의 서비스든 어느 api 모듈도 역참조할 수 없다.
- **`commandRecordsShouldBeBoundaryTyped`는 batch를 출처 모듈로 제외한다.** batch는 carve-out 없는 **엄격판**(`BatchSchedulerRulesTest#inboundPortsShouldBeBoundaryTyped`)을 쓰는데, 한 규칙으로 합치면 batch가 `MultipartFile` carve-out을 가진 느슨한 규칙에 얹혀 엄격함을 잃는다.

`allowEmptyShould(true)`는 어느 파일에도 쓰지 않는다 — 규칙이 대상을 잃으면 공허하게 통과시키지 말고 규칙을 지우거나 anchor를 고친다.

### anchor 하한

빈·UseCase 하한은 `RuleAnchorTest`가 **모듈별로** 갖는다 — `#moduleBeanCounts`(web ≥83 · admin ≥65 · ceo ≥122 · batch ≥15 · core ≥47), `#moduleUseCaseCounts`(web ≥185 · admin ≥207 · ceo ≥188 · batch =8 · core =0). 유스케이스 서비스 하한은 `#commandServicesExist` ≥313 · `#queryServicesExist` ≥267(`UseCaseServices` 술어로 센다), `..port.in..` 하한은 `#inboundPortsExist` ≥891. **모듈별로 두는 이유**: 합계 하나로 두면 한 앱의 빈·UseCase가 통째로 사라져도 나머지 앱이 하한을 떠받쳐 조용히 통과한다. core UseCase =0은 "코어에 UseCase가 새지 않는다"를 고정한다.

write 포트는 **≥ 192**(`RuleAnchorTest#writePortsExist` — `port.out.write`의 `*LoadPort`·`*SavePort` 인터페이스. 보조 record `AdminDongSyncResult`는 인터페이스가 아니라 세지 않는다). 읽기 계약 하한은 `#readContractsExist` ≥441(`..port.out..` 중 `..port.out.write..` 제외), 5모듈 전체 클래스 하한은 `#moduleIsNotEmpty` ≥1,500이다.

하한으로 두는 이유는 컨텍스트가 늘어나는 것이 정상이기 때문이다. 정확히 일치를 요구하면 기능 추가마다 이 파일을 고쳐야 해 anchor가 규칙이 아니라 잡음이 된다(batch UseCase는 규모가 작아 예외적으로 정확히 일치를 쓴다).

## Dependencies

### 빌드 스크립트 형태
- **(앱 마커 제거)** `testImplementation project(':{web,admin,ceo,batch}-application')` 4줄 — 이 모듈의 ArchUnit 테스트가 5모듈 전부를 보게 한다. test → main 방향이라 Gradle 순환이 아니다(앱 모듈은 `api project(':application-core')`로 이 모듈을 main에서 의존한다).
- `java-test-fixtures` 플러그인 — `ApplicationLayerScanAssertions`(앱 부트스트랩 스캔 단정 — 지금은 필터 없는 스캔·자기 앱 모듈만 로딩 단정 + imports 제거로 신설된 `assertScansModulesWithoutFilters`(중첩 `ModuleScanConfig`의 패키지 목록 정확 일치 + 패키지마다 스테레오타입 클래스가 1개 이상 실재 — 문자열 오타·패키지 리네임으로 모듈 빈이 조용히 사라지는 것을 막는다. `@ConditionalOnWebApplication`이 붙은 설정이 비-웹 스캐너에서 걸러지지 않도록 조건을 평가하지 않고 `@Component` 메타 애노테이션만 본다)·`assertNoTastyhouseAutoConfiguration`(`ImportCandidates`에 `com.tastyhouse.` 항목 0건). 중첩 스캔 클래스는 `ApplicationLayerScanConfig`·`ModuleScanConfig` 2개만 허용, 각 앱의 `ApplicationLayerScanConfigTest`가 호출)를 api 4모듈 테스트가 재사용하기 위한 것이다. `testFixturesImplementation`으로 `spring-boot-autoconfigure`(`@SpringBootApplication`·`@ComponentScan` 애노테이션 읽기)와 `assertj-core`를 선언한 것은 `ApplicationLayerScanAssertions` 때문이다. **같은 파일을 각 모듈에 복제하면 두 벌이 갈라지므로** test fixture로 공유한다. `testFixturesApi`로 `archunit-junit5`를 노출하는 이유는 `ModuleOrigin`·`UseCaseServices`가 ArchUnit `DescribedPredicate`를 공개 API로 내놓기 때문이다.
- **실행 모듈이 아니므로 `bootJar { enabled = false }` + `jar { enabled = true; archiveClassifier = '' }`** — plain jar만 만든다(`security-module` 선례). 아래 [주의](#주의) 참고.

### Internal
- **(앱 마커 제거) 이 모듈은 앱 모듈을 main에서 의존하지 않는다** — 반대로 `{web,admin,ceo,batch}-application`이 `api project(':application-core')`로 이 모듈을 의존한다. 코어가 앱 모듈 타입(예: `MailSenderPort`)을 import하면 컴파일 에러인 것이 앱 경계의 1차 방어선이다.
- `domain` (implementation) — 도메인 모델·VO·write 포트·도메인 서비스. **`implementation`이어서 이 모듈을 의존하는 쪽에 전이 노출되지 않는다** — 덩어리 01로 `api-common-module`(`api project(':application-core')`)·`security-module`(`implementation project(':application-core')`)이 `domain` 대신 이 모듈을 의존하게 되면서, presentation(web·admin·ceo-api, batch-module, api-common, security-module)의 컴파일 클래스패스에 `domain`이 사라졌다. **이 줄을 `api`로 바꾸지 않는다** — 바꾸는 순간 표현 계층에 domain이 되돌아오고 `shouldNotDependOnDomain`·`apiModuleShouldBeDomainModelFree`가 휴면 방어선에서 실제 실패로 바뀐다
- **표현 계층이 이 모듈에서 보는 domain 대체물 (덩어리 01)**: 에러 판정 `shared/error/`(`ErrorResponses`·`ErrorDescriptor`. 표현 계층이 직접 내는 에러코드는 `api-common-module`의 `ApiErrorCode`), 페이징 `shared/port/out/page/`(`PageQuery`·`PageResult`), enum 카탈로그 `shared/port/out/CodeLabelResult`, 그리고 enum 필드를 `String`으로 강등한 `*Result`
- `security-core` (implementation) — `JwtTokenProvider`·토큰 저장소 **포트**. **web·admin·ceo auth가 쓰는 서블릿-프리 타입 한정**. 챕터 01로 `security-core → infrastructure:redis` 간선이 끊겨, 이 모듈의 runtimeClasspath에서 `infrastructure:redis`·`api-common-module`이 사라졌다(전이 수신 0)
- **외부 연동 모듈(`infrastructure:{restclient,file-storage,firebase,aws-s3,aws-ses,aws-sns,oauth,kakao-oauth,naver-oauth,apple-oauth,facebook-oauth,pg,tosspayments,mail,javamail,sms,solapi,bbq,admdongkor}`) 의존은 두지 않는다** — 아웃바운드 계약은 application 계층이 소유하고 어댑터가 그것을 구현한다(**의존 역전**). 소셜 로그인 SPI는 `web-application`이, 크롤링·행정동 경계 포트는 `batch-application`이 소유하며(위 "앱 전용 SPI 포트"), 그 계약을 구현하는 쪽은 `infrastructure:{kakao,naver,apple,facebook}-oauth`(소셜 SPI — 스타터 `infrastructure:oauth`는 코드가 없어 조립만 한다)와 `infrastructure:bbq`·`infrastructure:admdongkor`(배치 포트)이며, 그 모듈들이 앱 모듈을 거쳐 이 모듈을 의존하므로 이 줄을 추가하면 순환이 되어 빌드가 깨진다
- **`security-module`·`api-common-module`을 추가하지 않는다** — 서블릿 스택이 유입된다

### External
- `spring-security-core` — admin·ceo `AuthenticationManager`·`SecurityContextHolder`·`PasswordEncoder`·`UserDetails`
- `spring-web` — web·admin·ceo `MultipartFile`(업로드 경계 파라미터). 실사용이 1종뿐이라 starter-web 전체 대신 이 좌표만 선언한다
- `jackson-databind` — ceo `ShopStorePriceVerificationRequestService`의 `ObjectMapper`
- `spring-tx` — `@Transactional`만을 위한 최소 의존
- `spring-boot-autoconfigure`는 **재선언하지 않는다** — batch `AdminDongSyncRunner`의 `@ConditionalOnProperty`가 쓰지만 루트 `build.gradle`의 `subprojects` 블록이 넣는 `spring-boot-starter`로 전이 충족된다

batch 유스케이스가 `spring-web`·`spring-security-core`를 컴파일 클래스패스에서 보게 되지만, **서블릿 스택(`security-module`·`starter-web`)은 여전히 없다.**

### infrastructure 의존 없음 — 이 모듈의 핵심

application 계층이 infra를 모른다는 규칙을 ArchUnit이 아니라 **빌드 그래프가 1차로 강제**한다. `import com.tastyhouse.infrastructure...` 한 줄이 실제 컴파일 에러가 된다. `shouldNotDependOnInfrastructure`는 누군가 build.gradle에 의존을 되돌리는 회귀를 막는 2차 방어선이다.

## 빈 배선 — 앱 부트스트랩의 중첩 스캔 설정

**이 모듈은 스캔을 선언하지 않는다.** 각 앱 부트스트랩(`WebApiApplication`·`AdminApiApplication`·`CeoApiApplication`·`BatchApplication`)의 static 중첩 클래스가 application 계층 스캔을 소유한다.

```java
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "com.tastyhouse.application")
static class ApplicationLayerScanConfig {
}
```

| 항목 | 규칙 |
|---|---|
| 필터 | 기본 필터(`@Component` 계열), include/exclude 없음 |
| 무엇이 뜨나 | 앱 클래스패스에 있는 `com.tastyhouse.application..`의 스테레오타입 클래스 전부 = 코어 + 자기 앱 모듈 |
| 흔한 실수 | 다른 앱 모듈을 의존에 추가 → 그 앱의 빈이 전부 뜬다(**"클래스패스 존재 = 활성화"**). 각 앱 `ApplicationModuleClasspathTest`가 막는다 |
| 앱의 `@Import` | 없음 |
| 이 모듈이 아는 것 | 없음 — 어느 앱이 무엇을 싣는지는 앱의 클래스패스가 정한다 |

**왜 중첩 클래스인가**: Spring Framework 6.1.5 `ConfigurationClassParser`는 설정 클래스에 직접 붙은 `@ComponentScan`을 먼저 모으고, 하나라도 있으면 메타 애노테이션(`@SpringBootApplication` 안의 `@ComponentScan`)을 무시한다. `@SpringBootApplication` 클래스에 직접 달면 앱 자기 패키지 스캔과 Boot의 `TypeExcludeFilter`·`AutoConfigurationExcludeFilter`가 조용히 사라지므로 금지다. 중첩 `@Configuration`은 별도 설정 클래스로 처리돼 안전하다. 부작용으로 `com.tastyhouse.application` 클래스패스 스캔이 기동 시 2회 돌지만(두 번째는 `ClassPathBeanDefinitionScanner.isCompatible`로 건너뜀) 빈 집합은 같다.

**`@Service`/`@Component`가 없는 클래스는 빈이 되지 않는다** — 일반 Spring과 같다. 빈 배선 실수는 빌드로 드러나지 않을 수 있으므로 아래 "주의"를 함께 본다.

## `<ctx>/listener/` — 도메인 이벤트 리스너

domain이 `shared/event/DomainEventPublisher` 포트로 발행한 도메인 이벤트를 구독하는 크로스커팅 리스너를 둔다. 전부 `@TransactionalEventListener(phase = AFTER_COMMIT)`이며, 발행 구현 `shared/event/SpringDomainEventPublisher`가 `ApplicationEventPublisher`로 위임한다(포트와 구현 모두 이 모듈 소유, 등록은 `shared/config/SharedBeanConfig`).

**위치는 `com.tastyhouse.application.<ctx>.listener`이고, 리스너는 전부 `@Component`이며 코어 `application-core` 모듈에만 둔다**(코어가 4앱 전부의 클래스패스에 있으므로 코어에 두는 것만으로 4앱 전부에 뜬다 — `LayerRulesTest#listenersAndConfigsShouldResideInCore`가 강제). 현재 12종 — `coupon`·`file`·`mail`·`member`(`MemberEventListener`·`ReferralRegisteredEventListener`)·`notification`(`ReviewOwnerReplyEventListener`·`ReviewBlindApprovedEventListener`)·`payment`·`point`·`policy`·`product`·`sms`. **리스너를 `infrastructure:persistence`나 앱 모듈에 두지 않는다** — 이벤트를 받아 도메인 서비스를 오케스트레이션하는 것은 유스케이스 계층의 일이고, 앱 모듈에 두면 다른 앱이 발행한 이벤트의 후속 처리가 **예외도 로그도 없이** 빠진다([ADR-0024](../../docs/adr/0024-event-listeners-in-application.md)). 빈 등록은 위 [빈 배선](#빈-배선--앱-부트스트랩의-중첩-스캔-설정) 참고. 리스너가 infra DAO를 직접 주입하지 않는다 — 이 모듈은 infra를 컴파일 클래스패스에 두지 않으므로 필요한 조회는 `port/out` 읽기 포트로 받는다(`ReviewOwnerReplyEventListener` → `ShopBasicInfoQueryPort#findShopName`).

**미소비 이벤트를 남기지 않는다.** 모든 `*Event` record에는 대응 리스너가 있어야 한다. 리스너 없는 이벤트는 "누군가 처리하고 있겠지"라는 착각을 낳고, 발행 지점만 보고는 그 착각이 드러나지 않는다. 소비 수요가 없다고 판단되면 리스너를 만드는 대신 **이벤트 record와 발행 호출을 함께 삭제**한다 — 둘 중 하나를 고르되 "발행만 하고 두는" 상태는 허용하지 않는다.

### ⚠️ AFTER_COMMIT은 실패하면 조용히 유실된다 — 금전 처리를 리스너에 두지 말 것

**이 프로젝트에는 재시도도 outbox도 없다.** AFTER_COMMIT 리스너가 예외로 죽으면 원본 트랜잭션은 이미 커밋된 뒤이므로 롤백되지 않고, 후속 처리만 소리 없이 사라진다. 로그 한 줄이 남을 뿐 실패를 감지하는 장치가 없다.

payment·point·coupon 리스너는 **금전에 직접 영향을 준다**(포인트 적립·환급·회수). 지금 이 처리들이 리스너에 있는 것은 유실이 허용돼서가 아니라 기존 구조가 그렇기 때문이며, **새로 추가하는 후속 처리에는 이 배치를 선례로 삼지 않는다.**

**판단 기준 — 유실되면 곤란한가?**

| 유실 시 결과 | 두는 곳 |
|---|---|
| 관측성만 손해(로그·통계 누락) | `<ctx>/listener/`의 `@TransactionalEventListener(AFTER_COMMIT)` |
| **데이터가 어긋남**(금전 정산, 목록에서 사라짐, 상태 불일치) | **동기 Recorder 패턴** — 원본 상태 전이와 **같은 트랜잭션**에서 도메인 서비스가 직접 호출 |

동기 Recorder의 선례는 `ShopChangeHistoryRecorder`·`ShopRequestIndexRecorder`다. 특히 후자는 이 판단을 명시적으로 기록해 두었다 — 요청처리 현황은 기록 유실이 곧 "요청이 목록에서 사라짐"이라 이벤트를 쓰지 않고 동기 기록을 택했고, Recorder를 도메인 서비스의 **생성자 필수 의존**으로 받아 새 상태 전이를 추가할 때 배선 필요성이 컴파일 단계에서 드러나게 했다. 상세는 루트 `CLAUDE.md`의 "요청 인덱스 동기화 규칙".

**outbox 도입은 현재 범위 밖이다.** 도입을 검토해야 할 시점의 근거만 남긴다 — (1) 유실 시 데이터가 어긋나는 후속 처리인데 동기 트랜잭션에 넣을 수 없는 경우(외부 API 호출처럼 원본 트랜잭션을 길게 잡으면 안 되는 것), (2) 그런 처리가 여러 컨텍스트에 생겨 Recorder 패턴만으로 감당되지 않는 경우. 그 전까지는 위 표의 두 선택지로 충분하다.

### 리스너 작성 규칙

- **도메인별로 분리한다**: 한 리스너가 여러 도메인 이벤트를 구독하면 한 도메인의 변경이 다른 도메인의 리스너 파일을 건드리게 된다. 같은 도메인의 이벤트 여러 개를 한 리스너가 받는 것은 정상이다(`CouponEventListener`가 발급·사용을 함께 받는 형태).
- **규칙 본체를 리스너에 두지 않는다**: 리스너는 이벤트 수신과 트랜잭션 경계만 담당하고, 판단·계산은 도메인 서비스가 갖는다(`PaymentEventListener` → `PointLedgerService`·`PaymentConfirmationService`, `ProductMenuReviewEventListener` → `ProductReviewStatsService`).
- **DB를 쓰면 `@Transactional(propagation = REQUIRES_NEW)`를 붙인다**: AFTER_COMMIT 시점에는 원본 트랜잭션이 이미 끝나 있다. 기록만 하는 핸들러는 붙이지 않는다.

### 리스너 단위 테스트

**리스너 파일마다 `<ctx>/listener/` 아래 대응 테스트를 둔다.** 스프링 컨텍스트 없이 리스너를 직접 생성해 핸들러를 이벤트 객체로 호출하는 순수 단위 테스트이며, AFTER_COMMIT 발화·`@Async`·트랜잭션 전파 같은 배선 자체는 프레임워크 몫이라 검증하지 않는다.

- **협력자가 있는 리스너**(payment·product)는 mock으로 **무엇을 호출/미호출하는지**를 검증한다. 조건 분기(현장 결제만 적립, `usedPoint > 0`일 때만 환급, `productId == null`이면 통계 미갱신)가 이 리스너들의 실질이고, 잘못되면 이중 정산·환급 누락으로 이어진다.
- **기록만 하는 리스너**(coupon·emailverification·file·member×2·phoneverification·point·policy)는 `LogCapture`(testFixtures `com.tastyhouse.testsupport.shared.logging`)로 Logback appender를 붙여 **무엇이 기록되는지**까지 확인한다. 로그를 관측하지 않으면 핸들러 본문을 통째로 지워도 통과하는 공허한 테스트가 된다.
- **같은 타입 파라미터가 여러 개면 서로 다른 값을 넣는다**: `ReferralRegisteredEvent`의 추천인·피추천인은 둘 다 `MemberId`라 순서를 바꿔도 컴파일된다 — 값이 뒤바뀌면 "누가 누구를 추천했는지"가 반대로 기록되므로 각각이 제 자리에 들어가는지 확인한다.

reference 구현: `PaymentEventListenerTest`(협력자 mock + 조건 분기 3종 + 환불 접수의 "포인트 미개입" 계약), `ProductMenuReviewEventListenerTest`(null 가드), `CouponEventListenerTest`(로그 캡처 기준 예시), 공용 유틸 `LogCapture`(`backend/application-core/src/testFixtures/java/com/tastyhouse/testsupport/shared/logging/LogCapture.java`, 리스너·서비스 가리지 않고 대상 클래스의 Logback 출력을 담는다).

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar(`security-module` 선례). 앱을 띄우는 것은 각 api 모듈의 fat jar 4개이며, 그 **이름·경로·포트는 통합 후에도 불변**이다. jar 내용만 application jar 4개 → `application-core-0.0.1-SNAPSHOT.jar` 1개로 바뀐다. **(앱 마커 제거 후)** 지금 각 fat jar에는 `application-core-0.0.1-SNAPSHOT.jar`와 자기 앱의 `{앱}-application-0.0.1-SNAPSHOT.jar` 2개가 들어간다(앱 모듈 4개도 같은 형태 — `bootJar` 비활성 + plain jar).
- **빈 배선 실수는 빌드로 드러나지 않는다** — `contextLoads` 테스트가 `@SpringBootTest` 없이 빈 껍데기라 스캔 설정(부트스트랩 중첩 `ApplicationLayerScanConfig` — 앱 마커 제거 후 필터 없는 스캔)이 누락·오기입돼도 빌드는 green이고 jar만 조용히 깨진다(오기입은 각 앱의 `ApplicationLayerScanConfigTest`가, 다른 앱 모듈 유입은 `ApplicationModuleClasspathTest`가 잡는다). 배선을 건드렸으면 실제로 띄워 `Started {Xxx}Application` 마커를 확인한다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

원문 주석은 챕터 04에서 제거되므로, 이 문서가 그 금지 지시의 유일한 소재지다.

### `*InNewTx` 호출자는 트랜잭션 밖이어야 한다

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/ExecutorCallerTransactionRulesTest.java` → `newTxCallersShouldNotBeTransactional` · `newTxCallersExistSoTheRuleIsNotVacuous`, 호출자 `backend/web-application/src/main/java/com/tastyhouse/application/payment/service/PgPaymentConfirmService.java` · `.../payment/service/PaymentCancelService.java` · `.../reservation/service/ReservationCreateService.java` · `backend/batch-application/src/main/java/com/tastyhouse/application/payment/service/PaymentReconcileSchedulerService.java`

원문 취지: `PaymentConfirmationExecutor`·`PaymentCancellationExecutor`·`ReservationBookingExecutor`·`PaymentReconcileExecutor`(batch-application)의 `*InNewTx` 메서드는 `Propagation.REQUIRED`다. 이름이 말하는 "새 트랜잭션"은 호출자가 트랜잭션 밖이라는 전제에서만 성립한다. 호출자에 `@Transactional`이 붙으면 Executor 호출들이 한 트랜잭션으로 합쳐지고, 호출자가 하는 PG HTTP 호출이 DB 트랜잭션(커넥션·결제 행 잠금) 안으로 들어간다. 그래서 `*InNewTx`를 호출하는 메서드와 그 소속 클래스에 `@Transactional`(Spring·jakarta 둘 다)을 금지한다. 이름을 바꾸거나 `REQUIRES_NEW`로 바꾸지 않은 이유: `REQUIRES_NEW`는 이름은 맞추지만 PG 호출이 바깥 트랜잭션 안에 들어가는 위험을 막지 못하고, 이름 변경은 위험을 드러내지 못한다. **한계**: 직접 호출자만 본다 — UseCase를 거쳐 간접 호출하는 `@Transactional`은 잡지 못하고, 람다 본문의 호출은 소속 클래스 단위로만 잡는다. batch `PaymentReconcileExecutor`의 세 메서드(`claimNextAttemptInNewTx`·`reconcileApprovedInNewTx`·`failUnapprovedInNewTx`)도 같은 규약으로 짓는다 — 가드는 메서드 이름의 접미사로 호출자를 찾으므로, 접미사가 없으면 `PaymentReconcileSchedulerService`에 `@Transactional`이 붙어 토스 조회 HTTP 호출이 DB 트랜잭션 안으로 들어가도 빌드가 통과한다. 이 테스트의 클래스패스에는 앱 application 모듈 4개가 모두 올라오므로(`application-core/build.gradle`의 `testImplementation`) batch 호출자도 검사된다. anchor는 `*InNewTx`를 호출하는 **클래스** 수 ≥4(지금 4개 — web 3 + batch 1)다. 반증 probe로 확인했다 — `ReservationCreateService`나 batch `PaymentReconcileSchedulerService`에 `@Transactional`을 임시로 붙이면 `newTxCallersShouldNotBeTransactional`이 실패한다.

### 영속 기술 중립 가드 — application은 영속 기술의 타입·예외를 모른다

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `applicationShouldNotDependOnPersistenceTechnology`

원문 취지: application 5모듈은 `org.springframework.dao..`·`org.springframework.orm..`·`org.springframework.data..`·`jakarta.persistence..`·`org.apache.ibatis..`·`org.mybatis..`에 의존하지 않는다. 도입 시점 위반은 `web-application`의 `ReservationCreateService`가 `DataIntegrityViolationException`을 catch하던 1건이었고, 어댑터가 `UniqueConstraintConflictException`(`application/shared/port/out/`)으로 번역하도록 바꿔 0건이 됐다. 같은 테스트의 두 번째 단정(try-catch 블록의 잡는 타입 검사)을 지우지 않는다 — ArchUnit 1.2.1은 catch 절 타입을 의존으로 기록하지 않아, 그 단정 없이는 도입 시점 위반(`catch (… | DataIntegrityViolationException e)`)도 통과했다(반증 probe로 확인). 패키지 목록은 `PERSISTENCE_TECHNOLOGY_PACKAGES` 상수 한 곳에 있고 두 단정이 함께 쓴다. 규칙에서 패키지를 빼지 않는다 — 빼면 어댑터를 MyBatis 등으로 바꿀 때 서비스까지 고쳐야 하는 결합이 조용히 되살아난다. 근거는 `backend/CLAUDE.md`의 "영속 포트 기술 중립 규칙" 절.

### write 포트는 조회(LoadPort)와 변경(SavePort)으로 나눈다 — 이름과 메서드 접두어가 분류다 (2026-10-09 Load/Save 분리)

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `writePortsShouldBeLoadOrSavePort`(구 `writePortsShouldBeNamedPort`) · `loadPortsShouldOnlyQuery` · `savePortsShouldOnlyMutate` · 상수 `LOAD_PREFIXES` · `SAVE_PREFIXES`
**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/RuleAnchorTest.java` → `writePortsExist`(하한 107 → 192, 대상 `*LoadPort`·`*SavePort`)

원문 취지:
- **`port.out.write`의 인터페이스는 `*LoadPort` 또는 `*SavePort`로 끝난다**(`writePortsShouldBeLoadOrSavePort`). 분리 전 `XxxPersistencePort`(106개)가 조회와 변경을 한 인터페이스에 섞어, 서비스가 쓰지 않는 메서드까지 의존하고 "이 서비스는 읽기만 한다"를 가드가 표현할 수 없었다. 옛 `PersistencePort` 이름을 되살리면 이 규칙이 실패한다.
- **`*LoadPort`의 메서드는 `LOAD_PREFIXES`(`find`·`exists`·`count`·`filter`)로만 시작한다**(`loadPortsShouldOnlyQuery`). 상태를 바꾸는 메서드가 LoadPort에 들어오면 조회 서비스가 변경을 호출할 수 있게 된다.
- **`*SavePort`의 메서드는 `SAVE_PREFIXES`(`save`·`delete`·`sync`·`bulk`·`expire`)로만 시작한다**(`savePortsShouldOnlyMutate`). 두 목록 모두 지금 포트가 실제로 쓰는 접두어만 담는다 — 미리 넓혀 두면 `getOrCreate…` 같은 변경 메서드가 LoadPort에 들어와도 통과하므로, 새 접두어는 필요해질 때 추가한다. **이름이 Save여도 삭제를 포함한다**(사용자 결정) — `DeletePort`를 따로 만들지 않는다. 조회 메서드가 SavePort에 들어오면 분리가 무의미해진다.
- **`writePortsExist` 하한을 낮추지 않는다**(192 = 읽기+쓰기 85쌍 170 + 쓰기만 18 + 읽기만 4). 규칙이 대상을 잃고 공허하게 통과하는 것을 막는 앵커이며, 접미어를 바꾸면 대상이 0이 된다.
- 어댑터 이름은 불변이다 — `{Ctx}PersistenceAdapter implements {Ctx}LoadPort, {Ctx}SavePort`(어댑터 1 : 포트 N). 규칙·네이밍 표는 `backend/CLAUDE.md`의 "아웃바운드 포트·어댑터 네이밍 규칙" 절.

### 앱 모듈 경계 가드 — 마커를 되살리지 않고, 다른 앱 모듈을 클래스패스에 올리지 않는다 (앱 마커 제거)

**대상**:
- `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `listenersAndConfigsShouldResideInCore` · `coreShouldNotContainUseCasesOrOrchestrators` · `coreBeansShouldOnlyDependOnCoreVisibleTypes` · `configurationsShouldNotRegisterStereotypedClasses` · `commandRecordsShouldBeBoundaryTyped`(batch 제외를 출처 모듈로 판정)
- `backend/application-core/src/test/java/com/tastyhouse/application/architecture/BatchSchedulerRulesTest.java` → 대상 선별(`batch-application` 출처 클래스)
- `backend/application-core/src/test/java/com/tastyhouse/application/architecture/RuleAnchorTest.java` → `moduleBeanCounts` · `moduleUseCaseCounts` · `testFixturesShouldNotResideInApplicationPackage`
- `backend/application-core/src/test/java/com/tastyhouse/application/architecture/ServiceContextBoundaryTest.java` → `domainServices()` · `EXCLUDED_COLLABORATORS` · `excludedCollaboratorsShouldNotBeStale`
- `backend/application-core/src/testFixtures/java/com/tastyhouse/architecture/ModuleOrigin.java` → 클래스 전체 · 짝 `backend/application-core/src/test/java/com/tastyhouse/application/architecture/ModuleOriginTest.java`
- `backend/application-core/src/testFixtures/java/com/tastyhouse/architecture/ApplicationLayerScanAssertions.java` → `assertScansApplicationLayerWithoutFilters` · `assertLoadsOnlyOwnApplicationModule`
- `backend/application-core/src/test/java/com/tastyhouse/application/architecture/SplitPackageUniquenessTest.java` → `classNamesShouldBeUniqueAcrossApplicationModules`

원문 취지:
- **마커 애노테이션을 되살리지 않는다.** 앱 소속은 모듈 위치가 표현한다. 빈은 언제나 `@Service`/`@Component`를 단다.
- **리스너와 `@Configuration`은 코어에만 둔다**(`listenersAndConfigsShouldResideInCore`). 리스너가 앱 모듈에 있으면 다른 앱이 같은 이벤트를 발행할 때 후속 처리가 조용히 사라진다(AFTER_COMMIT 실패는 예외도 남지 않는다). `@Configuration`이 스테레오타입 클래스를 `@Bean`으로 다시 등록하면 스캔 빈과 이름이 겹쳐 기동이 실패한다(`configurationsShouldNotRegisterStereotypedClasses`).
- **코어에 UseCase·오케스트레이터를 두지 않는다**(`coreShouldNotContainUseCasesOrOrchestrators`, `moduleUseCaseCounts`의 core =0). 코어는 4앱 전부에 뜨므로 거기 둔 UseCase는 쓰지 않는 앱에도 뜬다.
- **빈은 자기가 뜨는 앱 컨텍스트에서 보이지 않는 구현에 의존하지 않는다**(`coreBeansShouldOnlyDependOnCoreVisibleTypes`). 컴파일은 통과해도(인터페이스가 코어에 있으면) 그 구현이 없는 앱에서 기동이 실패한다. 판정: 모든 `@Service`/`@Component` 빈(코어·앱 모듈 모두)의 생성자 파라미터 중 `com.tastyhouse.application.` 인터페이스마다 **후보** = 그 인터페이스의 추상이 아닌 스테레오타입 구현체(같은 인터페이스를 생성자로 받는 데코레이터는 제외) + 그 타입을 반환하는 `@Bean` 메서드. 후보가 1개 이상이면, 그 빈이 뜨는 **각 앱 컨텍스트**(코어 빈 → 4앱 전부, 앱 빈 → 자기 앱. 컨텍스트 = 코어 + 그 앱 모듈)에서 보이는 후보가 0개면 위반(구현이 다른 앱 모듈에만 있어 그 앱이 기동하지 못한다), 2개 이상이면 모호 위반이다. 위반은 Set으로 모아 한 번에 보고한다. anchor: 검사한 의존 ≥ 300, 후보가 1개 이상인 의존(`RESOLVED_FLOOR`) ≥ 43. **알려진 한계**: `@Primary`/`@Qualifier`는 모델링하지 않는다. infrastructure 구현체는 import 대상이 아니어서 후보 0인 의존(persistence·벤더가 구현하는 포트)은 건너뛴다. **anchor 하한(300 / `RESOLVED_FLOOR` 43)을 낮추지 않는다** — 후보 계산이 깨지면 모든 의존이 "후보 0, 건너뜀"이 되어 규칙이 공허 통과한다.
- **`moduleBeanCounts`·`moduleUseCaseCounts` 하한을 낮추지 않는다**(빈: web ≥83 · admin ≥65 · ceo ≥122 · batch ≥15 · core ≥47 / UseCase: web ≥185 · admin ≥207 · ceo ≥188 · batch =8 · core =0). 한 모듈의 클래스가 통째로 사라지거나 엉뚱한 모듈로 옮겨지면 깨지는 것이 의도다. 유스케이스를 의도적으로 삭제할 때만 삭제 후 실측값으로 맞추며, 클래스 이동·출처 판정 오류로 줄어든 경우는 낮추지 않는다([ADR-0057](../../docs/adr/0057-anchor-floor-on-intentional-deletion.md)).
- **`ModuleOrigin`은 main 출력만 인정하고, 나머지는 예외를 던져야 한다.** 규칙은 `ModuleOrigin.from(module)`(`DescribedPredicate`)로 대상을 고른다. 인정하는 것은 클래스 디렉터리 `.../{module}/build/classes/java/main/...`와 main jar `{module}-<버전>.jar`뿐이다. testFixtures 출력(`build/classes/java/testFixtures`, `*-test-fixtures.jar`), IntelliJ 자체 빌드 출력(`out/production/...`), opaque·형식이 깨진 URI는 전부 `IllegalStateException`이다. 모르는 형태에서 빈 값이나 기본값을 돌려주면, 출처로 고르는 규칙(batch 선별 등)이 대상을 잃고 공허하게 통과한다. **그래서 아키텍처 테스트는 Gradle로 실행한다** — IntelliJ 자체 빌드로 돌리면 `out/production/...` 출력 때문에 예외로 실패한다. IntelliJ에서는 Settings → Build Tools → Gradle → "Build and run using: Gradle"(테스트 실행도 Gradle)로 둔다. 이 예외를 피하려고 `out/production`을 인정하도록 넓히지 않는다.
- **`EXCLUDED_COLLABORATORS` 21개의 근거** — 모든 서비스가 `@Service`를 달므로 "스테레오타입이 없으면 도메인 서비스"라는 술어를 쓸 수 없다. 그래서 `ServiceContextBoundaryTest#domainServices()`는 구조 조건(`..service..`, 인터페이스 아님, `@Configuration` 아님, `port.in` 구현 아님 — 유스케이스 서비스는 전부 `port.in`을 구현하므로 이 조건이 걸러 낸다)으로 고르고, **원래부터 스테레오타입이 있어 검사 대상이 아니던 협력 빈 21개**(Executor 6 · `SocialLoginService` 1 · Validator 7 · Reader 1(`ShopFoodTypeCategoryReader`) · `OwnedShopIdProvider` · `MemberAuthService` · `CredentialLoginService` · `PhoneLoginService` · `AuthPasswordResetService` · `AdminDongSyncRunner`)를 FQN으로 뺀다. `SocialLoginService`는 `member.service.MemberRegistrationService`·`member.port.out.write.*`를 직접 참조하므로 제외 대상이고, 같은 컨텍스트의 `auth.service.SocialOAuthClientRouter`는 auth 타입만 참조해 경계 검사를 통과하므로 넣지 않는다. 유스케이스 분리로 생긴 `{도메인}{명사}Reader`·`Validator`(예: ceo `ShopNoticeOwnerReader`·`ShopContentBoardOwnerReader`·`ShopBusinessHourOwnerValidator`)와 `ProductSoldOutUntilValidator`는 이 목록에 넣지 않았다 — **새 협력 빈은 경계 검사를 받게 두며 목록은 줄어들기만 한다.** 이 목록에서 항목을 빼면 그 협력 빈이 처음으로 경계 검사를 받아 봉인 밖 위반이 드러날 수 있다 — 빼려면 위반부터 확인한다. 새 협력 빈을 만들 때는 도메인 서비스인지 협력 빈인지 판단해 넣을지 정한다. 낡은 항목은 `excludedCollaboratorsShouldNotBeStale`이 잡는다 — **알려진 한계**: 이 짝 테스트는 더 이상 존재하지 않거나 구조 조건에 맞지 않게 된 항목만 잡고, "목록에서 빼도 경계 규칙을 통과할 항목"은 잡지 못한다. **이 술어는 사실상 "구조 + 이름 목록"이다.**
- **앱 테스트 클래스패스의 `com.tastyhouse.application` 클래스는 코어와 자기 앱 모듈에서만 와야 한다**(각 앱 `ApplicationModuleClasspathTest` → `assertLoadsOnlyOwnApplicationModule(ModuleOrigin.{WEB,ADMIN,CEO,BATCH})`, 출처 모듈 집합 == `{application-core, 자기 앱 모듈}`). 스캔에 필터가 없으므로 다른 앱 모듈이 의존에 섞이면 그 앱의 빈이 전부 뜬다("클래스패스 존재 = 활성화"). 판정은 클래스 출처(`ModuleOrigin`)로 하며, 앱 모듈에 표식 리소스(`META-INF/tastyhouse/application-module.properties` 같은 것)를 두지 않는다. 판정 대상에서 자기 앱 모듈이 빠져도 실패하므로 공허 통과가 없다.
- **같은 FQCN이 application 계층 5모듈 중 두 곳 이상에 있으면 안 된다**(`SplitPackageUniquenessTest`). 패키지를 유지한 split package라서 같은 FQCN이 core와 앱 모듈에 함께 생겨도 컴파일은 통과하고, 실행 시 클래스패스 순서로 한쪽이 조용히 가려진다. ArchUnit은 같은 이름의 클래스를 하나로 합쳐 보므로 잡지 못한다. 그래서 이 검사는 5모듈의 소스 경로로 한다(스캔 소스 ≥1400 anchor).
- 각 규칙은 위반 probe로 실패를 확인했다. 코어가 `MailSenderPort`를 import하면 컴파일 에러인 것도 확인했다.

### 도메인 서비스를 `@Bean`으로 등록하지 않는다 — `*ServiceConfig`를 되살리지 않는다

**대상**:
- `backend/application-core/src/main/java/com/tastyhouse/application/shared/config/SharedBeanConfig.java` → 클래스 전체(`@Bean` 10개)
- `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `configurationsShouldNotRegisterStereotypedClasses`

원문 취지:
- **새 도메인 서비스는 `@Service`를 달고 소비 앱 집합에 맞는 모듈에 둔다.** `@Bean`으로 등록하지 않고, `*ServiceConfig`를 되살리지 않는다(위 [application `*ServiceConfig`를 두지 않는다](#application-serviceconfig를-두지-않는다--도메인-서비스는-service로-스캔-등록)).
- **`SharedBeanConfig`에는 애노테이션을 달 수 없는 빈만 둔다** — domain 모듈 클래스(spring-free), 인터페이스 타입으로 등록해야 하는 `domainEventPublisher`, 상수·람다·데코레이터로 조립해야 하는 `shopDeliveryTipRangePolicy`·`prohibitedWordValidator`. 스테레오타입이 붙은 클래스를 여기 `@Bean`으로 추가하면 스캔 빈과 이름이 겹쳐 기동이 실패한다 — `configurationsShouldNotRegisterStereotypedClasses`가 잡는다.
- **앱 모듈에 `@Configuration`을 두지 않는다** — `@Configuration`은 코어에만 둔다(`listenersAndConfigsShouldResideInCore`).
- 규칙 통과는 빈 누락이 없다는 증거가 아니다 — 모듈 배치를 바꿨으면 4앱을 `java -jar`로 띄워 `Started *ApiApplication`을 확인한다(`contextLoads`는 빈 껍데기). `List<I>` 주입은 구현체 0개여도 기동되므로 규칙이 잡지 못한다.

### testFixtures 클래스는 `com.tastyhouse.application` 밖에 둔다 — `com.tastyhouse.architecture`

testFixtures에는 두 패키지가 있다 — `com.tastyhouse.architecture`(`ApplicationLayerScanAssertions`·`ModuleOrigin`·`UseCaseServices`)와 **`com.tastyhouse.testsupport.<ctx>..`**(공유 테스트 더블 20개 — `Fake*`/`Stub*`/`Recording*`/`LogCapture`). 테스트 더블을 testFixtures로 옮긴 이유는 앱 모듈의 테스트도 같은 더블을 써야 하기 때문이고, `com.tastyhouse.application` 아래에 두지 않은 이유는 아래 취지 그대로다. 옮기며 package-private이던 9개를 public으로 바꿨다. `testsupport`도 `ImportOrderConventionTest.TOP_SEGMENT_RANK`에서 `architecture`와 같은 4순위다.

**대상**:
- `backend/application-core/src/testFixtures/java/com/tastyhouse/architecture/` → `ApplicationLayerScanAssertions` · `ModuleOrigin` · `UseCaseServices`
- `backend/application-core/src/testFixtures/java/com/tastyhouse/testsupport/` → 공유 테스트 더블 20개(앱 마커 제거로 신설)
- `backend/application-core/src/test/java/com/tastyhouse/application/architecture/RuleAnchorTest.java` → `testFixturesShouldNotResideInApplicationPackage`
- `backend/domain/src/test/java/com/tastyhouse/domain/architecture/ImportOrderConventionTest.java` → `TOP_SEGMENT_RANK`의 `architecture`(4순위)

원문 취지:
- **testFixtures에 `com.tastyhouse.application..` 패키지를 만들지 않는다.** `ImportOption.Predefined.DO_NOT_INCLUDE_TESTS`는 `build/classes/*/test/` 경로만 걸러 낸다. 그래서 `-test-fixtures.jar`와 `build/classes/java/testFixtures/`는 통과한다. fixture가 `com.tastyhouse.application` 아래 있으면 `importPackages("com.tastyhouse.application")`를 쓰는 모든 규칙(이 모듈의 `LayerRulesTest`·`RuleAnchorTest`·`ServiceContextBoundaryTest` 등)이 fixture를 **프로덕션 클래스로 보고 검사한다**.
- 그 상태로는 "application의 모든 빈은 코어 또는 앱 모듈에서 온다"처럼 모듈 전역을 대상으로 하는 규칙을 새로 만들 때, fixture부터 위반으로 걸린다. `moduleIsNotEmpty` 같은 개수 anchor도 fixture 수만큼 부풀어 실제 개수보다 크게 나온다.
- `com.tastyhouse.architecture`는 어떤 ArchUnit importer의 대상 패키지도 아니다(리포 전체에 `importPackages("com.tastyhouse")`가 없다). 새 fixture도 이 패키지에 둔다. 이 규칙을 지키는 장치가 `testFixturesShouldNotResideInApplicationPackage`다. 소스 URI가 `test-fixtures`/`/testFixtures/`인 클래스가 application 대상 import에 섞이면 실패한다. 2026-10-03에 패키지 이동 전 상태로 두 클래스를 잡아 실패하는 것을 확인했다.
- 새 최상위 세그먼트라 import 순위가 필요하다. 테스트 공용 지원 코드라서 공유 횡단 모듈과 같은 4순위로 정했다.

### UseCase 구현 구체 주입 금지 — `servicesShouldDependOnUseCasesNotImplementations`

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`
→ `servicesShouldDependOnUseCasesNotImplementations`

- **검사 내용**: `port.in` 인터페이스를 구현한 클래스(구현 집합 `I`)에 다른 클래스가 구체 타입으로 의존하면 실패한다. `getDirectDependenciesFromSelf()`로 판정하므로 필드·생성자 파라미터·메서드 파라미터·호출이 모두 잡힌다.
- **제외 대상**:
  - 자기 자신과 자기 중첩 클래스. 최상위 클래스가 대상과 같으면 건너뛴다. non-static inner 클래스는 바깥 클래스를 암묵적으로 참조하기 때문에 이 제외가 없으면 오탐이 난다.
  - 봉인 목록은 두지 않는다. 기대 위반은 0건이다.
- **공허 통과 방지**: `I.size() >= 200`을 함께 단정한다(도입 시점 실측 202). 패키지 필터가 틀려 `I`가 비면 이 규칙은 아무것도 검사하지 않고 통과하기 때문이다.
- **위반이 나면**: `@SuppressWarnings`나 예외 목록을 두지 않는다. 호출부가 UseCase 인터페이스를 주입하게 바꾼다. 필요한 메서드가 도메인 타입을 쓴다면 write 포트나 도메인 서비스를 직접 주입한다(아래 "QueryUseCase에는 컨트롤러 표면만 올린다" 절).
- **반증**: 구체 서비스 클래스를 주입하는 probe 클래스를 넣었을 때 실패하는 것을 확인했다.

### UseCase 필드명 — `useCaseFieldsShouldBeNamedUseCase`

**대상**: `useCaseFieldsShouldBeNamedUseCase`. 아래 모든 모듈의 `LayerRulesTest`에 있다.
- `application-core/src/test/.../architecture/LayerRulesTest`
- `{web,admin,ceo}-api/src/test/.../architecture/LayerRulesTest`
- `batch-module/src/test/.../architecture/LayerRulesTest`

- **검사 내용**: `port.in` 인터페이스 타입 필드는 이름이 `UseCase`로 끝나야 한다. 변수명이 타입을 따르게 하려는 것이다(backend/CLAUDE.md 포트·어댑터 네이밍 규칙).
- **도입 배경**: 도입 전에는 컨트롤러 69곳이 `MemberAuthCommandUseCase authCommandService`처럼 UseCase를 주입하면서 필드 이름은 `*Service`였다. 코드만 보면 구체 서비스를 주입한 것으로 읽혔다.
- **공허 통과 방지**: 모듈별로 대상 필드 수의 하한을 단정한다(도입 시점 실측의 약 80%: application 15 · web 40 · admin 80 · ceo 80 · batch 5). 필드 타입 해석이 일부만 깨져도 공허 통과하지 않게 하려는 것이다. 필드가 크게 줄어드는 리팩터링을 하면 하한을 함께 조정한다.
- **판정식은 5개 파일이 같아야 한다**: `isPortInInterface`와 테스트 본문은 모듈별 테스트에 복제돼 있다(공유 testFixtures를 두지 않는 관행). 판정식을 바꿀 때는 5곳을 함께 고친다.
- **검사 범위의 한계**: 필드만 검사하고 생성자 파라미터명은 검사하지 않는다. javac `-parameters` 설정 유무에 따라 파라미터명을 읽을 수 없기 때문이다. 파라미터명은 리네임할 때 필드와 함께 맞춘다.

### CQRS 규칙의 `*QueryUseCase` 확장 — `commandServicesShouldNotDependOnQueryPorts`

**대상**: `LayerRulesTest#commandServicesShouldNotDependOnQueryPorts`

- **변경 내용**: 금지 대상에 이름 접미어 `QueryUseCase`를 추가했다. 기존 금지 대상은 `QueryPort`·`QueryService`였다.
- **변경 이유**: 조회 협력 메서드가 `*QueryUseCase`에 올라가자, CommandService가 구체 `*QueryService` 대신 `*QueryUseCase`를 주입하면 이름 기준 규칙을 우회할 수 있게 됐다.
- **(유스케이스 분리) 대상 쪽 판정**: 규칙의 대상은 이름 접미어 `*CommandService`가 아니라 `UseCaseServices.commands()`(구현 포트가 `…QueryUseCase`가 아닌 유스케이스 서비스)다. 명령 서비스 이름에 `Command`가 없어(`PgPaymentConfirmService` 등) 접미어로는 대상을 고를 수 없기 때문이다. 금지 대상 쪽(`QueryPort`·`QueryService`·`QueryUseCase` 접미어)은 그대로다.
- **위반 현황**: 도입 시점 위반은 0건이고, probe로 규칙이 실패하는 것을 확인했다. 이 확장을 되돌리지 않는다.

### 유스케이스 서비스 1:1 규칙 4종 — 포트 하나 = 연산 하나 = 서비스 하나 (유스케이스 분리)

**대상**:
- `backend/application-core/src/testFixtures/java/com/tastyhouse/architecture/UseCaseServices.java` → `isUseCaseService` · `isQueryService` · `isCommandService` · `commands()` · `queries()`
- `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `useCaseServicesShouldImplementExactlyOneUseCase` · `useCasesShouldDeclareSingleOperation` · `useCaseServiceNameShouldMatchPort` · `useCaseServicesShouldHaveSinglePublicOperation` · `useCaseServices()`(하한 580)
- `backend/application-core/src/test/java/com/tastyhouse/application/architecture/RuleAnchorTest.java` → `commandServicesExist` · `queryServicesExist` · `moduleUseCaseCounts` · `inboundPortsExist`

| 항목 | before | after |
|---|---|---|
| 명령/조회 서비스 판별 | 클래스명 접미어 `*CommandService`/`*QueryService` | `UseCaseServices` 술어 — 유스케이스 서비스 = `..service..`의 비인터페이스 + `port.in` 인터페이스 구현 + batch 출처 아님. 그중 구현 포트명이 `QueryUseCase`로 끝나면 조회, 나머지는 명령 |
| 포트 구현 개수 | "최소 1개"(`commandServicesShouldImplementUseCase`·`queryServicesShouldImplementUseCase`) | **정확히 1개**(`useCaseServicesShouldImplementExactlyOneUseCase`) — 두 옛 규칙은 삭제 |
| 포트의 연산 수 | 제한 없음(`PaymentCommandUseCase` 메서드 6개 등) | web·admin·ceo 출처 `port.in` `*UseCase`는 추상 메서드 1개(`useCasesShouldDeclareSingleOperation`) — 의미가 다른 오버로드도 이름을 나눈 별도 포트로 둔다 |
| 서비스 이름 | 도메인당 `{도메인}CommandService`/`{도메인}QueryService` | 포트명의 `UseCase`를 `Service`로 바꾼 이름(`useCaseServiceNameShouldMatchPort`) — `PgPaymentConfirmUseCase` → `PgPaymentConfirmService` |
| 서비스의 public 메서드 | 제한 없음 | 생성자를 뺀 public 메서드 1개(`useCaseServicesShouldHaveSinglePublicOperation`, static·synthetic·bridge 제외) |
| anchor | `countSuffix` 기반 `commandServicesExist` ≥91 · `queryServicesExist` ≥100 | `countSuffix` 삭제. 술어 기반 ≥313 · ≥267, `moduleUseCaseCounts` web ≥185 · admin ≥207 · ceo ≥188 · batch =8 · core =0, `inboundPortsExist` ≥891 |

원문 취지:
- **왜 1:1인가**: 도메인당 한 쌍이던 시절 서비스 하나가 연산 수십 개를 떠안았다(web shop 조회 서비스 하나가 714줄·public 메서드 20개, admin shop 명령 서비스 하나가 UseCase 37개를 구현했다). 그러면 트랜잭션 속성·주입 의존이 그 클래스의 모든 연산에 묶이고, 한 연산을 고칠 때 무관한 연산이 같은 diff에 섞인다. 『만들면서 배우는 클린 아키텍처』의 `SendMoneyService implements SendMoneyUseCase` 형태로 바꿔, 주입 목록이 곧 그 연산 하나의 의존을 증명하게 했다. 네 규칙 중 하나만 빠져도 1:1이 무너진다 — 포트 2개를 구현하는 서비스, 연산 2개짜리 포트, 포트와 이름이 어긋난 서비스, public 헬퍼를 노출한 서비스가 각각 다른 규칙에서 잡힌다.
- **왜 판별을 서비스 이름이 아니라 구현 포트 이름으로 하나**: 명령 서비스 이름에서 `Command`가 빠졌다(`PgPaymentConfirmService`). 접미어로 고르던 CQRS 교차 주입 규칙(`commandServicesShouldNotDependOnQueryPorts`·`queryServicesShouldNotDependOnWritePorts`·`commandServicesShouldNotDependOnRequestRecords`)이 대상을 통째로 잃고 공허 통과하므로, 판별자를 포트 이름 끝의 `QueryUseCase` 하나로 옮겼다.
- **왜 batch는 제외하나**: `*SchedulerService` 8개는 이미 UseCase 1개 = 서비스 1개다. 이름은 `BatchSchedulerRulesTest`가 `*SchedulerService`로 지키므로 `useCaseServiceNameShouldMatchPort`(포트명 기반)와 충돌한다. `SearchKeywordSchedulerService`는 UseCase 하나에 메서드 2개라 `useCaseServicesShouldHaveSinglePublicOperation`·`useCasesShouldDeclareSingleOperation`에 걸리는데, 이번 분할의 범위 밖이라 손대지 않았다. 그래서 `isUseCaseService`와 `useCasesShouldDeclareSingleOperation`이 `ModuleOrigin.BATCH` 출처를 뺀다.
- **하한 580을 낮추지 않는다**: 출처 판정이나 `port.in` 판정이 깨지면 대상이 줄어 네 규칙이 공허 통과한다. 하한은 실측(web 185 · admin 207 · ceo 188)이다. 유스케이스를 의도적으로 삭제할 때만 삭제 후 실측값으로 맞춘다([ADR-0057](../../docs/adr/0057-anchor-floor-on-intentional-deletion.md)).
- **`allowEmptyShould`를 쓰지 않고, 위반은 손으로 모아 한 번에 보고한다**(`assertThat(violations).isEmpty()`).
- **반증**: 네 규칙 모두 위반 probe(포트 2개를 구현하는 임시 클래스, 추상 메서드 2개짜리 포트, 포트와 이름이 다른 서비스, public 메서드 2개짜리 서비스)로 실패를 확인하고 probe를 지웠다.
- **이 규칙이 생긴 김에 함께 바뀐 이름**: ceo 도메인 서비스 `ShopRequestCancelService`(요청 유형별 취소 분기)를 `ShopRequestCancellationService`로 개명했다(`backend/ceo-application/src/main/java/com/tastyhouse/application/shop/service/ShopRequestCancellationService.java`, 테스트 `ShopRequestCancellationServiceTest`). 유스케이스 `cancelRequest`의 서비스 이름이 `ShopRequestCancelService`가 되어 완전히 같은 이름이 생기기 때문이다 — **도메인 서비스는 명사형, 유스케이스 서비스는 동사형**으로 구분한다. `ServiceContextBoundaryTest.SEALED_VIOLATIONS`의 해당 FQN도 함께 바뀌었고 항목 수는 그대로다.
- **적용 범위 — 인바운드에만**: 유스케이스 1:1 분리(포트당 연산 1개·서비스 1개)는 `port.in` UseCase와 그 구현 서비스에만 적용한다. 네 규칙이 판정하는 것도 `port.in`뿐이다. 아웃바운드 `port.out` 포트와 persistence `*QueryAdapter`는 대상이 아니다 — 포트는 쓰는 쪽 기준(ISP)으로, 어댑터는 응집도로 크기를 정하고, 어댑터 1개가 포트 N개를 구현하는 형태를 유지한다. 근거는 `../infrastructure/jpa/AGENTS.md`의 [`<ctx>/query/` 절](../infrastructure/jpa/AGENTS.md#ctxquery--read-어댑터-cqrs-query-측-개정됨--읽기-경로-포트화) "유스케이스 1:1 분리는 read 어댑터에 적용하지 않는다" 항목에 있다.

### `queryServicesShouldNotDependOnWritePorts` carve-out 3건 — 목록에 새 항목을 추가하지 않는다

**carve-out은 1건(`ShopDeliveryTipViewQueryService`)이다.** 제목의 "3건"은 앵커 호환을 위해 그대로 둔다. admin·ceo 아이디 중복확인 서비스는 원시값을 돌려주는 조회 유스케이스라 write 포트가 아니라 QueryPort(`AdminQueryPort`·`CeoOwnerQueryPort`, 구현은 `..query..`의 QueryAdapter)로 읽는다. 같은 `existsByUsername`을 `AdminCreateService`·`CeoCreateService`(명령 서비스)가 생성 중복검사에 쓰므로 write 포트 메서드도 있다 — 같은 행을 읽는 메서드가 두 포트에 있는 것은 목적이 달라 허용된다.

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`
→ `queryServicesShouldNotDependOnWritePorts()`

조회 유스케이스 서비스(`UseCaseServices.queries()` — 구현 포트명이 `QueryUseCase`로 끝나는 서비스.)는 write 포트(`..port.out.write..`)를 주입하지 않는다 — 조회 트랜잭션(`readOnly = true`)에서 쓰기 경로가 열리는 것을 구조적으로 막는다.

| FQN | 근거 |
|---|---|
| `com.tastyhouse.application.shop.service.ShopDeliveryTipViewQueryService` (web) | write 포트를 배달팁 계산 경로가 도메인 서비스에 넘길 애그리거트 로드에 쓴다. 표현용 투영이 아니라 **도메인 계산 입력**이다 |

**판정 기준은 simple name이 아니라 FQN이다.** 4개 모듈이 하나로 합쳐지면서 동명 클래스가 한 importer에 들어왔기 때문이다 — 예컨대 `ShopQueryService`는 web·admin·ceo에 각각 존재했으므로 `haveSimpleNameNotEndingWith("ShopQueryService")`를 그대로 두면 **의도한 web 1개가 아니라 3개 전부가 면제**되어 admin·ceo의 위반이 조용히 통과했다. 이후 개명·평탄화로 simple name이 다시 유일해졌지만 **FQN을 유지한다** — 나중에 같은 접미어의 형제가 생겨도 면제 범위가 넓어지지 않기 때문이다.

**이 목록에 새 항목을 추가하지 않는다.**

**이 규칙은 직접 주입만 본다.** 조회 유스케이스 서비스가 도메인 서비스·`@Component` 협력 빈을 거쳐 write 포트를 읽는 간접 경로는 걸리지 않는다. 그 경로의 허용 여부는 `backend/CLAUDE.md`의 "write 포트 잔류 판정 기준" 절 아래 "간접 경로 판정"이 정한다 — 도메인 계산·판정의 입력이면 허용하고, 계산 없이 화면으로 나가는 읽기면 `{Ctx}QueryPort`로 옮긴다. 그 기준으로 옮긴 4건(`ProductPriceQueryService`·`ProductExposureQueryService`·`ShopStorePriceVerificationQueryService`·`MemberMyGradeQueryService`)의 before/after도 그 절에 있다.

### `commandRecordsShouldBeBoundaryTyped` carve-out 3건 — 느슨한 판을 batch에 적용하지 않는다

**carve-out은 1건(`MultipartFile`)이다.** 제목의 "3건"은 앵커 호환을 위해 그대로 둔다. Command의 구조적 가드는 `ApplicationException(ApplicationErrorCode.INVALID_INPUT)`을 던지고 domain 타입을 import하지 않으며, 페이징 타입 `PageQuery`/`PageResult`는 `com.tastyhouse.application.shared.port.out.page`에 있어 domain 타입이 아니므로 제외할 것이 더 없다. 경위는 [ADR-0042](../../docs/adr/0042-error-code-module-split.md)·[ADR-0025](../../docs/adr/0025-presentation-domain-cut.md).

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`
→ `commandRecordsShouldBeBoundaryTyped()`

Command record는 경계 타입만 싣는다. `com.tastyhouse.domain..`·`infrastructure..`·`org.springframework.web..`을 의존하지 않는다. 유일한 예외:

- `org.springframework.web.multipart..`(`MultipartFile`) — 업로드를 받는 연산은 `method(XxxCommand, MultipartFile)`처럼 별도 파라미터로 두는 것이 규정된 형태이고, ArchUnit 의존 그래프는 같은 패키지 UseCase 인터페이스의 메서드 파라미터까지 함께 잡는다. Command **필드**로 실리는 것은 `commandRecordsShouldNotHoldMultipartFile`이 따로 막는다.

즉 이것은 규칙을 무르게 하는 것이 아니라, 규칙이 애초에 겨냥하지 않던 대상을 제외하는 것이다. 도메인 **모델**(`domain.{shop,order,member}.model..` 등)은 그대로 금지이며, Command가 실제로 도메인 타입을 필드로 실으면 여전히 걸린다.

**batch는 출처 모듈(`ModuleOrigin.BATCH`)로 제외한다.** batch에는 Command record가 없고 인바운드 포트가 `void foo()`뿐이라 carve-out 없는 **엄격판**을 쓸 수 있으며, 그쪽은 `BatchSchedulerRulesTest.inboundPortsShouldBeBoundaryTyped()`가 맡는다. **batch를 이 규칙에 함께 넣으면 `MultipartFile` carve-out을 가진 느슨한 판에 얹혀 엄격함을 잃는다.**

**`allowEmptyShould(true)`는 이 파일 어디에도 쓰지 않는다.** 규칙이 대상을 잃으면 공허하게 통과시키지 말고 규칙을 지우거나 anchor를 고친다(`RuleAnchorTest`가 자동 검증).

### `inboundPortsShouldBeBoundaryTyped` — carve-out 1건뿐인 엄격판, 죽은 코드로 보고 지우지 말 것

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/BatchSchedulerRulesTest.java`
→ `inboundPortsShouldBeBoundaryTyped()`

**carve-out이 없다** — `com.tastyhouse.domain..`·`infrastructure..`·`org.springframework.web..`을 전부 금지한다(제목의 "1건"은 앵커 호환을 위해 그대로 둔다). web·admin·ceo가 쓰는 업로드 carve-out(`MultipartFile`)은 여기 없다: batch에는 파일 업로드가 없어 느슨하게 할 이유가 없다.

**주의 — 지금은 검사할 표면이 없다.** UseCase 8개가 전부 파라미터·반환값 없는 `void foo()` 하나뿐이라 의존 그래프에 잡힐 타입 자체가 0건이다(`inboundPortsExist`가 세는 것은 "인터페이스가 존재함"이지 "검사 대상이 있음"이 아니다). **규칙과 carve-out은 UseCase가 처음으로 파라미터를 갖는 시점을 위해 미리 세워 둔 것이므로, 지금 아무것도 걸리지 않는다는 이유로 carve-out을 죽은 코드로 보고 지우지 말 것.**

같은 파일의 다른 고정 사항.

- **대상은 importer가 아니라 출처 모듈(`ModuleOrigin.from(ModuleOrigin.BATCH)`)로 좁힌다.** 패키지는 5모듈이 같아 importer로는 앱을 가를 수 없다. 다른 앱까지 대상에 들어오면 그 앱들이 정당하게 쓰는 업로드 타입에 걸려 실패한다.
- **대상이 0건이 된 규칙은 `allowEmptyShould(true)`로 공허 통과를 열지 않고 삭제한다**는 것이 이 저장소의 방침이다(`responseRecordsShouldBeDomainAndInfraFree`와 그 anchor `responseRecordsExist`를 실제로 삭제한 선례).
- anchor 2종은 batch가 규모가 작아 **정확히 일치**로 둔다(다른 앱은 하한). 잡이 늘거나 줄면 **이 숫자를 의식적으로 고치게 되는 것이 의도다.**

### `ShopStorePriceVerificationItemCommand` — 정적 참조가 없어도 죽은 코드가 아니다

**대상**: `backend/ceo-application/src/main/java/com/tastyhouse/application/shop/port/in/ShopStorePriceVerificationItemCommand.java`

`ShopStorePriceVerificationItemCommand`는 multipart의 **문자열 파트**로 들어온다. 컨트롤러도 Request record도 domain-free여야 해 파싱을 할 수 없으므로, Command가 원문을 `String items`로 담아 넘기고 `ShopStorePriceVerificationRequestService`가 `ObjectMapper`로 이 record 목록으로 역직렬화한다. 그래서 이 record는 어느 UseCase 시그니처에도, 어느 부모 Command의 컴포넌트로도 등장하지 않는다 — **정적 참조가 없다는 이유로 지우지 않는다.** 이 record의 앱 소속은 `ceo-application` 모듈에 있다는 사실 자체가 표현한다.

같은 형태(런타임 역직렬화로만 생성되는 record)를 새로 만들면 이 항목에 추가한다. 정적 참조가 없는 record는 대개 진짜 죽은 코드이므로, 이 항목에 넣기 전에 그 record를 **어디서 만드는지**를 먼저 찾는다.

### `CeoLoginService` — 기록 실패 정책의 의도적 비대칭 (인증 조회 carve-out)

**대상**: `backend/ceo-application/src/main/java/com/tastyhouse/application/auth/service/CeoLoginService.java` → `login` · `recordFailureQuietly`

login·refresh·logout은 `CeoLoginService`·`CeoTokenRefreshService`·`CeoLogoutService` 세 서비스이고, 아래 정책은 로그인 연산을 맡은 `CeoLoginService`에 있다. 세 서비스 모두 트랜잭션을 열지 않는다. 기록 호출은 `CeoLoginSuccessRecordUseCase#recordSuccess`·`CeoLoginFailureRecordUseCase#recordFailure` 두 포트로 한다.

**기록 실패 시 정책은 성공·실패 경로가 의도적으로 비대칭이다.**

- **성공 경로**: 기록 실패를 그대로 전파한다. 접속기록 없이 토큰이 발급되는 상태를 만들지 않는다 — 개인정보처리시스템 접속기록은 법적 요구사항이므로, 남기지 못했다면 접속도 허용하지 않는 편이 옳다.
- **실패 경로**: 기록 실패를 catch·로깅하고 원래 인증 예외를 rethrow한다. **감사 쓰기 실패가 인증 실패 응답 계약(401 `CEO_AUTHENTICATION_FAILED` 등)을 500으로 바꾸면 안 된다.**

이 비대칭은 `CeoLoginServiceTest`가 봉인한다.

### `CeoLoginServiceTest` — 점주 로그인 접속기록 배선 봉인 (소셜 4종 분기 carve-out)

**대상**: `backend/ceo-application/src/test/java/com/tastyhouse/application/auth/service/CeoLoginServiceTest.java`

대상 서비스는 `CeoLoginService`이고, 기록 포트는 `CeoLoginSuccessRecordUseCase`·`CeoLoginFailureRecordUseCase` mock이다.

이 테스트가 지키는 것은 네 가지다.

- 성공·실패 양쪽 모두 이력을 남긴다(실패 이력이 인증 예외와 함께 사라지지 않는다).
- 실패 시 **원래 인증 예외가 그대로 rethrow**된다 — 응답 계약이 바뀌지 않는다.
- 존재하지 않는 username은 기록하지 않는다(**계정 존재 여부 탐색 표면 방지**).
- 기록 실패 시 정책이 성공·실패 경로에서 **의도적으로 비대칭**이다.

### 읽기 계약 carve-out 5종 — 도메인 타입을 강등해 나르는 이유

아래 record들은 전부 **api 모듈이 도메인 타입을 알 수 없다는 경계** 때문에 존재한다. "중복 DTO"로 보고 합치거나 도메인 타입을 그대로 실으면 ArchUnit 규칙이 깨진다.

| 대상 (`backend/application-core/src/main/java/com/tastyhouse/application/...`) | 봉인 취지 |
|---|---|
| `product/port/out/ProductAvailabilityChangeView.java` | 판매상태 변경은 **Command 경로**의 반환값이라 조회 계약은 아니지만 코어 `product/port/out`에 둔다 — `commandServicesShouldNotDependOnQueryPorts`(CQRS 교차 주입 금지)는 이름 기준이라 이 타입을 반환하는 명령 서비스가 걸리지 않는다(위 "Command 경로의 반환 Result도 코어 `<ctx>/port/out`에 둔다"). `Failure`는 에러코드 타입이 아니라 `String code, String message`를 싣는다 — api 모듈은 domain·application 에러코드 타입을 볼 수 없다. 실패를 담아 나르는 값 `ProductAvailabilityFailure`와 결과 `ProductAvailabilityChangeResult`는 ceo-application만 만들고 쓰므로 `backend/ceo-application/src/main/java/com/tastyhouse/application/product/service/`에 있다(상세는 `backend/ceo-application/AGENTS.md`) |
| `region/port/out/AdminDongBoundaryViewResult.java` | `AdminDongBoundaryResult`는 DAO가 읽어 온 **인코딩된** `boundary` 문자열을 그대로 들고 있어 그 자체로는 응답을 만들 수 없다. 디코딩은 ceo-application `region/service/AdminDongBoundaryQueryService`가 `domain/shared/geo/GeoPolygonTextCodec.decodeRings`를 직접 호출해 수행하므로 **application에 남아야 하고**, 표현 계약이 `from(Result)` 한 번으로 끝낼 수 있도록 디코딩을 마친 이 타입을 따로 둔다. 좌표를 `GeoRing`·`GeoPoint`가 아니라 낱개 `BigDecimal` 쌍(`Point`)으로 내리는 이유는 api 모듈이 `com.tastyhouse.domain..`을 carve-out 없이 전면 금지하기 때문이다(`controllersShouldBeDomainFree`). 리포 전체에서 api 모듈이 geo 타입을 참조하는 곳은 한 곳도 없으며, **그 경계를 깨지 않는다** |
| `review/port/out/ReviewBlindReasonView.java` | 카탈로그는 도메인 enum의 `values()`를 훑어 만드는데 그 메서드는 api 모듈에 허용된 accessor가 아니므로(`apiModuleShouldOnlyReadDomainEnums` — 덩어리 01로 삭제됐고, 지금은 api 모듈이 domain을 아예 못 봐서 같은 결론) 목록 구성이 application에 남는다. **도메인 enum을 그대로 담지 않고 문자열로 강등해 나른다** — 인바운드 포트의 반환 타입에 `com.tastyhouse.domain..`이 실리면 `commandRecordsShouldBeBoundaryTyped`(carve-out은 예외·페이징 계약뿐)에 걸린다. **목록 요소는 제네릭 타입 인자로도 잡힌다** |
| `shop/port/out/GeoPointView.java` | 도형 계산은 도메인 기하 타입으로 수행하는데 api 모듈은 그 타입을 알 수 없다 — `apiModuleShouldBeDomainModelFree`의 carve-out은 `domain.exception..`·`domain.shared.page..`·도메인 enum뿐이고 **`domain.shared.geo..`는 포함되지 않는다**(덩어리 01로 carve-out이 전부 사라졌다). 추가로 **컴포넌트 선언 순서는 알파벳순(`latitude` → `longitude`)이다** — 둘 다 `BigDecimal`이라 순서가 어긋나면 컴파일은 통과하고 **값만 조용히 뒤바뀐다** |
| `shop/port/out/ShopStorePriceVerificationViewResult.java` | 세 출처를 합친다 — 최신 인증 요청(애그리거트), 인증 여부 플래그, 미충족 메뉴 목록(도메인 서비스). 앞의 둘은 애그리거트에서, 마지막은 도메인 서비스에서 나오므로 표현 계약이 직접 받을 수 없다(`apiModuleShouldBeDomainModelFree`). 미충족 사유는 `domain.product.model`의 `StorePriceUnverifiedItem`을 그대로 넘기지 않고 `UnverifiedItem`으로 옮겨 담는다 — 그 타입은 domain 타입이고 api 모듈은 `com.tastyhouse.domain..`을 carve-out 없이 전면 금지하기 때문이다. `status`·`UnverifiedItem.reason`도 `String`으로 강등해 나르고, 강등은 이 View를 만드는 서비스가 한다 |

### `ShopStorePriceVerificationRequestService` — 인덱스 기록이 도메인이 아니라 이 서비스에 있는 이유

**대상**: `backend/ceo-application/src/main/java/com/tastyhouse/application/shop/service/ShopStorePriceVerificationRequestService.java` → `requestVerification` · `toItemSpecs`

이 서비스는 `ShopStorePriceVerificationRequestUseCase`를 구현하며 유일한 연산은 `requestVerification`이다.

- **`items`가 JSON 문자열인 것은 요청 형식이 multipart이기 때문이다.** 가격표 이미지와 대상 목록은 한 트랜잭션에 함께 들어와야 한다 — 2단 요청으로 쪼개면 중간에서 끊긴 요청이 첨부만 있고 대상이 없는 고아 상태로 남고, 관리자 검수 큐에 검수할 수 없는 건이 쌓인다. multipart는 JSON 바디를 함께 실을 수 없으므로 목록만 문자열 파트로 받아 여기서 파싱한다.
- **인덱스 기록이 도메인이 아니라 이 서비스에 있는 것은 컨텍스트 경계 때문이다.** 다른 요청 유형(`ShopImageApprovalService`·`ShopDeliveryAreaAdjustmentService`)은 shop 컨텍스트 소유라 도메인 서비스가 직접 `ShopRequestIndexRecorder`를 호출한다. 그러나 인증 요청 애그리거트는 **product** 컨텍스트 소유여서, 그 도메인 서비스가 `shop.service`를 호출하면 `ContextBoundaryTest` 위반이 되고 **봉인 목록은 늘릴 수 없다.** 두 컨텍스트를 한 트랜잭션에서 잇는 일은 표현 계층의 몫이다.
- `MultipartFile`을 파라미터로 받는 것은 **파일 업로드 경계의 문서화된 예외**다 — 규격 검증이 업로드보다 앞서야 하고, 도메인은 통과분의 `fileId`만 받는다.

### QueryDSL 투영 전용 생성자 3건 — "never used" 경고를 근거로 삭제하지 말 것

**대상** (`backend/application-core/src/main/java/com/tastyhouse/application/review/port/out/`)

| record | 좁은 시그니처가 제외하는 것 | 투영 호출부 |
|---|---|---|
| `ReviewDetailResult` | 1:N인 이미지·태그 | `ReviewQueryAdapter` |
| `LatestReviewListItemResult` | 1:N인 이미지(`imageUrls`를 빈 목록으로 채운다) | `ReviewFeedQueryAdapter#selectLatestReviews`(투영 1곳. large-class-split 덩어리 04 이전에는 `ReviewQueryAdapter`의 6개 쿼리가 같은 투영을 각자 복제했다) |
| `ReviewManagementDetailResult` | 1:N인 이미지·태그 | `ReviewManagementQueryAdapter#findReviewManagementDetail` |

세 record는 canonical 생성자 외에 **QueryDSL 투영 전용 생성자**를 하나 더 갖는다. DAO가
`Projections.constructor`로 **리플렉션 호출**하므로 정적 호출부가 0개이고, 그래서 **IDE가
"never used"로 표시한다.** 그 경고를 근거로 지우면 **컴파일은 통과하고 그 쿼리가 실행되는 순간에만
500이 난다.**

- **파라미터 개수·타입·순서가 DAO의 select 인자와 정확히 일치해야 한다.** `Projections.constructor`는
  `Class<?>`를 받아 런타임에 생성자를 찾으므로 불일치도 컴파일에 걸리지 않는다. `@QueryProjection`에서
  전환하며 **컴파일 게이트가 사라졌고, 인자 개수 가드 테스트
  (`infrastructure:jpa`의 `ProjectionConstructorMatchingTest`)가 유일한 방어선**이다.
- record는 반드시 `public`이어야 한다 — package-private이면 `getConstructors()`가 찾지 못해
  `ExpressionException: No constructor found`로 실패한다(`ShopRiderGuidePickupPresenceResult` 장애 선례).
- **원문 주석 1건은 낡아 있었으므로 여기 옮기며 교정했다** — `ReviewManagementDetailResult`의 주석은
  "제거하면 Q타입이 생성되지 않아 빌드가 깨진다"고 적혀 있었으나, `@QueryProjection` → `Projections.constructor`
  전환으로 **`QReviewManagementDetailResult` Q타입은 더 이상 생성되지 않는다**(실측 0건). 실제 위험은
  빌드 실패가 아니라 **런타임 투영 실패**다.

### `ResultWitherComponentOrderTest` — `port/out` Result wither의 컴포넌트 순서 가드 (이 모듈의 유일한 방어선)

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/ResultWitherComponentOrderTest.java`
→ `witherArgumentsShouldFollowComponentOrder`, `detectWitherReordering`, 짝 테스트 `detectorShouldCatchSwappedSlots`

**검사 대상**: 소스 루트 5개 — `backend/{application-core,web-application,admin-application,ceo-application,batch-application}/src/main/java` — 아래 `com/tastyhouse/application/**/port/out/*.java`의 record가 가진(앱 모듈의 `port/out` record도 빠지지 않게 5모듈을 모두 훑는다)
`public {자기 record명} with\w+(...)` 메서드 전부. 도입 시점 15개였고, 02 롤아웃(URL 투영)으로 URL 변환용
wither 3개가 빠져 **현재 13개**다 — `MenuReviewWritableItemResult#withProductImageUrl`·
`MenuReviewListItemResult#withMemberProfileImageUrl`은 호출부가 0이 되어 삭제했고,
`OrderProductResult#withResolvedImageUrl(url, options)`는 URL 인자를 떼고 `#withOptions(options)`로 축소했다.
`OrderProductResult#withOptions` · `OrderDetailResult#withOrderProducts`/`#withPayment` ·
`ReviewBlindNoticeResult#withImageUrls` · `ReviewBlindRequestDetailResult#withUrls` ·
`ReviewManagementDetailResult#withImageUrls`/`#withTagNames` · `ReviewDetailResult#withImageUrls`/`#withTagNames` ·
`ShopReviewManagementDetailResult#withCollections` ·
`ShopReviewManagementListItemResult#withImageUrls`/`#withProductNames` · `LatestReviewListItemResult#withImageUrls`

- **wither는 전 컴포넌트를 위치 기반으로 재나열한다.** 인접한 같은 타입 컴포넌트를 바꿔 써도 컴파일되고 어떤
  테스트도 잡지 못한다. 가장 위험한 것은 `ShopReviewManagementListItemResult`(13개 컴포넌트를 **두 번**
  재나열하며 `imageUrls`·`productNames`가 인접한 `List<String>`)와 `OrderDetailResult`(24개 컴포넌트 중
  금액·포인트 `Integer`가 9개, 가게·주문자 `String`이 5개 연속)다.
- **`infrastructure:jpa`(jpa 모듈 분리 전 `infrastructure:persistence`)의 가드 2종은 이 모듈을 스캔하지 않는다.** `ProjectionConstructorMatchingTest`는
  자기 모듈 소스(`Projections.constructor` 인자)만 보고, 재조립 헬퍼 봉인은 개수만 센다. 그래서 이 테스트가
  wither 순서에 대한 **유일한 방어선**이다. 지우거나 `@Disabled`하지 않는다.
- **wither는 제거 대상이 아니다.** 별도 쿼리로 얻는 컬렉션(이미지·태그·상품명·주문 옵션)과 서브 애그리거트(주문 상품·결제)
  보강은 컬럼 표현식이 될 수 없어 post-fetch가 정상 형태다. 반대로 **URL 슬롯만 바꿔 끼우는 wither는 만들지 않는다** —
  URL 변환은 `infrastructure:jpa`가 투영식의 `fileUrlResolver.urlOf(...)`로 끝내고, 그쪽의 `withResolved*`
  재조립 헬퍼는 02 롤아웃으로 0개가 됐다.

**판정 방식** — 인자마다 아래 셋 중 하나로 본다. 어느 것에도 해당하지 않는 인자(`List.of()`·메서드 호출 등)는
건너뛴다.

| 인자 형태 | 의미 | 실패 조건 |
|---|---|---|
| `this.foo`(인자 전체가 이 형태일 때만) | 기존 필드 복사 | `foo`가 그 자리의 컴포넌트명과 다르면 |
| `foo`(wither 파라미터가 아님) | 기존 필드 복사(`this.` 생략형) | `foo`가 컴포넌트명인데 그 자리와 다르면 |
| `foo`(wither 파라미터) | 교체 슬롯 | 파라미터명이 **컴포넌트명과 같은데** 그 자리와 다르면. 이름이 다른 파라미터(`resolvedImageUrl`)는 건너뛴다 |

인자 개수가 컴포넌트 개수와 다르면 그것만으로 실패다. 이와 별개로 두 가지를 더 단정한다.

- **wither 파라미터는 전부 인자로 쓰여야 한다.** `withProductNames(productNames)`의 본문이 `this.productNames`를
  그대로 쓰면 모든 인자가 제자리라 순서 검사는 통과하지만, wither가 **기존 값을 그대로 반환**한다. 두 wither가
  같은 13개 인자를 복붙하는 `ShopReviewManagementListItemResult`에서 가장 일어나기 쉬운 실수다.
- **리플렉션으로 센 wither 수 = 소스에서 파싱한 wither 수.** 레코드마다 `getDeclaredMethods()` 중 비정적·비합성이고
  이름이 `with[A-Z]…`이며 반환 타입이 자기 record인 메서드를 세어, 시그니처 패턴(`public {Record} with\w+(`)이
  놓친 wither(`final`·제네릭 메서드·package-private 등)를 조용히 건너뛰지 않게 한다. 같은 이유로 `port/out`에
  record 선언이 있는데 클래스를 로드하지 못하면 건너뛰지 않고 실패한다.

- **`this.` 접두 인자만 보면 안 된다.** 원 스펙은 `this.` 인자만 검사하도록 설계했으나, 도입 시점 전수 확인에서
  15개 중 **8개가 `this.` 없이 필드명을 그대로** 쓰는 것으로 드러났다(`OrderProductResult` 1 ·
  `OrderDetailResult` 2 · `ReviewManagementDetailResult` 2 · `ReviewDetailResult` 2 · `LatestReviewListItemResult` 1).
  `this.`만 보면 이 8개는 검사 인자가 0개가 되어 공허하게 통과한다 — 그중 `OrderDetailResult`가 금액·포인트
  `Integer` 9개가 연속한 가장 위험한 record다. 그래서 파라미터가 아닌 bare 식별자도 필드 참조로 판정한다.
- **오탐이 없는 근거**: record 메서드 본문에서 파라미터가 아닌 bare 식별자는 컴포넌트 필드를 가리킬 수밖에 없고,
  현존 wither는 교체 슬롯에 파라미터명을 그대로 쓴다(`imageUrls` 파라미터 → `imageUrls` 자리). 교체 슬롯에
  이름이 다른 파라미터를 쓰는 형태(`resolvedImageUrl`)는 판정하지 않을 뿐 실패시키지 않는다.
- **시그니처 파싱만 `<`·`>`를 괄호 깊이로 센다.** `List<String> a, Map<K, V> b` 같은 제네릭 쉼표 때문이다.
  `new` 인자 파싱에서는 세지 않는다 — 인자 안의 `->`·비교 연산자가 깊이를 깨뜨린다.
- **공허 통과를 막는 장치 2겹**: `checked` 카운터는 `isPositive()`로 스캔 경로가 살아 있음을 단정하고
  (도입 시점 실측 15), 짝 테스트 `detectorShouldCatchSwappedSlots`는 합성 record `SwapProbe`에 대해 세 형태의
  교차를 **실제로 잡아내는지** 단정한다. 현존 wither가 전부 통과하는 상태에서는 검출기가 고장 나도 알 수 없으므로,
  짝 테스트를 지우지 않는다. `checked`를 15로 고정하지 않는 것은 wither 추가가 정상 변경이기 때문이다 — 누락은
  위 리플렉션 대조가 record 단위로 잡는다.
- **알려진 한계**: `new {Record}(` 탐색은 시그니처 뒤 **첫 번째** 등장을 쓰므로, 다른 wither에 위임하거나
  (`return withBoth(x, this.y);`) 본문에서 두 번 생성하는(if/else) wither는 정확히 검사되지 않는다. 현존 15개에는
  없는 형태다. 보조 생성자의 `this(...)` 위임 인자 순서(`OrderProductResult`의 축약 생성자)도 이 가드의 대상이 아니다.
- **이 테스트가 실패하면 그것은 가드의 오작동이 아니라 발견된 프로덕션 버그다.** 테스트를 고치거나 완화하지 말고,
  리팩터링과 분리한 별도 커밋(`fix(query): ...`)으로 해당 record의 wither 인자 순서를 바로잡은 뒤, 그 Result를
  쓰는 엔드포인트를 실제로 호출해 값이 교차돼 있었는지 확인한다.

### `//noinspection BusyWait` — 이 억제 마커는 정당하며 제거 대상이 아니다

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/crawling/bbq/BbqService.java`
→ 카테고리 루프의 `Thread.sleep(10000)`

**이 모듈에서 유일하게 남아 있는 주석 형태의 코드**다(챕터 04의 주석 전량 제거에서 의도적으로 제외).
정적분석 도구가 읽는 억제 마커이므로 주석이 아니라 **코드로 취급한다.**

억제가 정당한 이유는 **busy-wait가 아니라 외부 BBQ 서버 부하 방지를 위한 의도적인 요청 간
지연**이기 때문이다. 루프 안의 `Thread.sleep`이라는 형태만 보고 "폴링을 이벤트 대기로 바꾸라"는
지적으로 오인해 지연 자체를 없애면, 크롤링이 외부 서버를 연속 타격한다. 마커와 지연 둘 다 유지한다.

### 이벤트 리스너 단위 테스트 12종 — 현재 동작 봉인 (공통 규칙)

리스너 테스트는 **리스너의 현재 동작을 봉인하는 순수 단위 테스트**다. 아래가 12개 파일 전부에 적용되는 공통 규칙이며, 개별 항목은 이 규칙에서 벗어나는 것만 아래에 따로 적는다.

- **스프링 컨텍스트 없이 리스너를 직접 생성해 핸들러를 호출한다** — `AFTER_COMMIT` 발화 자체는 프레임워크 몫이라 검증 대상이 아니다. 스프링 배선을 검증하려고 `@SpringBootTest`를 붙이지 않는다.
- 협력자 없이 기록만 하는 리스너는 **무엇이 기록되는지**를 `LogCapture`로 확인한다.

**공통 유틸**: `backend/application-core/src/testFixtures/java/com/tastyhouse/testsupport/shared/logging/LogCapture.java`

리스너 12개 중 7개는 협력자 없이 `log.info(...)`만 수행한다. 이런 리스너에서 "무엇을 하는지"는 곧 "무엇을 기록하는지"이므로, **로그를 관측하지 않으면 핸들러 본문을 통째로 지워도 통과하는 공허한 테스트만 남는다.** 그래서 Logback `ListAppender`를 대상 로거에 직접 붙여, 이벤트의 어떤 값이 기록에 반영되는지까지 봉인한다. **사용 후에는 반드시 `detach()`를 호출한다**(JUnit `@AfterEach`) — 떼지 않으면 같은 로거를 쓰는 다른 테스트가 실행될 때 이벤트가 계속 쌓인다.

공통 규칙만 적용되는 파일 — `coupon/listener/CouponEventListenerTest.java` · `member/listener/MemberEventListenerTest.java`(가입·탈퇴는 web-api와 admin-api 양쪽에서 트리거되지만 리스너 자체는 발행 경로를 알지 않는다) · `policy/listener/PolicyActivatedEventListenerTest.java`.

#### 개별 예외 — 공통 규칙 위에 추가로 봉인하는 것

| 대상 (`backend/application-core/src/test/java/com/tastyhouse/application/...`) | 추가로 봉인하는 것 |
|---|---|
| `file/listener/FileUploadedEventListenerTest.java` | 기록되는 것은 **저장 경로**이지 표시용 URL이 아니다 — URL 변환은 조회 시점에 query DAO가 `FileUrlResolver`로 수행하므로 리스너가 경로를 그대로 남기는 것이 정상이다 |
| `emailverification/listener/MailVerificationEventListenerTest.java` | **이 리스너가 메일을 발송하지 않는 것이 정상**이라는 점을 함께 고정한다 — 이 이벤트는 인증 **완료** 시점이고 발송은 **발급** 시점에 필요하므로, 발송은 `MailVerificationService#issue`가 발급과 원자적으로 수행한다 |
| `phoneverification/listener/SmsVerificationEventListenerTest.java` | 위와 동일한 이유로 **발송하지 않음**을 고정한다 — 발송은 `SmsVerificationService#issue`가 발급과 원자적으로 수행한다 |
| `point/listener/PointEventListenerTest.java` | **이 리스너가 잔액을 건드리지 않는 것이 정상**임을 고정한다 — 포인트 증감은 `PointLedgerService`가 이벤트 발행 **이전에** 이미 수행했고 리스너는 기록만 한다. **협력자를 주입받지 않는 생성자가 그 증거이며, 여기에 원장 서비스가 추가되면 이중 정산이 된다** |
| `member/listener/ReferralRegisteredEventListenerTest.java` | referral↔point 두 컨텍스트를 잇는 지점이라 검증 대상이 로깅이 아니라 **적립 2건과 보상 완료 전이가 모두, 그리고 그 순서대로 일어나는가**이다 |
| `notification/listener/ReviewOwnerReplyEventListenerTest.java` | review↔notification을 잇는 지점이라 검증 대상은 **답변 등록 이벤트가 리뷰 작성자 앞으로 알림을 적재하는가**이다. 수신자가 `reviewerMemberId`(작성자)여야 하고 이동 대상이 그 리뷰여야 한다. 가게명은 `ShopBasicInfoQueryPort#findShopName`으로 조회하므로(테스트는 `mock(ShopBasicInfoQueryPort.class)`) **조회가 비어 있는 경우까지 함께 봉인한다** — 알림 본문에 "null 사장님"이 새는 것을 막기 위함이다 |
| `notification/listener/ReviewBlindApprovedEventListenerTest.java` | 게시중단 승인 이벤트가 **리뷰 작성자 앞으로 게시중단 기한을 담은 알림을 적재하는가**를 `NotificationService` mock의 `notifyReviewBlindApproved` 호출 인자로 검증한다. 가게명 조회가 없는 것이 이 리스너의 의도이므로(아래 배치 근거 참고) 조회 협력자를 추가하지 않는다 |
| `payment/listener/PaymentEventListenerTest.java` | 로그만 남기는 다른 리스너와 달리 **실제 금전 효과**(포인트 증감)를 낸다. 따라서 "무엇을 기록하는가"가 아니라 **"어떤 조건에서 원장 서비스를 호출/미호출하는가"**를 검증한다 — 조건 분기가 잘못되면 적립이 이중으로 되거나 환급이 누락되며, `AFTER_COMMIT`이라 실패해도 재시도가 없다. 특히 **환불 요청 접수 시점에는 아무것도 하지 않는 것이 이 핸들러의 계약이다** — 접수 시점에 포인트가 움직이면 이후 취소가 확정될 때 `PaymentCancelledEvent`가 같은 금액을 다시 반영해 **이중 정산**이 된다 |
| `product/listener/ProductMenuReviewEventListenerTest.java` | 상품 평점·평가 수라는 **영속 상태**를 갱신하므로 "어떤 상품 id로 통계 갱신을 호출하는가"를 검증한다. **이벤트 3종 모두가 같은 재집계를 트리거해야 한다** — 하나라도 빠지면 `PRODUCT.rating`이 조용히 낡는다 |

### 이벤트 리스너 — 지우거나 되돌리면 안 되는 것

`infrastructure:persistence`에서 리스너와 함께 옮겨 온 금지 항목이다. 아래 `.../` 경로의 루트는 `backend/application-core/src/main/java/com/tastyhouse/application/`다.

#### 알림 리스너의 `@Async` + `AFTER_COMMIT` + `REQUIRES_NEW` 3종 세트를 부분적으로 떼지 않는다

**대상**: `.../notification/listener/ReviewBlindApprovedEventListener.java` · `.../notification/listener/ReviewOwnerReplyEventListener.java` · `.../product/listener/ProductMenuReviewEventListener.java`

- `@EnableAsync`는 코어 소유다 — `.../shared/config/AsyncConfig.java`(package-private `@Configuration`)에만 두며 4앱이 공통으로 쓴다. 앱 모듈에 두면 다른 앱의 리스너가 동기로 돈다(가드: `AsyncRulesTest`와 각 api 모듈 `LayerRulesTest.appModuleShouldNotEnableAsync`).
- `@Async`가 빠지면 호출 스레드에서 동기 실행되어 **알림 실패가 원본 API로 전파된다** — DB에는 반영됐는데 화면은 실패로 뜬다.
- `REQUIRES_NEW`가 빠지면 커밋될 트랜잭션이 없어 **리스너가 조용히 아무것도 남기지 않는다.**

`ProductMenuReviewEventListener`의 **`productId == null` 가드도 유지한다** — 컬럼이 NOT NULL이라는 이유로 지우면, 향후 발행 경로가 늘어 null이 실릴 때 그 예외가 `AFTER_COMMIT`에서 조용히 유실된다.

#### `PaymentEventListener#onRefundRequested`는 포인트를 건드리지 않는 것이 계약이다

**대상**: `.../payment/listener/PaymentEventListener.java` → `onRefundRequested`

이 이벤트는 환불 **접수** 시점이며 실제 금전 정산은 결제가 취소로 확정될 때 `PaymentCancelledEvent`가 수행한다. **여기서 포인트를 함께 움직이면 승인 전 요청만으로 잔액이 바뀌고, 이후 취소 확정 시 같은 금액이 두 번 반영된다.** 이 핸들러는 접수 사실만 남기며, DB를 쓰지 않으므로 `REQUIRES_NEW`도 열지 않는다.

`onPaymentCompleted`의 **현장 결제 한정 적립**도 계약이다 — PG 결제는 주문 접수 시점에 이미 처리됐다.

#### `ReferralRegisteredEventListener`의 처리 순서를 바꾸지 않는다

**대상**: `.../member/listener/ReferralRegisteredEventListener.java`

**적립 먼저, 보상 완료 전이는 그 다음이다.** 전이가 먼저 커밋되면 "완료로 표시됐지만 포인트는 없는" 추천 관계가 남아 적립 실패 건을 상태로 식별할 수 없게 된다. 지금 순서라면 적립 실패 시 추천 관계가 `PENDING`에 머물러 재처리 대상으로 남는다.

적립을 이 리스너에서 도메인 서비스로 되돌리지 않는다 — 적립 시맨틱(잔액 증가 + EARNED 이력 + 적립 이벤트)의 단일 원천은 `PointLedgerService`다.

#### `ProductMenuReviewEventListener`가 REVIEW 이벤트를 다시 구독하게 하지 않는다

**대상**: `.../product/listener/ProductMenuReviewEventListener.java`

`PRODUCT.rating`의 근거가 MENU_REVIEW로 완전히 옮겨갔다. **두 리스너가 같은 `ProductReviewStatsService`를 호출하면 재집계가 두 번 돌고 "어느 쪽이 진짜 근거인가"가 코드에서 사라진다.**

#### 인증 리스너에 발송을 추가하지 않는다

**대상**: `.../emailverification/listener/MailVerificationEventListener.java` · `.../phoneverification/listener/SmsVerificationEventListener.java`

이 이벤트는 인증 **완료** 시점이고 발송은 **발급** 시점에 필요하므로 시점이 다르다. 발송은 `MailVerificationService#issue`·`SmsVerificationService#issue`가 발급과 원자적으로 수행한다.

### enum → `String` 강등 후 Object 타입 API는 컴파일러가 잡지 못한다 — 먼저 `valueOf`로 승격한다

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/**/service/*.java` 중 `*Result`의 강등된 문자열 필드를 도메인 enum과 비교·조회하는 곳 (발견 사례: `backend/web-application/src/main/java/com/tastyhouse/application/shop/service/ShopOrderMethodQueryService.java`의 주문 방식(order-methods) 조회 — `OrderMethod`)

덩어리 01로 `*Result`의 도메인 enum 필드가 `String`이 됐다. 필드 타입을 바꾸면 대부분의 사용처는 컴파일 에러로 드러나지만, **`Object`를 받는 API는 문자열을 그대로 받아들여 컴파일이 통과한다.** 검증 중 실제 사례: `Map<OrderMethod, ...>.get(dto.orderMethod())`가 `Map.get(Object)`라 컴파일은 됐지만 키 타입이 달라 **항상 `null`**을 돌려줬다.

| 같은 부류 | 증상 |
|---|---|
| `Map.get` / `Map.containsKey` | 항상 `null` / `false` |
| `Object#equals` (`enumValue.equals(result.status())`) | 항상 `false` |
| `Collection.contains` / `remove` | 항상 `false` |
| AssertJ `isEqualTo` (enum 기대값 vs 문자열 실제값) | 테스트가 실패하거나, 반대쪽으로 짜면 잘못된 단정이 통과 |

**규칙**: Result의 문자열을 도메인 enum과 비교·조회하려면 **먼저 승격한다**(`OrderMethod.valueOf(dto.orderMethod())`, 같은 방식의 `DayType.valueOf(...)`·`ClosedDayType.valueOf(...)`). 반대로 enum 쪽을 `.name()`으로 내려 문자열끼리 비교해도 되지만, 한 파일 안에서 두 방식을 섞지 않는다. 문자열 값은 DAO의 `x.status.stringValue()` 투영에서 오며 엔티티 enum이 전부 `EnumType.STRING`이라 `name()`과 같다(`valueOf` 승격이 안전한 근거).

**enum 필드를 또 강등할 때**: 그 필드를 읽는 모든 곳을 `grep`으로 찾아 `Map`·`equals`·`contains`·`isEqualTo` 사용처를 **눈으로** 확인한다 — 빌드 성공은 증거가 아니다.

### 03a로 domain에서 옮겨 온 봉인 항목 (도메인 서비스·포트·테스트)

아래 항목은 원래 `domain/AGENTS.md`에 있었다. 대상 코드(포트 주입 도메인 서비스·그 포트·그 단위 테스트)가 03a로 이 모듈로 옮겨오면서 **소유 모듈의 문서로 함께 옮겼다**(규칙: 설명은 그 코드를 소유한 모듈의 `AGENTS.md`에 산다). 본문은 그대로이고 앵커 경로와 경계 테스트 이름만 새 위치로 고쳤다 — 서비스 사이의 컨텍스트 경계는 이제 domain `ContextBoundaryTest`가 아니라 이 모듈의 `ServiceContextBoundaryTest`가 강제한다.

#### `ReplyPhraseTextValidatorPort` — 포트 carve-out (금칙어 검증기를 직접 부르지 않는다)

**대상**: `backend/ceo-application/src/main/java/com/tastyhouse/application/ceo/port/out/ReplyPhraseTextValidatorPort.java`(앱 마커 제거로 `backend/application-core/...`에서 이동 — 구현 `ReplyPhraseProhibitedWordValidatorAdapter`와 유일한 소비자가 ceo라 ceo 전용 SPI가 됐다)

실제 검수 규칙(금칙어 목록 대조)은 shop 컨텍스트의 `ProhibitedWordValidator`가 소유하고, 이 포트의 어댑터가 그것을 그대로 호출한다 — **규칙을 복제하지 않는다.**

**ceo 도메인이 `ProhibitedWordValidator`를 직접 부르지 않는 이유는 컨텍스트 경계다.** 컨텍스트 간 참조는 ID VO·도메인 이벤트·출력 포트로만 허용되고 타 컨텍스트의 `service` 직접 import는 금지되어 있다(`ServiceContextBoundaryTest`). **기존에 같은 검증기를 직접 import하는 도메인 서비스들이 있으나 그것은 규칙 도입 이전 코드로 봉인된 것이라 선례로 삼지 않는다.**

위반 시 `BusinessException(SHOP_TEXT_PROHIBITED_WORD)`(400)을 던진다.

#### `StorePriceVerificationPort` — CQRS write 포트 잔류 carve-out

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/product/port/out/StorePriceVerificationPort.java`

메뉴 가격 저장은 product 컨텍스트의 규칙이지만, "매장가·픽업가를 설정할 수 있는가"와 "배달가가 매장가를 넘어 인증을 내려야 하는가"는 **가게 단위 상태**다. 컨텍스트 경계 규칙(`ServiceContextBoundaryTest`)이 타 컨텍스트의 `model`·write 포트(`port.out.write`, 03a 이전 `repository`, 03b 동안 `store` — **persistence domain 재허용으로 다시 `port.out.write`**)·`service` 직접 참조를 금지하므로, product는 이 포트로만 그 상태를 다룬다 — **`ShopLoadPort`·`ShopSavePort`를 직접 주입하면 신규 위반이 되고 봉인 목록은 늘릴 수 없다.**

구현은 `StorePriceVerificationAdapter`(`backend/application-core/src/main/java/com/tastyhouse/application/shop/service/StorePriceVerificationAdapter.java`, `@Service`)가 `ShopLoadPort`·`ShopSavePort`에 위임한다. 도메인 모델 `Shop`을 로드해 `verifyStorePrice()`/`clearStorePriceVerification()`을 호출하고 `ResourceNotFoundException(SHOP_NOT_FOUND)`를 던지므로 application 계층에 둔다(persistence 조회 DAO는 domain을 모른다). **shop에 둔 이유는 `ShopRequestIndexSyncAdapter`와 같다** — product 쪽에 두면 어댑터가 shop의 write 포트를 참조해 `ServiceContextBoundaryTest` 위반이 되고, shop에 두면 "shop이 product의 포트(`port.out`)를 구현"하는 허용된 방향만 남는다.

#### `StorePriceVerificationService` — 애그리거트를 product가 소유하는 배치 (위 포트와 짝)

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/product/service/StorePriceVerificationService.java`

**왜 shop이 아니라 product 컨텍스트가 소유하는가**: 승인이 하는 일의 본체는 `PRODUCT_PRICE`의 매장가·픽업가를 채우는 것이다. 요청 애그리거트를 shop에 두면 그 승인 경로가 `product.model`·product의 write 포트를 import해야 해 컨텍스트 경계 규칙(`ServiceContextBoundaryTest`)을 위반하는데, **그 봉인 목록은 늘릴 수 없다.** 그래서 인증 요청 애그리거트 자체를 product가 소유하고, 가게 단위 상태인 인증 ON/OFF 플래그만 `StorePriceVerificationPort`로 다룬다 — **이 방향이 경계 위반 없이 성립하는 유일한 배치다.**

함께 고정되는 제약.

- 테이블명이 `SHOP_STORE_PRICE_VERIFICATION`인 것은 요청이 **가게 단위**로 접수되기 때문이며, 소유 컨텍스트와는 별개다.
- **승인은 요청 시점의 매장가를 쓴다** — 항목(`StorePriceVerificationItem`)에 박제된 값이며, 승인 시점에 현재 가격을 다시 읽지 않는다. 그러지 않으면 검수자가 보지 않은 값이 승인된다.
- **인증을 켜기 전에 항목을 먼저 반영한다.** 순서를 뒤집으면 반영 도중 실패했을 때 인증만 켜진 채 매장가가 비어 있는 상태가 남는다.
- 상태 전이는 원본 전이와 **같은 트랜잭션**에서 인덱스에 동기 기록한다. **전이 메서드마다 이 호출을 넣는다** — 한 곳이라도 빠지면 점주 화면의 요청처리 현황이 원본과 영구히 어긋난다. **배선이 api 모듈이 아니라 이 도메인 서비스에 있는 것이 중요하다** — 승인·반려는 admin이, 취소는 ceo가 호출하므로 api 모듈에 두면 같은 전이가 두 모듈로 흩어져 한쪽이 반드시 빠진다.
- 인증 상태 → 통합 상태 매핑을 product가 소유하는 이유도 컨텍스트 경계다 — shop이 `StorePriceVerificationStatus`(product 소유)를 알면 `ServiceContextBoundaryTest`를 위반한다. 다섯 상태가 이름까지 대응하지만 `name()`을 그대로 흘려보내지 않고 **명시 매핑**을 두는 이유는, 어느 한쪽 enum에 상수가 추가되면 **컴파일 단계에서** 대응을 결정하도록 강제하기 위해서다.
- 할인 중인 메뉴는 인증 요청 대상이 아니다(승인 시 매장가가 할인가와 뒤엉킨다). 판정식은 `ProductPriceService`와 같다 — 이 저장소에 할인 스케줄링이 없어 "할인가 존재"로 본다.
- 가격 행이 정말 그 메뉴의 것인지 확인한다. **확인하지 않으면 남의 메뉴 가격 행에 매장가를 심을 수 있다.**

#### 도메인 서비스 단위 테스트 10종 — 불변식 봉인 (테스트 스텁 carve-out)

아래 테스트들은 **현재 동작을 봉인**하는 것이 목적이다. 리팩터링으로 테스트가 깨지면 테스트를 고치기 전에 **봉인된 불변식을 깼는지 먼저 확인한다.** 전부 write 포트·이벤트 발행 포트를 fake로 대체해 Spring/DB 없이 판정 로직만 검증하는 순수 단위 테스트다.

| 대상 (`backend/application-core/src/test/java/com/tastyhouse/application/...`) | 봉인하는 불변식 |
|---|---|
| `menureview/service/MenuReviewLifecycleServiceTest.java` → `register_succeedsWithoutStoreReview` | **매장 리뷰가 없어도 메뉴 평가가 등록된다**(설계 원칙 1의 회귀 방어). 이 서비스가 `ReviewLoadPort`·`ReviewSavePort`를 **아예 주입받지 않는 것 자체가** 그 원칙의 구조적 보증이며, 테스트는 그 상태를 봉인한다 |
| `product/service/OrderProductValidationServiceTest.java` | 필수 옵션그룹을 비운 주문, 숨긴·품절 옵션을 실은 주문 차단. 전부 "프론트만 막고 서버는 통과시키던" 결함이다. 3단계 보증금이 도입되면 후자는 "보증금 옵션을 숨겨 보증금 없이 주문"하는 경로가 되므로, 이 테스트가 그 우회를 **영구히 봉인한다** |
| `product/service/ProductRepresentativeApprovalServiceTest.java` | 세 제약(최대 6개 · 이미지 필수 · 최소 1개 유지). 특히 **최소 1개 유지가 기존 `PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE`를 재사용**하는 것이 핵심 — 새 코드로 갈라지면 같은 불변식에 프론트가 두 갈래를 분기해야 하고 일괄 숨김 경로와 하한이 어긋난다 |
| `review/service/ReviewBlindRequestServiceTest.java` | 스펙의 세 규칙 — **1회 제한**(단 `CANCELED`는 예외) · **고객 동의 삭제** · **타인 리뷰 접근 차단**. 추가로 `IndexSync` 중첩 클래스가 신규 전이 2종(`EXPIRED`/`DELETED`)이 종결(`APPROVED`)로 접히는지 봉인한다 — 목록에 "재노출"·"삭제"라는 없는 통합 상태가 새어 나가면 안 된다. 원본→통합 상태 매핑은 컨텍스트 경계 때문에 recorder가 아니라 이 서비스가 소유한다 |
| `review/service/ReviewOwnerReplyServiceTest.java` | **30일 작성 제한이 등록에만 걸리는지**를 봉인한다. 기한 판정 기준일을 파라미터로 받는 설계 덕에 시계 조작 없이 29·30·31일차를 지정할 수 있다 — **도메인이 `LocalDate.now()`를 직접 부르면 이 테스트 자체가 불가능하다** |
| `shop/service/ShopCeoAssignmentServiceTest.java` | `ShopCeoAssignmentService`의 상태 규칙 표 전체. 특히 **재배정이 `REVOKE`+`GRANT` 2행**인 것을 봉인한다 — 한 행에 before/after를 담는 형태로 되돌아가면 "언제부터 언제까지 권한이 있었는가"를 읽을 수 없게 된다 |
| `shop/service/ShopLifecycleServiceTest.java` | 가게 등록 시 접근권한 이력 기록. 등록에서 점주를 함께 배정하는 것도 접근권한 부여이므로 나중에 배정한 경우와 **구별 없이 `GRANT` 이력이 남아야** 하고, 반대로 점주 없이 등록하면 아무 행도 남지 않아야 한다 |
| `shop/service/ShopMenuCollectionImageServiceTest.java` | 규칙이 전부 **행 하나만 보고는 판정할 수 없는 집합 차원**이라 애그리거트 단위 테스트로는 한 줄도 검증되지 않는다. **정원(최대 6개)은 상태를 가리지 않는다**(대기·반려 건도 슬롯을 차지한다), **순서 변경은 replace-all**(부분·초과·미지의 id 목록은 전부 거절 — 부분 목록을 받아주면 낡은 화면의 요청이 빠진 이미지를 목록 끝으로 밀어낸다). 가게 소유가 아닌 id는 존재를 알리지 않고 `SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND`로 합쳐 **IDOR을 막는 경로도 함께 봉인**한다 |
| `shop/service/ShopRequestCancellationServiceTest.java`(지금 위치는 `backend/ceo-application/src/test/java/com/tastyhouse/application/shop/service/` — 대상 도메인 서비스 `ShopRequestCancellationService`가 ceo-application에 있다. 유스케이스 분리 전 이름은 `ShopRequestCancelServiceTest`·`ShopRequestCancelService`) | 세 규칙 — (1) `PENDING`만 취소된다, (2) `IN_PROGRESS`는 409로 거부된다(가맹본부에 자료가 전달된 뒤라 플랫폼이 일방 취소할 수 없다), (3) **취소는 원본 애그리거트의 상태를 바꾼다**. (3)이 핵심으로, 인덱스에만 `CANCELED`를 두면 원본이 `PENDING`으로 남아 중복 차단이 재요청을 계속 막고 관리자가 취소된 요청을 승인·반려할 수 있다 |
| `shop/service/ShopRequestIndexRecorderTest.java` | 원본 → 통합 상태 **매핑 표를 전수** 봉인한다. 특히 조정 신청의 `COMPLETED → APPROVED`는 유일하게 값 이름이 어긋나는 매핑이라, 고정하지 않으면 목록에 "완료"라는 없는 상태가 새어 나가거나 매핑이 조용히 뒤집힌다. 게시중단만은 **통합 상태를 그대로 받는다** — 컨텍스트 경계 때문에 recorder가 `review.model.ReviewBlindStatus`를 import할 수 없어 매핑을 `ReviewBlindRequestService`가 소유하기 때문이다 |

#### 리뷰 부가 리포지토리 Fake 2종 — 보관하지 않는 것이 의도다

**대상**: `backend/application-core/src/testFixtures/java/com/tastyhouse/testsupport/review/service/FakeReviewTagSavePort.java`
→ 클래스 선언 / `saveAll(List<ReviewTag>)` · `deleteByReviewId(ReviewId)`
**대상**: `backend/application-core/src/testFixtures/java/com/tastyhouse/testsupport/review/service/FakeReviewImageSavePort.java`
→ 클래스 선언 / `saveAll(List<ReviewImage>)` · `deleteByReviewId(ReviewId)`

`ReviewTagSavePort`·`ReviewImageSavePort`(Save 포트만 있는 쓰기 전용 포트)에는 **조회 메서드가 없다.** 저장한 태그·이미지를
되읽어 검증할 수단이 계약에 없으므로, 이 Fake들은 보관용 컬렉션을 두지 않고 호출을 삼키기만 한다 —
협력 객체를 채우는 용도의 스텁이다.

**빈 메서드 본문을 "미구현"으로 오인해 채우지 않는다.** 보관 컬렉션을 추가해도 그것을 읽어 단언할
포트 메서드가 없어 검증에 쓰이지 못하며, 조회 계약이 실제로 생기면 그때 함께 채운다.

#### 03a로 `infrastructure:persistence`에서 옮겨 온 봉인 항목

아래 두 항목의 대상 클래스(`ShopRequestIndexSyncAdapter`·`CachingProhibitedWordLoadPort`(분리 전 `CachingProhibitedWordPersistencePort`))는 03a로 persistence에서 이 모듈의 `shop/service/`로 옮겨왔다. 본문은 `infrastructure/persistence/AGENTS.md`에 있던 그대로다.

##### `ShopRequestIndexSyncAdapter`의 enum 승격 실패를 삼키지 않는다

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/shop/service/ShopRequestIndexSyncAdapter.java`

포트 시그니처가 `String`인 것은 통합 상태 `ShopRequestStatus`가 shop 소유라 product 쪽 포트에 등장할 수 없기 때문이다. **승격 실패는 프로그래밍 오류(양쪽 enum이 어긋난 상태)이므로 `from(String)`의 400 변환에 맡기지 않고 그대로 전파시킨다** — 조용히 넘기면 인덱스가 원본과 어긋난 채 남는다.

이 기록은 **이벤트·`AFTER_COMMIT`이 아니라 원본 상태 전이와 같은 트랜잭션에서 동기 수행한다** — 기록 유실이 곧 "요청이 목록에서 사라짐"이기 때문이다. 리스너로 옮기지 않는다.

##### `CachingProhibitedWordLoadPort`에 락을 추가하지 않는다

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/shop/service/CachingProhibitedWordLoadPort.java`

`AtomicReference`에 스냅샷을 통째로 담아 교체하므로 락이 필요 없다. 만료 직후 동시 호출이 겹치면 적재가 중복될 수 있으나 결과가 같은 read-only 조회라 무해하며, **중복 적재를 막는 락이 주는 이득보다 락 경합 비용이 크다.** TTL을 제거해 무기한 캐싱으로 바꾸지도 않는다 — 시드 갱신이 재기동 전까지 반영되지 않는다.

### 매퍼·enum 비교값 봉인 항목

아래 두 매퍼 항목의 대상은 persistence `<ctx>/persistence/*Mapper`·`*MapperTest`다. 같은 봉인 규칙이 `../infrastructure/jpa/AGENTS.md`에도 있다.

#### 매퍼 테스트의 필드 값을 같은 값으로 채우지 않는다

**대상**: `backend/infrastructure/jpa/src/test/java/com/tastyhouse/infrastructure/jpa/<ctx>/persistence/*MapperTest.java` → 각 `reconstitute`·`XxxJpaEntity.create` 호출 인자

**같은 타입의 연속 필드(`String`·`Long`·`boolean`·`LocalDateTime`)는 반드시 서로 다른 값으로 채운다.** `title`과 `content`에 같은 문자열을 넣으면 매퍼가 둘을 뒤바꿔도 단언이 통과한다. 같은 타입 컴포넌트의 순서 뒤바뀜을 잡는 것은 이 테스트뿐이다. 자식 컬렉션·Embeddable이 있으면 비우지 말고 채워서 검사한다.

#### 매퍼의 null 가드를 "NOT NULL 컬럼이라 불필요"하다며 지우지 않는다

**대상**: `backend/infrastructure/jpa/src/main/java/com/tastyhouse/infrastructure/jpa/<ctx>/persistence/*Mapper.java` → `x == null ? null : XxxId.of(x)` 형태 전부

**모든 FK에 예외 없이** 삼항 가드를 둔다. 컬럼이 NOT NULL이어도 도메인 모델이 미배정 상태를 `null` VO로 들 수 있고(`toEntity` 방향 NPE), nullable FK는 **그 행이 실제로 있을 때만** `XxxId.of(null)`로 터진다. 컬럼별로 가드 유무를 나누지 않는다.

#### DAO에 enum 상수명을 리터럴로도, 복제 상수로도 쓰지 않는다 — 비교값은 포트 인자로 받는다

**대상**: `backend/infrastructure/jpa/**/*.java` 전체 · `backend/application-core/src/main/java/com/tastyhouse/application/**/port/out/`

리터럴 `"COMPLETED"`는 도메인 enum 상수가 개명·삭제돼도 컴파일되고 조회가 **조용히 0건**이 된다. application이 도메인 enum의 `name()`을 포트 인자로 넘기면, 상수가 개명·삭제될 때 **application 호출부가 컴파일 에러**가 난다(위 "enum 비교값 전달 규칙"). persistence에 enum 상수 리터럴이나 복제 enum(`XxxCodes`)을 들이지 않는다. 쓰기 어댑터 `XxxPersistenceAdapter`는 도메인 enum을 참조할 수 있으므로 비교값을 `XxxStatus.X.name()`으로 만든다(리터럴 금지는 그대로). 조회 DAO는 포트 인자로 받는다. 옵션 가용성 결과의 `"NORMAL"`/`"COMMON"` 리터럴 4곳도 같은 이유로 `ProductAvailabilityQueryPort#findProductOptionAvailability(condition, normalOptionType, commonOptionType)` 인자로 바꿨다. 잔존 검사: `grep -rnE '"[A-Z][A-Z_]{2,}"' --include='*.java' infrastructure/jpa/src/main | grep -vE '@Table|@Column|@Index|name = "|columnDefinition|columnList'` → 0건.

#### 정산이 limit 밖 행의 `memberId`까지 검증하는 것을 "불필요한 변환"이라며 줄이지 않는다

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/rank/service/RankSettlementService.java` → `settle`의 `memberIds`

03b 이전에는 `MemberReviewCount.memberId`가 `MemberId`여서 persistence가 **조회된 모든 행**을 `MemberId.of`로 변환했고, 0 이하·null id가 한 행이라도 있으면 정산 전체가 실패했다. `Long`으로 강등된 뒤에도 그 동작을 지키려고 `settle`은 상위 `limit`개가 아니라 **전 행**을 `MemberId`로 변환해 두고, `buildRanks`는 그중 앞 `limit`개만 쓴다. 변환 대상을 `limit`개로 좁히면 잘못된 id가 조용히 통과한다.

### UseCase 구현 서비스·리스너·설정에 `public`을 붙이지 않는다 (package-private 적용, 5모듈 공통 정본)

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `useCaseImplementationsShouldNotBePublic` · `serviceClassesShouldNotBePublic` · `publicByNecessityShouldStillBePublic` · `listenersAndConfigsShouldNotBePublic`

5모듈(`application`·`{web,admin,ceo,batch}-application`)에서 215개를 package-private으로 좁혔다 — `..service..`에서 `port.in` UseCase를 구현하는 서비스 202개(web 48 · admin 59 · ceo 88 · batch 7 `*SchedulerService`), 리스너(`<ctx>/listener`) 12개, `SharedBeanConfig` 1개. **후속 적용으로 협력 서비스 73개를 더 좁혔다** — `..service..`의 UseCase 비구현 class 103개 중 다른 패키지의 main·test가 타입으로 참조하지 않는 것(application 22 · web 16 · admin 3 · ceo 27 · batch 5). `useCaseImplementationsShouldNotBePublic`은 UseCase 구현만 걸러서 이들을 검사하지 않는 빈틈이 있었고, `serviceClassesShouldNotBePublic`이 그것을 메운다. 컨트롤러·협력 서비스는 UseCase 인터페이스로만 주입받고, 빈은 앱의 `ApplicationLayerScanConfig` 문자열 스캔으로 등록되므로 구현 클래스 이름이 패키지 밖에 나타날 필요가 없다. `useCaseImplementationsShouldNotBePublic`은 대상이 200개 이상인지(하한) 함께 확인해, 술어가 어긋나 대상을 잃고 공허하게 통과하는 것을 막는다. 생성자·메서드의 `public`은 유지한다(`@Transactional`은 public 메서드에만 적용된다).

**public으로 남는 것**:

| 대상 | 이유 |
|---|---|
| `port.in`·`port.out`·`*Result`·`*Command` | 모듈 간 계약 |
| `..service..`의 협력 서비스 30개 | 다른 패키지가 import한다 — 허용 목록 `PUBLIC_BY_NECESSITY`(아래 표). 같은 패키지에서만 쓰이는 73개는 package-private으로 좁혔다 |
| `SpringDomainEventPublisher` | 다른 패키지의 `SharedBeanConfig`가 참조한다(`..service..` 밖이라 허용 목록 대상 아님) |

**`serviceClassesShouldNotBePublic`의 대상과 판정**: `..service..` 패키지(하위 패키지 포함)의 최상위 class 중 interface·enum·record(`isAssignableTo(Record.class)`)·중첩 클래스를 뺀 것이다. 허용 목록에 없는 것이 `public`이면 실패하고, 대상이 650개 미만이면 공허 통과로 보고 실패한다(도입 시점 681개 = 711 − 30). 짝 테스트 `publicByNecessityShouldStillBePublic`은 목록의 FQN이 사라졌거나, 더는 검사 대상(`..service..` 최상위 class)이 아니거나, 더는 `public`이 아니면 실패한다. record·enum 6개(`ProductAvailabilityChangeResult` 등)는 반환·계약 타입이라 대상에서 뺐다.

**등재 기준 — 다른 *패키지*가 import할 때만이다.** 코어 클래스를 앱 모듈이 **같은 자바 패키지**에서 쓰는 것(split package)은 등재 사유가 아니다. 이름이 같은 패키지이고 fat jar의 클래스로더가 하나라서 package-private 접근이 성립하기 때문이다(73개 중 20개가 이 형태다 — 예: `shop/service/ShopLifecycleService` ← admin·ceo·web. 선례 `product/service/ProductOptionDepositAmounts`·`ShopCodeDescriptions`).

**`PUBLIC_BY_NECESSITY` 30개와 사유** (`com.tastyhouse.application.` 생략, 참조는 `모듈:패키지`):

| FQN | 참조하는 다른 패키지 |
|---|---|
| `coupon.service.CouponIssueService` | web-application:order.service |
| `emailverification.service.MailVerificationService` | web-application:auth.service |
| `file.service.FileUploadService` | batch-application:crawling.bbq |
| `holiday.service.PublicHolidayCalendar` | ceo-application:product.service · web-application:order.service · web-application:shop.service |
| `member.follow.service.MemberFollowService` | web-application:follow.service |
| `member.referral.service.ReferralRegistrationService` | web-application:member.service |
| `member.referral.service.ReferralRewardCompletionService` | application:member.listener |
| `member.service.GradeSettlementService` | batch-application:grade.service |
| `member.service.MemberAuthService` | web-application:auth.service |
| `member.service.MemberDeliveryAddressService` | web-application:order.service |
| `member.service.MemberRegistrationService` | web-application:auth.service |
| `member.service.OrdererLookupService` | web-application:order.service |
| `notification.service.NotificationService` | application:notification.listener |
| `order.service.OrderTransitionService` | application:payment.service · web-application:payment.service |
| `payment.service.PaymentConfirmationService` | application:payment.listener |
| `point.service.PointLedgerService` | application:member.listener · application:payment.listener · web-application:order.service |
| `product.service.OrderProductValidationService` | web-application:order.service |
| `product.service.ProductRegistrationService` | batch-application:crawling.bbq |
| `product.service.ProductReviewStatsService` | application:product.listener |
| `product.service.StorePriceVerificationService` | ceo-application:shop.service |
| `review.service.ReviewBlindRequestService` | batch-application:reviewblind.service · ceo-application:shop.service |
| `review.service.ReviewSortSpecs` | web-application:product.service |
| `shop.service.CachingProhibitedWordLoadPort` | application:shared.config |
| `shop.service.OwnedShopIdProvider` | ceo-application:product.service |
| `shop.service.ProhibitedWordValidator` | application:shared.config · ceo-application:product.service · ceo-application:review.service |
| `shop.service.ShopFoodTypeCategoryReader` | ceo-application:product.service |
| `shop.service.ShopOrderAvailabilityService` | web-application:reservation.service |
| `shop.service.ShopOrderContextService` | web-application:order.service |
| `shop.service.ShopOwnershipValidator` | ceo-application:product.service · ceo-application:review.service |
| `shop.service.ShopRequestIndexRecorder` | application:review.service |

목록에 FQN을 추가할 때는 이 표에 사유 행을 함께 추가하고, 참조가 사라져 좁혔으면 두 곳에서 함께 지운다.

새 UseCase 구현 서비스·협력 서비스·리스너·`@Configuration`은 `public` 없이 만든다. 근거와 전체 범주는 `backend/CLAUDE.md`의 "접근 제어자 규칙 (내부 구현은 package-private)" 절.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

챕터 04에서 이 모듈의 java 주석 11,312줄을 전부 제거하며, 코드만 읽어서는 도달할 수 없는
설계 근거를 여기로 옮겼다. 각 절은 **어느 코드 요소에 붙어 있던 서술인지**를 앵커로 밝힌다.

### PG 승인·실패 반영은 결제 행을 잠그고, "이미 처리됨" 거절은 실패로 바꾸지 않는다

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/payment/service/PaymentConfirmationService.java` → `recordPgAttempt` · `applyPgConfirmation` · `reconcileApproved` · `failPgConfirmation` · `isNotAwaitingPgApproval` · `isAlreadyAppliedApproval`, 계약 `backend/application-core/src/main/java/com/tastyhouse/application/payment/port/out/PgConfirmFailure.java` · `.../port/out/PgPaymentLookupResult.java` · `.../payment/service/PgReconcileOutcome.java`, 도메인 `backend/domain/src/main/java/com/tastyhouse/domain/payment/model/Payment.java` → `recordPgAttempt` · `complete` · `fail`, 오케스트레이터 `backend/web-application/src/main/java/com/tastyhouse/application/payment/service/PgPaymentConfirmService.java` → `confirmPgPayment`, 취소 차단 `backend/web-application/src/main/java/com/tastyhouse/application/payment/service/PaymentCancellationService.java` → `applyCancellation`

같은 결제에 토스 승인 요청이 두 번 오면(버튼 연타·성공 리다이렉트 재요청) 두 요청이 모두 잠금 없는 `preparePgConfirmation`을 통과해 PG를 두 번 부른다. 첫 요청은 성공하고 두 번째는 `ALREADY_PROCESSED_PAYMENT` 등으로 거절된다. 실패 반영이 결제를 잠금 없이 읽고 PENDING이면 FAILED로 저장하면, 저장이 load-copy-save라 늦게 커밋한 쪽이 이긴다. 그러면 "돈은 결제됐는데 결제는 FAILED"가 두 방향으로 생긴다 — 실패 반영이 승인 반영보다 늦게 커밋하면 결제 FAILED·주문 CONFIRMED, 먼저 커밋하면 승인 반영이 `PAYMENT_NOT_PENDING_APPROVAL`로 롤백돼 결제 FAILED·주문 PENDING(`PG_DB_MISMATCH`).

- **PG 시도 기록·승인 반영·대사 반영·실패 반영 모두 `PaymentLoadPort#findByPgOrderIdForUpdate`로 읽는다.** 같은 결제 행을 비관적 쓰기 잠금으로 직렬화하므로, 앞 트랜잭션이 이미 잠금을 쥐고 있었다면 뒤 트랜잭션은 그 커밋 결과를 본다. 실패 반영이 승인 반영보다 먼저 잠금을 잡는 순서는 막지 못한다(세 단계가 호출마다 독립 트랜잭션이라서다). 일반 `findByPgOrderId`로 되돌리면 경합이 조용히 재발한다 — `PaymentConfirmationServiceTest#applyAndFail_loadPaymentWithLock`이 잠금 조회 사용을 단언한다.
- **실패 반영은 `PgConfirmFailure.REJECTED`일 때만 결제 상태를 바꾼다.** `UNKNOWN`은 아래 항목대로 WARN 후 종료하고, 그 밖의 값(`ALREADY_PROCESSED` 포함)은 원장(`TOSS_PAYMENT_RECORD`)만 남기고 로그 없이 종료한다. "이미 처리됨"을 FAILED로 바꾸면 다른 요청이 승인했다는 사실과 어긋나기 때문이다. 토스 문자열은 벤더 어댑터가 번역하고, application은 `PgConfirmFailure`만 본다. **운영 경로에서 이 메서드는 `REJECTED`·`UNKNOWN`만 받는다** — web `PgPaymentConfirmService`는 `REJECTED`만 원래 승인 결과로 넘기고 `UNKNOWN`·`ALREADY_PROCESSED`는 토스 조회 결과(`failure`는 `REJECTED`·`UNKNOWN`뿐)를 넘기며, batch `PaymentReconcileExecutor#failUnapprovedInNewTx`도 조회 결과만 넘긴다. 그래서 `ALREADY_PROCESSED`에 대한 별도 로그 분기를 두지 않고 "`REJECTED`만 상태를 바꾸는 가드" 하나로 막는다. 분기를 통째로 지우면 `ALREADY_PROCESSED`가 들어왔을 때 아래 `payment.fail()`로 떨어지므로 가드는 지우지 않는다. FAILED인데 PG가 실제로 승인한 결제는 조회 `APPROVED` → `reconcileApproved`가 COMPLETED로 복구하고, 복구에 실패하면 아래 대사 결과의 `PG_DB_MISMATCH` ERROR가 알린다(`PaymentConfirmationServiceTest#failPgConfirmation_keepsPendingWhenAlreadyProcessed`·`#failPgConfirmation_keepsFailedWhenAlreadyProcessed`·`#failPgConfirmation_keepsCompletedWhenAlreadyProcessed` — 세 상태 모두 상태 무변경·로그 없음). 마커 문자열은 `payment/service/PaymentLogMarkers`(package-private 상수 홀더)의 `PG_DB_MISMATCH`(PG와 DB가 어긋남)·`PG_CONFIRM_UNKNOWN`(PG 결과를 확정하지 못함)·`PG_RECONCILE_MANUAL_REQUIRED`(batch 대사의 마지막 시도에서도 확정하지 못해 자동 대사가 끝남) 셋이다. 이 코어의 `PaymentConfirmationService`(결과 불명 경고·대사 불일치)와 `web-application`의 `PgPaymentConfirmService`(승인)·`PaymentCancelService`(환불), `batch-application`의 `PaymentReconcileSchedulerService`(주기 대사)가 같은 패키지(split package)로 이 상수를 참조한다. 홀더를 따로 두는 이유는 환불·승인 서비스가 승인 반영 서비스에 결합하지 않게 하기 위해서이고, 마커 철자가 한 곳에만 있어야 운영 알럿 검색에서 빠지는 로그가 생기지 않는다.
- **`PgConfirmFailure`는 세 가지다 — `REJECTED`(PG가 거절함), `ALREADY_PROCESSED`(다른 요청이 처리함), `UNKNOWN`(결과를 모름).** 어댑터가 5xx(`PG_SERVER_ERROR`)·통신 예외(`SYSTEM_ERROR`)·빈 응답(`UNKNOWN_ERROR`)·본문을 해석할 수 없는 4xx(`PG_UNREADABLE_RESPONSE` — 토스 에러 형식이 아니라 출처를 알 수 없는 응답)를 `UNKNOWN`으로 분류한다. `UNKNOWN`이면 실패 반영이 원장만 남기고 **결제 상태를 바꾸지 않으며** `PaymentLogMarkers.PG_CONFIRM_UNKNOWN`으로 WARN을 남긴다(`PaymentConfirmationServiceTest#failPgConfirmation_keepsPendingWhenUnknown`). FAILED는 "PG가 거절함" 또는 "조회로 미승인 확인"일 때만 생긴다.
- **PG 승인을 부르기 직전에 PG 시도를 기록한다(`recordPgAttempt`).** 결제 행을 잠그고 PENDING이면 `Payment#recordPgAttempt`로 `pg_provider`만 채운다. **PENDING이 아니면 `ApplicationException(PAYMENT_NOT_PENDING_APPROVAL)`로 저장 없이 끊는다** — 사전 확인 뒤 사용자가 취소한 결제에 PG 승인이 나가지 않게 하기 위해서다(호출부 `PgPaymentConfirmService`가 그 예외를 그대로 던져 PG를 부르지 않는다). 도메인 메서드 `Payment#recordPgAttempt`도 PENDING이 아니면 `DomainException(DomainErrorCode.PAYMENT_NOT_PENDING)`을 던진다 — `fail`·`complete`와 같은 형태의 방어선이고, 서비스가 먼저 거르므로 정상 흐름에서는 도달하지 않는다(`PaymentConfirmationServiceTest#recordPgAttempt_rejectsCancelledPayment`, `PgPaymentConfirmServiceTest#recordAttemptRejected_doesNotCallPg`, 도메인 `PaymentTest#recordPgAttempt_throwsWhenNotPending`). 미지원 PG는 `PgPaymentConfirmService`가 기록 **전에** `pgPaymentGateway.supports`로 거절한다(`PG_PROVIDER_UNSUPPORTED`) — 기록 뒤에 라우터가 거절하면 PG를 부르지 못한 결제가 "PENDING + `pg_provider`"로 남아 취소가 막히기 때문이다(`#unsupportedProvider_rejectsWithoutRecordingAttempt`). "PENDING + `pg_provider` 있음"이 "PG가 돈을 받았을 수 있는 결제"의 판별 조건이다 — 결과 불명, 승인 후 DB 반영 롤백, 커밋 전 프로세스 종료를 모두 같은 조건으로 찾는다. **`pgTid`는 건드리지 않는다** — PG 취소에 쓰는 값이라 시도 표시로 오염시키면 취소가 엉뚱한 거래로 간다. DDL이 필요 없는 기존 컬럼이다.
- **`UNKNOWN`·`ALREADY_PROCESSED`면 `PgPaymentConfirmService`가 토스 조회(`PgPaymentGatewayPort#lookupPayment`)를 한 번 한다.** 조회 `APPROVED`면 `reconcileApproved`, `NOT_APPROVED`면 조회 결과(`failure = REJECTED`)로 실패 반영 → 400 `PAYMENT_APPROVAL_FAILED`(메시지는 원래 승인 결과가 `UNKNOWN`이면 **기본 메시지** — `PG_SERVER_ERROR`·`SYSTEM_ERROR` 같은 내부 문구를 사용자에게 보이지 않기 위해 —, `ALREADY_PROCESSED`면 토스 원문), `UNKNOWN`이면 조회 결과(`failure = UNKNOWN`)로 실패 반영 → PENDING 유지 → 503 `WebErrorCode.PAYMENT_APPROVAL_UNCONFIRMED`. **조회 이후에는 원래 승인 결과를 실패 반영에 넘기지 않는다** — 넘기면 결과 불명을 거절로 오인하거나, 조회로 확정한 사실(미승인·결과 불명)을 버리게 된다. 그래서 위 "`REJECTED`만 상태를 바꾸는 가드"는 웹 승인 흐름에서 `ALREADY_PROCESSED`를 받을 일이 없다. 흐름 전체는 `PgPaymentConfirmServiceTest`가 지킨다.
- **대사 반영(`reconcileApproved`)의 결과는 넷이다(`PgReconcileOutcome`).** `COMPLETED`(PENDING·FAILED, 금액 일치, 주문 확정 가능 → 완료·주문 확정·`PaymentCompletedEvent`), `ALREADY_COMPLETED`(이미 완료 → 무변경), `AMOUNT_MISMATCH`(조회 `totalAmount`가 결제 금액과 다르거나 없음 → 상태 불변·**원장도 기록하지 않음**·`PG_DB_MISMATCH` ERROR. 판정 순서는 잠금 조회 → COMPLETED 멱등 → PENDING·FAILED 아니면 거절 → 금액 검사 → 원장 기록 → 완료 처리다. 금액이 다르면 결제가 PENDING으로 남아 batch 대사가 10분마다 같은 ERROR를 반복하는데, 수동 대사 신호로 감수한다), `ORDER_NOT_CONFIRMABLE`(주문이 CANCELLED 등 → **결제만 COMPLETED**, 주문 확정·이벤트 생략, `PG_DB_MISMATCH` ERROR로 환불 필요를 알림). CANCELLED 결제는 `PAYMENT_NOT_PENDING_APPROVAL`로 던진다. 웹 응답은 앞 둘이 200, 뒤 둘이 503이다. 확정 가능 판정은 예외 catch가 아니라 `OrderStatus#canTransitionTo(CONFIRMED)`다. `ORDER_NOT_CONFIRMABLE`에서도 결제를 완료로 바꾸는 이유는 PENDING으로 두면 재시도·batch 대사가 같은 실패를 끝없이 반복하기 때문이다. 금액 판정은 이 메서드 한 곳에서만 한다(어댑터는 DB 금액을 모른다). 이벤트의 memberId는 `order.getMemberId()`다 — 소유권은 호출 전 `preparePgConfirmation`이 확인했다.
- **승인 반영은 멱등이다.** 결제가 COMPLETED이고 `pgTid`가 이번 `result.paymentKey()`와 같으면 그 paymentId를 그대로 돌려준다. 이 판정은 **원장 기록(`recordTossDetail`)보다 먼저** 해서 원장 행이 중복으로 쌓이지 않게 한다. `pgTid`는 `complete(result.paymentKey())`로만 채워지고 승인·조회 경로 모두 토스 응답 값이라 클라이언트가 조작할 수 없다. `pgTid`가 다르면 `PAYMENT_NOT_PENDING_APPROVAL`이다(`#applyPgConfirmation_isIdempotentForSamePaymentKey`·`#applyPgConfirmation_rejectsCompletedPaymentWithDifferentPaymentKey`).
- **PG 시도가 기록된 PENDING 결제는 취소할 수 없다.** `PaymentCancellationService#applyCancellation`은 트랜잭션의 첫 조회를 `PaymentLoadPort#findByIdForUpdate`로 해 결제 행을 잠근다(`requestRefund`와 같은 형태 — `infrastructure/jpa/AGENTS.md`의 "비관적 락은 트랜잭션 첫 조회" 규칙). 시도 기록도 같은 PK 행을 잠그므로 취소와 시도 기록이 직렬화되고, 아래 판정은 최신 `pg_provider`를 본다(`PaymentCancellationServiceTest#applyCancellation_loadsPaymentWithLock` — 잠금 1·일반 0, 실제 대기는 jpa `PaymentConcurrencyIntegrationTest`). PG 왕복 전 사전 판정 `prepareCancellation`은 잠그지 않는다(`#prepareCancellation_doesNotLockPayment`). 결제 PENDING + `pgProvider != null`이면 409 `WebErrorCode.PAYMENT_CANCEL_UNCONFIRMED`로 거절한다(DB 조건만 보고 PG는 부르지 않는다). 막지 않으면 결제가 CANCELLED가 되어 대사 대상에서 빠지고 돈은 환불되지 않은 채 남는다. 기존 거절(`PaymentCancelCode`, 200)과 달리 예외인 이유는 확정된 거절이 아니라 "결과가 확정되면 다시 하라"는 일시적 상태이기 때문이다. 차단은 결과가 확정(재시도·batch 대사·수동 대사)될 때 풀린다. **batch 대사는 결제 생성 후 7일까지만 보므로, 그때까지 PG 조회가 계속 `UNKNOWN`이면 그 뒤로는 차단이 수동 대사로만 풀린다** — 사용자는 재승인으로 풀 수 있지만 취소는 계속 409다. 관리자 화면·`admin-api`에는 수동 대사 기능이 없어, 운영자가 토스 상태를 확인하고 결제를 직접 맞춰야 한다. 대상은 7일 전에 찾을 수 있다 — batch가 시도마다 `PG_CONFIRM_UNKNOWN` WARN(`attempt=` 포함)을 남기고, 자동 대사가 끝나는 마지막 시도에는 `PG_RECONCILE_MANUAL_REQUIRED` ERROR를 남긴다.
- **사전 판정과 반영 사이에 승인이 끝난 결제도 409로 거절한다.** `prepareCancellation`은 잠그지 않으므로 PENDING을 보고 `pgCancelRequired=false`로 정한 뒤, PG 왕복 동안 승인이 끝나 결제가 COMPLETED가 될 수 있다. 그때 `applyCancellation`이 그대로 `payment.cancel()`하면 환불 없이 CANCELLED가 되고 `PG_DB_MISMATCH`도 남지 않으며 batch 대사(PENDING만 봄)도 찾지 못한다. 그래서 `PaymentCancelService`가 사전 판정값 `target.pgCancelRequired()`를 `PaymentCancellationExecutor#applyInNewTx` → `applyCancellation(…, pgCancelRequiredAtPrepare)`로 넘기고, 잠금 아래에서 `!pgCancelRequiredAtPrepare && isPgCancelRequired(payment)`이면 같은 409 `PAYMENT_CANCEL_UNCONFIRMED`로 거절한다(결제·주문·포인트 무변경). 재시도하면 사전 판정이 COMPLETED를 보고 PG 취소부터 진행한다. 넘기는 값이 "PG 취소를 시도했는가"가 아니라 "사전 판정이 필요하다고 봤는가"인 이유는, 라우터가 지원하지 않는 provider의 COMPLETED 결제(PG 취소 없이 취소되는 기존 경로)를 바꾸지 않기 위해서다. 테스트: `PaymentCancellationServiceTest#applyCancellation_rejectsWhenPaymentCompletedAfterPrepare`·`#applyCancellation_cancelsCompletedOnSitePaymentWithoutPgCancel`, `PaymentCancelServiceTest#cancelPayment_propagatesConflictWhenApprovedDuringCancel`·`#cancelPayment_skipsPgWhenProviderUnsupported`(미지원 provider는 사전 판정값 `true`를 넘긴다).
- **승인 반영은 PENDING·FAILED 모두에서 완료로 전이한다(PG 성공 우선).** 토스가 동시 중복 승인에 `ALREADY_PROCESSED_PAYMENT`가 아닌 코드를 줄 수도 있으므로 마지막 방어선이다. `Payment#complete`도 같은 상태를 허용하고, COMPLETED·CANCELLED는 계속 거절한다(중복 승인 방지). 사전 확인(`preparePgConfirmation`)은 여전히 PENDING만 통과시키므로, 사용자가 FAILED 결제로 새 승인을 시작할 수는 없다.
- **`Payment#fail`은 PENDING에서만 허용한다.** 서비스가 먼저 상태를 확인하므로 정상 흐름에서는 도달하지 않는 방어선이며, 새 에러코드를 만들지 않고 `DomainErrorCode.PAYMENT_NOT_PENDING`을 재사용한다(메시지가 "완료 처리"를 말하지만 바꾸면 wire 변경이다).
- **두 번째 요청은 조회를 거쳐 응답이 정해진다.** 첫 요청이 이미 완료했으면 조회 `DONE` → 대사 `ALREADY_COMPLETED` → 기존 paymentId로 200, 첫 요청이 아직 승인 중이면 조회 `IN_PROGRESS` → 503 `PAYMENT_APPROVAL_UNCONFIRMED`다. 토스가 거절한 경우의 메시지는 토스 원문이다.
- **batch 주기 대사가 web 요청 밖에서 남은 미확정 결제를 확정한다.** `batch-application`의 `payment/service/PaymentReconcileSchedulerService`(트리거 `batch-module`의 `PaymentReconcileScheduler`, 기본 10분 주기)가 `PaymentLoadPort#findAllPgAttemptedReconcileDueByStatusCreatedBetween(PENDING, now-7일, now-10분, now, 100)`으로 "PENDING + `pg_provider` 있음 + 다음 대사 시각(`reconcile_next_at`)이 비었거나 지남" 결제를 생성 순으로 고르고, 건마다 먼저 `PaymentReconcileExecutor#claimNextAttemptInNewTx`(트랜잭션)로 **대사 차례를 선점**한다 — `PaymentSavePort#saveReconcileScheduleIfDue`가 "시도 횟수가 방금 읽은 값 그대로 **그리고** 다음 시각이 비었거나 실행 시각 이하"일 때만 횟수를 1 올리고 다음 시각을 `max(실행 시작 시각 + min(10분 × 2^이전 시도 횟수, 24시간) − 1분, 선점 시각 + 2분)`(계산은 `PaymentReconcileBackoffPolicy#nextAttemptAt`)으로 저장한다. 차례 비교(`dueAt`)와 다음 시각의 앞항은 대상 조회와 같은 실행 시작 시각이다 — 선점 순간만 기준으로 삼으면 주기 시작 1분 뒤에 선점한 결제가 다음 실행을 지나쳐 첫 재시도가 20분 뒤가 된다. 뒤항(선점 시각 + 2분)은 한 주기가 길어져 늦게 선점했을 때 저장값이 이미 지난 값이 되어 그 사이 시작한 다른 실행이 처리 중인 결제를 다시 선점하는 것을 막는다(2분의 근거와 타임아웃 결합은 `batch-application/AGENTS.md`의 대사 재시도 일정 항목). 영향 행이 0이면 다른 실행(다른 batch 인스턴스)이 먼저 가져갔거나 web 승인 시도가 일정을 초기화한 것이라 PG에 묻지 않고 넘어간다 — 그래서 batch를 여러 대 띄워도 같은 결제를 두 번 조회·반영하지 않고(`failPgConfirmation`이 원장을 상태 확인 전에 쓰므로 중복 처리는 곧 원장 중복이다), 횟수 갱신도 유실되지 않는다. 선점이 예외면 ERROR를 남기고 그 결제를 건너뛰며, **3건 연속** 예외면 DB 장애로 보고 남은 대상을 건드리지 않은 채 주기를 중단한다(다음 시각이 바뀌지 않아 다음 주기에 다시 고른다). 선점한 결제만 `PgPaymentGatewayPort#lookupPayment`(트랜잭션 밖)로 PG에 묻는다([ADR-0064](../../docs/adr/0064-payment-reconcile-claim.md)). 조회 `APPROVED`면 `PaymentReconcileExecutor#reconcileApprovedInNewTx`(→ `reconcileApproved`, 트랜잭션), `NOT_APPROVED`면 `#failUnapprovedInNewTx`(→ 조회 결과로 `failPgConfirmation`, FAILED), `UNKNOWN`이면 건드리지 않고 `PG_CONFIRM_UNKNOWN` WARN(`attempt=` 시도 횟수 포함)만 남겨 다음 차례로 넘긴다. `APPROVED` 반영이 예외면 `PG_DB_MISMATCH` ERROR다(PG는 승인, DB는 미반영). 확정하지 못한 결제(조회 예외·`UNKNOWN`·`AMOUNT_MISMATCH`·반영 예외)의 이번 시도가 **7일 창 안의 마지막 시도**면(`nextAttemptAt + 10분`이 `createdAt + 7일`을 넘음 — 기본 cron 주기 10분이고 다음 실행이 밀리지 않는다는 전제라, 앞 주기가 길어지거나 주기가 중단되면 신호 없이 빠지는 결제가 생길 수 있다) `PG_RECONCILE_MANUAL_REQUIRED` ERROR를 남긴다. 다음 주기부터 대상에서 빠져 수동 대사로만 풀리기 때문이다. 판정에 `+ 10분` 여유를 두어 다음 실행이 조금 밀려도 놓치지 않는 대신, 창 끝 직전에는 연속한 두 실행이 모두 마지막으로 판정돼 **최대 2건**이 남을 수 있다(누락보다 중복을 택했다). 건별 예외는 격리하고 결과별 건수(선점 실패·다른 실행이 처리·수동 대사 필요 포함)를 INFO로 남기며, 대상이 0건이면 DEBUG만 남긴다. 대상 창은 결제 **생성 시각**(`created_at`) 기준 반개구간 `[now-7일, now-10분)`이다. 생성 후 10분 미만은 web 요청 안의 즉시 조회와 겹치지 않게 제외하고, 7일을 넘긴 건은 수동 대사 대상이다. `pg_provider`가 비어 있는 PENDING(결제창을 닫고 떠난 결제)은 PG를 부른 적이 없으므로 건드리지 않는다. 조회 순서는 `created_at`(같으면 `id`) 오름차순이지만, 한 번 시도한 결제는 다음 차례까지 대상에서 빠지므로 결과를 확정하지 못하는 결제가 100건 슬롯을 계속 차지하지 않는다. 같은 이유로 `AMOUNT_MISMATCH`의 `PG_DB_MISMATCH` ERROR도 7일 동안 약 14회로 줄어든다. web 승인 시도(`PaymentConfirmationService#recordPgAttempt`)는 같은 트랜잭션에서 `PaymentSavePort#saveReconcileSchedule(paymentId, 0, 지금+10분)`으로 일정을 초기화한다 — 재승인한 결제가 이전 백오프에 묶여 하루씩 기다리지 않게 하고, 잠금 밖에서 진행 중인 web PG 호출과 batch 조회가 겹치지 않게 10분을 둔다([ADR-0062](../../docs/adr/0062-payment-reconcile-backoff.md)).
- 결정 경위: [ADR-0059](../../docs/adr/0059-payment-confirm-race-lock.md)(잠금), [ADR-0060](../../docs/adr/0060-pg-confirm-unknown-lookup.md)(결과 불명·조회 대사), [ADR-0061](../../docs/adr/0061-pg-ports-to-core-batch-reconcile.md)(PG 포트 코어 이동·batch 주기 대사).

### 도메인 서비스를 둘 모듈은 소비 앱 집합으로 정한다 — 공유 커널 단방향

**대상**: 코어·앱 모듈의 `<ctx>/service/` 도메인 서비스(예: `web-application`의 `payment/service/PaymentCancellationService`, 코어의 `payment/service/PaymentConfirmationService`·`shop/service/ShopLifecycleService`) · `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `coreBeansShouldOnlyDependOnCoreVisibleTypes`

원문 취지:

- **소비 앱 집합이 하나면 그 `{앱}-application`, 둘 이상이면 코어.** 소비 앱 집합은 생성자 주입을 따라 올라가 "최종적으로 이 빈을 쓰는 앱"의 집합이다. 4앱 공통으로 등록하면 한 앱 전용 빈이 쓰지 않는 앱에도 뜨고, 서비스 위치가 곧 "어느 앱에 뜨는가"를 표현하므로 정보가 한 곳에 산다.
- **"현재 소비자" 기준이다 — "언젠가 다른 앱이 쓸 수 있으니 미리 코어에"는 하지 않는다.** 미리 넓혀 두면 쓰지 않는 앱에 빈을 띄우는 비용만 있고, 좁게 둔 배치가 틀리면 컴파일 에러나 `coreBeansShouldOnlyDependOnCoreVisibleTypes`가 **빌드에서** 잡으므로 "필요할 때 코어로 옮긴다"가 안전하다. 그래서 `PaymentCancellationService`(소비자 `PaymentRefundRequestService`·`PaymentCancellationExecutor`, 모두 web)는 `web-application`, `PaymentConfirmationService`(코어 리스너 `PaymentEventListener`가 소비)는 코어다. 리스너 자체가 4앱 공통인 이유(이벤트는 어느 앱이 발행하든 처리돼야 한다)는 리스너에만 적용된다.
- **앱 → 공유는 허용, 공유 → 앱은 금지(공유 커널 단방향).** 코어 빈은 4앱 전부에 뜨는데, 그것이 앱 전용 빈을 주입하면 나머지 세 앱에서 그 빈을 찾지 못해 기동이 실패한다. 앱 모듈은 코어를 의존하고(`api project(':application-core')`) 코어는 앱 모듈을 모르므로 이 방향은 **컴파일 에러**로 막힌다.
- **고정점 계산이 필요하다.** 코어 빈이 의존하는 빈도 코어여야 하므로, 한 서비스를 코어로 올리면 그 의존 서비스도 따라 올라간다(`ReviewBlindRequestService` → `ReviewLifecycleService`, `ShopLifecycleService` → `ShopImageApprovalService`·`ShopCeoAssignmentRecorder`). 새 서비스를 추가하거나 소비자가 바뀌면 이 전파를 다시 따라간다 — 누락하면 컴파일 에러 또는 `coreBeansShouldOnlyDependOnCoreVisibleTypes`가 잡는다.
- **domain 계산기 7개는 애노테이션을 달 수 없다.** domain 모듈은 production 의존이 0개(spring-free)다. 그래서 `SharedBeanConfig`의 `@Bean`이 남는다. `cupDepositPolicy`가 단일 빈이어야 하는 근거(점주 설정·손님 메뉴판·주문 금액 확정이 같은 인스턴스를 주입)는 그대로다.

### 트랜잭션 경계를 파사드가 아니라 하위 서비스가 갖는 이유 — read-then-write 판정

**대상**: 조립 서비스(트랜잭션 없음) `backend/web-application/src/main/java/com/tastyhouse/application/member/service/MemberVerifiedPasswordUpdateService.java` ·
`.../member/service/MemberVerifiedPersonalInfoUpdateService.java` · `.../member/service/MemberWithdrawWithLogoutService.java` ·
`.../member/service/MemberPasswordVerifyService.java`, 내부 per-op 서비스(클래스 `@Transactional`)
`.../member/service/MemberPasswordUpdateService.java` · `.../member/service/MemberPersonalInfoUpdateService.java` ·
`.../member/service/MemberWithdrawService.java`, 그리고 `backend/web-application/src/main/java/com/tastyhouse/application/auth/service/AuthPasswordResetService.java`

화면 단위 흐름을 엮는 **조립 서비스는 `@Transactional`을 갖지 않는다.** 조립 서비스가 트랜잭션을 열면
DB 원자성이 필요 없는 단계(JWT 서명 검증·Redis 접근)까지 DB 커넥션을 네트워크 지연만큼
점유하게 되므로, 원자성이 실제로 필요한 구간만 하위 per-op 서비스가 단일 트랜잭션으로 갖는다.
**바깥 단계를 내부 서비스로 합치지 않는다** — 합치면 토큰 무효화가 DB 트랜잭션 안으로 들어가고,
토큰 없이 내부 `MemberPasswordUpdateUseCase`를 부르는 `AuthPasswordResetService` 경로가 깨진다.

판정 기준은 하나다 — **"이 단계가 DB에서 읽은 값에 근거해 DB를 쓰는가(read-then-write)?"**
그렇다면 검증과 쓰기가 같은 트랜잭션·같은 로드 안에 있어야 하고(그렇지 않으면 검증 후 쓰기
사이에 상태가 바뀌어 검사를 우회할 수 있다), 아니라면 묶지 않는다.

| 유스케이스 | 판정 | 근거 |
|---|---|---|
| 개인정보 변경 | 묶지 않는다 | 두 토큰 검증이 **JWT 서명·클레임 검증만** 수행하고 DB를 읽지 않는다(토큰이 발급 시점의 인증 사실을 서명으로 담고 있다). read-then-write 경합이 성립하지 않으며, 실제 DB write는 `MemberPersonalInfoUpdateService#updatePersonalInfo` 한 번뿐이라 이미 단일 트랜잭션이다 |
| 비밀번호 변경 | 묶었다(하강) | "새 비밀번호가 기존과 같은지" 검사가 **DB에서 읽은 현재 비밀번호**에 근거해 DB를 쓰는 read-then-write다. 검사를 별도 readOnly 트랜잭션에 두면 검사와 변경이 두 트랜잭션·두 번의 회원 로드로 쪼개져 **검사 후 변경 사이에 비밀번호가 바뀌면 우회 가능**하다. 그래서 `MemberPasswordUpdateService#updatePassword` 안에서 단일 트랜잭션·단일 로드로 원자화한다 |
| 회원 탈퇴 | 묶지 않는다(묶으면 틀린다) | 탈퇴는 DB 변경이지만 토큰 무효화는 **Redis 블랙리스트 등록**이라 DB 트랜잭션과 무관하다. 오히려 **순서가 중요**하다 — 탈퇴(`MemberWithdrawService`)가 커밋된 뒤 무효화해야 하며(`MemberWithdrawWithLogoutService`가 이 순서를 지킨다), 한 트랜잭션에 넣으면 Redis 등록이 커밋 전에 일어나 **탈퇴가 롤백돼도 토큰만 죽는** 불일치가 남는다 |
| 인증코드 발송 | 묶었다 | "기존 미완료 인증 만료 + 새 인증 저장 + 발송"이 함께 성립해야 한다 |

**비밀번호 변경의 검사 순서를 뒤집지 않는다** — 동일 여부(`MEMBER_PASSWORD_SAME_AS_OLD`) →
확인값 불일치(`MEMBER_PASSWORD_CONFIRM_MISMATCH`) 순서를 유지해야 하며, 뒤집으면 두 조건을
동시에 위반한 요청의 **응답 코드가 바뀐다**.

### PG·외부 왕복은 트랜잭션 밖에 둔다 — 3단 구조와 보상 불가 지점

**대상**: `backend/web-application/src/main/java/com/tastyhouse/application/payment/service/PgPaymentConfirmService.java` · `.../payment/service/PaymentCancelService.java` · `.../payment/service/PaymentConfirmationExecutor.java` · `.../payment/service/PaymentCancellationExecutor.java`

**PG 왕복이 있는 두 유스케이스 서비스(`PgPaymentConfirmService`·`PaymentCancelService`)에 `@Transactional`이 없는 것은 의도다.** 같은 결제 컨텍스트의 나머지 세 서비스(`PaymentCreateService`·`PaymentOnSiteCompleteService`·`PaymentRefundRequestService`)는 PG 왕복이 없어 클래스 레벨 `@Transactional`을 단다. (유스케이스 분리 전에는 이 여섯 연산이 `PaymentCommandService` 한 클래스에 있었고, 그래서 클래스 레벨 대신 메서드 레벨로 트랜잭션을 나눠 달았다.) PG 승인·결제 취소는 PG사와의 HTTP 왕복을
포함하는데, 그 왕복이 DB 트랜잭션 안에 있으면 (1) 커넥션과 결제·주문 행 락을 네트워크 지연만큼
점유하고, (2) PG 처리가 성공한 뒤 커밋이 실패하면 **"PG는 승인/취소, DB는 미반영"이라는 보상 불가
불일치**가 남는다. **취소는 `pgCancelRequired && pgPaymentGateway.supports(pgProvider)`일 때만 PG를 호출한다** —
`PaymentCancelService#cancelPayment`가 판정하는 `pgCancelAttempted`이며, 라우터가 해당 벤더를
지원하지 않으면(`PgPaymentGatewayRouter#supports`) PG 왕복 없이 DB 취소만 반영한다.

```
① 사전 검증     : PaymentConfirmationExecutor#prepareInNewTx          (트랜잭션, readOnly)
①' PG 시도 기록 : PaymentConfirmationExecutor#recordPgAttemptInNewTx  (트랜잭션)
② PG 호출       : PgPaymentGatewayPort#confirmPayment                  (트랜잭션 없음)  ← 서비스가 직접
②' PG 조회      : PgPaymentGatewayPort#lookupPayment                   (트랜잭션 없음)  ← UNKNOWN·ALREADY_PROCESSED일 때만
③ 결과 반영     : PaymentConfirmationExecutor#applyInNewTx · reconcileApprovedInNewTx · failInNewTx  (트랜잭션)
```

- **보상 장치**: ③이 실패하면 PG는 이미 처리됐으므로 자동 보상이 불가능하다. `PG_DB_MISMATCH`
  마커와 PG 거래 식별자를 담은 `log.error`를 **수동 개입·대조 배치의 진입점**으로 삼는다(운영에서
  이 마커로 알럿을 건다). 알럿은 로그 레벨이 아니라 **마커 문자열로** 건다. 마커가 ERROR로 남는 경우는 ③ 반영 실패, 대사 `AMOUNT_MISMATCH`·`ORDER_NOT_CONFIRMABLE`, 대사 반영 예외다. `failPgConfirmation`은 마커를 남기지 않는다(위 "`REJECTED`만 상태를 바꾼다" 항목). 버튼 연타의 정상 경합은 조회 후 조회 결과만 반영하므로 마커가 남지 않는다.
  ③의 실패(커밋 실패 포함)는 원 요청이 이미 ERROR로 남기므로(`applyInNewTx`·`reconcileApprovedInNewTx` 모두), 로그 없이 남는
  불일치는 원 요청이 커밋 전 프로세스 종료나 `Error`로 끝난 경우뿐이다. 그 결제는 ①'로 `pg_provider`가 채워진 PENDING이라
  사용자 재시도(토스 "이미 처리됨" → 조회 → 대사)나 batch 주기 대사(`batch-module`의 `PaymentReconcileScheduler` — 위 "batch 주기 대사" 항목)가 찾는다. 사용자에게는 실패를 그대로 전파해 "성공했지만 반영되지 않은" 상태를
  성공으로 오인하게 하지 않는다.
- **PG 호출 자체가 예외(타임아웃 등)·5xx·빈 응답·본문을 해석할 수 없는 4xx면 결제는 `PENDING`으로 남는다** — `TossPaymentClient`가 각각
  `SYSTEM_ERROR`·`PG_SERVER_ERROR`·`UNKNOWN_ERROR`·`PG_UNREADABLE_RESPONSE` 응답으로 바꾸고 `TossPaymentGatewayAdapter`가 `PgConfirmFailure.UNKNOWN`으로
  분류한다. `PgPaymentConfirmService`는 ②'로 토스에 한 번 묻고, 승인이면 대사 반영, 미승인이면 FAILED, 그래도 모르면
  PENDING 유지 + `PG_CONFIRM_UNKNOWN` WARN + 503이다. 승인 여부가 불확실한 상태를 FAILED로 단정하지 않으므로 "PG는 승인·DB는
  실패"인 반대 방향 불일치가 생기지 않는다. 조회까지 실패한 건은 사용자 재시도나 batch 주기 대사가 확정한다. PG 호출만 보면 응답은 최악의
  경우 승인과 조회 타임아웃을 합친 약 30초가 걸린다(프론트 승인 호출 타임아웃 35초). 시도 기록·승인 반영·실패 반영의
  `loadForUpdate` 행 잠금 대기(MySQL 기본 50초)는 이 계산에 들어 있지 않아, 같은 결제의 취소와 겹치면 35초를 넘을 수 있다.
  넘으면 프론트가 응답을 받지 못한 경로로 '확인 중' 화면을 띄우므로 실패로 단정하지 않는다.
- `failInNewTx`의 트랜잭션은 **커밋되어야 한다** — 실패 사실과 PG 응답 원본을 남기는 것이 목적이라
  예외 변환은 커밋 이후 호출자가 수행한다.
- PG 왕복을 포함하지 않는 명령(결제 개시·현장결제 완료·환불 요청)은 DB만 다루므로
  메서드 단위 `@Transactional` 하나로 충분하다.

### Executor를 별도 빈으로 분리하는 이유 — self-invocation은 프록시를 거치지 않는다

**대상**: `payment/service/PaymentConfirmationExecutor.java` ·
`reservation/service/ReservationBookingExecutor.java` ·
`reviewblind/service/ReviewBlindExpirationExecutor.java` ·
`productsoldout/service/ProductSoldOutReleaseExecutor.java` · `region/service/AdminDongSyncExecutor.java` ·
`payment/service/PaymentReconcileExecutor.java`(batch-application)

**같은 빈의 메서드를 호출하면 Spring 프록시를 거치지 않아(self-invocation) `@Transactional`이
적용되지 않는다.** 그래서 "재시도 루프·반복 처리는 트랜잭션 밖, 각 시도는 독립 트랜잭션"을
표현하려면 두 구간이 **서로 다른 빈**에 있어야 한다. 오케스트레이션하는 쪽은 트랜잭션을 가질 수
없고(외부 호출을 밖에 둬야 하므로), 도메인 서비스는 순수 POJO라 가질 수 없다 — 그 사이를 메우는
얇은 위임 빈이 Executor다.

- **낙관적 락 재시도**(`ReservationBookingExecutor`): 매 시도가 새 트랜잭션이어야 한다. 한 빈에
  두면 첫 시도에서 **rollback-only로 표시된 트랜잭션을 그대로 재사용**해 재시도가 무의미해진다.
- **건별 격리**(`ReviewBlindExpirationExecutor`·`ProductSoldOutReleaseExecutor`): 한 건이 실패해도
  앞서 성공한 건들이 함께 말려 들어가지 않아야 한다. 그래서 **스케줄러 서비스에는 `@Transactional`을
  붙이지 않는다** — 붙이면 전체가 한 트랜잭션이 되어 한 건의 실패가 전체를 되돌린다.
  실패 요약은 예외가 아니라 **로그**로 남긴다(예외를 던지면 스케줄러가 삼켜 성공 건수까지 잃는다).
- Executor는 `REQUIRES_NEW`가 아니라 기본 `REQUIRED`를 쓰되, **상위 트랜잭션이 없는 상태를 전제한
  설계**임을 밝히려 전파 속성을 명시적으로 남긴다.

### CQRS 교차 주입 금지가 실제로 강제하는 것

**대상**: `{web,admin,ceo}-application`의 `**/service/` 유스케이스 서비스 — `backend/application-core/src/testFixtures/java/com/tastyhouse/architecture/UseCaseServices.java` → `commands()` · `queries()`, 강제 규칙 `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `commandServicesShouldNotDependOnQueryPorts` · `queryServicesShouldNotDependOnWritePorts`
(유스케이스 분리 전 대상 표기는 `**/service/*CommandService.java` · `**/service/*QueryService.java` — 지금은 이름이 아니라 구현 포트로 명령/조회를 가른다)

명령 유스케이스 서비스는 infra query DAO도 `*QueryService`·`*QueryUseCase`도 주입하지 않고, 조회 유스케이스 서비스는
domain의 write 포트를 주입하지 않는다. 그 결과 아래가 **구조로 강제**된다.

- **모든 명령은 식별자만 반환하고, 응답 조립은 커밋 이후 컨트롤러가 QueryService로 재조회해 담당한다.**
- 명령 경로에서 다른 애그리거트를 참조해야 하면 표현용 투영이 아니라 **write 포트의 단건 로드**를
  쓴다 — 그 값이 화면 표시용이 아니라 **불변식 입력**이기 때문이다(예: 리뷰 등록 시 상품 → 가게
  역조회는 "리뷰가 어느 가게에 속하는가"를 확정한다).
- 조회 경로에서 소유권·인가 판정이 필요하면 write 포트 대신 **읽기 포트로 식별자만 조회해 대조**한다
  (상태를 바꾸지 않는 화면 접근 판정이라 표현 목적 조회다).
- 규칙을 우회하지 않으면서 인가 관심사를 다루려면 **write 포트를 감싼 협력 빈**에 가둔다
  (`ShopOwnershipValidator`·`OwnedShopIdProvider`). 규칙의 의도는
  "쓰기 경로가 표현용 조회를 끌어다 쓰는 것"을 막는 데 있고 소유권 판정은 그 범주가 아니다.
  **(정정 — 위임 래퍼 제거)** 과거 이 예시에 있던 `StorePriceVerificationReader`(web)·`StorePriceVerificationOwnerReader`(ceo)는
  삭제됐다. 감싸던 `StorePriceVerificationPort`(`application/product/port/out/`)는 write 포트가 아니어서(write 패키지에 있었던 적도 없다)
  감쌀 이유가 처음부터 없었다. 지금은 `ShopPriceBadgeQueryService`·`ShopStorePriceVerificationQueryService`가 포트를 직접 주입한다.
  이 포트에는 변경 메서드(`verifyStorePrice`·`clearStorePriceVerification`)도 있어 조회 서비스가 닿을 수는 있지만 호출하지 않는다.

**빈 순환 참조 회피**: 한 화면이 다른 컨텍스트의 데이터를 곁들여 보여줄 때 그쪽 QueryService를
경유하지 않고 **QueryPort를 직접 주입**한다 — 서비스를 경유하면 상대 쪽이 이 서비스를 다시 주입해야
해 순환이 생긴다. 표현 목적 조회는 DAO 계층에서 교차하는 것이 옳다(`ProductReviewsByRatingQueryService`가 `ReviewFeedQueryPort`를,
`ReviewProductQueryService`가 `ProductQueryPort`를 직접 주입하는 것이 실제 사례 — 유스케이스 분리 전에는 `ProductQueryService` ↔ `ReviewQueryService`).

### 도메인 계산 입력은 표현용 투영으로 대체하지 않는다

**대상**: `backend/web-application/src/main/java/com/tastyhouse/application/shop/service/ShopDeliveryTipViewQueryService.java` → `findVisibleShopAggregate`

표현용 단건 조회와 달리 **도메인 서비스에 넘길 도메인 모델이 필요한 조회는 write 포트를 쓴다.**
계산기가 도메인 모델을 받으므로 표현용 Result를 도메인으로 되돌리는 역변환을 두지 않기 위함이며,
이것이 `queryServicesShouldNotDependOnWritePorts` carve-out의 실질적 근거다(위 봉인 목록 참조).
화면 표기용 목록(지역 이름 조립 등)만 infra query DAO에서 받는다.

같은 이유로 **read model을 `reconstitute`로 도메인 모델까지 되짚어 올려** 도메인 정책의 술어를
재사용하는 경로가 있다(`ShopPriceBadgeQueryService`·`ProductDetailQueryService`·`ProductBatchQueryService`). 규칙을 복제하면 표시
가격과 결제 금액이 갈리거나, 요일 구분을 추가할 때 한쪽만 고쳐진다. `reconstitute`(검증 미수행)를
쓰는 것은 **기존 데이터가 현행 규격을 위반해도 조회는 되어야 하기 때문**이다.

### 시각·시계에 의존하는 계산은 application에 남는다

**대상**: `coupon/port/out/MyCouponListItemResult.java` · ceo-application `review/service/ShopReviewListQueryService.java`·`review/service/ShopReviewDetailQueryService.java`
→ `toReplyWindow` · `review/service/ReviewOwnerReplyCreateService.java` → `register`
(유스케이스 분리 전 이름은 `ShopReviewQueryService`·`ReviewOwnerReplyCommandService`. `toReplyWindow`는 목록·상세 두 서비스에 각각 복제됐다)

"오늘"을 읽어야 하는 판정은 표현 계약이 대신할 수 없다 — 표현 계약이 시계를 읽으면 **응답 조립이
시점에 따라 값이 달라지는 순수하지 않은 함수**가 된다. 마감일 상수는 도메인 모델이 소유하므로
api 모듈이 참조할 수 없다는 것(`apiModuleShouldBeDomainModelFree`)도 함께 작용한다.

반대 방향으로, **domain은 프레임워크-프리라 시계를 주입받을 수 없고** 도메인이 직접 `now()`를
부르면 단위 테스트에서 기한을 고정할 수 없다. 그래서 기준 시각은 **이 계층이 해석해 도메인 서비스에
넘긴다**.

### 도메인 enum에 대한 `switch`를 api 모듈로 내리지 않는다

**대상**: ceo-application `review/service/ShopReviewSortTypeQueryService.java` → `describeSortType` ·
`shop/service/ShopRequestDetailQueryService.java` → `toRequestStatus`
(유스케이스 분리 전 이름은 `ShopReviewQueryService`·`ShopRequestQueryService`)

도메인 enum에 대한 `switch`는 바이트코드에서 `ordinal()`·`values()` 호출이 되어 api 모듈에서는
`apiModuleShouldOnlyReadDomainEnums`(읽기 accessor 3종만 허용)에 걸린다. 그래서 분기·표시 문구
매핑은 이 계층에 남는다.

*(갱신 — 덩어리 01)* 그 규칙은 삭제됐다. 지금은 api 모듈 컴파일 클래스패스에 `domain`이 없어 도메인 enum
`switch`가 **컴파일되지 않으므로** 결론(분기·표시 문구 매핑은 이 계층)은 더 강하게 유지된다.

**`valueOf`가 아니라 `switch`를 쓰는 것도 의도다** — 어느 한쪽에 상수가 추가되면 컴파일이 깨져
매핑 누락이 드러난다. 값 이름이 그대로 대응하더라도 마찬가지다.

### 표시 문구를 서버가 완성하는 기준

**대상**: `backend/web-application/src/main/java/com/tastyhouse/application/shop/service/ShopDeliveryTipViewQueryService.java` → `toShopDeliveryTipBreakdownItems` ·
`.../shop/service/ShopScheduledOrderSlotQueryService.java` → `toScheduledOrderSlotItemResult`·`toDayLabel`

프론트가 분기·상수를 복제하지 않도록 서버가 문구를 완성한다. **문구 안의 숫자는 천 단위 콤마까지
서버가 넣는다** — 그 값은 응답의 금액 필드가 아니라 **이미 완성된 문장의 일부**라 프론트가 문자열을
뜯어 다시 포맷할 수 없기 때문이다(금액 필드 자체의 표기 포맷은 그대로 프론트 담당이다).

도메인 enum 승격이 필요한 표기(요일 표시명 등)도 여기서 끝낸다 — api 모듈이 호출할 수 없는
도메인 enum 메서드이기 때문이다.

### 컨텍스트 경계를 잇는 조립은 이 계층의 몫이다

**대상**: ceo-application `product/service/ProductSoldOutOwnerService.java`·`ProductOptionSoldOutService.java`(그 밖의 판매상태 변경 서비스 6개 포함) ·
`product/service/ProductVegetarianRequestService.java`·`ProductVegetarianClearService.java` ·
`menureview/service/MenuReviewCreateService.java` ·
`shop/service/ShopStorePriceVerificationRequestService.java`

한 유스케이스가 두 컨텍스트의 값을 함께 필요로 하면, 도메인 서비스가 상대 컨텍스트를 직접 참조하는
대신 **이 계층이 각각 주입해 연결한다** — 도메인에서 참조하면 `ContextBoundaryTest` 위반이 되고
**봉인 목록은 늘릴 수 없다.**

정책과 계산을 가르는 기준도 함께 기록한다 — "오픈 시각을 정할 수 없다"(계산기)와 "그러면 얼마로
할까"(정책)는 서로 다른 판단이므로, **순수 계산기가 정책을 삼키지 않도록** 폴백 정책은 이 계층에 둔다.

### 소유권 역조회를 생략하지 않는다 — 실제 IDOR 사고의 근거

**대상**: ceo-application `product/service/ProductImageDeleteService.java` → `deleteImage` ·
`product/service/ProductOptionGroupOwnershipValidator.java` ·
`shop/service/ShopDeliveryAreaDeleteService.java` → `removeDeliveryArea`
(유스케이스 분리 전 이름은 `ProductImageCommandService`·`ShopDeliveryAreaCommandService`)

경로에 소유자 식별자가 없더라도 **대상 행에서 소유자를 역조회할 수 있으면 반드시 검증한다.**
이 저장소는 배달가능지역 삭제에서 정확히 이 역조회를 빠뜨려 **아무 점주나 순번을 훑어 남의 가게
배달가능지역을 삭제**할 수 있는 IDOR을 낸 전례가 있다(피해 가게는 배달 범위를 잃거나, 등록 건수가
0이 되면 주문 접수의 지역 검사 자체가 비활성화됐다).

- **"없음"과 "남의 것"은 같은 404로 합친다** — 코드가 갈리면 존재 여부가 새어 식별자 열거에 쓰인다.
  403을 쓰면 그 리소스의 존재 자체가 드러난다.
- **연결이 0건이면 소유자를 판정할 수 없으므로 접근 불가로 다룬다** — `null`을 "허용"으로 읽으면
  곧 인가 우회다.
- **N:M 전환 이후 소유권 판정은 동등 비교가 아니라 포함 관계다** — 한 메뉴가 여러 가게에 걸리므로
  원본 가게만 인정하면 연결된 가게의 점주가 자기 메뉴판의 메뉴를 열지 못한다.

### 집합 규칙이 있는 교체는 전량 검증 후 업로드한다

**대상**: ceo-application `shop/service/ShopNoticeOwnerCreateService.java`·`ShopNoticeOwnerUpdateService.java` → `saveImages`
(유스케이스 분리 전에는 `ShopNoticeOwnerCommandService` 한 클래스의 private 헬퍼였다. 지금은 두 서비스가 각자 갖는다)

파일 단위로 검증·업로드를 교차하면 뒤쪽 파일이 규격 위반일 때 앞쪽은 **이미 외부 스토리지에 올라간**
상태가 된다. 트랜잭션 롤백은 `UPLOADED_FILE` 행만 되돌릴 뿐 **스토리지 바이트는 되돌리지 못해**
실패 시도마다 고아 파일이 누적된다. 그래서 전량 검증을 먼저 끝낸 뒤 업로드한다.

변경 전 요약은 `updateContent` **호출 전에** 확정해야 한다 — 같은 인스턴스를 제자리에서 갱신하므로
나중에 읽으면 이미 변경 후 값이다.

### multipart 문자열 파트의 파싱 위치

**대상**: ceo-application `shop/service/ShopStorePriceVerificationRequestService.java` → `toItemSpecs`

컨트롤러·Request record는 domain-free라 `BusinessException`을 던질 수 없고, 서비스는 `..request..`를
알 수 없다(`commandServicesShouldNotDependOnRequestRecords`). 세 규칙을 모두 만족하는 유일한 형태는
**Command가 원문을 경계 타입 `String`으로 담아 넘기고 서비스가 파싱하는 것**이다. 파싱 실패와 빈
목록이 같은 `ErrorCode`로 나가던 계약도 이때 그대로 보존된다(둘 다 서비스가 던진다).

실행 순서에도 의도가 있다 — **파싱을 업로드보다 앞에 둬야** 목록이 깨진 요청 때문에 쓸모없는
파일이 업로드되지 않는다.

### 조회 기간 상한을 이 계층에서 강제하는 이유

**대상**: ceo-application `ceo/service/CeoLoginHistoryListQueryService.java`(90일) ·
`ceo/service/CeoShopAccessHistoryListQueryService.java`(5년) ·
`shop/service/ShopChangeHistoryListQueryService.java`(6개월)
(유스케이스 분리 전 이름은 `CeoLoginHistoryQueryService`·`CeoShopAccessHistoryQueryService`·`ShopChangeHistoryQueryService`)

- **domain이 아닌 이유**: 기간 제한은 도메인 불변식이 아니라 **조회 화면 정책**이다. 기간이 지난
  행도 삭제하지 않고 계속 보관하며(고객센터 요청 시 장기 조회가 원 요구사항), 기록·저장은 제한하지
  않는다.
- **Bean Validation만으로 불가능한 이유**: `@PastOrPresent`는 상한만 막고 **"오늘 기준 -N일"이라는
  상대 하한**을 어노테이션으로 표현할 수 없다.
- **DAO 단독이 아닌 이유**: DAO가 조용히 잘라내면 사용자에게 "왜 비었는지"가 보이지 않는다.
- **기본값으로 파생된 경우에도 동일하게 검증한다** — 한쪽만 범위 밖으로 지정하면 나머지가 파생되어
  함께 밖으로 나가므로 그 조합도 거부되어야 한다.

**요청처리 현황에는 상한을 두지 않는다** — 변경이력의 6개월 제한을 대칭성을 이유로 복제하지 않는다.
"내가 낸 요청의 결과"는 반려 사유 확인·재요청 시 과거 제출물 참조를 위해 오래된 건도 열람돼야 한다.

### 접속기록은 인증 실패 경로에서도 남아야 한다

**대상**: ceo-application `auth/service/CeoLoginService.java` · `ceo/service/CeoLoginSuccessRecordService.java` · `ceo/service/CeoLoginFailureRecordService.java`
(기록 서비스는 성공·실패 두 개이고, 둘 다 같은 패키지의 `CeoLoginHistoryRecorder`로 저장한다)

**호출부 `CeoLoginService`에 `@Transactional`을 붙이지 않는다.** 로그인 실패는 Spring Security 예외로 전파되는데,
트랜잭션이 걸려 있으면 **실패 이력이 예외와 함께 롤백되어 영구히 남지 않는다.** 호출부가 비트랜잭션이므로
기록 서비스(`CeoLoginSuccessRecordService`·`CeoLoginFailureRecordService` — 클래스 레벨 `@Transactional`)의 매 호출이 프록시를 거쳐 **독립 트랜잭션으로 즉시 커밋**되고, 따라서 `REQUIRES_NEW`가
필요 없다.

**기록 실패 시 정책은 성공·실패 경로가 의도적으로 비대칭이다**(봉인 목록의 carve-out과 짝).

- 성공 경로는 기록 실패를 **그대로 전파한다** — 접속기록 없이 토큰이 발급되는 상태를 만들지 않으며,
  개인정보처리시스템 접속기록은 법적 요구사항이라 남기지 못했다면 접속도 허용하지 않는 편이 옳다.
- 실패 경로는 기록 실패를 catch·로깅하고 **원래 인증 예외를 rethrow한다** — 감사 쓰기 실패가 인증
  실패 응답 계약(401)을 500으로 바꾸면 안 된다.

`refresh`(`CeoTokenRefreshService`)는 접속기록을 남기지 않는다 — 토큰 갱신은 새로운 개인정보 접속이 아니라 기존 세션의 연장이다.
존재하지 않는 아이디도 기록하지 않는다 — 임의 username을 쌓으면 **계정 존재 여부를 탐색하는 표면**이 된다.

### 인증 타입의 앱별 중복은 의도된 것이다

**대상**: `auth/service/{Member,Admin,Ceo}LoginService.java`(와 같은 앱의 로그아웃·토큰 갱신 서비스) · `auth/token/*TokenService.java`
(유스케이스 분리 전 이름은 `{Member,Admin,Ceo}AuthCommandService`)

인증 주체(`Member`·`Admin`·`Ceo`), 앱별 `ErrorCode`, `JWT_SECRET_*` 분리 때문에 **통합하면 앱별 인증
경계가 무너진다**(동일 시크릿이면 회원 토큰이 admin 인증을 통과하는 권한 상승). backend/CLAUDE.md의
앱별 중복 허용 목록에 있는 항목이며, "중복 제거" 대상으로 보지 않는다.

### 인바운드 포트의 도입 근거는 다형성이 아니다

**대상**: `**/port/in/*UseCase.java`

구현체가 하나뿐이고 소비자도 하나뿐이라 다형성·교체 가능성의 실익은 0에 가깝다. 도입 근거는
**컴파일 게이트**(컨트롤러가 구체 서비스에 손대는 코드가 애초에 컴파일되지 않는다)와 **경계 계약의
문서화**(그 애그리거트의 연산 계약을 한 파일이 고정한다)다.

- **인자가 하나뿐인 연산은 Command로 묶지 않는다** — 이름 있는 record로 얻는 이득(같은 타입 인자
  순서 착각 방지)이 인자 1개에는 존재하지 않는다.
- **배치 잡 UseCase에는 Command record가 없다** — 스케줄이 유일한 입력이라 경계에서 받을 값이 없고,
  파라미터 없는 연산에 빈 Command를 만드는 것은 형식만 맞추는 껍데기다.
- `@Scheduled` 트리거가 이 인터페이스만 주입하고 구현을 알지 않아야 **잡 본문 교체·테스트 대역
  주입**이 가능하다.

### 화면용 Result와 포트 산출물 Result를 가르는 기준

**대상**: `point/port/out/PointHistoryItemViewResult.java` · `coupon/port/out/MyCouponListItemResult.java` ·
`payment/port/out/PaymentViewResult.java` · `order/port/out/OrderDetailViewResult.java` ·
`grade/port/out/GradeInfoResult.java`

포트 산출물 Result는 **포트 하나의 산출물**을 담는다. 아래에 해당하면 그 Result에 필드를 얹지 않고
같은 `<ctx>/port/out`에 별도 Result를 둔다.

- **DB 값이 아니라 계산 결과인 필드**가 있다(포인트 사용 내역의 부호 반전 — 저장된 양수를 음수로
  뒤집는 이 규칙은 표현 규칙이 아니라 회원 화면의 도메인 규칙이라 컨트롤러가 흉내낼 수 없다).
- **조회 시각 기준 파생값**이 있다(남은 일수·만료 여부).
- **VO 언랩·enum 강등**이 필요하다(금액이 `Money` VO라 `.value()`를 꺼내야 하는데 그것은 도메인
  타입을 아는 일이다).
- **다른 컨텍스트에 물어본 값이 합쳐진다**(주문상품의 리뷰 작성 여부).
- **포트 자체가 없다**(등급 정책은 도메인 enum 상수에서 파생되는 정적 목록이라 DB를 읽지 않는다).

**이 Result들은 어떤 금액도 계산하지 않는다** — 합계·할인 분해·최종금액 산출은 도메인과 DAO 투영이
이미 끝냈고, 이 계약은 거처만 옮긴다.

### 필드 셋이 다른 Result를 통합하지 않는다

**대상**: `coupon/port/out/MemberCouponItemResult.java` · `order/port/out/OrderListItemResult.java` ·
`product/port/out/ProductOptionGroupManagementResult.java` ·
`review/port/out/ShopReviewManagementDetailResult.java`

이름이 비슷하다고 상위집합 필드를 갖는 하나로 합치지 않는다 — 관리 화면에만 필요한 필드를 손님
응답 경로로 흘리면 **과잉 노출**이 되고, 어느 필드가 어느 화면 계약인지 추적할 수 없게 된다.
Result가 소비자별로 분리돼 있어 **실수로 새기 어렵다**는 것 자체가 이 배치의 이득이다(배달 평가가
ceo 전용 Result에만 있는 것이 그 사례).

### 관리 전용 조회가 별도 경로를 갖는 이유

**대상**: `review/port/out/ReviewBlindNoticeResult.java` ·
`review/service/ReviewBlindConsentQueryService.java`

일반 리뷰 상세 조회는 `hidden.isFalse()` 필터에 걸려 게시중단 리뷰에 404를 낸다. 그 필터는
**"게시중단은 정책 위반 제재"라는 판단이라 완화할 수 없으므로**, 작성자 본인에게만 열리는 전용
경로를 따로 둔다. 인가가 핵심이라 투영 결과의 작성자와 인증 주체 일치를 **재검증**하고, 불일치·부재를
모두 404로 응답한다.

### 스냅샷은 재조회하지 않는다

**대상**: `order/port/out/OrderProductResult.java`

주문 시점 가격·가격명은 스냅샷이므로 상품에서 재조회하지 않는다 — 가격명을 바꿔도 **과거 주문
전표가 변하지 않아야** 하기 때문이다.

### 인덱스는 파생 읽기모델이고 진실원은 원본이다

**대상**: ceo-application `shop/service/ShopRequestDetailQueryService.java` → `toStorePriceVerificationDetailResult`(유스케이스 분리 전 이름은 `ShopRequestQueryService`)

상세는 원칙적으로 원본 애그리거트를 다시 읽는다. 다만 인증 요청 유형은 원본이 **product 컨텍스트
소유**(승인의 본체가 `PRODUCT_PRICE` 갱신)여서 상세를 투영하는 shop 조회 DAO가 없다. 인덱스 상태는
접수·전이 시점마다 `ShopRequestIndexRecorder`가 동기화하므로 목록과 같은 값이고, 화면이 이 유형에서
필요한 것은 진행 상태와 반려 사유뿐이라 인덱스 값을 그대로 쓴다.

### 빈 상태·판정 불가를 예외로 만들지 않는다

**대상**: `shop/service/ShopPriceBadgeQueryService.java` → `getPriceBadges` ·
`shop/service/ShopNoticeQueryService.java` → `getShopNotice` ·
`product/service/ProductDetailQueryService.java` → `findProductById` ·
ceo-application `region/service/AdminDongBoundaryQueryService.java` → `getAdminDongBoundaries`(유스케이스 분리 전 이름은 `AdminDongQueryService`)

부가 표시(뱃지)는 판정 불가가 **가게 화면 전체를 깨서는 안 되므로** 예외 대신 `false`를 준다.
공지가 없는 것은 에러가 아니라 `null`이다 — 대부분의 가게에 공지가 없으므로 404를 쓰면 프론트가
정상 상태를 에러로 처리하게 된다. 가격 행이 없는 이관 이전 메뉴도 예외 대신 빈 목록을 준다(상세가
500으로 막히면 그 메뉴는 아예 팔 수 없다). 지도 축소도 정상 조작이라 **400이 아니라 빈 배열 +
`truncated: true`**로 응답한다.

### N+1을 부르는 반복 조회는 호출부가 한 번에 읽는다

**대상**: `review/service/ReviewWrittenProductIdsQueryService.java` → `findReviewedProductIds` ·
`product/service/ProductBatchQueryService.java` → `findProductsBatch`

주문 상세처럼 항목이 여러 건인 화면이 항목마다 단건 조회를 부르면 항목 수만큼 쿼리가 나간다.
호출부가 **루프 전에 1회 조회**한 뒤 메모리에서 판정하거나, 가격 행을 한 번에 읽어 그룹핑한다.

### 서버가 판정해 가리는 필드는 표현 계층에 맡길 수 없다

**대상**: `review/port/out/ReviewDetailView.java` · `review/service/ReviewDetailQueryService.java`
→ `toReviewDetailView`

배달 평가 3필드는 **뷰어가 작성자 본인일 때만** 채워진다(규격상 다른 고객에게 노출 금지, 본인은
수정 폼 초깃값으로 필요). 판정이 컨트롤러로 새면 **다른 호출부가 그 가림을 빠뜨릴 수 있으므로**
이 계약에 담긴 시점에 세 필드는 이미 "보여도 되는 값"이고 `null`이면 가려진 것이다.

**수정 폼은 받은 값을 그대로 되돌려 보내야 한다** — 수정 API는 PUT(전체 교체) 의미라 받은 값을 조건
없이 덮어쓴다. "값이 없으면 유지"를 서버에 넣지 않은 것은 그 순간 `null`이 "안 보냄"과 "지워줘" 두
뜻을 갖게 되어 **배달 평가를 지울 방법이 사라지기** 때문이다.

### 가시성 가드의 위치가 조회와 등록에서 다른 것은 의도다

**대상**: `review/service/ReviewDetailReader.java` → `requireVisibleReview` ·
`review/service/ReviewVisibilityQueryService.java` → `requireVisibleReview`

조회(GET)는 조회 서비스가 `ReviewDetailReader`로 직접 가드를 걸지만(예: `ReviewCommentListQueryService`), 등록(POST)은 컨트롤러가 가드를 호출한 뒤 command
서비스를 부른다 — command 서비스가 query 서비스를 주입받는 것이 **CQRS 교차 주입 금지 위반**이기
때문이다. **한쪽으로 통일하려다 중복 쿼리를 만들지 않는다.**

### 하위 호환을 위한 정규화

**대상**: `review/service/ReviewCreateService.java` → `createReview`·`validateDeliveryRating` ·
`review/service/ReviewUpdateService.java` → `validateDeliveryRating`

기존 클라이언트가 보내지 않는 필드는 `null`로 오므로 박싱 타입으로 받아 정규화한다(미전송 시 공개).
배달 평가도 **둘 다 null이면 검증 자체를 건너뛴다**. 새 필드를 추가할 때 이 형태를 따른다.

### 앱 클래스패스가 곧 스캔 포함 기준이다

**대상**: 각 앱 부트스트랩의 중첩 `ApplicationLayerScanConfig` · 각 앱 `ApplicationModuleClasspathTest`

application 계층 스캔에는 필터가 없다. **무엇이 뜨는지는 앱의 클래스패스(코어 + 자기 앱 모듈)와 일반 스테레오타입(`@Service`/`@Component`)이 정한다** — 위 [빈 배선](#빈-배선--앱-부트스트랩의-중첩-스캔-설정).

- **다른 앱 모듈을 앱 의존에 추가하지 않는다** — 클래스패스에 있는 것은 다 스캔되므로 그 앱의 빈이 전부 뜬다. 각 앱 `ApplicationModuleClasspathTest`(`assertLoadsOnlyOwnApplicationModule`)가 막는다.
- **앱 소속을 표현하는 애노테이션을 만들지 않는다**(위 "앱 모듈 경계 가드").
- **application은 자기 등록하지 않는다** — 라이브러리 모듈처럼 이 모듈도 `AutoConfiguration.imports`·스캔 설정을 갖지 않는다. 스캔 범위는 각 앱 부트스트랩의 중첩 `ApplicationLayerScanConfig`가 정하고, 라이브러리 모듈은 같은 부트스트랩의 중첩 `ModuleScanConfig`가 문자열 스캔으로 조립한다. 경위는 [ADR-0035](../../docs/adr/0035-application-config-into-bootstrap.md)·[ADR-0038](../../docs/adr/0038-autoconfiguration-imports-removal.md).

### `applicationShouldNotDeclareComponentScan` — application에 `@ComponentScan`을 되살리지 않는다

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`
→ `applicationShouldNotDeclareComponentScan` (`noClasses().should().beMetaAnnotatedWith(ComponentScan.class).orShould().beAnnotatedWith(ComponentScans.class)`)

application 모듈의 어떤 클래스도 `@ComponentScan`(직접·메타)이나 `@ComponentScans`를 선언할 수 없다. "어느 앱이 무엇을 스캔하는가"는 앱 조립 지식이라 컴포지션 루트(각 앱 부트스트랩의 중첩 `ApplicationLayerScanConfig`)의 것이고, 이 모듈에 스캔 선언이 돌아오면 과거의 `{App}ApplicationConfig` 구조가 되살아난다. 앱 쪽 오기입은 각 앱 모듈의 `ApplicationLayerScanConfigTest`가 막는다(`backend/{web-api,admin-api,ceo-api,batch-module}/AGENTS.md`의 봉인·가드 목록). `allowEmptyShould`는 쓰지 않는다 — 대상이 모듈 전체라 비지 않는다.

### 앱마다 얇은 래퍼를 두는 이유 — 트랜잭션 경계는 앱의 관심사다

**대상**: web-application `file/service/MemberFileUploadService.java` · admin-application `file/service/FileManagementUploadService.java` · ceo-application `file/service/FileOwnerUploadService.java`
(유스케이스 분리 전 이름은 `FileUpload*CommandService` — 예: ceo `FileUploadOwnerCommandService`·포트 `FileUploadOwnerCommandUseCase` → 지금 `FileOwnerUploadService`·`FileOwnerUploadUseCase`)

업로드 규칙 본체(허용 확장자·용량 한도·저장 경로·이벤트 발행)는 도메인 서비스가 단독으로 갖고,
이 클래스들은 `MultipartFile` 어댑팅과 `@Transactional` 경계 선언 둘만 한다. **세 벌은 로직 중복이
아니라 경계 선언 3개다** — 과거 api-common의 `FileService` 한 벌이 겸했는데, 표현 모듈이
`@Transactional` 유스케이스를 갖는 데다 application이 그것을 주입받아 **application → 표현 역방향
의존**이 생겼다.

### 조회 전용 컨텍스트에는 CommandService를 두지 않는다

**대상**: ceo-application `region/service/AdminDongListQueryService.java`·`AdminDongTreeQueryService.java`·`AdminDongBoundaryQueryService.java`(유스케이스 분리 전 `AdminDongQueryService` 한 클래스) · admin-application `ceo/service/CeoManagementQueryService.java`

`ADMIN_DONG`은 시드 SQL로만 관리하는 read-only 마스터다. 점주 계정의 생성·수정은 ceo-api가 담당하므로
관리 조회 쪽에는 명령 유스케이스 서비스(쌍 시절의 CommandService)를 두지 않는다. **빈 명령 서비스·포트를 형식으로 만들지 않는다** — 유스케이스 분리 후에는 연산이 없으면 포트도 서비스도 없다.

### `QueryUseCase`에는 컨트롤러 표면만 올린다 — 협력용 public 메서드를 전사하지 않는다

**대상**: `application-core/src/main/java/com/tastyhouse/application/**/port/in/*UseCase.java`(5모듈)와 그 구현 서비스

- **UseCase를 구현한 클래스는 구체 타입으로 주입하지 않는다.** 서비스 간 협력도 `port.in` UseCase 인터페이스로 주입한다(`LayerRulesTest#servicesShouldDependOnUseCasesNotImplementations`). 경위는 [ADR-0033](../../docs/adr/0033-usecase-interface-injection.md).
- **협력에 필요한 것이 domain-free 타입(`port.out` `*Result`·원시값)이면 UseCase에 선언한다.** 예: `ReviewShopByRatingQueryUseCase`·`ReviewShopStatisticsQueryUseCase`·`ReviewMemberCountQueryUseCase`·`ReviewWrittenProductIdsQueryUseCase`·`ReviewMyListQueryUseCase`.
- **도메인 타입을 주고받는 협력이면 UseCase에 올리지 않고, 그 타입을 소유한 write 포트나 도메인 서비스를 직접 주입한다** — 예: 인증 계정 로드는 `AdminLoadPort`/`CeoLoadPort`, 회원 가입은 도메인 서비스 `MemberRegistrationService`. UseCase에 올리지 않는 이유는 둘이다. 도메인 타입이라 `commandRecordsShouldBeBoundaryTyped`(`..port.in..`에서 `com.tastyhouse.domain..` 금지)에 걸리고, 비밀번호 해시를 담은 `Admin`/`Ceo`를 컨트롤러가 닿는 인터페이스로 노출하게 되기 때문이다.
- **위임만 하는 협력 빈을 두지 않는다** — 포트·UseCase 호출 한 줄을 넘겨주기만 하는 빈 대신 호출부가 그 포트나 UseCase를 직접 주입한다. 위임을 걷어낼 때는 그 위임 대상이 제공하던 `@Transactional` 속성을 호출부로 옮긴다(예: `CredentialLoginService#signUp`은 `@Transactional` — 회원 저장과 추천 등록의 원자성, `AdminTokenService#refresh`·`CeoTokenService#refresh`는 `@Transactional(readOnly = true)`).
- **UseCase에 올린 협력 메서드는 컨트롤러 표면이 아니다.** 예를 들어 `ReviewMyListQueryUseCase#findMyReviews`는 `MemberMyReviewListQueryService`가 쓰는 협력 메서드다. 컨트롤러가 이런 메서드를 새로 호출하려면 그 화면 계약이 맞는지 먼저 확인한다.
- **같은 시그니처 주의**: `ReviewMemberListQueryUseCase#findMemberReviews`와 `ReviewMyListQueryUseCase#findMyReviews`는 둘 다 `(Long, int, int) → PageResult<MyReviewListItemResult>`다. 타입으로는 구별되지만, 주입할 포트를 고를 때 아래 차이를 확인한다.
  - `findMemberReviews`: `visibleToCustomer()` 조건이라 고객에게 보이는 리뷰만 나온다. `ownerOnly`는 항상 `false`다.
  - `findMyReviews`: `hidden = false` 조건만 걸려 점주에게만 공개한 리뷰도 포함한다. `ownerOnly`에 실제 값이 들어간다.
  - 둘 다 구현은 `infrastructure/jpa/.../review/query/ReviewQueryAdapter`에 있고, 각각 `findReviewsByMemberId`·`findMyReviews`다.
- **판별법 — `port/in` 인터페이스 파일에 `com.tastyhouse.domain.` 또는 `com.tastyhouse.infrastructure.` import가 생기면 잘못 올린 것이다.** 페이징 계약은 `com.tastyhouse.application.shared.port.out.page`라 domain import가 아니다.
- **가드**: 위 [봉인·가드 목록](#봉인가드-목록)의 "UseCase 구현 구체 주입 금지"·"UseCase 필드명"·"CQRS 규칙의 `*QueryUseCase` 확장" 항목을 본다.

### 리스너를 4앱 전부가 스캔하는 코어 `application-core`에 두는 이유 (개별 사유)

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/**/listener/*.java`

리스너 작성 규칙 일반은 이 문서의 [`<ctx>/listener/`](#ctxlistener--도메인-이벤트-리스너) 절에 있다. 여기에는 **"왜 특정 앱 모듈이 아니라 4앱 전부의 클래스패스에 있는 코어에 두는가"의 리스너별 근거**만 적는다(`listenersAndConfigsShouldResideInCore`) — 공통 답은 "특정 앱에만 뜨게 두면 다른 앱이 같은 이벤트를 발행할 때 후속 처리가 조용히 누락된다"이고, 각 리스너의 발행 경로가 그 근거다.

| 리스너 | 발행 경로가 여럿인 근거 |
|---|---|
| `CouponEventListener` | 발급은 admin(수동)·이벤트 경유(가입·추천 보상), 사용은 web(주문 결제) |
| `MemberEventListener` | 가입·탈퇴가 web-api(본인)와 admin-api(관리자 강제 탈퇴) 양쪽 |
| `PointEventListener` | web(주문 결제)·admin(수동 조정)·이벤트 경유(결제 취소·추천 보상) |
| `PaymentEventListener` | 지금은 web-api뿐이지만 admin-api의 환불·관리 경로가 같은 이벤트를 발행하게 되어도 포인트 연동이 누락되면 안 된다 |
| `ReferralRegisteredEventListener` | 추천 등록이 일반 가입과 소셜 가입(4종) 어느 경로에서도 발생한다 |
| `ProductMenuReviewEventListener` | 평가는 web-api에서 등록되지만 admin-api의 숨김·삭제로도 통계가 바뀐다 |
| `ReviewBlindApprovedEventListener` · `ReviewOwnerReplyEventListener` | 지금은 admin/ceo 경로뿐이지만, **알림 적재는 행위 주체가 아니라 "그 일이 일어났다"는 사실에 반응해야 한다** |
| `PolicyActivatedEventListener` | 활성화 자체가 특정 액터에 묶이지 않는 도메인 불변식(`PolicyActivationService`)이다 |
| `MailVerificationEventListener` · `SmsVerificationEventListener` | 도메인별 분리 원칙 — 한 리스너가 여러 도메인 이벤트를 구독하면 한 도메인의 변경이 다른 도메인의 리스너 파일을 건드린다 |

개별 판단으로 따로 남길 것.

- **`ReferralRegisteredEventListener` — 순서가 중요하다. 적립 먼저, 보상 완료 전이는 그 다음이다.** 전이가 먼저 커밋되면 "완료로 표시됐지만 포인트는 없는" 추천 관계가 남아 적립 실패 건을 상태로 식별할 수 없게 된다. 지금 순서라면 적립 실패 시 추천 관계가 `PENDING`에 머물러 재처리 대상으로 남는다. 과거에는 `ReferralRegistrationService`가 point 애그리거트와 리포지토리를 직접 주입해 적립을 재구현했으나, 적립 시맨틱(잔액 증가 + EARNED 이력 + 적립 이벤트)의 단일 원천은 `PointLedgerService`여야 하므로 컨텍스트를 잇는 책임을 리스너로 옮겼다. `AFTER_COMMIT`이라 이 핸들러가 실패해도 추천 등록은 롤백되지 않는데, **추천 등록과 보상 적립의 결합을 끊기 위해 의도적으로 감수한 트레이드오프**다.
- **`PaymentEventListener` — 적립액 계산의 단일 원천은 `PaymentConfirmationService#calculateEarnedPoint`다**(주문에 기록되는 적립 포인트와 실제 적립액이 갈리지 않도록). 리스너는 이벤트 수신과 트랜잭션 경계만 담당한다.
- **`ReviewBlindApprovedEventListener` · `ReviewOwnerReplyEventListener` — `@Async` + `AFTER_COMMIT` + `REQUIRES_NEW` 3종 세트를 반드시 함께 단다(`@EnableAsync`는 코어 `AsyncConfig` 소유).** `AFTER_COMMIT`만 달면 호출 스레드에서 동기 실행되어 알림 실패가 원본 API로 전파된다 — DB에는 반영됐는데 화면은 실패로 뜨는, "알림이 실패해도 원본은 유효하다"는 판단과 정면으로 어긋나는 상태가 된다. `REQUIRES_NEW`가 없으면 커밋될 트랜잭션이 없어 리스너가 조용히 아무것도 남기지 않는다. 동기가 아니라 리스너인 근거는 「크로스 컨텍스트 후처리」 판정표의 "후처리가 실패하면 원본도 없던 일이 되어야 하는가?"에 **아니오**이기 때문이며, 인증코드 발송이 동기인 것과 반대 방향이다.
- **`ReviewBlindApprovedEventListener`에 가게명 조회가 없는 것은 의도적이다** — 게시중단 안내 문구가 가게명을 노출하지 않는다. 고객에게 필요한 정보는 재노출 예정일이지 어느 가게가 요청했는지가 아니며, **요청 주체를 알리면 리뷰 작성자와 점주 사이의 분쟁을 부추길 수 있다.**
- **`ProductMenuReviewEventListener`의 구독 대상은 REVIEW가 아니라 MENU_REVIEW 이벤트다.** `PRODUCT.rating`의 근거가 MENU_REVIEW로 완전히 옮겨갔으므로 구독도 하나만 남는 것이 맞다 — 두 리스너가 같은 `ProductReviewStatsService`를 호출하면 재집계가 두 번 돌고 "어느 쪽이 진짜 근거인가"가 코드에서 사라진다. `ReviewCreatedEvent`/`ReviewDeletedEvent`는 발행만 남고 소비자가 0이다. **`productId == null` 가드는 유지한다** — MENU_REVIEW의 `product_id`는 NOT NULL이지만 이벤트 record가 VO를 담고 있어 향후 발행 경로가 늘 때 null이 실릴 수 있고, 그 예외는 `AFTER_COMMIT` 리스너에서 조용히 유실된다.
- **`MailVerificationEventListener` · `SmsVerificationEventListener`는 발송을 담당하지 않는다.** 이 이벤트는 인증 **완료** 시점이고 발송은 **발급** 시점에 필요하므로 시점이 다르다 — 발송은 `MailVerificationService#issue`·`SmsVerificationService#issue`가 발급과 원자적으로 수행한다.
- **`PolicyActivatedEventListener`는 재동의 요청·개정 고지 발송을 아직 담당하지 않는다** — 발송 대상이 전체 회원이라 요청 스레드에서 처리할 수 없고 배치·큐 설계가 선행돼야 한다. 그때까지는 전이 사실만 남겨, 어떤 정책이 언제 현행이 됐는지가 발행 지점 밖에서도 관측 가능하게 한다.

### `ErrorResponses` — 정적 유틸이고, cause를 따라가지 않으며, AOP로 하지 않는다

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/shared/error/ErrorResponses.java` → `resolve(Throwable)`
· `backend/application-core/src/main/java/com/tastyhouse/application/shared/error/ErrorDescriptor.java`
· 가드: `backend/application-core/src/test/java/com/tastyhouse/application/shared/error/ErrorResponsesTest.java`

표현 계층(web-api·api-common-module의 `GlobalExceptionHandler`, security-module의 필터 단계 핸들러)이 `com.tastyhouse.domain.exception`을 보지 않고도 에러 응답을 만들게 하는 번역점이다(덩어리 01). 규칙 본문은 `backend/CLAUDE.md` "예외·에러코드 소유 규칙".

- **최상위 예외만 본다 — `getCause()`를 따라가지 않는다.** `resolve`는 넘겨받은 예외 자체가 `BusinessException`(하위 타입 `ResourceNotFoundException` 등 포함)일 때만 `ErrorDescriptor(status, code, message)`를 돌려주고, 그 밖은 `Optional.empty()`다(`message`는 `getMessage()`). 삭제된 전용 `@ExceptionHandler(BusinessException.class)`도 최상위 타입으로만 매칭했으므로 이것이 기존 동작과 같은 의미다. cause를 따라가면 **오늘 500으로 응답되는, 다른 예외에 감싸인 `BusinessException`이 조용히 4xx로 바뀐다** — wire 계약 변경이다. "더 친절하게" 만들려고 cause 탐색을 넣지 않는다.
- **빈이 아니라 정적 유틸이다.** 빈으로 만들면 application 계층 스캔은 앱 부트스트랩의 중첩 `ApplicationLayerScanConfig`가 하므로, api-common의 `ratelimit/ApiCommonRateLimitConfigTest`처럼 앱 부트스트랩 없이 뜨는 컨텍스트에서는 빈을 찾지 못해 실패한다. 상태 없는 순수 번역이라 빈일 이유도 없다(`ProblemDetails`가 static인 것과 같은 판단).
- **AOP로 예외를 번역하지 않는다.** 서비스 경계에서 `BusinessException`을 다른 타입으로 감싸는 aspect를 두면, 예약 경로(web-application `reservation/service/ReservationCreateService` → `ReservationBookingExecutor`)가 **`OptimisticLockConflictException`을 잡아 재시도**하는 루프가 감싼 예외를 못 알아봐 재시도가 깨진다. 게다가 이 모듈에는 aspectjweaver가 없다. 번역은 응답 직전(핸들러)에서 한 번만 한다.
- **표현 계층이 직접 내는 코드(rate limit 429·권한 403·인증 401)는 `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/exception/ApiErrorCode.java`의 `RATE_LIMIT_EXCEEDED`·`ACCESS_DENIED`·`AUTH_REQUIRED`가 갖는다**(`security-module`이 `api-common-module`을 `implementation`으로 의존한다). application에 HTTP 계약 미러(`ErrorContracts`)를 두지 않는다. `AUTH_REQUIRED`는 `WebErrorCode.AUTH_REQUIRED`의 미러라 같은 값이어야 하며, 전역 유일성 검사의 **봉인 예외 1건**이다(아래 "에러 카탈로그 가드"). 경위는 [ADR-0042](../../docs/adr/0042-error-code-module-split.md).

### 에러 카탈로그 가드 — 코드는 던지는 가장 안쪽 모듈에 두고, 봉인 집합을 늘리지 않는다 (에러코드 모듈 분할)

**대상**:
- `backend/application-core/src/test/java/com/tastyhouse/application/shared/exception/ErrorCatalogConventionTest.java`
- `backend/application-core/src/test/java/com/tastyhouse/application/shared/exception/ErrorCatalogSnapshotTest.java` + `backend/application-core/src/test/resources/error-catalog-before.tsv`
- `backend/application-core/src/test/java/com/tastyhouse/application/shared/error/ErrorResponsesTest.java`

| 항목 | before | after |
|---|---|---|
| 카탈로그 | `domain.exception.ErrorCode` 단일 enum 442개 | 7개 — `DomainErrorCode`(153, domain)·`ApplicationErrorCode`(85, 코어)·`WebErrorCode`(88)·`AdminErrorCode`(28)·`CeoErrorCode`(65)·`BatchErrorCode`(1: `ADMIN_DONG_BOUNDARY_FETCH_FAILED`)·`ApiErrorCode`(`api-common-module`) |
| 배치 규칙 | 전부 domain | **그 코드를 던지는 가장 안쪽 모듈.** domain이 던지면 `DomainErrorCode`(application도 함께 던지는 22개 포함), 코어 또는 2개 이상 앱 모듈이 던지면 `ApplicationErrorCode`, 앱 하나만 던지면 그 앱의 `{X}ErrorCode` |
| 예외 | `BusinessException`(구체) · `ResourceNotFoundException`(domain) | `BusinessException`은 abstract. domain은 `DomainException(DomainErrorCode)`, application은 `ApplicationException(ApplicationErrorCodeSpec)` 또는 domain 코드면 `DomainException`. `ResourceNotFoundException`은 `application.shared.exception`으로 이동해 `ApplicationException`을 상속 |
| 가드 | `domain/.../ErrorCodeConventionTest`(`ErrorCode` 하나), `ErrorContractsConsistencyTest` | `ErrorCatalogConventionTest`(7개 카탈로그 전체), `ErrorCatalogSnapshotTest`, `ErrorResponsesTest` |
| Command 가드 | `BusinessException(ErrorCode.INVALID_INPUT)` + carve-out `domain.exception..` | `ApplicationException(ApplicationErrorCode.INVALID_INPUT)`, carve-out 제거 |
| 동작 | — | wire 계약(HTTP 상태·code 문자열·메시지) 불변. 호출부가 없던 23개(`ENTITY_NOT_FOUND`·`ADMIN_NOT_FOUND`·`TAG_NOT_FOUND` 등)만 삭제 |

**규칙**
- 새 코드는 던지는 가장 안쪽 모듈에 둔다. 앱 전용 코드를 **두 번째 앱이 쓰게 되면 코어 `ApplicationErrorCode`로 올린다**(split package라 import 경로는 같다). 앱 모듈의 `{X}ErrorCode`는 같은 앱 안에서만 던진다.
- 코드를 인자로 받는 메서드는 가장 좁은 enum 타입을 쓴다. 앱 코드가 섞이면 `ApplicationErrorCodeSpec`(5개 application 계층 enum이 구현, `ErrorCodeSpec`을 확장), domain 코드와 앱 코드가 섞이면 오버로드한다. **예외 1건**: 코드를 던지지 않고 **싣기만** 하는 값 record는 `ErrorCodeSpec`을 써도 된다 — `backend/ceo-application/src/main/java/com/tastyhouse/application/product/service/ProductAvailabilityFailure.java`뿐이다.
- `ErrorResponses.resolve`는 `instanceof BusinessException`으로 판정하고 cause를 따라가지 않는다(변경 없음). HTTP 상태는 각 enum에서 `int`로 둔다 — api 모듈이 코드 enum을 볼 수 없기 때문이다.

**`ErrorCatalogConventionTest`가 검사하는 것과 봉인 집합** — 전역 code 유일성(봉인: `AUTH_REQUIRED` 미러 1건), `name == code`(봉인: `*_VERIFICATION_CODE_*` 6건 — `SMS_`/`MAIL_` × `NOT_FOUND`·`EXPIRED`·`MISMATCH`), `*_NOT_FOUND → 404`(봉인 4건: `SMS_VERIFICATION_CODE_NOT_FOUND`·`MAIL_VERIFICATION_CODE_NOT_FOUND`·`REFERRAL_REFERRER_NOT_FOUND`·`FOLLOW_NOT_FOUND`), HTTP 상태 400~599, 메시지 비공백, 계층 계약(각 enum이 자기 계층 예외로만 던져지는 구조). 봉인 사유는 프론트가 분기하는 wire 계약이라 지금 고치면 클라이언트가 깨지는 것이다 — `backend/domain/AGENTS.md`의 같은 이름 봉인 항목 참고. **봉인 집합에 새 항목을 추가하지 않는다.** 노후 감지 짝 테스트(`whitelistIsNotStale`)가 고쳐진 봉인을 알려준다.

**`ErrorCatalogSnapshotTest`** — 7개 카탈로그의 `(code, status, message)` 합집합이 스냅샷 `error-catalog-before.tsv`(445행)에서 삭제한 23개를 뺀 **422건**과 같음을 단정한다. 분할이 wire 계약을 바꾸지 않았다는 증거다. **스냅샷 파일을 갱신해 테스트를 맞추지 않는다** — 기존 코드의 코드·상태·메시지를 바꾸면 이 테스트가 실패해야 정상이다. 의도한 wire 변경이라면 그 사실을 문서에 먼저 적는다. **신규 에러코드는 additive라 wire 계약을 깨지 않으므로** 다음 절차로 추가한다 — (1) 소유 카탈로그 enum에 상수 추가, (2) tsv에 `NAME\tstatus\tCODE\tmessage` 한 행 추가, (3) `hasSize`를 1 올림, (4) `backend/CLAUDE.md`의 "예외·에러코드 소유 규칙" 절과 이 문서 위 표의 개수 표기를 함께 갱신. ([ADR-0056](../../docs/adr/0056-error-catalog-additive-snapshot.md)) `application`의 build.gradle에 `testImplementation project(':api-common-module')`가 추가됐다(`ApiErrorCode`를 검사하기 위함).

### 03a로 domain에서 옮겨 온 설계 근거 (도메인 서비스)

아래 항목은 원래 `domain/AGENTS.md`의 "코드 주석에서 이관된 설계 근거" 절에 있었다. 대상 서비스가 03a로 이 모듈의 `<ctx>/service/`로 옮겨와 함께 옮겼다. **상대 경로(`shop/service/...`·`order/service/...`)는 `backend/application-core/src/main/java/com/tastyhouse/application/` 기준**이고, 본문에 등장하는 `<ctx>/model/`·`<ctx>/vo/`·순수 계산기(`*Calculator`·`*Policy`)는 여전히 domain(`backend/domain/src/main/java/com/tastyhouse/domain/`)에 있다. 본문은 그대로이며 "도메인 서비스"라는 말은 "(ServiceConfig 삭제 후에는 `@Service` 없이 앱 마커만 단) 마커 없는 POJO로 이 모듈에 사는, 도메인 불변식을 오케스트레이션하는 서비스"를 뜻한다.

#### 도메인 서비스에 남는 것과 애그리거트에 남는 것의 경계

**대상**: `shop/service/ShopDeliveryTipService.java`, `shop/service/ShopBusinessHourService.java`

도메인 서비스에 두는 것은 **행 하나만 보고는 판정할 수 없는 규칙**뿐이다.

- 집합의 개수·정렬·단조성 (구간별 배달팁의 "3개 이하 + 주문금액 오름차순 + 팁 내림차순")
- 두 리소스에 걸친 상호 배타 (거리별 ↔ 지역별)
- 다른 애그리거트 컬렉션을 읽어야 판정되는 것 (지역별 팁의 행정동이 가게 배달가능지역에 속하는지)
- 집합 관계 (같은 요일 시간대 겹침)

**행 하나의 값 불변식(금액 범위·시각 유효성)은 각 애그리거트의 `of`·`update`가 강제한다.** 서비스에 두면 팩토리를 직접 부르는 경로(배치·마이그레이션)가 규격을 우회할 수 있기 때문이다.

#### replace-all 교체와 변경이력 1행 규칙

**대상**: `shop/service/ShopDeliveryTipService.java` → `replaceTiers`·`replaceRegionTips`·`replaceScheduleTips`

**배달팁 컬렉션은 전부 replace-all로 교체한다.** 위 규칙들이 집합 전체를 봐야 판정되므로, 행 단위 CRUD면 어떤 순서로 조작해도 중간 상태가 규칙을 위반한다. (`ShopBusinessHour`가 개별 CRUD인 것은 요일 간에 이런 관계가 없기 때문이다.)

**변경이력 기록도 이 서비스가 소유한다**(`DELIVERY_TIP_TIER`·`DELIVERY_TIP_DISTANCE`·`DELIVERY_TIP_REGION`·`DELIVERY_TIP_SCHEDULE`·`DELIVERY_TIP_HOLIDAY`). 이 서비스가 replace-all을 하려고 **삭제 전에** 컬렉션을 읽을 수 있는 유일한 지점이고, ceo-api의 `CommandService`는 CQRS 교차 주입 금지로 QueryAdapter를 주입할 수 없어 변경 전 값을 구조적으로 볼 수 없다.

**replace-all은 컬렉션 1행당 이력을 남기지 않고 저장 1회당 1행만 남긴다.** `deleteAll + saveAll`로 교체하므로 PK 기반 diff가 불가능하고, 행 단위로 남기면 이력 목록이 "점주가 저장한 횟수"가 아니라 "바뀐 행 수"로 페이징되어 읽을 수 없게 된다. 그래서 변경 전·후 컬렉션 전체를 `ShopChangeValueFormatter#snapshot`으로 요약해 한 행에 담는다.

이 규칙에서 파생되는 개별 판단들:

- **`tier_order`는 호출부가 보낸 순서가 아니라 정렬 후 재부여한다** — 그래야 저장된 순서와 금액 정렬이 어긋나지 않고, 화면에 보이는 순서와 이력의 순서가 일치한다.
- **`clearDistanceTip`은 설정 헤더가 없으면 이력도 남기지 않는다** — 애초에 거리별을 쓰지 않던 가게에 "해제했다"고 기록하면 일어나지 않은 변경이 이력에 남는다.
- **`clearRegionTips`는 전용 `DELETE` 행을 만들지 않고 빈 컬렉션으로의 교체로 기록한다** — 이 경로가 빈 배열 PUT과 완전히 같은 연산이라, 구분하면 같은 결과가 두 형태로 기록된다.
- **`changeHolidayTip`은 0원(삭제)도 `UPDATE` 한 행으로 남긴다** — 이 엔드포인트는 스칼라 하나를 설정하는 경로이고 0원은 그 스칼라의 유효한 값(미설정)이라, 같은 저장 버튼이 금액에 따라 `UPDATE`/`DELETE`로 갈리면 이력 목록에서 같은 조작이 두 종류로 보인다.
- **지역별 팁 스냅샷에서 마스터에 없는 행정동(폐지 동 등)은 식별자를 그대로 노출한다** — 행을 통째로 빠뜨리면 "그때 무엇이 설정돼 있었는가"가 부정확해진다.
- **`loadOrCreateSetting`은 그 시점에 저장하지 않는다** — 호출부가 전환을 마친 뒤 한 번만 저장해야 "만들었지만 전환에 실패한" 빈 헤더가 남지 않는다.
- **빈 목록으로 지역별을 교체할 때, 거리별을 쓰던 가게의 설정은 건드리지 않는다** — 지역별을 비우는 요청이 거리별 설정을 조용히 지우면 안 된다.

#### 시간 구간 겹침 판정 — 자정 넘김 분할

**대상**: `shop/service/ShopDeliveryTipService.java` → `validateScheduleOverlap`·`toSegments`, `shop/service/ShopBusinessHourService.java`

자정을 넘기는 구간은 `[start, 24:00)`과 `[00:00, end)` **두 조각으로 나눠 판정한다.** 그러지 않으면 22:00~02:00과 01:00~03:00처럼 실제로 겹치는 쌍을 놓친다. 두 서비스가 같은 기법을 쓴다.

**서로 다른 요일 구분끼리는 겹침을 검사하지 않는다** — 겹쳐도 적용 시점에 구체성 우선으로 하나만 선택되므로 이중 부과가 생기지 않고, 오히려 DAILY 기본값 위에 특정 요일을 덧씌우는 정상적인 설정 방식이기 때문이다.

#### 부분실패 판정은 "요청 전체를 반영한 뒤의 최종 상태" 기준

**대상**: `backend/ceo-application/src/main/java/com/tastyhouse/application/product/service/ProductAvailabilityService.java`(메뉴 — `hideProducts`·`markProductsSoldOut`·`releaseProducts`·`changeProductsSoldOutUntil`), `backend/ceo-application/src/main/java/com/tastyhouse/application/product/service/ProductOptionAvailabilityService.java`(옵션 — `hideOptions`·`markOptionsSoldOut`·`releaseOptions`·`changeOptionsSoldOutUntil`), 품절 기간 검증 `backend/ceo-application/src/main/java/com/tastyhouse/application/product/service/ProductSoldOutUntilValidator.java` → `validate`

| 항목 | before | after |
|---|---|---|
| 클래스 | `ProductAvailabilityService` 한 클래스(메뉴·옵션·기간 검증, 생성자 의존 10개) | 메뉴 `ProductAvailabilityService`(의존 3개) / 옵션 `ProductOptionAvailabilityService`(의존 10개) / 기간 검증 `ProductSoldOutUntilValidator` |
| 옵션 유스케이스 4개의 주입 | `ProductAvailabilityService` | `ProductOptionAvailabilityService` |
| 동작 | — | 변경 없음 — 메서드 본문을 글자 그대로 옮겼다(`validateSoldOutUntil(` → `productSoldOutUntilValidator.validate(` 4곳만 다르다) |

메뉴·옵션의 품절·숨김 전이에서 **하나씩 순차로 검사하면 요청 배열의 순서에 따라 결과가 갈린다.** 노출 메뉴가 2개일 때 둘 다 숨김 요청하면 순차 검사는 첫 건을 통과시키고 두 번째만 실패시키는데, 어느 것이 통과할지가 배열 순서에 좌우된다. **최종 상태 기준이면 "노출 메뉴가 0개가 되므로 마지막 1개는 남긴다"는 판정이 결정적이다.**

이 제약들(노출 메뉴 ≥1 · 추천 메뉴 ≥1 · 옵션 `minSelect` 잔여 개수)이 api 모듈이 아니라 도메인에 있는 이유는 **애그리거트 불변식이고, ceo/admin 두 모듈에 흩어지면 한쪽만 고쳐지기 때문**이다.

**두 서비스는 shop 컨텍스트를 참조하지 않는다.** 품절 기간 기본값("익일 가게 오픈 시간") 산출은 `ShopNextOpenTimeCalculator`(shop 컨텍스트)가 담당하고, 품절 유스케이스 서비스(`ProductSoldOutOwnerService`·`ProductOptionSoldOutService`)가 그 계산기와 메뉴·옵션 서비스를 각각 주입해 조립한다 — `ShopBusinessHour`를 직접 참조하면 컨텍스트 경계 위반이다.

**품절 기간 검증(현재+30분 이상·7일 이하)을 `ProductSoldOutUntilValidator`로 따로 둔 이유**는 메뉴와 옵션 두 서비스가 같은 규칙을 써야 하기 때문이다 — 각 서비스에 복제하면 한쪽만 고쳐져 메뉴와 옵션의 허용 기간이 갈린다. `CeoErrorCode`를 던지므로 domain이 아니라 이 모듈에 둔다.

#### 옵션그룹 합치기 — 기준 그룹 불변·흡수 그룹은 숨김

**대상**: `product/service/ProductOptionGroupMergeService.java`

- **기준 그룹은 손대지 않는다.** "기준 옵션그룹"이 곧 살아남는 정의다. 기준을 덮어쓰면 멱등성이 깨지고, 무엇보다 **과거 주문에 박제된 옵션을 조용히 바꾸게 된다.**
- **흡수 그룹은 행을 남긴 채 감춘다**(`hide()`) — `ORDER_PRODUCT_OPTION`이 `option_group_id`로 이 행을 참조하므로 **하드 삭제는 주문 이력을 끊는다.**
- **흡수 그룹의 옵션을 기준 그룹으로 재부모화(union)하지 않는다.** 합치기 확인 화면은 기준 그룹의 옵션 목록 **하나만** 보여주므로 union이면 중복된 합집합이 나와 화면이 약속한 것과 결과가 달라진다. 또한 추천 합치기는 옵션명·가격이 전부 같은 그룹만 제안하므로 union은 순수 중복 생성이다.
- **링크만 기준 그룹으로 옮긴다**(sort 보존 + 재정규화).

#### 주문 접수 — 타 컨텍스트는 전부 소유 컨텍스트의 서비스를 경유한다

**대상**: `order/service/OrderPlacementService.java`

주문 한 건의 접수는 `Order` 헤더 · 상품 라인(`OrderProduct`) · 라인 옵션(`OrderProductOption`) 세 애그리거트를 한 트랜잭션에서 함께 만들고, 그 과정에서 계산한 금액을 헤더에 되반영해야 하는 **원자 연산**이다. 세 애그리거트 중 하나라도 빠지면 주문이 반쪽으로 저장되고, **금액 되반영이 빠지면 결제 금액이 0원인 주문이 남는다.** 쿠폰 사용·포인트 차감까지 같은 트랜잭션에 묶이는 크로스 애그리거트 불변식 오케스트레이션이라 도메인 계층에 둔다.

**타 컨텍스트는 전부 그 컨텍스트의 서비스를 경유한다** — 상품·옵션 검증은 `OrderProductValidationService`(product 소유), 가게 로드·주문가능·최소주문금액·배달지역·배달팁·예약슬롯은 `ShopOrderContextService`(shop 소유), 주문자 조회는 `OrdererLookupService`, 배달 주소 로드·소유권 검증은 `MemberDeliveryAddressService`(둘 다 member 소유)가 담당한다.

과거 이 서비스는 외부 컨텍스트 6개에서 모델·리포지토리를 직접 주입해 26개를 import했는데, 그러면 **각 컨텍스트의 규칙(판매중지 판정·배달지역 미등록 처리·주소 소유권)이 주문 안에서 재구현되어, 소유 컨텍스트가 정책을 바꿀 때 주문 경로만 낡은 규칙으로 남는다.** 지금 남은 타 컨텍스트 의존은 **서비스와 그 결과 record**뿐이며 모델·리포지토리 직접 import는 0건이다.

**이관해도 트랜잭션 경계는 그대로다** — 전부 같은 트랜잭션 안의 동기 호출이며(이벤트로 바꾸지 않는다), 검증 순서·에러코드·응답 계약도 이관 전과 동일하다.

주요 협력자의 존재 이유(생성자 `@param`에 있던 것):

- **`memberDeliveryAddressService`** — 배달 주소 로드와 소유권 검증. **좌표는 저장된 주소에서만 읽는다 — 위조 방지.**
- **`publicHolidayCalendar`** — 접수 시각의 공휴일 여부 판정. **shop이 이 캘린더를 직접 부르면 컨텍스트 경계를 위반하므로, 판정 결과만 배달팁 산출에 넘긴다.**

**주문 접수는 도메인 이벤트를 발행하지 않는다** — 과거 `OrderCreatedEvent`를 발행했으나 수신 리스너가 없는 no-op이어서 제거했다. 접수 이후 비동기 후처리(알림·집계)가 필요해지면 이 메서드 말미에 발행을 다시 추가하면 된다.

**저장 횟수**: 더티 체킹이 없으므로 헤더는 신규 저장 후 금액 갱신으로 **2회**, 상품 라인은 신규 저장 후 가격 갱신으로 **2회** 저장한다.

**반환은 생성된 주문의 식별자(`OrderId`)만이다** — 응답 조립(가게명·상품 라인·결제 요약)은 커밋 이후 소비 모듈의 조회 서비스(web `OrderDetailQueryService`)가 재조회해 담당한다(CQRS 분리).

#### 리뷰 게시중단 — 상태 전이와 반영은 한 트랜잭션

**대상**: `review/service/ReviewBlindRequestService.java`

"점주 요청 → 관리자 심사 → 승인 시 리뷰 숨김 → 30일 뒤 재노출 또는 고객 동의 시 삭제" 워크플로의 규칙은 요청자(ceo)·심사자(admin)·고객(web)·배치가 서로 다른 액터임에도 동일하게 유지되어야 한다.

**승인은 요청 애그리거트의 상태 전이와 리뷰의 숨김 반영이 한 트랜잭션에서 반드시 함께** 일어나야 하는 원자 연산이다 — 둘 중 하나만 반영되면 **"승인됐는데 리뷰가 계속 노출되는"** 상태가 남는다(`ShopImageApprovalService`가 이미지 교체에 대해 갖는 것과 같은 성질).

**`ShopRequestIndexRecorder`를 생성자 필수 의존으로 받는다** — 요청처리 현황(`SHOP_REQUEST_INDEX`)은 파생 읽기모델이고 기록이 누락되면 그 요청이 통합 목록에서 아예 보이지 않는다. 필수 의존으로 두면 **새 상태 전이 메서드를 추가할 때 동기화 배선이 필요하다는 사실이 컴파일 단계에서 드러난다.**

**취소는 원본 애그리거트의 상태 전이**이며, 취소 후에는 같은 리뷰에 재요청이 가능해진다 — PENDING 중복 차단과 1회 제한이 모두 상태 조회에 기반하므로 **코드 추가 없이 자동으로 풀린다.**

**대상 리뷰가 그 가게의 것인지 역조회로 재검증한다** — 경로의 `shopId`만 믿으면 남의 가게 리뷰에 게시중단을 걸 수 있다.

#### 결제 승인 — PG HTTP 왕복은 트랜잭션 밖

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/payment/service/PaymentConfirmationService.java`(+`PgConfirmationTarget`, `@Service`). `PgPaymentGatewayPort`/`PgProviderGatewayPort`는 이 모듈(코어)의 `payment/port/out`에 있다(web·batch가 함께 쓴다). 이 서비스는 이 모듈 `ServiceContextBoundaryTest`의 봉인 목록에 있다.

결제 승인은 결제 애그리거트의 상태 전이와 주문 애그리거트의 확정 전이를 한 트랜잭션에서 반드시 함께 수행해야 하는 원자 연산이다. 한쪽만 반영되면 **"결제는 됐지만 주문은 대기"** 이거나 **"주문은 확정인데 결제는 미승인"** 인 정합성 붕괴가 남는다. 승인 경로는 두 가지(PG 승인 · 현장결제 완료)인데 규칙은 하나여야 하므로 유스케이스 계층에 둔다.

주문 상태 전이는 직접 `order.confirm()`을 호출하지 않고 `OrderTransitionService`(03a로 이 모듈의 `order/service/`로 이동)에 위임한다 — **전이와 저장을 항상 함께 수행한다는 규칙의 단일 원천을 주문 도메인에 유지하기 위함**이다.

**PG HTTP 왕복은 이 서비스 안에서 하지 않는다.** PG 승인은 (1) 금액·상태·소유권을 검증하는 `preparePgConfirmation`(DB 읽기, 트랜잭션 안)과 (2) PG 응답을 반영하는 `applyPgConfirmation(memberId, pgProvider, pgOrderId, result)`/`failPgConfirmation`(DB 쓰기, 별도 트랜잭션)으로 쪼개져 있고, **그 사이의 PG 호출은 소비 모듈이 트랜잭션 밖에서, 도메인 포트 `PgPaymentGatewayPort`(구현은 라우터 `PgPaymentGatewayRouter`, 이 라우터도 `application`으로 이동)를 통해 수행한다.**

과거에는 PG 왕복 전체가 DB 트랜잭션 안에 있어 커넥션·행 락을 네트워크 지연만큼 점유했고, **PG 승인 성공 후 커밋이 실패하면 "PG는 승인, DB는 미승인"이 되어 보상이 불가능했다.** 그래서 이 서비스는 `PgPaymentGatewayPort`를 주입받지 않는다.

이벤트 발행은 Spring `ApplicationEventPublisher`를 직접 주입하지 않고 이 모듈의 포트 `shared/event/DomainEventPublisher`를 쓴다(구현 `SpringDomainEventPublisher` — 위 "덩어리 03a" 절).

#### 예약 — 정원 차감은 낙관적 락, 재시도는 트랜잭션 밖

**대상**: `reservation/service/ReservationBookingService.java`

예약 생성은 "슬롯 정원 검증 → 정원 차감 → 예약 저장"이, 취소·거절은 "예약 상태 전이 → 슬롯 정원 반납"이 반드시 함께 일어나야 하는 원자 연산이다. 도메인 계층에 두어 **트리거 액터(회원 취소 · 점주 거절)가 달라도 "예약 건수와 슬롯 점유 수는 항상 함께 움직인다"는 규칙이 갈리지 않게** 한다.

**동시성**: 정원 차감은 슬롯의 낙관적 락(`@Version`)으로 보호한다. 이 서비스는 차감 직후 `saveImmediately`로 **충돌을 트랜잭션 커밋 전에 노출시키기만 하고 재시도는 하지 않는다** — 재시도는 매 시도마다 새 트랜잭션이 필요하므로 트랜잭션 경계 **바깥**(소비 모듈의 command 서비스)에서 수행해야 한다. 충돌 예외는 프레임워크-프리 `OptimisticLockConflictException`(낙관적 락)·`UniqueConstraintConflictException`(유니크 충돌)으로 번역되어 올라간다(infrastructure 어댑터가 번역).

#### 자주 쓰는 문구 — 5개 상한은 완전하지 않다 (의도된 감수)

**대상**: `ceo/service/CeoReplyPhraseService.java`

**5개 상한은 DB가 아니라 애플리케이션 코드가 강제한다 — 그래서 완전하지 않다.** MySQL에는 "한 점주당 행 5개 이하" 같은 행 수 제약을 걸 수단이 없으므로, 건수 조회와 삽입 사이의 경합을 막을 최종 방어선이 존재하지 않는다. **같은 점주가 동시에 등록 요청을 보내면 6개가 될 수 있다.**

이를 감수하는 이유는 (1) 이 목록이 답변 작성 시 골라 쓰는 **표시용**이라 6개가 되어도 데이터 정합성이나 금전에 피해가 없고, (2) 이를 막으려면 점주 행에 비관적 잠금을 걸어야 하는데 **그 비용이 피해에 비해 과하기** 때문이다. 초과 상태가 발견되면 점주가 하나 지우면 그만이다.

소유권은 `shopId`가 아니라 **문구의 `ceoId`와 요청 점주의 일치**로 검증한다 — 문구는 가게가 아니라 점주 계정에 귀속되므로 `ShopOwnershipValidator`가 개입할 자리가 없다. 불일치는 404가 아니라 `CEO_REPLY_PHRASE_ACCESS_DENIED`(403)다.

**등록·수정 시점에 금칙어를 검수**하므로 실제 답변에 넣을 때 `ReviewOwnerReplyService`가 한 번 더 검수하는 것과 중복되지만 **그것이 의도다** — 문구 등록 후 금칙어 목록이 늘어났을 수 있다.

#### 03a로 `infrastructure:persistence`에서 옮겨 온 설계 근거

아래 항목은 `infrastructure/persistence/AGENTS.md`에 있던 것이다(대상 `<Ctx>DomainConfig` 18개, `CachingProhibitedWordLoadPort`, `SpringDomainEventPublisher`가 이 모듈로 옮겨왔다). 본문의 "`<Ctx>DomainConfig`의 `@Bean xxx`"는 **같은 이름의 서비스 클래스**(빈 이름 = 클래스명 첫 글자 소문자)로 읽는다 — 도메인 서비스는 `@Service`로 스캔 등록되고, domain 계산기·`prohibitedWordValidator`·`shopDeliveryTipRangePolicy`만 `shared/config/SharedBeanConfig`의 `@Bean`이다.

##### `<Ctx>ServiceConfig`(구 `<Ctx>DomainConfig`) — 도메인 서비스 빈 등록 근거

**대상**: 코어·앱 모듈 `src/main/java/com/tastyhouse/application/*/service/`의 도메인 서비스 클래스들 + `backend/application-core/src/main/java/com/tastyhouse/application/shared/config/SharedBeanConfig.java`

등록 위치 규칙 자체는 이 문서의 "application `*ServiceConfig`를 두지 않는다" 절에 있다. 여기에는 **각 `@Bean`이 왜 도메인 서비스인가**(= 왜 애그리거트나 api 모듈이 아닌가)라는 판단 근거를 모은다. 전 config에 공통으로, 클래스 Javadoc은 "도메인 서비스는 `@Service` 없는 순수 POJO라 Spring이 스캔할 수 없으므로 새 POJO 도메인 서비스를 추가하면 여기에 `@Bean`을 추가한다"는 같은 문장이었다 — 규칙 절과 중복이라 옮기지 않는다.

##### 도메인 서비스로 뺀 판정 기준

주석들이 반복해 든 사유는 아래 네 가지다. 새 서비스를 만들 때도 이 기준으로 판단한다.

| 사유 | 뜻 | 예 |
|---|---|---|
| **집합 차원 불변식** | 행 하나만 보고는 판정할 수 없다 | `ShopNoticeExposureService`(가게당 노출 공지 1건) · `ShopMenuCollectionImageService`(최대 6·최소 1) · `ShopDeliveryAreaAdjustmentService`(진행 중 신청 중복 차단) |
| **크로스 애그리거트 원자성** | 여러 애그리거트가 한 트랜잭션에서 함께 바뀌어야 한다 | `ShopImageApprovalService`(요청 승인 + 이미지 반영) · `ShopPhoneNumberRegistryService`(대표번호 + 가게 애그리거트) · `ProductReviewStatsService` |
| **액터 무관 규칙** | 요청자(ceo)와 검수자(admin), 또는 admin CRUD와 batch 크롤링이 **같은 규칙**을 써야 한다 | `ProductRegistrationService` · `ShopOrderNoticeService` · `ShopRequestCancellationService`(이름이 비슷한 `ShopRequestCancelService`는 이 도메인 서비스를 호출하는 유스케이스 서비스다) |
| **컨텍스트 경계 파사드** | 소비 컨텍스트가 남의 모델·리포지토리를 직접 쓰지 않게 한다 | `ShopOrderContextService` · `OrderProductValidationService` |

복제하면 한쪽만 고쳐진다는 것이 공통 위험이다 — `ShopNextOpenTimeCalculator`가 요일별 영업시간 선택 규칙을 새로 짜지 않고 `ShopOperatingStatusCalculator`를 주입해 재사용하는 것도 같은 이유다(복제하면 요일 구분 추가 시 한쪽만 고쳐진다).

##### `ShopDomainConfig` — 개별 판단

**대상**: 각 항목의 빈 이름과 같은 이름의 클래스 `shop/service/{ShopOrderAvailabilityService,ShopDeliveryAreaPolygonService,...}.java`(코어 또는 소비 앱 모듈). `prohibitedWordValidator`·`shopNextOpenTimeCalculator`는 `backend/application-core/src/main/java/com/tastyhouse/application/shared/config/SharedBeanConfig.java`의 `@Bean`

- `prohibitedWordValidator` — **캐싱 데코레이터로 감싼 포트를 주입한다.** 검증기가 텍스트 검증마다 `findAll()`을 호출하므로 전량 로드가 매번 DB로 나가지 않게 한다. 금칙어는 SQL 시드 read-only 데이터라 정합성 리스크가 낮고, **캐싱을 어댑터 쪽에 두어 domain의 순수 POJO 검증기는 그대로 둔다.**
- `shopNextOpenTimeCalculator` — **product가 아니라 shop에 둔다.** 영업시간·휴무일 해석은 shop의 관심사이고, product 도메인 서비스가 `ShopBusinessHour`를 직접 참조하면 **컨텍스트 경계 위반**이다. 두 서비스의 조립은 ceo-api의 command service가 담당한다.
- `shopOrderAvailabilityService` — 주문 접수(`OrderPlacementService`)와 예약 생성(`ReservationBookingService`)이 **같은 규칙**을 쓰도록 검증을 이 서비스 하나에 모았다.
- `shopDeliveryAreaPolygonService` — 도형 원본과 그것을 환산한 행정동 집합이 **같은 트랜잭션에서 항상 일치**해야 한다. 환산을 비동기로 미루면 "저장은 됐는데 주문은 거절되는" 창이 생기고, **그 사이 등록 건수가 0이 되면 주문 접수의 지역 검사가 통째로 비활성된다.**
- `shopDeliveryAreaRadiusService` — 후보 행정동을 **write 포트로 읽는다.** 명령 경로가 infra query DAO를 주입하면 CQRS 교차 주입 금지 규칙에 걸린다. 거리 판정은 원 근사 다각형이 아니라 **하버사인 직선거리**로 한다.
- `shopOriginInfoService` — **금칙어 검수를 하지 않는다.** 원산지 본문은 마케팅 문구가 아니라 **법령이 요구하는 사실 표시**라, 검수로 저장을 막으면 표시 의무를 이행할 수 없게 된다.
- `shopChangeHistoryRecorder` / `shopCeoAssignmentRecorder` / `shopRequestIndexRecorder` — 변경을 수행하는 도메인 서비스가 **같은 트랜잭션에서 동기 호출**한다. **새 배정 경로나 새 요청 성격 애그리거트를 만들면 그 도메인 서비스에 이 Recorder를 배선해야 한다** — 배선 누락은 컴파일에 걸리지 않는다. `ShopChangeValueFormatter`는 상태 없는 static 유틸이라 빈으로 등록하지 않는다.
- `shopOrderNoticeService` — PUT 하나가 기존 행 유무에 따라 insert/update로 갈리므로 단일 애그리거트 연산이 아니고, 그 분기 규칙을 ceo·admin 두 api 모듈이 각자 갖지 않도록 도메인 서비스가 소유한다. 승인 절차가 없어 상태 전이가 `hidden` 하나뿐이라 서비스를 더 쪼갤 이유가 없다.
- `shopCeoAssignmentService` — 재배정을 `REVOKE` + `GRANT` **2행**으로 남긴다.

##### `ProductDomainConfig` — 개별 판단

**대상**: 각 항목의 빈 이름과 같은 이름의 클래스 `product/service/{ProductDeletionService,ProductSortService,...}.java`(코어 또는 소비 앱 모듈). `cupDepositPolicy`는 `backend/application-core/src/main/java/com/tastyhouse/application/shared/config/SharedBeanConfig.java`의 `@Bean`(단일 빈이라 "같은 인스턴스" 근거가 성립한다)

- `cupDepositPolicy` — 순수 계산기인데도 **빈으로 두는 이유는 요율을 단 한 곳에 두기 위함**이다. 점주 설정(ceo)·손님 메뉴판(web)·주문 금액 확정(order) 세 경로가 **같은 인스턴스를 주입받아야** "화면 금액과 결제 금액이 다른" 사고가 구조적으로 불가능해진다.
- `productDeletionService` — 삭제에도 숨김과 **같은 불변식**(노출 메뉴 ≥1 등)을 적용한다. **숨김만 막고 삭제를 열어두면 점주가 삭제로 우회해 빈 메뉴판을 만들 수 있다.**
- `productSortService` — `sort` 값을 클라이언트에서 받지 않고 **순서 있는 id 배열만 받아 서버가 0..N-1로 정규화**한다.
- `productOptionGroupLinkService` — **옵션그룹은 단일 가게에만 속한다**는 불변식을 강제해, 소유권 판정에서 ANY/ALL 구분이 사라지게 한다. 이 불변식이 `ProductOptionGroupQueryAdapter#findLinkedProductsByShop`(분할 전 `ProductQueryAdapter`)의 단일 조회를 성립시킨다.
- `productOptionGroupMergeService` — 링크 재배치는 `ProductOptionGroupLinkService#relink`에 위임한다. UNIQUE 충돌 처리와 sort 불변식이 그 클래스 소유로 남아야 `renumber`를 공개하지 않아도 된다.
- `productExposureService` — 요일 묶음과 개별 요일의 **혼용을 금지한다.** 그 조합을 저장할 수 없게 하면 SQL 술어(`ProductStorefrontQueryAdapter#exposedNow` — 분할 전 `ProductQueryAdapter`)와 계산기(`ProductExposureCalculator`)가 갈릴 여지가 없다.
- `productNutritionService` — 영양성분과 알레르기를 **한 서비스가 소유한다.** 나누면 "영양성분만 저장되고 알레르기는 이전 값이 남은" 중간 상태가 손님 화면에 **잘못된 알레르기 표시**로 노출된다. 승인 워크플로가 없는 것은 점주(가맹본사)만이 아는 사실 정보여서 관리자가 검증할 근거가 없기 때문이다.
- `productPriceService` — **전체 교체(PUT) 의미론**이라 정렬·가격명 중복 같은 컬렉션 단위 불변식을 한 번에 판정한다. `sort=0` 행의 배달가를 `PRODUCT.original_price`에 **동기화**해 그 컬럼을 읽는 기존 수십 경로(주문·검색·오늘의할인·목록)의 동작을 그대로 유지한다. `StorePriceVerificationPort`를 받는 이유는 가격 변경으로 배달가 > 매장가가 되면 **그 자리에서** 가게 인증을 내려야 하기 때문이다 — 배치로 미루면 그 사이 손님이 잘못된 뱃지를 본다.

###### 승인 워크플로의 방향 비대칭

→ `productImageApprovalService` · `productRepresentativeApprovalService` · `productVegetarianApprovalService`

**등록·지정은 승인을 거치고, 순서 변경·삭제·해제는 즉시 반영된다.** 검수의 목적이 "부적합한 내용의 노출을 막는 것"이므로 내리는 방향에는 그 위험이 없기 때문이다. 채식 설정만은 신청조차 즉시 반영하지 않는데, **채식 표기가 알레르기·신념과 직결돼 잘못된 표기의 대가가 크기** 때문이다.

사장님 추천은 가게당 최대 6개·이미지 필수·최소 1개 유지 세 제약을 `ProductRepresentativeApprovalService`가 단독으로 소유한다. 세 번째 제약은 일괄 숨김(`ProductAvailabilityService`)이 이미 쓰는 `PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE`를 재사용하므로, **두 경로가 같은 하한을 공유한다.**

##### `CachingProhibitedWordLoadPort` — 캐싱은 도메인이 아니라 어댑터에 둔다

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/shop/service/CachingProhibitedWordLoadPort.java`
→ `TTL` · `Snapshot`

`ProhibitedWordValidator`는 텍스트 검증 때마다 `ProhibitedWordLoadPort#findAll()`을 호출하는데, 점주 입력(가게소개·찾아오는길 등) 저장 경로마다 금칙어 테이블을 통째로 다시 읽는 것이 낭비다. **검증기는 마커 없는 순수 POJO(03a 이전에는 domain 소속)라 스프링 `@Cacheable`을 붙일 수 없으므로**, 캐싱을 write 포트를 감싸는 데코레이터로 구현하고 빈 등록 지점(`SharedBeanConfig#prohibitedWordValidator`, 구 `ShopServiceConfig` ← `ShopDomainConfig`)에서 감싼다 — 검증기·도메인 서비스 코드는 그대로다. 03a 이전에는 이 데코레이터가 persistence(`infrastructure/shop/persistence/`)에 있었으나, 감싸는 쪽(설정)이 이 모듈로 오면서 함께 옮겼다 — 순수 자바라 기술 의존이 없다.

금칙어는 SQL 시드로 관리되는 read-only 데이터(Java 계층에 생성·수정 경로가 없다)라 정합성 리스크가 낮다. 그래도 **무기한 캐싱은 시드 갱신이 재기동 전까지 반영되지 않으므로 TTL(10분)을 둬서 자연히 만료시킨다.**

`AtomicReference`에 (적재 시각, 목록) 스냅샷을 통째로 담아 교체하므로 **락이 필요 없다.** 만료 직후 동시 호출이 겹치면 적재가 중복될 수 있으나 결과가 같은 read-only 조회라 무해하다 — 중복 적재를 막는 락이 주는 이득보다 락 경합 비용이 크다.

##### `SpringDomainEventPublisher` — 발행 포트 어댑터

**대상**: `backend/application-core/src/main/java/com/tastyhouse/application/shared/event/SpringDomainEventPublisher.java`

`DomainEventPublisher` 포트(03a로 domain에서 이동, 같은 패키지)를 Spring `ApplicationEventPublisher`에 위임한다. `@TransactionalEventListener`/`@EventListener` 기반 리스너가 그대로 수신한다. **03a 이전에는 persistence의 마커 없는 `@Component`였고**, 지금은 `@Component`를 떼고 `shared/config/SharedBeanConfig`(`@SharedApp`, 구 `SharedEventConfig`)가 `@Bean domainEventPublisher`로 등록한다 — 포트가 `port.out` 밖(`shared/event`)에 있어 persistence가 볼 수 없게 됐기 때문이다. 리스너도 같은 모듈의 `<ctx>/listener/`에 있다.
