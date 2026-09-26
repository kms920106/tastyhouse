<!-- Parent: ../../AGENTS.md -->

# infrastructure:mail

메일 **채널 모듈**(`java-library`). 메일 인증 도메인 서비스 빈 등록(`MailDomainConfig`), 채널 설정(`mail.provider`·`mail.sender-address`), 기본 벤더 조립(`runtimeOnly infrastructure:javamail`)을 소유한다. 포트 `MailSender`의 구현은 이 모듈이 아니라 벤더 모듈(`infrastructure:javamail` 기본, `infrastructure:aws-ses` 대안)에 있다.

`infrastructure:messaging`을 채널·벤더 4모듈(`mail`·`javamail`·`sms`·`solapi`)로 나누며(2026-09-26) 신설됐다. 형태는 `infrastructure:file-storage`(스타터) + `infrastructure:firebase`(벤더)와 대칭이다.

## 무엇을 소유하는가

```
backend/infrastructure/mail/
  build.gradle                                       domain + runtimeOnly javamail
  src/main/java/com/tastyhouse/external/mail/
    MailModuleAutoConfiguration.java                 @AutoConfiguration + @ComponentScan("com.tastyhouse.external.mail")
    config/MailDomainConfig.java                     MailVerificationService @Bean
  src/main/resources/
    application-mail.yml                             mail.provider · mail.sender-address + 벤더 yml 중첩 import
    META-INF/spring/...AutoConfiguration.imports     자기 등록
```

## 어느 앱이 의존하는가

**web-api 하나뿐이다**(`runtimeOnly`). admin-api·ceo-api·batch-module은 의존하지 않는다. 클래스패스 존재가 곧 활성화이고 `MailDomainConfig`에는 조건이 없으므로, 다른 앱에 이 모듈을 추가하면 그 즉시 `MailVerificationService` 빈이 `MailSender`를 요구한다. 새로 의존을 추가할 앱이 없는지 신중히 확인한다.

## 채널 모듈과 파일 저장 스타터의 차이

`infrastructure:file-storage`는 자바 코드가 없는 조립 전용 스타터다. 이 모듈은 코드(`MailDomainConfig`와 진입 설정)를 갖는다. 이유는 두 가지다.

- `MailVerificationService`는 생성자로 `MailSender`를 요구하는데 그 구현이 web-api에만 있다. persistence로 되돌리면 admin·ceo·batch가 발송 어댑터를 강제로 받게 된다(`backend/CLAUDE.md`의 DomainConfig 예외 규칙). 벤더 모듈에 두면 javamail·aws-ses 두 곳에 중복된다.
- 파일 저장의 `FileDomainConfig`는 4앱 전부 구현이 있어 persistence에 남으므로 스타터에 둘 코드가 없다.

즉 "벤더를 조립하는 쪽이 DomainConfig를 갖는다"가 채널 모듈의 정의다. `sms` 모듈도 같다.

## 벤더 전환 절차 (JavaMail → SES)

**web-api를 건드리지 않는다.** 이 모듈의 두 파일만 바꾼다.

1. `build.gradle`: `runtimeOnly project(':infrastructure:javamail')` → `runtimeOnly project(':infrastructure:aws-ses')`
2. `application-mail.yml`: import를 `classpath:application-aws-ses.yml`로, `mail.provider: ses`로
3. `.env`에 `AWS_SES_ACCESS_KEY`·`AWS_SES_SECRET_KEY`(이미 있다)

`mail.sender-address`는 채널 값이라 전환해도 그대로다. **벤더 모듈 없이 provider만 바꾸면 기동 시 실패한다** — `JavaMailAdapter`는 조건으로 빠지고 SES 구현은 클래스패스에 없어 `MailDomainConfig`가 `MailSender` 빈을 찾지 못한다. 이 실패가 전환의 안전장치이며, 전환 검증도 틀린 provider 값으로 실패를 확인하는 반증 방향으로 한다.

## yml — `application-mail.yml`

`mail.provider`(`javamail` | `ses`)와 `mail.sender-address`(`${MAIL_SENDER_ADDRESS}`)를 소유하고, 벤더 yml(`classpath:application-javamail.yml`)을 중첩 `spring.config.import`로 로딩한다(file-storage → firebase와 같은 방식). web-api `application.yml`에는 `classpath:application-mail.yml` 한 줄만 있다.

**`MAIL_SENDER_ADDRESS` 환경변수가 반드시 있어야 한다.** 벤더가 `@Value("${mail.sender-address}")`로 읽으므로 미해석 placeholder는 기동 실패다(과거 `@ConfigurationProperties` 바인딩 시절에는 미해석 값이 문자열 그대로 넘어가 기동은 됐다).

