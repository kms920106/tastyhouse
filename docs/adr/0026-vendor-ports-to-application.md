# ADR-0026: 벤더 16모듈의 domain 의존을 끊고 벤더 포트·서비스를 application으로, 등록은 application config(PgRouterConfig·Mail/SmsServiceConfig)로 옮긴다(덩어리 02)

- 상태: 대체됨(→ [ADR-0034](0034-service-config-removal.md))
- 결정 커밋: `d98b46c0c` (2026-09-27)
- 대체한 결정: —

## 맥락

- **벤더 모듈**: 외부 서비스 구현을 담는 infrastructure 모듈(`firebase`·`javamail`·`solapi`·`tosspayments`·`*-oauth`·`aws-*`·`bbq`·`admdongkor` 등).
- **채널 모듈**: 벤더를 `runtimeOnly`로 묶어 앱에 노출하는 조립 모듈(`pg`·`mail`·`sms`).
- **출력 포트(아웃바운드 포트)**: 바깥 시스템 호출을 안쪽 계층이 인터페이스로 선언한 것. 벤더가 구현한다.

엄격 레이어드 전환(덩어리 01~03)의 두 번째 단계다. 파일·메일·SMS·결제 포트는 domain이 소유했고, 벤더 모듈은 그 포트를 구현하려고 domain을 의존했다. 채널 모듈 `pg`·`mail`·`sms`는 라우터·도메인 서비스를 등록하는 코드를 갖고 있었다.

## 이전 규칙

### CLAUDE.md — 리포지토리 구조

- 조립 모듈은 "채널(등록 코드 있음) vs 스타터(코드 없음)"로 나뉘었다. `pg`·`mail`·`sms` 세 채널 모듈은 `PgGatewayConfig`·`MailDomainConfig`·`SmsDomainConfig`를 갖고 있었고, `mail`·`sms`는 "`file-storage`와 달리 코드가 있다"고 서술했다.

### backend/CLAUDE.md — 모듈 지도 / 모듈 등록 컨벤션

- 모듈 지도는 조립 6개를 "스타터 3 + 채널 3"으로 그렸다. 채널은 `:pg`(PgGatewayConfig 라우터 + tosspayments 조립), `:mail`(MailDomainConfig + javamail 조립), `:sms`(SmsDomainConfig + solapi 조립)였다.
- 채널 3개는 `PgModuleAutoConfiguration`·`MailModuleAutoConfiguration`·`SmsModuleAutoConfiguration`으로 자기 패키지(`com.tastyhouse.external.{pg,mail,sms}`)를 스캔했다. 포트 구현이 아니라 라우터·도메인 서비스 빈을 등록했다.
- 파일 저장의 벤더 무관 계약은 `com.tastyhouse.domain.file.port.FileStoragePort`였다.

### backend/CLAUDE.md — 공존형 채널 — PG 라우터 / 포트 구현이 일부 앱에만 있을 때의 등록

- `PgPaymentGatewayRouter`는 domain의 순수 POJO였고, 빈 등록은 채널 모듈 `infrastructure:pg`의 `config/PgGatewayConfig`가 맡았다.
- `MailDomainConfig`·`SmsDomainConfig`는 `infrastructure:persistence`에서 `infrastructure:messaging`을 거쳐(4분할, 2026-09-26) 채널 모듈 `infrastructure:mail`·`infrastructure:sms`로 옮겨져 있었다. "채널 모듈이 벤더를 조립하는 쪽이라 DomainConfig를 갖는다"는 것이 당시 규칙이었다.

### backend/CLAUDE.md — 인증코드 발송은 발급과 원자적으로 수행하는 규칙

- `MailVerificationService`/`SmsVerificationService`는 domain의 순수 POJO 도메인 서비스였다.

### backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md — Common Patterns

- domain이 벤더 무관 계약(`FileStoragePort`·`MailSender`·`SmsSender`·`PgPaymentGateway`/`PgProviderGateway`)을 직접 선언하고, 외부 연동 모듈이 그것을 구현했다.

### backend/infrastructure/pg/AGENTS.md — 무엇이 바뀌었는가

- `PgModuleAutoConfiguration`(`@AutoConfiguration` + `@ComponentScan("com.tastyhouse.external.pg")`), `config/PgGatewayConfig`(`@Bean PgPaymentGatewayRouter(List<PgProviderGatewayPort>)`), 자기 등록 imports 파일이 있었다. `build.gradle`은 `implementation project(':domain')` + `runtimeOnly tosspayments`였다.
- `PgPaymentGatewayRouter`는 `domain/payment/service/`의 순수 POJO였고 `PgGatewayConfig`가 `@Bean`으로 등록했다. 벤더의 `provider()`는 도메인 `PgProvider`를 반환했다.
- 문서는 이 모듈을 "DomainConfig 대신 라우터를 등록하는 공존형 채널 모듈"로 구분했고, "채널 모듈이 벤더를 조립하는 쪽이라 라우터를 등록한다"가 규칙이었다.

