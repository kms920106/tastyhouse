<!-- Parent: ../../AGENTS.md -->

# infrastructure:restclient (코어)

벤더 중립 공용 자산만 담는 코어 인프라 모듈(`java-library`). **이 모듈에 있는 것은 설정뿐이다** — Boot `RestClient.Builder`를 꾸미는 요청 팩토리 customizer(`RestClientConfig`·`HttpRequestFactories`). 진입 설정 클래스는 없고, `RestClientConfig`는 앱 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔으로 등록된다. 파일 저장 SPI는 두지 않고(아래 [파일 저장 SPI를 두지 않는 이유](#파일-저장-spi를-두지-않는-이유)), 예외·에러코드도 두지 않는다(아래 [예외·에러코드를 두지 않는 이유](#예외에러코드를-두지-않는-이유)). HTTP 클라이언트는 Spring `RestClient` 하나이며 `WebClient`/webflux는 쓰지 않는다. 즉 이 모듈은 **설정만 갖는 순수 HTTP 코어**다.

## 왜 RestClient를 골랐나

이 모듈을 의존하는 호출부(oauth 벤더 4종·tosspayments·solapi·bbq·admdongkor)는 전부 **동기 호출**이다 — 반응형 합성(체이닝·백프레셔·논블로킹 I/O 활용)이 필요한 곳이 없다. 소비 앱 4개(web-api·admin-api·ceo-api·batch-module)도 전부 서블릿 MVC라 리액티브 스택이 프레임워크 차원에서 맞지 않는다. 그래서 Spring Boot 3.2+가 1급으로 지원하는 동기 클라이언트 `RestClient`(`RestTemplate`은 유지보수 모드)로 통일한다 — webflux 의존·reactor-netty·`.block()` 호출이 생기지 않는다. 포트(`MailSenderPort`·`SmsSenderPort`·`SocialOAuthClientPort` 등)는 프레임워크-프리이므로 어댑터 밖(application·domain)은 클라이언트 종류를 모른다 — 어댑터가 `RestClient`를 알아도 되는 유일한 층이며, 그래서 자체 래퍼 추상화(어댑터가 공용 인터페이스를 통해서만 HTTP를 부르게 하는 것)는 두 번째 구현체가 없어 두지 않는다.

## 외부 연동 모듈 지도

외부 연동은 앱별 실사용에 맞춰 벤더·채널 모듈로 나뉘어 있고, 이 코어는 HTTP를 쓰는 벤더만 의존한다([결정 0016](../../AGENTS.md#decision-0016)).

| 기능 | 모듈 | 문서 |
|---|---|---|
| Firebase Storage 파일 저장 | `infrastructure:firebase` | `../firebase/AGENTS.md` |
| 파일 저장 벤더 선택(`file.provider`) | `infrastructure:file-storage`(코드 없는 스타터)의 `application-file-storage.yml` | `../file-storage/AGENTS.md` |
| S3 (파일 저장) | `infrastructure:aws-s3` | `../aws-s3/AGENTS.md` |
| SES (메일) | `infrastructure:aws-ses` | `../aws-ses/AGENTS.md` |
| SNS (SMS) | `infrastructure:aws-sns` | `../aws-sns/AGENTS.md` |
| 소셜 로그인 | `infrastructure:oauth`(코드 없는 스타터) + `infrastructure:{kakao,naver,apple,facebook}-oauth`(벤더 구현) | `../oauth/AGENTS.md`·`../kakao-oauth/AGENTS.md`·`../naver-oauth/AGENTS.md`·`../apple-oauth/AGENTS.md`·`../facebook-oauth/AGENTS.md` |
| 토스페이먼츠 결제 | `infrastructure:pg`(코드 없는 스타터) + `infrastructure:tosspayments`(벤더 구현) | `../pg/AGENTS.md`·`../tosspayments/AGENTS.md` |
| 메일(JavaMail)·SMS(Solapi) | `infrastructure:{mail,sms}`(코드 없는 스타터) + `infrastructure:{javamail,solapi}`(벤더 구현) | `../mail/AGENTS.md`·`../javamail/AGENTS.md`·`../sms/AGENTS.md`·`../solapi/AGENTS.md` |
| BBQ 메뉴 수집 · 원격 이미지 다운로드 | `infrastructure:bbq` | `../bbq/AGENTS.md` |
| 행정동 경계 GeoJSON 수집 | `infrastructure:admdongkor` | `../admdongkor/AGENTS.md` |

형제 driven 모듈은 `infrastructure:jpa`(`../jpa/AGENTS.md`)·`infrastructure:redis`(`../redis/AGENTS.md`) 등이다. `infrastructure` 아래 24개 모듈의 전체 구성(driven 18 + 조립 6)은 루트 `CLAUDE.md`의 "리포지토리 구조" 절이 정본이다.

## 예외·에러코드를 두지 않는 이유

**이 모듈에는 `exception/` 패키지가 없다.** 외부 연동 실패 코드(`SMS_SEND_NO_RESPONSE`·`SMS_SEND_FAILED`·`SMS_SEND_API_ERROR`·`MAIL_SEND_FAILED`·`ADMIN_DONG_BOUNDARY_FETCH_FAILED`, HTTP 502)는 그 실패를 번역하는 앱 모듈의 에러코드 enum이 소유한다 — `WebErrorCode`(`SMS_SEND_*`·`MAIL_SEND_FAILED`)·`BatchErrorCode`(`ADMIN_DONG_BOUNDARY_FETCH_FAILED`). 벤더 어댑터는 예외를 던지지 않고 결과 record(`SmsSendResult`·`MailSendResult` 등)를 돌려주며, 번역은 `SmsVerificationService`(web-application)·batch-application이 맡는다. 코드 문자열·HTTP 502는 `ErrorCatalogSnapshotTest`가 지킨다([결정 0042](../../AGENTS.md#decision-0042)).

**왜 이 모듈에 두지 않는가**: 이 실패들은 전부 "채널 발송 실패"이고 어느 것도 "HTTP 클라이언트 오류"가 아니다 — SES·SNS SDK 호출과 JavaMail SMTP 연동은 `RestClient`를 쓰지도 않는다. 이 모듈에 예외를 두면 HTTP를 쓰지 않는 벤더까지 예외 하나 때문에 코어를 의존하게 된다. 생성자 위임만 하는 전용 예외 서브클래스도 만들지 않는다 — 타입으로 구분해 catch하는 지점이 없고, 전용 핸들러가 없는 앱에서 의도한 상태 코드 대신 500으로 응답되는 결함을 낳는다. 에러코드 소유 규칙 전문은 [예외·에러코드 소유 규칙](../../CLAUDE.md#예외에러코드-소유-규칙-errorcodespec-공통-계약--businessexception-단일-계층).

## 패키지 구조

```
com.tastyhouse.infrastructure.restclient/
├── RestClientConfig.java        @Bean RestClientCustomizer restClientTimeoutCustomizer — 모든 Boot RestClient.Builder에 요청 팩토리 connect 5s / read 10s 적용
└── HttpRequestFactories.java     public static ClientHttpRequestFactory withTimeouts(Duration connect, Duration read) — SimpleClientHttpRequestFactory(HttpURLConnection) 기반, 이 저장소에서 타임아웃 있는 요청 팩토리를 만드는 유일한 지점
```

**이 모듈에는 `exception/` 패키지도, 진입 설정 클래스도 없다.** 이 모듈이 실린 앱(web·batch — 벤더 경유 전이)의 `ModuleScanConfig`가 `com.tastyhouse.infrastructure`를 스캔해 `RestClientConfig`를 등록한다(등록할 Properties record는 없다). 어느 앱에 어떤 벤더 빈이 뜨는지는 **모듈 경계(= 의존 선언)**가 정한다([결정 0038](../../AGENTS.md#decision-0038)).

### `RestClient.Builder`는 코어가 직접 등록하지 않는다

`RestClient.Builder`는 Boot `RestClientAutoConfiguration`이 **prototype**으로 제공한다 — 이 모듈은 그 빌더에 `RestClientCustomizer`로 요청 팩토리만 얹을 뿐, `RestClient.Builder`/`RestClient` 빈을 자신이 직접 등록하지 않는다. 매번 새 prototype 빌더가 주입되므로 한 어댑터가 `.baseUrl()`을 박아도 다른 어댑터의 빌더가 오염되지 않는다.

### 벤더 패키지를 `external.file` 아래에 두지 않는 이유 (패키지 예외 3건)

벤더 모듈마다 **겹치지 않는 하위 패키지 하나**를 소유한다 — `com.tastyhouse.infrastructure.firebase`(firebase)·`.aws.s3`(aws-s3)·`.aws.ses`(aws-ses)·`.aws.sns`(aws-sns)·`.bbq`(bbq, `RemoteImageDownloader` 포함). 같은 패키지를 두 모듈이 나눠 갖는 split package가 없으므로, 한 모듈의 스캔에 다른 모듈의 빈이 딸려 올라올 수 없다. 공통 상위 패키지(예: 파일 저장 채널 이름) 아래에 벤더 패키지를 두지 않는다([결정 0037](../../AGENTS.md#decision-0037)).

## 파일 저장 SPI를 두지 않는 이유

**벤더 구현이 포트를 직접 구현한다.** 파일 저장의 벤더 무관 계약은 `application`이 소유한 포트 `FileStoragePort`(`com.tastyhouse.application.file.port.out`)이고, `FirebaseFileStorage`(`infrastructure:firebase`)·`S3FileStorage`(`infrastructure:aws-s3`)가 `implements FileStoragePort`다. 다른 driven 어댑터(`MailSenderPort`·`SmsSenderPort`·`PgPaymentGatewayPort`)와 같은 형태다. 둘 다 `@ConditionalOnProperty(file.provider)`로 배타 선택되므로 `FileStoragePort` 빈은 항상 하나이며, 주입받는 쪽은 jpa의 `FileUrlResolver`와 `application-core`의 `FileUploadService`(`@Service`)다([결정 0021](../file-storage/AGENTS.md#decision-0021)).

**이 코어에 전략 인터페이스·위임 어댑터를 두지 않는다.** 포트와 시그니처가 같은 전략 인터페이스를 코어에 두고 위임 어댑터로 잇는 구조는 중간 두 겹이 아무 일도 하지 않는다. 검토한 대안과 비채택 사유:

- **SPI를 `infrastructure:file-storage`로 옮긴다** — file-storage가 firebase를 `runtimeOnly`로 의존하는데 firebase가 SPI를 구현하려면 file-storage를 의존해야 해 **file-storage ↔ firebase 순환**이 된다.
- **SPI 전용 모듈을 신설한다** — 인터페이스 1개(그것도 포트와 동형)를 위해 모듈을 하나 늘리는 것이라 얻는 것이 없다.
- 포트 `FileStoragePort`가 이미 벤더 무관 계약이므로, 벤더 모듈은 그 포트를 소유한 모듈만 의존하면 된다.

**결과.** `infrastructure:firebase`는 `application-core` + firebase-admin만 갖고, 스타터 `infrastructure:file-storage`는 `runtimeOnly project(':infrastructure:firebase')` 한 줄만 조립한다. 그래서 파일 저장만 쓰는 **admin-api·ceo-api의 런타임 클래스패스에는 코어 모듈이 없다.** web은 oauth·pg(경유 tosspayments)·solapi를, batch는 bbq·admdongkor를 경유해 코어를 갖는다. `aws-s3`는 `application-core` + spring-cloud-aws-starter-s3만 가져 코어 의존이 없다.

## Dependencies

### Internal
- **없음.** 이 모듈은 `domain`·`application-core`를 포함해 어떤 내부 모듈도 의존하지 않는다 — 예외 계약이 없어 `ErrorCodeSpec`·`BusinessException`을 참조할 일이 없고, 포트(`FileStoragePort`·소셜 로그인 SPI·BBQ·행정동 경계 포트)는 벤더 모듈이 직접 구현한다.

### External
- `spring-web` (`api`) — `RestClientAutoConfiguration`이 등록하는 `RestClient.Builder`를 코어의 `RestClientCustomizer`가 꾸민다. 벤더 어댑터가 이 좌표를 반복 선언하지 않도록 `api`로 노출한다.
- `spring-boot-starter-json` (`api`) — Jackson. 벤더 어댑터가 wire DTO 역직렬화에 그대로 쓴다.
- **webflux·reactor-netty·`WebClient`는 없다.** **AWS SDK·Firebase Admin·jjwt·`spring-boot-starter-mail` 의존도 없다** — 각각 aws-s3/aws-ses/aws-sns·firebase·apple-oauth·javamail 모듈이 소유한다.

## 어댑터 작성 규칙 (10모듈 공통)

이 절은 코어뿐 아니라 `infrastructure:{firebase,aws-s3,aws-ses,aws-sns,kakao-oauth,naver-oauth,apple-oauth,facebook-oauth,pg,tosspayments,mail,javamail,sms,solapi,bbq,admdongkor}` 전부에 적용된다(코드 없는 스타터 `file-storage`·`oauth`·`pg`·`mail`·`sms`는 작성할 어댑터가 없다).

- **외부 HTTP 호출은 코어가 customizer로 꾸민 Boot `RestClient.Builder`를 주입받아 생성자에서 한 번 build한다.** `restClientBuilder.baseUrl(...).build()`로 만든 `RestClient`를 필드로 보유한다(스레드 안전). WebClient·webflux·JDK `java.net.http.HttpClient` 직접 사용은 도입하지 않는다 — 타임아웃 있는 요청 팩토리를 만드는 유일한 지점은 코어의 `HttpRequestFactories.withTimeouts(...)`(`SimpleClientHttpRequestFactory`/`HttpURLConnection` 기반)다. **대용량 응답은 `exchange()` 스트리밍이다**(`retrieve().body(String/...)` 금지 — 선례: admdongkor의 `AdminDongBoundaryClient`).
- **포트를 구현하되 프레임워크 타입을 시그니처로 누출하지 않는다**: 포트(`MailSenderPort`·`SmsSenderPort`·`FileStoragePort`·`PgPaymentGatewayPort`)는 프레임워크-프리이므로 `RestClient`·SDK 타입·wire DTO가 포트 시그니처에 등장하면 안 된다. 변환은 어댑터 안에서 끝낸다.
- **외부 응답 DTO는 도메인 타입을 보유하지 않는다 (역방향 누수 금지)**: 상세는 `../oauth/AGENTS.md`, DTO별 봉인은 `../kakao-oauth/AGENTS.md`·`../naver-oauth/AGENTS.md`.
- **자격증명은 코드에 하드코딩하지 않는다**: 환경변수(`.env`)·configtree 시크릿(`SECRETS_DIR`, `../firebase/AGENTS.md`)으로 주입한다.
- **provider 선택은 `@ConditionalOnProperty`로 한다**: `file.provider`·`mail.provider`·`sms.provider`. 조건 애노테이션은 스캔되는 구현 클래스에 붙어 있고, `{Xxx}ModuleConfig`는 조건을 갖지 않는다.
- **어댑터는 예외를 던지지 않고 결과 record를 돌려주며, application이 `ApplicationException`으로 번역한다.** 모듈마다 예외 타입을 새로 만들거나 전역 핸들러에 `@ExceptionHandler`를 추가하지 않는다. `RestClientResponseException`은 결과 record를 돌려주는 어댑터(`solapi`·`tosspayments`)가 안에서 잡아 실패 결과로 담는다. `bbq`의 `BbqApiClient`는 예외다 — 로깅한 뒤 `RestClientResponseException`을 그대로 다시 던지고(`BbqApiClientTest`가 전파를 검증), 포트 구현체 `BbqMenuAdapter`가 이 클라이언트를 감싼다.
- **전역 타임아웃이 안 맞는 어댑터만 per-client override한다**: `requestFactory(HttpRequestFactories.withTimeouts(connect, read))`로 개별 지정하되, **`MockRestServiceServer.bindTo(builder)` 목 팩토리를 덮어써 단위 테스트가 불가능해지므로** 전역값과 실제로 다를 때만 쓴다(BBQ가 `timeoutSeconds` 프로퍼티를 두지 않고 override도 하지 않는 이유).

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar. 스타터를 포함한 외부 연동 모듈 전부 같다.
- **이 모듈은 설정만 갖는다 — 예외·에러코드를 두지 않는다.** 외부 연동 실패 코드는 그 실패를 번역하는 application 모듈의 에러코드 enum이 소유한다(SMS·메일 → `WebErrorCode`, 행정동 경계 → `BatchErrorCode`). 새 채널 연동을 추가할 때 이 모듈에 예외 타입을 만들지 않는다(위 [예외·에러코드를 두지 않는 이유](#예외에러코드를-두지-않는-이유)).
- **RestTemplate·WebClient 모듈을 미리 만들어 두지 않는다.** `infrastructure:aws-s3`/`aws-ses`/`aws-sns`가 "만들어 뒀지만 어느 앱도 안 쓰는" 선례이긴 하나, 그것들은 포트 뒤에서 설정 한 줄로 교체 가능한 구현체다. HTTP 클라이언트는 어댑터 코드가 직접 호출하는 라이브러리라 교체 = 어댑터 재작성이며, 미리 만들면 webflux·reactor-netty가 빌드 그래프로 들어온다. WebClient가 필요해지면 그때 `infrastructure:webclient`를 신설한다(1순위는 virtual threads + `RestClient`, 불가피하면 그 모듈 + 가드 예외만 도입).
- **이 모듈의 main 클래스는 전부 `com.tastyhouse.infrastructure.restclient` 아래에 둔다.** 루트 밖으로 클래스를 두면 `backend/infrastructure/restclient/src/test/java/com/tastyhouse/infrastructure/restclient/architecture/PackageRootTest.java` → `shouldResideInModuleRootPackage`이 실패한다.
- **빈 배선**: 이 모듈의 빈(`RestClientConfig`)은 클래스패스에 실리기만 하면 앱 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔으로 등록된다. **앱은 이 모듈을 직접 선언하지 않는다** — web은 oauth(경유 kakao/naver/apple/facebook-oauth)·pg(경유 tosspayments)·solapi를, batch는 bbq·admdongkor를 경유해 전이로 받는다. 스타터 `infrastructure:file-storage`는 이 모듈을 조립하지 않으므로(firebase 한 줄), admin-api·ceo-api의 `runtimeClasspath`에는 이 모듈이 없다. 파일 저장의 기동 실패 조건도 이 모듈과 무관하다 — `FileStoragePort` 구현이 없으면 jpa의 `FileUrlResolver`와 `application-core`의 `FileUploadService`가 `FileStoragePort` 빈을 찾지 못해 **기동 시** 실패한다(`../file-storage/AGENTS.md`).
- **하위 문서**: 코어 패키지 설명은 `src/main/java/com/tastyhouse/infrastructure/restclient/AGENTS.md`.
- **가드 테스트**: `NoReactiveHttpClientTest`와 `PackageRootTest`는 `src/test/java/com/tastyhouse/infrastructure/restclient/architecture/`에 있다(ArchUnit `testImplementation`). `NoReactiveHttpClientTest`는 가드 자신을 예외로 지정하는 `GUARD_SOURCE` 경로 문자열을 갖는다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 루트는 `com.tastyhouse.infrastructure.{모듈}`이다

**대상**: `backend/infrastructure/restclient/src/test/java/com/tastyhouse/infrastructure/restclient/architecture/PackageRootTest.java` → `shouldResideInModuleRootPackage` · 벤더 13모듈의 `architecture/VendorLayerRulesTest` → `shouldResideInModuleRootPackage`

**코드가 있는 infrastructure 모듈(이 모듈 + 벤더 13 + jpa·redis·mybatis)의 main 클래스는 전부 `com.tastyhouse.infrastructure.{모듈명의 하이픈을 점으로}` 아래에 둔다.** 모듈마다 아키텍처 테스트의 `shouldResideInModuleRootPackage`(이 모듈·redis·mybatis는 `PackageRootTest`, 벤더는 `VendorLayerRulesTest`, jpa는 `LayerRulesTest`)가 강제한다. `com.tastyhouse.external..`·`com.tastyhouse.restclient..` 같은 다른 루트에 두지 않는다([결정 0037](../../AGENTS.md#decision-0037)).

### `@ConfigurationProperties` record는 명시 등록한다

**대상**: 이 모듈의 루트 패키지 `com.tastyhouse.infrastructure.restclient` — 지금은 Properties record도 설정 클래스도 없다

`@ConfigurationPropertiesScan`을 쓰지 않는 것이 이 저장소의 방침이다. 이 모듈에 Properties record를 추가한다면 컴포넌트 스캔에 맡기지 말고, 루트 패키지에 `RestClientModuleConfig`(`@Configuration(proxyBeanMethods = false)`, 스캔 없음)를 새로 두고 그 `@EnableConfigurationProperties`에 명시한다.

### `RestClient.Builder`/`RestClient` 빈을 직접 등록하지 않는다

**대상**: `backend/infrastructure/restclient/src/main/java/com/tastyhouse/infrastructure/restclient/RestClientConfig.java`

Boot가 제공하는 prototype 빌더 + `RestClientCustomizer`만 쓴다. 싱글톤 빌더를 직접 등록하면 한 어댑터가 `.baseUrl()` 등으로 공유 빌더를 바꿔 다른 어댑터까지 오염시키는 유형의 버그가 생길 수 있다.

### `java.net.http.HttpClient`는 쓰지 않는다 — 요청 팩토리는 `HttpRequestFactories.withTimeouts`(Simple)만

**대상**: `backend/infrastructure/restclient/src/main/java/com/tastyhouse/infrastructure/restclient/HttpRequestFactories.java`

이 저장소에서 `java.net.http.*`(JDK `HttpClient`) 사용은 **0곳**이다. 타임아웃 있는 요청 팩토리가 필요한 모든 지점(코어의 전역 customizer, admdongkor·bbq의 per-client override)은 `HttpRequestFactories.withTimeouts(connect, read)`(`SimpleClientHttpRequestFactory`/`HttpURLConnection` 기반) 하나만 쓴다.

**JDK 팩토리를 쓰지 않는다 — Spring 6.1.5의 JDK 팩토리는 본문 read timeout을 적용하지 않는다.** `JdkClientHttpRequestFactory`의 타임아웃은 `HttpRequest.timeout()`/`sendAsync().get(timeout)`으로 구현되어 **응답 헤더가 도착할 때까지만** 제한하고, 헤더 이후 본문 스트리밍이 멎어도(응답이 시작됐지만 끝나지 않는 "본문 정체") 무기한 대기한다. `SimpleClientHttpRequestFactory`는 소켓 `SO_TIMEOUT`으로 구현되어 본문 read 한 번 한 번에 적용되므로(스트리밍 `exchange()`에도 동일하게 적용) 이 문제가 없다. 부수효과로 JDK `HttpClient`의 HTTP/2 협상 관련 위험도 함께 피한다. **Spring 6.2.x 이후 이 제약이 실제로 개선됐는지 확인되기 전에는 JDK 팩토리를 쓰지 않는다.**

### WebClient·webflux 재도입 금지

**대상**: `backend/infrastructure/restclient/src/test/java/com/tastyhouse/infrastructure/restclient/architecture/NoReactiveHttpClientTest.java`

`org.springframework.web.reactive`·`reactor.`·`java.net.http.` **토큰**을 소스 전체(FQN 직접 사용·static import 우회 포함)에서 검사하고, `*/build.gradle`의 `spring-boot-starter-webflux`를 전 `infrastructure/*` 모듈에서 금지한다. 예외는 가드 자신의 파일 1개(경로 전체로 지정)뿐이다 — import 줄 접두어만 검사하면 FQN 직접 사용이나 static import로 우회할 수 있어 토큰으로 검사한다. 필요해지면 1순위 대안은 Java 21 virtual threads + `RestClient`이고, 그래도 불가피하면 그 모듈만 예외로 도입하되 이 가드의 예외 목록과 그 모듈 AGENTS.md에 근거를 남긴다.

### per-client `requestFactory` override는 전역값과 실제로 다를 때만

**대상**: 벤더 어댑터의 `requestFactory(HttpRequestFactories.withTimeouts(...))` 호출부(예: `admdongkor`의 `AdminDongBoundaryClient`, `bbq`의 `RemoteImageDownloader`)

override는 `MockRestServiceServer.bindTo(builder)`가 심어 둔 목 팩토리를 덮어써 단위 테스트를 불가능하게 만든다. BBQ가 `BbqProperties`에 타임아웃 프로퍼티를 두지 않고 override도 하지 않는 것이 이 근거다 — 전역 customizer 값(connect 5s / read 10s)과 실제로 달라야 할 이유가 없으면 override하지 않는다.

### 가드 테스트는 test 태스크 inputs에 infrastructure 소스를 선언해야 재실행된다

**대상**: `backend/infrastructure/restclient/build.gradle` → `test` 태스크의 `inputs.files(fileTree(...))` 선언

`NoReactiveHttpClientTest`는 이 모듈이 아니라 `backend/infrastructure/*/src/**/*.java`와 `*/build.gradle`을 읽는다. Gradle에 그 파일들을 입력으로 선언하지 않으면 그 파일들이 바뀌어도 이 모듈의 `test` 태스크가 UP-TO-DATE로 건너뛰어 회귀를 못 잡는 것을 실측으로 확인했다.

### 최상위 클래스에 `public`을 붙이지 않는다 (package-private 적용, 허용 목록 1개)

**대상**: `backend/infrastructure/restclient/src/test/java/com/tastyhouse/infrastructure/restclient/architecture/PackageRootTest.java` → `topLevelClassesShouldNotBePublic` · `publicByNecessityShouldStillBePublic` · 상수 `PUBLIC_BY_NECESSITY`

이 모듈의 최상위 타입은 허용 목록 하나를 빼고 전부 package-private이다. 앱 `ModuleScanConfig`의 문자열 스캔으로만 조립되므로 타입 이름이 모듈 밖에 나타날 필요가 없다.

| 허용 목록(`PUBLIC_BY_NECESSITY`) | 이유 |
|---|---|
| `com.tastyhouse.infrastructure.restclient.HttpRequestFactories` | `infrastructure:bbq`·`infrastructure:admdongkor`가 다른 모듈에서 per-client 타임아웃용으로 호출한다 |

`publicByNecessityShouldStillBePublic`은 허용 목록이 낡지 않았는지(그 FQN이 아직 public인지) 검사한다. 새로 public이 필요해지면 FQN을 이 상수에 추가하고 이 표에 이유를 적는다. 근거와 전체 범주는 `backend/CLAUDE.md`의 "접근 제어자 규칙 (내부 구현은 package-private)" 절.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 코어와 형제 모듈의 빈 등록

**대상**: 이 모듈과 형제 외부 연동 모듈의 루트 패키지 · 앱 부트스트랩의 `ModuleScanConfig`

이 모듈과 형제 모듈의 빈은 **앱 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 문자열 스캔**이 등록한다. 앱은 이 모듈을 `@Import`하지도, 직접 선언하지도 않는다(어댑터 모듈을 통해 전이로 받는다). 형제 모듈 중 Properties가 있는 것만 `{Xxx}ModuleConfig`(`FirebaseModuleConfig`·`AwsS3ModuleConfig`·`{Kakao,Naver,Apple,Facebook}OAuthModuleConfig`·`TossPaymentsModuleConfig`·`SolapiModuleConfig`·`BbqModuleConfig`·`AdmdongkorModuleConfig`)를 둔다. 앱은 실제로 쓰는 모듈만 의존하므로 클래스패스에 실린 것만 스캔된다.

### `RestClient.Builder`를 코어가 customizer로 꾸미는 이유와 버퍼 한도

**대상**: `backend/infrastructure/restclient/src/main/java/com/tastyhouse/infrastructure/restclient/RestClientConfig.java`

동기 HTTP 클라이언트(타임아웃 포함)를 코어가 customizer로 꾸민다. OAuth·결제 등 외부 연동 클라이언트가 Boot `RestClient.Builder`를 주입받아 build하므로, 이 설정을 코어에 두어 코어를 받는 모든 앱에서 customizer가 적용되게 한다. **`RestClient`는 WebClient의 `maxInMemorySize` 같은 버퍼 상한이 없다** — 대신 대용량 응답을 다루는 어댑터는 `retrieve().body(...)`가 아니라 `exchange()` 스트리밍 + `BoundedInputStream`으로 힙 사용을 직접 제어한다(선례: admdongkor 모듈의 `AdminDongBoundaryClient`).
