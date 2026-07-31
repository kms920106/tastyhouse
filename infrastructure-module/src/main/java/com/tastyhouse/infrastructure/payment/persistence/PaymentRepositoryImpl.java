package com.tastyhouse.infrastructure.payment.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRepository;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;

import static com.tastyhouse.infrastructure.payment.persistence.QPaymentJpaEntity.paymentJpaEntity;

/**
 * 결제 write 어댑터.
 *
 * <p>write 포트 전용으로 축소되어 상태 전이 대상의 단건 로드·중복 검증·저장만 담당한다(공통 지침 패턴 4).
 * 표현 목적 조회(주문별 결제 조회 등)는 같은 모듈의 {@code payment/query/PaymentQueryDao}로 이관했다.
 */
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final JPAQueryFactory queryFactory;
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        return paymentJpaRepository.findById(paymentId.value()).map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByPgOrderId(String pgOrderId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(paymentJpaEntity)
                .where(paymentJpaEntity.pgOrderId.eq(pgOrderId))
                .fetchOne()
        ).map(PaymentMapper::toDomain);
    }

    @Override
    public boolean existsByOrderId(OrderId orderId) {
        return queryFactory.selectOne().from(paymentJpaEntity)
            .where(paymentJpaEntity.orderId.eq(orderId))
            .fetchFirst() != null;
    }

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            PaymentJpaEntity saved = paymentJpaRepository.save(PaymentMapper.toEntity(payment));
            return PaymentMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        PaymentJpaEntity entity = paymentJpaRepository.findById(payment.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 결제입니다: " + payment.getId()));
        PaymentMapper.applyChanges(entity, payment);
        return PaymentMapper.toDomain(entity);
    }
}
