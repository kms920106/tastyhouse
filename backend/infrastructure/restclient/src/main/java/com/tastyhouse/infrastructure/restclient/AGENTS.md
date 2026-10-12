<!-- Parent: ../../../../../../../AGENTS.md -->

# restclient (코어 어댑터 패키지)

`infrastructure:restclient` 코어 모듈의 자바 패키지 루트 `com.tastyhouse.infrastructure.restclient`. **이 디렉터리에는 클래스 2개(`RestClientConfig`·`HttpRequestFactories`)만 있고 하위 패키지가 없다.** 파일 저장 SPI(`file/`)와 예외 계약(`exception/`)은 두지 않는다 — 그 이유는 모듈 문서의 "파일 저장 SPI를 두지 않는 이유"·"예외·에러코드를 두지 않는 이유" 절. 모듈 차원의 배경은 `../../../../../../../AGENTS.md`(= `infrastructure/restclient/AGENTS.md`) 참조.

## Purpose
외부 연동 모듈들이 공통으로 쓰는 `RestClient.Builder` customizer(요청 팩토리 타임아웃)를 소유하는 순수 HTTP 코어다. 포트 `FileStoragePort`(`application.file.port.out`)는 이 패키지가 아니라 벤더 모듈(`infrastructure:firebase`의 `FirebaseFileStorage`, `infrastructure:aws-s3`의 `S3FileStorage`)이 직접 구현하고, 외부 연동 실패 코드는 이 패키지가 아니라 그 실패를 번역하는 앱 모듈의 `WebErrorCode`·`BatchErrorCode`가 소유한다(어댑터는 예외를 던지지 않고 결과 record를 돌려준다).

## Packages
| Package | Purpose |
|---------|---------|
| (루트) `com.tastyhouse.infrastructure.restclient` | `RestClientConfig`(`RestClientCustomizer` 빈 — 모든 Boot `RestClient.Builder`에 타임아웃 적용) · `HttpRequestFactories`(`withTimeouts(connect, read)` — 소켓 read timeout이 본문에도 적용되는 `SimpleClientHttpRequestFactory`를 만드는 유일한 지점. per-client override도 이것을 쓴다). 진입 설정 클래스는 없고, 앱 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔이 등록한다 |

외부 연동 기능별 소유 모듈은 모듈 문서의 "외부 연동 모듈 지도" 표를 본다.

## For AI Agents

### Working In This Directory
- **포트 구현 시 프레임워크 타입을 누출하지 않는다**: 포트(`FileStoragePort` 등)는 프레임워크-프리이므로 `MultipartFile`·SDK 타입·`RestClient` 타입이 시그니처에 등장하면 안 된다.
- **파일 저장 코드를 이 디렉터리에 두지 않는다**: 벤더 무관 계약은 `application`의 포트 `FileStoragePort`(`application.file.port.out`)가 맡고, 벤더 구현은 별도 모듈(`infrastructure:{벤더}`)이 자기 패키지(`com.tastyhouse.infrastructure.{벤더}`)에서 그 포트를 직접 구현한다. 그 포트와 동형인 전략 인터페이스·위임 어댑터를 두지 않는다.
- **예외·에러코드를 이 디렉터리에 두지 않는다**: 새 예외 타입을 만들어 전역 핸들러에 전용 `@ExceptionHandler`를 추가하지 않는다. 외부 연동 실패 코드가 새로 필요하면 이 패키지가 아니라 그 실패를 번역하는 application 모듈의 에러코드 enum(`backend/web-application/src/main/java/com/tastyhouse/application/shared/exception/WebErrorCode.java` 등)에 추가한다.
- **자격증명은 코드에 하드코딩하지 않는다**: 환경변수(`.env`) 또는 configtree 시크릿(`SECRETS_DIR`)으로 주입한다.

### Testing Requirements
- 외부 호출은 `MockRestServiceServer.bindTo(RestClient.builder())`로 모킹한다(스프링 컨텍스트 없이 생성자 직접 호출). 실네트워크 테스트는 `@Disabled`로 빌드 게이트에서 제외한다(선례: bbq 모듈의 `BbqApiClientTest`).

### Common Patterns
- HTTP 호출은 코어가 customizer로 꾸민 Boot `RestClient.Builder`를 주입받아 생성자에서 `build()`한다. 대용량 응답은 예외이며 `exchange()` 스트리밍 파서를 쓴다(선례: admdongkor 모듈의 `AdminDongBoundaryClient`).
- provider 선택은 구현 클래스의 `@ConditionalOnProperty`로 한다(`file.provider`·`mail.provider`·`sms.provider`). 파일 저장의 `file.provider` 값은 스타터 `infrastructure:file-storage`가 소유하고, 파일 저장은 이 모듈을 거치지 않으므로 벤더를 바꿀 때 이 모듈은 손대지 않는다.

## Dependencies

### Internal
- **없음.** 이 모듈은 `domain`을 포함해 어떤 내부 모듈도 의존하지 않는다(`FileStoragePort`는 벤더 모듈이 직접 구현한다).

### External
- `spring-web`(`api`) + `spring-boot-starter-json`(`api`). **webflux·reactor-netty는 없다** — AWS SDK·Firebase Admin·jjwt·starter-mail은 전부 분리된 모듈이 소유한다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### main 클래스는 전부 `com.tastyhouse.infrastructure.restclient` 아래에 둔다

**대상**: `backend/infrastructure/restclient/src/test/java/com/tastyhouse/infrastructure/restclient/architecture/PackageRootTest.java` → `shouldResideInModuleRootPackage`

이 모듈의 main 클래스는 전부 `com.tastyhouse.infrastructure.restclient` 아래에 둔다(`PackageRootTest`가 강제, ArchUnit은 `testImplementation`으로만 선언). 하위 패키지를 새로 만들 수는 있지만 루트 밖(`com.tastyhouse.restclient` 등)에 두지 않는다. 모듈 차원의 서술은 `../../../../../../../AGENTS.md`.

### 최상위 클래스는 `HttpRequestFactories`만 public

**대상**: `backend/infrastructure/restclient/src/test/java/com/tastyhouse/infrastructure/restclient/architecture/PackageRootTest.java` → `topLevelClassesShouldNotBePublic`

이 패키지의 최상위 타입은 `HttpRequestFactories`(다른 모듈 `bbq`·`admdongkor`가 호출)를 빼고 전부 package-private이다. 상세는 모듈 문서 `backend/infrastructure/restclient/AGENTS.md`의 같은 항목과 `backend/CLAUDE.md`의 "접근 제어자 규칙 (내부 구현은 package-private)" 절.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 예외 계약을 두지 않는 이유

**대상**: 이 디렉터리(`com.tastyhouse.infrastructure.restclient`) — `exception/` 부재

외부 연동 실패는 채널 발송 실패이지 HTTP 클라이언트 오류가 아니므로, 그 실패 코드는 이 패키지가 아니라 실패를 번역하는 앱 모듈의 `WebErrorCode`·`BatchErrorCode`가 소유한다. 생성자 위임만 하는 전용 예외 서브클래스를 두면 전용 핸들러가 없는 앱에서 의도한 502 대신 `Exception` 폴백의 500으로 응답되는 결함이 생긴다. 상세는 모듈 문서의 "예외·에러코드를 두지 않는 이유" 절과 `backend/CLAUDE.md`의 "예외·에러코드 소유 규칙" 절.