### backend/infrastructure/mail/AGENTS.md · backend/infrastructure/sms/AGENTS.md — 무엇이 바뀌었는가

- `mail`은 `MailModuleAutoConfiguration`(`@AutoConfiguration` + `@ComponentScan("com.tastyhouse.external.mail")`), `config/MailDomainConfig`(`MailVerificationService` `@Bean` 등록, `com.tastyhouse.external.mail.config` 소속), 자기 등록 imports 파일을 가졌고 `build.gradle`은 `implementation project(':domain')` + `runtimeOnly javamail`이었다.
- `sms`도 같은 형태로 `SmsModuleAutoConfiguration`(`@ComponentScan("com.tastyhouse.external.sms")`), `config/SmsDomainConfig`(`SmsVerificationService` 등록, `com.tastyhouse.external.sms.config` 소속), imports 파일, `implementation project(':domain')` + `runtimeOnly solapi`였다.
- 문서는 "채널 모듈은 DomainConfig를 갖고 스타터(`file-storage`·`oauth`)는 코드가 없다"고 구분했다.

### backend/infrastructure/file-storage/AGENTS.md — ⚠️ 이 모듈에 코드를 넣지 않는다

- 메일·SMS 채널 모듈은 이 모듈과 다른 개념으로 서술됐다. 그쪽은 포트 구현이 web에만 있어 도메인 서비스 빈을 persistence에 둘 수 없는 채널이었고, 파일 저장은 `FileDomainConfig`가 persistence에 남아 있어 이 모듈에 둘 코드가 없다는 것이 근거였다.

### backend/AGENTS.md — Module Dependency Graph

- `firebase`·`aws-s3`(`FileStoragePort`), `aws-ses`·`aws-sns`, pg/mail/sms 벤더는 domain 포트를 구현하려고 `domain`을 의존했다.
- 애플·페이스북 로그인 벤더는 실패를 domain 쪽 예외로 표현해 `domain`을 의존했다(카카오·네이버는 애초에 domain 의존이 없었다).
- `bbq`·`admdongkor`는 `RemoteImagePort`/`AdminDongBoundaryPort` 구현에서 domain의 `GeoRing`/`GeoPoint` 등 도메인 타입을 알아야 했다.

## 결정

| 항목 | before | after |
|---|---|---|
| 파일·메일·SMS·PG 포트 | `domain.file.port.FileStoragePort`, `domain.mail.port.MailSender`, `domain.sms.port.SmsSender`, `domain.payment.port.{PgPaymentGatewayPort,PgProviderGatewayPort}` | `application/<ctx>/port/out`(`FileStoragePort`·`MailSenderPort`·`SmsSenderPort`·`PgPaymentGatewayPort`·`PgProviderGatewayPort` + 결과 record) |
| 그 포트를 쓰는 서비스 | domain의 `FileUploadService`·`MailVerificationService`·`SmsVerificationService`·`PaymentConfirmationService`·`PgPaymentGatewayRouter` | application `<ctx>/service/`의 마커 없는 POJO |
| 등록 | persistence `FileDomainConfig`·`PaymentDomainConfig`(일부), 채널 모듈 `MailDomainConfig`·`SmsDomainConfig`·`PgGatewayConfig` | application 마커 설정: `FileServiceConfig`·`PaymentServiceConfig`(`@SharedApp`), `MailServiceConfig`·`SmsServiceConfig`·`PgRouterConfig`(`@WebApp`) |
| 채널 모듈 `pg`·`mail`·`sms` | 설정·자동구성 코드 있음 | 코드 없는 조립 스타터(`runtimeOnly` 벤더 한 줄). 조립 모듈 전부가 코드 없는 스타터가 됨 |
| 벤더 실패 표현 | 벤더가 `BusinessException`을 던짐 | 결과 record(`MailSendResult`·`SmsSendResult`·`FileDeleteResult`·`SocialOAuthResult`·`ImageDownloadResult`·`AdminDongBoundaryFetchResult`)를 돌려주고 application이 같은 트랜잭션 안에서 기존과 같은 에러코드로 번역 |
| 벤더 쪽 PG 식별자 | domain `PgProvider` | application 신설 enum `PgProviderCode`. 라우터가 `name()`으로 `PgProvider`와 연결 |
| 행정동 경계 | 어댑터가 domain `GeoPoint`/`GeoRing`을 조립 | 어댑터는 원시 좌표(`BoundaryRing`·`BoundaryCoordinate`)만, 중심점 계산은 `AdminDongSchedulerService` |
| ArchUnit | — | 벤더 13모듈 `VendorLayerRulesTest`(domain 의존 금지), `AppIsolationTest#sharedBeansShouldNotDependOnWebOnlyServices`, `EnumCodeConstantsTest` 신설, domain `ContextBoundaryTest` 봉인 목록 2건 삭제(16 → 14) |

## 근거

