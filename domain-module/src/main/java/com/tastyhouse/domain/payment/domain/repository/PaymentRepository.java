package com.tastyhouse.domain.payment.domain.repository;

import java.util.Optional;

import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.payment.domain.model.Payment;
import com.tastyhouse.domain.payment.domain.vo.PaymentId;

/**
 * 결제 write 포트.
 *
 * <p>command 경로·도메인 서비스의 트랜잭션 안에서 소비되는 단건 로드와 저장만 남긴다(공통 지침 패턴 4).
 * 표현 목적 조회는 infrastructure-module의 {@code infrastructure/payment/query/PaymentQueryDao}가
 * 담당한다.
 *
 * <p>잔류 판정: {@link #findById}는 승인·취소·환불의 상태 전이 대상 로드, {@link #findByPgOrderId}는 PG
 * 주문번호(자연키)로 승인 대상을 찾는 단건 로드, {@link #existsByOrderId}는 중복 결제 방지 불변식 검증에
 * 각각 필요하다 — 모두 "이 조회가 없으면 상태 전이·불변식 검증이 불가능"하므로 포트에 남는다. 반면 화면
 * 조립용 주문별 결제 조회({@code findByOrderId})는 query DAO로 이관해 여기서 제거했다.
 */
public interface PaymentRepository {

    Optional<Payment> findById(PaymentId paymentId);

    Optional<Payment> findByPgOrderId(String pgOrderId);

    boolean existsByOrderId(OrderId orderId);

    Payment save(Payment payment);
}
