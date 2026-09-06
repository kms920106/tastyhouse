<!-- Parent: ../../AGENTS.md -->

# infrastructure:crawling

배치 전용 외부 수집 어댑터 모듈(`java-library`) — BBQ 메뉴 크롤링 · 행정동 경계 GeoJSON 수집 · 원격 이미지 다운로드. `infrastructure:external` 7모듈 분리(챕터 01) 산물이다.

## 무엇을 소유하는가

```
com.tastyhouse.external/
├── crawling/
│   ├── CrawlingModuleAutoConfiguration.java  진입점 — 챕터 02로 CrawlingModuleConfig에서 리네임 + @AutoConfiguration, 자기 등록
│   ├── RemoteImageDownloader.java      ← external.file 에서 패키지 변경
│   └── bbq/
│       ├── BbqApiClient.java           BBQ 메뉴 API 호출 (WebClient)
│       ├── BbqMenuAdapter.java         아웃바운드 포트 BbqMenuPort 구현
│       ├── BbqProperties.java          crawling.bbq.*
│       └── dto/  BbqMenuCategoryResponse · BbqMenuResponse · BbqMenuSubOptionResponse
└── region/
    ├── AdminDongBoundaryClient.java     아웃바운드 포트 AdminDongBoundaryPort 구현
    ├── AdminDongBoundaryProperties.java region.admin-dong.boundary.*
    └── BoundedInputStream.java          응답 크기 상한 스트림
```

`crawling.bbq`·`region` 패키지는 **불변**이고, `RemoteImageDownloader`만 패키지가 바뀌었다 — 아래 절 참조.

## 어느 앱이 의존하는가

**batch-module 하나뿐이다.** 세 기능 전부 배치 작업에서만 쓰므로 web-api·admin-api·ceo-api는 이 모듈을 의존하지 않는다(챕터 02 이후로는 의존 선언이 곧 활성화이므로 `@Import` 여부는 무관하다).

## `RemoteImageDownloader`의 패키지가 바뀌었다

`com.tastyhouse.external.file.RemoteImageDownloader` → **`com.tastyhouse.external.crawling.RemoteImageDownloader`**. 코어 `ExternalModuleAutoConfiguration`(구 `ExternalModuleConfig`)가 `com.tastyhouse.external.file`을 스캔하므로, 그 자리에 남겨두면 파일 저장만 쓰는 admin/ceo에도 이 빈이 동반 스캔된다(`../external/AGENTS.md`의 패키지 예외 3건).

**⚠️ 이 클래스는 persistence가 등록하는 빈에 런타임 의존한다.** 생성자로 `com.tastyhouse.domain.file.service.FileUploadService`를 요구하는데, 그것은 순수 POJO 도메인 서비스라 **`infrastructure:persistence`의 `FileDomainConfig`가 `@Bean`으로 등록**한다. 즉 이 모듈만 의존하고 `infrastructure:persistence`를 빼면 빈 부재로 기동에 실패한다(batch-module은 둘 다 의존하므로 성립한다 — 챕터 02 이후로는 `runtimeOnly` 의존 선언만으로 `PersistenceModuleAutoConfiguration`·`CrawlingModuleAutoConfiguration` 둘 다 자동 등록된다). 컴파일 의존은 `domain`이고 빈 제공자는 persistence라, **컴파일이 통과해도 배선이 보장되지 않는 지점**이다.

## `region/` — 행정동 경계 수집

원천은 통계청 SGIS 행정동 경계를 행정구역 변경 이력에 맞춰 보정하고 WGS84 GeoJSON으로 정리한 공개 데이터셋(CC BY 4.0, 출처 표시 시 상업적 이용 허용)이다. SGIS 원본은 SHP + EPSG:5179라 좌표계 변환이 필요한데 이 원천은 이미 **EPSG:4326(WGS84)**이라 그대로 쓸 수 있다. batch-module의 행정동 마스터 동기화가 소비한다.

