<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-07-25 | Updated: 2026-07-31 -->

# ceo-api

## Purpose
점주(매장 오너)용 REST API 애플리케이션 (실행 가능한 Spring Boot bootJar). `web-api`(일반 회원)·`admin-api`(관리자)와 대칭인 3번째 프레젠테이션 모듈로, 매장 사장님이 자기 매장·주문·예약·리뷰 등을 관리하는 셀프 서비스 API를 제공한다.

**챕터 04로 application 계층이 `application` 모듈로 물리 분리되어, 이 모듈은 인바운드 어댑터(컨트롤러 + `request/`)와 config·security 정책·부트스트랩만 담당한다** (과거 "점주 유스케이스의 application 계층은 이 모듈이 직접 소유한다"의 번복 — 유스케이스가 점주 전용이라는 사실은 그대로이고 담는 모듈만 바뀌었다). 컨트롤러는 `com.tastyhouse.application.<ctx>.port.in`의 UseCase 인터페이스만 주입한다.

> **챕터 03 — `application`의 자바 패키지가 평탄화됐다.** 과거 `com.tastyhouse.ceoapplication`이던 것이 `com.tastyhouse.application` 하나로 4개 앱과 합쳐졌다. 패키지만으로는 이 모듈이 주입하는 UseCase가 ceo 것인지 알 수 없으므로, 앱 소속은 마커 애노테이션(`@CeoApp`)이 표현한다 — 마커·유도 규칙 상세는 `application/AGENTS.md` 참고.

**현재 상태: 로그인 + 점주 가게 관리 API 구현 완료** — 모듈 골격 + JWT 인증 인프라 + 공통(common/exception) 요소, `auth`(로그인/토큰갱신/로그아웃)에 더해, 배민 사장님 셀프서비스 가이드 기반 **점주 가게 설정 API(`shop`)** 를 구현했다: 내 가게 조회, 영업시간·휴게시간(PDF 규격 검증), 휴무일(공휴일/정기/임시), 전화번호(다건+대표번호), 가게 상태(노출정지), 가게소개(금칙어 검수), 편의정보·찾아오는길·노출위치, 상표·대표이미지 변경요청(승인 워크플로), 콘텐츠보드, 영업 임시중지, 위생정보 조회. order 등 나머지 도메인 엔드포인트는 아직 없다.

- **점주-가게 소유권**: `Shop`에 `ceoId` 컬럼을 두어 1점주 N가게를 표현한다(관리자가 admin-api에서 배정). 모든 가게 관리 엔드포인트는 `application`의 `shop/ShopOwnershipValidator.validateOwnership(ceoId, shopId)`를 서비스 진입부에서 먼저 호출해 `shop.ceoId == 로그인 ceoId`를 확인하고, 불일치 시 `BusinessException(ErrorCode.SHOP_ACCESS_DENIED)`(403)을 던진다. `CustomUserDetails`는 `ceoId`만 노출하므로 shopId는 경로/바디로 받아 이 검증기로 소유권을 확인한다.
- **검수/승인**: 상표·대표이미지는 `domain`의 `shared/model/ApprovalStatus`(PENDING/APPROVED/REJECTED)를 쓰는 공용 `ShopImageChangeRequest` 애그리거트로 "점주 변경요청 → admin 승인/반려 → 승인 시 Shop 반영" 워크플로를 구현한다. 가게소개·찾아오는길은 `ProhibitedWordValidator`(금칙어) 통과 시 즉시 반영, 콘텐츠보드는 즉시 노출 + admin 사후 숨김/삭제. 노출정지는 PENDING 승인요청 존재 시 차단(`SHOP_STATUS_CHANGE_BLOCKED_BY_PENDING_REQUEST`).
- **이미지 규격 검증**: `application`의 `shop/ShopImageSpecValidator`가 상표(JPG·≤900KB·560×560↑·1:1)/콘텐츠(IMAGE JPG·PNG ≤10MB 700×700↑, GIF ≤10MB 250×250↑) 규격을 업로드 전 검증하고, 통과분만 `FileService`로 업로드한다. 유튜브 영상 길이(5~30분)는 서버 검증 불가라 URL 형식만 검증한다.

