<!-- Parent: ../../AGENTS.md -->

# infrastructure:admdongkor

배치 전용 행정동 경계 GeoJSON 수집 어댑터 모듈(`java-library`). 아웃바운드 계약 `AdminDongBoundaryPort`(`com.tastyhouse.application.region.port.out`)를 `AdminDongBoundaryClient`가 구현한다. 모듈명은 firebase·aws-s3처럼 **원천명**을 따른다 — 원천은 GitHub `vuski/admdongkor` 데이터셋이다.

## 이 모듈이 `crawling`에서 갈라진 이유 (2026-09-26)

과거에는 BBQ 메뉴 수집과 함께 `infrastructure:crawling` 한 모듈에 있었다. 두 수집은 구현 포트(`crawling.bbq.port.out` vs `region.port.out`)·yml 접두사(`crawling.bbq.*` vs `region.admin-dong.boundary.*`)·소비 유스케이스(`BbqService` vs `AdminDongSchedulerService`)를 **하나도 공유하지 않았다.** 또한 이 수집은 남의 사이트를 긁는 크롤링이 아니라 공개 데이터셋 다운로드라 `crawling`이라는 이름이 사실과 어긋났다. (당시에는 HTTP 클라이언트도 갈렸다 — BBQ는 `WebClient`, 이쪽은 JDK `HttpClient`. 이후 RestClient 전환으로 이 차이는 소멸했다 — 아래 §수집 방식 참고.)

**클래스패스·활성화 경로 이득은 없다** — 두 모듈 모두 batch-module만 의존하고 활성화 조건도 같다. aws 3분할과 달리 분리 근거는 응집도와 이름의 정확성이다. 부수 이득은 하나다 — 이 모듈은 `spring-boot-starter-json`만 선언한다(아래 §Dependencies, webflux는 애초에 리포 전체에서 제거됐다).

## 무엇을 소유하는가

```
com.tastyhouse.external.admdongkor/
├── AdmdongkorModuleAutoConfiguration.java  @AutoConfiguration + @ComponentScan(이 패키지) + @EnableConfigurationProperties(AdminDongBoundaryProperties)
├── AdminDongBoundaryClient.java            AdminDongBoundaryPort 구현
├── AdminDongBoundaryProperties.java        region.admin-dong.boundary.*
└── BoundedInputStream.java                 응답 크기 상한 스트림
```

패키지는 분리 때 `com.tastyhouse.external.region`에서 **`com.tastyhouse.external.admdongkor`로 바뀌었다**(모듈명과 맞춤 — `external.file.firebase → external.firebase` 선례). 클래스명·프로퍼티 접두사·환경변수 이름은 불변이다.

## 어느 앱이 의존하는가

**batch-module 하나뿐이다**(`runtimeOnly project(':infrastructure:admdongkor')`). 행정동 마스터 동기화(`AdminDongSchedulerService`)가 소비한다. 클래스패스 존재만으로 `AdmdongkorModuleAutoConfiguration`이 발화하므로 `@Import`는 없다.

## 수집 방식

원천은 통계청 SGIS 행정동 경계를 행정구역 변경 이력에 맞춰 보정하고 WGS84 GeoJSON으로 정리한 공개 데이터셋(CC BY 4.0, 출처 표시 시 상업적 이용 허용)이다. SGIS 원본은 SHP + EPSG:5179라 좌표계 변환이 필요한데 이 원천은 이미 **EPSG:4326(WGS84)**이라 그대로 쓸 수 있다.

