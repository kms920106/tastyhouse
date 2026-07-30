# order 도메인 전환

> 선행: `tasks/README.md` + `00-phase0` + 그룹 3 완료 + **22-point·23-coupon 완료 필수**. 그룹 4 — **41-payment와 같은 AI가 이 파일부터 순서대로 수행할 것**(원자 동기화 클러스터).

## 현황
- core: `order/application/` — `OrderCommandService`(3 repo: Order+OrderProduct+OrderProductOption, 이벤트 발행, 명시적 save — createOrder 금액/가격 갱신 2곳·changeOrderStatus·deleteOrder), QueryService + result(`OrderListItemResult`/`OrderManagementListItemResult` 등). `OrderRepositoryImpl`은 shop/payment Q타입 조인.
- 소비자: web-api(주문 생성·내 주문), admin-api(주문 관리 `findOrderDetailById`), ceo-api(주문가능 상태 연동 여부 확인).

## 작업
1. **(C) 하강 — 클러스터 핵심**: `createOrder`(Order+상품+옵션 원자 생성 + 금액 계산 + 재고/가게 검증 + 이벤트 발행)를 `OrderPlacementService`(가칭)로, 주문 상태전이(`changeOrderStatus` — 결제·포인트 연쇄의 진입점)를 `OrderTransitionService`(가칭)로 하강. 포인트/쿠폰 사용 연동은 22/23이 하강시킨 `PointLedgerService`/`CouponIssueService` 호출로 연결. 이벤트 발행은 `DomainEventPublisher`.
2. **(A)**: web `OrderCommandService`(주문 생성 요청 조립·회원 스코프), admin `OrderCommandService`(관리자 상태 변경)가 각자 `@Transactional`로 도메인 서비스 호출(패턴 2 — 기존 facade `OrderService`는 Command/Query로 분해).
3. **(B)**: infra `infrastructure/order/query/OrderQueryDao` 신설(패턴 3 — web용 내 주문 목록/상세 `OrderListItemResult`, admin용 `OrderManagementListItemResult` 메서드 분리). `Management` 한정어는 **유지**한다 — 두 result가 같은 패키지에 공존하므로 충돌은 사라지지 않는다(과거 "제거 가능" 문구 폐지). shop/payment Q타입 조인은 같은 모듈 내 참조. web/admin 각 `OrderQueryService`(readOnly)가 DAO 주입. Repository 3개 write 순수화(패턴 4).
4. core `order/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시. `OrderTransitionService` 시그니처 하단 기록 — **41-payment가 즉시 사용**.

---

## 완료 기록 (41-payment 인계)

### `OrderTransitionService` (POJO, `core/domain/order/domain/service/`)

빈 등록: infrastructure-module `DomainServiceConfig#orderTransitionService(OrderRepository)`.

```java
Order load(OrderId orderId)                            // PK 로드, 없으면 ORDER_NOT_FOUND
Order loadOwnedBy(OrderId orderId, MemberId memberId)  // 로드 + 소유권 검증(ORDER_ACCESS_DENIED)
void changeStatus(OrderId orderId, OrderStatus status) // 로드 → 전이 → save
void changeStatus(Order order, OrderStatus status)     // 이미 로드된 주문 전이 → save
void confirm(Order order)                              // CONFIRMED 전이 → save
void cancel(Order order)                               // CANCELLED 전이 → save
void delete(OrderId orderId)                           // soft delete → save
```

**41-payment 사용법**: 결제 승인/취소는 같은 트랜잭션에서 주문을 함께 바꾸므로, 이미 로드한 `Order`를 넘기는
`changeStatus(Order, ...)`/`confirm`/`cancel` 오버로드를 쓰면 중복 조회 없이 "전이와 저장은 항상 함께"가 보장된다.
현재 `PaymentCommandService`는 `orderRepository.findById(...)` + `orderRepository.save(order)`를 직접 호출하고
있는데(이번 작업에서 core `OrderQueryService` 삭제에 따른 컴파일 복구만 수행), 41-payment에서 이 지점들을
위 도메인 서비스 호출로 바꾸면 저장 누락 위험이 사라진다.

