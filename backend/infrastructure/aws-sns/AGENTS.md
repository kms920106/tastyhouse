<!-- Parent: ../../AGENTS.md -->

# infrastructure:aws-sns

AWS SNS SMS 발송 어댑터를 소유하는 모듈(`java-library`). 도메인 포트 `SmsSender`를 `SnsSmsSender`가 구현한다. SMS 채널의 기본 구현(Solapi)은 `infrastructure:messaging`에 있고, 이 모듈은 그 AWS 대안이다.

## ⚠️ 어느 앱도 이 모듈을 의존하지 않는다

기본값이 `sms.provider: solapi`(`infrastructure:messaging`의 `application-messaging.yml`)라 SNS 경로가 활성화되지 않는다. `settings.gradle` 포함으로 **컴파일만 검증**되며, 어댑터 테스트가 없어 런타임 동작은 검증되지 않는다. 사용자 결정으로 수용된 한계다.

## 이 모듈이 따로 있는 이유 (3분할, 2026-09-26)

과거 S3·SES·SNS가 `infrastructure:aws` 한 모듈이었고, 활성화 경로가 다른 채널을 한 모듈에 둔 탓에 결함이 있었다(상세: `../aws-s3/AGENTS.md` §이 모듈이 따로 있는 이유). SMS를 메일과 독립적으로 AWS로 옮길 수 있게 SES와도 나눴다.

## SNS로 전환하는 절차 (web-api 3단계)

1. `web-api/build.gradle`에 `runtimeOnly project(':infrastructure:aws-sns')`
2. `web-api/src/main/resources/application.yml`의 `spring.config.import`에 `- classpath:application-aws-sns.yml`
3. `sms.provider=sns`

`.env`에는 `AWS_SNS_ACCESS_KEY`·`AWS_SNS_SECRET_KEY`가 이미 있다.

**모듈 없이 provider만 바꾸면 기동 시 실패한다.** `SolapiSmsClient`는 조건으로 빠지고 SNS 구현은 클래스패스에 없어, `SmsDomainConfig`가 `SmsSender` 빈을 찾지 못한다.

## 무엇을 소유하는가

```
com.tastyhouse.external.aws.sns/
├── AwsSnsModuleAutoConfiguration.java  @AutoConfiguration + @ComponentScan(이 패키지)
├── SnsConfig.java                      SnsClient 빈 + SmsSender 빈   @ConditionalOnProperty(sms.provider=sns)
└── SnsSmsSender.java                   SmsSender 구현 (POJO — SnsConfig가 @Bean으로 등록)
```

## yml — `application-aws-sns.yml`

`sms.aws.sns.access-key` · `secret-key` · `region`. import하는 앱은 없다 — 전환 절차 2번으로 추가한다.

## Dependencies

### Internal
- `infrastructure:external` (implementation) — `ExternalApiException`/`ExternalApiErrorCode.SMS_SEND_API_ERROR`·`SMS_SEND_FAILED`
- `domain` (implementation) — `SmsSender` 포트

**`infrastructure:messaging`을 의존하지 않는다.** 옛 `infrastructure:aws`가 messaging을 가졌던 것은 SES의 `MailProperties` 때문이었고, SNS는 발신 번호를 읽지 않는다(`SnsConfig`가 `sms.aws.sns.*`만 읽는다). 발신 번호 지정이 필요해지면 `SmsProperties`를 쓰기 위해 그때 의존을 추가한다.

### External
- `software.amazon.awssdk:sns` — 버전은 `implementation platform('software.amazon.awssdk:bom:2.21.46')`이 고정한다. 3분할 전 spring-cloud-aws BOM이 고정하던 값과 같아 SDK 버전은 바뀌지 않았다. `dependencyManagement` 블록이 아니라 `platform()`인 이유는 소비 앱으로 전파돼야 하기 때문이다(`../aws-s3/AGENTS.md` §Dependencies — 블록 방식은 web-api에서 버전 미해석 FAILED가 된다).

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **jar 실측으로 미포함을 확인한다**: 4개 앱 fat jar에 `aws-sns-0.0.1-SNAPSHOT.jar`·`sns-2.*.jar`가 있으면 안 된다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.external.aws.sns` 봉인

**대상**: `backend/infrastructure/aws-sns/src/main/java/com/tastyhouse/external/aws/sns/`

원래 패키지 `external.sms.sns`로 되돌리면 `infrastructure:messaging`의 `@ComponentScan`에 동반 스캔된다. `com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 통째 스캔에 걸려 admin·ceo·batch 부팅이 깨진다.

### 진입 설정은 자기 하위 패키지만 스캔한다

**대상**: `backend/infrastructure/aws-sns/src/main/java/com/tastyhouse/external/aws/sns/AwsSnsModuleAutoConfiguration.java`

`com.tastyhouse.external.aws` 루트를 스캔하지 않는다. 형제 모듈이 같은 클래스패스에 있으면 그 빈까지 등록하게 된다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 조건부 전략 배선은 반증 테스트로 확인한다

**대상**: `backend/infrastructure/aws-sns/src/main/java/com/tastyhouse/external/aws/sns/SnsConfig.java` (`@ConditionalOnProperty(sms.provider=sns)`)

기동 성공이 곧 SNS가 선택됐다는 증거가 아니다. 전환 후 검증은 틀린 provider 값으로 실패를 확인하는 반증 방향으로 한다.
