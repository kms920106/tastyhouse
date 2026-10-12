# ADR-0027: 나머지 포트·포트 주입 도메인 서비스·이벤트 발행기를 application으로 옮기고 등록은 <Ctx>ServiceConfig가 한다(덩어리 03a)

- 상태: 대체됨(→ [ADR-0034](0034-service-config-removal.md))
- 결정 커밋: `ef957e488` (2026-09-27)
- 대체한 결정: —

## 맥락

- **write 포트**: 도메인 모델을 저장·조회하는 Repository 인터페이스(지금 이름은 `XxxLoadPort`·`XxxSavePort`).
- **포트 주입 도메인 서비스**: 생성자로 포트를 받아 도메인 불변식을 오케스트레이션하는 서비스.
- **`<Ctx>DomainConfig`**: 도메인 서비스를 `@Bean`으로 등록하던 `infrastructure:persistence`의 설정 클래스.

엄격 레이어드 전환(infrastructure는 application만 본다)의 세 번째 단계다. [ADR-0026](0026-vendor-ports-to-application.md)이 파일·메일·SMS·결제 4개 컨텍스트를 옮긴 뒤에도, 나머지 write 포트·출력 포트·포트 주입 서비스·이벤트 발행 포트는 domain에 있었고 그 서비스의 빈 등록은 persistence가 맡고 있었다.

## 이전 규칙

### backend/domain/AGENTS.md — Purpose / Working In This Directory

- domain이 Repository write 포트(`<ctx>/repository/`), 외부 어댑터용 출력 포트(`<ctx>/port/`), 그 포트를 주입받는 도메인 서비스(`<ctx>/service/`의 오케스트레이션), 이벤트 발행 포트 `shared/event/DomainEventPublisher`, `shared/exception/OptimisticLockConflictException`을 소유했다.
- "출력 포트는 `<ctx>/port/`에 둔다 — 외부 시스템을 도메인이 인터페이스로 선언하고 외부 연동 모듈이 기술별로 구현한다." 남아 있던 출력 포트는 7종(`ProductReviewStatisticsPort`·`StorePriceVerificationPort`·`ShopRequestIndexSyncPort`·`MemberReviewCountPort`·`KeywordCountPort`·`ReplyPhraseTextValidatorPort` 등)이었다. `DomainEventPublisher`는 "이동 대상이 아니다"였고 스프링 구현 `SpringDomainEventPublisher`는 persistence에 있었다.
- 덩어리 02 직후 문서는 "domain에 남은 출력 포트는 `product/port/ProductReviewStatisticsPort`·`rank/port/MemberReviewCountPort`(`infrastructure:jpa` 소관)뿐"이라고 적었다.
- 명시적 save 책임(도메인 변경 후 `repository.save(domain)` 호출)은 "도메인 서비스와 api 모듈의 `{도메인}CommandService` 양쪽에 있다"고 적혀 있었다.
- 배달팁 규격의 대상은 `shop/service/ShopDeliveryTipService.java`, `ProductPrice` 규격의 대상은 `product/service/ProductPriceService.java`로 domain 경로를 가리켰다.

### backend/domain/AGENTS.md — 컨텍스트 경계 위반 봉인 / 컨텍스트 간 순환

- `ContextBoundaryTest.SEALED_VIOLATIONS`는 14개였다(덩어리 02에서 16 → 14).
- 강결합 성분 `"order,product,review,shop"` 1건을 `SEALED_CYCLES`로 봉인하고 짝 테스트 `sealedCyclesShouldNotBeStale()`을 두었다.
- 03a에서 봉인 목록에서 빠진 13개는 `MemberDeliveryAddressService`·`OrderPlacementService`·`PaymentCancellationService`·`ReservationBookingService`·`ReviewBlindRequestService`·`ReviewLifecycleService`·`ReviewOwnerReplyService`·`ShopCeoAssignmentService`·`ShopDeliveryAreaPolygonService`·`ShopDeliveryAreaRadiusService`·`ShopDeliveryAreaService`·`ShopDeliveryTipService`·`ShopRequestCancelService`(이후 유스케이스 분리로 `ShopRequestCancellationService`로 개명)이다.

### backend/CLAUDE.md — 도메인 컨텍스트 경계 규칙 (ArchUnit 강제 — 봉인 목록 방식)

- domain `ContextBoundaryTest`의 허용 목록에 `<ctx>.port..`(출력 포트)가 있었다. 03a로 출력 포트가 전부 `application/<ctx>/port/out`으로 옮겨가 대상이 소멸해 허용 목록에서 뺐다.

### backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md — Bounded Contexts

- 컨텍스트 표에 `repository`(write 포트)·`port`(출력 포트) 열이 있었고, `service` 열은 포트를 주입받는 오케스트레이션 서비스까지 셌다.

### backend/AGENTS.md — Module Dependency Graph

