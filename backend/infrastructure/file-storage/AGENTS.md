<!-- Parent: ../../AGENTS.md -->

# infrastructure:file-storage

파일 저장 **기능 한 벌**을 묶어 앱에 노출하는 Spring Boot starter 형태의 조립 모듈(`java-library`). 챕터 03에서 신설됐다. **자바 코드가 없다** — 의존 선언(`build.gradle`)과 설정 진입점(`application-file-storage.yml`) 둘뿐이다.

## 왜 있는가

4개 앱(web·admin·ceo·batch)은 파일 저장을 유스케이스 호출로만 쓴다. 컴파일 시점에 어떤 벤더인지 알지 못하고(`runtimeOnly`), 알 이유도 없다. 그런데 챕터 03 이전에는 벤더 선택이 앱마다 **두 곳**(`build.gradle`의 external·firebase 2줄, `application.yml`의 import 2줄)에 드러나 있어 **같은 결정이 8곳에 복제**돼 있었다. 벤더를 바꾸려면 8곳을 고쳐야 했다.

이 모듈이 벤더 구현(`infrastructure:firebase`)을 노출하므로, 앱은 **"파일을 저장한다"** 까지만 알고 **"Firebase로"** 는 모른다. `spring-boot-starter-data-redis`가 Lettuce를 고르는 것과 같은 형태다. 조립 대상은 firebase 하나다 — 벤더 구현이 `application-core`의 포트 `FileStoragePort`(`file/port/out`)를 직접 구현하므로 중간 SPI 모듈이 필요 없다(`../restclient/AGENTS.md`의 파일 저장 SPI 삭제 절). ([결정 0021](#decision-0021))

**조립 대상이 하나여도 이 스타터를 유지한다.** 앱이 firebase를 직접 선언하도록 되돌리면 두 가지를 잃는다.

- **앱이 벤더 이름을 알게 된다** — 4개 앱의 `build.gradle`·`application.yml`에 "Firebase"가 다시 등장한다. 앱이 모르게 하는 것이 이 모듈의 존재 이유이고, 그 가치는 조립 대상 개수와 무관하다.
- **벤더 전환이 이 모듈 두 파일로 끝나지 않는다** — 아래 전환 절차가 다시 4앱 × 2곳 = 8곳 수정이 된다.

## 구성

```
backend/infrastructure/file-storage/
  build.gradle                                  runtimeOnly firebase (한 줄)
  AGENTS.md
  src/main/resources/application-file-storage.yml   file.provider + 벤더 yml 중첩 import
```

`compileJava`는 NO-SOURCE로 넘어가고 jar에는 yml만 실린다(비어 있지 않으므로 리소스 로딩에 문제없음).

**전이 메커니즘**: `runtimeOnly`는 `runtimeElements` 변형에 포함되므로, 이 모듈을 `runtimeOnly`로 의존하는 앱의 `runtimeClasspath`에 firebase가 전이로 실린다. compileClasspath에는 둘 다 나타나지 않는다(헥사고날 강제 유지). **코어(`infrastructure:restclient`)는 이 경로로 실리지 않는다** — 파일 저장만 쓰는 admin-api·ceo-api의 런타임 클래스패스에서는 코어 모듈이 빠진다(webflux는 애초에 리포 전체에서 제거돼 있다). web·batch는 다른 어댑터 모듈을 경유해 코어를 계속 받는다.

**중첩 `spring.config.import`**: `application-file-storage.yml`이 다시 `classpath:application-firebase.yml`을 import 한다. config data 파일 안에서 동작하며, `classpath:` 리소스는 다른 jar(firebase)에 있어도 해석된다(분리 전 허브 `application-external.yml`이 같은 방식으로 6개를 import했던 선례). `application-firebase.yml`의 configtree import(`optional:configtree:${SECRETS_DIR}/`)는 그대로 firebase 모듈이 소유한다.

**순환 없음**: file-storage → firebase, firebase → application-core. 이 모듈을 의존하는 것은 앱 4개뿐이다. SPI를 이 모듈로 옮기는 안은 firebase가 이 모듈을 되받아야 해 file-storage ↔ firebase 순환이 되므로 채택하지 않는다(`../restclient/AGENTS.md`의 파일 저장 SPI 삭제 절).

## 벤더 전환 절차 (Firebase → S3)

**앱을 건드리지 않는다.** 이 모듈의 두 파일에서 세 곳을 바꾼다. **①②③은 항상 함께 바꾼다** — 이 모듈은 벤더를 하나만 `runtimeOnly`로 싣기 때문에, 셋 중 하나라도 빠지면 켤 수 있는 벤더가 없다.

1. `build.gradle`: `runtimeOnly project(':infrastructure:firebase')` → `runtimeOnly project(':infrastructure:aws-s3')`
2. `application-file-storage.yml`의 `spring.config.import`: `classpath:application-firebase.yml` → `classpath:application-aws-s3.yml`
3. `application-file-storage.yml`의 `file.provider`: `firebase` → `s3`

그 밖에 `.env`에 `S3_BUCKET_NAME`·`AWS_S3_ACCESS_KEY`·`AWS_S3_SECRET_KEY`를 추가한다. 벤더 쪽에서 본 같은 절차는 `../aws-s3/AGENTS.md`의 "S3로 전환하는 절차 (앱 무수정)"에 있다.

**`file.provider` 줄에 허용값 주석(`# firebase | s3`)을 달지 않는다.** 벤더 코드에 정의된 값은 `firebase`·`s3` 둘이지만 이 모듈이 벤더를 하나만 싣기 때문에 지금 켤 수 있는 값은 `firebase` 하나뿐이다. 값만 `s3`로 바꾸면 `FileStoragePort` 빈이 없어 기동이 실패하므로, 나열은 거짓 선택지다.

**해소됨(2026-09-26)**: 과거 `infrastructure:aws`가 external·messaging을 implementation으로 가져 전환 시 admin·ceo에 전이로 실릴 위험이 실측으로 확인됐고, `aws-s3` 분리로 해소했다 — `aws-s3`의 의존은 domain과 spring-cloud-aws-starter-s3뿐이다.

메일(SES)·SMS(SNS)는 이 스타터를 거치지 않고, 각자의 채널 모듈(`../mail/AGENTS.md`·`../sms/AGENTS.md`)이 같은 방식(채널 모듈 `build.gradle` + yml 2파일)으로 벤더를 조립한다. 소셜 로그인 채널 `infrastructure:oauth`는 이 모듈과 **같은 형태의 코드 없는 스타터**다(채널·벤더 분할 2026-09-27) — 다만 배타 선택(`file.provider`)이 아니라 벤더 4종이 공존하고, web-api 한 앱만 의존한다(`../oauth/AGENTS.md`).

## ⚠️ 이 모듈에 코드를 넣지 않는다

**`FileDomainConfig`·`MailDomainConfig`·`SmsDomainConfig`는 없다** — 도메인 서비스(`FileUploadService`·`MailVerificationService`·`SmsVerificationService`)는 유스케이스 계층 클래스이고 `@Service`로 등록된다(`FileUploadService`는 코어 `application`, 나머지 둘은 `web-application`). 그래서 파일·메일·SMS 세 채널이 같은 형태(코드 없는 스타터)다. ([결정 0036](../../AGENTS.md#decision-0036))

**자바 소스를 추가하지 않는다.** 이 모듈의 존재 이유는 "무엇을 조립하는가"를 한 파일에서 읽히게 하는 것이고, 코드가 들어오는 순간 조립 선언과 구현이 섞여 그 가독성이 사라진다. 또한 이 모듈은 설정 클래스도 `AutoConfiguration.imports`도 갖지 않는다 — 빈 등록은 4앱 부트스트랩 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔이 조립 대상(firebase)의 클래스를 잡아 수행하고, firebase의 `FirebaseModuleConfig`가 Properties만 등록한다.

파일 저장 관련 코드가 필요해지면 소속은 둘 중 하나다.

- 벤더 무관 계약 → `application`의 포트 `FileStoragePort`(`application.file.port.out`, chunk 02-vendor-ports로 `domain`에서 이관)가 이미 맡는다. 그 포트와 동형인 전략 인터페이스를 새로 만들지 않는다
- 벤더 구현 → `infrastructure:firebase` / `infrastructure:aws-s3` (포트를 직접 구현)

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- `file.provider` 값은 이 모듈이 소유한다. 챕터 03 이전에는 `infrastructure:external`의 `application-external.yml`이 소유했으나, 그 파일은 이 값만 담고 있어 이동 후 삭제됐다. 이 값은 벤더 구현(`FirebaseFileStorage`·`S3FileStorage` 등)의 `@ConditionalOnProperty` 문자열로만 소비되며, 바인딩하는 Properties record는 없다.
- 기동 성공이 곧 스타터 경유 배선의 증명이다. persistence의 `FileUrlResolver`와 코어 `application`의 `FileUploadService`(`@Service` — 코어는 4앱 전부의 클래스패스에 있어 4앱 전부에 스캔 등록된다. 앱 마커 제거 전에는 `@SharedApp` 마커로 같은 효과를 냈다. 과거 등록처 `FileDomainConfig`·`FileServiceConfig`는 둘 다 삭제됨)가 `FileStoragePort`(Firebase 구현) 빈을 주입받으므로, 전이 의존이 끊기면 `FileStoragePort` 빈을 찾지 못해 컨텍스트 로딩이 실패한다.

## 결정 경위

이 문서가 소유한 규칙이 **왜 지금 모양이 됐는지**를 결정 단위로 적습니다. 본문은 현재 규칙만 담고, "예전에는 어땠고 왜 바뀌었는가"는 이 절에만 둡니다. 이 절은 항상 문서의 마지막 `##` 절이며, 항목은 번호 순입니다. 작성 규칙은 루트 `CLAUDE.md`의 "규칙 문서 개정 규칙" 절에 있습니다.

<a id="decision-0021"></a>

### 0021. external 코어의 파일 저장 SPI를 지우고 벤더가 FileStoragePort를 직접 구현한다

- 상태: 채택
- 결정 커밋: `13127bc9f` (2026-09-26)
- 대체한 결정: [결정 0016](../../AGENTS.md#decision-0016)(일부 — 7모듈로 나눌 때 코어에 둔 파일 저장 SPI)
- 원문: 삭제 전 git 이력의 `docs/adr/0021-file-storage-spi-removal.md`

#### 맥락

**SPI**(Service Provider Interface)는 코어 모듈이 정의하고 벤더 모듈이 구현하는 확장 지점 인터페이스입니다. **`FileStoragePort`**는 "파일을 저장한다"는 벤더 무관 계약(출력 포트)입니다.

`infrastructure:external`을 7모듈로 나눌 때([결정 0016](../../AGENTS.md#decision-0016)) 파일 저장은 "코어 SPI + 교체 가능한 벤더 구현" 형태가 됐습니다. 코어의 `com.tastyhouse.external.file` 패키지에는 다음 셋이 있었습니다.

- `FileStorageStrategy` — 벤더 전략 인터페이스
- `FileStoragePortAdapter` — 도메인 포트 `FileStoragePort`를 구현하며 전략에 위임
- `FileStorageProperties` — `file.*` 설정 바인딩

`infrastructure:firebase`·`infrastructure:aws`(현 `aws-s3`)가 `FileStorageStrategy`를 구현했습니다. 같은 분리에서 전략 시그니처가 `MultipartFile` → `byte[]`로 바뀌었는데, 그러자 이 중간 두 겹이 아무 일도 하지 않게 됐습니다.

#### 이전 규칙

##### backend/infrastructure/restclient/AGENTS.md — (당시) 코어 모듈 서술

코어(당시 `infrastructure:external`)가 파일 저장 SPI 3종(`FileStorageStrategy`·`FileStoragePortAdapter`·`FileStorageProperties`)을 소유했다. 벤더 모듈은 그 전략을 구현했다. 그래서 `infrastructure:firebase`도 코어를 의존했다.

- `FileStoragePortAdapter`는 당시 도메인 포트 `com.tastyhouse.domain.file.port.FileStoragePort`를 구현했고, 그래서 코어의 Internal 의존에 그 포트가 걸려 있었다.
- 7모듈 분리 때 전략 시그니처가 `MultipartFile` → `byte[]`로 바뀌며 `ByteArrayMultipartFile` 래퍼가 사라지고 코어의 `spring-web` 의존이 끊겼다.
- 코어의 `application-external.yml`은 `file.provider` 한 줄만 담고 있었고, 챕터 03에서 그 값의 소유가 스타터의 `application-file-storage.yml`로 옮겨가며 삭제됐다. 바인딩 대상 `FileStorageProperties`(`file.*`)는 그 뒤에도 코어에 남아 있다가 이 결정에서 삭제됐다.
- 코어 진입 설정은 `ExternalModuleAutoConfiguration`(이후 `HttpClientModuleAutoConfiguration`을 거쳐 `RestClientModuleAutoConfiguration`으로 개명)이었고, 코어 전체를 스캔하며 `@EnableConfigurationProperties`를 가졌다.

##### backend/CLAUDE.md — 벤더 선택은 앱이 아니라 스타터 모듈이 한다 (챕터 03)

스타터 `infrastructure:file-storage`는 신설 당시 코어 SPI(당시 `infrastructure:external`)와 벤더 구현(`infrastructure:firebase`) **둘을** `runtimeOnly`로 묶어 노출했다. 스타터 후보 판단 기준에도 "교체 가능한 벤더 구현과 그 설정, 그리고 코어 SPI"를 반복 선언하는 경우가 들어 있었다.

##### backend/infrastructure/file-storage/AGENTS.md — 왜 있는가

이 모듈은 코어 SPI와 벤더 구현 둘을 조립하는 모듈이었다.

#### 결정

코어의 `com.tastyhouse.external.file` 패키지(3개 타입)를 삭제합니다. 벤더 구현이 `FileStoragePort`를 **직접 구현**하도록 바꿔, 다른 driven 어댑터(`MailSenderPort`·`SmsSenderPort`·`PgPaymentGatewayPort`)와 같은 형태로 통일합니다.

| 항목 | before | after |
|---|---|---|
| 파일 저장 계약 | 코어 `FileStorageStrategy` + 위임 어댑터 `FileStoragePortAdapter` | 포트 `FileStoragePort` 하나 |
| 벤더 구현 | `FirebaseFileStorage`·`S3FileStorage`가 `FileStorageStrategy` 구현 | 두 클래스가 `implements FileStoragePort` |
| `FileStorageProperties`(`file.*`) | 코어에 존재(주입처 0건) | 삭제. `file.provider`는 벤더 구현의 `@ConditionalOnProperty` 문자열로만 소비 |
| 코어 auto-configuration 스캔 | 코어 전체 | `config` 패키지 하나, `@EnableConfigurationProperties` 제거 |
| `infrastructure:firebase` 의존 | 코어 + domain + firebase-admin | domain + firebase-admin(코어 의존 제거) |
| 스타터 `file-storage` 조립 대상 | 코어 + firebase | firebase 하나 |
| admin-api·ceo-api 런타임 클래스패스 | 코어(external)·webflux 포함 | 둘 다 없음 |
| 벤더 빈 선택 | — | `@ConditionalOnProperty(file.provider)` 배타 선택이라 `FileStoragePort` 빈은 항상 하나 |

#### 근거

- `byte[]` 전환 뒤 `FileStorageStrategy`의 메서드 3개는 `FileStoragePort`와 **시그니처가 완전히 같았고**, `FileStoragePortAdapter`는 변환 없이 위임만 했습니다. `FileStorageProperties`는 주입처가 0건인 죽은 코드였습니다.
- 포트가 이미 벤더 무관 계약이므로, 벤더 모듈은 그 포트를 소유한 모듈만 의존하면 됩니다.
- 검토하고 채택하지 않은 대안은 다음 두 가지입니다.
  - **SPI를 `infrastructure:file-storage`로 옮긴다**: file-storage가 firebase를 `runtimeOnly`로 의존하는데, firebase가 SPI를 구현하려면 file-storage를 의존해야 합니다. file-storage ↔ firebase 순환이 됩니다.
  - **SPI 전용 모듈을 신설한다**: 포트와 모양이 같은 인터페이스 하나를 위해 모듈을 늘리는 것이라 얻는 것이 없습니다.

#### 결과·감수한 손실

- 코어는 `config/`만 남은 HTTP 코어가 됐습니다(이후 `infrastructure:restclient`로 리네임). 파일 저장만 쓰는 admin·ceo의 런타임 클래스패스에서 코어가 빠졌습니다. web은 oauth·pg·메일·SMS 경로로, batch는 수집 모듈 경로로 코어를 계속 받습니다.
- 당시 `infrastructure:aws`는 SES/SNS 실패를 던지느라 코어 의존을 유지했습니다(이후 [결정 0022](../../AGENTS.md#decision-0022) 3분할로 `aws-s3`만 코어 의존이 없어짐).
- 벤더 모듈 분리(firebase·aws), 모듈 수 21, 앱 설정은 바뀌지 않았습니다. API·동작 변경도 없습니다.
- 반증으로 확인했습니다. 구현 빈이 없으면 `FileStoragePort`를 주입받는 빈(`FileUrlResolver`)이 기동 시 실패합니다. 2026-09-26에 `--file.provider=s3`로 admin-api를 aws-s3 모듈 없이 띄우면 `FileUrlResolver`에서 `APPLICATION FAILED TO START`가 났습니다.
- 당시 `infrastructure:persistence`에서 `FileStoragePort`를 주입받는 빈은 `FileUrlResolver`와 `FileDomainConfig`(`FileUploadService` 등록)였습니다. `FileUploadService` 등록은 이후 덩어리 02-vendor-ports로 application의 `FileServiceConfig`로 옮겨졌다가, application `*ServiceConfig` 삭제로 클래스에 `@SharedApp` 마커만 달아 스캔 등록하는 방식이 됐습니다(그 마커는 이후 [결정 0036](../../AGENTS.md#decision-0036)에서 제거).
- 코어 진입 설정의 스캔은 `com.tastyhouse.restclient.config` 하나로 줄었습니다(지금 패키지 루트는 `com.tastyhouse.infrastructure.restclient`).
- "포트 시그니처에 `MultipartFile`·SDK 타입·`RestClient` 타입을 누출하지 않는다"는 원칙은 그대로입니다. 이후 RestClient 전환으로 `spring-web`은 코어의 정식 `api` 의존으로 되돌아왔지만, 그것은 HTTP 클라이언트 용도이지 파일 저장 시그니처 때문이 아닙니다.
- 스타터의 조립 대상은 하나로 줄었지만 스타터는 유지합니다. 앱이 벤더 이름을 모르게 한다는 목적은 조립 대상 개수와 무관하고, 교체 가능한 벤더는 여전히 firebase·s3 둘이기 때문입니다.
- `FileStoragePort` 자체는 이후 [결정 0026](../../application-core/AGENTS.md#decision-0026)에서 `domain`을 떠나 `application`(`com.tastyhouse.application.file.port.out`)으로 옮겨졌습니다. 벤더 모듈의 의존도 `domain` → `application`으로 바뀌었습니다.
