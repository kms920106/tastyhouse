<!-- Parent: ../../AGENTS.md -->

# infrastructure:mail

메일 **채널 스타터**(`java-library`). **자바 코드가 없다** — 의존 선언(`build.gradle`)과 설정 진입점(`application-mail.yml`) 둘뿐이며, 형태는 `infrastructure:file-storage`·`infrastructure:oauth`와 같다. 포트 `MailSender`의 구현은 벤더 모듈(`infrastructure:javamail` 기본, `infrastructure:aws-ses` 대안)에 있고, 이 모듈은 그것을 `runtimeOnly`로 조립해 web-api에 노출한다.

`infrastructure:messaging`을 채널·벤더 4모듈(`mail`·`javamail`·`sms`·`solapi`)로 나누며(2026-09-26) 신설됐다. 당시엔 채널 모듈이 `MailDomainConfig`(도메인 서비스 빈 등록 코드)를 갖고 있었으나, **채널·벤더 포트 이관 프로그램("chunk 02-vendor-ports")으로 그 코드가 삭제되고 이 모듈은 코드 없는 스타터가 됐다** — 아래 "무엇이 바뀌었는가" 절 참고.

## 무엇을 소유하는가

```
backend/infrastructure/mail/
  build.gradle                                       runtimeOnly javamail (한 줄)
  AGENTS.md
  src/main/resources/application-mail.yml             mail.provider · mail.sender-address + 벤더 yml 중첩 import
```

`compileJava`는 NO-SOURCE로 넘어가고 jar에는 yml만 실린다(비어 있지 않으므로 리소스 로딩에 문제없음). `META-INF/spring/...AutoConfiguration.imports`도 없다 — 등록할 빈이 없기 때문이다.

## 무엇이 바뀌었는가 (chunk 02-vendor-ports)

**이 모듈은 과거 코드를 가진 채널 모듈이었다. 그 코드는 삭제됐고, 지금은 `infrastructure:file-storage`·`infrastructure:oauth`와 같은 부류의 코드 없는 스타터다.**

| 항목 | before | after |
|---|---|---|
| `MailModuleAutoConfiguration.java` | `@AutoConfiguration` + `@ComponentScan("com.tastyhouse.external.mail")` | **삭제** |
| `config/MailDomainConfig.java` | `MailVerificationService` `@Bean` 등록 | **삭제** — 등록 책임이 `application`으로 이동 |
| `META-INF/spring/...AutoConfiguration.imports` | 자기 등록 | **삭제** |
| `build.gradle` | `implementation project(':domain')` + `runtimeOnly javamail` | `runtimeOnly project(':infrastructure:javamail')` **한 줄만** |
| `MailVerificationService`(도메인 서비스) | `MailDomainConfig`가 `@Bean`으로 등록, `com.tastyhouse.external.mail.config` 소속 | **annotation-free POJO**로 `application/mail/service/`로 이동, 등록은 `application`의 `MailServiceConfig`(`@WebApp`) 담당. **(번복됨 — application `*ServiceConfig` 삭제)** 지금은 `MailServiceConfig`도 삭제됐고 `MailVerificationService` 클래스에 `@WebApp` 마커만 붙어 스캔 등록된다 |

**"채널 모듈이 벤더를 조립하는 쪽이라 DomainConfig를 갖는다"던 과거 규칙(아래 §역사 참고)은 이 모듈에서는 번복됐다** — 도메인 서비스 등록이 애초에 이 모듈의 자리가 아니게 재설계됐기 때문이다. `find backend/infrastructure/mail -name "*.java" -not -path "*/build/*"`는 아무것도 반환하지 않는다(자바 코드 0개, 확인됨).

## 지금 이 모듈이 하는 일 — 조립과 설정 두 가지만

- **조립**: `build.gradle`의 `runtimeOnly project(':infrastructure:javamail')` 한 줄이 기본 벤더를 web-api의 runtimeClasspath에 싣는다.
- **설정**: `application-mail.yml`이 `mail.provider`·`mail.sender-address`를 소유하고 벤더 yml을 중첩 import한다(아래 §yml).

빈 등록은 이제 조립 대상(javamail/aws-ses)의 auto-configuration이 `MailSender` 구현체를, `web-application`의 `MailVerificationService` 클래스가 `@Service` 스캔으로(앱 마커 제거 전에는 `application` + `@WebApp` 마커) 각각 담당한다 — 이 모듈은 어느 쪽도 하지 않는다.

## 도메인 서비스 등록은 이제 어디인가 — `application`의 `MailVerificationService` 클래스 마커