- **`WebClient`가 아니라 `java.net.http.HttpClient`를 쓴다**: 응답이 **30MB대 단일 JSON**이라 `bodyToMono(String.class)`로 받으면 문자열 하나로 힙에 통째 올라간다. `InputStream`으로 받아 Jackson **스트리밍 파서**로 feature 하나씩 소비하면 전체 문서를 메모리에 올리지 않는다.
- **`BoundedInputStream`으로 상한을 건다**: 원천이 예상 밖으로 커졌을 때 힙을 지키는 안전장치이며, 기본 128MB(`region.admin-dong.boundary.max-bytes`)다.
- **원천 URL은 버전 디렉터리 단위로 배포되어 "최신"을 가리키는 고정 URL이 없다** — 행정구역 개편이 반영된 새 버전이 나오면 yml의 `source-url` 안 `ver` 날짜를 올린다.
- **`sidoName` 정규화**: 원천은 `"서울특별시"` 같은 정식 명칭을 쓰지만 이 저장소의 주소 데이터는 `"서울 강남구 …"`처럼 짧은 형태다. 행정동 매칭이 주소 문자열 토큰과 `sido_name`을 직접 비교하므로(회원 배달주소의 행정동 채우기) 저장 시점에 짧은 형태로 맞춘다.
- 도메인 포트가 없어 자체 아웃바운드 계약(`AdminDongBoundaryPort`·`AdminDongBoundarySource`, `com.tastyhouse.application.region.port.out`)을 구현한다.

## 진입 설정과 스캔 범위

