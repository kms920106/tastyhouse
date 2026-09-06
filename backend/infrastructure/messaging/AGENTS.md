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
    ├── MessagingModuleAutoConfiguration.java  진입점 — 챕터 02로 MessagingModuleConfig에서 리네임 + @AutoConfiguration, 자기 등록
    └── config/
        ├── MailDomainConfig.java      ← persistence에서 이관
        └── SmsDomainConfig.java       ← persistence에서 이관
```

자바 패키지 `external.mail..`·`external.sms..`는 **불변**이다(코어 스캔 범위가 `external.config`·`external.file`이라 동반 스캔 위험이 없다). `MailDomainConfig`·`SmsDomainConfig`만 이관하며 패키지가 `com.tastyhouse.infrastructure.{mail,sms}.config` → `com.tastyhouse.external.messaging.config`로 바뀌었고, **내용은 불변**이다.

## 어느 앱이 의존하는가

**web-api 하나뿐이다.** 메일·SMS 인증은 사용자 앱에서만 쓴다. admin-api·ceo-api·batch-module은 이 모듈을 의존하지 않으며(챕터 02 이후로는 의존 선언이 곧 활성화다), `spring.mail.*`·`mail.*`·`sms.*` 설정도 더 이상 받지 않는다(그 값을 읽는 빈이 세 앱에 없다 — 소비자가 전부 `@WebApp`이다).

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

`MessagingModuleAutoConfiguration`(챕터 02 — `@AutoConfiguration(proxyBeanMethods = false)`, `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록)이 아래를 갖는다.

- `@ComponentScan({"com.tastyhouse.external.mail", "com.tastyhouse.external.sms", "com.tastyhouse.external.messaging"})` — 채널 어댑터 두 패키지와 이관된 DomainConfig 패키지
- `@EnableConfigurationProperties({MailProperties.class, SmsProperties.class, SolapiProperties.class})`

**provider 조건은 구현 클래스에 붙어 있고 진입 설정에는 없다.** `JavaMailAdapter`·`SolapiSmsClient` 둘 다 `matchIfMissing = true`라 provider 값이 없어도 기본 구현으로 등록된다. AWS로 전환하려면 `../aws/AGENTS.md`의 절차를 따른다(`mail.provider=ses` / `sms.provider=sns`).

## yml — `application-messaging.yml`

**web-api만** `spring.config.import`로 로딩한다. `mail.provider`·`mail.sender-address`, `sms.provider`·`sms.sender-number`·`sms.solapi.*`(api-key·api-secret·base-url·send-many-path), 그리고 SMTP 접속 정보 `spring.mail.*`(호스트·포트·계정·starttls)을 담는다. 자격증명은 전부 `.env` 환경변수 참조(`${GMAIL_APP_PASSWORD}`·`${SOLAPI_API_SECRET}` 등)다.

분리 시 주석 처리된 채 남아 있던 `spring.cloud.aws` 블록은 삭제했다 — AWS 설정은 `infrastructure:aws`의 `application-aws.yml`이 소유한다.

## Dependencies

### Internal
- `infrastructure:external` (implementation) — `WebClient.Builder`(Solapi 호출), `ExternalApiException`/`ExternalApiErrorCode`
- `domain` (implementation) — 구현하는 `MailSender`(`mail/port/`)·`SmsSender`(`sms/port/`) 포트, 그리고 **이관된 DomainConfig가 등록하는 도메인 서비스**(`MailVerificationService`·`SmsVerificationService`)와 그 생성자가 요구하는 리포지토리 포트·`DomainEventPublisher`

### External
- `spring-boot-starter-mail` — `JavaMailSender`. **이 좌표를 클래스패스에 올리는 유일한 모듈이며, 의존하는 앱은 web-api뿐이다**(분리 전에는 4개 앱 전부가 받았다)
- `spring-boot-starter-webflux` — Solapi HTTP 호출

**`infrastructure:aws`가 이 모듈을 의존한다** — `SesMailSender`가 `MailProperties`의 발신자 주소를 읽기 때문이다. 방향은 벤더 → 채널이며 그 반대가 아니다.

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **빈 배선 (챕터 02 개정)**: web-api만 `runtimeOnly project(':infrastructure:messaging')`를 선언한다(챕터 02 — `implementation`에서 강등). `MessagingModuleAutoConfiguration`이 클래스패스 존재만으로 자동 등록되므로 `@Import`는 없다 — 다른 앱이 실수로 이 모듈에 의존을 추가하면 `MailSender`/`SmsSender` 부재가 아니라 오히려 그 즉시 이관된 DomainConfig의 도메인 서비스 빈이 등록돼 버리므로(조건이 없다), 새로 의존을 추가할 앱이 없는지 신중히 확인한다.
- **발송 실패는 인증 레코드 저장을 롤백시킨다** — 이 도메인에서는 그것이 올바른 의미다(발송되지 않은 인증코드는 존재 가치가 0). 상세는 `backend/CLAUDE.md`의 "인증코드 발송은 발급과 원자적으로 수행하는 규칙".
- **`@RateLimit keyPrefix`는 개명하지 않는다** — Redis 카운터 키라 바꾸면 배포 시점에 발송 한도가 전원 리셋된다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### `MailProperties`를 Spring Boot의 동명 타입과 한 파일에서 함께 import 하지 않는다

