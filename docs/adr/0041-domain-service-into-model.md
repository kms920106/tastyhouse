# ADR-0041: domain의 service 패키지를 model로 흡수한다

- 상태: 채택
- 결정 커밋: `b4f1dc866` (2026-10-04)
- 대체한 결정: [ADR-0027](0027-domain-ports-to-application.md)(일부 — "domain의 `<ctx>/service/`에는 순수 계산기·정책만 남는다")

## 맥락

- **바운디드 컨텍스트**: `order`·`shop`·`member`처럼 하나의 업무 영역을 뜻하는 패키지 단위입니다. domain 모듈 안에 25개가 함께 있습니다.
- **도메인 서비스**: 엔티티 하나에 담기 어려운 도메인 규칙(계산·정책·검증)을 담는 클래스입니다.

[ADR-0027](0027-domain-ports-to-application.md)(덩어리 03a)로 포트를 주입받는 오케스트레이션 서비스는 전부 `application`으로 떠났습니다. 그 뒤 domain의 `<ctx>/service/`에는 포트를 주입받지 않는 순수 계산기·정책과 그 입출력 record 47개만 남았습니다. 컨텍스트 경계 규칙상 이것들을 `model`과 따로 둘 의미가 없었습니다(커밋 메시지).

## 이전 규칙

### backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md — 도메인 서비스 규칙 (`<ctx>/service/`)

- 03a 항목: "이 패키지의 `<ctx>/service/`에는 포트를 주입받지 않는 순수 계산기·정책·검증기와 그 입출력 record만 남는다." 포트(write 포트·출력 포트·`DomainEventPublisher`)를 주입받는 오케스트레이션 서비스는 `application/<ctx>/service/`로 옮겨졌습니다.
- Purpose 절: 각 Bounded Context는 `<ctx>/{model,vo,event,repository,service,port}` 구조였고, `repository`·`port`가 03a로 `application`에 가면서 `<ctx>/{model,vo,event,service}`가 됐습니다.

### backend/domain/AGENTS.md — 컨텍스트 경계 위반 1건 — 현상 동결 봉인

타 컨텍스트의 `model..`/`repository..`/`service..` 직접 import를 금지했습니다. `ContextBoundaryTest`·`ServiceContextBoundaryTest`가 domain 쪽 `service` 패키지도 판정했습니다.

### backend/domain/AGENTS.md — 순수 계산기 형태

이 절의 대상 `ShopDeliveryTipCalculator`·`ShopOperatingStatusCalculator`는 `backend/domain/src/main/java/com/tastyhouse/domain/shop/service/`에 있었습니다.

### backend/domain/AGENTS.md — 모듈 전역 — 프레임워크-프리와 명시적 저장

이 항목의 대상은 "domain 전 컨텍스트의 model·service"였습니다.

## 결정

domain의 `<ctx>/service/`에 남아 있던 순수 계산기·정책·검증기와 입출력 record 47개(8개 컨텍스트 — `coupon`·`member`·`order`·`payment`·`product`·`reservation`·`review`·`shop`, 테스트 14개도 같은 경로로 이동)를 각 컨텍스트의 `<ctx>/model/`로 옮깁니다. domain 컨텍스트 하위 패키지는 `model`·`vo`·`event` 셋입니다.

| 항목 | before | after |
|---|---|---|
| domain 컨텍스트 하위 패키지 | `model`·`vo`·`event`·`service` | `model`·`vo`·`event` |
| 순수 계산기·정책(예: `CupDepositPolicy`, `ShopDeliveryTipCalculator`) | `domain/<ctx>/service/` | `domain/<ctx>/model/` (예: `product/model/CupDepositPolicy`, `shop/model/ShopDeliveryTipCalculator`) |
| 배치 판정 | 포트 주입 → `application/<ctx>/service/`, 아니면 domain `<ctx>/service/` | 포트 주입 → `application/<ctx>/service/`, 아니면 domain `<ctx>/model/` |
| 경계 가드의 금지 목록 | 타 컨텍스트 `model..`·`repository..`·`service..` | 타 컨텍스트 `model..`·`repository..` (domain에 `service`가 없음) |
| 재생성 방지 | 없음 | `DomainPurityTest#domainShouldNotHaveServicePackage` |

## 근거

- 03a 이후 domain `service`에 남은 것은 포트 없는 순수 계산기·정책뿐이라, 컨텍스트 경계 규칙상 `model`과 구분할 의미가 없었습니다.
- 컨텍스트 간 참조 규칙(`ContextBoundaryTest`)에서 `service`는 `model`과 똑같이 "타 컨텍스트 import 금지"였으므로, 두 패키지를 나눠도 경계 규칙상 달라지는 것이 없었습니다. 남은 차이는 역할 이름뿐이었습니다. `event`는 타 컨텍스트에 허용된 공개 계약이라 그대로 분리해 둡니다.
- 패키지가 하나 줄면 경계 가드(`ContextBoundaryTest`·`ServiceContextBoundaryTest`)에서 domain 쪽 `service` 판정을 지울 수 있습니다.

## 결과·감수한 손실

- FQN만 바뀌었습니다. 동작·빈 이름·API·DB는 변경이 없습니다.
- `backend/application-core/AGENTS.md`의 앱 네임스페이스 View 표(`ShopStorePriceVerificationViewResult` 행)는 미충족 사유 타입 `StorePriceUnverifiedItem`이 `domain.product.service`에 있어 api 모듈의 carve-out 어디에도 들어가지 않는다고 적었습니다. 이 결정으로 패키지가 `domain.product.model`로 바뀌었고, `UnverifiedItem`으로 옮겨 담는 이유(api 모듈은 `com.tastyhouse.domain..`을 전면 금지)는 그대로입니다.
- 03a 시점 문서에서 domain `<ctx>/service/`를 가리키는 서술은 `<ctx>/model/`로 읽어야 합니다.

## 영향받은 문서

- `backend/domain/AGENTS.md` · Purpose > 컨텍스트 하위 패키지는 `model`·`vo`·`event` 셋이다 (domain `service` → `model` 흡수) · 판정 규칙과 근거
- `backend/domain/AGENTS.md` · 봉인·가드 목록 > 컨텍스트 경계 위반 1건 — 현상 동결 봉인 · 금지 목록에 `service` 없음
- `backend/domain/AGENTS.md` · 코드 주석에서 이관된 설계 근거 > 모듈 전역 — 프레임워크-프리와 명시적 저장 · 대상은 model
- `backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md` · Purpose / Working In This Directory · `<ctx>/{model,vo,event}` 구조
- `backend/application-core/AGENTS.md` · "읽기 계약 carve-out 5종 — 도메인 타입을 강등해 나르는 이유" · domain 계산기의 위치
