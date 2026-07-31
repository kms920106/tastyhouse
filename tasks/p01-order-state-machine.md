# P1. Order 애그리거트 상태 머신 도입

## 배경

domain-module의 애그리거트 중 `Reservation`은 전이별 가드(잘못된 전이 시 상태별 `ErrorCode`)를 갖춘 완전한 상태 머신인데, 결제·포인트·환불이 전부 얹혀 있는 `Order`만 유일하게 상태 전이 가드가 전혀 없다. CANCELLED 주문을 `confirm()`할 수 있고, `changeStatus()`는 어떤 전이든 무검증으로 허용한다.

## 문제 상세

- `domain-module/src/main/java/com/tastyhouse/domain/order/domain/model/Order.java:201-215`

```java
public void confirm()  { this.orderStatus = OrderStatus.CONFIRMED; }   // 무조건 대입
public void cancel()   { this.orderStatus = OrderStatus.CANCELLED; }   // 무조건 대입
public void changeStatus(OrderStatus status) { this.orderStatus = status; }  // 임의 전이 공개
public void delete()   { this.deleted = true; }
```

- `domain-module/.../order/domain/service/OrderTransitionService.java:85-95` — `changeStatus(OrderId, OrderStatus)`를 외부(admin 수동 상태변경 경로)에 그대로 공개해 이 구멍이 실제 사용 중.
- `Order.java:217-235` `updateAmounts(...)` — 8개 파라미터 벌크 세터. 금액 정합(`totalDiscountAmount == productDiscount + couponDiscount + pointDiscount`, `finalAmount == totalProductAmount - totalDiscountAmount`, 각 금액 음수 금지)을 스스로 검증하지 않는다. 그 검증은 `OrderPlacementService.validateAmounts()`(:247)에 있으나 "클라이언트 값 대조"일 뿐 애그리거트 불변식이 아니다.
- 참고 모범 사례: `domain-module/.../reservation/domain/model/Reservation.java:128-135`의 `cancel()` — CANCELED/REJECTED/COMPLETED 각각 다른 `ErrorCode`를 던지는 switch 가드.

## 작업 지시

1. `OrderStatus` enum에 허용 전이 판정을 추가한다(예: `boolean canTransitionTo(OrderStatus target)` 또는 전이 테이블). 실제 허용 전이는 **현재 호출부 전수 조사로 확정**한다 — `OrderTransitionService`, `PaymentConfirmationService`, `PaymentCancellationService`, admin-api의 주문 상태변경 경로가 실제로 수행하는 전이를 모두 수집한 뒤, 그 집합 + 업무상 자연스러운 전이만 허용한다.
2. `Order.confirm()`/`cancel()`에 현재 상태 가드를 추가한다. 잘못된 전이는 기존 `BusinessException` + `ErrorCode` 체계로 던진다(Reservation 선례처럼 상태별로 구분된 ErrorCode 신설 — `ErrorCode.java`에 추가).
3. `Order.changeStatus(OrderStatus)`는 **폐기가 1안**이다. admin 수동 변경이 정말 필요하면 전이 테이블 검증을 통과하는 형태로만 남긴다. 폐기/유지 판단이 애매하면 admin-api 호출부의 실제 용도를 확인해 결정하고, 결정 근거를 Javadoc에 남긴다.
4. `updateAmounts(...)`에 금액 정합 불변식 검증을 추가한다(음수 금지 + 합산 정합). 위반 시 `BusinessException`.
5. `OrderTransitionService`의 공개 시그니처를 새 가드에 맞춰 정리한다.
6. domain-module 순수 단위 테스트를 추가한다: `OrderTest`에 전이표 케이스(허용 전이 전부 + 대표 금지 전이), `updateAmounts` 정합 위반 케이스. 기존 `OrderTransitionServiceTest`도 갱신.

## 수용 기준

- [ ] CANCELLED 상태에서 `confirm()` 호출 시 `BusinessException` 발생 (테스트로 증명)
- [ ] 무검증 `changeStatus`가 public API에서 사라지거나 전이 테이블 검증을 통과해야만 동작
- [ ] `updateAmounts`가 음수·합산 불일치 입력을 거부 (테스트로 증명)
- [ ] 기존 정상 플로(주문 생성→결제 확정→confirm, 취소 플로)가 깨지지 않음 — 호출부 전수 확인
- [ ] domain-module 테스트 전량 통과 (gradle 금지 — verify-without-gradle 절차)

## 주의사항

- **P9(도메인 이벤트) 태스크와 충돌 가능**: P9가 `Order` 전이 시 이벤트 발행을 추가할 수 있다. P1을 먼저 완료할 것.
- `reconstitute()`는 DB 재구성 전용이므로 전이 가드를 태우지 않는다(기존 데이터에 어떤 상태가 있어도 로드는 가능해야 함).
- 기존 DB에 이미 존재하는 "비정상 전이 이력" 데이터는 이 작업의 대상이 아니다 — 앞으로의 전이만 막는다.