## Dependencies

- `domain` (implementation) — `MailDomainConfig`가 등록하는 `MailVerificationService`와 그 생성자가 요구하는 `MemberRepository`·`MailVerificationRepository`·`MailSender`·`DomainEventPublisher`
- `infrastructure:javamail` (runtimeOnly) — 기본 벤더. compileClasspath에는 없다
- `infrastructure:restclient` 의존 없음(직접·전이 모두)

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **발송 실패는 인증 레코드 저장을 롤백시킨다** — 발송되지 않은 인증코드는 존재 가치가 0이다. 상세는 `backend/CLAUDE.md`의 "인증코드 발송은 발급과 원자적으로 수행하는 규칙".
- **`@RateLimit keyPrefix`는 개명하지 않는다** — Redis 카운터 키라 바꾸면 배포 시점에 발송 한도가 전원 리셋된다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 발신자 주소는 클래스가 아니라 프로퍼티 키로 공유한다

**대상**: `backend/infrastructure/javamail/src/main/java/com/tastyhouse/external/javamail/JavaMailAdapter.java` → 생성자 `@Value("${mail.sender-address}")`
**대상**: `backend/infrastructure/aws-ses/src/main/java/com/tastyhouse/external/aws/ses/SesConfig.java` → `awsSesMailSender` `@Value("${mail.sender-address}")`

`mail.sender-address`는 이 채널 모듈의 yml이 소유하고 벤더가 키로 읽는다. 과거 `MailProperties` record를 되살려 벤더가 이 모듈을 `implementation`으로 의존하게 하지 않는다 — 이 모듈이 벤더를 `runtimeOnly`로 조립하므로 **채널 ↔ 벤더 순환**이 된다(`../file-storage/AGENTS.md`가 SPI 이동안을 같은 이유로 비채택했다). 과거 `aws-ses`가 `MailProperties` 하나 때문에 messaging 전체(restclient·Solapi·starter-mail)를 전이로 받던 문제도 이 방식으로 사라졌다.

### 진입 설정은 자기 패키지만 스캔한다, 벤더는 형제 패키지에 둔다

**대상**: `backend/infrastructure/mail/src/main/java/com/tastyhouse/external/mail/MailModuleAutoConfiguration.java`

스캔 범위는 `com.tastyhouse.external.mail`이다. 벤더를 이 하위(`external.mail.javamail`·`external.mail.ses`)에 두면 이 스캔에 동반 스캔되어 벤더 선택이 조립(`build.gradle`)이 아니라 채널 모듈 존재로 결정된다. 벤더는 `external.javamail`·`external.aws.ses`처럼 형제 패키지에 둔다. 패키지 루트를 `com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 통째 스캔에 걸려 admin·ceo·batch 부팅이 깨진다(`../restclient/AGENTS.md`).

### 새 POJO 도메인 서비스는 `@Bean`을 손으로 추가한다

**대상**: `backend/infrastructure/mail/src/main/java/com/tastyhouse/external/mail/config/MailDomainConfig.java`

도메인 서비스는 `@Service` 없는 순수 POJO라 스캔되지 않는다. mail 컨텍스트에 POJO 도메인 서비스를 추가하면 이 설정에 `@Bean` 메서드를 함께 추가해야 한다. 빠뜨리면 컴파일은 통과하고 주입 시점에 빈 부재로 실패한다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 인증 발급이 발송까지 원자적인 이유 (도메인 서비스가 포트를 직접 든다)

**대상**: `backend/infrastructure/mail/src/main/java/com/tastyhouse/external/mail/config/MailDomainConfig.java` → `MailVerificationService` 빈

`MailVerificationService`는 같은 이메일의 기존 미완료 인증을 함께 만료시키는 크로스 인스턴스 불변식을 갖는다. 발급이 발송까지 원자적으로 수행되도록 `MailSender` 포트를 도메인 서비스에 직접 주입한다(발송 누락 방지). 이것이 "포트 구현이 일부 앱에만 있으면 벤더를 조립하는 채널 모듈이 도메인 서비스 빈을 등록한다"는 예외가 성립하는 전제다.

### `@ConditionalOnBean`을 쓰지 않는 이유

`MailDomainConfig`를 persistence에 남긴 채 `@ConditionalOnBean(MailSender.class)`를 붙이는 대안은 채택하지 않았다. 사용자 `@Configuration` 사이에서 등록 순서에 따라 조건이 거짓이 되어 배선이 옳은데도 빈이 조용히 사라질 수 있다. 실패가 "빈 부재"로 즉시 드러나는 편이 낫다. `MailVerificationEventListener`는 발송 포트를 주입받지 않으므로 persistence에 잔류한다.
