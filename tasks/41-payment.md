# payment 도메인 전환

> 선행: **40-order 완료 필수**(같은 AI 연속 수행 권장). 그룹 4. **출력 포트·이벤트 리스너 보유.**

## 현황
- core: `payment/application/` — `PaymentCommandService`(4 repo, **Payment+Order 원자 동기화**: confirmPayment/confirmTossPayment/cancelPayment/completeOnSitePayment 모두 orderRepository.save 동반), `PaymentEventListener`, **출력 포트 `application/port/PgPaymentGateway`(+`PgConfirmResult`/`PgCancelResult`/`TossPaymentDetail` dto)** — external-api(토스)가 구현. `TossPaymentRecord` insert-only 원장.
- 소비자: web-api(결제 확인/취소), admin-api(환불·관리), 이벤트 경유 point 연동.

## 작업
1. **포트 이동**: `application/port/**` → `domain/payment/port/`(dto 포함). external-api 토스 어댑터 import 갱신.
2. **(C) 하강 — 플랜 시나리오 B의 실전**: confirm/cancel/completeOnSite 각각을 `PaymentConfirmationService`/`PaymentCancellationService`(가칭)로 하강. Order 상태 동기화는 40-order의 `OrderTransitionService` 호출 또는 직접 orderRepository 사용(둘 중 시그니처가 자연스러운 쪽 — 단일 원천 유지가 기준). PG 원장(`TossPaymentRecord`) 기록 포함. 이벤트 발행은 `DomainEventPublisher`.
3. **(E)**: `PaymentEventListener` → infrastructure `payment/listener/`, 본문은 도메인 서비스 호출로 축소(포인트 연동은 `PointLedgerService`).
4. **(A)**: web `PaymentCommandService`(회원 결제 확인 — 소유권 검증), admin `PaymentCommandService`(환불 처리)로 각자 `@Transactional` 조율자 구성(패턴 2).
5. **(B)**: infra `infrastructure/payment/query/PaymentQueryDao` 신설(패턴 3 — admin용 결제/환불 내역 조회), Result는 infra query 소유. admin `PaymentQueryService`(readOnly)가 DAO 주입. Repository 3개 write 순수화(패턴 4).
6. core `payment/application/` 삭제.

## 완료 기준
- external-api 포함 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시. "결제·주문 동기화가 단일 원천(도메인 서비스 1곳)에만 존재"함을 grep으로 확인해 기록.
