<!-- Parent: ../../AGENTS.md -->

# infrastructure:sms

SMS **채널 스타터**(`java-library`). **자바 코드가 없다** — 의존 선언(`build.gradle`)과 설정 진입점(`application-sms.yml`) 둘뿐이며, 형태는 `infrastructure:file-storage`·`infrastructure:oauth`·`infrastructure:mail`과 같다. 포트 `SmsSender`의 구현은 벤더 모듈(`infrastructure:solapi` 기본, `infrastructure:aws-sns` 대안)에 있고, 이 모듈은 그것을 `runtimeOnly`로 조립해 web-api에 노출한다.

`infrastructure:messaging` 4분할(2026-09-26)로 신설됐다. 당시엔 채널 모듈이 `SmsDomainConfig`(도메인 서비스 빈 등록 코드)를 갖고 있었으나, **채널·벤더 포트 이관 프로그램("chunk 02-vendor-ports")으로 그 코드가 삭제되고 이 모듈은 코드 없는 스타터가 됐다** — 아래 "무엇이 바뀌었는가" 절 참고. 형태·역사는 `../mail/AGENTS.md`와 동형이다.

## 무엇을 소유하는가

```
backend/infrastructure/sms/
  build.gradle                                       runtimeOnly solapi (한 줄)
  AGENTS.md
  src/main/resources/application-sms.yml              sms.provider · sms.sender-number + 벤더 yml 중첩 import
```

`compileJava`는 NO-SOURCE로 넘어가고 jar에는 yml만 실린다. `META-INF/spring/...AutoConfiguration.imports`도 없다 — 등록할 빈이 없기 때문이다.

## 무엇이 바뀌었는가 (chunk 02-vendor-ports)

**이 모듈은 과거 코드를 가진 채널 모듈이었다. 그 코드는 삭제됐고, 지금은 `infrastructure:file-storage`·`infrastructure:oauth`와 같은 부류의 코드 없는 스타터다.**

| 항목 | before | after |
|---|---|---|
| `SmsModuleAutoConfiguration.java` | `@AutoConfiguration` + `@ComponentScan("com.tastyhouse.external.sms")` | **삭제** |
| `config/SmsDomainConfig.java` | `SmsVerificationService` `@Bean` 등록 | **삭제** — 등록 책임이 `application`으로 이동 |
| `META-INF/spring/...AutoConfiguration.imports` | 자기 등록 | **삭제** |
| `build.gradle` | `implementation project(':domain')` + `runtimeOnly solapi` | `runtimeOnly project(':infrastructure:solapi')` **한 줄만** |
| `SmsVerificationService`(도메인 서비스) | `SmsDomainConfig`가 `@Bean`으로 등록, `com.tastyhouse.external.sms.config` 소속 | **annotation-free POJO**로 `application/sms/service/`로 이동, 등록은 `application`의 `SmsServiceConfig`(`@WebApp`) 담당 |

**"채널 모듈이 벤더를 조립하는 쪽이라 DomainConfig를 갖는다"던 과거 규칙(`../mail/AGENTS.md`의 §역사 참고)은 이 모듈에서도 번복됐다** — 도메인 서비스 등록이 애초에 이 모듈의 자리가 아니게 재설계됐기 때문이다. `find backend/infrastructure/sms -name "*.java" -not -path "*/build/*"`는 아무것도 반환하지 않는다(자바 코드 0개, 확인됨).

## 지금 이 모듈이 하는 일 — 조립과 설정 두 가지만

- **조립**: `build.gradle`의 `runtimeOnly project(':infrastructure:solapi')` 한 줄이 기본 벤더를 web-api의 runtimeClasspath에 싣는다.
- **설정**: `application-sms.yml`이 `sms.provider`·`sms.sender-number`를 소유하고 벤더 yml을 중첩 import한다(아래 §yml).

빈 등록은 이제 조립 대상(solapi/aws-sns)의 auto-configuration이 `SmsSender` 구현체를, `application`의 `SmsServiceConfig`(`@WebApp`)가 `SmsVerificationService`를 각각 담당한다 — 이 모듈은 어느 쪽도 하지 않는다.

## 도메인 서비스 등록은 이제 어디인가 — `application`의 `SmsServiceConfig`

`SmsVerificationService`(생성자로 `SmsSender`·`SmsVerificationPersistencePort`·`DomainEventPublisher`를 요구)는 `application/sms/service/`의 annotation-free POJO이고, `application`의 `sms/config/SmsServiceConfig`(`@WebApp`)가 `@Bean`으로 등록한다. 이 설정은 web-api에서만 스캔되므로(마커 `@WebApp`), 발송 기능이 없는 admin·ceo·batch에는 이 빈이 뜨지 않는다.

**이 모듈은 그 등록에 관여하지 않는다.** 벤더(solapi/aws-sns)를 조립해 `SmsSender` 구현체를 web-api의 클래스패스에 올리는 것까지가 이 모듈의 일이다.

## 벤더 전환 절차 (Solapi → SNS)

