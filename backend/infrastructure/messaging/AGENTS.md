<!-- Parent: ../../AGENTS.md -->

# infrastructure:messaging

메일·SMS **발송 채널**을 소유하는 어댑터 모듈(`java-library`). `infrastructure:external` 7모듈 분리(챕터 01) 산물이며, 기본 구현은 메일 = JavaMail(SMTP), SMS = Solapi다. **AWS 구현(SES·SNS)은 이 모듈이 아니라 `infrastructure:aws`에 있다**(벤더 단위로 모아 스캔을 격리하기 위함 — `../aws/AGENTS.md`).

## 무엇을 소유하는가

```
com.tastyhouse.external/
├── mail/
│   ├── MailProperties.java            mail.* (provider·sender-address)
│   └── javamail/JavaMailAdapter.java  MailSender 구현 @ConditionalOnProperty(mail.provider=javamail, matchIfMissing=true)
├── sms/
│   ├── SmsProperties.java             sms.* (provider·sender-number)
│   └── solapi/
│       ├── SolapiSmsClient.java       SmsSender 구현 @ConditionalOnProperty(sms.provider=solapi, matchIfMissing=true)
│       ├── SolapiProperties.java      sms.solapi.*
│       ├── request/SolapiMessageRequest.java
│       └── response/SolapiMessageResponse.java
└── messaging/
    ├── MessagingModuleConfig.java     진입점
    └── config/
        ├── MailDomainConfig.java      ← persistence에서 이관
        └── SmsDomainConfig.java       ← persistence에서 이관
```

자바 패키지 `external.mail..`·`external.sms..`는 **불변**이다(코어 스캔 범위가 `external.config`·`external.file`이라 동반 스캔 위험이 없다). `MailDomainConfig`·`SmsDomainConfig`만 이관하며 패키지가 `com.tastyhouse.infrastructure.{mail,sms}.config` → `com.tastyhouse.external.messaging.config`로 바뀌었고, **내용은 불변**이다.

## 어느 앱이 의존하는가

**web-api 하나뿐이다.** 메일·SMS 인증은 사용자 앱에서만 쓴다. admin-api·ceo-api·batch-module은 이 모듈을 의존하지도 `@Import` 하지도 않으며, `spring.mail.*`·`mail.*`·`sms.*` 설정도 더 이상 받지 않는다(그 값을 읽는 빈이 세 앱에 없다 — 소비자가 전부 `@WebApp`이다).

## `MailDomainConfig`·`SmsDomainConfig` 이관 — 컨벤션의 예외

### 원칙과 그 예외

이 저장소의 원칙은 **"도메인 서비스 빈은 persistence의 `<ctx>/config/<Ctx>DomainConfig`가 등록한다"**이다(도메인 서비스가 `@Service` 없는 순수 POJO라 스캔되지 않으므로). 여기에 이번 분리로 예외를 추가한다.

> **생성자가 요구하는 아웃바운드 포트의 구현이 일부 앱에만 있으면, 그 포트를 구현하는 모듈이 도메인 서비스 빈을 등록한다.**

`MailVerificationService`는 생성자로 `MailSender`를, `SmsVerificationService`는 `SmsSender`를 요구한다(인증코드 발급과 발송을 원자적으로 수행하기 위해 도메인 서비스가 포트를 직접 들고 있다). 그런데 그 구현은 web-api에만 있다. 두 설정이 persistence에 남아 있으면 persistence를 의존하는 **admin/ceo/batch도 발송 어댑터를 강제로 들여와야 했다** — 분리 전 admin/ceo가 file 하나만 쓰면서 메일·SMS 코드를 통째로 받던 직접 원인이 이것이다.

**대조군**: `FileDomainConfig`의 `FileStoragePort`는 4개 앱 전부가 구현을 갖는다(firebase). 그래서 그 설정은 persistence에 그대로 남는다 — 예외는 "일부 앱에만 있는" 경우에 한정된다.

### 채택하지 않은 대안 — `@ConditionalOnBean`

`MailDomainConfig`를 persistence에 남긴 채 `@ConditionalOnBean(MailSender.class)`를 붙이는 방법이 있다. **채택하지 않았다.** `@ConditionalOnBean`은 사용자 `@Configuration` 사이에서 **등록 순서에 의존**하기 때문이다 — 조건 평가 시점에 `MailSender` 빈 정의가 아직 등록되지 않았으면 조건이 거짓이 되어, 배선이 옳은데도 도메인 서비스 빈이 조용히 사라진다. 실패가 "빈 부재"로 즉시 드러나는 편이 낫다. (Spring 레퍼런스도 `@ConditionalOnBean`을 auto-configuration 전용으로 권고한다.)

