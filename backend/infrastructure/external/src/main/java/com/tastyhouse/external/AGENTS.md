<!-- Parent: ../../../../../../AGENTS.md -->

# external (코어 어댑터 패키지)

`infrastructure:external` 코어 모듈의 자바 패키지 루트. **이 디렉터리에는 `config/`·`exception/` 둘만 남는다.** 7모듈 분리(챕터 01) 직후에는 `file/`까지 셋이었으나 파일 저장 SPI 삭제로 사라졌다(모듈 문서의 "과거 판단의 번복 — 파일 저장 SPI 삭제" 절). 모듈 차원의 배경·분리 근거는 `../../../../../../AGENTS.md`(= `infrastructure/external/AGENTS.md`) 참조.

## Purpose
외부 연동 모듈들이 공통으로 쓰는 `WebClient` 빌더와 외부 연동 예외 계약을 소유하는 순수 HTTP 코어다. 도메인 포트 `FileStoragePort`는 이 패키지가 아니라 벤더 모듈(`infrastructure:firebase`의 `FirebaseFileStorage`, `infrastructure:aws-s3`의 `S3FileStorage`)이 직접 구현한다.

## Packages
| Package | Purpose |
|---------|---------|
| `config/` | `ExternalModuleAutoConfiguration`(진입 설정 — 챕터 02로 `ExternalModuleConfig`에서 리네임 + `@AutoConfiguration`, 스캔 범위는 `external.config` 한 패키지) · `WebClientConfig`(`WebClient.Builder` 빈) |
| `exception/` | `ExternalApiException`(`BusinessException` 상속) · `ExternalApiErrorCode`(`ErrorCodeSpec` 구현). 7모듈 전부 이 예외로 실패를 표현한다 |

## 다른 모듈로 이동한 패키지
아래는 과거 이 디렉터리에 있었고, 지금은 각 모듈이 소유한다. 패키지 이름이 바뀐 것은 표에 별도 표시했다(사유는 모듈 문서의 "벤더 패키지를 `external.file` 아래에 두지 않았던 이유" 절).

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

## For AI Agents

### Working In This Directory
- **포트 구현 시 프레임워크 타입을 누출하지 않는다**: 도메인 포트(`FileStoragePort` 등)는 프레임워크-프리이므로 `MultipartFile`·SDK 타입·`WebClient` 타입이 시그니처에 등장하면 안 된다. 과거 코어의 파일 저장 전략이 `byte[]`로 바뀌며 이 모듈의 `spring-web` 의존이 사라진 것이 그 원칙의 결과다.
- **파일 저장 코드를 이 디렉터리에 되살리지 않는다**: 벤더 무관 계약은 도메인 포트 `FileStoragePort`(domain)가 이미 맡고, 벤더 구현은 별도 모듈(`infrastructure:{벤더}`)이 자기 패키지(`external.{벤더}`)에서 그 포트를 직접 구현한다. 도메인 포트와 동형인 전략 인터페이스·위임 어댑터를 다시 두지 않는다.
- **예외는 `ExternalApiException`으로 던진다**: 새 예외 타입을 만들어 전역 핸들러에 전용 `@ExceptionHandler`를 추가하지 않는다(`BusinessException` 단일 계층 규칙).
- **자격증명은 코드에 하드코딩하지 않는다**: 환경변수(`.env`) 또는 configtree 시크릿(`SECRETS_DIR`)으로 주입한다.

### Testing Requirements
- 외부 호출은 모킹한다. 실네트워크 테스트는 `@Disabled`로 빌드 게이트에서 제외한다(선례: bbq 모듈의 `BbqApiClientTest`).

### Common Patterns
- HTTP 호출은 `WebClientConfig`가 제공하는 `WebClient.Builder`를 주입받아 구성한다. 대용량 응답은 예외이며 `HttpClient` + 스트리밍 파서를 쓴다(선례: admdongkor 모듈의 `AdminDongBoundaryClient`).
- provider 선택은 구현 클래스의 `@ConditionalOnProperty`로 한다(`file.provider`·`mail.provider`·`sms.provider`). 파일 저장의 `file.provider` 값은 스타터 `infrastructure:file-storage`가 소유하고, 파일 저장은 이 모듈을 거치지 않으므로 벤더를 바꿀 때 이 모듈은 손대지 않는다.

## Dependencies

### Internal
- `domain` — 예외 계약만: `exception/`의 `ErrorCodeSpec`·`BusinessException` (`file/port/FileStoragePort`는 더 이상 이 모듈이 구현하지 않는다)

### External
- `spring-boot-starter-webflux` (`WebClient`). **이것 하나뿐이다** — AWS SDK·Firebase Admin·jjwt·starter-mail·spring-web은 전부 분리된 모듈이 소유한다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 이 패키지 루트를 `com.tastyhouse.infrastructure` 아래로 옮기지 않는다

**대상**: `backend/infrastructure/external/src/main/java/com/tastyhouse/external/` (디렉터리 자체)

`infrastructure:persistence`의 `PersistenceModuleAutoConfiguration`이 `@ComponentScan("com.tastyhouse.infrastructure")`로 그 트리를 통째 스캔하므로, 이 패키지를 그 아래로 옮기면 **의존하지 않은 어댑터까지 스캔 대상이 되어 admin-api·ceo-api·batch-module의 부팅이 깨진다.** 모듈 이름(`infrastructure:external`)과 패키지 이름(`com.tastyhouse.external`)이 어긋나 보이는 것은 의도된 것이며, 외부 연동 9모듈 전부에 동일하게 적용된다. 모듈 차원의 서술은 `../../../../../../AGENTS.md`.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### `exception/`의 예외가 502를 지키는 방식

**대상**: `backend/infrastructure/external/src/main/java/com/tastyhouse/external/exception/ExternalApiException.java`

`BusinessException` 상속이라 각 api 모듈의 기존 `BusinessException` 핸들러가 그대로 처리한다. 독립 예외였던 시절에는 admin-api·ceo-api에 전용 핸들러가 없어 **502로 의도된 외부 연동 실패가 `Exception` 폴백을 타고 500으로 나가는 결함**이 있었다. `ExternalApiErrorCode`는 SMS·Mail·Region 세 갈래로 상수를 묶어 선언한다.
