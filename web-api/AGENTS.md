<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-05 -->

# web-api

## Purpose
최종 사용자용 REST API 애플리케이션 (실행 가능한 Spring Boot bootJar). 도메인별 컨트롤러와 Request/Response DTO를 제공하며, JWT 기반 인증·소셜 로그인(OAuth)·Spring Security·Redis 캐시·요청 제한(rate limit)·스케줄러·로깅을 담당한다. 비즈니스 로직은 직접 구현하지 않고 `core-module`의 application(Command/Query) 서비스만 호출한다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | web, webflux, security, data-redis, aop, validation, JJWT, springdoc, p6spy 의존 |
| `src/main/resources/` | `application*.yml` 등 환경 설정 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/webapi/` | 컨트롤러·인증·설정 등 프레젠테이션 코드 루트 (see `src/main/java/com/tastyhouse/webapi/AGENTS.md`) |
| `src/test/` | 컨트롤러/통합 테스트 (`spring-security-test`) |

## For AI Agents

### Working In This Directory
- **presentation 레이어만 담당**: 컨트롤러는 core를 직접 호출하지 않고 도메인별 Facade(`{도메인}Service`)를 통해서만 호출한다. JPA Repository를 직접 주입하지 않는다.
  - web-api도 admin-api와 동일하게 도메인별 Facade를 통해 core를 호출한다(reference: `admin-api/notice/NoticeService`, `order/OrderService`). Facade가 core 서비스 호출과 core DTO↔Request/Response 변환을 전담하며, 컨트롤러는 `com.tastyhouse.core.*`를 import하지 않는다.
- 도메인별 폴더(`member/`, `order/`, `shop/` …) 안에 `request/`·`response/` DTO를 둔다.
- **DTO 조립 시 `new` 직접 호출 지양**: 컨트롤러에서 command/condition/response를 `new`로 조립하지 않고, 대상 record 자신의 정적 팩토리 `of(...)`/`from(...)`로 위임한다. Request DTO에는 `toCommand()` 같은 변환 메서드를 두지 않는다 — 단순 매핑은 컨트롤러가 Request를 원시 필드로 언패킹해 `Command.of(...)`를 직접 호출하고, 복잡한 중첩 요청은 private 변환 헬퍼로 분리할 수 있다(reference: `order/OrderApiController#toCreateOrderCommand`). 상세는 루트 [CLAUDE.md](../CLAUDE.md#dto-조립-규칙-new-직접-호출-지양) 참고.
- 401 처리는 web-api의 `UnauthorizedException`을, 비즈니스 예외는 `core-module`의 `BusinessException` 계열을 사용 — 전역 처리는 `exception/GlobalExceptionHandler`.

### Testing Requirements
- `@WebMvcTest` / `@SpringBootTest` + `spring-security-test`로 인증 흐름 검증.

### Common Patterns
- JWT 발급/검증은 `config/jwt/`, 보안 설정은 `config/security/`.
- 소셜 로그인은 `auth/{kakao,naver,apple,facebook}` — 실제 외부 호출은 `external-api`에 위임.
- 응답은 공통 래퍼(`common/`)로 일관화.

## Dependencies

### Internal
- `core-module` — application 서비스, 도메인
- `external-api` — OAuth/결제/메시징/파일 어댑터

### External
- Spring Web/WebFlux/Security, Redis, AOP, Validation
- JJWT 0.12.3, springdoc-openapi 2.3.0, p6spy

<!-- MANUAL: -->