### persistence에 남는 것

`MailVerificationEventListener`·`SmsVerificationEventListener`는 **persistence에 잔류한다.** 이 리스너들은 발송 포트를 주입받지 않고 도메인 이벤트를 관찰(로깅 등)만 하므로, 위 예외의 조건("생성자가 아웃바운드 포트 구현을 요구")에 해당하지 않는다. 두 컨텍스트의 JPA 어댑터(`MailVerificationJpaEntity`/`Mapper`/`RepositoryImpl` 등)도 그대로 persistence다.

## 진입 설정과 스캔 범위

`MessagingModuleConfig`(`@Configuration(proxyBeanMethods = false)`)가 아래를 갖는다.

- `@ComponentScan({"com.tastyhouse.external.mail", "com.tastyhouse.external.sms", "com.tastyhouse.external.messaging"})` — 채널 어댑터 두 패키지와 이관된 DomainConfig 패키지
- `@EnableConfigurationProperties({MailProperties.class, SmsProperties.class, SolapiProperties.class})`

**provider 조건은 구현 클래스에 붙어 있고 진입 설정에는 없다.** `JavaMailAdapter`·`SolapiSmsClient` 둘 다 `matchIfMissing = true`라 provider 값이 없어도 기본 구현으로 등록된다. AWS로 전환하려면 `../aws/AGENTS.md`의 절차를 따른다(`mail.provider=ses` / `sms.provider=sns`).

## yml — `application-messaging.yml`

**web-api만** `spring.config.import`로 로딩한다. `mail.provider`·`mail.sender-address`, `sms.provider`·`sms.sender-number`·`sms.solapi.*`(api-key·api-secret·base-url·send-many-path), 그리고 SMTP 접속 정보 `spring.mail.*`(호스트·포트·계정·starttls)을 담는다. 자격증명은 전부 `.env` 환경변수 참조(`${GMAIL_APP_PASSWORD}`·`${SOLAPI_API_SECRET}` 등)다.

분리 시 주석 처리된 채 남아 있던 `spring.cloud.aws` 블록은 삭제했다 — AWS 설정은 `infrastructure:aws`의 `application-aws.yml`이 소유한다.

## Dependencies

### Internal
- `infrastructure:external` (implementation) — `WebClient.Builder`(Solapi 호출), `ExternalApiException`/`ExternalApiErrorCode`
- `domain-module` (implementation) — 구현하는 `MailSender`(`mail/port/`)·`SmsSender`(`sms/port/`) 포트, 그리고 **이관된 DomainConfig가 등록하는 도메인 서비스**(`MailVerificationService`·`SmsVerificationService`)와 그 생성자가 요구하는 리포지토리 포트·`DomainEventPublisher`

### External
- `spring-boot-starter-mail` — `JavaMailSender`. **이 좌표를 클래스패스에 올리는 유일한 모듈이며, 의존하는 앱은 web-api뿐이다**(분리 전에는 4개 앱 전부가 받았다)
- `spring-boot-starter-webflux` — Solapi HTTP 호출

**`infrastructure:aws`가 이 모듈을 의존한다** — `SesMailSender`가 `MailProperties`의 발신자 주소를 읽기 때문이다. 방향은 벤더 → 채널이며 그 반대가 아니다.

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **빈 배선**: web-api만 `@Import(MessagingModuleConfig.class)` 한다. 빠뜨리면 `MailSender`/`SmsSender` 부재로 이관된 DomainConfig의 도메인 서비스 빈 생성이 실패해 **기동 시** 드러난다.
- **발송 실패는 인증 레코드 저장을 롤백시킨다** — 이 도메인에서는 그것이 올바른 의미다(발송되지 않은 인증코드는 존재 가치가 0). 상세는 `backend/CLAUDE.md`의 "인증코드 발송은 발급과 원자적으로 수행하는 규칙".
- **`@RateLimit keyPrefix`는 개명하지 않는다** — Redis 카운터 키라 바꾸면 배포 시점에 발송 한도가 전원 리셋된다.
