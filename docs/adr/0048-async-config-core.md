# ADR-0048: AsyncConfig(@EnableAsync)를 앱이 아니라 코어 application이 소유한다

- 상태: 채택
- 결정 커밋: `456db1b13` (2026-10-09) — 커밋 메시지 1번째 줄 `fix(async)`
- 대체한 결정: —

## 맥락

- **`@Async`**: 메서드를 별도 스레드에서 실행하게 하는 Spring 애노테이션입니다. `@EnableAsync`가 어딘가에 선언돼 있어야 동작하고, 없으면 **에러 없이 조용히 동기**(호출한 스레드에서 그대로) 실행됩니다.
- **코어 `application`**(현 `application-core` 모듈): 4개 앱(web-api, admin-api, ceo-api, batch-module)이 모두 의존하는 공통 application 모듈입니다.

`@EnableAsync`는 web-api의 `AsyncConfig`에만 있었습니다. 그런데 `@Async` 리스너 3개(`notification/listener/ReviewBlindApprovedEventListener`·`ReviewOwnerReplyEventListener`, `product/listener/ProductMenuReviewEventListener`)는 코어에 있어 4앱이 모두 로드했습니다. 그 결과 ceo(`ReviewOwnerReplyCreatedEvent` 발행)와 admin(`ReviewBlindApprovedEvent` 발행)에서는 알림 리스너가 요청 스레드에서 동기로 돌았습니다. 알림이 실패하면 DB는 반영됐는데 API는 실패로 응답하는 상태가 됩니다(`docs/tasks/async-config-core/backend.md`).

## 이전 규칙

### backend/CLAUDE.md — 컴포지션 루트 규칙 > 앱이 가질 수 있는 조립 코드의 상한

허용되는 앱 `@Configuration` 중 **정책**은 "`SecurityConfig`·`PublicPaths`(web·admin·ceo 각자)와 `OpenApiConfig`·`AsyncConfig`(web만). 앱마다 값이 실제로 다른 결정이라 공유할 수 없다"였습니다.

### backend/CLAUDE.md — 접근 제어자 규칙

앱 설정 표에 `AsyncConfig`가 `{web,admin,ceo}-api`의 앱 설정 중 하나로 들어 있었습니다(설정 10 + 예외 핸들러 1 = 11).

## 결정

`AsyncConfig`를 web-api에서 코어 `application/shared/config/AsyncConfig`로 옮깁니다.

| 항목 | before | after |
|---|---|---|
| `@EnableAsync` 위치 | `backend/web-api/.../webapi/config/AsyncConfig.java` | 코어 `.../application/shared/config/AsyncConfig.java`(package-private `@Configuration`, `SharedBeanConfig`과 같은 위치) |
| web-api `AsyncConfig` | 있음 | 삭제 |
| web 알림 리스너 | 비동기 | 비동기 |
| ceo·admin 알림 리스너 | 동기(요청 스레드) | 비동기 |
| 실행기 | Boot 기본 `applicationTaskExecutor` | 동일 |
| 가드 | 없음 | 코어 `AsyncRulesTest`(@Async 클래스 존재·코어 `@EnableAsync` 1개·위치 `shared.config`) + 각 앱 `LayerRulesTest.appModuleShouldNotEnableAsync` |

## 근거

- `AsyncConfig`는 값이 앱마다 다른 결정이 아닙니다. `@Async` 리스너 3개가 코어에 있어 4앱 공통입니다.
- web에만 두면 ceo·admin이 발행한 이벤트의 리스너가 조용히 동기로 도는 결함이 생깁니다.
- 앱 모듈 규칙을 코어가 아니라 각 앱 테스트에 둔 이유: 코어 테스트 모듈은 api 모듈 클래스를 클래스패스에 두지 않아 볼 수 없습니다.

## 결과·감수한 손실

- **의도된 동작 변경(결함 수정)**: ceo·admin 알림 리스너가 비동기로 바뀌었습니다. web과 batch는 영향이 없습니다(batch는 이벤트를 발행하지 않음). API 요청·응답·에러코드·DB 스키마는 불변입니다.
- `@EnableAsync`가 앱 모듈에 다시 생기면 `appModuleShouldNotEnableAsync`가 실패합니다.

## 영향받은 문서

- `backend/CLAUDE.md` · 컴포지션 루트 규칙 > 앱이 가질 수 있는 조립 코드의 상한 · 정책 `@Configuration`에서 `AsyncConfig` 제외, 코어 소유
- `backend/CLAUDE.md` · 접근 제어자 규칙 (내부 구현은 package-private) · 앱 설정 표의 개수
- `backend/application-core/AGENTS.md` · "리스너를 4앱 전부가 스캔하는 코어 `application-core`에 두는 이유 (개별 사유)" / "알림 리스너의 `@Async` + `AFTER_COMMIT` + `REQUIRES_NEW` 3종 세트를 부분적으로 떼지 않는다" · `@EnableAsync`는 코어 소유
- `backend/{web,admin,ceo}-api/AGENTS.md` · "Subdirectories" / "Common Patterns" · 앱 `config/`에 `AsyncConfig`가 없다(앱 설정 서술에서 제거)
