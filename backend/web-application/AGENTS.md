# web-application

**web(사용자 모바일 웹, `web-api`) 하나만 쓰는 application 계층을 담는 앱 모듈.** 앱 마커 제거로 신설됐다. 과거에는 이 코드가 `application` 한 모듈 안에서 `@WebApp` 마커로 구분됐으나, 지금은 **이 모듈에 있다는 사실 자체가 "web 소속"** 이다. 5모듈 공통 규칙의 정본은 `backend/application/AGENTS.md`(코어)이고, 모듈 지도와 배치 기준은 `backend/CLAUDE.md`의 "앱 모듈 경계 규칙" 절이다. 이 문서는 요약만 둔다.

## 무엇이 여기 사는가

- web 전용 `@Service`/`@Component` 빈 — `*CommandService`/`*QueryService`(오케스트레이터)와 web만 쓰는 도메인 서비스(예: `MailVerificationService`·`SmsVerificationService`·`PgPaymentGatewayRouter`·`PaymentCancellationService`)
- UseCase 인터페이스와 Command record(`<ctx>/port/in/`)
- **web 전용 SPI 포트**(구현 벤더가 web에만 조립되는 포트):
  - `mail.port.out.{MailSender,MailSendResult}` — 구현 `infrastructure:javamail`·`aws-ses`
  - `sms.port.out.{SmsSender,SmsSendResult,SmsSendFailure}` — 구현 `infrastructure:solapi`·`aws-sns`
  - `payment.port.out.{PgProviderGateway,PgPaymentGateway,PgCancelResult,PgProviderCode}` — 구현 `infrastructure:tosspayments`
  - `auth.port.out.{SocialOAuthClient,SocialAuthorization,SocialCredential,SocialOAuthResult,SocialOAuthFailure,SocialProfile,SocialProvider}` — 구현 `infrastructure:{kakao,naver,apple,facebook}-oauth`
- 자바 패키지는 코어와 같은 `com.tastyhouse.application.<ctx>..`다(split package). 클래스를 코어와 이 모듈 사이로 옮겨도 import는 바뀌지 않는다.

## 의존

- `api project(':application')` — 코어(공유 도메인 서비스·`port.out` 계약·리스너·`shared/**`)를 이 모듈의 소비자에게 함께 노출한다.
- `implementation project(':domain')`, `implementation project(':security-core')`, 그리고 `spring-security-core`·`spring-web`·`jackson-databind`·`spring-tx`.
- **이 모듈을 의존하는 쪽**: `web-api`(`implementation`), 그리고 위 SPI 포트를 구현하는 벤더 모듈 `infrastructure:{kakao,naver,apple,facebook}-oauth`·`tosspayments`·`javamail`·`solapi`·`aws-ses`·`aws-sns`.
- **다른 앱 모듈(`admin`·`ceo`·`batch-application`)을 의존하지 않는다.** 앱 간 수평 의존을 컴파일러가 막는 것이 이 모듈이 존재하는 이유다.

## 규칙

- **`@Configuration`과 도메인 이벤트 리스너(`@TransactionalEventListener`)를 여기 두지 않는다** — 코어에만 둔다(`backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `listenersAndConfigsShouldResideInCore`). 리스너가 앱 모듈에 있으면 다른 앱이 같은 이벤트를 발행할 때 후속 처리가 조용히 사라진다.
- **빈이면 `@Service`/`@Component`를 단다.** 앱 마커는 없다.
- **이 모듈 전용 에러코드는 `WebErrorCode`(`backend/web-application/src/main/java/com/tastyhouse/application/shared/exception/WebErrorCode.java`)에 둔다; 두 번째 앱이 쓰면 코어 `ApplicationErrorCode`로 올린다.** 예외는 `ApplicationException(WebErrorCode.X)`로 던진다. 규칙 정본은 `backend/application/AGENTS.md`의 봉인·가드 목록 "에러 카탈로그 가드" 항목이다.
- **두 번째 앱이 쓰게 되면 코어로 옮긴다.** 이 모듈에는 web 하나만 쓰는 것만 둔다.
- **web 전용 SPI 포트를 코어로 옮기지 않는다.** 코어로 가면 admin·ceo·batch의 코어 빈이 그 포트를 주입해도 컴파일이 통과하고, 구현이 없는 앱에서 기동 시점에야 실패한다. 여기 있으면 코어가 import하는 순간 컴파일 에러다.
- **이 모듈에는 앱 소속 표식 리소스가 없다.** `web-api`의 스캔(`ApplicationLayerScanConfig`)은 필터 없이 `com.tastyhouse.application`을 훑으므로, 클래스패스에 다른 앱 모듈이 섞이면 그 앱의 빈이 전부 뜬다("클래스패스 존재 = 활성화"). `web-api`의 `ApplicationModuleClasspathTest`가 `com.tastyhouse.application` 클래스의 출처 모듈 집합이 `{application, web-application}`인지를 `ModuleOrigin`으로 판정해 그 사고를 막는다. 과거에는 `src/main/resources/META-INF/tastyhouse/application-module.properties`(`app=web`) 표식으로 판정했으나 삭제했다.
- 아키텍처 테스트(ArchUnit)는 이 모듈이 아니라 코어 `application`의 테스트에 있다. 코어가 `testImplementation project(':web-application')`로 이 모듈을 본다.
- 공유 테스트 더블은 코어 testFixtures의 `com.tastyhouse.testsupport.<ctx>..`에 있다(`testImplementation(testFixtures(project(':application')))`).
- **UseCase 구현 서비스·리스너는 package-private이다**(`public` 금지). 가드는 코어 `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`의 `useCaseImplementationsShouldNotBePublic`·`listenersAndConfigsShouldNotBePublic`이고, 정본은 `backend/application/AGENTS.md`의 봉인·가드 목록 "UseCase 구현 서비스·리스너·설정에 `public`을 붙이지 않는다" 항목이다.
