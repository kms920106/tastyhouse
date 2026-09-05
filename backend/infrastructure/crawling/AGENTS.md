<!-- Parent: ../../AGENTS.md -->

# infrastructure:crawling

배치 전용 외부 수집 어댑터 모듈(`java-library`) — BBQ 메뉴 크롤링 · 행정동 경계 GeoJSON 수집 · 원격 이미지 다운로드. `infrastructure:external` 7모듈 분리(챕터 01) 산물이다.

## 무엇을 소유하는가

```
com.tastyhouse.external/
├── crawling/
│   ├── CrawlingModuleConfig.java       진입점
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

**batch-module 하나뿐이다.** 세 기능 전부 배치 작업에서만 쓰므로 web-api·admin-api·ceo-api는 이 모듈을 의존하지도 `@Import` 하지도 않는다.

## `RemoteImageDownloader`의 패키지가 바뀌었다

`com.tastyhouse.external.file.RemoteImageDownloader` → **`com.tastyhouse.external.crawling.RemoteImageDownloader`**. 코어 `ExternalModuleConfig`가 `com.tastyhouse.external.file`을 스캔하므로, 그 자리에 남겨두면 파일 저장만 쓰는 admin/ceo에도 이 빈이 동반 스캔된다(`../external/AGENTS.md`의 패키지 예외 3건).

**⚠️ 이 클래스는 persistence가 등록하는 빈에 런타임 의존한다.** 생성자로 `com.tastyhouse.domain.file.service.FileUploadService`를 요구하는데, 그것은 순수 POJO 도메인 서비스라 **`infrastructure:persistence`의 `FileDomainConfig`가 `@Bean`으로 등록**한다. 즉 이 모듈만 import 하고 `InfrastructureModuleConfig`를 빼면 빈 부재로 기동에 실패한다(batch-module은 둘 다 import 하므로 성립한다). 컴파일 의존은 `domain-module`이고 빈 제공자는 persistence라, **컴파일이 통과해도 배선이 보장되지 않는 지점**이다.

## `region/` — 행정동 경계 수집

원천은 통계청 SGIS 행정동 경계를 행정구역 변경 이력에 맞춰 보정하고 WGS84 GeoJSON으로 정리한 공개 데이터셋(CC BY 4.0, 출처 표시 시 상업적 이용 허용)이다. SGIS 원본은 SHP + EPSG:5179라 좌표계 변환이 필요한데 이 원천은 이미 **EPSG:4326(WGS84)**이라 그대로 쓸 수 있다. batch-module의 행정동 마스터 동기화가 소비한다.

- **`WebClient`가 아니라 `java.net.http.HttpClient`를 쓴다**: 응답이 **30MB대 단일 JSON**이라 `bodyToMono(String.class)`로 받으면 문자열 하나로 힙에 통째 올라간다. `InputStream`으로 받아 Jackson **스트리밍 파서**로 feature 하나씩 소비하면 전체 문서를 메모리에 올리지 않는다.
- **`BoundedInputStream`으로 상한을 건다**: 원천이 예상 밖으로 커졌을 때 힙을 지키는 안전장치이며, 기본 128MB(`region.admin-dong.boundary.max-bytes`)다.
- **원천 URL은 버전 디렉터리 단위로 배포되어 "최신"을 가리키는 고정 URL이 없다** — 행정구역 개편이 반영된 새 버전이 나오면 yml의 `source-url` 안 `ver` 날짜를 올린다.
- **`sidoName` 정규화**: 원천은 `"서울특별시"` 같은 정식 명칭을 쓰지만 이 저장소의 주소 데이터는 `"서울 강남구 …"`처럼 짧은 형태다. 행정동 매칭이 주소 문자열 토큰과 `sido_name`을 직접 비교하므로(회원 배달주소의 행정동 채우기) 저장 시점에 짧은 형태로 맞춘다.
- 도메인 포트가 없어 자체 아웃바운드 계약(`AdminDongBoundaryPort`·`AdminDongBoundarySource`, `com.tastyhouse.application.region.port.out`)을 구현한다.

## 진입 설정과 스캔 범위

`CrawlingModuleConfig`(`@Configuration(proxyBeanMethods = false)`)가 아래를 갖는다.

- `@ComponentScan({"com.tastyhouse.external.crawling", "com.tastyhouse.external.region"})`
- `@EnableConfigurationProperties({BbqProperties.class, AdminDongBoundaryProperties.class})`

## yml — `application-crawling.yml`

**batch-module만** `spring.config.import`로 로딩한다. `crawling.bbq.api.base-url`과 `region.admin-dong.boundary.*`(source-url·timeout-seconds·max-bytes)를 담으며, 위 절의 판단 근거가 주석으로 함께 남아 있다. 경계 수집 값 3개는 전부 환경변수로 덮어쓸 수 있다(`ADMIN_DONG_BOUNDARY_*`).

## 테스트

`src/test/.../external/bbq/BbqApiClientTest`가 이 모듈에 함께 왔고, **`@Disabled("실네트워크(bbq.co.kr) 호출 — 빌드 게이트에서 제외")`가 붙어 있다.** 실제 외부 호스트를 호출하는 테스트라 `./gradlew build`가 외부 서비스 가용성에 묶이면 안 되기 때문이다. 이 테스트는 응답 형태를 사람이 확인할 때 수동으로 활성화해 돌리는 용도이며, **크롤링 로직의 회귀 방어 수단이 아니다.**

## Dependencies

### Internal
- `infrastructure:external` (implementation) — `WebClient.Builder`, `ExternalApiException`/`ExternalApiErrorCode`
- `application` (implementation) — 구현하는 아웃바운드 계약(`com.tastyhouse.application.crawling.bbq.port.out.RemoteImagePort`·`BbqMenuPort`, `com.tastyhouse.application.region.port.out.AdminDongBoundaryPort`)의 소유 모듈. adapter → port 방향이며 반대 방향 선언이 없어 순환이 아니다
- `domain-module` (implementation) — `FileUploadService`·`FileUploadCommand`·`UploadedFileId`, `shared/geo`의 `GeoPoint`·`GeoRing`·`InteriorPoint`, 예외 계약
- **런타임 의존(빌드 그래프에 없음)**: `infrastructure:persistence`의 `FileDomainConfig`가 등록하는 `FileUploadService` 빈

### External
- `spring-boot-starter-webflux` — BBQ API 호출(`WebClient`)과 Jackson(경계 스트리밍 파싱이 `ObjectMapper`를 쓴다). 경계 수집 자체는 JDK `HttpClient`를 쓰므로 WebClient가 아니다.

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **빈 배선**: batch-module만 `@Import(CrawlingModuleConfig.class)` 한다. `InfrastructureModuleConfig`를 함께 import 해야 `RemoteImageDownloader`가 뜬다(위 런타임 의존).
- **크롤링 대상은 남의 서비스다** — `base-url`·응답 형태가 예고 없이 바뀔 수 있고, 그 실패는 빌드가 아니라 배치 실행에서 드러난다. 배치 잡은 실패를 잡아 로그로 남기고 다음 주기에 재실행하는 잡 단위 격리가 정상 설계다.
