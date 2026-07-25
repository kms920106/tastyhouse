<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-07-25 -->

# ceo-api

## Purpose
점주(매장 오너)용 REST API 애플리케이션 (실행 가능한 Spring Boot bootJar). `web-api`(일반 회원)·`admin-api`(관리자)와 대칭인 3번째 프레젠테이션 모듈로, 매장 사장님이 자기 매장·주문·예약·리뷰 등을 관리하는 셀프 서비스 API를 제공할 예정이다. `core-module`의 동일한 application 서비스를 재사용한다.

**현재 상태: 로그인 + 점주 가게 관리 API 구현 완료** — 모듈 골격 + JWT 인증 인프라 + 공통(common/exception) 요소, `auth`(로그인/토큰갱신/로그아웃)에 더해, 배민 사장님 셀프서비스 가이드 기반 **점주 가게 설정 API(`shop`)** 를 구현했다: 내 가게 조회, 영업시간·휴게시간(PDF 규격 검증), 휴무일(공휴일/정기/임시), 전화번호(다건+대표번호), 가게 상태(노출정지), 가게소개(금칙어 검수), 편의정보·찾아오는길·노출위치, 상표·대표이미지 변경요청(승인 워크플로), 콘텐츠보드, 영업 임시중지, 위생정보 조회. order 등 나머지 도메인 엔드포인트는 아직 없다.

- **점주-가게 소유권**: `Shop`에 `ceoId` 컬럼을 두어 1점주 N가게를 표현한다(관리자가 admin-api에서 배정). 모든 가게 관리 엔드포인트는 `shop/ShopOwnershipValidator.validateOwnership(ceoId, shopId)`를 먼저 호출해 `shop.ceoId == 로그인 ceoId`를 확인하고, 불일치 시 `AccessDeniedException(SHOP_ACCESS_DENIED)`(403)을 던진다. `CustomUserDetails`는 `ceoId`만 노출하므로 shopId는 경로/바디로 받아 이 검증기로 소유권을 확인한다.
- **검수/승인**: 상표·대표이미지는 `core/shared/model/ApprovalStatus`(PENDING/APPROVED/REJECTED)를 쓰는 공용 `ShopImageChangeRequest` 애그리거트로 "점주 변경요청 → admin 승인/반려 → 승인 시 Shop 반영" 워크플로를 구현한다. 가게소개·찾아오는길은 `ProhibitedWordValidator`(금칙어) 통과 시 즉시 반영, 콘텐츠보드는 즉시 노출 + admin 사후 숨김/삭제. 노출정지는 PENDING 승인요청 존재 시 차단(`SHOP_STATUS_CHANGE_BLOCKED_BY_PENDING_REQUEST`).
- **이미지 규격 검증**: `shop/ShopImageSpecValidator`가 상표(JPG·≤900KB·560×560↑·1:1)/콘텐츠(IMAGE JPG·PNG ≤10MB 700×700↑, GIF ≤10MB 250×250↑) 규격을 업로드 전 검증하고, 통과분만 `FileService`로 업로드한다. 유튜브 영상 길이(5~30분)는 서버 검증 불가라 URL 형식만 검증한다.

