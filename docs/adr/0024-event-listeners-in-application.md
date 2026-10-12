# ADR-0024: 도메인 이벤트 리스너는 persistence가 아니라 application에 둔다

- 상태: 채택(`@SharedApp` 마커 부분은 [ADR-0036](0036-app-marker-removal.md)으로 대체)
- 결정 커밋: `59285a4e7` (2026-09-27)
- 대체한 결정: —

## 맥락

**도메인 이벤트 리스너**는 domain이 `DomainEventPublisher` 포트로 발행한 이벤트(가입·결제·리뷰 답글 등)를 구독해 후속 처리를 하는 클래스입니다. 포인트 적립·환급, 알림 적재 같은 일을 하며, 전부 `@TransactionalEventListener(phase = AFTER_COMMIT)`입니다.

리스너 12종은 영속화 어댑터 모듈 `infrastructure:persistence`의 `com.tastyhouse.infrastructure.<ctx>.listener`에 있었습니다(`1f3945b41`, 2026-08-15). 그 모듈은 모든 실행 앱이 스캔했으므로, 어느 앱이 이벤트를 발행해도 리스너가 떴습니다. 하지만 이벤트를 받아 도메인 서비스를 조합(오케스트레이션)하는 일은 DB 어댑터의 일이 아닙니다. 또 `ReviewOwnerReplyEventListener`는 같은 모듈의 `ShopQueryDao`를 구체 타입으로 직접 주입하고 있었습니다.

## 이전 규칙

### backend/infrastructure/persistence/AGENTS.md(현 `backend/infrastructure/jpa/AGENTS.md`) — 규칙 / 패키지 구조

이벤트 리스너는 이 모듈의 `<ctx>/listener/`에 둔다. 특정 api 모듈에 두면 다른 모듈이 같은 이벤트를 트리거할 때 리스너가 없어 누락되므로, 크로스커팅 리스너는 모든 실행 모듈이 스캔하는 이 모듈에 둔다. `<ctx>/listener/`를 가진 도메인은 `coupon`·`file`·`mail`·`member`·`payment`·`point`·`policy`·`product`·`sms`였다. 같은 모듈 안에서는 포트에 없는 public 조회도 쓸 수 있었다(`ShopQueryDao#findShopName`을 `ReviewOwnerReplyEventListener`가 구체 타입으로 주입).

### backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md — Common Patterns

DomainEvent는 "발행은 포트 경유, 리스너는 infra"였다. 리스너를 api 모듈에 두면 다른 모듈이 같은 이벤트를 트리거할 때 누락되므로 infrastructure 모듈에 두었다.

### backend/AGENTS.md — Common Patterns

계층 배치 요약에서 영속화 어댑터 모듈의 하위 패키지를 `<ctx>/{persistence,query,listener}`로 적어, 리스너를 persistence(현 `infrastructure:jpa`) 쪽 계층으로 분류했다. 이 결정 이후 리스너는 코어 application의 `<ctx>/listener/`로 옮겨졌고, 앱 마커 제거 전에는 `@Component`에 `@SharedApp`도 달았다.

## 결정

리스너 12종을 `application`의 `com.tastyhouse.application.<ctx>.listener`로 옮깁니다. 당시에는 4앱 공통 스캔을 위해 리스너 전용 마커 `@SharedApp`을 신설해 앱별 `{App}ApplicationConfig` 스캔 필터에 추가했습니다.

| 항목 | before | after(이 결정) | 지금([ADR-0036](0036-app-marker-removal.md) 이후) |
|---|---|---|---|
| 리스너 위치 | `infrastructure:persistence`의 `com.tastyhouse.infrastructure.<ctx>.listener` | `application`의 `com.tastyhouse.application.<ctx>.listener` | 코어 `application-core`의 같은 패키지 |
| 4앱에 뜨게 하는 수단 | persistence 모듈 스캔 | `@Component` + `@SharedApp` 마커 스캔 | `@Component`만. 코어가 4앱 전부의 클래스패스에 있음 |
| 리스너의 조회 | `ShopQueryDao` 구체 타입 주입 | 읽기 포트 `ShopBasicInfoQueryPort#findShopName` | 같음 |
| 가드 | — | `listenersShouldBeShared`(마커 누락 차단)·`sharedAppOnlyOnListeners`(`@SharedApp`은 `@TransactionalEventListener` 보유 클래스에만) | `LayerRulesTest#listenersAndConfigsShouldResideInCore` |

## 근거

- 포인트 적립·환급·알림 적재 같은 이벤트 오케스트레이션은 **유스케이스 계층의 일**입니다. 영속화 어댑터에 둘 이유가 없습니다.
- 특정 앱에만 뜨게 두면 다른 앱이 같은 이벤트를 발행할 때 후속 처리가 조용히 누락됩니다. 그래서 4앱 전부가 스캔하는 위치여야 합니다.
- 마커를 빠뜨리면 리스너가 어느 앱에도 뜨지 않아 이벤트가 **예외도 로그도 없이** 유실됩니다. 그래서 누락과 남용을 양방향으로 막는 ArchUnit 규칙을 함께 추가했습니다.
- application은 infra를 컴파일 클래스패스에 두지 않습니다. 그래서 리스너가 DAO를 직접 주입하지 않고 `port/out` 읽기 포트로 받습니다.

## 결과·감수한 손실

- 동작 변경은 없습니다. 테스트가 없던 `ReviewBlindApprovedEventListener`에 테스트를 추가했습니다.
- 이후 [ADR-0027](0027-domain-ports-to-application.md)(덩어리 03a)로 발행 포트·구현(`DomainEventPublisher`·`SpringDomainEventPublisher`)도 application으로 옮겨져, persistence(현 jpa)에는 이벤트 관련 코드가 남지 않았습니다.
- `@SharedApp` 마커는 [ADR-0036](0036-app-marker-removal.md)에서 제거됐습니다. 지금은 리스너를 코어 `application-core`에 두는 것만으로 4앱 전부에 뜹니다.

## 영향받은 문서

- `backend/application-core/AGENTS.md` · "`<ctx>/listener/` — 도메인 이벤트 리스너" · 리스너는 `com.tastyhouse.application.<ctx>.listener`에 `@Component`로 두고 코어에만 두며, DAO 대신 읽기 포트를 받는다
- `backend/application-core/AGENTS.md` · "리스너를 4앱 전부가 스캔하는 코어 `application-core`에 두는 이유 (개별 사유)" · 리스너별 발행 경로 근거
- `backend/infrastructure/jpa/AGENTS.md` · "패키지 구조" / "규칙" · 이 모듈에는 `<ctx>/listener/`도 이벤트 관련 코드도 없다
- `backend/domain/AGENTS.md` · "Common Patterns" · 리스너는 코어 application에 둔다
- `backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md` · "Common Patterns" · DomainEvent 발행 포트와 리스너 위치
