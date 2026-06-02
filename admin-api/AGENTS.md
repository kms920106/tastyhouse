<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-06-02 -->

# admin-api

## Purpose
관리자용 REST API 애플리케이션 (실행 가능한 Spring Boot bootJar). 현재는 최소 구현 상태로 정책(policy) 도메인 위주이며, `core-module`의 동일한 application 서비스를 재사용한다. 사용자 API(web-api)와 application 서비스를 공유하므로 command/query 시그니처 변경 시 동시 수정이 필요하다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | web + springdoc 의존, `core-module`·`external-api` 참조 |
| `src/main/resources/` | 관리자 앱 환경 설정 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/adminapi/` | 관리자 컨트롤러 루트 — `common/`, `policy/`(+`request/`) |
| `src/test/` | 관리자 API 테스트 |

## For AI Agents

### Working In This Directory
- 현재 매우 작음(약 5개 Java 파일). 새 관리 기능 추가 시 web-api와 동일한 도메인-폴더 + `request/`·`response/` 컨벤션을 따른다.
- 비즈니스 로직은 `core-module` application 서비스에 위임 — admin 전용 로직이라도 가능하면 core에 둔다.

### Testing Requirements
- `@SpringBootTest` 기반 컨트롤러 검증.

### Common Patterns
- web-api와 동일한 presentation 규칙. application 서비스 공유로 인한 회귀 주의.

## Dependencies

### Internal
- `core-module`, `external-api`

### External
- Spring Web, springdoc-openapi 2.3.0, Lombok

<!-- MANUAL: -->
