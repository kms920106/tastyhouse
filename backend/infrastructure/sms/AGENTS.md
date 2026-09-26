<!-- Parent: ../../AGENTS.md -->

# infrastructure:sms

SMS **채널 모듈**(`java-library`). SMS 인증 도메인 서비스 빈 등록(`SmsDomainConfig`), 채널 설정(`sms.provider`·`sms.sender-number`), 기본 벤더 조립(`runtimeOnly infrastructure:solapi`)을 소유한다. 포트 `SmsSender`의 구현은 벤더 모듈(`infrastructure:solapi` 기본, `infrastructure:aws-sns` 대안)에 있다.

`infrastructure:messaging` 4분할(2026-09-26)로 신설됐다. 채널 모듈의 정의와 파일 저장 스타터와의 차이는 `../mail/AGENTS.md`의 "채널 모듈과 파일 저장 스타터의 차이"와 같다.

## 무엇을 소유하는가

```
backend/infrastructure/sms/
  build.gradle                                       domain + runtimeOnly solapi
  src/main/java/com/tastyhouse/external/sms/
    SmsModuleAutoConfiguration.java                  @AutoConfiguration + @ComponentScan("com.tastyhouse.external.sms")
    config/SmsDomainConfig.java                      SmsVerificationService @Bean
  src/main/resources/
    application-sms.yml                              sms.provider · sms.sender-number + 벤더 yml 중첩 import
    META-INF/spring/...AutoConfiguration.imports     자기 등록
```

## 어느 앱이 의존하는가

**web-api 하나뿐이다**(`runtimeOnly`). `SmsDomainConfig`에 조건이 없으므로 다른 앱에 추가하면 그 즉시 `SmsSender`를 요구한다.

## 벤더 전환 절차 (Solapi → SNS)

**web-api를 건드리지 않는다.** 이 모듈의 두 파일만 바꾼다.

1. `build.gradle`: `runtimeOnly project(':infrastructure:solapi')` → `runtimeOnly project(':infrastructure:aws-sns')`
2. `application-sms.yml`: import를 `classpath:application-aws-sns.yml`로, `sms.provider: sns`로
3. `.env`에 `AWS_SNS_ACCESS_KEY`·`AWS_SNS_SECRET_KEY`(이미 있다)

**벤더 모듈 없이 provider만 바꾸면 기동 시 실패한다** — `SolapiSmsClient`는 조건으로 빠지고 SNS 구현이 없어 `SmsDomainConfig`가 `SmsSender` 빈을 찾지 못한다. 전환 검증은 반증 방향으로 한다.

## yml — `application-sms.yml`

`sms.provider`(`solapi` | `sns`)와 `sms.sender-number`(`${SMS_SENDER_NUMBER}`)를 소유하고, 벤더 yml(`classpath:application-solapi.yml`)을 중첩 import로 로딩한다. 발신 번호의 주인은 채널이다 — Solapi yml은 `sender-number: ${sms.sender-number}`로 이 값을 참조한다. 과거 `sms.*`를 바인딩하던 `SmsProperties` record는 읽는 코드가 없어 4분할과 함께 삭제됐다.

## Dependencies

- `domain` (implementation) — `SmsDomainConfig`가 등록하는 `SmsVerificationService`와 그 생성자가 요구하는 `SmsVerificationRepository`·`SmsSender`·`DomainEventPublisher`
- `infrastructure:solapi` (runtimeOnly) — 기본 벤더. compileClasspath에는 없다

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **발송 실패는 인증 레코드 저장을 롤백시킨다.** 상세는 `backend/CLAUDE.md`의 "인증코드 발송은 발급과 원자적으로 수행하는 규칙".
- **`@RateLimit keyPrefix`는 개명하지 않는다** — Redis 카운터 키다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 발신 번호는 클래스가 아니라 프로퍼티 키로 공유한다

**대상**: `backend/infrastructure/sms/src/main/resources/application-sms.yml` → `sms.sender-number`
**대상**: `backend/infrastructure/solapi/src/main/resources/application-solapi.yml` → `sms.solapi.sender-number`

벤더가 발신 번호를 쓰려고 이 모듈을 `implementation`으로 의존하게 하지 않는다(채널 ↔ 벤더 순환). 벤더는 자기 yml에서 `${sms.sender-number}`를 참조하거나 `@Value`로 키를 읽는다. `aws-sns`가 발신 번호 지정이 필요해지면 같은 방식으로 추가한다.

### 진입 설정은 자기 패키지만 스캔한다, 벤더는 형제 패키지에 둔다

**대상**: `backend/infrastructure/sms/src/main/java/com/tastyhouse/external/sms/SmsModuleAutoConfiguration.java`

스캔 범위는 `com.tastyhouse.external.sms`다. 벤더는 `external.solapi`·`external.aws.sns`처럼 형제 패키지에 둔다. `com.tastyhouse.infrastructure` 아래로 옮기면 persistence 통째 스캔에 걸린다.

### 새 POJO 도메인 서비스는 `@Bean`을 손으로 추가한다

**대상**: `backend/infrastructure/sms/src/main/java/com/tastyhouse/external/sms/config/SmsDomainConfig.java`

sms 컨텍스트에 POJO 도메인 서비스를 추가하면 이 설정에 `@Bean` 메서드를 함께 추가해야 한다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 인증 발급이 발송까지 원자적인 이유

**대상**: `backend/infrastructure/sms/src/main/java/com/tastyhouse/external/sms/config/SmsDomainConfig.java` → `SmsVerificationService` 빈

`SmsVerificationService`는 같은 번호의 기존 미완료 인증을 함께 만료시키는 크로스 인스턴스 불변식을 갖고, 발송 누락을 막으려 `SmsSender`를 직접 주입받는다. `SmsVerificationEventListener`는 발송 포트를 주입받지 않으므로 persistence에 잔류한다.
