<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-06-02 -->

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
- **presentation 레이어만 담당**: 컨트롤러는 `core-module`의 application 서비스만 호출한다. JPA Repository를 직접 주입하지 않는다.
- 도메인별 폴더(`member/`, `order/`, `shop/` …) 안에 `request/`·`response/` DTO를 둔다.
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
