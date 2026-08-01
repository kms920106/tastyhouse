package com.tastyhouse.domain.payment.domain.service;

/**
 * 토스 승인 대상(사전 검증 결과).
 *
 * <p>PG HTTP 호출을 DB 트랜잭션 밖으로 빼면서 토스 승인이 "사전 검증(트랜잭션 1) → PG 호출(트랜잭션 없음)
 * → 결과 반영(트랜잭션 2)" 3단으로 쪼개졌다. 이 record는 1단이 확정한 값 중 PG 호출에 필요한 것만 담아
 * 트랜잭션 밖으로 실어 나른다 — 도메인 모델({@code Payment})을 트랜잭션 밖으로 내보내면 그 인스턴스에
 * 변경을 가한 뒤 다시 저장하려는 유혹이 생기고(detached 상태의 애그리거트를 다루는 문제), 2단은 어차피
 * 새 트랜잭션에서 결제를 다시 로드해야 하기 때문이다.
 *
 * <p>{@code paymentId}는 PG 요청의 상관관계 키({@code PgPaymentGateway#confirmPayment}의 첫 인자)로
 * 쓰이는 원시 식별자이며, {@code amount}는 1단에서 이미 결제 금액과 대조를 마친 값이다.
 */
public record TossConfirmationTarget(
    Long paymentId,
    String pgOrderId,
    int amount
) {
}
