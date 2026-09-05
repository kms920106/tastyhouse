<!-- Parent: ../../AGENTS.md -->

# infrastructure:aws

AWS 벤더 어댑터 3종(S3 파일 저장 · SES 메일 발송 · SNS SMS 발송)을 소유하는 모듈(`java-library`). `infrastructure:external` 7모듈 분리(챕터 01) 산물이며, **채널별로 흩어져 있던 AWS 구현을 벤더 단위로 한데 모은 것**이 이 모듈의 존재 이유다.

## ⚠️ 어느 앱도 이 모듈을 의존하지 않는다

**4개 앱(web·admin·ceo·batch) 어느 것도 `implementation`/`runtimeOnly project(':infrastructure:aws')`를 선언하지 않는다(챕터 02 이후로는 `@Import`라는 별도 배선 단계 자체가 없다 — 의존 선언이 곧 활성화다).** 이유는 기본 provider가 전부 비-AWS이기 때문이다.

| 프로퍼티 | 기본값 | 소유 yml |
|---|---|---|
| `file.provider` | `firebase` | `infrastructure:file-storage`의 `application-file-storage.yml` (챕터 03 — 이전에는 `infrastructure:external`의 `application-external.yml`이었고 그 파일은 삭제됐다) |
| `mail.provider` | `javamail` | `infrastructure:messaging`의 `application-messaging.yml` |
| `sms.provider` | `solapi` | `infrastructure:messaging`의 `application-messaging.yml` |

게다가 `.env`에 `S3_BUCKET_NAME`이 없어 S3 경로는 설정값조차 채워지지 않는다. **AWS 경로가 어느 앱에서도 활성화되지 않으므로 모듈을 앱에 매달 이유가 없고**, 대신 `settings.gradle`에 포함되어 있어 `./gradlew build`가 **컴파일만 검증**한다.

### 한계 — 부패 방지 수단이 컴파일뿐이다

SES·SNS·S3 어댑터에 대한 테스트가 없다. 따라서 이 모듈이 지켜지는 범위는 "컴파일이 깨지지 않는다"까지이며, **런타임 동작(SDK 호출 형태·자격증명 로딩·리전 설정)은 검증되지 않는다.** 이 한계는 사용자 결정으로 수용된 것이며, AWS로 전환할 때는 아래 절차를 밟은 뒤 실제 기동·발송을 직접 확인해야 한다.

## AWS로 전환하는 절차 (챕터 03 — 파일 채널과 메일·SMS 채널의 경로가 갈린다)

**챕터 02의 auto-configuration 전환으로 `@Import` 단계가 사라졌고, 챕터 03의 스타터 신설로 파일 채널은 앱을 아예 건드리지 않게 됐다.** 채널마다 경로가 다르므로 아래 표에서 먼저 어느 쪽인지 확인한다.

| 채널 | 전환 대상 파일 | 앱 수정 |
|---|---|---|
| **파일 저장 (S3)** | `infrastructure/file-storage/`의 **2파일뿐** | **없음** |
| **메일 (SES) · SMS (SNS)** | web-api의 `build.gradle` + `application.yml` (기존 3단계) | 필요 |

### 파일 저장 → S3 (앱 무수정)

파일 저장은 챕터 03에서 스타터 `infrastructure:file-storage`가 벤더 선택을 흡수했다. 4개 앱은 `runtimeOnly project(':infrastructure:file-storage')` 한 줄과 `classpath:application-file-storage.yml` 한 줄만 갖고 있으므로, **앱 4개는 손대지 않는다.**

1. `infrastructure/file-storage/build.gradle`: `runtimeOnly project(':infrastructure:firebase')` → `runtimeOnly project(':infrastructure:aws')`
2. `infrastructure/file-storage/src/main/resources/application-file-storage.yml`: `spring.config.import`를 `classpath:application-aws.yml`로, `file.provider`를 `s3`로

상세는 `../file-storage/AGENTS.md`.

### 메일 → SES · SMS → SNS (기존 3단계 유지)

메일·SMS는 **web 전용 채널이라 스타터를 거치지 않는다.** 앱(web-api)이 `infrastructure:messaging`을 직접 의존하는 구조 그대로이므로 종전 절차를 밟는다.

1. **web-api `build.gradle`에 의존 추가** — `runtimeOnly project(':infrastructure:aws')`
2. **web-api `application.yml`의 `spring.config.import`에 `- classpath:application-aws.yml` 추가**
3. **provider 값 변경** — `mail.provider=ses` / `sms.provider=sns` 중 전환할 채널만