> **(번복됨 — 앱 마커 제거)** 지금 `MailVerificationService`는 **`web-application` 모듈**의 `mail/service/`에 있고 **`@Service`**로 등록된다(마커 없음). `MailSender` 포트도 `web-application`의 `mail.port.out`에 있다(패키지 불변). web-api만 `web-application`을 의존하므로 발송 기능이 없는 admin·ceo·batch에는 여전히 이 빈이 뜨지 않는다. 아래의 "`MailSender`를 받는 마커 클래스가 `@WebApp`이 아니면 `AppIsolationTest#appRestrictedPortDependentsShouldBelongToThatApp`이 잡는다"는 번복됐다 — 그 규칙은 삭제됐고, `MailSender`가 `web-application`에 있어 다른 앱의 빈은 **컴파일 단계에서** 그 포트를 볼 수 없다. 빈 이름과 등록 앱(web)은 그대로다.

**(번복됨 — application `*ServiceConfig` 삭제)** 아래 문단의 `MailServiceConfig`는 삭제됐다. 지금은 `application/mail/service/MailVerificationService` 클래스에 `@WebApp` 마커만(`@Service` 없이) 붙고, web-api의 마커 기반 컴포넌트 스캔이 생성자 주입으로 등록한다. 빈 이름(`mailVerificationService`)과 등록 앱(web)은 바뀌지 않았다. `MailSender`를 생성자로 받는 마커 클래스가 `@WebApp`이 아니면 `AppIsolationTest#appRestrictedPortDependentsShouldBelongToThatApp`이 빌드에서 실패시킨다. 아래는 ServiceConfig 삭제 전의 기록이다.

`MailVerificationService`(생성자로 `MailSender`·`MemberPersistencePort`·`MailVerificationPersistencePort`·`DomainEventPublisher`를 요구)는 `application/mail/service/`의 annotation-free POJO이고, `application`의 `mail/config/MailServiceConfig`(`@WebApp`)가 `@Bean`으로 등록한다. 이 설정은 web-api에서만 스캔되므로(마커 `@WebApp`), 발송 기능이 없는 admin·ceo·batch에는 이 빈이 뜨지 않는다 — 과거 이 모듈이 지키던 "포트 구현이 web에만 있다"는 제약이 이제 `application` 쪽 마커로 표현된다.

**이 모듈은 그 등록에 관여하지 않는다.** 벤더(javamail/aws-ses)를 조립해 `MailSender` 구현체를 web-api의 클래스패스에 올리는 것까지가 이 모듈의 일이고, 그 구현체를 누가 소비하는지는 알지 못한다.

## 채널 스타터와 파일 저장 스타터의 관계 (역사 — 지금은 형태가 같다)

과거 문서는 "채널 모듈은 DomainConfig를 갖고 스타터(`file-storage`·`oauth`)는 코드가 없다"고 구분했다. 그 구분은 **소멸했다** — 지금은 `mail`도 코드가 없는 스타터이므로 `file-storage`·`oauth`와 형태가 같다. 남은 차이는 배타 선택 방식뿐이다: `mail`은 `mail.provider`로 벤더 하나만 배타적으로 켜고(`file-storage`의 `file.provider`와 동형), `oauth`는 벤더 4종이 공존한다.

## 벤더 전환 절차 (JavaMail → SES)

**web-api를 건드리지 않는다.** 이 모듈의 두 파일에서 세 곳을 바꾼다. **①②③은 항상 함께 바꾼다** — 이 모듈은 벤더를 하나만 `runtimeOnly`로 싣기 때문에, 셋 중 하나라도 빠지면 켤 수 있는 벤더가 없다.

1. `build.gradle`: `runtimeOnly project(':infrastructure:javamail')` → `runtimeOnly project(':infrastructure:aws-ses')`
2. `application-mail.yml`의 `spring.config.import`: `classpath:application-javamail.yml` → `classpath:application-aws-ses.yml`
3. `application-mail.yml`의 `mail.provider`: `javamail` → `ses`

그 밖에 `.env`에 `AWS_SES_ACCESS_KEY`·`AWS_SES_SECRET_KEY`가 있어야 한다(이미 있다). 벤더 쪽에서 본 같은 절차는 `../aws-ses/AGENTS.md`의 "SES로 전환하는 절차 (채널 모듈 2파일)"에 있다.

`mail.sender-address`는 채널 값이라 전환해도 그대로다. **벤더 모듈 없이 provider만 바꾸면 기동 시 실패한다** — `JavaMailAdapter`는 조건으로 빠지고 SES 구현은 클래스패스에 없어 `application`의 `MailVerificationService`(구 `MailServiceConfig` 등록)가 `MailSender` 빈을 찾지 못한다. 이 실패가 전환의 안전장치이며, 전환 검증도 틀린 provider 값으로 실패를 확인하는 반증 방향으로 한다.

## yml — `application-mail.yml`

`mail.provider`와 `mail.sender-address`(`${MAIL_SENDER_ADDRESS}`)를 소유하고, 벤더 yml(`classpath:application-javamail.yml`)을 중첩 `spring.config.import`로 로딩한다(file-storage → firebase와 같은 방식). web-api `application.yml`에는 `classpath:application-mail.yml` 한 줄만 있다.

