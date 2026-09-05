<!-- Parent: ../../../../../../AGENTS.md -->

# external (코어 어댑터 패키지)

`infrastructure:external` 코어 모듈의 자바 패키지 루트. **7모듈 분리(챕터 01) 이후 이 디렉터리에는 `config/`·`exception/`·`file/` 셋만 남는다.** 모듈 차원의 배경·분리 근거는 `../../../../../AGENTS.md`(= `infrastructure/external/AGENTS.md`) 참조.

## Purpose
`domain-module`이 `file/port/`에 선언한 출력 포트 `FileStoragePort`를 구현하고, 7모듈 공통으로 쓰는 `WebClient` 빌더와 외부 연동 예외 계약을 소유한다. 벤더 구현(Firebase·S3)은 이 패키지에 두지 않는다.

## Packages
| Package | Purpose |
|---------|---------|
| `config/` | `ExternalModuleAutoConfiguration`(진입 설정 — 챕터 02로 `ExternalModuleConfig`에서 리네임 + `@AutoConfiguration`, 스캔 범위는 `external.config`·`external.file` 두 패키지) · `WebClientConfig`(`WebClient.Builder` 빈) |
| `exception/` | `ExternalApiException`(`BusinessException` 상속) · `ExternalApiErrorCode`(`ErrorCodeSpec` 구현). 7모듈 전부 이 예외로 실패를 표현한다 |
| `file/` | 파일 저장 코어 SPI — `FileStorageStrategy`(벤더 전략 인터페이스, `byte[]` 기반) · `FileStoragePortAdapter`(도메인 포트 구현, 전략에 그대로 위임) · `FileStorageProperties`(`file.*`) |

## 다른 모듈로 이동한 패키지
아래는 과거 이 디렉터리에 있었고, 지금은 각 모듈이 소유한다. 패키지 이름이 바뀐 것은 표에 별도 표시했다(사유는 모듈 문서의 "벤더 패키지를 `external.file` 아래에 두지 않는다" 절).

| 과거 패키지 | 현재 |
|---|---|
| `oauth/{kakao,naver,apple,facebook}` | → `infrastructure:oauth` (패키지 불변) |
| `payment/toss` | → `infrastructure:payment` (패키지 불변) |
| `mail/`, `mail/javamail`, `sms/`, `sms/solapi` | → `infrastructure:messaging` (패키지 불변) |
| `mail/ses`, `sms/sns` | → `infrastructure:aws` (`external.aws.ses` · `external.aws.sns`로 **변경**) |
| `file/firebase` | → `infrastructure:firebase` (`external.firebase`로 **변경**) |
| `file/s3` | → `infrastructure:aws` (`external.aws.s3`로 **변경**) |
| `crawling/bbq`, `region/` | → `infrastructure:crawling` (패키지 불변) |
| `file/RemoteImageDownloader` | → `infrastructure:crawling` (`external.crawling`으로 **변경**) |
| `file/ByteArrayMultipartFile` | **삭제** — `FileStorageStrategy`가 `byte[]`를 받게 되어 래퍼가 불필요해졌다 |

## For AI Agents

### Working In This Directory
- **포트 구현 시 프레임워크 타입을 누출하지 않는다**: `FileStoragePort`는 프레임워크-프리이므로 `MultipartFile`·SDK 타입·`WebClient` 타입이 시그니처에 등장하면 안 된다. `FileStorageStrategy`가 `byte[]`를 받는 것이 그 원칙을 코어에서 관철한 결과이며, 이 전환으로 코어의 `spring-web` 의존이 사라졌다.
- **벤더 구현을 `file/` 아래에 새로 만들지 않는다**: `ExternalModuleAutoConfiguration`(구 `ExternalModuleConfig`)가 `com.tastyhouse.external.file`을 스캔하므로 하위 패키지가 동반 스캔된다. 새 저장소 전략은 별도 모듈(`infrastructure:{벤더}`)에 자기 패키지(`external.{벤더}`)로 둔다.
- **예외는 `ExternalApiException`으로 던진다**: 새 예외 타입을 만들어 전역 핸들러에 전용 `@ExceptionHandler`를 추가하지 않는다(`BusinessException` 단일 계층 규칙).
- **자격증명은 코드에 하드코딩하지 않는다**: 환경변수(`.env`) 또는 configtree 시크릿(`SECRETS_DIR`)으로 주입한다.

### Testing Requirements
- 외부 호출은 모킹한다. 실네트워크 테스트는 `@Disabled`로 빌드 게이트에서 제외한다(선례: crawling 모듈의 `BbqApiClientTest`).

### Common Patterns
- HTTP 호출은 `WebClientConfig`가 제공하는 `WebClient.Builder`를 주입받아 구성한다. 대용량 응답은 예외이며 `HttpClient` + 스트리밍 파서를 쓴다(선례: crawling 모듈의 `AdminDongBoundaryClient`).
- provider 선택은 구현 클래스의 `@ConditionalOnProperty`로 한다(`file.provider` 등).

## Dependencies

### Internal
- `domain-module` — `file/port/FileStoragePort`, `exception/`의 `ErrorCodeSpec`·`BusinessException`

### External
- `spring-boot-starter-webflux` (`WebClient`). **이것 하나뿐이다** — AWS SDK·Firebase Admin·jjwt·starter-mail·spring-web은 전부 분리된 모듈이 소유한다.
