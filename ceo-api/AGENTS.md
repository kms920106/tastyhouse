<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-07-25 -->

# ceo-api

## Purpose
점주(매장 오너)용 REST API 애플리케이션 (실행 가능한 Spring Boot bootJar). `web-api`(일반 회원)·`admin-api`(관리자)와 대칭인 3번째 프레젠테이션 모듈로, 매장 사장님이 자기 매장·주문·예약·리뷰 등을 관리하는 셀프 서비스 API를 제공할 예정이다. `core-module`의 동일한 application 서비스를 재사용한다.

**현재 상태: 뼈대(skeleton)** — 모듈 골격 + JWT 인증 인프라 + 공통(common/exception) 요소만 존재하며, 도메인 엔드포인트(auth·shop·order 등)와 점주 계정 도메인(core-module)은 아직 없다. 인증 필터 체인은 admin-api 패턴대로 완비되어 있으나, 점주 계정 도메인에 의존하는 `UserDetailsService`·로그인 서비스는 도메인 신설 후 추가한다.

컨트롤러가 `core-module`에 직접 결합되는 것을 막기 위해, 신규 도메인은 admin-api와 동일하게 도메인별 ceo-api 소속 `{도메인}Service`를 두어 컨트롤러와 core 사이를 중개하는 패턴을 따른다(컨트롤러는 `com.tastyhouse.core.*`를 import하지 않고 ceo-api 타입에만 의존).

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | web + springdoc 의존, `core-module`·`infrastructure-module`·`external-api`·`logging-module`·`security-module` 참조 (admin-api와 동일 구성) |
| `src/main/resources/application.yml` | 점주 앱 환경 설정 (포트 `8100`, CORS 기본 `http://localhost:3020`, `jwt.secret=${JWT_SECRET_CEO}`) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/ceoapi/` | 점주 컨트롤러 루트 — `common/`(공용 인프라: `ApiResponse`·`PageRequest`·`PaginationResponse`), `config/`(JWT·Security), `exception/`(`GlobalExceptionHandler`). 도메인 폴더는 신규 기능 추가 시 admin-api 컨벤션(`컨트롤러 + {도메인}Service + request/·response/`)대로 생성 |
| `src/test/` | 점주 API 테스트 (`contextLoads`) |

## For AI Agents

### Working In This Directory
- 새 기능 추가 시 admin-api와 동일한 도메인-폴더 + `request/`·`response/` 컨벤션, `{도메인}Service` 중개 계층, DTO 조립·`@ModelAttribute` 조회·`@PathVariable id` 통일·`@Schema` 문서화 규칙을 그대로 따른다. 상세·근거·예시는 루트 CLAUDE.md 및 `admin-api/AGENTS.md` 참고.
- **import 순서 — presentation 내부 서브정렬**: 자사 import의 presentation 계층(`com.tastyhouse.ceoapi.*`) 안에서 공용 인프라(`common`·`config`)를 도메인 전용(`<도메인>.request`·`.response`)보다 위에 둔다. 상세는 루트 CLAUDE.md 참고.
- 비즈니스 로직은 `core-module` application 서비스에 위임한다 — `{도메인}Service`는 위임·변환 계층일 뿐이다. web-api/admin-api와 core 서비스를 공유하므로 command/query 시그니처 변경 시 동시 수정 주의.

### Testing Requirements
- `@SpringBootTest` 기반 컨텍스트 로드/컨트롤러 검증.

### Common Patterns
- **JWT 인증 메커니즘은 `security-module`의 `com.tastyhouse.security.jwt`에 공유**된다. ceo-api의 `config/jwt/JwtTokenProvider`는 그 공용 provider를 상속해 `ceoId` 클레임·`CustomUserDetails` 재구성만 주입한다(검증 토큰 없음). 공용 필터는 `config/jwt/JwtConfig`가 점주 전용 블랙리스트 저장소(`ceo:bl:`)로 빈 등록하고, refresh 저장소는 `RedisRepositoryConfig`가 `ceo:rt:` 접두사로 등록한다. 정책은 ceo-api에 잔류: `config/security/SecurityConfig`·`PublicPaths`·`CustomUserDetails`(`JwtPrincipal` 구현).
- **인가 체인은 현재 `.anyRequest().authenticated()`** — 점주 전용 역할 도메인(예: `ROLE_CEO`)이 확정되면 admin-api처럼 `.hasRole("CEO")`로 강화한다(심층 방어).
- **`jwt.secret`은 web-api·admin-api와 반드시 달라야 한다**(ceo=`JWT_SECRET_CEO`). 동일 시크릿이면 다른 API의 토큰이 점주 인증을 통과하는 권한 상승이 발생한다 — 상세는 `security-module/AGENTS.md`.
- **Redis 키 접두사는 점주 전용으로 분리**: refresh `ceo:rt:`, blacklist `ceo:bl:` (web=`rt:`/`bl:`, admin=`admin:rt:`/`admin:bl:`와 겹치지 않음).

## Dependencies

### Internal
- `core-module`, `infrastructure-module`(runtimeOnly), `external-api`, `logging-module`, `security-module`

### External
- Spring Web, Spring Security, springdoc-openapi 2.3.0, jjwt 0.13.0, Spring Data Redis, Lombok

<!-- MANUAL: -->
