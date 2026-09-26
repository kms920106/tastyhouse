<!-- Parent: ../../AGENTS.md -->

# infrastructure:bbq

배치 전용 BBQ 메뉴 수집 어댑터 모듈(`java-library`). 아웃바운드 계약 `BbqMenuPort`·`RemoteImagePort`(`com.tastyhouse.application.crawling.bbq.port.out`)를 각각 `BbqMenuAdapter`·`RemoteImageDownloader`가 구현한다.

## 이 모듈이 `crawling`에서 갈라진 이유 (2026-09-26)

과거에는 행정동 경계 수집과 함께 `infrastructure:crawling` 한 모듈에 있었다. 두 수집은 HTTP 클라이언트·구현 포트·yml 접두사·소비 유스케이스를 하나도 공유하지 않아 `bbq`·`admdongkor` 두 모듈로 나눴다. 근거와 "클래스패스 이득은 없다"는 판단은 `../admdongkor/AGENTS.md`의 같은 절에 있다.

**`RemoteImageDownloader`는 세 번째 책임이 아니라 BBQ 덩어리다.** 구현하는 포트 `RemoteImagePort`가 BBQ 패키지(`crawling.bbq.port.out`) 소유이고, 소비자도 `BbqService`(BBQ 메뉴 이미지 수집) 하나뿐이다. 판정 기준은 "어느 포트를 구현하는가"다. 다른 수집이 원격 이미지 저장을 필요로 하게 되면 그때 포트를 공용 위치로 올리고 이 클래스의 소속을 다시 판단한다.

## 무엇을 소유하는가

```
com.tastyhouse.external.bbq/
├── BbqModuleAutoConfiguration.java   @AutoConfiguration + @ComponentScan(이 패키지) + @EnableConfigurationProperties(BbqProperties)
├── BbqApiClient.java                 BBQ 메뉴 API 호출 (RestClient, 동기)
├── BbqMenuAdapter.java               BbqMenuPort 구현
├── BbqProperties.java                bbq.api.* (base-url만 — timeoutSeconds는 삭제됨)
├── RemoteImageDownloader.java        RemoteImagePort 구현 — 원격 이미지를 받아 FileUploadService로 저장 (RestClient, per-client 5s/30s, 10MB 응답 상한)
└── dto/  BbqMenuCategoryResponse · BbqMenuResponse · BbqMenuSubOptionResponse
```

패키지는 분리 때 바뀌었다 — `external.crawling.bbq` → **`external.bbq`**, `external.crawling.RemoteImageDownloader` → **`external.bbq.RemoteImageDownloader`**(모듈명과 맞춤). 포트·DTO는 전부 `application` 소유라 소비자 import 변경은 0건이었다.

## 어느 앱이 의존하는가

**batch-module 하나뿐이다**(`runtimeOnly project(':infrastructure:bbq')`). 클래스패스 존재만으로 `BbqModuleAutoConfiguration`이 발화하므로 `@Import`는 없다. batch는 이 모듈을 경유해 코어 `infrastructure:restclient`(`RestClient.Builder` customizer뿐 — 예외·에러코드 없음)를 전이로 받는다.

## ⚠️ `RemoteImageDownloader`는 persistence가 등록하는 빈에 런타임 의존한다

생성자로 `com.tastyhouse.domain.file.service.FileUploadService`를 요구하는데, 그것은 순수 POJO 도메인 서비스라 **`infrastructure:persistence`의 `FileDomainConfig`가 `@Bean`으로 등록**한다. 즉 이 모듈만 의존하고 `infrastructure:persistence`를 빼면 빈 부재로 기동에 실패한다(batch-module은 둘 다 의존하므로 성립한다). 컴파일 의존은 `domain`이고 빈 제공자는 persistence라, **컴파일이 통과해도 배선이 보장되지 않는 지점**이다. 파일 저장 자체는 `FileUploadService`가 도메인 포트 `FileStoragePort`(스타터 `infrastructure:file-storage` → firebase)로 위임한다.

