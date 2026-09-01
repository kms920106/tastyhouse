# web-application

사용자 웹(web-api)의 **application 계층**을 소유하는 모듈. 컨텍스트별 인바운드 포트(`<ctx>/port/in/`), 그 구현인 `*CommandService`/`*QueryService`, 그리고 QueryService가 조립하는 표현 계약(`<ctx>/response/`)이 여기 있다.

컨트롤러(`<ctx>/adapter/in/web/`)·`request/`·config·security 정책·전역 예외 핸들러와 부트스트랩(`WebApiApplication`)은 `web-api`에 남아 있다(`web-api/AGENTS.md`).

## 신설 배경 (챕터 02)

`web-api` 하나가 인바운드 어댑터와 application 계층을 함께 담고 있었다(main 394파일). 두 계층이 같은 모듈 안 다른 패키지였을 뿐이라 경계가 **규율로만** 유지됐고, ArchUnit이 사후에 잡을 수는 있어도 "서비스가 서블릿·컨트롤러로 새는 것"을 컴파일 단계에서 막지는 못했다. 모듈을 나누면 그 경계가 빌드 그래프가 된다.

챕터 01(`batch-application`)에서 확정한 레시피(모듈 신설 → `git mv` → 패키지 리네임 → config 배선 → ArchUnit 분할)를 그대로 적용했다.

## 패키지 구조

```
com.tastyhouse.webapplication/
├── WebApplicationConfig.java     @ComponentScan 진입점 — 쓰는 앱이 @Import 한다
├── <ctx>/port/in/                UseCase 인터페이스 + Command record
├── <ctx>/service/                *CommandService/*QueryService implements {Ctx}UseCase
├── <ctx>/response/               QueryService가 조립하는 표현 계약 record
└── auth/                         인증 컨텍스트 (아래 "auth가 왜 여기까지 왔나" 참고)
    ├── token/                    JwtTokenProvider · TokenService
    └── security/                 CustomUserDetails · CustomUserDetailsService
```

컨텍스트 27종: `auth` · `banner` · `bug` · `coupon` · `event` · `faq` · `follow` · `grade` · `mail` · `member` · `menureview` · `notice` · `notification` · `order` · `partnership` · `payment` · `point` · `policy` · `product` · `rank` · `referral` · `reservation` · `review` · `search` · `shop` · `sms`(+`file`은 컨트롤러만 있어 이 모듈에 없음).

## 왜 `response/`가 함께 왔나

이 저장소의 확정 규칙상 **`{도메인}QueryService`가 Result → Response 변환을 담당**한다(루트 `backend/CLAUDE.md`의 DTO 조립 규칙). 따라서 `response/`를 web-api에 남기면 서비스가 `com.tastyhouse.webapi..`를 역참조하게 되어 `applicationMustNotDependOnAdapters`가 곧바로 위반되고, 모듈 분리 자체가 성립하지 않는다. `batch-application`이 `crawling/bbq/response/`를 함께 옮긴 것과 같은 판단이다.

**`request/`는 반대로 web-api에 남는다** — Request → Command 매핑은 인바운드 어댑터의 책임이고(완전 매핑 전략), 컨트롤러가 `request.toCommand(...)`로 조립해 넘긴다. 그 방향(web-api → web-application)은 정상 의존이다.

## auth가 왜 여기까지 왔나 (챕터 02 서블릿 결합 판단 기록)

챕터 스펙은 auth를 "완전 레거시 + 서블릿 결합"으로 분류하고 **마지막 컨텍스트**로 처리하도록 했다. 실제로 확인한 결과 결합의 실체는 서블릿이 아니라 **Spring Security core**였다.

- auth 컨텍스트 전체에 `jakarta.servlet`·`org.springframework.web` import가 **0건**이었다. 컨트롤러가 이미 원시값(Bearer 토큰 문자열·인가 코드)만 넘기고 있었다.
- 실제 blocker는 `webapi.config.jwt`의 `JwtTokenProvider`·`TokenService`와 `webapi.config.security`의 `CustomUserDetails`·`CustomUserDetailsService`였다. 이 넷은 `AuthenticationManager`·`SecurityContextHolder`·`UserDetails`·JWT만 쓰는 **서블릿-프리** 타입이라, `security-module` 의존(스펙이 이 모듈에 추가하도록 지시한 바로 그 의존)만으로 함께 이동할 수 있다.
- **서블릿 결합 타입은 web-api에 남았다** — `JwtConfig`(필터 빈 등록)·`SecurityConfig`(필터체인)·`PublicPaths`. `applicationMustBeServletFree`가 이 경계를 강제한다.