- **`RestClient.exchange()`로 `InputStream`을 받아 스트리밍 파싱한다(`retrieve().body(String)` 금지)**: 응답이 **30MB대 단일 JSON**이라 `retrieve().body(String.class)`로 받으면 문자열 하나로 힙에 통째 올라간다. `exchange((req, res) -> ...)`로 `InputStream`을 받아 Jackson **스트리밍 파서**로 feature 하나씩 소비하면 전체 문서를 메모리에 올리지 않는다 — 이 스트리밍 요구가 WebClient 시절부터 RestClient 전환 후까지 이 클라이언트가 `retrieve().body(...)` 대신 `exchange()`를 쓰는 유일한 이유다(과거 근거였던 "WebClient가 응답을 통째로 힙에 올려서 JDK HttpClient를 쓴다"는 WebClient 고유 제약이었지, RestClient의 제약이 아니다 — RestClient도 스트리밍 없이 쓰면 같은 문제가 재현된다).
- **`BoundedInputStream`으로 상한을 건다**: 원천이 예상 밖으로 커졌을 때 힙을 지키는 안전장치이며, 기본 128MB(`region.admin-dong.boundary.max-bytes`)다.
- **원천 URL은 버전 디렉터리 단위로 배포되어 "최신"을 가리키는 고정 URL이 없다** — 행정구역 개편이 반영된 새 버전이 나오면 yml의 `source-url` 안 `ver` 날짜를 올린다.
- **`sidoName` 정규화**: 원천은 `"서울특별시"` 같은 정식 명칭을 쓰지만 이 저장소의 주소 데이터는 `"서울 강남구 …"`처럼 짧은 형태다. 행정동 매칭이 주소 문자열 토큰과 `sido_name`을 직접 비교하므로(회원 배달주소의 행정동 채우기) 저장 시점에 짧은 형태로 맞춘다.
- 도메인 포트가 없어 `application`이 소유한 아웃바운드 계약(`AdminDongBoundaryPort`·`AdminDongBoundarySource`)을 구현한다.
- 실패는 도메인 `BusinessException(ErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED)`로 던진다. 에러코드는 wire 계약이라 코드 문자열·HTTP 상태(502)를 그대로 유지한 채 도메인 `ErrorCode`(`backend/domain/src/main/java/com/tastyhouse/domain/exception/ErrorCode.java`)로 옮겨졌다(과거에는 코어 `infrastructure:http-client`의 `ExternalApiException(ExternalApiErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED)`였으나, 그 예외 계약 자체가 완전히 삭제됐다). **RestClient는 다운로드·파싱·용량초과의 `IOException`을 `ResourceAccessException`으로 감싸므로**, `AdminDongBoundaryClient`는 그 예외를 catch해 `ADMIN_DONG_BOUNDARY_FETCH_FAILED`로 변환한다(계약 불변 — 과거 WebClient/JDK HttpClient 시절과 응답 계약이 같다).

## yml — `application-admdongkor.yml`

**batch-module만** `spring.config.import`로 로딩한다(분리 전 파일명은 `application-crawling.yml`). `region.admin-dong.boundary.*`(source-url·timeout-seconds·max-bytes)를 담고, 세 값 전부 환경변수(`ADMIN_DONG_BOUNDARY_*`)로 덮어쓸 수 있다.

**세 값의 판단 근거는 아래가 유일한 출처다.**

- **`source-url` — 원천은 행정동 경계 GeoJSON(통계청 SGIS 파생, CC BY 4.0 / EPSG:4326 WGS84)이다.** 이 원천은 **버전 디렉터리 단위로 배포되어 "최신"을 가리키는 고정 URL이 없다**(`.../ver20260701/HangJeongDong_ver20260701.geojson`처럼 경로와 파일명에 버전 날짜가 박힌다). 따라서 행정구역 개편이 반영된 새 버전이 나오면 **이 URL의 ver 날짜를 사람이 올려야 한다** — 자동으로 최신을 따라가지 않으므로, 경계 데이터가 오래됐다는 신고가 들어오면 먼저 이 값을 확인한다.
- **`timeout-seconds` 기본 180 — 30MB대 단일 파일이라 넉넉히 잡은 값이다.** 일반적인 API 호출 타임아웃 감각으로 줄이면 정상 수집이 중간에 끊긴다. 코어의 전역 타임아웃(connect 5s / read 10s)과 다르므로 `AdminDongBoundaryClient`는 `HttpRequestFactories.withTimeouts(...)`로 connect·read 모두 이 값을 쓰는 per-client override를 적용한다(코어의 "전역값과 실제로 다를 때만 override" 규칙에 부합하는 사례).
- **`max-bytes` 기본 134217728(128MB) — 원천이 예상 밖으로 커졌을 때 힙을 지키는 상한이다.** 경계 파싱은 스트리밍이지만 상한이 없으면 원천 비대화가 곧 OOM이 되므로, 이 값은 성능 튜닝 노브가 아니라 **안전장치**다. 수집이 이 상한에 걸려 실패하면 값을 올리기 전에 원천 크기가 왜 늘었는지부터 확인한다.