- "persistence·조회·이벤트 발행·도메인 서비스 빈 등록은 전부 `infrastructure:persistence`가 전담한다."
- "도메인에 새 POJO 서비스를 추가할 때도 스캔 엔트리를 되살리지 말고 해당 컨텍스트의 `<Ctx>DomainConfig`에 `@Bean`을 추가한다."

### infrastructure:persistence (당시) — `<Ctx>DomainConfig`

- 도메인 서비스 등록은 persistence의 `<ctx>/config/<Ctx>DomainConfig` 18개(마커 없음 — 4앱 전부 스캔)가 했다(`3b099d9f4` 2026-08-15 도입).
- 서비스 연결 어댑터 `ShopRequestIndexSyncAdapter`·`ReplyPhraseProhibitedWordValidatorAdapter`와 금칙어 캐시 데코레이터는 persistence에 `@Component` 등으로 있었다.

## 결정

| 항목 | before | after |
|---|---|---|
| write 포트 106개 + 보조 타입 2개 | `domain/<ctx>/repository/` | `application/<ctx>/port/out/write/` |
| 출력 포트 7개 + record 3개 | `domain/<ctx>/port/` | `application/<ctx>/port/out/` |
| 포트를 주입받는 서비스 71개(+`NotificationMessage`) | `domain/<ctx>/service/` | `application/<ctx>/service/`(마커 없는 POJO) |
| 빈 등록 18개 | persistence `<Ctx>DomainConfig` | application `<ctx>/config/<Ctx>ServiceConfig`(`@Configuration(proxyBeanMethods = false) @SharedApp`). `@Bean` 메서드 이름 불변. `PaymentDomainConfig`는 기존 `PaymentServiceConfig`에 병합 |
| `DomainEventPublisher` / `SpringDomainEventPublisher` | domain / persistence `@Component` | `application/shared/event/`, `SharedEventConfig`(`@SharedApp`)의 `@Bean domainEventPublisher` |
| `OptimisticLockConflictException` | `domain/shared/exception/` | `application/shared/port/out/` |
| 서비스 연결 어댑터 2개·금칙어 캐시 데코레이터 | persistence | `application/shop/service/` |
| domain `ContextBoundaryTest` | 봉인 위반 14 · 순환 성분 1 봉인 | 봉인 위반 1(`DeliveryAreaProjection`) · 순환 봉인 해제, `beFreeOfCycles()` 예외 없이 강제 |
| application ArchUnit | — | `ServiceContextBoundaryTest` 신설(domain에서 봉인 15건·순환 성분 1개 승계), `RuleAnchorTest#writePortsExist`(≥106) 신설, `queryServicesShouldNotDependOnWritePorts` 대상을 `port.out.write`로 교체 |

## 근거

- 엄격 레이어드에서 infrastructure는 application만 본다. 포트와 그 포트를 주입받는 서비스가 domain에 있으면 이 방향을 만들 수 없다.
- 순수 서비스가 이동 서비스를 주입하면 함께 옮겼다(`ShopRiderGuideValidator`). domain은 application을 볼 수 없기 때문이다.
- `SpringDomainEventPublisher`를 persistence에 두면 persistence가 `port.out` 밖의 application 타입을 봐야 해 `shouldNotDependOnApiModules`에 걸린다. application 설정이 persistence 구현을 `new`할 수도 없다(모듈 의존 없음). 구현이 `ApplicationEventPublisher`에 위임하는 한 줄이라 application이 자연스럽다.
- `OptimisticLockConflictException`을 `port/out` 아래에 둔 것은 03b에서 persistence가 domain 없이 던질 수 있게 하기 위해서다.
- 서비스 연결 어댑터는 DB 기술이 아니라 서비스 연결부라, persistence에 두면 infra가 application 서비스를 부르게 된다. 포트를 소유한 product·ceo가 아니라 `shop`에 둔 이유는 product·ceo 쪽에 두면 어댑터가 shop의 `service`를 직접 참조해 `ServiceContextBoundaryTest` 위반이 되고 봉인 목록은 늘릴 수 없기 때문이다.
- domain 순환 성분을 만들던 간선이 전부 포트 주입 서비스에서 나왔다. 서비스가 떠나자 domain 순환 성분이 0개가 됐고, 짝 테스트가 봉인의 낡음을 알려 준 대로 봉인을 해제했다.
- 규칙을 따라 옮기지 않으면 경계가 조용히 사라지므로, 서비스가 옮겨 간 application에서 같은 구조(봉인 목록 + 짝 테스트)로 컨텍스트 경계를 이어서 강제했다. domain에서 빠진 봉인 항목은 위반을 해소한 것이 아니라 대상이 application으로 옮겨간 것이다.
- `queryServicesShouldNotDependOnWritePorts`의 대상을 바꾸지 않으면 대상 패키지가 사라져 아무것도 검사하지 않고 통과한다. `writePortsExist`가 그 무력화를 빌드 실패로 바꾼다.

## 결과·감수한 손실