**대상**: `backend/infrastructure/messaging/src/main/java/com/tastyhouse/external/mail/MailProperties.java`

Spring Boot 자동설정의 `org.springframework.boot.autoconfigure.mail.MailProperties`와 **단순 클래스명이 같다.** 두 타입을 한 파일에서 함께 import하지 않는다(하나는 FQN으로 쓰거나 import를 나눈다). 바인딩 접두어는 서로 달라 충돌하지 않는다 — 이 클래스는 최상위 `mail.*`, Spring 쪽은 `spring.mail.*`이다. 이름이 겹친다는 이유로 이 클래스를 개명하지 않는다(프로퍼티 접두어 `mail`이 wire 계약이자 yml 계약이다).

### 자바 패키지 `com.tastyhouse.external.mail..`·`external.sms..` 봉인

**대상**: `backend/infrastructure/messaging/src/main/java/com/tastyhouse/external/{mail,sms,messaging}/`

외부 연동 7모듈 공통 규칙으로, 패키지 루트를 `com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 `@ComponentScan("com.tastyhouse.infrastructure")`가 통째로 스캔해 **admin-api·ceo-api·batch-module의 부팅이 깨진다.** 상세는 `../external/AGENTS.md`.

### 새 POJO 도메인 서비스는 `@Bean`을 손으로 추가한다

**대상**: `backend/infrastructure/messaging/src/main/java/com/tastyhouse/external/messaging/config/MailDomainConfig.java`
**대상**: `backend/infrastructure/messaging/src/main/java/com/tastyhouse/external/messaging/config/SmsDomainConfig.java`

도메인 서비스는 `@Service` 없는 순수 POJO라 Spring이 스캔할 수 없다. mail·sms 컨텍스트에 POJO 도메인 서비스를 추가하면 **이 두 설정 클래스에 `@Bean` 메서드를 함께 추가해야 한다.** 빠뜨리면 컴파일은 통과하고 주입 시점에 빈 부재로 실패한다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### `JavaMailAdapter`가 `JavaMailMailSender`가 아닌 이유

**대상**: `backend/infrastructure/messaging/src/main/java/com/tastyhouse/external/mail/javamail/JavaMailAdapter.java`

도메인 포트 `MailSender`의 기본 구현(JavaMail/SMTP)이다. 클래스명이 `JavaMailMailSender`가 아닌 것은 이 어댑터가 **주입받는 Spring의 `JavaMailSender`와 타입명이 혼동되기 때문**이며, `Adapter` 접미어로 구분한다. 포트 구현체 이름을 포트명에 맞춰 정리하려는 시도가 이 지점에서 되돌아오기 쉽다.

### 인증 발급이 발송까지 원자적인 이유 (도메인 서비스가 포트를 직접 든다)

**대상**: `backend/infrastructure/messaging/src/main/java/com/tastyhouse/external/messaging/config/MailDomainConfig.java` → `MailVerificationService` 빈
**대상**: `backend/infrastructure/messaging/src/main/java/com/tastyhouse/external/messaging/config/SmsDomainConfig.java` → `SmsVerificationService` 빈

두 도메인 서비스는 **같은 이메일/같은 번호의 기존 미완료 인증을 함께 만료시키는 크로스 인스턴스 불변식**을 갖는다. `MailSender`·`SmsSender`는 이 모듈(`infrastructure:messaging`)의 어댑터가 구현하며, **발급이 발송까지 원자적으로 수행되도록 그 포트를 도메인 서비스에 직접 주입한다**(발송 누락 방지). 이것이 위 "생성자가 요구하는 아웃바운드 포트의 구현이 일부 앱에만 있으면 그 포트를 구현하는 모듈이 도메인 서비스 빈을 등록한다"는 예외가 성립하는 전제다.

### AWS 구현의 소재

**대상**: `backend/infrastructure/messaging/src/main/java/com/tastyhouse/external/messaging/MessagingModuleAutoConfiguration.java`

메일·SMS 인증은 사용자 앱에서만 쓰므로 web-api만 이 모듈을 의존하며, **클래스패스 존재만으로 활성화된다.** 분리 전에는 persistence의 `MailDomainConfig`·`SmsDomainConfig`가 `MailSender`·`SmsSender` 빈을 무조건 요구해 admin/ceo/batch도 발송 어댑터를 강제로 들여와야 했고, 두 설정을 이 모듈로 이관해 그 결합을 끊었다. AWS SES·SNS 구현은 이 모듈이 아니라 `infrastructure:aws`에 있다(`../aws/AGENTS.md`).