**`mail.provider` 줄에 허용값 주석(`# javamail | ses`)을 달지 않는다.** 벤더 코드에 정의된 값은 `javamail`·`ses` 둘이지만, 이 모듈이 벤더를 하나만 싣기 때문에 지금 켤 수 있는 값은 `javamail` 하나뿐이다. 주석으로 `ses`를 나열하면 "값만 바꾸면 SES로 전환된다"는 거짓 선택지가 되고, 실제로 값만 바꾸면 `MailSender` 빈이 없어 기동이 실패한다. 전환은 위 3곳 동시 교체로만 한다.

**`MAIL_SENDER_ADDRESS` 환경변수가 반드시 있어야 한다.** 벤더가 `@Value("${mail.sender-address}")`로 읽으므로 미해석 placeholder는 기동 실패다.

## Dependencies

- `infrastructure:javamail` (runtimeOnly) — 기본 벤더. compileClasspath에는 없다
- `domain`·`application` 의존 없음 — 자바 코드가 없어 도메인·유스케이스 타입을 참조할 일이 없다(과거엔 `MailDomainConfig`가 요구하는 타입들 때문에 `domain`을 `implementation`으로 의존했으나, 그 설정 클래스가 `application`으로 이동하며 이 모듈의 의존도 사라졌다)
- `infrastructure:restclient` 의존 없음(직접·전이 모두)

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **발송 실패는 인증 레코드 저장을 롤백시킨다** — 발송되지 않은 인증코드는 존재 가치가 0이다. 상세는 `backend/CLAUDE.md`의 "인증코드 발송은 발급과 원자적으로 수행하는 규칙". 이 규칙을 구현하는 코드(`MailVerificationService`)는 지금 `application`에 있다.
- **`@RateLimit keyPrefix`는 개명하지 않는다** — Redis 카운터 키라 바꾸면 배포 시점에 발송 한도가 전원 리셋된다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 이 모듈에 코드를 넣지 않는다 (번복됨 — 과거엔 DomainConfig를 가진 채널 모듈이었다)

**대상**: `backend/infrastructure/mail/` 전체(`src/main/java` 부재가 정상)

이 모듈은 원래 `MailDomainConfig`를 가진 채널 모듈이었으나(chunk 02-vendor-ports 이전), 도메인 서비스 등록이 `application`(당시 `MailServiceConfig`, 이후 `MailVerificationService` 클래스의 `@WebApp` 마커, 앱 마커 제거 후에는 `web-application`의 `@Service`)으로 이관되며 코드 없는 스타터가 됐다(`../file-storage/AGENTS.md`·`../oauth/AGENTS.md`와 같은 판단). **`MailModuleAutoConfiguration`·`MailDomainConfig`를 되살리지 않는다** — 도메인 서비스 등록은 `application`의 일이다.

### 발신자 주소는 클래스가 아니라 프로퍼티 키로 공유한다

**대상**: `backend/infrastructure/javamail/src/main/java/com/tastyhouse/external/javamail/JavaMailAdapter.java` → 생성자 `@Value("${mail.sender-address}")`
**대상**: `backend/infrastructure/aws-ses/src/main/java/com/tastyhouse/external/aws/ses/SesConfig.java` → `awsSesMailSender` `@Value("${mail.sender-address}")`

`mail.sender-address`는 이 채널 스타터의 yml이 소유하고 벤더가 키로 읽는다. 과거 `MailProperties` record를 되살려 벤더가 이 모듈을 `implementation`으로 의존하게 하지 않는다 — 이 모듈이 벤더를 `runtimeOnly`로 조립하므로 **채널 ↔ 벤더 순환**이 된다(`../file-storage/AGENTS.md`가 SPI 이동안을 같은 이유로 비채택했다).

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 인증 발급이 발송까지 원자적인 이유 (도메인 서비스가 포트를 직접 든다)

**대상**: `backend/application/src/main/java/com/tastyhouse/application/mail/service/MailVerificationService.java`

`MailVerificationService`는 같은 이메일의 기존 미완료 인증을 함께 만료시키는 크로스 인스턴스 불변식을 갖는다. 발급이 발송까지 원자적으로 수행되도록 `MailSender` 포트를 도메인 서비스에 직접 주입한다(발송 누락 방지). 등록 위치는 chunk 02-vendor-ports로 `infrastructure:mail`의 `MailDomainConfig`에서 `application`의 `MailServiceConfig`로 옮겨갔고 이후 application `*ServiceConfig` 삭제로 `MailVerificationService` 클래스의 `@WebApp` 마커가 등록을 대신하게 됐으나, 이 설계 근거 자체는 바뀌지 않았다.

### `@ConditionalOnBean`을 쓰지 않는 이유

`MailVerificationService`의 등록을 `@ConditionalOnBean(MailSender.class)`로 조건부화하는 대안은 채택하지 않는다. 사용자 `@Configuration` 사이에서 등록 순서에 따라 조건이 거짓이 되어 배선이 옳은데도 빈이 조용히 사라질 수 있다. 실패가 "빈 부재"로 즉시 드러나는 편이 낫다.