컨트롤러가 `core-module`에 직접 결합되는 것을 막기 위해, 신규 도메인은 admin-api와 동일하게 도메인별 ceo-api 소속 `{도메인}Service`를 두어 컨트롤러와 core 사이를 중개하는 패턴을 따른다(컨트롤러는 `com.tastyhouse.core.*`를 import하지 않고 ceo-api 타입에만 의존).

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | web + springdoc 의존, `core-module`·`infrastructure-module`·`external-api`·`logging-module`·`security-module` 참조 (admin-api와 동일 구성) |
| `src/main/resources/application.yml` | 점주 앱 환경 설정 (포트 `8100`, CORS 기본 `http://localhost:3020`, `jwt.secret=${JWT_SECRET_CEO}`) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/ceoapi/` | 점주 컨트롤러 루트 — `common/`(공용 인프라: `ApiResponse`·`PageRequest`·`PaginationResponse`), `config/`(JWT·Security·`CeoSeeder`/`CeoSeedProperties`), `exception/`(`GlobalExceptionHandler`), `auth/`(로그인·토큰갱신·로그아웃). 신규 도메인 폴더는 admin-api 컨벤션(`컨트롤러 + {도메인}Service + request/·response/`)대로 생성 |
| `src/test/` | 점주 API 테스트 (`contextLoads`) |

## For AI Agents

### Working In This Directory
- 새 기능 추가 시 admin-api와 동일한 도메인-폴더 + `request/`·`response/` 컨벤션, `{도메인}Service` 중개 계층, DTO 조립·`@ModelAttribute` 조회·`@PathVariable id` 통일·`@Schema` 문서화 규칙을 그대로 따른다. 상세·근거·예시는 루트 CLAUDE.md 및 `admin-api/AGENTS.md` 참고.
- **import 순서 — presentation 내부 서브정렬**: 자사 import의 presentation 계층(`com.tastyhouse.ceoapi.*`) 안에서 공용 인프라(`common`·`config`)를 도메인 전용(`<도메인>.request`·`.response`)보다 위에 둔다. 상세는 루트 CLAUDE.md 참고.
- 비즈니스 로직은 `core-module` application 서비스에 위임한다 — `{도메인}Service`는 위임·변환 계층일 뿐이다. web-api/admin-api와 core 서비스를 공유하므로 command/query 시그니처 변경 시 동시 수정 주의.

### Testing Requirements
- `@SpringBootTest` 기반 컨텍스트 로드/컨트롤러 검증.

### Common Patterns
- **JWT 인증 메커니즘은 `security-module`의 `com.tastyhouse.security.jwt`에 공유**된다. ceo-api의 `config/jwt/JwtTokenProvider`는 그 공용 provider를 상속해 `ceoId` 클레임·`CustomUserDetails` 재구성만 주입한다(검증 토큰 없음). 공용 필터는 `config/jwt/JwtConfig`가 점주 전용 블랙리스트 저장소(`ceo:bl:`)로 빈 등록하고, refresh 저장소는 `RedisRepositoryConfig`가 `ceo:rt:` 접두사로 등록한다. 정책은 ceo-api에 잔류: `config/security/SecurityConfig`·`PublicPaths`·`CustomUserDetails`(`JwtPrincipal` 구현, `Ceo` 도메인 모델 기반 생성자 포함)·`CeoUserDetailsService`.
- **점주 계정 도메인(`ceo`)은 core-module의 `admin` 도메인과 동일한 최소 CRUD 패턴**이다(`Admin` 대비 `role` 없이 `status`만 보유). `Ceo`(순수 POJO)/`CeoStatus`/`CeoId`/`CeoRepository`/`CeoQueryService`/`CeoCommandService`는 `core-module/.../domain/ceo/`에, 영속 어댑터(`CeoJpaEntity`/`CeoMapper`/`CeoJpaRepository`/`CeoRepositoryImpl`)는 `infrastructure-module/.../ceo/persistence/`에 있다. DDL은 `create.sql`의 `CEO` 테이블.
- **인가 체인은 `.anyRequest().hasRole("CEO")`로 강화되어 있다** — `CeoUserDetailsService`가 로그인 시 고정 `ROLE_CEO` 권한을 부여한다(점주는 단일 역할이라 역할 enum 없음).
- **최초 점주 계정은 부팅 시드로 주입**된다(`config/CeoSeeder`+`CeoSeedProperties`, admin-api `AdminSeeder` 패턴과 동일). `ceo.seed.password`가 기본 센티넬(`__UNSET__`)이면 fail-fast로 부팅을 거부하므로, 운영/최초 기동 시 `CEO_SEED_PASSWORD` 환경변수가 필수다.
- **`jwt.secret`은 web-api·admin-api와 반드시 달라야 한다**(ceo=`JWT_SECRET_CEO`). 동일 시크릿이면 다른 API의 토큰이 점주 인증을 통과하는 권한 상승이 발생한다 — 상세는 `security-module/AGENTS.md`.
- **Redis 키 접두사는 점주 전용으로 분리**: refresh `ceo:rt:`, blacklist `ceo:bl:` (web=`rt:`/`bl:`, admin=`admin:rt:`/`admin:bl:`와 겹치지 않음).

## Dependencies

### Internal
- `core-module`, `infrastructure-module`(runtimeOnly), `external-api`, `logging-module`, `security-module`

### External
- Spring Web, Spring Security, springdoc-openapi 2.3.0, jjwt 0.13.0, Spring Data Redis, Lombok

<!-- MANUAL: -->