## yml — `application-bbq.yml`

**batch-module만** `spring.config.import`로 로딩한다(분리 전 파일명은 `application-crawling.yml`). `bbq.api.base-url` 하나를 담는다. **접두사는 분리 때 `crawling.bbq.api` → `bbq.api`로 바뀌었다** — 환경변수 오버라이드가 없는 값이라 yml 한 줄만 따라 바뀌었다. **`BbqProperties.timeoutSeconds`는 삭제됐다** — yml에서 한 번도 지정된 적이 없어 값이 항상 기본 10초였고, 이는 코어 `RestClientConfig`의 전역 read 타임아웃(10초)과 같았다. 남겨 두면 그 값을 쓰기 위한 per-client `requestFactory` override가 필요해 `MockRestServiceServer` 기반 단위 테스트가 불가능해지므로 삭제하고 전역 타임아웃에 맡겼다.

**yml import 누락은 기동으로 드러나지 않는다.** `baseUrl`에 기본값이 없어 null이 되지만 부팅은 성공하고, 실패는 BBQ 동기화 잡이 실제로 돌 때 `null/api/...` URL로 드러난다. batch `application.yml`의 import 줄을 지우지 않는다.

## 테스트

`src/test/.../external/bbq/BbqApiClientTest`는 **`MockRestServiceServer.bindTo(RestClient.builder())` 기반 단위 테스트(3건)로 교체됐다** — 과거 실네트워크(bbq.co.kr) 호출을 `@Disabled`로 빌드 게이트에서 제외하던 `@SpringBootTest`를 대체한다. `BbqApiClient`가 동기 메서드로 전환되고 per-client `requestFactory` override가 사라져(위 §yml) 스프링 컨텍스트 없이 생성자 직접 호출 + 목 서버 바인딩만으로 검증할 수 있게 됐다.

## Dependencies

### Internal
- `infrastructure:restclient` (implementation) — `RestClient.Builder` customizer만(예외·에러코드는 도메인 `ErrorCode` 소유)
- `application` (implementation) — 구현하는 아웃바운드 계약 `BbqMenuPort`·`RemoteImagePort`와 포트 DTO의 소유 모듈. adapter → port 방향이며 순환이 아니다
- `domain` (implementation) — `FileUploadService`·`FileUploadCommand`·`UploadedFileId`, 예외 계약
- **런타임 의존(빌드 그래프에 없음)**: `infrastructure:persistence`의 `FileDomainConfig`가 등록하는 `FileUploadService` 빈

