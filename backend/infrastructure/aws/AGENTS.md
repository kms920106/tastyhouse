<!-- Parent: ../../AGENTS.md -->

# infrastructure:aws

AWS 벤더 어댑터 3종(S3 파일 저장 · SES 메일 발송 · SNS SMS 발송)을 소유하는 모듈(`java-library`). `infrastructure:external` 7모듈 분리(챕터 01) 산물이며, **채널별로 흩어져 있던 AWS 구현을 벤더 단위로 한데 모은 것**이 이 모듈의 존재 이유다.

## ⚠️ 어느 앱도 이 모듈을 의존하지 않는다

**4개 앱(web·admin·ceo·batch) 어느 것도 `implementation project(':infrastructure:aws')`를 선언하지 않고 `@Import(AwsModuleConfig.class)`도 하지 않는다.** 이유는 기본 provider가 전부 비-AWS이기 때문이다.

| 프로퍼티 | 기본값 | 소유 yml |
|---|---|---|
| `file.provider` | `firebase` | `infrastructure:external`의 `application-external.yml` |
| `mail.provider` | `javamail` | `infrastructure:messaging`의 `application-messaging.yml` |
| `sms.provider` | `solapi` | `infrastructure:messaging`의 `application-messaging.yml` |

게다가 `.env`에 `S3_BUCKET_NAME`이 없어 S3 경로는 설정값조차 채워지지 않는다. **AWS 경로가 어느 앱에서도 활성화되지 않으므로 모듈을 앱에 매달 이유가 없고**, 대신 `settings.gradle`에 포함되어 있어 `./gradlew build`가 **컴파일만 검증**한다.

### 한계 — 부패 방지 수단이 컴파일뿐이다

SES·SNS·S3 어댑터에 대한 테스트가 없다. 따라서 이 모듈이 지켜지는 범위는 "컴파일이 깨지지 않는다"까지이며, **런타임 동작(SDK 호출 형태·자격증명 로딩·리전 설정)은 검증되지 않는다.** 이 한계는 사용자 결정으로 수용된 것이며, AWS로 전환할 때는 아래 절차를 밟은 뒤 실제 기동·발송을 직접 확인해야 한다.

## AWS로 전환하는 절차

전환은 **네 가지를 함께** 해야 한다. 하나라도 빠지면 전환이 성립하지 않는다.

1. **앱 `build.gradle`에 의존 추가** — `implementation project(':infrastructure:aws')`
2. **`{Xxx}Application.java`에 `@Import(AwsModuleConfig.class)` 추가**
3. **앱 `application.yml`의 `spring.config.import`에 `- classpath:application-aws.yml` 추가**
4. **provider 값 변경** — `file.provider=s3` / `mail.provider=ses` / `sms.provider=sns` 중 전환할 채널만

**모듈 없이 provider만 바꾸면 기동 시 실패한다 — 조용한 오동작은 없다.** 예컨대 `file.provider=s3`로만 바꾸면 firebase 전략은 `@ConditionalOnProperty`로 등록되지 않고 S3 전략은 클래스패스에 없으므로, 코어의 `FileStoragePortAdapter`가 `FileStorageStrategy` 빈을 찾지 못해 컨텍스트 로딩이 실패한다. 메일·SMS도 같다(`MailSender`/`SmsSender` 빈 부재 → `MailDomainConfig`/`SmsDomainConfig`의 도메인 서비스 빈 생성 실패). 이 "실패로 드러남"이 provider 전환의 안전장치다.

## 무엇을 소유하는가

```
com.tastyhouse.external.aws/
├── AwsModuleConfig.java              진입점 (현재 어느 앱도 import 하지 않는다)
├── s3/
│   ├── S3FileStorage.java            FileStorageStrategy 구현 @ConditionalOnProperty(file.provider=s3)
│   ├── S3FileStorageConfig.java      S3 클라이언트 빈    @ConditionalOnProperty(file.provider=s3)
│   └── S3FileStorageProperties.java  file.aws.s3.*
├── ses/
│   ├── SesConfig.java                SesClient 빈       @ConditionalOnProperty(mail.provider=ses)
│   └── SesMailSender.java            도메인 포트 MailSender 구현
└── sns/
    ├── SnsConfig.java                SnsClient 빈       @ConditionalOnProperty(sms.provider=sns)
    └── SnsSmsSender.java             도메인 포트 SmsSender 구현
```

### 패키지가 바뀐 이유