- 서비스까지 옮긴 이유: 네 서비스는 이관된 포트를 생성자로 주입받는다. 포트만 application으로 옮기고 서비스를 domain에 남기면 domain이 application의 포트 인터페이스를 참조해야 해 의존 방향이 뒤집힌다. "아웃바운드 포트가 domain 밖에 있으면 그 포트를 쓰는 오케스트레이션도 domain 밖에 있어야 한다"는 조건이 기존 배치 기준보다 우선한다.
- 소셜 로그인 SPI(`auth.port.out`)·BBQ 크롤링 포트가 이미 application에 있던 형태를 확장한 것이다. "계약은 유스케이스 계층이 갖고 외부 연동 모듈이 구현한다."
- `PgProviderCode`·결과 record·원시 좌표 타입은 "어댑터는 벤더 무관 원시 타입만, 판단은 유스케이스 계층"이라는 원칙을 반복 적용한 것이다. 벤더 모듈이 domain 없이 포트를 구현할 수 있게 한다.
- 등록 범위는 포트 구현이 있는 앱으로 한정한다. 발송 포트 구현이 web에만 있으므로 `mail`·`sms`·PG 라우터는 `@WebApp`, `FileStoragePort`는 4앱 전부가 구현을 가지므로 `@SharedApp`이다. 조건은 "외부 연동 포트인가"가 아니라 "구현이 일부 앱에만 있는가"다.
- 벤더 모듈에 등록을 두지 않는 이유는 그대로다. 벤더마다 같은 등록이 중복된다.
- `@ConditionalOnBean(MailSenderPort.class)` 대안은 채택하지 않았다. 사용자 `@Configuration` 사이의 등록 순서에 결과가 좌우되어 조용히 빈이 빠질 수 있다.

## 결과·감수한 손실

- 응답 code·HTTP 상태·DB 스키마는 바뀌지 않았다(동작 변경 없음).
- 채널 모듈 3개의 등록 코드가 사라지면서 "채널(등록 코드 있음) vs 스타터(코드 없음)" 구분이 소멸했다. 남은 차이는 배타 선택 방식뿐이다. `pg`·`oauth`는 벤더가 공존하고, `mail`·`sms`·`file-storage`는 `provider` 키로 벤더 하나만 켠다.
- `PgProviderCode`와 `PgProvider`는 상수명·순서가 항상 같아야 한다. 어긋나면 라우터의 `PgProvider.valueOf(code.name())`이 런타임에 `IllegalArgumentException`을 내므로 `EnumCodeConstantsTest#pgProviderCodeMatchesPgProvider`가 막는다.
- 대체: application 마커 설정 클래스로 등록하는 방식은 [ADR-0034](0034-service-config-removal.md)가 지웠고(서비스 클래스에 마커만), 이어 [ADR-0036](0036-app-marker-removal.md)이 마커를 없애 세 web 전용 서비스를 `web-application`의 `@Service`로 옮겼다. 포트를 application이 소유하고 채널 모듈이 코드 없는 스타터라는 부분은 현재도 유효하다.

## 영향받은 문서

- `CLAUDE.md` · "리포지토리 구조" · 조립 모듈 6개 모두 자바 코드가 없는 조립 스타터이고, 메일·SMS 인증 서비스와 PG 라우터는 `web-application`의 `@Service`로 등록된다.
- `backend/CLAUDE.md` · "벤더 선택은 앱이 아니라 스타터 모듈이 한다 (챕터 03)" · 스타터는 코드가 없고 벤더 무관 계약은 application이 소유한 `FileStoragePort`이다.
- `backend/CLAUDE.md` · "공존형 채널 — PG 라우터 (`infrastructure:pg`)" · 라우터는 `web-application`에 있고 `List<PgProviderGatewayPort>`를 받아 `PgProviderCode`를 `PgProvider`로 변환한다.
- `backend/CLAUDE.md` · "인증코드 발송은 발급과 원자적으로 수행하는 규칙 (도메인 서비스에 Sender 포트 주입)" · 두 인증 서비스는 application 계층(`web-application`)에 있다.
- `backend/application-core/AGENTS.md` · "덩어리 02/03a — 벤더 포트·서비스 4종을 `domain`에서 이관" · 이관 목록과 서비스까지 옮긴 이유.
- `backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md` · "Common Patterns" · 파일·메일·SMS·결제 출력 포트는 domain에 없다.
- `backend/infrastructure/pg/AGENTS.md` · "라우터 등록은 어디인가 — `web-application`의 `PgPaymentGatewayRouter`" / "이 모듈에 코드를 넣지 않는다" · 이 모듈은 조립과 설정만 한다.
- `backend/infrastructure/mail/AGENTS.md`·`backend/infrastructure/sms/AGENTS.md` · "도메인 서비스 등록은 어디인가 — `web-application`의 `MailVerificationService`" / "이 모듈에 코드를 넣지 않는다" · 등록은 `web-application`이 한다.
- `backend/infrastructure/file-storage/AGENTS.md` · "⚠️ 이 모듈에 코드를 넣지 않는다" · `FileDomainConfig`·`MailDomainConfig`·`SmsDomainConfig`는 없다.