`AuthService`(파사드)는 `AuthCommandService`로 개명하며 `AuthCommandUseCase`를 구현하게 했다. 파라미터 13~12개짜리 `signUp`/`socialSignUp`은 `AuthSignUpCommand`/`AuthSocialSignUpCommand`로 묶었다(같은 타입 인자가 줄줄이 늘어서 순서 착각이 조용한 버그가 되는 형태였다).

## Dependencies

### Internal
- `domain-module` (implementation) — 도메인 모델·VO·write 포트·도메인 서비스
- `security-module` (implementation) — `JwtProperties`·Redis 토큰 저장소. **auth/token이 쓰는 서블릿-프리 타입 한정**이며, Spring Security core는 이 모듈이 `api`로 노출하는 starter를 타고 들어온다
- `external-api` (implementation) — 소셜 로그인 SPI(`external.oauth.spi`)
- `api-common-module` (implementation) — `PaginationResponse<T>` 등 표현 계약

### External
- `spring-boot-starter-web` (implementation) — **`MultipartFile`(업로드 경계 파라미터) 때문에 필요**하다. 서블릿 결합은 `applicationMustBeServletFree`가 막으므로 이 의존이 계층을 무너뜨리지 않는다
- `spring-tx` (implementation) — `@Transactional`만을 위한 최소 의존 (batch-application 선례)

### infrastructure 의존 없음 — 이 모듈의 핵심

application 계층이 infra를 모른다는 규칙을 ArchUnit이 아니라 **빌드 그래프가 1차로 강제**한다. `import com.tastyhouse.infrastructure...` 한 줄이 실제 컴파일 에러가 된다. ArchUnit `shouldNotDependOnInfrastructure`는 누군가 build.gradle에 의존을 되돌리는 회귀를 막는 2차 방어선이다.

## ArchUnit (`architecture/LayerRulesTest` 12종)

web-api에서 **이동한 규칙 10종**(CQRS 교차 주입 2 · UseCase 구현 강제 2 · Command 경계 타입 3 · request 금지 1 · QueryDSL/infra 차단 2)과, **물리 분리로 비로소 표현 가능해진 신설 규칙 2종**:

| 신설 규칙 | 내용 |
|---|---|
| `applicationMustBeServletFree` | ✗ `jakarta.servlet..` · `org.springframework.web..` (`MultipartFile`만 예외) |
| `applicationMustNotDependOnAdapters` | ✗ `com.tastyhouse.webapi..` (역참조 금지) |

web-api에 함께 있을 때는 컨트롤러가 정당하게 서블릿 타입을 쓰므로 모듈 전역 금지를 걸 수 없었다.

`RuleAnchorTest`가 각 규칙의 anchor 개수(`*CommandService` 17 · `*QueryService` 29 · `..port.in..` 99 · 전체 297)를 **하한으로** 검사해, 클래스가 대량 소실되면 빌드가 실패하게 한다. 하한으로 두는 이유는 컨텍스트가 늘어나는 것이 정상이기 때문이다 — 정확히 일치를 요구하면 기능 추가마다 이 파일을 고쳐야 해 anchor가 규칙이 아니라 잡음이 된다(batch-application은 규모가 작아 일치를 썼다).

## 빈 배선

`WebApplicationConfig`(`@ComponentScan("com.tastyhouse.webapplication")`)를 `WebApiApplication`의 `@Import`에 추가한다. `scanBasePackages` 문자열 나열이 아니라 타입 세이프 조합을 쓰는 것이 이 저장소의 표준 구성이다(`InfrastructureModuleConfig`·`BatchApplicationConfig` 선례).

- **읽기 계약을 이 모듈이 소유한다 (챕터 09 — `application-common-module` 해체 완료)**: 이 앱만 소비하는 `{Ctx}QueryPort`·`*Result`·`*SearchCondition`은 `src/main/java/com/tastyhouse/application/<ctx>/port/out/`에 있다. 패키지가 모듈명과 어긋나는 것은 의도된 선택이며(`infrastructure:persistence`가 `com.tastyhouse.infrastructure..`를 쓰는 것과 같은 선례), 그 덕분에 계약이 어느 모듈로 가든 소비 측 import와 ArchUnit 패키지 규칙이 바뀌지 않는다.
  - **2개 이상의 앱이 쓰는 공유 계약은 여기 두지 않고 `domain-module`이 소유한다** — 다른 앱이 이 모듈을 의존하게 되는 앱 간 수평 의존을 막기 위해서다. 새 계약을 추가하기 전에 소비 앱이 몇 개인지 먼저 센다.
  - **프레임워크-프리를 `LayerRulesTest#readContractsShouldBeFrameworkFree`가 지킨다**: 이 모듈은 spring starter를 받으므로 `application-common-module` 시절의 컴파일 게이트가 없다. 계약이 참조해도 되는 것은 `java..`·`com.tastyhouse.domain..`과 자기 자신뿐이다.