세 어댑터의 원래 패키지는 `external.file.s3`·`external.mail.ses`·`external.sms.sns`였다. 코어 `ExternalModuleConfig`가 `external.file`을, `MessagingModuleConfig`가 `external.mail`·`external.sms`를 스캔하므로 **그 하위에 남겨두면 코어·메시징을 import 한 앱에 AWS 빈이 동반 스캔된다.** 그래서 `external.aws.{s3,ses,sns}` 아래로 모았다(`../external/AGENTS.md`의 패키지 예외).

이 재배치는 `backend/CLAUDE.md`가 과거 "비채택 대안 (3) AWS 어댑터를 벤더 패키지로 모으기"로 기록했던 것을 **번복한 것**이다. 당시 근거("제공자 선택 축이 벤더가 아니라 채널이고 자격증명도 채널별로 따로여서 공유할 AWS 설정 코드가 없다")는 지금도 사실이다 — `mail.aws.ses.*`·`sms.aws.sns.*`·`file.aws.s3.*`가 각각 별도 자격증명을 갖는다. 바뀐 것은 **스캔 격리라는 새 요구**이며, 벤더 패키지는 그 수단이지 설정 공유를 위한 것이 아니다.

## 진입 설정과 스캔 범위

`AwsModuleConfig`가 `@ComponentScan("com.tastyhouse.external.aws")` + `@EnableConfigurationProperties(S3FileStorageProperties.class)`를 갖는다. SES·SNS는 Properties record 없이 `@Value`/설정 클래스로 값을 읽으므로 등록 대상이 S3 하나뿐이다.

## yml — `application-aws.yml` (import 하는 앱이 없다)

`file.aws.s3.*`(bucket-name·region·base-url), `mail.aws.ses.*`(access-key·secret-key·region), `sms.aws.sns.*`(access-key·secret-key·region) 세 블록을 담는다. **현재 이 파일을 `spring.config.import` 하는 앱은 없다** — 전환 시 위 절차 3번으로 추가한다. 파일 첫머리에 그 사실이 주석으로 적혀 있다.

**`${S3_BUCKET_NAME}` 미해결 플레이스홀더는 부팅을 막지 않는다.** `@ConfigurationProperties` 바인딩은 Binder가 수행하는데, 해당 빈이 조건(`file.provider=s3`)으로 등록되지 않으면 바인딩 자체가 일어나지 않는다. 이것이 `.env`에 `S3_BUCKET_NAME`이 없는데도 4개 앱이 정상 기동하는 이유다.

## Dependencies

### Internal
- `infrastructure:external` (implementation) — `FileStorageStrategy` SPI, `ExternalApiException`/`ExternalApiErrorCode`
- `infrastructure:messaging` (implementation) — **`MailProperties`(`mail.sender-address`) 하나 때문이다.** `SesMailSender`가 발신자 주소를 그 record에서 읽는다. 채널 공통 설정(발신자 주소·발신 번호)은 벤더가 아니라 채널 모듈이 소유하므로, 벤더 모듈이 채널 모듈을 의존하는 이 방향이 정상이다
- `domain-module` (implementation) — 구현하는 `MailSender`·`SmsSender` 포트와 예외 계약

### External
- `software.amazon.awssdk:ses` · `software.amazon.awssdk:sns` · `io.awspring.cloud:spring-cloud-aws-s3`
- **`dependencyManagement { imports { mavenBom 'io.awspring.cloud:spring-cloud-aws-dependencies:3.1.1' } }` — 이 BOM은 원래 루트 `build.gradle`의 `subprojects` 클로저 안에 있어 전 모듈에 적용됐으나, AWS SDK를 쓰는 모듈이 여기 하나뿐이라 이 모듈로 이관했다.** 확인 방법: `grep -rn 'awspring\|awssdk' --include=build.gradle backend`가 이 모듈만 내놓아야 한다.

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **jar 실측으로 미포함을 확인한다**: 4개 앱 fat jar 어디에도 `aws-0.0.1-SNAPSHOT.jar`·`ses-`·`sns-`·`spring-cloud-aws-*`가 들어 있으면 안 된다. `unzip -l {앱}/build/libs/{앱}-0.0.1-SNAPSHOT.jar | grep BOOT-INF/lib/`로 확인한다.
- **채널을 부분 전환할 수 있다**: 세 어댑터의 조건 프로퍼티가 각각 다르므로 메일만 SES로 바꾸고 파일은 Firebase에 두는 조합이 가능하다. 이때도 모듈 의존·`@Import`·yml import는 모듈 단위로 한 번만 하면 된다.