**web-api를 건드리지 않는다.** 이 모듈의 두 파일에서 세 곳을 바꾼다. **①②③은 항상 함께 바꾼다** — 이 모듈은 벤더를 하나만 `runtimeOnly`로 싣기 때문에, 셋 중 하나라도 빠지면 켤 수 있는 벤더가 없다.

1. `build.gradle`: `runtimeOnly project(':infrastructure:solapi')` → `runtimeOnly project(':infrastructure:aws-sns')`
2. `application-sms.yml`의 `spring.config.import`: `classpath:application-solapi.yml` → `classpath:application-aws-sns.yml`
3. `application-sms.yml`의 `sms.provider`: `solapi` → `sns`

그 밖에 `.env`에 `AWS_SNS_ACCESS_KEY`·`AWS_SNS_SECRET_KEY`가 있어야 한다(이미 있다). 벤더 쪽에서 본 같은 절차는 `../aws-sns/AGENTS.md`의 "SNS로 전환하는 절차 (채널 모듈 2파일)"에 있다.

**벤더 모듈 없이 provider만 바꾸면 기동 시 실패한다** — `SolapiSmsClient`는 조건으로 빠지고 SNS 구현이 없어 `application`의 `SmsServiceConfig`가 `SmsSender` 빈을 찾지 못한다. 전환 검증은 반증 방향으로 한다.

## yml — `application-sms.yml`

`sms.provider`와 `sms.sender-number`(`${SMS_SENDER_NUMBER}`)를 소유하고, 벤더 yml(`classpath:application-solapi.yml`)을 중첩 import로 로딩한다. 발신 번호의 주인은 채널이다 — Solapi yml은 `sender-number: ${sms.sender-number}`로 이 값을 참조한다.

**`sms.provider` 줄에 허용값 주석(`# solapi | sns`)을 달지 않는다.** 벤더 코드에 정의된 값은 `solapi`·`sns` 둘이지만 이 모듈이 벤더를 하나만 싣기 때문에 지금 켤 수 있는 값은 `solapi` 하나뿐이다. 값만 `sns`로 바꾸면 `SmsSender` 빈이 없어 기동이 실패하므로, 나열은 거짓 선택지다. 전환은 위 3곳 동시 교체로만 한다.

## Dependencies

- `infrastructure:solapi` (runtimeOnly) — 기본 벤더. compileClasspath에는 없다
- `domain`·`application` 의존 없음 — 자바 코드가 없어 도메인·유스케이스 타입을 참조할 일이 없다(과거엔 `SmsDomainConfig`가 요구하는 타입들 때문에 `domain`을 `implementation`으로 의존했으나, 그 설정 클래스가 `application`으로 이동하며 이 모듈의 의존도 사라졌다)

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **발송 실패는 인증 레코드 저장을 롤백시킨다.** 상세는 `backend/CLAUDE.md`의 "인증코드 발송은 발급과 원자적으로 수행하는 규칙". 이 규칙을 구현하는 코드(`SmsVerificationService`)는 지금 `application`에 있다.
- **`@RateLimit keyPrefix`는 개명하지 않는다** — Redis 카운터 키다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 이 모듈에 코드를 넣지 않는다 (번복됨 — 과거엔 DomainConfig를 가진 채널 모듈이었다)

**대상**: `backend/infrastructure/sms/` 전체(`src/main/java` 부재가 정상)

이 모듈은 원래 `SmsDomainConfig`를 가진 채널 모듈이었으나(chunk 02-vendor-ports 이전), 도메인 서비스 등록이 `application`의 `SmsServiceConfig`로 이관되며 코드 없는 스타터가 됐다(`../file-storage/AGENTS.md`·`../oauth/AGENTS.md`와 같은 판단). **`SmsModuleAutoConfiguration`·`SmsDomainConfig`를 되살리지 않는다** — 도메인 서비스 등록은 `application`의 일이다.

### 발신 번호는 클래스가 아니라 프로퍼티 키로 공유한다

**대상**: `backend/infrastructure/sms/src/main/resources/application-sms.yml` → `sms.sender-number`
**대상**: `backend/infrastructure/solapi/src/main/resources/application-solapi.yml` → `sms.solapi.sender-number`

벤더가 발신 번호를 쓰려고 이 모듈을 `implementation`으로 의존하게 하지 않는다(채널 ↔ 벤더 순환). 벤더는 자기 yml에서 `${sms.sender-number}`를 참조하거나 `@Value`로 키를 읽는다. `aws-sns`가 발신 번호 지정이 필요해지면 같은 방식으로 추가한다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 인증 발급이 발송까지 원자적인 이유

**대상**: `backend/application/src/main/java/com/tastyhouse/application/sms/service/SmsVerificationService.java`

`SmsVerificationService`는 같은 번호의 기존 미완료 인증을 함께 만료시키는 크로스 인스턴스 불변식을 갖고, 발송 누락을 막으려 `SmsSender`를 직접 주입받는다. 등록 위치는 chunk 02-vendor-ports로 `infrastructure:sms`의 `SmsDomainConfig`에서 `application`의 `SmsServiceConfig`로 옮겨갔으나, 이 설계 근거 자체는 바뀌지 않았다.
