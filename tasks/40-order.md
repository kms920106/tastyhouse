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