### 두 경로가 겹쳐도 충돌하지 않는다

파일은 Firebase로 두고 메일만 SES로 바꾸는 조합처럼 한쪽만 전환하는 경우, `infrastructure:aws` jar가 web의 `runtimeClasspath`에 실리면서 S3 어댑터도 함께 올라온다. **그래도 문제되지 않는다** — `S3FileStorage`·`S3FileStorageConfig`는 `@ConditionalOnProperty(file.provider=s3)`를 달고 있고 스타터가 `file.provider: firebase`를 유지하므로 S3 전략은 등록되지 않는다. `FileStorageStrategy` 빈은 Firebase 구현 하나뿐이라 중복 주입도 없다.

반대로 파일만 S3로 바꾸고 메일도 SES로 바꾼 경우에는 `application-aws.yml`이 **두 경로에서 import된다**(스타터의 중첩 import + web `application.yml`의 직접 import). 이것도 무해하다 — 같은 `classpath:` 리소스라 Spring이 같은 property source를 두 번 읽을 뿐 값이 달라지지 않는다.

**모듈 없이 provider만 바꾸면 기동 시 실패한다 — 조용한 오동작은 없다.** 예컨대 `file.provider=s3`로만 바꾸고 스타터의 gradle 의존을 그대로 두면 firebase 전략은 `@ConditionalOnProperty`로 등록되지 않고 S3 전략은 클래스패스에 없으므로, 코어의 `FileStoragePortAdapter`가 `FileStorageStrategy` 빈을 찾지 못해 컨텍스트 로딩이 실패한다. 메일·SMS도 같다(`MailSender`/`SmsSender` 빈 부재 → `MailDomainConfig`/`SmsDomainConfig`의 도메인 서비스 빈 생성 실패). 이 "실패로 드러남"이 provider 전환의 안전장치다.

## 무엇을 소유하는가

```
com.tastyhouse.external.aws/
├── AwsModuleAutoConfiguration.java   진입점 (챕터 02로 AwsModuleConfig에서 리네임 + @AutoConfiguration. 현재 어느 앱도 의존하지 않는다)
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

세 어댑터의 원래 패키지는 `external.file.s3`·`external.mail.ses`·`external.sms.sns`였다. 코어 `ExternalModuleAutoConfiguration`(구 `ExternalModuleConfig`)가 `external.file`을, `MessagingModuleAutoConfiguration`(구 `MessagingModuleConfig`)가 `external.mail`·`external.sms`를 스캔하므로 **그 하위에 남겨두면 코어·메시징을 import 한 앱에 AWS 빈이 동반 스캔된다.** 그래서 `external.aws.{s3,ses,sns}` 아래로 모았다(`../external/AGENTS.md`의 패키지 예외).

이 재배치는 `backend/CLAUDE.md`가 과거 "비채택 대안 (3) AWS 어댑터를 벤더 패키지로 모으기"로 기록했던 것을 **번복한 것**이다. 당시 근거("제공자 선택 축이 벤더가 아니라 채널이고 자격증명도 채널별로 따로여서 공유할 AWS 설정 코드가 없다")는 지금도 사실이다 — `mail.aws.ses.*`·`sms.aws.sns.*`·`file.aws.s3.*`가 각각 별도 자격증명을 갖는다. 바뀐 것은 **스캔 격리라는 새 요구**이며, 벤더 패키지는 그 수단이지 설정 공유를 위한 것이 아니다.

## 진입 설정과 스캔 범위

`AwsModuleAutoConfiguration`(챕터 02 — `@AutoConfiguration(proxyBeanMethods = false)`, `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록)이 `@ComponentScan("com.tastyhouse.external.aws")` + `@EnableConfigurationProperties(S3FileStorageProperties.class)`를 갖는다. SES·SNS는 Properties record 없이 `@Value`/설정 클래스로 값을 읽으므로 등록 대상이 S3 하나뿐이다.

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
- **채널을 부분 전환할 수 있다**: 세 어댑터의 조건 프로퍼티가 각각 다르므로 메일만 SES로 바꾸고 파일은 Firebase에 두는 조합이 가능하다. 다만 챕터 03 이후 **의존을 선언하는 위치가 채널별로 다르다** — 파일은 스타터 `infrastructure:file-storage`, 메일·SMS는 web-api다(위 전환 절차 표). 두 경로가 동시에 aws를 끌어와도 충돌하지 않는다(위 "두 경로가 겹쳐도 충돌하지 않는다").