**yml import 누락은 기동으로 드러나지 않는다** — `AdminDongBoundaryProperties`의 `@DefaultValue`가 같은 기본값을 갖고 있어 부팅은 성공한다. 다만 환경변수 오버라이드 경로가 사라지므로 batch `application.yml`의 import 줄을 지우지 않는다.

## Dependencies

### Internal
- `infrastructure:restclient` (implementation) — `RestClient.Builder` customizer만(예외·에러코드는 도메인 `ErrorCode` 소유)
- `application` (implementation) — 구현하는 아웃바운드 계약 `AdminDongBoundaryPort`·`AdminDongBoundarySource`의 소유 모듈. adapter → port 방향이며 순환이 아니다
- `domain` (implementation) — `shared/geo`의 `GeoPoint`·`GeoRing`·`InteriorPoint`

### External
- `spring-boot-starter-json` — Jackson(`ObjectMapper`·스트리밍 `JsonParser`). **webflux를 선언하지 않는다** — 코어 `infrastructure:restclient`가 `api`로 노출하는 `spring-web`을 전이로 받아 `RestClient.Builder`를 조립한다. `AdminDongBoundaryClient`는 응답을 스트리밍 파싱해야 하므로 `exchange()`로 `InputStream`을 받는다(위 §수집 방식). `ObjectMapper` 빈은 Boot `JacksonAutoConfiguration`이 등록한다.

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **원천은 남의 저장소다** — URL·응답 형태가 예고 없이 바뀔 수 있고, 그 실패는 빌드가 아니라 배치 실행에서 드러난다. 배치 잡은 실패를 잡아 로그로 남기고 다음 주기에 재실행하는 잡 단위 격리가 정상 설계다.
- **어댑터 테스트가 없다.** 부패 방지 수단은 컴파일과 batch 기동뿐이다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### `@SuppressWarnings("NullableProblems")` — `org.jetbrains:annotations` 의존을 들이지 않는다

**대상**: `backend/infrastructure/admdongkor/src/main/java/com/tastyhouse/external/admdongkor/BoundedInputStream.java`
→ `read(byte[], int, int)`

이 프로젝트는 nullability 애노테이션을 쓰지 않으므로, JetBrains 외부 애노테이션이 상위 `FilterInputStream#read`의 buffer에 걸어 둔 `@NotNull`을 애노테이션 없이 덮게 된다. **그 경고 하나를 없애려고 `org.jetbrains:annotations` 의존을 추가하지 않는다** — 억제만 한다(`PhoneNumber` 선례). 이 `@SuppressWarnings`를 제거하면 경고가 되살아나므로 제거 대상이 아니다.

### 좌표 순서 — GeoJSON `[경도, 위도]` vs `GeoPoint(위도, 경도)`

**대상**: `backend/infrastructure/admdongkor/src/main/java/com/tastyhouse/external/admdongkor/AdminDongBoundaryClient.java`
→ `appendPolygonRings`

GeoJSON 좌표 배열은 `[경도, 위도]` 순서이고 `GeoPoint`는 `(위도, 경도)` 순서다. 이 메서드가 **인덱스를 뒤집어** 넣는다(`point.get(1)` → 위도, `point.get(0)` → 경도). 두 값 다 `BigDecimal`이라 **바꿔 넣어도 컴파일·실행이 성공하고 경계만 조용히 엉뚱한 곳에 놓인다.** 이 뒤집기를 "실수처럼 보인다"고 되돌리지 않는다.

### 자바 패키지 `com.tastyhouse.external.admdongkor` 봉인

