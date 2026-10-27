<!-- Parent: ../../AGENTS.md -->

# infrastructure:sms

SMS **채널 스타터**(`java-library`). **자바 코드가 없다** — 의존 선언(`build.gradle`)과 설정 진입점(`application-sms.yml`) 둘뿐이며, 형태는 `infrastructure:file-storage`·`infrastructure:oauth`·`infrastructure:mail`과 같다. 포트 `SmsSenderPort`의 구현은 벤더 모듈(`infrastructure:solapi` 기본, `infrastructure:aws-sns` 대안)에 있고, 이 모듈은 그것을 `runtimeOnly`로 조립해 web-api에 노출한다.

`infrastructure:messaging`을 채널·벤더 4모듈(`mail`·`javamail`·`sms`·`solapi`)로 나눈 것 중 채널 모듈이며, 도메인 서비스 빈 등록은 이 모듈이 아니라 `web-application`이 한다. 형태는 `../mail/AGENTS.md`와 동형이다. ([결정 0036](../../AGENTS.md#decision-0036))

## 무엇을 소유하는가

```
backend/infrastructure/sms/
  build.gradle                                       runtimeOnly solapi (한 줄)
  AGENTS.md
  src/main/resources/application-sms.yml              sms.provider · sms.sender-number + 벤더 yml 중첩 import
```

`compileJava`는 NO-SOURCE로 넘어가고 jar에는 yml만 실린다. `META-INF/spring/...AutoConfiguration.imports`도 없다 — 등록할 빈이 없기 때문이다.

## 이 모듈이 하는 일 — 조립과 설정 두 가지만

- **조립**: `build.gradle`의 `runtimeOnly project(':infrastructure:solapi')` 한 줄이 기본 벤더를 web-api의 runtimeClasspath에 싣는다.
- **설정**: `application-sms.yml`이 `sms.provider`·`sms.sender-number`를 소유하고 벤더 yml을 중첩 import한다(아래 §yml).

빈 등록은 조립 대상(solapi/aws-sns)의 `@Configuration`이 `SmsSenderPort` 구현체를, `web-application`의 `SmsVerificationService` 클래스가 `@Service` 스캔으로 각각 담당한다 — 이 모듈은 어느 쪽도 하지 않는다.

## 도메인 서비스 등록은 어디인가 — `web-application`의 `SmsVerificationService`

`SmsVerificationService`는 **`web-application` 모듈**의 `phoneverification/service/`에 있고 **`@Service`**로 등록된다. `SmsSenderPort` 포트도 `web-application`의 `phoneverification.port.out`에 있다. web-api만 `web-application`을 의존하므로 발송 기능이 없는 admin·ceo·batch에는 이 빈이 뜨지 않고, 다른 앱의 빈은 **컴파일 단계에서** 그 포트를 볼 수 없다. 빈 이름은 `smsVerificationService`다. ([결정 0036](../../AGENTS.md#decision-0036))

`SmsVerificationService`는 생성자로 `SmsSenderPort`·`SmsVerificationLoadPort`·`SmsVerificationSavePort`·`DomainEventPublisher`를 요구한다.

**이 모듈은 그 등록에 관여하지 않는다.** 벤더(solapi/aws-sns)를 조립해 `SmsSenderPort` 구현체를 web-api의 클래스패스에 올리는 것까지가 이 모듈의 일이다.

## 벤더 전환 절차 (Solapi → SNS)

**web-api를 건드리지 않는다.** 이 모듈의 두 파일에서 세 곳을 바꾼다. **①②③은 항상 함께 바꾼다** — 이 모듈은 벤더를 하나만 `runtimeOnly`로 싣기 때문에, 셋 중 하나라도 빠지면 켤 수 있는 벤더가 없다.

1. `build.gradle`: `runtimeOnly project(':infrastructure:solapi')` → `runtimeOnly project(':infrastructure:aws-sns')`
2. `application-sms.yml`의 `spring.config.import`: `classpath:application-solapi.yml` → `classpath:application-aws-sns.yml`
3. `application-sms.yml`의 `sms.provider`: `solapi` → `sns`

그 밖에 `.env`에 `AWS_SNS_ACCESS_KEY`·`AWS_SNS_SECRET_KEY`가 있어야 한다(이미 있다). 벤더 쪽에서 본 같은 절차는 `../aws-sns/AGENTS.md`의 "SNS로 전환하는 절차 (채널 모듈 2파일)"에 있다.

**벤더 모듈 없이 provider만 바꾸면 기동 시 실패한다** — `SolapiSmsClient`는 조건으로 빠지고 SNS 구현이 없어 `web-application`의 `SmsVerificationService`가 `SmsSenderPort` 빈을 찾지 못한다. 전환 검증은 반증 방향으로 한다.

## yml — `application-sms.yml`

`sms.provider`와 `sms.sender-number`(`${SMS_SENDER_NUMBER}`)를 소유하고, 벤더 yml(`classpath:application-solapi.yml`)을 중첩 import로 로딩한다. 발신 번호의 주인은 채널이다 — Solapi yml은 `sender-number: ${sms.sender-number}`로 이 값을 참조한다.

**`sms.provider` 줄에 허용값 주석(`# solapi | sns`)을 달지 않는다.** 벤더 코드에 정의된 값은 `solapi`·`sns` 둘이지만 이 모듈이 벤더를 하나만 싣기 때문에 지금 켤 수 있는 값은 `solapi` 하나뿐이다. 값만 `sns`로 바꾸면 `SmsSenderPort` 빈이 없어 기동이 실패하므로, 나열은 거짓 선택지다. 전환은 위 3곳 동시 교체로만 한다.

## Dependencies

- `infrastructure:solapi` (runtimeOnly) — 기본 벤더. compileClasspath에는 없다
- `domain`·`application` 의존 없음 — 자바 코드가 없어 도메인·유스케이스 타입을 참조할 일이 없다

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **발송 실패는 인증 레코드 저장을 롤백시킨다.** 상세는 `backend/CLAUDE.md`의 "인증코드 발송은 발급과 원자적으로 수행하는 규칙". 이 규칙을 구현하는 코드(`SmsVerificationService`)는 `web-application`에 있다.
- **`@RateLimit keyPrefix`는 개명하지 않는다** — Redis 카운터 키다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 이 모듈에 코드를 넣지 않는다

**대상**: `backend/infrastructure/sms/` 전체(`src/main/java` 부재가 정상)

이 모듈은 코드 없는 스타터다(`../file-storage/AGENTS.md`·`../oauth/AGENTS.md`와 같은 판단). **`SmsModuleAutoConfiguration`·`SmsDomainConfig` 같은 등록 코드를 두지 않는다** — 도메인 서비스 등록은 `web-application`의 `@Service`가 한다.

### 발신 번호는 클래스가 아니라 프로퍼티 키로 공유한다

**대상**: `backend/infrastructure/sms/src/main/resources/application-sms.yml` → `sms.sender-number`
**대상**: `backend/infrastructure/solapi/src/main/resources/application-solapi.yml` → `sms.solapi.sender-number`

벤더가 발신 번호를 쓰려고 이 모듈을 `implementation`으로 의존하게 하지 않는다(채널 ↔ 벤더 순환). 벤더는 자기 yml에서 `${sms.sender-number}`를 참조하거나 `@Value`로 키를 읽는다. `aws-sns`가 발신 번호 지정이 필요해지면 같은 방식으로 추가한다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 인증 발급이 발송까지 원자적인 이유

**대상**: `backend/web-application/src/main/java/com/tastyhouse/application/phoneverification/service/SmsVerificationService.java`

`SmsVerificationService`는 같은 번호의 기존 미완료 인증을 함께 만료시키는 크로스 인스턴스 불변식을 갖고, 발송 누락을 막으려 `SmsSenderPort`를 직접 주입받는다.
