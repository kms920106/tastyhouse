<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-05 -->

# admin-api

## Purpose
관리자용 REST API 애플리케이션 (실행 가능한 Spring Boot bootJar). 현재는 최소 구현 상태로 정책(policy)·공지(notice) 도메인 위주이며, `core-module`의 동일한 application 서비스를 재사용한다. 사용자 API(web-api)와 application 서비스를 공유하므로 command/query 시그니처 변경 시 동시 수정이 필요하다.

컨트롤러가 `core-module`에 직접 결합되는 것을 막기 위해, 도메인별로 admin-api 소속 **Facade(`{도메인}Service`)** 를 두어 컨트롤러와 core 사이를 중개하는 패턴을 도입한다(reference 구현: `notice/NoticeService`). Facade가 core application 서비스 호출과 core DTO↔admin-api Request/Response 변환을 전담하므로, 컨트롤러는 `com.tastyhouse.core.*`를 import하지 않고 admin-api 타입(Facade·Request·Response)에만 의존한다. 신규 도메인은 이 패턴을 따르고, 기존 컨트롤러(policy 등)도 수정 시 점진적으로 전환한다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | web + springdoc 의존, `core-module`·`external-api` 참조 |
| `src/main/resources/` | 관리자 앱 환경 설정 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/adminapi/` | 관리자 컨트롤러 루트 — `common/`, `policy/`(+`request/`), `notice/`(+`request/`·`response/`, Facade `NoticeService`) |
| `src/test/` | 관리자 API 테스트 |

## For AI Agents

### Working In This Directory
- 새 관리 기능 추가 시 web-api와 동일한 도메인-폴더 + `request/`·`response/` 컨벤션을 따른다.
- 도메인별로 admin-api 소속 Facade(`{도메인}Service`)를 두고, 컨트롤러는 Facade만 주입한다 — 컨트롤러에서 `com.tastyhouse.core.*`(core service/DTO/command)를 직접 import하지 않는다. core 호출과 DTO↔Request/Response 변환은 Facade가 전담한다.
- **도메인 enum도 컨트롤러/Request에 core 타입으로 노출하지 않는다** — HTTP 경계는 `String`(다중값 `List<String>`)으로 받고, Facade에서 core enum의 `Enum.from(String)` 정적 팩토리로 승격한다(ID를 `Long`으로 받아 `XxxId.of()`로 승격하는 것과 대칭). String 파라미터에는 `@Schema(allowableValues={...})`/`@Parameter(...)`로 Swagger 후보값을 명시하고, 변환 실패는 core enum `from()` 내부에서 `BusinessException(ErrorCode.XXX_TYPE_UNKNOWN)`으로 처리한다(reference: `banner/BannerService`, `BannerType.from`). 상세는 루트 [CLAUDE.md](../CLAUDE.md#도메인-enum-경계-규칙) 참고.
- 비즈니스 로직은 `core-module` application 서비스에 위임 — admin 전용 로직이라도 가능하면 core에 둔다. Facade는 위임·변환 계층일 뿐 비즈니스 로직을 담지 않는다.
- core의 `PageResult`도 컨트롤러에 노출하지 않는다 — Facade가 admin-api 페이지 응답 타입(예: `notice/response/NoticePageResponse`)으로 변환해 반환한다.
- **DTO 조립 시 `new` 직접 호출 지양**: Facade에서도 command/condition/response를 `new`로 조립하지 않는다. Facade는 Request 타입이 아니라 개별 원시 파라미터(예: `String title, String content, boolean visible`)를 받고, 내부에서 대상 record의 정적 팩토리 `of(...)`/`from(...)`로 위임한다(reference: `NoticeService#createNotice(String, String, boolean)` → `CreateNoticeCommand.of(...)`, `NoticeSearchCondition.of(...)`, `NoticePageResponse.from(...)`). Request DTO에는 `toCommand()` 같은 변환 메서드를 두지 않는다 — 컨트롤러가 Request를 원시 필드로 언패킹해 Facade에 전달한다. 상세는 루트 [CLAUDE.md](../CLAUDE.md#dto-조립-규칙-new-직접-호출-지양) 참고.
- **`record`는 별도 파일로 분리**: Facade·컨트롤러 본문 안에 응답·결과 record를 중첩 선언하지 않고 도메인 폴더의 `response/`에 `public record`로 둔다(reference: `notice/response/NoticePageResponse`). 상세는 루트 [CLAUDE.md](../CLAUDE.md#record-파일-분리-규칙-중첩-record-선언-지양) 참고.

### Testing Requirements
- `@SpringBootTest` 기반 컨트롤러 검증.

### Common Patterns
- 컨트롤러 → Facade(`{도메인}Service`) → `core-module` application 서비스의 2-hop 구조. 컨트롤러는 HTTP 매핑만, Facade는 위임·변환만 담당한다.
- application 서비스 공유(web-api와 동일 core 서비스)로 인한 회귀 주의 — command/query 시그니처 변경 시 web-api와 동시 수정.

## Dependencies

### Internal
- `core-module`, `external-api`

### External
- Spring Web, springdoc-openapi 2.3.0, Lombok

<!-- MANUAL: -->
