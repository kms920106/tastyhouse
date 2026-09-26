<!-- Parent: ../../AGENTS.md -->

# infrastructure:restclient (코어)

벤더 중립 공용 자산만 담는 코어 인프라 모듈(`java-library`). **이 모듈에 남은 것은 설정뿐이다** — Boot `RestClient.Builder`를 꾸미는 요청 팩토리 customizer(`RestClientConfig`·`HttpRequestFactories`)와 진입 설정(`RestClientModuleAutoConfiguration`). 7모듈 분리(챕터 01) 직후에는 파일 저장 코어 SPI까지 있었으나 삭제됐고(아래 [과거 판단의 번복 — 파일 저장 SPI 삭제](#과거-판단의-번복--파일-저장-spi-삭제)), 이후 예외 계약(`ExternalApiException`/`ExternalApiErrorCode`)도 **완전히 해체**됐다(아래 [예외 계약 해체 — 도메인 `ErrorCode`로 흡수](#예외-계약-해체--도메인-errorcode로-흡수)). `WebClient`/webflux를 전면 제거하고 Spring `RestClient`로 전환했으며, 모듈명도 역할에 맞춰 `infrastructure:external` → **`infrastructure:restclient`로 리네임**했다. 즉 이 모듈은 이제 **설정만 갖는 순수 HTTP 코어**다 — 예외·에러코드를 두지 않는다.

## 왜 WebClient/RestClient를 골랐나

이 모듈을 의존하는 7개 호출부(oauth 4종·payment·messaging의 Solapi·bbq)는 전부 **`.block()`으로 동기 호출**하고 있었다 — 반응형 합성(체이닝·백프레셔·논블로킹 I/O 활용)이 실제로는 0건이었다. 소비 앱 4개(web-api·admin-api·ceo-api·batch-module)도 전부 서블릿 MVC라 리액티브 스택이 프레임워크 차원에서 맞지 않았다. Spring Boot 3.2+가 1급으로 지원하는 동기 클라이언트 `RestClient`(`RestTemplate`은 유지보수 모드)로 통일하면 `.block()` 호출·webflux 의존·reactor-netty가 전부 사라진다. 포트(`MailSender`·`SmsSender`·`SocialOAuthClient` 등)는 프레임워크-프리이므로 어댑터 밖(application·domain)은 애초에 클라이언트 종류를 모른다 — 어댑터가 `RestClient`를 알아도 되는 유일한 층이며, 그래서 자체 래퍼 추상화(어댑터가 공용 인터페이스를 통해서만 HTTP를 부르게 하는 것)는 두 번째 구현체가 없어 비채택했다.

## 분리 배경 (챕터 01)

분리 전에는 이 한 모듈이 OAuth 4종·Toss 결제·메일(JavaMail/SES)·SMS(Solapi/SNS)·파일(Firebase/S3)·BBQ 크롤링·행정동 경계와 벤더 SDK 3종(AWS SES/SNS/S3, Firebase Admin)을 전부 품었고, **4개 앱이 그것을 통째로 받았다.** 실사용은 그렇지 않았다.

| 앱 | 실제로 쓰는 어댑터 |
|---|---|
| web-api | OAuth 4종 · Toss · Mail · SMS · File |
| admin-api | **File만** |
| ceo-api | **File만** |
| batch-module | File(원격 이미지) · BBQ · 행정동 경계 |

즉 admin/ceo는 파일 저장 하나만 쓰면서 OAuth·Toss·메일·SMS·크롤링 코드와 무거운 SDK(AWS·Firebase)를 전부 클래스패스에 얹고 있었다. admin/ceo/batch가 메일·SMS 어댑터까지 강제로 들여와야 했던 직접 원인은 persistence의 `MailDomainConfig`·`SmsDomainConfig`가 `MailSender`/`SmsSender` 빈을 무조건 요구한 것이며, 그 결합은 두 설정을 `infrastructure:messaging`으로 이관해 함께 끊었다(이후 messaging 4분할로 채널 모듈 `../mail/AGENTS.md`·`../sms/AGENTS.md`로 옮겨졌다).

이 분리는 `backend/CLAUDE.md` "external을 infrastructure 아래로 들인 이유" 절의 **비채택 대안 (1) 기술별 추가 분할·(3) AWS 벤더 패키지 모으기를 명시적으로 번복**한 것이다. 번복 근거는 위 실사용 표(admin/ceo가 file 하나)와 무거운 SDK가 두 벤더에 국한된다는 점이다.

## 어디로 갔는지 (포인터)

| 옮겨간 것 | 모듈 | 문서 |
|---|---|---|
| Firebase Storage 파일 저장 | `infrastructure:firebase` | `../firebase/AGENTS.md` |
| `application-external.yml`의 `file.provider` (챕터 03) | `infrastructure:file-storage`의 `application-file-storage.yml` | `../file-storage/AGENTS.md` |
| S3 (파일 저장) | `infrastructure:aws-s3` | `../aws-s3/AGENTS.md` |
| SES (메일) | `infrastructure:aws-ses` | `../aws-ses/AGENTS.md` |
| SNS (SMS) | `infrastructure:aws-sns` | `../aws-sns/AGENTS.md` |
| 소셜 로그인 클라이언트 4종 | `infrastructure:oauth` | `../oauth/AGENTS.md` |
| 토스페이먼츠 연동 | `infrastructure:payment` | `../payment/AGENTS.md` |
| 메일(JavaMail)·SMS(Solapi) + Mail/SmsDomainConfig | `infrastructure:messaging` → 4분할(2026-09-26)로 `infrastructure:{mail,javamail,sms,solapi}` | `../mail/AGENTS.md`·`../javamail/AGENTS.md`·`../sms/AGENTS.md`·`../solapi/AGENTS.md` |
| BBQ 메뉴 수집 · 원격 이미지 다운로드 | `infrastructure:bbq` | `../bbq/AGENTS.md` |
| 행정동 경계 GeoJSON 수집 | `infrastructure:admdongkor` | `../admdongkor/AGENTS.md` |
| `file/{FileStorageStrategy,FileStoragePortAdapter,FileStorageProperties}` | **삭제** — 벤더 어댑터(`FirebaseFileStorage`·`S3FileStorage`)가 도메인 포트 `FileStoragePort`를 직접 구현한다 | 아래 [과거 판단의 번복](#과거-판단의-번복--파일-저장-spi-삭제) |
| `exception/{ExternalApiException,ExternalApiErrorCode}` | **삭제** — 5개 상수는 도메인 `ErrorCode`로, 어댑터는 `BusinessException`을 직접 던진다 | 아래 [예외 계약 해체](#예외-계약-해체--도메인-errorcode로-흡수) |

형제 모듈은 `infrastructure:persistence`(`../persistence/AGENTS.md`)·`infrastructure:redis`(`../redis/AGENTS.md`)이며, 이 12개는 전부 driven(아웃바운드) 어댑터다. 여기에 챕터 03에서 신설된 `infrastructure:file-storage`(`../file-storage/AGENTS.md`)가 더해져 `infrastructure` 아래는 13개가 됐는데, 이 하나만 어댑터가 아니라 **자바 코드 없는 조립 전용 스타터**다.

**`application-external.yml`은 챕터 03에서 삭제됐다.** 담고 있던 것이 `file.provider` 한 줄뿐이었고, 그 값의 소유가 스타터 `infrastructure:file-storage`의 `application-file-storage.yml`로 옮겨갔기 때문이다. 당시 이 모듈에 남아 있던 바인딩 대상 `FileStorageProperties`(`file.*`)는 이후 주입처가 0건인 죽은 코드로 확인돼 삭제됐다 — 지금 `file.provider`는 벤더 구현의 `@ConditionalOnProperty` 문자열로만 소비된다.

## 예외 계약 해체 — 도메인 `ErrorCode`로 흡수

**이 모듈의 `exception/` 패키지(`ExternalApiException`·`ExternalApiErrorCode`)는 완전히 삭제됐다.** 그 5개 상수(`SMS_SEND_NO_RESPONSE`·`SMS_SEND_FAILED`·`SMS_SEND_API_ERROR`·`MAIL_SEND_FAILED`·`ADMIN_DONG_BOUNDARY_FETCH_FAILED`)는 코드 문자열을 그대로 유지한 채 도메인 `ErrorCode`(`backend/domain/src/main/java/com/tastyhouse/domain/exception/ErrorCode.java`)로 옮겨졌고, HTTP 상태 502도 동일하게 승계됐다(각각 `SMS_VERIFICATION_CODE_*` 블록 뒤, `MAIL_VERIFICATION_CODE_*` 블록 뒤, `ADMIN_DONG_QUERY_INVALID` 뒤에 위치). 어댑터(`SolapiSmsClient`·`JavaMailAdapter`·`SesMailSender`·`SnsSmsSender`·`AdminDongBoundaryClient`)는 이제 `new BusinessException(ErrorCode.X[, cause])`를 직접 던진다.

**해체 근거**: 이 5개 상수는 전부 "채널 발송 실패"였고 그중 어느 것도 실제로는 "HTTP 클라이언트 오류"가 아니었다 — 5개 소비처 중 3개(SES·SNS SDK 호출, JavaMail SMTP 연동)는 애초에 `RestClient`를 쓰지 않으면서도 이 예외 하나 때문에 코어 모듈에 의존하고 있었다. `ExternalApiException` 자신도 `BusinessException`의 생성자 3개를 그대로 위임만 하는 빈 서브클래스였고, 타입으로 구분해 catch하는 지점이 0건이었다. 모듈을 `HttpClientException`으로 리네임하는 안은 5개 상수 중 5개 모두와 어긋났고(HTTP 클라이언트 실패가 아니므로), 채널별 예외 타입을 새로 만드는 안은 빈 껍데기 클래스를 늘리고 `aws-sns → messaging`이라는 새 모듈 의존까지 만들었다. 도메인 `ErrorCode`는 이미 어댑터 실패 코드(`FILE_STORE_FAILED` 500, `SOCIAL_OAUTH_FAILED` 502)를 갖고 있었으므로, "카탈로그는 하나"라는 기존 원칙([예외·에러코드 소유 규칙](../../../CLAUDE.md#예외에러코드-소유-규칙-errorcodespec-공통-계약--businessexception-단일-계층))을 그대로 따랐다.

**`ErrorCodeSpec`은 폐지하지 않고 유지한다.** 지금 이 인터페이스의 구현체는 `ErrorCode` 하나뿐이지만, 없앤다고 정리되는 것이 거의 없다 — `BusinessException`의 필드·생성자·`getErrorCode()`가 여전히 `ErrorCodeSpec` 타입이어야 하고(타입을 `ErrorCode`로 좁히는 것 자체가 API 변경), `ErrorCodeConventionTest`의 계약 검증 케이스 1건도 그대로 필요하다. 두 `GlobalExceptionHandler`(web-api·api-common-module)는 `e.getErrorCode().getCode()`/`getHttpStatusCode()`만 호출해 구체 타입을 몰랐으므로 이번 변경으로 그 두 파일의 소스가 바뀌지 않았다(실측 확인). 남겨두는 이유는 "두 계열을 통합하기 위해서"가 아니라 **"카탈로그는 하나로 유지하되, domain이 필요하면 모듈별 에러 카탈로그를 다시 호스트할 수 있는 확장점"**이다 — 이 인터페이스가 없으면 다음에 같은 문제가 생겼을 때 처음부터 다시 설계해야 한다.

**해체 이력 (3단계)**: (1) 원래 `ExternalApiException`은 `BusinessException`과 무관한 독립 예외였다 — admin-api·ceo-api에 이 타입 전용 핸들러가 없어, 502로 의도된 실패(SMS·메일 발송)가 `Exception` 폴백을 타고 500으로 응답되는 결함이 있었다. (2) `BusinessException`을 상속하도록 고쳐 그 결함을 해소했다. (3) 상속 후에도 여전히 생성자 위임만 하는 빈 서브클래스라는 것이 드러나 해체하고, 상수는 도메인 `ErrorCode`로, 예외 자체는 어댑터가 `BusinessException`을 직접 쓰는 형태로 정리했다.

## 코어 패키지는 `com.tastyhouse.restclient..`, 벤더 9모듈은 `com.tastyhouse.external..` 유지

**코어(이 모듈)만 자기 패키지 루트 `com.tastyhouse.restclient`로 옮겼다.** `security-core`/`security-module`이 이미 `com.tastyhouse.security..`를 공유하는 선례(모듈명 ≠ 패키지명)를 따라, 코어는 모듈 리네임과 함께 패키지도 `com.tastyhouse.restclient.config`로 옮겼다. **벤더·채널 12모듈(oauth·payment·mail·javamail·sms·solapi·bbq·admdongkor·firebase·aws-s3·aws-ses·aws-sns)의 패키지는 `com.tastyhouse.external.*`로 그대로 남는다** — persistence의 `PersistenceModuleAutoConfiguration`(챕터 02로 `InfrastructureModuleConfig`에서 리네임)이 `@ComponentScan("com.tastyhouse.infrastructure")`로 그 트리를 통째 스캔하기 때문에, 벤더 모듈을 그 아래로 옮기면 앱이 의존하지도 않은 어댑터까지 스캔 대상이 된다(분리 전에는 이 스캔이 진입 설정의 OAuth REGEX 제외 필터를 우회해 admin/ceo/batch가 `Could not resolve placeholder 'apple.team-id'`로 부팅에 실패했다). **코어는 그 스캔 트리에 들어가는 벤더 빈이 없으므로**(설정 클래스뿐, `@ComponentScan` 대상 자체가 이 모듈 안에서 끝난다) 이 제약에서 자유롭고, 패키지를 옮겨도 스캔 범위 충돌이 생기지 않는다.

## 패키지 구조

```
com.tastyhouse.restclient/
└── config/
    ├── RestClientModuleAutoConfiguration.java  진입점 — 구 ExternalModuleAutoConfiguration(챕터 02) → HttpClientModuleAutoConfiguration을 거쳐 개명, @AutoConfiguration + @ComponentScan(이 패키지), 자기 등록(oauth·payment·solapi·bbq·admdongkor를 경유해 web·batch에만 실린다)
    ├── RestClientConfig.java        @Bean RestClientCustomizer restClientTimeoutCustomizer — 모든 Boot RestClient.Builder에 요청 팩토리 connect 5s / read 10s 적용
    └── HttpRequestFactories.java     public static ClientHttpRequestFactory withTimeouts(Duration connect, Duration read) — SimpleClientHttpRequestFactory(HttpURLConnection) 기반, 이 저장소에서 타임아웃 있는 요청 팩토리를 만드는 유일한 지점
```

**이 모듈에는 이제 `exception/` 패키지가 없다.** `RestClientModuleAutoConfiguration`의 `@ComponentScan`은 `com.tastyhouse.restclient.config` 한 패키지뿐이고, `@EnableConfigurationProperties`는 없다(등록할 Properties record가 이 모듈에 남지 않았다). 분리 전에 있던 OAuth REGEX `excludeFilters`와 타 모듈 Properties 등록은 제거됐다 — **모듈 경계(= 의존 선언)가 그 역할을 대신한다.**

### `RestClient.Builder`는 코어가 직접 등록하지 않는다

`RestClient.Builder`는 Boot `RestClientAutoConfiguration`이 **prototype**으로 제공한다 — 이 모듈은 그 빌더에 `RestClientCustomizer`로 요청 팩토리만 얹을 뿐, `RestClient.Builder`/`RestClient` 빈을 자신이 직접 등록하지 않는다. 과거 싱글톤 `WebClient.Builder`에 `TossPaymentClient`가 `@PostConstruct`로 `.baseUrl()`을 clone 없이 박아 공유 빌더를 오염시키던 잠재 버그는, Boot가 매번 새 prototype 빌더를 내주는 지금 구조에서는 성립할 수 없다.

### 벤더 패키지를 `external.file` 아래에 두지 않았던 이유 (패키지 예외 3건 — 이력)

7모듈 분리 당시 코어 스캔이 `com.tastyhouse.external.file`을 대상으로 했으므로, **하위 패키지가 클래스패스에 있으면 동반 스캔됐다.** 즉 `external.file.firebase`·`external.file.s3`를 그대로 뒀다면 코어를 import 한 것만으로 벤더 빈이 딸려 올라왔다. 그래서 이동 시 아래 3건만 패키지를 바꿨다(그 외 이동 파일은 패키지 불변). **파일 저장 SPI 삭제로 그 스캔은 사라졌지만**, 바뀐 패키지는 되돌리지 않는다 — 벤더 모듈마다 겹치지 않는 하위 패키지를 소유한다는 split package 회피 구조가 여전히 필요하기 때문이다.

| 원래 패키지 | 바뀐 패키지 | 소유 모듈 |
|---|---|---|
| `external.file.firebase` | `external.firebase` | firebase |
| `external.file.s3` | `external.aws.s3` | aws-s3 |
| `external.file.RemoteImageDownloader` | `external.crawling.RemoteImageDownloader` → (2분할 후) `external.bbq.RemoteImageDownloader` | crawling → bbq |

같은 취지로 AWS 채널 어댑터도 `external.mail.ses` → `external.aws.ses`, `external.sms.sns` → `external.aws.sns`로 모았다(메시징 스캔에 딸려 오지 않게 하기 위함). split package는 없다 — `external.mail`(messaging) vs `external.aws.ses`(aws-ses), `external.firebase`(firebase) vs `external.aws.s3`(aws-s3)가 각각 다른 모듈에 온전히 속한다. `external.file` 패키지는 SPI 삭제로 어느 모듈에도 존재하지 않는다. **이후 3분할(2026-09-26)로 `external.aws.s3`·`external.aws.ses`·`external.aws.sns`는 옛 `:aws` 한 모듈이 아니라 각각 `aws-s3`·`aws-ses`·`aws-sns` 모듈이 소유한다** — 패키지 세그먼트는 그대로다.

## 과거 판단의 번복 — 파일 저장 SPI 삭제

**경위.** 7모듈 분리(챕터 01) 때 파일 저장은 "코어 SPI + 교체 가능한 벤더 구현" 형태로 나뉘었다. 코어(이 모듈)의 `com.tastyhouse.external.file`에 벤더 전략 인터페이스 `FileStorageStrategy`, 도메인 포트 `com.tastyhouse.domain.file.port.FileStoragePort`를 구현하는 `FileStoragePortAdapter`, `file.*`를 바인딩하는 `FileStorageProperties` 셋을 두고, `infrastructure:firebase`·`infrastructure:aws`(현 `infrastructure:aws-s3`)가 전략을 구현했다. 같은 분리에서 전략의 시그니처가 `MultipartFile` → `byte[]`로 바뀌며 `ByteArrayMultipartFile` 래퍼가 사라지고 코어의 `spring-web` 의존이 끊겼다.

**왜 삭제했나.** 그 `byte[]` 전환의 결과 **`FileStorageStrategy`의 메서드 3개가 `FileStoragePort`와 시그니처가 완전히 같아졌고**, `FileStoragePortAdapter`는 변환 없이 위임만 했다. 중간 두 겹(전략 인터페이스 + 위임 어댑터)이 아무 일도 하지 않았다. `FileStorageProperties`는 주입처가 0건인 죽은 코드였다. 그래서 셋을 삭제하고, 다른 driven 어댑터(`MailSender`·`SmsSender`·`PgPaymentGateway`)처럼 **벤더 구현이 도메인 포트를 직접 구현**하는 형태로 통일했다 — `FirebaseFileStorage`·`S3FileStorage`가 `implements FileStoragePort`다. 둘 다 `@ConditionalOnProperty(file.provider)`로 배타 선택되므로 `FileStoragePort` 빈은 항상 하나이며, 주입받는 쪽은 persistence의 `FileDomainConfig`(`FileUploadService` 생성)와 `FileUrlResolver`다.

**벤더 모듈 분리는 유지한다.** 검토한 대안과 비채택 사유:

- **SPI를 `infrastructure:file-storage`로 옮긴다** — file-storage가 firebase를 `runtimeOnly`로 의존하는데 firebase가 SPI를 구현하려면 file-storage를 의존해야 해 **file-storage ↔ firebase 순환**이 된다.
- **SPI 전용 모듈을 신설한다** — 인터페이스 1개(그것도 도메인 포트와 동형)를 위해 모듈을 하나 늘리는 것이라 얻는 것이 없다.
- 도메인 포트 `FileStoragePort`가 이미 벤더 무관 계약이므로, 벤더 모듈은 `domain`만 의존하면 된다.

**결과.** 코어(현 `infrastructure:restclient`)는 `config/`만 남은 순수 HTTP 코어가 됐다. `infrastructure:firebase`는 코어 의존을 끊어 `domain` + firebase-admin만 갖고, 스타터 `infrastructure:file-storage`는 `runtimeOnly project(':infrastructure:firebase')` 한 줄만 조립한다. 그 결과 파일 저장만 쓰는 **admin-api·ceo-api의 런타임 클래스패스에서 코어 모듈이 빠진다.** web은 oauth·payment·messaging을, batch는 crawling(현 bbq·admdongkor)을 경유해 코어를 계속 갖는다. **이후 3분할(2026-09-26)로 옛 `infrastructure:aws`는 `aws-s3`·`aws-ses`·`aws-sns` 3모듈로 나뉘었다** — `aws-ses`·`aws-sns`는 SES/SNS 실패를 `BusinessException`으로 던지므로(당시 `MailProperties`는 messaging 소유라 `aws-ses`가 그쪽도 의존했다 — messaging 4분할로 `MailProperties`가 삭제돼 그 의존은 사라졌다) 코어 의존을 유지하고, `aws-s3`는 domain + spring-cloud-aws-starter-s3만 가져 코어 의존이 없다.

**이후 RestClient 전환(모듈 리네임과 함께)으로 webflux는 완전히 제거됐다.** 이 모듈의 외부 의존은 `spring-web`·`spring-boot-starter-json`뿐이다(아래 §Dependencies).

## Dependencies

### Internal
- **이 모듈은 `domain`을 의존하지 않는다.** 예외 계약 해체로 `ErrorCodeSpec`·`BusinessException`을 참조하던 유일한 지점(`exception/`)이 사라졌다. 과거 `FileStoragePortAdapter`가 구현하던 `com.tastyhouse.domain.file.port.FileStoragePort`도 이제 벤더 모듈(firebase·aws-s3)이 직접 구현한다.

**`application`에 의존하지 않는다.** 분리 전에는 소셜 로그인 SPI·BBQ·행정동 경계 포트를 구현하느라 `implementation project(':application')`이 있었으나, 그 어댑터들이 전부 oauth·crawling(현 bbq·admdongkor) 모듈로 떠나 코어에는 아웃바운드 계약 소비자가 남지 않았다.

### External
- `spring-web` (`api`) — `RestClientAutoConfiguration`이 등록하는 `RestClient.Builder`를 코어의 `RestClientCustomizer`가 꾸민다. 벤더 어댑터가 이 좌표를 반복 선언하지 않도록 `api`로 노출한다.
- `spring-boot-starter-json` (`api`) — Jackson. 벤더 어댑터가 wire DTO 역직렬화에 그대로 쓴다.
- **webflux·reactor-netty·`WebClient`는 전면 제거됐다.** **AWS SDK·Firebase Admin·jjwt·`spring-boot-starter-mail` 의존도 없다** — 각각 aws-s3/aws-ses/aws-sns·firebase·oauth·javamail 모듈이 소유한다.

## 어댑터 작성 규칙 (10모듈 공통)

이 절은 코어뿐 아니라 `infrastructure:{firebase,aws-s3,aws-ses,aws-sns,oauth,payment,mail,javamail,sms,solapi,bbq,admdongkor}` 전부에 적용된다.

- **외부 HTTP 호출은 코어가 customizer로 꾸민 Boot `RestClient.Builder`를 주입받아 생성자에서 한 번 build한다.** `restClientBuilder.baseUrl(...).build()`로 만든 `RestClient`를 필드로 보유한다(스레드 안전). WebClient·webflux·JDK `java.net.http.HttpClient` 직접 사용은 도입하지 않는다 — 타임아웃 있는 요청 팩토리를 만드는 유일한 지점은 코어의 `HttpRequestFactories.withTimeouts(...)`(`SimpleClientHttpRequestFactory`/`HttpURLConnection` 기반)다. **대용량 응답은 `exchange()` 스트리밍이다**(`retrieve().body(String/...)` 금지 — 선례: admdongkor의 `AdminDongBoundaryClient`).
- **도메인 포트를 구현하되 프레임워크 타입을 시그니처로 누출하지 않는다**: 포트(`MailSender`·`SmsSender`·`FileStoragePort`·`PgPaymentGateway`)는 프레임워크-프리이므로 `RestClient`·SDK 타입·wire DTO가 포트 시그니처에 등장하면 안 된다. 변환은 어댑터 안에서 끝낸다.
- **외부 응답 DTO는 도메인 타입을 보유하지 않는다 (역방향 누수 금지)**: 상세는 `../oauth/AGENTS.md`.
- **자격증명은 코드에 하드코딩하지 않는다**: 환경변수(`.env`)·configtree 시크릿(`SECRETS_DIR`, `../firebase/AGENTS.md`)으로 주입한다.
- **provider 선택은 `@ConditionalOnProperty`로 한다**: `file.provider`·`mail.provider`·`sms.provider`. 조건 애노테이션은 스캔되는 구현 클래스에 붙어 있고, `{Xxx}ModuleConfig`는 조건을 갖지 않는다.
- **에러는 도메인 `BusinessException(ErrorCode.X[, cause])`을 직접 던진다**: 모듈마다 예외 타입을 새로 만들고 전역 핸들러에 `@ExceptionHandler`를 추가하지 않는다. `RestClientResponseException`을 catch해 이 형태로 번역한다(과거 `WebClientResponseException`, 그 이전에는 `ExternalApiException`으로 번역했다 — 지금은 그 중간 타입 없이 곧바로 `BusinessException`이다).
- **전역 타임아웃이 안 맞는 어댑터만 per-client override한다**: `requestFactory(HttpRequestFactories.withTimeouts(connect, read))`로 개별 지정하되, **`MockRestServiceServer.bindTo(builder)` 목 팩토리를 덮어써 단위 테스트가 불가능해지므로** 전역값과 실제로 다를 때만 쓴다(BBQ가 `timeoutSeconds` 프로퍼티를 삭제하고 override를 제거한 이유).

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar. 스타터 `file-storage`를 포함한 11모듈 전부 같다.
- **이 모듈은 설정만 갖는다 — 예외·에러코드를 두지 않는다.** 외부 연동 실패 코드는 도메인 `ErrorCode`가 소유한다. 새 채널 연동을 추가할 때 이 모듈에 예외 타입을 되살리지 않는다(위 [예외 계약 해체](#예외-계약-해체--도메인-errorcode로-흡수)).
- **RestTemplate·WebClient 모듈을 미리 만들어 두지 않는다.** `infrastructure:aws-s3`/`aws-ses`/`aws-sns`가 "만들어 뒀지만 어느 앱도 안 쓰는" 선례이긴 하나, 그것들은 도메인 포트 뒤에서 설정 한 줄로 교체 가능한 구현체다. HTTP 클라이언트는 어댑터 코드가 직접 호출하는 라이브러리라 교체 = 어댑터 재작성이며, 미리 만들면 webflux·reactor-netty가 빌드 그래프로 되돌아온다. WebClient가 필요해지면 그때 `infrastructure:webclient`를 신설한다(1순위는 virtual threads + `RestClient`, 불가피하면 그 모듈 + 가드 예외만 도입).
- **`com.tastyhouse.infrastructure.restclient`로 두지 않는다.** `infrastructure:persistence`의 `@ComponentScan("com.tastyhouse.infrastructure")`가 그 경로도 스캔 대상에 넣어, 진입 설정을 persistence의 스캔이 다시 등록하는 이중 등록이 된다. Spring은 같은 클래스의 중복 스캔을 한 번만 등록하므로 **기동 실패로 이어지지는 않지만**, 등록 주체가 persistence 쪽으로 넘어가 `@AutoConfiguration`의 순서·조건 계약이 무력화된다(redis REGEX 제외와 같은 이유). 이 이동은 후속 프로그램의 stage B 대상이다 — `backend/CLAUDE.md`의 "후속 프로그램 — 벤더 패키지를 `com.tastyhouse.infrastructure.*`로 정렬" 절에서 `restclient`를 벤더·redis와 함께 stage B 대상으로 명시한다.
- **빈 배선 (파일 저장 SPI 삭제로 개정)**: `RestClientModuleAutoConfiguration`은 클래스패스 존재만으로 자동 등록된다. **앱은 이 모듈을 직접 선언하지 않는다** — web은 oauth·payment·solapi를, batch는 bbq·admdongkor를 경유해 전이로 받는다. 스타터 `infrastructure:file-storage`는 더 이상 이 모듈을 조립하지 않으므로(firebase 한 줄), admin-api·ceo-api의 `runtimeClasspath`에는 이 모듈이 없다. 파일 저장의 기동 실패 조건도 이 모듈과 무관해졌다 — `FileStoragePort` 구현이 없으면 persistence의 `FileUrlResolver`·`FileDomainConfig`가 `FileStoragePort` 빈을 찾지 못해 **기동 시** 실패한다(`../file-storage/AGENTS.md`).
- **하위 문서**: 코어에 남은 어댑터 패키지 설명은 `src/main/java/com/tastyhouse/restclient/AGENTS.md`.
- **가드 테스트**: `NoReactiveHttpClientTest`는 `src/test/java/com/tastyhouse/restclient/architecture/`로 이동했다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.external..` 봉인 (벤더 9모듈 공통 — 코어는 대상 아님)

**대상**: 벤더·채널 12개 모듈(`firebase`·`aws-s3`·`aws-ses`·`aws-sns`·`oauth`·`payment`·`mail`·`javamail`·`sms`·`solapi`·`bbq`·`admdongkor`)의 `com.tastyhouse.external..` 패키지 루트. **코어(이 모듈)의 `com.tastyhouse.restclient..`는 이 봉인 대상이 아니다** — 이미 리네임됐다.

위 "코어 패키지는 `com.tastyhouse.restclient..`, 벤더 9모듈은 `com.tastyhouse.external..` 유지" 절과 같은 사실을, **가드로서** 다시 못박는다. 벤더 모듈을 `com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 `@ComponentScan("com.tastyhouse.infrastructure")`가 그 트리를 통째로 스캔하므로 **빈 스캔 범위가 어긋나 admin-api·ceo-api·batch-module의 부팅이 깨진다.** 모듈 디렉터리와 패키지 이름이 어긋나 보인다는 이유로 정리하지 않는다.

### `@ConfigurationProperties` record는 명시 등록한다

**대상**: `backend/infrastructure/restclient/src/main/java/com/tastyhouse/restclient/config/RestClientModuleAutoConfiguration.java` → 클래스 선언부(현재 `@EnableConfigurationProperties` 없음)

`@ConfigurationPropertiesScan`을 쓰지 않는 것이 이 저장소의 방침이다. 파일 저장 SPI 삭제로 이 모듈에 등록할 Properties record가 없어져 `@EnableConfigurationProperties`도 함께 제거됐다. 이후 Properties record를 이 모듈에 추가한다면 컴포넌트 스캔에 맡기지 말고 진입 설정의 `@EnableConfigurationProperties`에 명시한다.

### `RestClient.Builder`/`RestClient` 빈을 직접 등록하지 않는다

**대상**: `backend/infrastructure/restclient/src/main/java/com/tastyhouse/restclient/config/RestClientConfig.java`

Boot가 제공하는 prototype 빌더 + `RestClientCustomizer`만 쓴다. 싱글톤 빌더를 직접 등록하면 과거 `WebClient.Builder` 싱글톤을 `TossPaymentClient`가 `.baseUrl()`로 오염시키던 것과 같은 유형의 버그가 재발할 수 있다.

### `java.net.http.HttpClient`는 쓰지 않는다 — 요청 팩토리는 `HttpRequestFactories.withTimeouts`(Simple)만

**대상**: `backend/infrastructure/restclient/src/main/java/com/tastyhouse/restclient/config/HttpRequestFactories.java`

이 저장소에서 `java.net.http.*`(JDK `HttpClient`) 사용은 **0곳**이다. 타임아웃 있는 요청 팩토리가 필요한 모든 지점(코어의 전역 customizer, admdongkor·bbq의 per-client override)은 `HttpRequestFactories.withTimeouts(connect, read)`(`SimpleClientHttpRequestFactory`/`HttpURLConnection` 기반) 하나만 쓴다.

**JDK 팩토리로 되돌리지 않는다 — Spring 6.1.5의 JDK 팩토리는 본문 read timeout을 적용하지 않는다.** `JdkClientHttpRequestFactory`의 타임아웃은 `HttpRequest.timeout()`/`sendAsync().get(timeout)`으로 구현되어 **응답 헤더가 도착할 때까지만** 제한하고, 헤더 이후 본문 스트리밍이 멎어도(응답이 시작됐지만 끝나지 않는 "본문 정체") 무기한 대기한다 — 과거 reactor `ReadTimeoutHandler(10s)`가 막고 있던 바로 그 실패 양식이 JDK 팩토리로는 재현되지 않는다는 뜻이다. `SimpleClientHttpRequestFactory`는 소켓 `SO_TIMEOUT`으로 구현되어 본문 read 한 번 한 번에 적용되므로(스트리밍 `exchange()`에도 동일하게 적용) 이 회귀가 없다. 부수효과로 JDK `HttpClient`의 HTTP/2 협상 관련 위험도 함께 사라진다. **Spring 6.2.x 이후 이 제약이 실제로 개선됐는지 확인되기 전에는 JDK 팩토리로 되돌리지 않는다.**

### WebClient·webflux 재도입 금지

**대상**: `backend/infrastructure/restclient/src/test/java/com/tastyhouse/restclient/architecture/NoReactiveHttpClientTest.java`

`org.springframework.web.reactive`·`reactor.`·`java.net.http.` **토큰**을 소스 전체(FQN 직접 사용·static import 우회 포함)에서 검사하고, `*/build.gradle`의 `spring-boot-starter-webflux`를 전 `infrastructure/*` 모듈에서 금지한다. 예외는 가드 자신의 파일 1개(경로 전체로 지정)뿐이다 — 이전에는 import 줄 접두어만 검사해 FQN 직접 사용이나 static import로 우회할 수 있었으나, 토큰 검사로 강화해 그 우회를 막았다. 필요해지면 1순위 대안은 Java 21 virtual threads + `RestClient`이고, 그래도 불가피하면 그 모듈만 예외로 도입하되 이 가드의 예외 목록과 그 모듈 AGENTS.md에 근거를 남긴다.

### per-client `requestFactory` override는 전역값과 실제로 다를 때만

**대상**: 벤더 어댑터의 `requestFactory(HttpRequestFactories.withTimeouts(...))` 호출부(예: `admdongkor`의 `AdminDongBoundaryClient`, `bbq`의 `RemoteImageDownloader`)

override는 `MockRestServiceServer.bindTo(builder)`가 심어 둔 목 팩토리를 덮어써 단위 테스트를 불가능하게 만든다. BBQ가 `BbqProperties.timeoutSeconds`를 삭제하고 override 자체를 제거한 것이 이 근거다 — 전역 customizer 값(connect 5s / read 10s)과 실제로 달라야 할 이유가 없으면 override하지 않는다.

### 가드 테스트는 test 태스크 inputs에 infrastructure 소스를 선언해야 재실행된다

**대상**: `backend/infrastructure/restclient/build.gradle` → `test` 태스크의 `inputs.files(fileTree(...))` 선언

`NoReactiveHttpClientTest`는 이 모듈이 아니라 `backend/infrastructure/*/src/**/*.java`와 `*/build.gradle`을 읽는다. Gradle에 그 파일들을 입력으로 선언하지 않으면 그 파일들이 바뀌어도 이 모듈의 `test` 태스크가 UP-TO-DATE로 건너뛰어 회귀를 못 잡는 것을 실측으로 확인했다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 코어 auto-configuration의 발화 조건과 형제 모듈의 자기 등록

**대상**: `backend/infrastructure/restclient/src/main/java/com/tastyhouse/restclient/config/RestClientModuleAutoConfiguration.java`

이 코어는 **클래스패스 존재만으로 활성화**되며, 앱은 이 클래스를 `@Import` 하지 않는다(앱이 직접 선언하지도 않고 어댑터 모듈을 통해 전이로 받는다). 실제 저장소 구현(Firebase·S3)·OAuth·결제·메시징·외부 수집(BBQ·행정동 경계)은 각각 별도 모듈이며, 그 모듈들도 자기 auto-configuration(`FirebaseModuleAutoConfiguration`·`AwsS3ModuleAutoConfiguration`·`AwsSesModuleAutoConfiguration`·`AwsSnsModuleAutoConfiguration`·`OAuthModuleAutoConfiguration`·`PaymentModuleAutoConfiguration`·`MessagingModuleAutoConfiguration`·`BbqModuleAutoConfiguration`·`AdmdongkorModuleAutoConfiguration`)으로 자기 등록한다. 앱은 실제로 쓰는 모듈만 의존한다.

### 예외 계약을 이 모듈에 두지 않는 이유 (결함 이력 — `backend/CLAUDE.md`로 이관)

**대상**: `backend/domain/src/main/java/com/tastyhouse/domain/exception/ErrorCode.java`의 `SMS_SEND_*`·`MAIL_SEND_FAILED`·`ADMIN_DONG_BOUNDARY_FETCH_FAILED` 5개 상수

이 모듈이 한때 소유했던 `ExternalApiException`/`ExternalApiErrorCode`의 3단계 이력(독립 예외 → `BusinessException` 상속 → 완전 해체)은 `backend/CLAUDE.md`의 "예외·에러코드 소유 규칙" 절이 서술한다. 이 모듈에는 그 이력의 결론(예외·에러코드를 두지 않는다)만 남긴다.

### `RestClient.Builder`를 코어가 customizer로 꾸미는 이유와 버퍼 한도 (RestClient 전환으로 개정)

**대상**: `backend/infrastructure/restclient/src/main/java/com/tastyhouse/restclient/config/RestClientConfig.java`

동기 HTTP 클라이언트(타임아웃 포함)를 코어가 customizer로 꾸민다. OAuth·결제 등 외부 연동 클라이언트가 Boot `RestClient.Builder`를 주입받아 build하므로, 이 설정을 코어에 두어 코어를 받는 모든 앱에서 customizer가 적용되게 한다. **`RestClient`는 WebClient의 `maxInMemorySize` 같은 버퍼 상한이 없다** — 대신 대용량 응답을 다루는 어댑터는 `retrieve().body(...)`가 아니라 `exchange()` 스트리밍 + `BoundedInputStream`으로 힙 사용을 직접 제어한다(선례: admdongkor 모듈의 `AdminDongBoundaryClient`).
