<!-- Parent: ../../../../../../AGENTS.md -->

# restclient (코어 어댑터 패키지)

`infrastructure:restclient` 코어 모듈의 자바 패키지 루트(구 `com.tastyhouse.external`). **이 디렉터리에는 `config/`만 남는다.** 7모듈 분리(챕터 01) 직후에는 `file/`까지 있었으나 파일 저장 SPI 삭제로 사라졌고(모듈 문서의 "과거 판단의 번복 — 파일 저장 SPI 삭제" 절), 이후 `exception/`도 완전히 해체됐다(모듈 문서의 "예외 계약 해체 — 도메인 `ErrorCode`로 흡수" 절). 모듈 리네임(`infrastructure:external` → `infrastructure:restclient`)과 함께 이 코어 패키지도 `com.tastyhouse.restclient`로 옮겨졌다 — 벤더 9모듈의 패키지(`com.tastyhouse.external.*`)는 불변이다. 모듈 차원의 배경·분리 근거는 `../../../../../../AGENTS.md`(= `infrastructure/restclient/AGENTS.md`) 참조.

## Purpose
외부 연동 모듈들이 공통으로 쓰는 `RestClient.Builder` customizer(요청 팩토리 타임아웃)를 소유하는 순수 HTTP 코어다. 도메인 포트 `FileStoragePort`는 이 패키지가 아니라 벤더 모듈(`infrastructure:firebase`의 `FirebaseFileStorage`, `infrastructure:aws-s3`의 `S3FileStorage`)이 직접 구현하고, 외부 연동 실패 코드는 이 패키지가 아니라 도메인 `ErrorCode`가 소유한다(어댑터는 `BusinessException`을 직접 던진다).

## Packages
| Package | Purpose |
|---------|---------|
| `config/` | `RestClientModuleAutoConfiguration`(진입 설정 — 구 `ExternalModuleAutoConfiguration` → `HttpClientModuleAutoConfiguration`을 거쳐 개명, `@AutoConfiguration`, 스캔 범위는 `restclient.config` 한 패키지) · `RestClientConfig`(`RestClientCustomizer` 빈 — 모든 Boot `RestClient.Builder`에 타임아웃 적용) · `HttpRequestFactories`(`withTimeouts(connect, read)` — 소켓 read timeout이 본문에도 적용되는 `SimpleClientHttpRequestFactory`를 만드는 유일한 지점. per-client override도 이것을 쓴다) |

## 다른 모듈로 이동한 패키지
아래는 과거 이 디렉터리에 있었고, 지금은 각 모듈이 소유한다. 패키지 이름이 바뀐 것은 표에 별도 표시했다(사유는 모듈 문서의 "벤더 패키지를 `external.file` 아래에 두지 않았던 이유" 절). 이 표의 대상은 전부 벤더 패키지(`com.tastyhouse.external.*`)이며, 코어 자신의 `config`가 `com.tastyhouse.restclient`로 옮겨간 것과는 별개다.

| 과거 패키지 | 현재 |
|---|---|
| `oauth/{kakao,naver,apple,facebook}` | → `infrastructure:oauth` (패키지 불변) |
| `payment/toss` | → `infrastructure:payment` (패키지 불변) |
| `mail/`, `mail/javamail`, `sms/`, `sms/solapi` | → `infrastructure:messaging` (패키지 불변) |
| `mail/ses` | → `infrastructure:aws-ses` (`external.aws.ses`로 **변경**) |
| `sms/sns` | → `infrastructure:aws-sns` (`external.aws.sns`로 **변경**) |
| `file/firebase` | → `infrastructure:firebase` (`external.firebase`로 **변경**) |
| `file/s3` | → `infrastructure:aws-s3` (`external.aws.s3`로 **변경**) |
| `crawling/bbq`, `region/` | → `infrastructure:crawling` (패키지 불변) → 이후 2분할로 `infrastructure:bbq`(`external.bbq`)·`infrastructure:admdongkor`(`external.admdongkor`) |
| `file/RemoteImageDownloader` | → `infrastructure:crawling` (`external.crawling`으로 **변경**) → 이후 `infrastructure:bbq`(`external.bbq`) |
| `file/ByteArrayMultipartFile` | **삭제** — 당시 전략 인터페이스 `FileStorageStrategy`가 `byte[]`를 받게 되어 래퍼가 불필요해졌다 |
| `file/{FileStorageStrategy,FileStoragePortAdapter,FileStorageProperties}` | **삭제 (벤더 어댑터가 `FileStoragePort`를 직접 구현)** — 전략 시그니처가 도메인 포트와 같아져 위임만 남았고, Properties는 주입처 0건이었다 |
| `exception/{ExternalApiException,ExternalApiErrorCode}` | **삭제 (5개 상수는 도메인 `ErrorCode`로 이관, 어댑터는 `BusinessException`을 직접 던진다)** — 상세는 모듈 문서의 "예외 계약 해체" 절 |

## For AI Agents