**도메인당 CQRS 분리 (서비스는 `application` 소유)**: 컨트롤러가 `domain`에 직접 결합되는 것을 막기 위해, admin과 동일하게 관심사별 CQRS 서비스 쌍을 둔다 — `{관심사}CommandService`(`@Transactional`, domain write 포트·도메인 서비스만 주입)와 `{관심사}QueryService`(`@Transactional(readOnly = true)`, `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스만 주입 + Response 조립 private 매퍼). `shop`은 점주 설정 관심사가 많아 서비스를 관심사 단위로 쪼갠다(`ShopBusinessHour*`/`ShopClosedDay*`/`ShopPhoneNumber*`/`ShopStatus*`/`ShopIntroduction*`/`ShopConvenienceInfo*`/`ShopTrademark*`/`ShopContentBoard*`/`ShopSuspension*`/`ShopHygieneBadgeQueryService`/`ShopDeliveryTip*`/`ShopDeliveryArea*`). **컨트롤러는 서비스 구현이 아니라 그 짝인 UseCase 인터페이스(`..port.in..`)를 주입한다** — 구체 서비스 클래스는 이 모듈의 컴파일 클래스패스에 보이지 않는다(`webAdaptersShouldNotDependOnApplicationServices`). **이 모듈 전체가 domain-free다** — 컨트롤러뿐 아니라 `config..`·`security..` 등 어디서도 `com.tastyhouse.domain.*`를 import하지 않는다(`apiModuleShouldBeDomainModelFree`, 공용 에러 계약 `domain.exception..`만 예외). 도메인 enum도 부트스트랩 시더까지 문자열로 넘긴다.

**배달팁 관심사는 예외적으로 컨트롤러 하나가 파트 5종을 소유한다**: `ShopDeliveryTipApiController`가 구간별·거리별·지역별·시간별·공휴일 8개 엔드포인트를 함께 갖는다. 관심사별로 쪼개는 관례를 따르지 않은 이유는 **거리별↔지역별 상호 배타가 두 리소스에 걸친 불변식**이라, 컨트롤러를 나누면 그 검증이 두 곳으로 흩어지기 때문이다. 또한 각 파트는 개별 행 CRUD가 아니라 **replace-all `PUT`**으로 교체하는데, 구간의 "3개 이하 + 금액 오름차순 + 팁 내림차순"이 집합 전체를 봐야 판정되는 규칙이어서 행 단위로 열면 중간 상태가 반드시 규칙을 위반하기 때문이다(상세 근거와 판정 기준은 `backend/CLAUDE.md`의 "집합 불변식 설정 컬렉션은 replace-all PUT으로 교체하는 규칙" 참고). 반면 `ShopDeliveryAreaApiController`는 행 하나가 스스로 유효하므로 기존 관례대로 행 단위 CRUD다.

**QueryDSL도 infrastructure도 절대 쓰지 않는다 (개정)** — `src/main`에 `com.querydsl.*` import·`@QueryProjection` 선언·`com.tastyhouse.infrastructure..` import가 **전면 0건**이며 `architecture/LayerRulesTest`(ArchUnit)가 이를 차단한다(챕터 04의 임시 장치 `shouldNotDependOnInfrastructureQuery`는 챕터 05에서 제거됐다).

**부트스트랩에 `scanBasePackages`가 없다 (챕터 02)**: `CeoApiApplication`은 `@SpringBootApplication` + `@Import(CeoApplicationConfig.class)` + `@EnableConfigurationProperties(CeoSeedProperties.class)` 셋만 갖는다 — 과거의 `scanBasePackages`/`@ComponentScan basePackages` 나열(`com.tastyhouse.ceoapi`·`infrastructure`·`external`·`security`·`logging`)도, 그 `excludeFilters`도 **전부 사라졌다**. 라이브러리 모듈이 각자의 `{Xxx}ModuleAutoConfiguration`으로 자기 자신을 등록하고, 이 앱에 실리지 않는 모듈은 애초에 클래스패스에 없어(예: `infrastructure:oauth`는 web 전용) 제외 필터가 필요 없기 때문이다. `application`은 여전히 `CeoApplicationConfig`를 `@Import`해 배선하며, `domain`은 `@Component`/`@Service`/`@Configuration`이 0건이라(도메인 서비스는 POJO, 빈 등록은 infra `<ctx>/config/<Ctx>DomainConfig`) 스캔 대상이 아니다. 조립의 상한은 루트 [CLAUDE.md 컴포지션 루트 규칙](../CLAUDE.md#컴포지션-루트-규칙-조립은-실행-앱-모듈의-것--챕터-03) 참고.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | web + springdoc 의존, **`application`**·`security-module`·`api-common-module`을 `implementation`으로 참조하고, `infrastructure:persistence`·`infrastructure:file-storage`·`infrastructure:redis`·`logging-module`은 **챕터 02로 `runtimeOnly`**로 내려갔다 (admin-api와 동일 구성). 외부 연동은 **파일 저장 스타터 `infrastructure:file-storage` 하나뿐**이며(챕터 03 — 이전에는 코어 `external`과 구현 `firebase` 2줄이었다), OAuth·결제·메일/SMS 어댑터는 web-api 전용이라 이 모듈에 오지 않는다. QueryDSL 의존은 없다 |
| `src/main/resources/application.yml` | 점주 앱 환경 설정 (포트 `8100`, CORS 기본 `http://localhost:3020`, `jwt.secret=${JWT_SECRET_CEO}`) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/ceoapi/` | 점주 컨트롤러 루트 — `config/`(JWT·Security·`CeoSeeder`/`CeoSeedProperties`), `auth/`(로그인·토큰갱신·로그아웃), `ceo/`(점주 계정 CQRS 서비스), `file/`, `shop/`(점주 가게 설정 — 관심사별 CQRS 서비스 + `ShopOwnershipValidator`·`ShopImageSpecValidator`). **공용 플럼빙(`ApiResponse`·`PageRequest`·`PaginationResponse`·`FileService`·`GlobalExceptionHandler`)은 `api-common-module`(`com.tastyhouse.apicommon`) 소유**이며 `CeoApiApplication`이 그 패키지를 스캔한다. **shop 응답 record는 전부 이 모듈이 소유한다** — `ShopBreakTimeResponse`·`ShopBusinessHourResponse`·`ShopHygieneBadgeResponse`가 admin과 필드 구성이 같은 것은 중복이 아니라 **우연히 일치한 앱별 응답 계약**이며(한쪽 화면 요구가 바뀌면 다른 쪽을 건드리지 않고 갈라져야 한다), `ShopDetailResponse`(필드 셋 차이)·`ShopAmenityResponse`/`ShopListItemResponse`(`@Schema` 문구 차이)는 애초에 계약이 다르다. 신규 도메인 폴더는 admin-api 컨벤션대로 생성 |
| `src/test/` | 점주 API 테스트 (`contextLoads`) |

## For AI Agents

### Working In This Directory
- **presentation(인바운드 어댑터) 레이어만 담당한다 (챕터 04 개정)**: 컨트롤러는 도메인을 직접 호출하지 않고 `application`의 UseCase 포트를 통해서만 호출한다.
  - **이 모듈에 `@Service` 빈을 두지 않는다**: `architecture/LayerRulesTest#apiModuleMustNotContainApplicationLayer`가 강제한다. `@RestController`는 `..adapter.in.web..`에만 둔다(짝 규칙 `restControllersShouldResideInWebAdapterPackage`).
  - **컨텍스트 패키지는 3층 구조다 (챕터 09 개정)**: `<ctx>/adapter/in/web/`(컨트롤러) + `.../request/`(Request record) + `.../response/`(Response record). **`response/`는 챕터 09로 `application`에서 이 모듈로 이동했다**(105개) — 유스케이스 계층에서 Swagger·HTTP 표현을 걷어내기 위함이며, admin이 챕터 06에서 한 것과 같은 설계다. `service/`·`port/in/`은 여전히 이 모듈에 없다(`application` 소유). **web-api도 챕터 10으로 같은 3층이 됐다 — 3개 앱 전부 완료다.**
- **매핑은 양방향 모두 이 모듈의 책임이다 (챕터 09)** — Request → Command(컨트롤러의 `request.toCommand(...)`)와 Result → Response(Response record의 `from(XxxResult)`) 둘 다 인바운드 어댑터가 한다. 페이징은 유스케이스가 `PageResult<XxxResult>`를 반환하고 컨트롤러가 `PaginationResponse.from(pageResult.map(XxxResponse::from))`으로 감싼다.
  - **표현 계약이 만들 수 없는 값은 application이 `*View`/`*ViewResult`로 넘긴다.** 승격 과정에서 실제로 걸린 유형은 넷이다 — (1) 도메인 <b>애그리거트·도메인 서비스</b>에서 나오는 값(`ShopStatusResult`·`ShopDetailViewResult`·`ProductPriceView`), (2) 여러 읽기 포트를 합친 결과(`ShopImageStatusResult`·`ShopClosedDaysResult`·`ShopDeliveryTipViewResult`), (3) 도메인 enum의 <b>비-accessor 호출</b>이 필요한 값(`ShopRequestType#isContractAmending` → `ShopRequestListItemViewResult`, `values()` 카탈로그 → `ShopChangeCategoryResult`·`ShopRequestTypeCatalogResult`·`ProductAllergenTypeView`), (4) `domain.shared.geo..`처럼 carve-out이 아닌 도메인 타입(`GeoPointView`).
  - **api 모듈에서 도메인 enum을 `switch`할 수 없다** — 바이트코드에서 `ordinal()`·`values()` 호출이 되어 `apiModuleShouldOnlyReadDomainEnums`(허용 accessor는 `name`·`getDescription`·`getDisplayName` 3종)에 걸린다. 리뷰 정렬 표시명(`ShopReviewSortTypeView`)이 이 이유로 application에 남았다.
  - **Command 경로의 반환 Result는 `port.out`에 두어도 된다 (챕터 03 개정)** — 과거(패키지 평탄화 전)에는 앱별 `ceoapplication.<ctx>.port.out`이 읽기 계약 패키지와 물리적으로 분리돼 있어 이 구분이 필요했으나, 챕터 03으로 4개 앱이 `com.tastyhouse.application.<ctx>.port.out` 한 패키지에 합쳐지면서 **`port.out`의 정의 자체가 "이 도메인의 모든 아웃바운드 계약"으로 넓어졌다** — 읽기 계약뿐 아니라 CommandService가 반환하는 Result/View record(`ShopDeliveryAreaBulkResult`·`ProductAvailabilityChangeView`)도 정당하게 여기 산다. `commandServicesShouldNotDependOnQueryDaos`가 그 record를 위반으로 잡지 않는 이유는 이 규칙이 챕터 03에서 패키지 술어(`resideInAPackage("..port.out..")`)가 아니라 **이름 기준**(`haveSimpleNameEndingWith("QueryPort")`/`"QueryService"`)으로 바뀌었기 때문이다.
- 새 기능 추가 시 admin과 동일한 도메인-폴더 + `request/`·`response/` 컨벤션(서비스·`port/in/`은 `application`에), `{도메인}CommandService`/`{도메인}QueryService` CQRS 중개 계층, DTO 조립·`@ModelAttribute` 조회·`@PathVariable id` 통일·`@Schema` 문서화 규칙을 그대로 따른다. 상세·근거·예시는 루트 CLAUDE.md 및 `admin-api/AGENTS.md` 참고.
- **import 순서 — presentation 내부 서브정렬**: 자사 import의 presentation 계층(`com.tastyhouse.ceoapi.*`) 안에서 공용 인프라(`common`·`config`)를 도메인 전용(`<도메인>.request`·`.response`)보다 위에 둔다. 상세는 루트 CLAUDE.md 참고.
- **불변식은 `domain`에 둔다** — 한 트랜잭션에서 2개 이상 애그리거트를 다루는 오케스트레이션과 무상태 정책·검증기(`ProhibitedWordValidator` 등)는 `<ctx>/service/` POJO로 내리고, `application`의 CommandService는 트랜잭션 경계·소유권 검증·VO 승격·명시적 `save` 호출·응답 조립만 담당한다. 도메인 모델은 POJO라 더티 체킹이 없으므로 변경 후 반드시 `repository.save(domain)`을 호출한다. `domain`의 write 포트·도메인 서비스와 다중 앱 공유 `{Ctx}QueryPort`(`domain` 소유)는 web-api/admin-api와 공유되므로, 그 시그니처를 바꿀 때는 소비 모듈 전체를 함께 확인한다.

### Testing Requirements
- `@SpringBootTest` 기반 컨텍스트 로드/컨트롤러 검증.
- **레이어 경계는 `src/test/.../architecture/LayerRulesTest`(ArchUnit)가 강제**한다 — **CQRS 서비스 대상 규칙 10종은 챕터 04에서 `application`으로 이동**했고, 이 모듈에는 인바운드 어댑터를 대상으로 하는 규칙만 남았다: `controllersShouldNotDependOnRepositories`·`controllersShouldNotDependOnQueryDaos`·`controllersShouldBeDomainFree`·`requestRecordsShouldBeDomainAndInfraFree`·`controllersShouldDependOnUseCasesOnly`·`webAdaptersShouldNotDependOnApplicationServices`·`shouldNotDependOnQuerydsl`·`shouldNotDependOnInfrastructurePersistence`, 그리고 분리로 신설된 `apiModuleMustNotContainApplicationLayer`·`restControllersShouldResideInWebAdapterPackage`. `allowEmptyShould(true)`를 쓰지 않아 대상 0건이면 실패로 드러난다. 챕터 04의 임시 장치였던 `shouldNotDependOnInfrastructureQuery`와 이중 패키지 매칭은 챕터 05에서 제거됐다.

### Common Patterns
- **JWT 인증 메커니즘은 `security-module`의 `com.tastyhouse.security.jwt`에 공유**된다. `application`의 `CeoJwtTokenProvider`(`@Component @CeoApp`)가 그 공용 provider를 상속해 `ceoId` 클레임·`CeoUserDetails` 재구성만 주입한다(검증 토큰 없음). **공용 필터 빈은 `SecurityModuleAutoConfiguration`이 `BlacklistRepository` 포트를 받아 등록**한다(챕터 02) — 이 앱에 조립 코드가 없다. 저장소 구현·접두사(`ceo:bl:`/`ceo:rt:`)는 챕터 01부터 `infrastructure:redis`가 프로퍼티로 소유한다. 정책은 ceo-api에 잔류: `config/security/SecurityConfig`·`PublicPaths`. (`config/jwt/` 디렉터리는 챕터 01~02로 소멸.)
- **점주 계정 도메인(`ceo`)은 `domain`의 `admin` 도메인과 동일한 최소 CRUD 패턴**이다(`Admin` 대비 `role` 없이 `status`만 보유). `Ceo`(순수 POJO)/`CeoStatus`/`CeoId`/`CeoRepository`는 `domain/src/main/java/com/tastyhouse/domain/ceo/`에, 영속 어댑터(`CeoJpaEntity`/`CeoMapper`/`CeoJpaRepository`/`CeoRepositoryImpl`)와 조회 DAO(`ceo/query/`)는 `infrastructure/persistence/.../ceo/`에, application 서비스(`CeoCommandService`/`CeoQueryService`)는 `application`의 `ceo/service/`에 있다(admin-api에도 관리자용 `ceo/CeoQueryService`가 따로 있다). DDL은 `schema.sql`의 `CEO` 테이블.
- **인가 체인은 `.anyRequest().hasRole("CEO")`로 강화되어 있다** — `CeoUserDetailsService`가 로그인 시 고정 `ROLE_CEO` 권한을 부여한다(점주는 단일 역할이라 역할 enum 없음).
- **최초 점주 계정은 부팅 시드로 주입**된다(`config/CeoSeeder`+`CeoSeedProperties`, admin-api `AdminSeeder` 패턴과 동일). `ceo.seed.password`가 기본 센티넬(`__UNSET__`)이면 fail-fast로 부팅을 거부하므로, 운영/최초 기동 시 `CEO_SEED_PASSWORD` 환경변수가 필수다.
- **`jwt.secret`은 web-api·admin-api와 반드시 달라야 한다**(ceo=`JWT_SECRET_CEO`). 동일 시크릿이면 다른 API의 토큰이 점주 인증을 통과하는 권한 상승이 발생한다 — 상세는 `security-module/AGENTS.md`.
- **Redis 키 접두사는 점주 전용으로 분리**: refresh `ceo:rt:`, blacklist `ceo:bl:` (web=`rt:`/`bl:`, admin=`admin:rt:`/`admin:bl:`와 겹치지 않음).
- **등록(POST) API는 생성된 `Long` id만 반환**한다: `ResponseEntity<ApiResponse<Long>>`로 PK 하나만 반환하고, 생성 응답 전용 래퍼 record를 만들거나 생성 직후 QueryService로 재조회해 상세 DTO를 반환하지 않는다. 벌크 등록은 `ApiResponse<List<Long>>`(reference: `ShopSuspensionApiController#createSuspension`). 검증 전용 POST(`ShopIntroductionApiController#validateIntroduction`)·인증/토큰 발급·토글/상태전이는 리소스 등록이 아니므로 적용 제외. 이 모듈은 shop 하위 등록 API 전부가 이미 이 형태이며 프로젝트 reference 구현이다. 상세는 루트 CLAUDE.md 참고.


## 설정 파일 (`src/main/resources/application.yml`)

서버 포트 `8100`, CORS 허용 오리진(`CORS_ALLOWED_ORIGINS`, 기본 `http://localhost:3020`), JWT 만료(access 1시간 / refresh 7일 / 로그인 상태 유지 30일), multipart 상한 10MB를 담고, 공유 모듈의 설정을 `spring.config.import`로 끌어온다.

- **Redis 연결 설정은 이 파일이 갖지 않는다** — 챕터 05 §5b에서 `infrastructure:redis` 모듈이 소유하게 됐고, 이 파일은 `classpath:application-redis.yml`을 import할 뿐이다. 그 설정만 담고 있던 `security-module`의 `application-security.yml`은 **파일째 이관되고 삭제됐다.** Redis 접속 정보를 바꿔야 하면 이 파일이 아니라 `infrastructure/redis/src/main/resources/application-redis.yml`을 본다.
- **`.env`를 `optional:file:.env[.properties]`와 `optional:file:backend/.env[.properties]` 두 경로로 선언한다.** `spring.config.import`의 `file:` 상대경로는 **JVM 작업 디렉터리(CWD) 기준으로 해석**되므로, 한 경로만 선언하면 실행 위치에 따라 `.env`가 조용히 로드되지 않는다. 두 줄을 함께 두어 **모노레포 루트에서 실행하는 경우와 `backend`에서 실행하는 경우를 모두 지원**한다. `optional:` 접두어라 없는 쪽은 건너뛴다. 그 밖의 디렉터리에서 `java -jar`를 실행하면 두 경로 모두 빗나가 DB 접속 정보 같은 필수 환경변수가 비므로, 실행 디렉터리 규칙은 루트 `CLAUDE.md`의 "실행 디렉터리(CWD) 주의"를 따른다.
- `jwt.secret`은 ``JWT_SECRET_CEO``를 읽는다(앱별로 반드시 달라야 하는 이유는 위 참고).
- **최초 점주 시드(`ceo.seed.*`)** — `ceo.seed.password`가 기본 센티넬 `__UNSET__`이면 **신규 시드를 거부**하므로, 운영·최초 기동 시 `CEO_SEED_PASSWORD` 환경변수가 필수다(위 `CeoSeeder` 항목 참고). `CEO_SEED_USERNAME`(기본 `ceo`)·`CEO_SEED_NAME`(기본 `점주`)은 선택이다.

## Dependencies

### Internal
- `application` (implementation) — 컨텍스트 UseCase 인바운드 포트(컨트롤러가 주입) + `CeoApplicationConfig`
- `infrastructure:persistence` (**챕터 02로 `runtimeOnly`로 강등 — 과거 서술의 번복**): 소스 import는 0건이고, **auto-configuration 전환으로 부트스트랩의 컴파일 타임 참조 자체가 사라졌다.** 과거에는 `@Import(InfrastructureModuleConfig.class)`가 진입점 설정 클래스를 컴파일 타임에 참조해 `runtimeOnly`로 내리면 4개 모듈 전부 "package does not exist"로 깨졌으나, `InfrastructureModuleConfig` → `PersistenceModuleAutoConfiguration`으로 리네임되며 `@AutoConfiguration` + `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록하는 형태가 되어 `@Import` 자체가 사라졌다. 은닉은 여전히 의존 스코프가 아니라 ArchUnit(`LayerRulesTest`)이 담당하지만, 이제는 컴파일 타임 은닉도 `runtimeOnly`가 실제로 보장한다
- `infrastructure:file-storage` — 파일 저장 스타터(챕터 03). `infrastructure:external`(코어 — `WebClientConfig`·`ExternalApiException`·파일 저장 SPI)과 `infrastructure:firebase`(`FileStorageStrategy` 구현 — 파일 업로드)를 묶어 전이로 공급하므로 **앱은 두 모듈을 직접 선언하지 않는다**. **OAuth·결제·메시징 모듈은 의존하지 않는다** — 점주 화면에는 소셜 로그인·PG 결제·메일/SMS 발송 유스케이스가 없다
- `logging-module`, `security-module`
- `infrastructure:redis` (**runtimeOnly**) — rate limit 카운터와 `StringRedisTemplate` 빈. `RedisModuleAutoConfiguration`이 자기 등록하며(챕터 02) 부트스트랩은 `@Import`하지 않는다
- `api-common-module` — `ApiResponse`·`PaginationResponse`·`PageRequest`·`FileService`·공용 `GlobalExceptionHandler`
- **`domain`은 선언하지 않는다** — 이 모듈 소스에 `com.tastyhouse.domain..` import가 0건이고(`apiModuleShouldBeDomainModelFree`가 강제), domain 타입이 다시 필요해져도 `api-common-module`이 `api project(':domain')`로 전이 노출하므로 재선언이 필요 없다. web/admin/ceo 3모듈이 모두 같은 상태다(web-api `GlobalExceptionHandler`가 쓰는 `domain.exception..`도 이 전이 경로로 해결된다).
- `testFixtures(project(':application'))` — `adaptersShouldOnlyUseOwnAppUseCases`가 Command record의 앱 소속 유도(`AppOwnership`)를 application 모듈과 공유한다. **복제하면 두 벌이 갈라지므로** test fixture로 받는다(챕터 03).

### External — starter를 직접 선언하지 않는다
공유 모듈이 `api`로 전이 노출하므로 이 모듈은 starter 좌표를 직접 쓰지 않는다.

| 전이되는 것 | 노출 모듈 |
|---|---|
| `starter-web` · `starter-validation` · springdoc | `api-common-module` |
| `starter-security` | `security-module` |
| `starter-aop` | `logging-module` |
| jjwt-api (impl·jackson은 runtimeOnly 전이) | `security-module` → `security-core` |

"직접 쓰는 것은 직접 선언"하는 Gradle 관례와는 상충하나, 위 노출은 **의도된 계약**이라 소비 측 중복 선언을 노이즈로 판단해 걷어냈다. 공유 모듈이 노출을 `implementation`으로 좁히면 여기서 **즉시 컴파일 에러**로 드러나므로 침묵 파손은 없다.

**~~예외 — `spring-boot-starter-data-redis`는 직접 선언한다~~ (챕터 01에서 소멸).** 이 앱은 더 이상 Redis 타입을 참조하지 않으므로 그 선언이 **삭제**됐고, `runtimeOnly project(':infrastructure:redis')`가 유일한 Redis 선언이다. 근거: 접두사를 생성자로 주입하려고 `StringRedisTemplate`을 직접 참조하던 `config/jwt/RedisRepositoryConfig`가 사라졌다.

**키 접두사 (불변 계약)**: `application.yml`의 `security.token-store.key-prefix: "ceo:"` → `ceo:rt:{username}`·`ceo:bl:{accessToken}`. **콜론을 빠뜨리면**(`ceo`) 키가 `ceort:`가 되어 **예외 없이** 기존 세션이 전부 무효화된다.

**이것은 어댑터 모듈을 `implementation`으로 되돌리는 것과 다르다.** 앱이 보는 것은 `StringRedisTemplate`이라는 **라이브러리 타입**뿐이고, `infrastructure:redis`의 어댑터 클래스(`RedisRateLimitCounter` 등)는 여전히 컴파일 타임에 보이지 않는다 — 헥사고날 은닉은 그대로다. 두 판단을 섞어 "전이가 끊겼으니 모듈을 다시 `implementation`으로" 되돌리지 않는다.

<!-- MANUAL: -->

## 봉인·가드 목록

<!-- 분류 A. ceo-api 고유분. 3앱 공통분은 backend/AGENTS.md "계층 규칙 봉인 — api 앱 3종 공통" 참조 -->

**대상**: `backend/ceo-api/src/test/java/com/tastyhouse/ceoapi/architecture/LayerRulesTest.java`

이 파일의 규칙 대부분은 web-api·다른 앱과 동일하며, 그 공통분은 [backend/AGENTS.md](../AGENTS.md)의 "계층 규칙 봉인 — api 앱 3종 공통"에 있다. **아래는 이 앱 고유의 차이다.**

### `seedersShouldDependOnUseCasesOnly` — 이 앱에만 있는 규칙

**대상**: `backend/ceo-api/src/test/java/com/tastyhouse/ceoapi/architecture/LayerRulesTest.java`
→ `seedersShouldDependOnUseCasesOnly()`

부트스트랩(`..config..`)도 UseCase 인터페이스만 주입한다. 구체 서비스 주입은 금지다.

`webAdaptersShouldNotDependOnApplicationServices`가 `..adapter.in.web..`로 대상을 좁히므로 **`config..`의 구체 서비스 주입은 무검사 사각지대였다.** 실제로 `UceoSeeder`가 인바운드 포트가 아니라 구체 클래스 `UceoQueryService`를 주입하고 있었고, 호출하던 연산(`existsByUsername`)은 이미 포트에 선언돼 있어 신규 코드 없이 교체됐다.

**시더가 없는 web-api와 `config..`가 없는 batch-module에는 대상 0건이라 두지 않는다(공허 통과 회피).**

### `shouldDependOnOauthSpiOnlyNotProviderPackages`는 이 모듈에 두지 않는다

web-api에 있는 이 규칙을 **이 모듈에 복제하지 않는다** — ceo에는 소셜 로그인이 없어 대상 0건으로 **공허하게 통과**하기 때문이다(전환 전 이 앱의 `LayerRulesTest`에도 없던 규칙이다).

### 이하 — 챕터 06에서 코드 주석으로부터 이관된 가드

<!-- 분류 A. 원문 주석은 챕터 06에서 제거됐으므로 이 절이 그 금지 지시의 유일한 소재지다 -->

### `AuthApiController.login` — `keyPrefix` 개명 봉인

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/auth/adapter/in/web/AuthApiController.java`
→ `login()` 의 `@RateLimit(... keyPrefix = "rate_limit:ceo_login")`

`keyPrefix`를 **개명하지 않는다**. Redis 카운터 키라서 바꾸면 배포 시점에 진행 중인 rate limit 카운터가 전부 리셋된다.

> **함께 보존할 사실(챕터 06 완료 게이트)**: `ceo-api`에도 rate limit이 **활성**이다. `ApiCommonConfig` 제외는 클래스 단위라 `RateLimitAspect`는 admin·ceo에도 적용된다 — "web만 rate limit"은 **오독**이며 보안 회귀 함정이다. 이 컨트롤러의 `@RateLimit` 사용이 그 반증이다.

### `CeoLoginHistorySearchRequest` — 날짜 필드에 Bean Validation 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/ceo/adapter/in/web/request/CeoLoginHistorySearchRequest.java`
→ record 컴포넌트 `startDate` / `endDate`

**날짜 필드에 Bean Validation을 걸지 않는다.** `@PastOrPresent`는 컨트롤러 진입 전에 걸려 `errorCode` 없는 범용 400을 내리므로, 같은 규칙 위반인데 입력값에 따라 응답 계약이 갈린다. "조회 가능 기간은 최근 90일"은 상한(미래 금지)과 하한(90일 초과 금지)이 **하나의 규칙**이므로 `CeoLoginHistoryQueryService`가 통째로 판정해 `CEO_LOGIN_HISTORY_DATE_OUT_OF_RANGE` 하나로 응답한다.

### `CeoShopAccessHistorySearchRequest` — 날짜 Bean Validation 금지 · `shopId` 소유권 검증 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/ceo/adapter/in/web/request/CeoShopAccessHistorySearchRequest.java`
→ record 컴포넌트 `shopId`, `startDate`/`endDate`

- `shopId`에 **소유권 검증을 걸지 않는다** — 토큰의 `ceoId`로 함께 필터하므로 남의 가게 id를 넣으면 빈 목록이 될 뿐이고, 그래서 가게 존재 여부가 새지 않는다.
- **날짜 필드에 Bean Validation을 걸지 않는다** — `CeoLoginHistorySearchRequest`와 같은 이유. 보관 기간·미래일자 판정은 전부 `CeoShopAccessHistoryQueryService`가 담당해 `CEO_SHOP_ACCESS_HISTORY_DATE_OUT_OF_RANGE` 하나로 응답한다.

### `ShopChangeHistorySearchRequest.changedDate` — Bean Validation 제약 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopChangeHistorySearchRequest.java`
→ record 컴포넌트 `changedDate`

`changedDate`에 Bean Validation 제약을 **두지 않는다**. "조회 가능 기간은 최근 6개월(과거~오늘)"은 상한(미래 금지)과 하한(6개월 초과 금지)이 **하나의 규칙**이므로 `ShopChangeHistoryQueryService`가 통째로 판정해 `SHOP_CHANGE_HISTORY_DATE_OUT_OF_RANGE` 하나로 응답한다. 과거에 상한만 `@PastOrPresent`로 잡았을 때 미래 날짜가 컨트롤러 진입 전에 걸려 `errorCode` 없는 범용 400이 내려갔고, 같은 규칙 위반인데 프론트가 받는 응답 계약이 상·하한에서 갈리는 사고가 실제로 있었다.

### `ShopRequestSearchRequest` — 날짜 두 필드 Bean Validation 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopRequestSearchRequest.java`
→ record 컴포넌트 `startDate`/`endDate`

날짜 두 필드에 Bean Validation 제약을 **두지 않는다**. 기간의 상·하한 관계(`startDate <= endDate`)가 하나의 규칙이라 서비스가 통째로 판정해 `SHOP_REQUEST_DATE_RANGE_INVALID` 하나로 응답한다. 변경이력에서 상한만 `@PastOrPresent`로 잡았다가 응답 계약이 갈린 선례가 있다. 또한 변경이력과 달리 **조회 기간 상한이 없다**(근거는 `ShopRequestQueryService`).

### `ShopReviewSearchRequest` — 기간 상·하한 관계 검증은 서비스가 소유

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/review/adapter/in/web/request/ShopReviewSearchRequest.java`
→ record 컴포넌트 `startDate`/`endDate`

`startDate`/`endDate`의 **상·하한 관계는 Bean Validation이 아니라 서비스가 판정한다**. 두 필드에 걸친 하나의 규칙이라 어노테이션으로 쪼개면 같은 규칙 위반인데 응답 계약이 갈린다(`ShopRequestSearchRequest` 선례와 같은 판단).

### `ProductNutritionUpdateRequest` — 필수 5종에 `@NotNull` 금지 · 음수 금지는 의도된 이중 방어

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductNutritionUpdateRequest.java`
→ 필수 5종 record 컴포넌트 및 수치 컴포넌트

- **필수 5종에 `@NotNull`을 붙이지 않는다.** "전부 채우거나 전부 비우기"는 필드 하나로 판정할 수 없는 집합 제약이고, 개별 `@NotNull`을 걸면 "전부 비우기"(영양성분 미표시)라는 정상 요청이 400으로 막힌다. 판정은 도메인(`ProductNutrition`)이 한 곳에서 수행해 `PRODUCT_NUTRITION_REQUIRED_FIELD_MISSING`으로 응답한다.
- 음수 금지는 `@Min(0)`과 도메인 검증에 **이중**으로 있다(중복이 아니다). 도메인 쪽이 계약상의 `code`(`PRODUCT_NUTRITION_VALUE_NEGATIVE`)를 보장하는 단일 소유자이고, 여기의 `@Min`은 그 앞단 방어다. **한쪽만 지우지 말 것.**

### `ProductNutritionUpdateRequest.toCommand` — 위치 기반 조립 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductNutritionUpdateRequest.java`
→ `toCommand(Long, Long)`

같은 타입의 영양성분 필드가 **11개 연달아** 있어 위치 기반 조립은 뒤바뀜을 컴파일러가 잡지 못한다. **반드시 이름 기반 접근자로 조립한다.**

### `ProductOptionCreateRequest` / `ProductOptionUpdateRequest` — `cupCount`에 `@Min`/`@Max` 재부착 금지

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductOptionCreateRequest.java` → record 컴포넌트 `cupCount`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductOptionUpdateRequest.java` → record 컴포넌트 `cupCount`

범위(1~10) 검증은 Bean Validation이 아니라 도메인 계층(`CupDepositPolicy#validateCupCount`)이 소유한다. **여기에 `@Min`/`@Max`를 다시 붙이지 말 것** — 경계별로 다른 문구가 나가 `ErrorCode.PRODUCT_OPTION_CUP_COUNT_INVALID`의 통합 메시지("1개 이상 10개 이하")와 어긋난다.

### `ProductOptionCreateRequest.toCommand` / `ProductOptionUpdateRequest.toCommand` / `ProductPriceItemRequest.toCommand` — 위치 기반 조립 금지

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductOptionCreateRequest.java` → `toCommand(Long, Long)`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductOptionUpdateRequest.java` → `toCommand(Long, Long)`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductPriceItemRequest.java` → `toCommand()`

같은 타입의 금액·수량 필드가 연달아 있어 위치 기반 조립은 뒤바뀜을 컴파일러가 잡지 못한다. **반드시 이름 기반 접근자로 조립한다.**

### `ProductPriceItemRequest` — `storePrice`·`pickupPrice`·`priceName`에 `@NotNull`/`@NotBlank` 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductPriceItemRequest.java`
→ record 컴포넌트 `storePrice`, `pickupPrice`, `priceName`

- **`storePrice`·`pickupPrice`에 `@NotNull`을 붙이지 않는다.** 두 값은 매장 가격 인증을 받은 가게만 채울 수 있고 미인증 가게는 비워 보내는 것이 정상 요청이다. 인증 게이트는 도메인(`ProductPriceService`)이 `PRODUCT_PRICE_STORE_NOT_VERIFIED`로 판정한다.
- **가격명도 `@NotBlank`가 아니다.** 가격 행이 1개면 가격명이 없어도 되고 2개 이상일 때만 필수라는 **집합 제약**이라 필드 하나로 판정할 수 없다(도메인이 `PRODUCT_PRICE_NAME_REQUIRED`로 판정).

### `ProductRepresentativeCreateRequest` — 개수 상한에 `@Size(max = 6)` 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductRepresentativeCreateRequest.java`
→ record 컴포넌트 `productIds`

개수 상한을 Bean Validation으로 가로채지 않는다(**`@Size(max = 6)`을 붙이지 않는다**). "6개 초과"가 400 검증 오류로 걸리고 "이미 5개 있는데 2개 추가"는 도메인 에러코드로 내려가면 같은 개수 위반이 상황에 따라 다른 `code`로 응답되어 프론트 분기가 갈린다. 판정은 도메인 한 곳(`PRODUCT_REPRESENTATIVE_LIMIT_EXCEEDED`)에 맡긴다.

### `ProductOrderRequest.productCategoryId` — `@NotNull` 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductOrderRequest.java`
→ record 컴포넌트 `productCategoryId`

`@NotNull`을 붙이지 않는다 — 미분류(`null`) 메뉴 목록도 정당한 재정렬 대상이다.

### `ShopDeliveryTipTiersUpdateRequest.tiers` — `@NotEmpty` 금지(`@NotNull`만)

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopDeliveryTipTiersUpdateRequest.java`
→ record 컴포넌트 `tiers`

`@NotEmpty`를 붙이지 않고 `@NotNull`만 두는 것은 **의도**다. "1~3개" 개수 불변식은 도메인 서비스가 `SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED`로 판정하므로, 여기서 빈 배열만 따로 400 검증 오류로 가로채면 같은 위반(개수 규칙)이 입력값에 따라 서로 다른 에러코드로 내려가 프론트 분기가 갈린다.

### `ShopDeliveryTipRegionsUpdateRequest` / `ShopDeliveryTipSchedulesUpdateRequest` — 빈 배열 허용(`@NotEmpty` 금지)

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopDeliveryTipRegionsUpdateRequest.java` → record 컴포넌트 `regions`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopDeliveryTipSchedulesUpdateRequest.java` → record 컴포넌트 `schedules`

빈 배열은 "전부 삭제"를 뜻하는 **정상 입력**이므로 `@NotEmpty`를 쓰지 않는다. 특히 지역별은 전부 지워야 거리별로 전환할 수 있는 규격이라 빈 배열이 실제 사용되는 경로다.

### `ShopOriginInfoUpdateRequest` — `content`·`url`에 필수 검증 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopOriginInfoUpdateRequest.java`
→ record 컴포넌트 `content`, `url`

**`content`·`url`에 Bean Validation의 필수 검증을 걸지 않는다.** 두 필드의 필수 여부가 `sourceType`에 따라 갈리는 조건부 제약이라, 어느 한쪽에 `@NotBlank`를 붙이면 다른 방식으로 저장하는 정상 요청이 400으로 막힌다. 조건부 판정은 도메인(`ShopOriginInfo`)이 수행하고 스펙이 약속한 `code`(`SHOP_ORIGIN_CONTENT_REQUIRED` 등)로 응답한다 — 길이 제약만 여기서 미리 걸러 낸다.

### `ReviewBlindRequestCreateRequest` — `detailReason` 조건부 필수는 도메인 소유 / 첨부 3개 상한만 Bean Validation

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/review/adapter/in/web/request/ReviewBlindRequestCreateRequest.java`
→ record 컴포넌트 `detailReason`, `attachmentFileIds`

- `reason=ETC`일 때 `detailReason` 필수라는 규칙은 Bean Validation이 아니라 도메인 서비스가 판정한다(`REVIEW_BLIND_DETAIL_REASON_REQUIRED`) — 두 필드에 걸친 조건부 규칙이라 어노테이션으로 표현할 수 없고, 사유별 필수 여부는 도메인 규칙이다.
- 반면 **첨부 개수 상한(3개)은 Bean Validation이 판정한다** — 한 필드 안에서 닫히는 규칙이고, 개수는 스키마가 아니라 정책이라 별도 테이블 대신 여기서 막는다. **두 규칙의 소유자를 서로 옮기지 말 것.**

### `ShopRiderVisitGuideValidateRequest` — 길이 제한 Bean Validation 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopRiderVisitGuideValidateRequest.java`
→ record 컴포넌트 `visitGuide`

길이 제한을 Bean Validation으로 걸지 않는다 — 프론트 `maxLength`를 우회한 입력도 400이 아니라 **위반 사유 목록으로 같은 자리에서** 보여주기 위함이다.

### `ShopRiderVisitGuideUpdateRequest` / `ShopRiderPickupLocationUpdateRequest` — 도메인 규칙을 Request로 끌어올리지 말 것

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopRiderVisitGuideUpdateRequest.java` → record 컴포넌트 `content`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopRiderPickupLocationUpdateRequest.java` → record 컴포넌트 `latitude`/`longitude`

금칙어·실주소·배차 어휘 판정과 좌표 범위 판정은 도메인(`ShopRiderGuide`)이 담당한다. **Request로 끌어올리지 않는다** — 관리자 교정 경로(`admin-api`)에서도 같은 게이트가 적용되어야 하기 때문이다. 또한 방문 안내 문구는 **빈 문자열을 허용한다**(삭제 전용 엔드포인트를 두지 않고 "빈 값 PUT = 삭제"로 통일).

### `GeoPointRequest` / `GeoPointResponse` — 좌표를 배열로 바꾸지 말 것

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/GeoPointRequest.java` → record `GeoPointRequest`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/GeoPointResponse.java` → record `GeoPointResponse`

**`[경도, 위도]` 배열이 아니라 이름 있는 객체(`{latitude, longitude}`)로 주고받는다.** GeoJSON은 배열의 0번이 경도인데 지도 SDK·사람의 직관은 대개 "위도, 경도" 순서라, 배열로 바꾸면 순서를 뒤집어 보내도 값이 유효 범위 안이면 검증을 통과하고 **엉뚱한 곳에 배달지역이 그려진다**. 응답도 요청과 대칭으로 유지한다.

### `CeoReplyPhraseResponse` — `name`과 `displayName`을 합치지 말 것

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/ceo/adapter/in/web/response/CeoReplyPhraseResponse.java`
→ record 컴포넌트 `name`, `displayName`

`name`과 `displayName`을 **함께 내려준다**. 전자는 수정 폼에 되돌려 채울 원본이고(비어 있으면 비어 있는 그대로여야 한다), 후자는 목록에 찍을 표시명이다. **둘을 하나로 합치면** 이름을 비운 채로 등록한 문구를 수정하려 할 때 파생된 앞부분이 이름 칸에 들어가 그대로 저장되는 사고가 난다.

### `CeoShopAccessHistoryListItemResponse` / `ShopChangeHistoryListItemResponse` / `ShopRequestCommentResponse` — 내부 식별자·실명 노출 금지

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/ceo/adapter/in/web/response/CeoShopAccessHistoryListItemResponse.java` → record 컴포넌트 집합 (`actorAdminId` 부재)
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopChangeHistoryListItemResponse.java` → record 컴포넌트 집합 (`actorType`/`actorId` 부재)
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopRequestCommentResponse.java` → record 컴포넌트 집합 (작성자 실명·식별자 부재)

`actorAdminId`·`actorType`·`actorId`·관리자 실명을 **노출하지 않는다** — 내부 식별자이고, 점주는 자기 가게 이력만 보므로 행위자 정보가 필요 없다. 문의 스레드는 작성자 유형 라벨("점주"/"담당자")로만 구성한다.

### `ProductFeedbackApiController` / `ProductFeedbackResponse` — 제보자 정보 노출 금지

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductFeedbackApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/response/ProductFeedbackResponse.java` → record 컴포넌트 집합

**제보자 정보는 어떤 응답에도 담기지 않는다.** 점주가 특정 손님을 식별하면 보복 우려가 있고, 제보의 목적은 정보 수정이지 손님 응대가 아니다.

### `ProductFeedbackSearchRequest` — 조회 범위(지난 7일)를 파라미터로 열지 말 것 / `page`·`size`는 `Integer`

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductFeedbackSearchRequest.java`
→ record 컴포넌트 `page`/`size`, compact constructor `ProductFeedbackSearchRequest {}`

- 조회 범위(지난 7일)는 **요청 파라미터로 받지 않는다** — 점주가 창을 넓힐 수 있으면 중복 제보 방지 기간과 어긋나 집계가 왜곡된다. 서버가 고정한다.
- `page`/`size`를 프리미티브 `int`가 아니라 **`Integer`로 둔다.** `@ModelAttribute` 바인딩은 쿼리파라미터가 아예 없을 때 프리미티브에 `null`을 주입하려다 `MethodArgumentTypeMismatchException`(400)을 던지고, 그러면 compact constructor에 도달하지 못해 기본값 보정(0/10)이 무의미해진다. **프리미티브로 바꾸지 말 것.**

### `ProductPriceResponse.storePrice`/`pickupPrice` — `null`을 0으로 뭉개지 말 것

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/response/ProductPriceResponse.java`
→ record 컴포넌트 `storePrice`, `pickupPrice`, `id`

- `storePrice`·`pickupPrice`는 매장 가격 인증 전에는 `null`이다 — **빈 값을 0으로 뭉개지 않는다.** 0원은 "무료"라는 정당한 값이라, 미설정과 합치면 화면이 "매장가 0원"을 표시한다.
- `id`를 함께 내려주는 것이 전체 교체(PUT)의 전제다. 화면은 이 `id`를 그대로 실어 보내야 기존 행이 갱신되고, 빠뜨린 행은 삭제된다.

### `ProductOptionGroupMergeApiController` — 분리(unmerge) 엔드포인트 신설 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductOptionGroupMergeApiController.java`
→ 클래스 전체 (라우트 집합)

**분리(unmerge) 엔드포인트는 없다** — 합치기는 비가역이며, 그 사실을 **라우트의 부재로 표현**한다(`ProductOptionGroup`이 un-hide 메서드를 의도적으로 두지 않은 것과 같은 형태). 라우트를 추가하지 말 것.

### `ProductOptionApiController` — 옵션 목록 조회 엔드포인트 신설 금지 / 역조회 소유권 검증 필수

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductOptionApiController.java`
→ 클래스 전체

- 목록 조회 엔드포인트가 **없다** — 옵션은 옵션그룹 목록(`ProductOptionGroupApiController#getProductOptionGroups`)에 중첩되어 함께 내려온다. 옵션만 따로 조회할 화면이 없으므로 경로를 만들지 않는다.
- **모든 경로가 옵션그룹의 소유 가게를 역조회해 검증한다** — 옵션은 자기 가게를 모르므로 `옵션 → 그룹 → 링크 → 메뉴 → 가게` 역조회 없이는 남의 가게 옵션 조작을 막을 수 없다. **역조회를 생략하지 말 것.**

### `ProductOptionGroupUpdateRequest` — `productId`·순서를 이 경로로 받지 말 것

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductOptionGroupUpdateRequest.java`
→ record 컴포넌트 집합 (`productId` 부재)

등록 요청과 달리 `productId`를 **받지 않는다** — 그룹이 어느 메뉴에 연결되는지는 연결 API(`ProductOptionGroupLinkApiController`)의 관심사이고, 이 경로로 바꾸면 연결 집합이 두 곳에서 변경돼 단일 가게 불변식 검증이 흩어진다. **순서도 마찬가지로 이 경로로 바꾸지 않는다.**

### `ProductOptionUpdateRequest` — 품절·숨김·순서를 이 경로로 바꾸지 말 것

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductOptionUpdateRequest.java`
→ record 컴포넌트 집합

품절·숨김 상태와 순서는 이 경로로 바꾸지 않는다 — 각각 품절·숨김 API와 순서 변경 API가 소유한다.

### 순서 변경 계열 — `sort` 값 수신 금지 (replace-all)

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductSortApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductCategoryOrderRequest.java` → record 컴포넌트 집합
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductOptionGroupSortRequest.java` → record 컴포넌트 집합
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductOptionSortRequest.java` → record 컴포넌트 집합
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductImageSortRequest.java` → record 컴포넌트 집합
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductShopLinkItemRequest.java` → record 컴포넌트 집합
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopMenuCollectionImageOrderRequest.java` → record 컴포넌트 집합

**`sort` 값을 받지 않는다.** 순서 있는 id 배열만 받고 서버가 배열 인덱스로 `0..N-1`을 부여하므로 "sort 충돌"이라는 개념 자체가 존재하지 않는다. 클라이언트가 계산한 정렬값을 신뢰하면 중복·구멍이 생기고, 개별 위치 지정 방식은 동시 편집 시 두 항목이 같은 순서를 갖는 상태를 만든다. 목록이 최신 상태와 집합으로 다르면 `PRODUCT_ORDER_TARGET_MISMATCH`(400) 등으로 **거절한다**. 신규 연결(`ProductShopLinkItemRequest`)도 `sort`를 받지 않으며 대상 가게 메뉴판 **끝에 붙이는 것이 서버 규칙**이다 — 요청이 순서를 정하면 그 가게의 기존 배열을 헤집는다.

### replace-all(PUT) 전용 리소스 — 행 단위 CRUD를 열지 말 것

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductPriceApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductExposureApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductExposureRequest.java` → record 컴포넌트 집합
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductShopLinkReplaceRequest.java` → record 컴포넌트 집합
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopDeliveryTipApiController.java` → 클래스 전체

수정이 개별 행 CRUD가 아니라 **전체 교체(PUT) 하나**인 것이 이 리소스들의 핵심이다. 가격명 중복 금지·"2개 이상이면 가격명 필수"·표시 순서, "요일 묶음과 개별 요일 혼용 금지", "3개 이하 + 주문금액 오름차순 + 팁 내림차순", "링크 1개 이상 유지" 같은 규칙은 **목록 전체를 봐야 판정**되므로, 행 단위로 열면 어떤 순서로 조작해도 중간 상태가 반드시 규칙을 위반한다(두 구간의 금액을 맞바꾸려면 반드시 단조성이 깨진 상태를 한 번 거친다). **행 단위 CRUD 엔드포인트를 추가하지 말 것.** `ShopBusinessHour`가 개별 CRUD인 것은 요일 간에 이런 관계가 없기 때문이며, 배달팁은 그 조건을 만족하지 않는다.

### `ShopDeliveryTipApiController` — 파트별로 컨트롤러를 쪼개지 말 것

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopDeliveryTipApiController.java`
→ 클래스 전체

**파트별로 컨트롤러를 4개로 쪼개지 않는다.** 거리별과 지역별은 상호 배타라 두 리소스에 걸친 불변식을 가지며(지역별이 하나라도 있으면 거리별을 설정할 수 없고, 그 반대도 같다) 한 트랜잭션 안에서 함께 판정돼야 한다. 컨트롤러를 나누면 그 배타성이 어느 컨트롤러에도 **소유자 없이 흩어진다.**

### `ProductOptionGroupLinkApiController` — 연결·해제·순서를 분리하지 말 것

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductOptionGroupLinkApiController.java`
→ 클래스 전체

연결·해제·순서를 **한 컨트롤러가 소유한다** — 셋 모두 같은 링크 집합을 다루고, 특히 해제와 순서는 남은 연결의 `sort`를 함께 재정규화하므로 관심사를 흩어놓으면 불변식이 두 곳으로 나뉜다.

### `ProductSortApiController` / `ProductCategoryApiController` — 순서 관심사를 흩지 말 것

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductSortApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductCategoryApiController.java` → 클래스 전체

순서 변경은 메뉴 컨트롤러·메뉴그룹 컨트롤러가 아니라 `ProductSortApiController`가 소유한다 — 그룹 이동이 출발·도착 두 그룹의 정렬 집합을 동시에 바꾸므로 한 트랜잭션이어야 하고, 흩어지면 그 규칙이 두 곳으로 갈라진다.

### `ProductNutritionApiController` — 영양성분과 알레르기를 분리하지 말 것

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductNutritionApiController.java`
→ 클래스 전체

영양성분과 알레르기를 **한 컨트롤러가 소유한다** — 한 화면에서 함께 저장·삭제되는 한 벌이라, 나누면 두 리소스에 걸친 교체가 두 요청으로 갈라져 중간 상태(영양성분만 갱신되고 알레르기는 이전 값)가 **손님 화면에 잘못된 알레르기 표시로 노출된다.**

### 소유권 검증 생략 금지 (IDOR 전례)

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductAvailabilityApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductImageApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductPriceApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductFeedbackApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductNutritionApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductCategoryApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductOptionGroupApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductShopScopeRequest.java` → record 컴포넌트 `shopId`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductPriceReplaceRequest.java` → record 컴포넌트 `shopId`

**모든 핸들러가 body 또는 query의 `shopId`로 소유권을 검증한다.** 경로에 `shopId`가 없다는 이유로 검증을 생략하면 **IDOR가 된다** — 이 저장소는 배달가능지역 삭제에서 실제로 그 사고를 냈다. 그래서 `shopId`를 경로가 아니라 query·바디로 받고, 일괄 API도 `shopId`를 필수로 받게 해 그 형태를 구조적으로 없앤다. 나아가:

- **가게 소유권만 확인하면 부족하다.** 그 메뉴가 정말 그 가게 것인지(`ProductNutritionApiController`), 옵션그룹의 소유 가게가 맞는지(`ProductOptionGroupOwnershipValidator`)까지 대조한다.
- **이미지 삭제는 경로에 메뉴·가게 식별자가 없어 서비스가 이미지 → 메뉴 → 가게로 역조회해 대조한다** — 이 저장소는 그 역조회를 생략했다가 IDOR 사고를 낸 전례가 있다.

### `ShopMenuCollectionImageApiController` — 삭제는 가게 범위 안에서 대상을 찾는다

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopMenuCollectionImageApiController.java`
→ 클래스 전체

모든 핸들러가 경로의 `shopId`로 소유권을 검증한다. 삭제는 이미지 id가 경로에 있지만 **가게 범위 안에서 대상을 찾으므로**, 남의 가게 이미지 id는 소유권 검증을 통과했더라도 404로 떨어진다. 이 조회 범위를 넓히지 말 것.

### `CeoSeedProperties.UNSET_PASSWORD` / `CeoSeeder.seed` — 기본 비밀번호 시드 차단(fail-fast)

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/config/CeoSeedProperties.java` → `UNSET_PASSWORD`, `isDefaultPassword()`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/config/CeoSeeder.java` → 시드 실행 메서드의 `isDefaultPassword()` 분기

`UNSET_PASSWORD`(`__UNSET__`)는 `CEO_SEED_PASSWORD` 미설정 시의 **센티넬**이며, 이 값이면 시드를 거부(fail-fast)한다. 기본(취약) 비밀번호로 운영에 시드되는 것을 방지하기 위해 **신규 시드 시에는 외부 주입 비밀번호를 강제한다.** 이 게이트를 완화하거나 기본값을 실제 비밀번호로 바꾸지 말 것.

### `CeoReplyPhraseResponse.resolveDisplayName` — 20자 이하에는 말줄임표 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/ceo/adapter/in/web/response/CeoReplyPhraseResponse.java`
→ `resolveDisplayName(String, String)`, 상수 `DISPLAY_NAME_LENGTH`(20) · `ELLIPSIS`

내용이 `DISPLAY_NAME_LENGTH`자 **이하면 잘린 것이 없으므로 말줄임표를 붙이지 않는다** — 붙이면 뒤에 더 있다는 잘못된 인상을 준다.

### `ProductOptionGroupResponse` / `ProductCategoryResponse` — 숨김·감춘 항목 필터 금지

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/response/ProductCategoryResponse.java` → record 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/response/ProductOptionGroupResponse.java` → record 전체

손님 메뉴판과 달리 **숨긴 그룹·감춘 옵션도 포함**해 내려온다 — 이 화면이 숨김·감추기(소프트 삭제)를 조작하므로 **필터를 걸면 되살릴 방법이 없어진다.** 또한 `ProductOptionGroupResponse`는 일반 옵션그룹만 담는다 — 공통 옵션그룹은 점주 CRUD 대상이 아니며, 두 테이블의 id 공간이 독립적이라 한 목록에 섞으면 후속 요청의 id가 어느 갈래인지 알 수 없어진다.

### `ProductImageResponse` — 파일 식별자 노출 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/response/ProductImageResponse.java`
→ record 컴포넌트 집합 (`fileId` 부재)

파일 식별자를 노출하지 않고 표시용 URL만 담는다 — 프론트엔드가 `fileId`로 URL을 조립할 공식 엔드포인트가 없어 존재하지 않는 경로를 추측하게 되기 때문이다.

### `ShopOrderNoticeResponse` — 식별자를 담지 말 것

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopOrderNoticeResponse.java`
→ record 컴포넌트 집합 (id 부재), `empty()`

**식별자를 담지 않는다.** 주문안내는 가게당 1건이고 모든 조작이 `shopId` 경로로 이루어지므로(PUT 전체교체, 관리자 hide/unhide) 프론트가 ID를 쓸 곳이 없다. 미설정 가게도 `data: null`이 아니라 `content: null`인 객체를 받는다 — 그래야 프론트가 두 가지 빈 상태(미설정 / 응답 없음)를 구분하지 않는다. `empty()`는 항상 게시중 상태다(게시중단은 등록된 문구에만 걸린다).

### `ShopStorePriceUnverifiedItemResponse.reason` — 한글 문구가 아니라 enum 상수명

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopStorePriceUnverifiedItemResponse.java`
→ record 컴포넌트 `reason`

`reason`은 한글 문구가 아니라 **enum 상수명**이다. 사유별로 점주가 할 조치가 다르므로(미등록은 매장가 입력, 배달가 초과는 배달가 인하) 화면이 코드로 분기해 각기 다른 안내·버튼을 띄운다 — **문구를 내려주면 서버 문구 변경이 곧 화면 분기 파손이 된다.**

### `ShopStorePriceVerificationResponse` — `verified`와 `status`를 합치지 말 것

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopStorePriceVerificationResponse.java` → record 컴포넌트 `verified`, `status`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopStorePriceVerificationApiController.java` → 조회 핸들러

**`verified`와 `status`는 서로 다른 축이며 합칠 수 없다.** 인증은 승인 후에도 배달가가 매장가를 넘어서면 자동 해제되므로, 최근 요청이 `APPROVED`인데 `verified=false`인 상태가 **정상적으로 존재한다.** 화면은 매장가·픽업가 입력 가능 여부를 `verified`로, 진행 중 안내(대기·검수 중·반려 사유)를 `status`로 판단한다. 한 번도 요청하지 않은 가게는 `id`·`status`·`rejectReason`이 모두 `null`이고 `verified`만 유효하다 — **미요청을 별도 상태값으로 만들지 않는다**(도메인 enum에 없는 값이 응답 계약에 섞이면 프론트가 서버 enum과 화면 상수를 따로 관리해야 한다).

### `AdminDongBoundaryItemResponse` / `ShopDeliveryAreaPolygonResponse` — 미보유·미설정을 404로 만들지 말 것

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/region/adapter/in/web/response/AdminDongBoundaryItemResponse.java` → record 컴포넌트 `rings`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopDeliveryAreaPolygonResponse.java` → record 컴포넌트 `exists`

- **경계 미보유는 404가 아니라 `rings: null`인 200이다.** 시드가 코드·좌표 먼저, 경계는 나중에 들어오므로 "좌표는 있고 경계는 없는" 상태가 정상이다. 그런 동을 목록에서 빼면 화면이 "이 지역에 행정동이 없다"로 오해하게 된다.
- **도형 미설정은 404가 아니라 `exists: false`인 200이다.** 도형을 그리지 않고 행정동만 직접 등록한 가게가 정상적으로 존재하므로 미설정은 오류가 아니라 상태다. 404로 응답하면 화면이 정상 상태를 에러로 처리한다.

### `AdminDongBoundaryResponse.truncated` — 넓은 영역을 400으로 거절하지 말 것

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/region/adapter/in/web/response/AdminDongBoundaryResponse.java`
→ record 컴포넌트 `truncated`

`truncated`가 `true`면 요청 영역이 너무 넓어 경계를 내려보내지 않은 것이다(전국 줌 레벨에서 3,600개 동 경계는 수십 MB). **400으로 거절하지 않고 빈 배열로 응답한다** — 지도를 축소하는 것은 오류가 아니라 정상 조작이기 때문이다.

### `AdminDongPointResponse` — `GeoPointResponse`와 타입을 공유하지 말 것

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/region/adapter/in/web/response/AdminDongPointResponse.java`
→ record `AdminDongPointResponse`

배달지역 도형의 `GeoPointResponse`와 형태가 같지만 **타입을 공유하지 않는다** — 그쪽은 `shop` 도메인의 응답이고 이쪽은 `region` 도메인의 응답이라, 한쪽 스키마가 바뀔 때 다른 쪽 계약이 함께 끌려가지 않게 분리한다(도메인별 response 소유 규칙). 중복이라고 통합하지 말 것.

### `ShopDeliveryAreaRadiusRequest.replace` — 기본값 `false`(더하기) 유지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopDeliveryAreaRadiusRequest.java`
→ record 컴포넌트 `replace`

`replace`가 `false`(기본)면 기존 설정 위에 더하고, `true`면 반경 밖의 기존 행정동 직접 등록분을 닫고 교체한다. **기본값을 "더하기"로 두는 이유는, 실수로 보냈을 때 기존 설정이 사라지지 않는 쪽이 안전하기 때문**이다. 기본값을 뒤집지 말 것.

### `ShopDeliveryAreaPolygonSaveRequest` — 링·정점 상한의 이중 검증은 층이 다르다

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopDeliveryAreaPolygonSaveRequest.java`
→ record 컴포넌트 `rings`

링 개수·정점 개수 상한은 여기서 형식으로 한 번, 도메인 정책(`ShopDeliveryAreaPolicy`)에서 한 번 검증한다. **중복 검증이 아니라 층이 다르다** — Bean Validation은 요청 형식을, 도메인은 저장 가능한 도형인지를 본다(도메인 서비스는 HTTP 경계 밖에서도 호출될 수 있다). **한쪽을 지우지 말 것.**

### `ShopOrderNoticeUpsertRequest` — Bean Validation은 1차 방어일 뿐

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopOrderNoticeUpsertRequest.java`
→ record 컴포넌트 `content`

Bean Validation은 프론트에 필드 단위 오류를 빠르게 돌려주기 위한 **1차 방어일 뿐**이고, 같은 규칙을 `ShopOrderNoticeService`가 도메인 예외(`SHOP_ORDER_NOTICE_CONTENT_*`)로 다시 지킨다 — presentation 계약은 다른 진입 경로가 추가되면 우회되므로 **유일한 방어선일 수 없다.** 도메인 쪽 검증을 지우지 말 것.

### `ProductVegetarianRequest.ingredients` — 필수 유지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductVegetarianRequest.java`
→ record 컴포넌트 `ingredients`

`ingredients`가 필수인 이유는 그것이 **관리자 검수의 유일한 근거**이기 때문이다 — 재료를 보지 않고는 이 메뉴가 정말 그 채식 단계인지 판정할 수 없다. 선택 필드로 완화하지 말 것.

### `ProductOptionGroupCreateRequest.productId` — 필수 유지(고아 그룹 방지)

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductOptionGroupCreateRequest.java`
→ record 컴포넌트 `productId`

`productId`가 필수인 이유: `PRODUCT_OPTION_GROUP.product_id`가 1단계 배포 동안 `NOT NULL`로 남아 있고(`product-menu-management.sql` STEP 6에서 제거 예정), 무엇보다 **연결이 0건인 그룹은 어느 화면에서도 보이지 않는 고아**가 된다. 등록 시 이 메뉴에 곧바로 연결해 그룹이 항상 소유 가게로 역조회되도록 보장한다. **STEP 6 이후에도 이 필드를 선택으로 바꾸기 전에 고아 방지 대책을 먼저 마련할 것.**

### `ProductOptionGroupMergeSuggestionResponse.signature` — 불투명 토큰

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/response/ProductOptionGroupMergeSuggestionResponse.java`
→ record 컴포넌트 `signature`

`signature`는 프론트가 해석하지 않고 제외([X]) 요청에 **그대로 실어 보내는 불투명 토큰**이다. 서버는 함께 받은 `optionGroupIds`로 서명을 재계산해 위조·낡은 토큰을 거부한다. 프론트가 파싱하게 만들지 말 것.

### `ProductAvailabilityChangeResponse` — 전체 실패도 200

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/response/ProductAvailabilityChangeResponse.java`
→ record 컴포넌트 `failed`

**전체가 실패해도 HTTP 200 + `failed` 전량으로 응답한다** — 부분 성공과 전체 실패의 응답 형태를 갈라놓으면 프론트가 두 경로를 타야 하고, "1건 실패"와 "전건 실패"의 화면 처리가 실제로는 같다. 요청 자체가 잘못된 경우(빈 배열·기간 범위 위반·소유권 위반·가게 미존재)**만** 4xx다.

### `ShopRiderVisitGuideValidationResponse` — 위반도 200

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopRiderVisitGuideValidationResponse.java`
→ record 전체

위반이 있어도 예외가 아니라 **200으로 사유 목록을 반환한다** — 검수 결과 자체가 정상 응답이며, 프론트가 저장 실패 토스트가 아니라 인라인 위반 목록으로 보여주기 위함이다.

### `ShopReviewStatisticsResponse.hasData` — 0으로 채운 그래프 금지

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/review/adapter/in/web/response/ShopReviewStatisticsResponse.java`
→ record 컴포넌트 `hasData`

`hasData`가 `false`면(최근 180일 리뷰 0건) 나머지는 전부 `null`·빈 값이다 — 원문이 "180일간 리뷰가 없으면 대시보드를 노출하지 않는다"로 규정하므로, **0으로 채운 그래프를 보여주는 대신** 화면이 통째로 빈 상태를 렌더링할 수 있게 한다.

### `ShopMenuCollectionImageResponse` — 검수 대기·반려 건도 내려간다

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopMenuCollectionImageResponse.java`
→ record 컴포넌트 `status`, `rejectReason`

검수 대기·반려 건도 함께 내려간다 — 원문 규격이 점주 화면에 대기/승인/취소 상태를 보여주도록 규정하기 때문이다. 손님 화면(`web-api`)의 응답에는 `status`·`rejectReason`이 **없다**. 두 응답을 통합하지 말 것.

### `ShopRequestDetailResponse` — 다형 응답·`Map` 금지 / `status`는 원본 애그리거트 값

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopRequestDetailResponse.java`
→ record 컴포넌트 `status`, `rejectReason`, 유형별 nullable 서브 객체

- 다형 응답(`oneOf`)이나 `Map<String,Object>` 대신 **nullable 서브 객체**를 쓴다 — OpenAPI로 그대로 표현되고, 프론트 분기가 `requestType` 하나로 결정되며, 유형이 추가될 때 필드 추가만으로 끝난다.
- `status`·`rejectReason`은 **원본 애그리거트 값**이다. 인덱스 행은 파생 읽기모델이라 진실원이 아니므로, drift가 생겨도 영향 범위가 목록 배지 하나로 좁혀진다. **인덱스 행 값으로 바꾸지 말 것.**

### `ShopRequestListItemResponse.requestId` — 유일한 대외 식별자

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopRequestListItemResponse.java`
→ record 컴포넌트 `requestId`, 첨부 관련 컴포넌트

`requestId`는 **요청의 유일한 대외 식별자**다 — 상세·취소·댓글 URL이 모두 이 값 하나만 쓴다. 첨부는 존재 여부만 내려주고 URL은 상세에서 준다(목록에서 파일 join·URL 조립 비용을 치르지 않는다).

### `ShopDeliveryAreaBulkDeleteResponse` — 부분 삭제가 아니다

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopDeliveryAreaBulkDeleteResponse.java`
→ record 전체

지역별 배달팁이 참조하는 동이 하나라도 섞이면 **한 건도 지우지 않고 409**로 끝나므로, 이 응답은 항상 "전부 지워진" 상태만 나타낸다(부분 삭제 결과가 아니다). 부분 삭제로 완화하지 말 것.

### `ShopDeliveryAreaRadiusPreviewResponse.unresolvedCount` — 감추지 말 것

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopDeliveryAreaRadiusPreviewResponse.java`
→ record 컴포넌트 `unresolvedCount`, `circle`

- `unresolvedCount`는 좌표·경계를 보유하지 않아 판정하지 못한 동 수다. **조용히 감추지 않고 노출해** 시드 데이터 공백을 점주와 운영이 인지할 수 있게 한다.
- `circle`(72각형 근사 원)을 함께 내려주는 이유는 화면이 서버와 **같은 도형**을 그리게 하기 위해서다. 클라이언트가 자체 공식으로 원을 그리면 경도 보정(`1/cos φ`) 유무에 따라 서버 판정과 눈에 보이는 원이 어긋난다. **클라이언트 계산으로 대체하지 말 것.**

### `ShopBusinessHourResponse` / `ShopBreakTimeResponse` / `ShopHygieneBadgeResponse` — `api-common-module`로 되돌리지 말 것

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopBusinessHourResponse.java` → record 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopBreakTimeResponse.java` → record 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopHygieneBadgeResponse.java` → record 전체

과거에는 admin·ceo가 바이트 동일하다는 이유로 `api-common-module`이 이 record들을 단독 소유했으나, 그 위치는 **표현 계약을 공유 웹 어댑터 모듈이 갖는** 배치라 application 계층이 조립하려면 `api-common`에 의존해야 했다. 지금은 앱별로 각자 소유한다 — admin·ceo가 같은 필드 구성을 갖는 것은 중복이 아니라 **우연히 일치한 앱별 응답 계약**이며, 한쪽 화면 요구가 바뀌면 다른 쪽을 건드리지 않고 갈라질 수 있어야 한다. **중복 제거를 이유로 공유 모듈로 되돌리지 말 것.**


## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. ceo-api 고유분 (챕터 06 이관). 3앱 공통 규칙(인가·enum 경계·조회 파라미터)은 backend/AGENTS.md 참조 -->

3앱 공통 규칙 — **인가는 `SecurityConfig`가 소유** · **도메인 enum은 경계에서 `String`** · **조회 파라미터는 Request record** — 는 [backend/AGENTS.md](../AGENTS.md)의 "계층 규칙 봉인 — api 앱 3종 공통"에 한 벌로 있다. 아래는 `ceo-api` 고유분이다.

### 컨트롤러가 서블릿 타입을 풀어 넘긴다 — 서비스 계층 web 의존 금지 경계

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/auth/adapter/in/web/AuthApiController.java` → `login(LoginRequest, HttpServletRequest)`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/auth/adapter/in/web/request/LoginRequest.java` → `toCommand(String, String)`

점주 로그인은 성공·실패 모두 개인정보처리시스템 접속기록으로 남으므로, 서블릿 타입을 **컨트롤러에서 풀어** IP·User-Agent를 `String`으로 서비스에 넘긴다. 접속기록에 남길 `ipAddress`·`userAgent`는 본문이 아니라 서블릿 요청에서 나오므로 컨트롤러가 뽑아 넘기고, **Command는 경계 타입만 싣고 서블릿을 알지 않는다**(서비스 계층의 web 의존 금지 경계).

### multipart는 JSON 바디를 함께 실을 수 없다 — 문자열 파트 파싱은 서비스가 한다

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopStorePriceVerificationApiController.java` → 등록(multipart) 핸들러
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopDeliveryAreaAdjustmentCreateRequest.java` → record 전체 (`file` 부재)

**등록이 multipart인 것은 가격표 이미지와 대상 목록이 한 요청에 함께 와야 하기 때문**이다. 두 요청으로 쪼개면 중간에서 끊긴 건이 첨부만 있고 대상이 없는 고아 상태로 남아, 관리자 검수 큐에 검수할 수 없는 건이 쌓인다. multipart는 JSON 바디를 함께 실을 수 없어 대상 목록만 `items` **문자열 파트**로 받아 command에 그대로 실어 넘기고, **파싱은 서비스가 한다**(컨트롤러·Request는 domain-free라 컨트롤러에서 파싱할 수 없다).

같은 이유로 배달지역 조정 신청의 동의서 파일(`file`)은 Request record가 아니라 컨트롤러가 `MultipartFile` 파라미터로 별도 수신하고, `multipart/form-data`의 **텍스트 파트만** record에서 검증·문서화한다.

### 챕터 09 — 표현 규칙의 파생 위치를 QueryService에서 Response로 옮겼다

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/ceo/adapter/in/web/response/CeoReplyPhraseResponse.java` → `resolveDisplayName(String, String)`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/review/adapter/in/web/response/ShopReviewListItemResponse.java` → `toReviewNumber(Long)`, 상수 `REVIEW_NUMBER_LENGTH`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopConvenienceInfoResponse.java` → `empty(Long)`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopOriginInfoResponse.java` → `empty()`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopDeliveryTipDistanceResponse.java` → `from(ShopDeliveryTipSettingResult)`, 상수 `EXTRA_TIP_TYPE_DISTANCE`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopRiderPickupLocationResponse.java` → `from(ShopRiderGuideResult)`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopIntroductionResponse.java` → `from(String)`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopBusinessHourResponse.java` → record 전체

챕터 09(Response 승격)에서 **표현 규칙을 QueryService에서 Response 표현 계약으로 옮겼다.** 판단 기준은 "도메인 불변식인가, 화면 규칙인가"다.

- "이름이 비면 내용 앞부분을 보여준다"는 **화면 규칙**이지 도메인 불변식이 아니다(파생값을 저장하면 내용 수정 시 어긋난다).
- 리뷰 번호 0-pad 자릿수(16자리)도 화면 표기 규칙이다.
- 미등록 가게의 기본값 조립(`empty()`)도 표현 계약이다 — 미등록을 `data: null`이 아니라 기본값 객체로 내려, 프론트가 두 가지 빈 상태(미등록 / 응답 없음)를 구분하지 않게 한다.
- 픽업 위치는 **세 값(주소·위도·경도)이 모두 있어야 위치로 성립한다**는 것이 표현 규칙이라 하나라도 없으면 `null`을 반환해 프론트가 "가게 실주소로 폴백" 상태임을 한 필드로 판정한다.
- 등록 이력이 없는 조회는 `Result`가 없는 정상 형태이므로 Response가 원시값을 그대로 받는다(`ShopIntroductionResponse.from(String)`).

반면 **`application` 잔류분**: 요일 표시명(`description`)처럼 도메인 enum에서 파생되는 값은 `ShopBusinessHourResult`를 받은 QueryService의 private 매퍼가 풀어 넘긴다 — Response record 자체는 **domain-free**여야 하기 때문이다.

### `CeoSeeder`는 인바운드 포트만 주입하며 원래부터 domain-free다

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/config/CeoSeeder.java`
→ 클래스 전체 / 생성자 주입 `CeoOwnerQueryUseCase`

공개 회원가입이 없으므로 첫 점주 계정은 부팅 시 **멱등하게** 주입한다(초기 자격증명은 `application.yml`의 `ceo.seed.*`). 조회는 구체 서비스가 아니라 **인바운드 포트**(`CeoOwnerQueryUseCase`)를 주입한다(`seedersShouldDependOnUseCasesOnly` 규칙). `AdminSeeder`와 달리 role 개념이 없어(`CeoCreateCommand`에 role 필드가 없다) **이 모듈은 원래부터 domain-free**다.

### 리소스 경계 — 어느 컨트롤러가 무엇을 소유하는가

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductOptionGroupApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductOptionGroupMergeApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopOrderNoticeApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopStorePriceVerificationApiController.java` → 클래스 전체

- **옵션그룹은 여러 메뉴에 연결될 수 있으므로 가게 단위 리소스**다 — 그래서 목록·등록이 메뉴 하위 경로가 아니라 `/option-groups`에 있다. 어느 메뉴에 연결하느냐는 별도 관심사이며 `ProductOptionGroupLinkApiController`가 소유한다.
- **합치기는 컨트롤러를 분리**했다 — 추천·제외·미리보기·실행 4개 워크플로를 갖는 독립 기능이어서 `ProductOptionGroupApiController`에 넣으면 그 클래스가 두 배가 된다.
- **주문안내와 사장님 공지는 별개 컨트롤러**다 — 공지는 여러 건을 등록해 그중 1건만 노출하는 목록형 자원(`/notices/{noticeId}`)이고, 주문안내는 가게당 1건 단일 자원(`/order-notice`)이라 경로 형태와 메서드 구성이 다르다.
- **매장 가격 인증의 요청 취소·검수는 이 컨트롤러에 없다** — 취소는 통합 요청처리 현황(`ShopRequestApiController`)이, 검수는 `admin-api`가 담당한다.
- **삭제도 body로 받는 컨벤션**: `shopId`를 경로가 아니라 query 또는 body로 받고 삭제도 body로 받는다 — 메뉴 일괄 삭제(`ProductApiController#deleteProducts`)와 동일하며, 프론트엔드 `ApiClient#delete`가 DELETE 요청 본문에 JSON으로 `shopId`를 담아 보낸다.

### 조회 전용 엔드포인트를 두지 않는 리소스

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopMinOrderAmountApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopScheduledOrderApiController.java` → 클래스 전체

최소주문금액·예약주문 설정은 조회 전용 엔드포인트를 따로 두지 않는다(`ShopStatusApiController`가 GET/PUT 쌍인 것과 다른 점). 현재 값은 가게 상세 조회(`GET /api/shops/v1/{id}`)의 `minOrderAmount`·`scheduledOrderEnabled` 필드로 이미 내려가고, 점주 대시보드가 가게 정보를 한 덩어리로 받아 설정 행들을 렌더하므로 별도 조회가 왕복만 늘린다.

### 계정 단위 리소스는 가게 식별자를 받지 않는다

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/ceo/adapter/in/web/CeoLoginHistoryApiController.java` → 목록 조회 핸들러
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/ceo/adapter/in/web/CeoReplyPhraseApiController.java` → 클래스 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/ceo/adapter/in/web/CeoShopAccessHistoryApiController.java` → 목록 조회 핸들러

로그인 이력·자주 쓰는 문구는 **점주 계정 단위**라 가게에 종속되지 않으므로 가게 식별자를 받지 않는다. 인가는 토큰의 `ceoId`로 필터하는 것 자체이거나(로그인 이력), 토큰의 `ceoId`와 문구의 `ceoId` 일치로 수행한다(자주 쓰는 문구).

접근권한 이력의 `shopId`는 **필터일 뿐 인가 대상이 아니다** — 토큰의 `ceoId`로 함께 필터하므로 남의 가게 id를 넣으면 빈 목록이 되고, 가게 존재 여부가 새지 않는다.

### 앱 간 같은 URL은 의도된 것이다 — 응답 계약은 앱마다 다르다

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopMenuCollectionImageApiController.java` → 목록 조회 핸들러
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopOrderNoticeApiController.java` → 조회 핸들러
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopOriginInfoApiController.java` → 조회 핸들러
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopOriginInfoResponse.java` → record 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/response/ProductNutritionResponse.java` → record 컴포넌트 `allergens`

메뉴모음컷 목록·주문안내·원산지 조회 경로는 `web-api`의 손님용 엔드포인트와 **같은 URL**이며 이것은 의도된 것이다 — 앱은 서로 다른 호스트·포트로 서비스되고, **응답 계약이 달라 각 모듈이 자기 버전을 소유한다.**

| 항목 | 점주(`ceo-api`) | 손님(`web-api`) |
|---|---|---|
| 메뉴모음컷 | `status`·`rejectReason` 포함 | 미포함 |
| 주문안내 | 게시중단 여부·사유 포함 | 미포함 |
| 원산지 | `updatedAt` 포함, 미설정 시 빈 폼용 기본값 | `updatedAt` 없음, 미설정 시 `data: null` |
| 알레르기 | **코드 배열**(체크박스 상태 복원용) | **한글 라벨 배열** |

### 라벨을 서버가 내려준다 — 프론트 상수 복제 방지

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/ceo/adapter/in/web/response/CeoLoginHistoryListItemResponse.java` → record 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopChangeHistoryListItemResponse.java` → record 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopRequestListItemResponse.java` → record 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/response/ShopRequestTypeCatalogResponse.java` → record 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/response/ProductAllergenTypeResponse.java` → record 전체

**코드와 한글 라벨을 함께 내려준다** — 코드는 프론트 분기용, 라벨은 표시용이다. 라벨을 서버가 내려주면 프론트에 라벨 상수(변경이력 29개 중분류, 요청 유형·상태 등)를 복제하지 않아 표기 변경이 **서버 배포만으로** 반영된다. 알레르기 체크박스 목록도 서버가 공급하며(화면이 매핑표를 들면 성분 추가·변경 때 화면 배포가 필요해진다) **배열 순서는 법령 열거 순서**이고 화면은 그 순서대로 그린다. `ShopRequestTypeCatalogResponse`처럼 가게에 종속되지 않는 정적 카탈로그는 소유권 검증이 없다(`/v1/change-history-types` 선례).

### `POST`지만 의미는 조회 — 도형은 URL에 들어갈 수 없다

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/ShopDeliveryAreaApiController.java` → `previewPolygon(...)`
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/shop/adapter/in/web/request/ShopDeliveryAreaPolygonSaveRequest.java` → `toRingCommands()`

HTTP 메서드는 `POST`지만 **의미는 조회**다 — 도형이 URL에 들어갈 수 없어 본문으로 받을 뿐이며, 저장하지 않고 환산 결과만 계산해 돌려준다. 그래서 미리보기는 `ceoId`·`shopId`를 실은 저장 command가 아니라 `toRingCommands()`로 **링 배열만 경계 타입으로 승격**해 넘긴다.

### record는 상속을 지원하지 않는다 — 조회 조건 record를 합치지 않는 이유

**대상**:
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductFeedbackSearchRequest.java` → record 전체
- `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/request/ProductShopScopeRequest.java` → record 전체

`ProductFeedbackSearchRequest`는 `ProductShopScopeRequest`에 페이징을 더한 형태지만 **두 record를 합치지 않는다** — record가 상속을 지원하지 않고, 조회 조건마다 필요한 필드가 달라 하나로 묶으면 쓰지 않는 필드가 섞이기 때문이다.

반대로 **같은 필드 셋이면 한 record를 공용한다**: 등록·수정 요청(`CeoReplyPhraseCreateRequest`, `ReviewOwnerReplyCreateRequest`, `ShopOrderNoticeUpsertRequest`), 추가·삭제 요청(`ShopDeliveryAreaBulkRequest`) — 필드가 같은데 타입만 나누면 이름 외에 구별 정보가 없고, 한쪽에 제약을 추가할 때 다른 쪽을 빠뜨리기 쉽다. 주문안내가 하나의 record인 것은 엔드포인트가 하나(가게당 1건이라 `PUT`이 전체교체/upsert 의미론)이기 때문이다.

### Bean Validation과 도메인 검증의 층 구분 원칙 (`ceo-api` 전반)

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/**/adapter/in/web/request/*.java` (A절 각 항목의 앵커 참조)

`ceo-api` Request record 전반을 관통하는 판정 기준이다.

| 규칙 형태 | 소유자 | 근거 |
|---|---|---|
| 한 필드 안에서 닫히는 형식 제약(길이, 배열 길이 상한, 음수 금지) | **Bean Validation** | 요청 형식의 제약이며 앞단에서 걸러도 응답 계약이 갈리지 않는다 |
| 두 필드에 걸친 조건부 규칙(`sourceType`에 따른 필수, 기간 상·하한) | **도메인/서비스** | 어노테이션으로 쪼개면 같은 규칙 위반인데 응답 계약이 갈린다 |
| 집합 제약(전부 채우거나 전부 비우기, 개수 상한, 목록 전체 판정) | **도메인** | 필드 하나로 판정할 수 없고, 앞단에서 일부만 가로채면 같은 위반이 서로 다른 `code`로 내려간다 |
| 다른 진입 경로(`admin-api`)에서도 적용돼야 하는 규칙 | **도메인** | Request로 끌어올리면 관리자 교정 경로가 게이트를 우회한다 |

**의도된 이중 방어**도 있다(중복이 아니다): 배열 길이 상한(형식 vs 저장 가능성), 음수 금지(앞단 필터 vs 계약 `code` 단일 소유자), `@NotEmpty`와 `PRODUCT_PRICE_EMPTY`(가격 0개는 어떤 해석으로도 정상 요청이 아니어서 앞단에서 걸러도 응답 계약이 갈리지 않는다). **층이 다른 검증을 "중복"이라고 지우지 말 것** — 각 항목의 상세 근거는 A절 앵커에 있다.

### `ProductDetailResponse` — 별도 조회 API로 분리한 필드와 예외 1건

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/response/ProductDetailResponse.java`
→ record 컴포넌트 `exposureScheduled`

노출기간 상세값(요일·시간대·기간)·이미지·연결된 옵션그룹은 각각 별도 조회 API(§6·§7·§5-2)가 담당하므로 이 응답에 담지 않는다. 다만 **`exposureScheduled`만은 예외로 포함**한다 — 화면이 §6을 열기 전(최초 렌더·새로고침)에도 "노출기간 설정됨" 요약을 보여줘야 하기 때문이다.

### `ProductOptionGroupLinkedProductsResponse` — N+1 제거를 위한 벌크 응답

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/response/ProductOptionGroupLinkedProductsResponse.java`
→ record 전체

옵션그룹 연결 다이얼로그가 후보 그룹마다 `/option-groups/{id}/products`를 개별 호출하던 **N+1을 없애기 위해**, 가게의 옵션그룹 전체에 대한 연결 메뉴를 한 번에 담아 내려준다.

### `ProductShopLinkApiController` — 진입 축이 두 개인 이유

**대상**: `backend/ceo-api/src/main/java/com/tastyhouse/ceoapi/product/adapter/in/web/ProductShopLinkApiController.java`
→ `PUT /v1/{id}/shops` 및 `POST`·`DELETE /v1/{id}/shops/{targetShopId}`

**진입 축이 두 개다.** `PUT /v1/{id}/shops`는 **메뉴 기준**(이 메뉴를 어느 가게들에 노출할지 한 번에 정한다)이고, `POST`·`DELETE /v1/{id}/shops/{targetShopId}`는 **가게 기준**(이 가게 메뉴판에 메뉴를 불러오거나 뺀다)이다. 화면 진입 경로가 달라 둘 다 필요하다. 또한 `shopId`(요청 주체 가게)와 `links[].shopId`(연결 대상 가게)는 다른 축이다 — 앞의 것은 소유권 검증 기준, 뒤의 것은 노출 대상이다.
