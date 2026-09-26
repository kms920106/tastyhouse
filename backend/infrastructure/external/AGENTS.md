<!-- Parent: ../../AGENTS.md -->

# infrastructure:external (코어)

벤더 중립 공용 자산만 담는 코어 인프라 모듈(`java-library`). **이 모듈에 남은 것은 둘뿐이다** — `WebClient` 빌더와 외부 연동 예외 계약(`ExternalApiException`/`ExternalApiErrorCode`). 7모듈 분리(챕터 01) 직후에는 파일 저장 코어 SPI까지 셋이었으나, 그 SPI는 삭제됐다(아래 [과거 판단의 번복 — 파일 저장 SPI 삭제](#과거-판단의-번복--파일-저장-spi-삭제)). 즉 이 모듈은 이제 **순수 HTTP 코어**다.

## 분리 배경 (챕터 01)

분리 전에는 이 한 모듈이 OAuth 4종·Toss 결제·메일(JavaMail/SES)·SMS(Solapi/SNS)·파일(Firebase/S3)·BBQ 크롤링·행정동 경계와 벤더 SDK 3종(AWS SES/SNS/S3, Firebase Admin)을 전부 품었고, **4개 앱이 그것을 통째로 받았다.** 실사용은 그렇지 않았다.

| 앱 | 실제로 쓰는 어댑터 |
|---|---|
| web-api | OAuth 4종 · Toss · Mail · SMS · File |
| admin-api | **File만** |
| ceo-api | **File만** |
| batch-module | File(원격 이미지) · BBQ · 행정동 경계 |

즉 admin/ceo는 파일 저장 하나만 쓰면서 OAuth·Toss·메일·SMS·크롤링 코드와 무거운 SDK(AWS·Firebase)를 전부 클래스패스에 얹고 있었다. admin/ceo/batch가 메일·SMS 어댑터까지 강제로 들여와야 했던 직접 원인은 persistence의 `MailDomainConfig`·`SmsDomainConfig`가 `MailSender`/`SmsSender` 빈을 무조건 요구한 것이며, 그 결합은 두 설정을 `infrastructure:messaging`으로 이관해 함께 끊었다(`../messaging/AGENTS.md`).

이 분리는 `backend/CLAUDE.md` "external을 infrastructure 아래로 들인 이유" 절의 **비채택 대안 (1) 기술별 추가 분할·(3) AWS 벤더 패키지 모으기를 명시적으로 번복**한 것이다. 번복 근거는 위 실사용 표(admin/ceo가 file 하나)와 무거운 SDK가 두 벤더에 국한된다는 점이다.

## 어디로 갔는지 (포인터)

| 옮겨간 것 | 모듈 | 문서 |
|---|---|---|
| Firebase Storage 파일 저장 | `infrastructure:firebase` | `../firebase/AGENTS.md` |
| `application-external.yml`의 `file.provider` (챕터 03) | `infrastructure:file-storage`의 `application-file-storage.yml` | `../file-storage/AGENTS.md` |
| S3 · SES · SNS (AWS SDK 전부) | `infrastructure:aws` | `../aws/AGENTS.md` |
| 소셜 로그인 클라이언트 4종 | `infrastructure:oauth` | `../oauth/AGENTS.md` |
| 토스페이먼츠 연동 | `infrastructure:payment` | `../payment/AGENTS.md` |
| 메일(JavaMail)·SMS(Solapi) + Mail/SmsDomainConfig | `infrastructure:messaging` | `../messaging/AGENTS.md` |
| BBQ 크롤링 · 행정동 경계 · 원격 이미지 다운로드 | `infrastructure:crawling` | `../crawling/AGENTS.md` |
| `file/{FileStorageStrategy,FileStoragePortAdapter,FileStorageProperties}` | **삭제** — 벤더 어댑터(`FirebaseFileStorage`·`S3FileStorage`)가 도메인 포트 `FileStoragePort`를 직접 구현한다 | 아래 [과거 판단의 번복](#과거-판단의-번복--파일-저장-spi-삭제) |

형제 모듈은 `infrastructure:persistence`(`../persistence/AGENTS.md`)·`infrastructure:redis`(`../redis/AGENTS.md`)이며, 이 9개는 전부 driven(아웃바운드) 어댑터다. 여기에 챕터 03에서 신설된 `infrastructure:file-storage`(`../file-storage/AGENTS.md`)가 더해져 `infrastructure` 아래는 10개가 됐는데, 이 하나만 어댑터가 아니라 **자바 코드 없는 조립 전용 스타터**다.

**`application-external.yml`은 챕터 03에서 삭제됐다.** 담고 있던 것이 `file.provider` 한 줄뿐이었고, 그 값의 소유가 스타터 `infrastructure:file-storage`의 `application-file-storage.yml`로 옮겨갔기 때문이다. 당시 이 모듈에 남아 있던 바인딩 대상 `FileStorageProperties`(`file.*`)는 이후 주입처가 0건인 죽은 코드로 확인돼 삭제됐다 — 지금 `file.provider`는 벤더 구현의 `@ConditionalOnProperty` 문자열로만 소비된다.

## 자바 패키지는 `com.tastyhouse.external..`로 유지한다

**7모듈로 나뉜 뒤에도 패키지 루트는 전부 `com.tastyhouse.external..`이다.** `com.tastyhouse.infrastructure.external`로 옮기지 않는 이유는 persistence의 `PersistenceModuleAutoConfiguration`(챕터 02로 `InfrastructureModuleConfig`에서 리네임)이 `@ComponentScan("com.tastyhouse.infrastructure")`로 그 트리를 통째 스캔하기 때문이다 — 그 아래로 옮기면 앱이 의존하지도 않은 어댑터까지 스캔 대상이 된다(분리 전에는 이 스캔이 `ExternalModuleAutoConfiguration`(구 `ExternalModuleConfig`)의 OAuth REGEX 제외 필터를 우회해 admin/ceo/batch가 `Could not resolve placeholder 'apple.team-id'`로 부팅에 실패했다). 모듈명 ≠ 패키지명은 `infrastructure:persistence`=`com.tastyhouse.infrastructure..`, `security-core`/`security-module`=`com.tastyhouse.security..` 선례와 같다.

## 패키지 구조

```
com.tastyhouse.external/
├── config/
│   ├── ExternalModuleAutoConfiguration.java  진입점 — 챕터 02로 ExternalModuleConfig에서 리네임 + @AutoConfiguration, 자기 등록(oauth·payment·messaging·crawling을 경유해 web·batch에만 실린다)
│   └── WebClientConfig.java        WebClient.Builder 빈
└── exception/
    ├── ExternalApiException.java   BusinessException 상속 (전용 핸들러를 두지 않는다)
    └── ExternalApiErrorCode.java   ErrorCodeSpec 구현
```

`ExternalModuleAutoConfiguration`의 `@ComponentScan`은 `com.tastyhouse.external.config` 한 패키지뿐이고, `@EnableConfigurationProperties`는 없다(등록할 Properties record가 이 모듈에 남지 않았다). 분리 전에 있던 OAuth REGEX `excludeFilters`와 타 모듈 Properties 등록은 제거됐다 — **모듈 경계(= 의존 선언)가 그 역할을 대신한다.** 파일 저장 SPI 삭제 전에는 `com.tastyhouse.external.file`도 스캔 대상이었고 `FileStorageProperties`를 등록했다.

### 벤더 패키지를 `external.file` 아래에 두지 않았던 이유 (패키지 예외 3건 — 이력)

7모듈 분리 당시 코어 스캔이 `com.tastyhouse.external.file`을 대상으로 했으므로, **하위 패키지가 클래스패스에 있으면 동반 스캔됐다.** 즉 `external.file.firebase`·`external.file.s3`를 그대로 뒀다면 코어를 import 한 것만으로 벤더 빈이 딸려 올라왔다. 그래서 이동 시 아래 3건만 패키지를 바꿨다(그 외 이동 파일은 패키지 불변). **파일 저장 SPI 삭제로 그 스캔은 사라졌지만**, 바뀐 패키지는 되돌리지 않는다 — 벤더 모듈마다 겹치지 않는 하위 패키지를 소유한다는 split package 회피 구조가 여전히 필요하기 때문이다.

| 원래 패키지 | 바뀐 패키지 | 소유 모듈 |
|---|---|---|
| `external.file.firebase` | `external.firebase` | firebase |
| `external.file.s3` | `external.aws.s3` | aws |
| `external.file.RemoteImageDownloader` | `external.crawling.RemoteImageDownloader` | crawling |

같은 취지로 AWS 채널 어댑터도 `external.mail.ses` → `external.aws.ses`, `external.sms.sns` → `external.aws.sns`로 모았다(메시징 스캔에 딸려 오지 않게 하기 위함). split package는 없다 — `external.mail`(messaging) vs `external.aws.ses`(aws), `external.firebase`(firebase) vs `external.aws.s3`(aws)가 각각 다른 모듈에 온전히 속한다. `external.file` 패키지는 SPI 삭제로 어느 모듈에도 존재하지 않는다.

## 과거 판단의 번복 — 파일 저장 SPI 삭제

**경위.** 7모듈 분리(챕터 01) 때 파일 저장은 "코어 SPI + 교체 가능한 벤더 구현" 형태로 나뉘었다. 코어(이 모듈)의 `com.tastyhouse.external.file`에 벤더 전략 인터페이스 `FileStorageStrategy`, 도메인 포트 `com.tastyhouse.domain.file.port.FileStoragePort`를 구현하는 `FileStoragePortAdapter`, `file.*`를 바인딩하는 `FileStorageProperties` 셋을 두고, `infrastructure:firebase`·`infrastructure:aws`가 전략을 구현했다. 같은 분리에서 전략의 시그니처가 `MultipartFile` → `byte[]`로 바뀌며 `ByteArrayMultipartFile` 래퍼가 사라지고 코어의 `spring-web` 의존이 끊겼다.

**왜 삭제했나.** 그 `byte[]` 전환의 결과 **`FileStorageStrategy`의 메서드 3개가 `FileStoragePort`와 시그니처가 완전히 같아졌고**, `FileStoragePortAdapter`는 변환 없이 위임만 했다. 중간 두 겹(전략 인터페이스 + 위임 어댑터)이 아무 일도 하지 않았다. `FileStorageProperties`는 주입처가 0건인 죽은 코드였다. 그래서 셋을 삭제하고, 다른 driven 어댑터(`MailSender`·`SmsSender`·`PgPaymentGateway`)처럼 **벤더 구현이 도메인 포트를 직접 구현**하는 형태로 통일했다 — `FirebaseFileStorage`·`S3FileStorage`가 `implements FileStoragePort`다. 둘 다 `@ConditionalOnProperty(file.provider)`로 배타 선택되므로 `FileStoragePort` 빈은 항상 하나이며, 주입받는 쪽은 persistence의 `FileDomainConfig`(`FileUploadService` 생성)와 `FileUrlResolver`다.

**벤더 모듈 분리는 유지한다.** 검토한 대안과 비채택 사유:

- **SPI를 `infrastructure:file-storage`로 옮긴다** — file-storage가 firebase를 `runtimeOnly`로 의존하는데 firebase가 SPI를 구현하려면 file-storage를 의존해야 해 **file-storage ↔ firebase 순환**이 된다.
- **SPI 전용 모듈을 신설한다** — 인터페이스 1개(그것도 도메인 포트와 동형)를 위해 모듈을 하나 늘리는 것이라 얻는 것이 없다.
- 도메인 포트 `FileStoragePort`가 이미 벤더 무관 계약이므로, 벤더 모듈은 `domain`만 의존하면 된다.

**결과.** `external`은 `config/`·`exception/`만 남은 순수 HTTP 코어가 됐다. `infrastructure:firebase`는 external 의존을 끊어 `domain` + firebase-admin만 갖고, 스타터 `infrastructure:file-storage`는 `runtimeOnly project(':infrastructure:firebase')` 한 줄만 조립한다. 그 결과 파일 저장만 쓰는 **admin-api·ceo-api의 런타임 클래스패스에서 external과 webflux(reactor-netty)가 빠진다.** web은 oauth·payment·messaging을, batch는 crawling을 경유해 external을 계속 갖는다. `infrastructure:aws`는 SES/SNS가 `ExternalApiException`/`ExternalApiErrorCode`를 쓰므로(`MailProperties`는 messaging 소유라 그쪽 의존의 이유다) external 의존을 유지한다.

이 모듈의 외부 의존은 `spring-boot-starter-webflux` 하나뿐이다.

## Dependencies

### Internal
- `domain` (implementation) — 예외 계약만 쓴다: `ExternalApiErrorCode`가 구현하는 `ErrorCodeSpec`, `ExternalApiException`이 상속하는 `BusinessException`. 과거 `FileStoragePortAdapter`가 구현하던 `com.tastyhouse.domain.file.port.FileStoragePort`는 이제 벤더 모듈(firebase·aws)이 직접 구현한다.

**`application`에 의존하지 않는다.** 분리 전에는 소셜 로그인 SPI·BBQ·행정동 경계 포트를 구현하느라 `implementation project(':application')`이 있었으나, 그 어댑터들이 전부 oauth·crawling 모듈로 떠나 코어에는 아웃바운드 계약 소비자가 남지 않았다.

### External
- `spring-boot-starter-webflux` — `WebClientConfig`의 `WebClient.Builder`. Jackson도 이것이 전이로 제공한다.
- **AWS SDK·Firebase Admin·jjwt·`spring-boot-starter-mail`·`spring-web` 의존은 전부 제거됐다** — 각각 aws·firebase·oauth·messaging 모듈이 소유한다.

## 어댑터 작성 규칙 (7모듈 공통)

이 절은 코어뿐 아니라 `infrastructure:{firebase,aws,oauth,payment,messaging,crawling}` 전부에 적용된다.

- **도메인 포트를 구현하되 프레임워크 타입을 시그니처로 누출하지 않는다**: 포트(`MailSender`·`SmsSender`·`FileStoragePort`·`PgPaymentGateway`)는 프레임워크-프리이므로 `WebClient`·SDK 타입·wire DTO가 포트 시그니처에 등장하면 안 된다. 변환은 어댑터 안에서 끝낸다.
- **외부 응답 DTO는 도메인 타입을 보유하지 않는다 (역방향 누수 금지)**: 상세는 `../oauth/AGENTS.md`.
- **자격증명은 코드에 하드코딩하지 않는다**: 환경변수(`.env`)·configtree 시크릿(`SECRETS_DIR`, `../firebase/AGENTS.md`)으로 주입한다.
- **provider 선택은 `@ConditionalOnProperty`로 한다**: `file.provider`·`mail.provider`·`sms.provider`. 조건 애노테이션은 스캔되는 구현 클래스에 붙어 있고, `{Xxx}ModuleConfig`는 조건을 갖지 않는다.
- **에러는 `ExternalApiException`(`BusinessException` 상속)으로 던진다**: 모듈마다 예외 타입을 새로 만들고 전역 핸들러에 `@ExceptionHandler`를 추가하지 않는다.

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar. 스타터 `file-storage`를 포함한 8모듈 전부 같다.
- **빈 배선 (파일 저장 SPI 삭제로 개정)**: `ExternalModuleAutoConfiguration`은 클래스패스 존재만으로 자동 등록된다. **앱은 이 모듈을 직접 선언하지 않는다** — web은 oauth·payment·messaging을, batch는 crawling을 경유해 전이로 받는다. 스타터 `infrastructure:file-storage`는 더 이상 이 모듈을 조립하지 않으므로(firebase 한 줄), admin-api·ceo-api의 `runtimeClasspath`에는 이 모듈이 없다. 파일 저장의 기동 실패 조건도 이 모듈과 무관해졌다 — `FileStoragePort` 구현이 없으면 persistence의 `FileUrlResolver`·`FileDomainConfig`가 `FileStoragePort` 빈을 찾지 못해 **기동 시** 실패한다(`../file-storage/AGENTS.md`).
- **하위 문서**: 코어에 남은 어댑터 패키지 설명은 `src/main/java/com/tastyhouse/external/AGENTS.md`.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.external..` 봉인 (외부 연동 7모듈 공통)

**대상**: `backend/infrastructure/external/src/main/java/com/tastyhouse/external/` 및 형제 모듈 6개의 같은 패키지 루트

위 "자바 패키지는 `com.tastyhouse.external..`로 유지한다" 절과 같은 사실을, **가드로서** 다시 못박는다. `com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 `@ComponentScan("com.tastyhouse.infrastructure")`가 그 트리를 통째로 스캔하므로 **빈 스캔 범위가 어긋나 admin-api·ceo-api·batch-module의 부팅이 깨진다.** 모듈 디렉터리와 패키지 이름이 어긋나 보인다는 이유로 정리하지 않는다.

### `@ConfigurationProperties` record는 명시 등록한다

**대상**: `backend/infrastructure/external/src/main/java/com/tastyhouse/external/config/ExternalModuleAutoConfiguration.java` → 클래스 선언부(현재 `@EnableConfigurationProperties` 없음)

`@ConfigurationPropertiesScan`을 쓰지 않는 것이 이 저장소의 방침이다. 파일 저장 SPI 삭제로 이 모듈에 등록할 Properties record가 없어져 `@EnableConfigurationProperties`도 함께 제거됐다. 이후 Properties record를 이 모듈에 추가한다면 컴포넌트 스캔에 맡기지 말고 진입 설정의 `@EnableConfigurationProperties`에 명시한다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 코어 auto-configuration의 발화 조건과 형제 모듈의 자기 등록

**대상**: `backend/infrastructure/external/src/main/java/com/tastyhouse/external/config/ExternalModuleAutoConfiguration.java`

이 코어는 **클래스패스 존재만으로 활성화**되며, 앱은 이 클래스를 `@Import` 하지 않는다(앱이 직접 선언하지도 않고 어댑터 모듈을 통해 전이로 받는다). 실제 저장소 구현(Firebase·S3)·OAuth·결제·메시징·크롤링은 각각 별도 모듈이며, 그 모듈들도 자기 auto-configuration(`FirebaseModuleAutoConfiguration`·`AwsModuleAutoConfiguration`·`OAuthModuleAutoConfiguration`·`PaymentModuleAutoConfiguration`·`MessagingModuleAutoConfiguration`·`CrawlingModuleAutoConfiguration`)으로 자기 등록한다. 앱은 실제로 쓰는 모듈만 의존한다.

### `ExternalApiException`이 `BusinessException`을 상속하는 이유 (결함 이력)

**대상**: `backend/infrastructure/external/src/main/java/com/tastyhouse/external/exception/ExternalApiException.java`

`BusinessException`을 상속하므로 **각 api 모듈의 `BusinessException` 핸들러가 그대로 처리한다.** 과거에는 독립 예외라 모듈별 전용 핸들러가 필요했고, admin-api·ceo-api가 그 핸들러를 갖지 않아 **502로 의도된 발송 실패가 `Exception` 폴백을 타고 500으로 응답되는 결함**이 있었다. 새 예외 타입을 만들어 전역 핸들러에 `@ExceptionHandler`를 추가하지 않는 규칙의 근거가 이 이력이다.

### `WebClient` 빌더를 코어가 소유하는 이유와 버퍼 한도

**대상**: `backend/infrastructure/external/src/main/java/com/tastyhouse/external/config/WebClientConfig.java`

비동기·논블로킹 HTTP 클라이언트(타임아웃·연결 풀 포함)를 코어가 등록한다. OAuth·결제 등 외부 연동 클라이언트가 `WebClient`를 주입받으므로, 이 설정을 코어에 두어 코어를 받는 모든 앱에서 빈이 등록되게 한다. **`maxInMemorySize`는 2MB(`2 * 1024 * 1024`)** 이며, 이보다 큰 응답을 다루는 어댑터는 `WebClient`가 아니라 `HttpClient` + 스트리밍 파서를 쓴다(선례: crawling 모듈의 `AdminDongBoundaryClient`).