**대상**: `backend/infrastructure/admdongkor/src/main/java/com/tastyhouse/external/admdongkor/`

`com.tastyhouse.infrastructure..` 아래로 옮기지 않는다. `PersistenceModuleAutoConfiguration`이 `com.tastyhouse.infrastructure`를 통째로 스캔하므로, 그 아래로 옮기면 빈 스캔 범위가 어긋나 **admin/ceo/batch 부팅이 깨진다.** 옛 위치 `external.region`으로도 되돌리지 않는다 — 외부 연동 모듈마다 모듈명과 같은 하위 패키지를 소유한다.

### 진입 설정은 자기 패키지만 스캔한다

**대상**: `backend/infrastructure/admdongkor/src/main/java/com/tastyhouse/external/admdongkor/AdmdongkorModuleAutoConfiguration.java`

분리 전 `CrawlingModuleAutoConfiguration`은 `external.crawling`과 `external.region` 두 패키지를 함께 스캔했다. 스캔 범위를 `com.tastyhouse.external` 루트 등으로 넓히면 같은 클래스패스의 형제 모듈 빈까지 이 설정이 등록하게 되므로 넓히지 않는다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 경계 수집은 한 동의 실패로 전국 동기화를 실패시키지 않는다

**대상**: `backend/infrastructure/admdongkor/src/main/java/com/tastyhouse/external/admdongkor/AdminDongBoundaryClient.java`
→ `fetchAll` · `appendPolygonRings`

대표점을 만들지 못한 행(경계가 깨졌거나 링 정점이 부족한 경우)은 **건너뛰고 로그만 남긴다.** 한 동 때문에 전국 동기화를 실패시키는 것보다 그 동만 빠지는 편이 낫고, 빠진 동은 다음 동기화에서 원천이 고쳐지면 자연히 복구된다. 같은 이유로 **정점이 3개 미만인 퇴화 링은 면을 이루지 못하므로 버린다**(`GeoRing.of`의 `IllegalArgumentException`을 잡아 `debug` 로그만 남긴다).

### 여러 폴리곤을 링 목록 하나로 합친다

**대상**: `backend/infrastructure/admdongkor/src/main/java/com/tastyhouse/external/admdongkor/AdminDongBoundaryClient.java`
→ `toRings`(GeoJSON `MultiPolygon`/`Polygon` 평탄화)

여러 폴리곤(본토 + 부속 섬)을 **하나의 링 목록으로 합친다** — 이 저장 형식이 링 목록만 표현하기 때문이다. 대표점은 `GeoRing`/`InteriorPoint`가 **첫 링(가장 먼저 나오는 외곽)** 을 기준으로 잡으므로, 링 순서를 임의로 정렬하면 대표점이 부속 섬으로 옮겨갈 수 있다.

### `BoundedInputStream`은 `Content-Length`를 믿지 않는다

**대상**: `backend/infrastructure/admdongkor/src/main/java/com/tastyhouse/external/admdongkor/BoundedInputStream.java`
→ `countRead`

읽은 **누적 바이트**가 상한을 넘으면 실패하는 스트림이다. 원천이 예고 없이 커지거나 응답이 엉뚱한 내용으로 바뀌었을 때 배치가 힙을 모두 소진하며 죽는 것을 막는다. `Content-Length` 헤더를 믿지 않고 **실제로 읽은 양**을 세는 이유는, 헤더가 없거나(chunked) 실제와 다를 수 있기 때문이다.

### `sidoName`·`admDongName` 정규화 규칙

**대상**: `backend/infrastructure/admdongkor/src/main/java/com/tastyhouse/external/admdongkor/AdminDongBoundaryClient.java`
→ `shortSidoName` · `lastToken` · `SIDO_SUFFIXES`

- `shortSidoName`: `"서울특별시"` → `"서울"`. **접미어가 없으면 원래 값을 그대로 둔다**(예: `"제주"`). 위 §수집 방식의 정규화 근거(주소 문자열 토큰 직접 비교)가 이 변환의 이유다.
- `lastToken`: `"서울특별시 종로구 사직동"` → `"사직동"`.