`CrawlingModuleAutoConfiguration`(챕터 02 — `@AutoConfiguration(proxyBeanMethods = false)`, `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록)이 아래를 갖는다.

- `@ComponentScan({"com.tastyhouse.external.crawling", "com.tastyhouse.external.region"})`
- `@EnableConfigurationProperties({BbqProperties.class, AdminDongBoundaryProperties.class})`

## yml — `application-crawling.yml`

**batch-module만** `spring.config.import`로 로딩한다. `crawling.bbq.api.base-url`과 `region.admin-dong.boundary.*`(source-url·timeout-seconds·max-bytes)를 담는다. 경계 수집 값 3개는 전부 환경변수로 덮어쓸 수 있다(`ADMIN_DONG_BOUNDARY_*`).

**세 값의 판단 근거는 아래가 유일한 출처다**(과거에는 yml 주석에도 있었으나 중복을 없애려 이 문서로 일원화했다).

- **`source-url` — 원천은 행정동 경계 GeoJSON(통계청 SGIS 파생, CC BY 4.0 / EPSG:4326 WGS84)이다.** 이 원천은 **버전 디렉터리 단위로 배포되어 "최신"을 가리키는 고정 URL이 없다**(`.../ver20260701/HangJeongDong_ver20260701.geojson`처럼 경로와 파일명에 버전 날짜가 박힌다). 따라서 행정구역 개편이 반영된 새 버전이 나오면 **이 URL의 ver 날짜를 사람이 올려야 한다** — 자동으로 최신을 따라가지 않으므로, 경계 데이터가 오래됐다는 신고가 들어오면 먼저 이 값을 확인한다.
- **`timeout-seconds` 기본 180 — 30MB대 단일 파일이라 넉넉히 잡은 값이다.** 일반적인 API 호출 타임아웃 감각으로 줄이면 정상 수집이 중간에 끊긴다.
- **`max-bytes` 기본 134217728(128MB) — 원천이 예상 밖으로 커졌을 때 힙을 지키는 상한이다.** 경계 파싱은 스트리밍이지만 상한이 없으면 원천 비대화가 곧 OOM이 되므로, 이 값은 성능 튜닝 노브가 아니라 **안전장치**다. 수집이 이 상한에 걸려 실패하면 값을 올리기 전에 원천 크기가 왜 늘었는지부터 확인한다.

## 테스트

`src/test/.../external/bbq/BbqApiClientTest`가 이 모듈에 함께 왔고, **`@Disabled("실네트워크(bbq.co.kr) 호출 — 빌드 게이트에서 제외")`가 붙어 있다.** 실제 외부 호스트를 호출하는 테스트라 `./gradlew build`가 외부 서비스 가용성에 묶이면 안 되기 때문이다. 이 테스트는 응답 형태를 사람이 확인할 때 수동으로 활성화해 돌리는 용도이며, **크롤링 로직의 회귀 방어 수단이 아니다.**

## Dependencies

### Internal
- `infrastructure:external` (implementation) — `WebClient.Builder`, `ExternalApiException`/`ExternalApiErrorCode`
- `application` (implementation) — 구현하는 아웃바운드 계약(`com.tastyhouse.application.crawling.bbq.port.out.RemoteImagePort`·`BbqMenuPort`, `com.tastyhouse.application.region.port.out.AdminDongBoundaryPort`)의 소유 모듈. adapter → port 방향이며 반대 방향 선언이 없어 순환이 아니다
- `domain` (implementation) — `FileUploadService`·`FileUploadCommand`·`UploadedFileId`, `shared/geo`의 `GeoPoint`·`GeoRing`·`InteriorPoint`, 예외 계약
- **런타임 의존(빌드 그래프에 없음)**: `infrastructure:persistence`의 `FileDomainConfig`가 등록하는 `FileUploadService` 빈

### External
- `spring-boot-starter-webflux` — BBQ API 호출(`WebClient`)과 Jackson(경계 스트리밍 파싱이 `ObjectMapper`를 쓴다). 경계 수집 자체는 JDK `HttpClient`를 쓰므로 WebClient가 아니다.

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **빈 배선 (챕터 02 개정)**: batch-module만 `runtimeOnly project(':infrastructure:crawling')`를 선언한다(챕터 02 — `implementation`에서 강등). `CrawlingModuleAutoConfiguration`이 클래스패스 존재만으로 자동 등록되므로 `@Import`는 없다. `infrastructure:persistence`에도 의존해야 `RemoteImageDownloader`가 뜬다(위 런타임 의존 — batch-module은 이미 둘 다 의존한다).
- **크롤링 대상은 남의 서비스다** — `base-url`·응답 형태가 예고 없이 바뀔 수 있고, 그 실패는 빌드가 아니라 배치 실행에서 드러난다. 배치 잡은 실패를 잡아 로그로 남기고 다음 주기에 재실행하는 잡 단위 격리가 정상 설계다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### `@SuppressWarnings("NullableProblems")` — `org.jetbrains:annotations` 의존을 들이지 않는다

**대상**: `backend/infrastructure/crawling/src/main/java/com/tastyhouse/external/region/BoundedInputStream.java`
→ `read(byte[], int, int)`

이 프로젝트는 nullability 애노테이션을 쓰지 않으므로, JetBrains 외부 애노테이션이 상위 `FilterInputStream#read`의 buffer에 걸어 둔 `@NotNull`을 애노테이션 없이 덮게 된다. **그 경고 하나를 없애려고 `org.jetbrains:annotations` 의존을 추가하지 않는다** — 억제만 한다(`PhoneNumber` 선례). 이 `@SuppressWarnings`를 제거하면 경고가 되살아나므로 제거 대상이 아니다.

### `region/` 좌표 순서 — GeoJSON `[경도, 위도]` vs `GeoPoint(위도, 경도)`

**대상**: `backend/infrastructure/crawling/src/main/java/com/tastyhouse/external/region/AdminDongBoundaryClient.java`
→ `appendPolygonRings`

GeoJSON 좌표 배열은 `[경도, 위도]` 순서이고 `GeoPoint`는 `(위도, 경도)` 순서다. 이 메서드가 **인덱스를 뒤집어** 넣는다(`point.get(1)` → 위도, `point.get(0)` → 경도). 두 값 다 `BigDecimal`이라 **바꿔 넣어도 컴파일·실행이 성공하고 경계만 조용히 엉뚱한 곳에 놓인다.** 이 뒤집기를 "실수처럼 보인다"고 되돌리지 않는다.

### `com.tastyhouse.external..` 패키지 유지 (모듈 공통 봉인)

**대상**: `backend/infrastructure/crawling/src/main/java/com/tastyhouse/external/**`

이 모듈의 자바 패키지를 `com.tastyhouse.infrastructure..` 아래로 옮기지 않는다. `InfrastructureModuleConfig`가 `com.tastyhouse.infrastructure`를 통째로 스캔하므로, 그 아래로 옮기면 빈 스캔 범위가 어긋나 **admin/ceo/batch 부팅이 깨진다.** 외부 연동 모듈이 `com.tastyhouse.external..`을 유지하는 것은 취향이 아니라 스캔 범위 제약이다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### `BbqApiClient`는 포트 계약에 올리지 않는다

**대상**: `backend/infrastructure/crawling/src/main/java/com/tastyhouse/external/crawling/bbq/BbqMenuAdapter.java`
→ 클래스 전체

`BbqMenuAdapter`는 `BbqMenuPort`의 구현으로, BBQ wire DTO를 application 계약 타입으로 변환한다. 변환 로직은 이전에 `BbqService`가 갖고 있던 `convertToProduct*` 메서드를 그대로 옮긴 것이며, **값 매핑(널 `Boolean` → primitive 기본값 등)은 동작을 바꾸지 않도록 원본과 동일하다.**

`BbqApiClient`는 이 어댑터의 **내부 협력자로 남는다** — `WebClient`·`Mono` 같은 반응형 타입이 시그니처에 드러나므로 포트 계약에 올릴 수 없다. 크롤링 응답 형태를 바꾸는 작업에서 이 클라이언트를 포트로 승격하고 싶어지면, 반응형 타입이 application 계층으로 새어 나간다는 점을 먼저 본다.

### 경계 수집은 한 동의 실패로 전국 동기화를 실패시키지 않는다

**대상**: `backend/infrastructure/crawling/src/main/java/com/tastyhouse/external/region/AdminDongBoundaryClient.java`
→ `fetchAll` · `appendPolygonRings`

대표점을 만들지 못한 행(경계가 깨졌거나 링 정점이 부족한 경우)은 **건너뛰고 로그만 남긴다.** 한 동 때문에 전국 동기화를 실패시키는 것보다 그 동만 빠지는 편이 낫고, 빠진 동은 다음 동기화에서 원천이 고쳐지면 자연히 복구된다. 같은 이유로 **정점이 3개 미만인 퇴화 링은 면을 이루지 못하므로 버린다**(`GeoRing.of`의 `IllegalArgumentException`을 잡아 `debug` 로그만 남긴다).

### 여러 폴리곤을 링 목록 하나로 합친다

**대상**: `backend/infrastructure/crawling/src/main/java/com/tastyhouse/external/region/AdminDongBoundaryClient.java`
→ `toRings`(GeoJSON `MultiPolygon`/`Polygon` 평탄화)

여러 폴리곤(본토 + 부속 섬)을 **하나의 링 목록으로 합친다** — 이 저장 형식이 링 목록만 표현하기 때문이다. 대표점은 `GeoRing`/`InteriorPoint`가 **첫 링(가장 먼저 나오는 외곽)** 을 기준으로 잡으므로, 링 순서를 임의로 정렬하면 대표점이 부속 섬으로 옮겨갈 수 있다.

### `BoundedInputStream`은 `Content-Length`를 믿지 않는다

**대상**: `backend/infrastructure/crawling/src/main/java/com/tastyhouse/external/region/BoundedInputStream.java`
→ `countRead`

읽은 **누적 바이트**가 상한을 넘으면 실패하는 스트림이다. 원천이 예고 없이 커지거나 응답이 엉뚱한 내용으로 바뀌었을 때 배치가 힙을 모두 소진하며 죽는 것을 막는다. `Content-Length` 헤더를 믿지 않고 **실제로 읽은 양**을 세는 이유는, 헤더가 없거나(chunked) 실제와 다를 수 있기 때문이다.

### `sidoName`·`admDongName` 정규화 규칙

**대상**: `backend/infrastructure/crawling/src/main/java/com/tastyhouse/external/region/AdminDongBoundaryClient.java`
→ `shortSidoName` · `lastToken` · `SIDO_SUFFIXES`

- `shortSidoName`: `"서울특별시"` → `"서울"`. **접미어가 없으면 원래 값을 그대로 둔다**(예: `"제주"`). 위 §`region/` 절의 정규화 근거(주소 문자열 토큰 직접 비교)가 이 변환의 이유다.
- `lastToken`: `"서울특별시 종로구 사직동"` → `"사직동"`.