- 로직·HTTP 계약·DB 스키마는 바뀌지 않았다. 클래스와 빈 등록 위치만 옮겼다.
- `ServiceContextBoundaryTest`는 타 컨텍스트의 domain `model`·`service`와 application `port.out.write`·`service`를 금지하고 `vo`·`event`·`port.out`(write 제외)을 허용한다. 봉인 15건은 03a가 옮긴 13건 + 02가 옮긴 `MailVerificationService`·`PaymentConfirmationService`이고, 순환 봉인 성분 `"order,product,review,shop"`과 성분 단위(SCC) 봉인 원칙도 그대로 승계했다. 두 목록 모두 줄어들기만 한다.
- 서비스 연결 어댑터 2개는 이관 당시 `ShopServiceConfig`가 등록했다. JPA/DAO를 쓰는 `StorePriceVerificationAdapter`·집계 조회 어댑터 4종은 이때 persistence에 남았다(`StorePriceVerificationAdapter`는 이후 03b에서 application으로 이동).
- 이벤트 발행기를 등록하던 `SharedEventConfig`는 이후 application `*ServiceConfig` 삭제 때 `shared/config/SharedBeanConfig`로 리네임됐다.
- 발행기 빈 이름만 `springDomainEventPublisher` → `domainEventPublisher`로 바뀌었다(`@Qualifier` 참조 0건).
- domain에 남은 순수 서비스(`CupDepositPolicy`·`ShopDeliveryTipCalculator` 등)의 `@Bean`도 `<Ctx>ServiceConfig`가 가졌다. 클래스는 domain, 등록은 application이었다.
- 이 덩어리가 끝나도 persistence는 `:domain`을 의존했다. write 포트 시그니처가 domain 모델이었기 때문이다(03b에서 State로 바꿨다가 이후 persistence domain 재허용으로 되돌아왔다).
- 대체: `<Ctx>ServiceConfig`로 등록하는 방식은 [ADR-0034](0034-service-config-removal.md)가 지웠고 이후 [ADR-0036](0036-app-marker-removal.md)이 `@Service` 스캔 등록으로 바꿨다. domain `<ctx>/service/`에 남은 순수 서비스는 [ADR-0041](0041-domain-service-into-model.md)로 `model`에 흡수됐다. 포트·포트 주입 서비스·이벤트 발행기를 application이 소유한다는 부분은 현재도 유효하다.

## 영향받은 문서

- `backend/domain/AGENTS.md` · "Purpose" · write 포트·출력 포트·포트를 주입받는 도메인 서비스는 이 모듈에 없고 전부 application에 있다.
- `backend/domain/AGENTS.md` · "Working In This Directory" · write 포트·출력 포트는 이 모듈에 두지 않는다.
- `backend/domain/AGENTS.md` · "컨텍스트 경계 위반 1건 — 현상 동결 봉인" · 봉인은 `DeliveryAreaProjection` 1건이다.
- `backend/domain/AGENTS.md` · "컨텍스트 간 순환 — 예외 없이 강제" · `beFreeOfCycles()`를 예외 없이 강제한다.
- `backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md` · "Key Files / Cross-cutting packages" / "Bounded Contexts" · `DomainEventPublisher`·`OptimisticLockConflictException`은 application에 있고, 컨텍스트 표에 `repository`·`port` 열이 없다.
- `backend/AGENTS.md` · "Module Dependency Graph" · 이벤트 발행·도메인 서비스 빈 등록은 application이 맡는다.
- `backend/CLAUDE.md` · "write 포트 잔류 판정 기준 (domain repository에 남길 조회의 경계)" · write 포트는 `application/<ctx>/port/out/write/`에 있고 판정 기준은 그대로다.
- `backend/CLAUDE.md` · "도메인 컨텍스트 경계 규칙 (ArchUnit 강제 — 봉인 목록 방식)" · 서비스 사이 경계는 application `ServiceContextBoundaryTest`가 강제한다.
- `backend/CLAUDE.md` · "도메인 모델 / JPA 엔티티 분리 규칙 (선별 적용, persistence는 `infrastructure-module`로)" · 저장 흐름은 application write 포트 ← persistence 어댑터다.
- `backend/application-core/AGENTS.md` · "덩어리 03a — 나머지 포트·도메인 서비스·이벤트 발행기를 `domain`에서 이관" / "`ServiceContextBoundaryTest` — 도메인 서비스 사이의 컨텍스트 경계" · 이관 목록과 경계 테스트.
- `backend/application-core/AGENTS.md` · "03a로 domain에서 옮겨 온 설계 근거 (도메인 서비스)" · `<Ctx>ServiceConfig`(구 `<Ctx>DomainConfig`) 등록 근거 등.
- `backend/infrastructure/jpa/AGENTS.md` · "컨텍스트 경계를 건너는 것은 얇은 포트 어댑터가 흡수한다" · 서비스 연결 어댑터는 application으로 갔고 이 모듈에는 집계 조회 어댑터만 남는다.