### `OrderPlacementService` (POJO, 주문 접수)

```java
OrderId place(MemberId memberId, OrderPlacement placement)
```
입력 record: `OrderPlacement`(shopId, orderMethod, items, memberCouponId, usePoint, 금액 검증 6종) /
`OrderPlacementItem`(productId, quantity, selectedOptions) / `OrderPlacementItemOption`(groupId, optionId).
쿠폰·포인트 연동은 `CouponIssueService#useCoupon`·`PointLedgerService#usePoints` 호출로 연결됨.
반환은 식별자만 — 응답 조립은 커밋 후 `OrderQueryService`가 재조회.

### infra query DAO

`infrastructure/order/query/OrderQueryDao`(@Repository) — `findOrders(MemberId, PageQuery)`(web),
`findOrders(OrderSearchCondition, PageQuery)`(admin), `findOrderDetail(OrderId)`(공용).
Result: `OrderListItemResult`, `OrderManagementListItemResult`(`Management` 유지),
`OrderDetailResult`, `OrderProductResult`, `OrderProductOptionResult`, `OrderPaymentResult`, `OrderSearchCondition`.
`OrderDetailResult`/`OrderProductResult`는 1:N·0..1 필드를 `withXxx` 복사 메서드로 덧붙인다(review 선례).

### write 포트 순수화 결과

- `OrderRepository`: `findById`/`save`만
- `OrderProductRepository`: `findById`/`save`만 (`findByOrderId` 제거)
- `OrderProductOptionRepository`: `save`만 (`findByOrderProductId` 제거)

### 소비 모듈 CQRS

web `OrderCommandService`/`OrderQueryService`, admin `OrderCommandService`/`OrderQueryService`
(기존 facade `OrderService` 2개 삭제, 컨트롤러가 두 서비스를 각각 주입).
web 상세는 DAO가 투영한 `memberId`를 요청 회원과 대조해 소유권을 검증한다.

### 이관하지 않은 기존 동작(범위 외 — 다음 작업 참고)

이 작업은 순수 재배치이므로 아래 기존 허점을 **그대로 옮겼다**(README 절대규칙 4 — 자기 도메인 밖·기존
동작 개편 금지). 신규 회귀가 아니라 이전부터 있던 것이며, 고치려면 별도 작업으로 다뤄야 한다.

- **가게-상품 소속 검증 없음**: `place()`는 가게를 존재 확인만 하고(로드 후 미사용), 각 상품이 그 가게의
  상품인지 검증하지 않는다. `shopId=1`로 주문하면서 가게 2의 `productId`를 보내도 통과한다. 위 작업 항목
  1의 "재고/가게 검증" 문구는 실제로는 **판매중지(`isSoldOut`) 검증만** 해당한다.
- **soft delete된 주문의 상세는 계속 조회된다**: `findOrderDetail`에 `deleted.isFalse()` 필터가 없다
  (기존 `orderRepository.findById`도 동일). 목록에서는 사라지지만 id 직접 조회로는 주문자 연락처까지 보인다.
- **금액 필드 null 방어는 HTTP 경계에 의존**: `OrderPlacement`의 `usePoint`·금액 6종은 도메인에서
  언박싱·`equals` 되므로, `OrderCreateRequest`의 Bean Validation이 유일한 방어선이다.

### 크로스 도메인 복구(컴파일 복구 목적만)

- web `ReviewQueryService`/`ReviewCommandService`: `OrderQueryService#findOrderProductById` →
  `OrderProductRepository#findById` 직접 주입(리뷰 작성 자격 확인 = write 경로 불변식 검증).
- core `PaymentCommandService`/`PaymentQueryService`: `OrderQueryService#findById` → `OrderRepository#findById`.
