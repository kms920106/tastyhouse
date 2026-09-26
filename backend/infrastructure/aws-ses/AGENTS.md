<!-- Parent: ../../AGENTS.md -->

# infrastructure:aws-ses

AWS SES 메일 발송 어댑터를 소유하는 모듈(`java-library`). 도메인 포트 `MailSender`를 `SesMailSender`가 구현한다. 메일 채널의 기본 벤더(JavaMail)는 `infrastructure:javamail`이고, 이 모듈은 그 AWS 대안이다. 조립은 채널 모듈 `infrastructure:mail`이 한다.

## ⚠️ 어느 앱도 이 모듈을 의존하지 않는다

기본값이 `mail.provider: javamail`(`infrastructure:mail`의 `application-mail.yml`)이고 채널 모듈이 javamail을 조립하므로 SES 경로가 활성화되지 않는다. `settings.gradle` 포함으로 **컴파일만 검증**되며, 어댑터 테스트가 없어 런타임 동작(SDK 호출·자격증명·리전)은 검증되지 않는다. 사용자 결정으로 수용된 한계다.

## 이 모듈이 따로 있는 이유 (3분할, 2026-09-26)

과거 S3·SES·SNS가 `infrastructure:aws` 한 모듈이었고, 그 탓에 파일 저장을 S3로 바꾸면 SES용 의존(당시 external·messaging)이 4앱 전부에 실리는 결함이 있었다(상세: `../aws-s3/AGENTS.md` §이 모듈이 따로 있는 이유). 채널마다 모듈을 두어 각 활성화 경로가 자기 의존만 끌고 가게 했다. SES와 SNS는 활성화 지점(web-api)이 같지만, 메일·SMS를 서로 독립적으로 AWS로 옮길 수 있고 그때 web fat jar에 미사용 SDK가 실리지 않도록 이것도 나눴다.

## SES로 전환하는 절차 (채널 모듈 2파일)

**web-api를 건드리지 않는다.** 채널 모듈 `infrastructure:mail`의 `build.gradle`(`runtimeOnly` 대상을 `:infrastructure:aws-ses`로)과 `application-mail.yml`(import를 `classpath:application-aws-ses.yml`로, `mail.provider: ses`)만 바꾼다. 상세는 `../mail/AGENTS.md`의 "벤더 전환 절차". 과거 messaging 시절에는 web-api의 `build.gradle`·`application.yml`·provider 3곳을 고치는 절차였다(4분할, 2026-09-26로 변경).

`.env`에는 `AWS_SES_ACCESS_KEY`·`AWS_SES_SECRET_KEY`가 이미 있다.

**모듈 없이 provider만 바꾸면 기동 시 실패한다.** `JavaMailAdapter`는 조건으로 빠지고 SES 구현은 클래스패스에 없어, `MailDomainConfig`가 `MailSender` 빈을 찾지 못해 도메인 서비스 빈 생성이 실패한다. 이 "실패로 드러남"이 전환의 안전장치다.

## 무엇을 소유하는가

```
com.tastyhouse.external.aws.ses/
├── AwsSesModuleAutoConfiguration.java  @AutoConfiguration + @ComponentScan(이 패키지)
├── SesConfig.java                      SesClient 빈 + MailSender 빈   @ConditionalOnProperty(mail.provider=ses)
└── SesMailSender.java                  MailSender 구현 (POJO — SesConfig가 @Bean으로 등록)
```

`@ConfigurationProperties` record가 없어 `@EnableConfigurationProperties`를 달지 않는다. `SesConfig`가 `@Value`로 `mail.aws.ses.*`와 채널 값 `mail.sender-address`를 읽는다.

## yml — `application-aws-ses.yml`

`mail.aws.ses.access-key` · `secret-key` · `region`. 지금은 아무도 import하지 않는다 — 전환 시 채널의 `application-mail.yml`이 중첩 import한다.

## Dependencies

### Internal
- **`infrastructure:restclient`(구 `infrastructure:http-client`) 의존이 없다.** 과거에는 그 코어의 `ExternalApiException`/`ExternalApiErrorCode.MAIL_SEND_FAILED`를 쓰느라 의존했으나, 그 예외 계약 자체가 완전히 삭제되고 상수(`MAIL_SEND_FAILED`)가 도메인 `ErrorCode`로 이관되면서 이 모듈은 도메인만 있으면 충분해졌다. `SesMailSender`는 발송 실패를 `new BusinessException(ErrorCode.MAIL_SEND_FAILED[, cause])`로 직접 던진다. 4분할 전에는 messaging을 통해 런타임에 `restclient`가 전이로 실렸으나, 그 의존이 사라져 **직접·전이 모두 없다**.
- **채널 모듈(`infrastructure:mail`)을 의존하지 않는다.** 과거 `infrastructure:messaging`을 `MailProperties`(`mail.sender-address`) 하나 때문에 의존했으나, 채널 모듈이 이 모듈을 `runtimeOnly`로 조립하는 구조에서는 순환이 되므로 `SesConfig`가 `@Value("${mail.sender-address}")`로 키만 읽는다(`../mail/AGENTS.md` 봉인 목록). 결과적으로 의존은 domain + SDK뿐이라 `aws-s3`·`aws-sns`와 동형이다.
- `domain` (implementation) — `MailSender` 포트 + `ErrorCode`(`MAIL_SEND_FAILED`)·`BusinessException`

### External
- `software.amazon.awssdk:ses` — 버전은 `implementation platform('software.amazon.awssdk:bom:2.21.46')`이 고정한다. 3분할 전 spring-cloud-aws BOM이 고정하던 값과 같아 SDK 버전은 바뀌지 않았다. `dependencyManagement` 블록이 아니라 `platform()`인 이유는 소비 앱으로 전파돼야 하기 때문이다(`../aws-s3/AGENTS.md` §Dependencies — 블록 방식은 web-api에서 버전 미해석 FAILED가 된다).

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **jar 실측으로 미포함을 확인한다**: 4개 앱 fat jar에 `aws-ses-0.0.1-SNAPSHOT.jar`·`ses-2.*.jar`가 있으면 안 된다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.external.aws.ses` 봉인

**대상**: `backend/infrastructure/aws-ses/src/main/java/com/tastyhouse/external/aws/ses/`

원래 패키지 `external.mail.ses`로 되돌리면 채널 모듈 `infrastructure:mail`의 `@ComponentScan("com.tastyhouse.external.mail")`에 동반 스캔되어 mail을 받은 앱에 SES 빈이 딸려 올라온다. `com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 통째 스캔에 걸려 admin·ceo·batch 부팅이 깨진다.

### 진입 설정은 자기 하위 패키지만 스캔한다

**대상**: `backend/infrastructure/aws-ses/src/main/java/com/tastyhouse/external/aws/ses/AwsSesModuleAutoConfiguration.java`

`com.tastyhouse.external.aws` 루트를 스캔하지 않는다. 형제 모듈(`aws-s3`·`aws-sns`)이 같은 클래스패스에 있으면 그 빈까지 이 설정이 등록하게 된다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 조건부 전략 배선은 반증 테스트로 확인한다

**대상**: `backend/infrastructure/aws-ses/src/main/java/com/tastyhouse/external/aws/ses/SesConfig.java` (`@ConditionalOnProperty(mail.provider=ses)`)

기동 성공이 곧 SES가 선택됐다는 증거가 아니다. 전환 후 검증은 틀린 provider 값으로 실패를 확인하는 반증 방향으로 한다(위 "모듈 없이 provider만 바꾸면 기동 시 실패한다").
