# ADR-0040: 내부 구현 클래스를 package-private으로 좁힌다(벤더 wire DTO는 루트 평면 배치)

- 상태: 채택
- 결정 커밋: `151eadb88` (2026-10-04)
- 대체한 결정: —

## 맥락

- **package-private**: 자바에서 클래스 선언에 `public`을 붙이지 않은 상태입니다. 같은 패키지 안에서만 그 클래스를 이름으로 쓸 수 있고, 다른 패키지에서 import하면 컴파일 에러가 납니다.
- **wire DTO**: 외부 API(카카오·토스 등)와 주고받는 JSON을 그대로 옮긴 요청·응답 record입니다.

결정 전에는 엔티티·JPA 리포지토리·어댑터·UseCase 구현 서비스·리스너·컨트롤러·설정·스케줄러가 모두 `public`이었습니다. 원래는 이유가 있었습니다. 모듈이 자기 `@Configuration`을 `AutoConfiguration.imports`로 등록하고 다른 모듈이 구체 타입을 import해 조립했으며, 협력 서비스가 구체 클래스를 주입받았습니다. 그런데 [ADR-0038](0038-autoconfiguration-imports-removal.md)(문자열 패키지 스캔 조립)과 [ADR-0033](0033-usecase-interface-injection.md)(UseCase 인터페이스 주입)으로 두 이유가 모두 사라졌습니다.

## 이전 규칙

### backend/CLAUDE.md — infrastructure 패키지 규칙 (루트 = 모듈명)

"벤더: 외부 API의 요청·응답 wire DTO는 `{루트}.dto`에 둔다." — [ADR-0037](0037-infrastructure-package-root.md)이 같은 날 정한 하위 패키지 배치입니다.

### 벤더 모듈 AGENTS.md

- kakao·naver·apple·facebook-oauth: 루트 통일 후 wire DTO는 하위 패키지 `com.tastyhouse.infrastructure.{vendor}.oauth.dto`로 모였습니다.
- solapi: `request/`·`response/` 두 하위 패키지를 `dto/` 하나로 합쳐 다른 벤더의 `dto/` 관례와 맞췄습니다(`SolapiMessageRequest`·`SolapiMessageResponse`).
- tosspayments·bbq: `dto/` 구성은 루트 통일 후에도 불변이었습니다(bbq는 `BbqMenuCategoryResponse`·`BbqMenuResponse`·`BbqMenuSubOptionResponse`).
- oauth "벤더 추가·제거 절차": 신설 벤더는 wire DTO를 `.dto` 하위에 두었습니다.

### CLAUDE.md — 리포지토리 구조 (infrastructure 중첩 프로젝트 문단)

"벤더의 외부 API wire DTO는 `{루트}.dto`에 둡니다."

### 클래스 선언의 접근 제어자

명시된 규칙이 없었고, 내부 구현 클래스도 관례적으로 `public`이었습니다.

## 결정

다른 패키지가 이름으로 참조하지 않는 내부 구현 클래스는 최상위 `public`을 붙이지 않습니다. 클래스 선언의 `public`만 지우고, 생성자·메서드는 그대로 둡니다.

| 항목 | before | after |
|---|---|---|
| 엔티티·JPA 리포지토리·어댑터·UseCase 구현 서비스·리스너·컨트롤러·설정·스케줄러 | `public class`/`public interface` | package-private (854개) |
| 벤더 wire DTO 16개 | `{루트}.dto`의 `public record` | `{루트}`로 이동 + package-private |
| 벤더 모듈의 하위 패키지 | `dto/` | 없음 — Client·Adapter·Properties·Config·DTO가 루트에 평면 |
| `public` 유지 대상 | — | 다른 패키지가 참조하는 7개, `Projections.constructor` 대상 record, `BaseEntity`·Embeddable, Request/Response, 공유 도메인 서비스 |
| 가드 | 없음 | 각 모듈 ArchUnit에 가시성 규칙 + 허용 목록 낡음 검사(예: `VendorLayerRulesTest#topLevelClassesShouldNotBePublic`) |

## 근거

- "이건 내부 구현이니 직접 쓰지 말라"는 약속을 문서·리뷰가 아니라 컴파일러가 지키게 하려는 것입니다. 다른 패키지에서 import하는 순간 컴파일이 실패합니다.
- 조립은 문자열 패키지 스캔이고 스캔은 접근 제어자를 보지 않습니다. 주입은 포트 인터페이스로만 합니다. Spring 스캔·CGLIB 프록시·Spring Data 리포지토리·`@ConfigurationProperties`·Hibernate·Jackson 모두 package-private 클래스로 동작합니다.
- package-private 타입은 같은 패키지에서만 보입니다. 그래서 wire DTO는 그것을 쓰는 `*Client`와 같은 패키지에 있어야 하고, `dto/` 하위 패키지에 둘 수 없습니다.

## 결과·감수한 손실

- 동작은 바뀌지 않았습니다. 4앱 싱글턴 빈 이름 집합과 api-docs 경로 수가 변경 전과 같았습니다.
- "wire DTO는 `{루트}.dto`" 규칙은 [ADR-0037](0037-infrastructure-package-root.md) 커밋 직후 같은 날 뒤집혔습니다.
- UseCase를 구현하지 않는 협력 서비스(약 48개는 다른 패키지에서 import)는 이 결정에서 후속 과제로 남았고, [ADR-0054](0054-collaborator-service-package-private.md)가 마무리했습니다.
- wire DTO를 옮겨도 클래스명은 그대로라 소셜 벤더 클라이언트의 빈 이름(`kakaoOAuthClient` 등, 소비 측 `@Qualifier`)은 불변입니다. solapi의 두 record는 `SolapiSmsClient`와, tosspayments의 wire DTO는 `TossPaymentClient`와 같은 패키지의 package-private 타입이 됐습니다.
- logging-module은 이 가드 때문에 archunit 테스트 의존이 추가됐습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · 접근 제어자 규칙 (내부 구현은 package-private) · 대상·근거·854개 변경 전후 표
- `backend/CLAUDE.md` · infrastructure 패키지 규칙 (루트 = 모듈명) · 벤더 wire DTO는 `{루트}`에 평면으로 두고 package-private
- `CLAUDE.md` · 리포지토리 구조 · 벤더 wire DTO는 루트 평면·package-private
- `backend/infrastructure/{kakao,naver,apple,facebook}-oauth/AGENTS.md` · "무엇을 소유하는가" / "최상위 클래스에 `public`을 붙이지 않는다 (package-private 적용)" · wire DTO는 루트에 평면, package-private
- `backend/infrastructure/{solapi,tosspayments,bbq}/AGENTS.md` · "최상위 클래스에 `public`을 붙이지 않는다 (package-private 적용)" · 같은 취지
- `backend/infrastructure/oauth/AGENTS.md` · 벤더 추가·제거 절차 · 신설 벤더도 루트 평면 + `topLevelClassesShouldNotBePublic` 복사
- `backend/application-core/AGENTS.md` · UseCase 구현 서비스·리스너·설정에 `public`을 붙이지 않는다 (package-private 적용, 5모듈 공통 정본)
