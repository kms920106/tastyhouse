<!-- Parent: ../../AGENTS.md -->

# infrastructure:aws-ses

AWS SES 메일 발송 어댑터를 소유하는 모듈(`java-library`). 도메인 포트 `MailSender`를 `SesMailSender`가 구현한다. 메일 채널의 기본 구현(JavaMail)은 `infrastructure:messaging`에 있고, 이 모듈은 그 AWS 대안이다.

## ⚠️ 어느 앱도 이 모듈을 의존하지 않는다

기본값이 `mail.provider: javamail`(`infrastructure:messaging`의 `application-messaging.yml`)이라 SES 경로가 활성화되지 않는다. `settings.gradle` 포함으로 **컴파일만 검증**되며, 어댑터 테스트가 없어 런타임 동작(SDK 호출·자격증명·리전)은 검증되지 않는다. 사용자 결정으로 수용된 한계다.

## 이 모듈이 따로 있는 이유 (3분할, 2026-09-26)

과거 S3·SES·SNS가 `infrastructure:aws` 한 모듈이었고, 그 탓에 파일 저장을 S3로 바꾸면 SES용 의존(external·messaging)이 4앱 전부에 실리는 결함이 있었다(상세: `../aws-s3/AGENTS.md` §이 모듈이 따로 있는 이유). 채널마다 모듈을 두어 각 활성화 경로가 자기 의존만 끌고 가게 했다. SES와 SNS는 활성화 지점(web-api)이 같지만, 메일·SMS를 서로 독립적으로 AWS로 옮길 수 있고 그때 web fat jar에 미사용 SDK가 실리지 않도록 이것도 나눴다.

## SES로 전환하는 절차 (web-api 3단계)

메일은 web 전용 채널이라 스타터를 거치지 않는다.

1. `web-api/build.gradle`에 `runtimeOnly project(':infrastructure:aws-ses')`
2. `web-api/src/main/resources/application.yml`의 `spring.config.import`에 `- classpath:application-aws-ses.yml`
3. `mail.provider=ses` (`application-messaging.yml` 값 변경 또는 환경변수)

`.env`에는 `AWS_SES_ACCESS_KEY`·`AWS_SES_SECRET_KEY`가 이미 있다.

**모듈 없이 provider만 바꾸면 기동 시 실패한다.** `JavaMailAdapter`는 조건으로 빠지고 SES 구현은 클래스패스에 없어, `MailDomainConfig`가 `MailSender` 빈을 찾지 못해 도메인 서비스 빈 생성이 실패한다. 이 "실패로 드러남"이 전환의 안전장치다.

## 무엇을 소유하는가

```
com.tastyhouse.external.aws.ses/
├── AwsSesModuleAutoConfiguration.java  @AutoConfiguration + @ComponentScan(이 패키지)
├── SesConfig.java                      SesClient 빈 + MailSender 빈   @ConditionalOnProperty(mail.provider=ses)
└── SesMailSender.java                  MailSender 구현 (POJO — SesConfig가 @Bean으로 등록)
```

`@ConfigurationProperties` record가 없어 `@EnableConfigurationProperties`를 달지 않는다. `SesConfig`가 `@Value`로 `mail.aws.ses.*`를 읽는다.

## yml — `application-aws-ses.yml`

`mail.aws.ses.access-key` · `secret-key` · `region`. import하는 앱은 없다 — 전환 절차 2번으로 추가한다.

## Dependencies

### Internal
- **`infrastructure:restclient`(구 `infrastructure:http-client`) 의존이 없다.** 과거에는 그 코어의 `ExternalApiException`/`ExternalApiErrorCode.MAIL_SEND_FAILED`를 쓰느라 의존했으나, 그 예외 계약 자체가 완전히 삭제되고 상수(`MAIL_SEND_FAILED`)가 도메인 `ErrorCode`로 이관되면서 이 모듈은 도메인만 있으면 충분해졌다. `SesMailSender`는 발송 실패를 `new BusinessException(ErrorCode.MAIL_SEND_FAILED[, cause])`로 직접 던진다. **다만 `infrastructure:messaging`을 여전히 의존하므로(아래) 런타임에는 `restclient`가 messaging을 통해 전이로 실린다** — compileClasspath에는 없다.
- `infrastructure:messaging` (implementation) — **`MailProperties`(`mail.sender-address`) 하나 때문이다.** 발신자 주소는 벤더가 아니라 채널 모듈이 소유하므로 벤더 → 채널 방향이 정상이다. SNS 모듈은 이 의존이 없다.
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

원래 패키지 `external.mail.ses`로 되돌리면 `infrastructure:messaging`의 `@ComponentScan`에 동반 스캔되어 messaging을 받은 앱에 SES 빈이 딸려 올라온다. `com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 통째 스캔에 걸려 admin·ceo·batch 부팅이 깨진다.

### 진입 설정은 자기 하위 패키지만 스캔한다

**대상**: `backend/infrastructure/aws-ses/src/main/java/com/tastyhouse/external/aws/ses/AwsSesModuleAutoConfiguration.java`

`com.tastyhouse.external.aws` 루트를 스캔하지 않는다. 형제 모듈(`aws-s3`·`aws-sns`)이 같은 클래스패스에 있으면 그 빈까지 이 설정이 등록하게 된다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 조건부 전략 배선은 반증 테스트로 확인한다

**대상**: `backend/infrastructure/aws-ses/src/main/java/com/tastyhouse/external/aws/ses/SesConfig.java` (`@ConditionalOnProperty(mail.provider=ses)`)

기동 성공이 곧 SES가 선택됐다는 증거가 아니다. 전환 후 검증은 틀린 provider 값으로 실패를 확인하는 반증 방향으로 한다(위 "모듈 없이 provider만 바꾸면 기동 시 실패한다").