### Working In This Directory
- **포트 구현 시 프레임워크 타입을 누출하지 않는다**: 도메인 포트(`FileStoragePort` 등)는 프레임워크-프리이므로 `MultipartFile`·SDK 타입·`RestClient` 타입이 시그니처에 등장하면 안 된다. 과거 코어의 파일 저장 전략이 `byte[]`로 바뀌며 이 모듈의 `spring-web` 의존이 사라졌다가(이후 RestClient 전환으로 `spring-web`이 코어의 정식 `api` 의존으로 되돌아왔다) 그 원칙 자체는 바뀌지 않았다.
- **파일 저장 코드를 이 디렉터리에 되살리지 않는다**: 벤더 무관 계약은 도메인 포트 `FileStoragePort`(domain)가 이미 맡고, 벤더 구현은 별도 모듈(`infrastructure:{벤더}`)이 자기 패키지(`external.{벤더}`)에서 그 포트를 직접 구현한다. 도메인 포트와 동형인 전략 인터페이스·위임 어댑터를 다시 두지 않는다.
- **예외·에러코드를 이 디렉터리에 되살리지 않는다**: 새 예외 타입을 만들어 전역 핸들러에 전용 `@ExceptionHandler`를 추가하지 않는다(`BusinessException` 단일 계층 규칙). 외부 연동 실패 코드가 새로 필요하면 이 패키지가 아니라 도메인 `ErrorCode`(`backend/domain/src/main/java/com/tastyhouse/domain/exception/ErrorCode.java`)에 추가한다.
- **자격증명은 코드에 하드코딩하지 않는다**: 환경변수(`.env`) 또는 configtree 시크릿(`SECRETS_DIR`)으로 주입한다.

### Testing Requirements
- 외부 호출은 `MockRestServiceServer.bindTo(RestClient.builder())`로 모킹한다(스프링 컨텍스트 없이 생성자 직접 호출). 실네트워크 테스트는 `@Disabled`로 빌드 게이트에서 제외한다(선례: bbq 모듈의 `BbqApiClientTest`).

### Common Patterns
- HTTP 호출은 코어가 customizer로 꾸민 Boot `RestClient.Builder`를 주입받아 생성자에서 `build()`한다. 대용량 응답은 예외이며 `exchange()` 스트리밍 파서를 쓴다(선례: admdongkor 모듈의 `AdminDongBoundaryClient`).
- provider 선택은 구현 클래스의 `@ConditionalOnProperty`로 한다(`file.provider`·`mail.provider`·`sms.provider`). 파일 저장의 `file.provider` 값은 스타터 `infrastructure:file-storage`가 소유하고, 파일 저장은 이 모듈을 거치지 않으므로 벤더를 바꿀 때 이 모듈은 손대지 않는다.

## Dependencies

### Internal
- **없음.** 예외 계약 해체로 이 모듈은 이제 `domain`을 포함해 어떤 내부 모듈도 의존하지 않는다(`file/port/FileStoragePort`는 벤더 모듈이 직접 구현한다).

### External
- `spring-web`(`api`) + `spring-boot-starter-json`(`api`). **webflux·reactor-netty는 없다** — AWS SDK·Firebase Admin·jjwt·starter-mail은 전부 분리된 모듈이 소유한다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 벤더 패키지(`com.tastyhouse.external..`)를 `com.tastyhouse.infrastructure` 아래로 옮기지 않는다

**대상**: 벤더 9모듈의 `com.tastyhouse.external.*` 패키지 루트(이 디렉터리 자신의 `com.tastyhouse.restclient`는 대상 아님)

`infrastructure:persistence`의 `PersistenceModuleAutoConfiguration`이 `@ComponentScan("com.tastyhouse.infrastructure")`로 그 트리를 통째 스캔하므로, 벤더 패키지를 그 아래로 옮기면 **의존하지 않은 어댑터까지 스캔 대상이 되어 admin-api·ceo-api·batch-module의 부팅이 깨진다.** 코어만 `com.tastyhouse.restclient`로 옮긴 것은 코어에 그 스캔 트리에 걸리는 벤더 빈이 없기 때문이며, 벤더 9모듈에는 이 제약이 그대로 적용된다. 모듈 차원의 서술은 `../../../../../../AGENTS.md`.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 예외 계약을 두지 않는 이유

**대상**: 이 디렉터리(`com.tastyhouse.restclient`) — `exception/` 부재

과거 `exception/ExternalApiException`은 `BusinessException` 상속이라 각 api 모듈의 기존 `BusinessException` 핸들러가 그대로 처리했다. 독립 예외였던 시절에는 admin-api·ceo-api에 전용 핸들러가 없어 **502로 의도된 외부 연동 실패가 `Exception` 폴백을 타고 500으로 나가는 결함**이 있었다. 이후 그 예외 자체가 생성자 위임만 하는 빈 서브클래스로 확인돼 완전히 해체됐고, 5개 상수(과거 SMS·Mail·Region 세 갈래)는 도메인 `ErrorCode`로 이관됐다. 이력 전체는 `backend/CLAUDE.md`의 "예외·에러코드 소유 규칙" 절 참조.