### External
- webflux 없음 — BBQ API 호출은 **동기 `RestClient`**(`BbqApiClient`의 `getMenuCategories`·`getMenusByCategoryId`·`getMenuDetail`·`getMenuSubOptions` 4개 메서드, 과거 Mono 반환 + `...Sync()` 래퍼는 제거됐다). 코어 `infrastructure:restclient`가 `api`로 노출하는 `spring-web`·`spring-boot-starter-json`을 전이로 받는다. `RemoteImageDownloader`도 RestClient(`retrieve().toEntity(byte[].class)`)를 쓴다.

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **크롤링 대상은 남의 서비스다** — `base-url`·응답 형태가 예고 없이 바뀔 수 있고, 그 실패는 빌드가 아니라 배치 실행에서 드러난다. 배치 잡은 실패를 잡아 로그로 남기고 다음 주기에 재실행하는 잡 단위 격리가 정상 설계다.
- **application 쪽 패키지명에는 `crawling`이 남아 있다**(`com.tastyhouse.application.crawling.bbq`). 이번 분리는 infrastructure 모듈만 대상이었으며, application 패키지 평탄화는 후속 판단 항목이다.
- **`RemoteImageDownloader`는 URI를 문자열이 아니라 `URI.create(imageUrl)`로 넘긴다** — `.uri(String)` 오버로드는 URI 템플릿 확장 과정에서 `%`를 재인코딩하므로, 원격 이미지 URL이 이미 퍼센트 인코딩(`%20`·한글 인코딩 등)돼 있으면 이중 인코딩되어 404가 났다. `URI.create(...)`로 넘기면 그 문자열을 있는 그대로 쓴다.
- **`RemoteImageDownloader`는 응답 크기 상한(10MB, `FileUploadService` 업로드 한도와 동일)을 둔다** — `exchange()`로 응답을 받아 초과하면 `BusinessException(ErrorCode.FILE_SIZE_EXCEEDED)`를 던진다. WebClient 시절의 `maxInMemorySize`(2MB) 같은 버퍼 상한이 RestClient에는 없어 생긴 공백을 메운 것이다. 그 밖의 계약은 무변경 — 비정상 상태는 `FILE_EMPTY`, 그 밖의 IO 실패는 `RuntimeException`으로 던진다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.external.bbq` 봉인

**대상**: `backend/infrastructure/bbq/src/main/java/com/tastyhouse/external/bbq/`

`com.tastyhouse.infrastructure..` 아래로 옮기지 않는다. `PersistenceModuleAutoConfiguration`이 `com.tastyhouse.infrastructure`를 통째로 스캔하므로, 그 아래로 옮기면 빈 스캔 범위가 어긋나 **admin/ceo/batch 부팅이 깨진다.** 외부 연동 모듈이 `com.tastyhouse.external..`을 유지하는 것은 취향이 아니라 스캔 범위 제약이다. 옛 위치 `external.crawling`으로도 되돌리지 않는다.

### 진입 설정은 자기 패키지만 스캔한다

**대상**: `backend/infrastructure/bbq/src/main/java/com/tastyhouse/external/bbq/BbqModuleAutoConfiguration.java`

스캔 범위를 `com.tastyhouse.external` 루트 등으로 넓히면 같은 클래스패스의 형제 모듈(admdongkor 등) 빈까지 이 설정이 등록하게 되므로 넓히지 않는다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### `BbqApiClient`는 포트 계약에 올리지 않는다

**대상**: `backend/infrastructure/bbq/src/main/java/com/tastyhouse/external/bbq/BbqMenuAdapter.java`
→ 클래스 전체

`BbqMenuAdapter`는 `BbqMenuPort`의 구현으로, BBQ wire DTO를 application 계약 타입으로 변환한다. 변환 로직은 이전에 `BbqService`가 갖고 있던 `convertToProduct*` 메서드를 그대로 옮긴 것이며, **값 매핑(널 `Boolean` → primitive 기본값 등)은 동작을 바꾸지 않도록 원본과 동일하다.**

`BbqApiClient`는 이 어댑터의 **내부 협력자로 남는다** — BBQ wire DTO가 시그니처에 드러나므로 포트 계약에 올릴 수 없다(과거에는 `WebClient`·`Mono` 같은 반응형 타입이 그 이유였으나, RestClient 전환으로 동기 메서드가 되면서 지금 남은 이유는 wire DTO 노출이다). 크롤링 응답 형태를 바꾸는 작업에서 이 클라이언트를 포트로 승격하고 싶어지면, wire DTO가 application 계층으로 새어 나간다는 점을 먼저 본다.

### `RemoteImageDownloader`의 패키지 이력

**대상**: `backend/infrastructure/bbq/src/main/java/com/tastyhouse/external/bbq/RemoteImageDownloader.java`

`external.file.RemoteImageDownloader`(external 7모듈 분리 전) → `external.crawling.RemoteImageDownloader`(crawling 모듈) → `external.bbq.RemoteImageDownloader`(이 모듈). 첫 이동은 당시 코어가 `external.file`을 스캔해 admin/ceo에도 이 빈이 동반 스캔됐기 때문이고, 두 번째 이동은 소유 모듈의 패키지에 두는 규칙을 따른 것이다. `external.file`로 되돌리지 않는다.
