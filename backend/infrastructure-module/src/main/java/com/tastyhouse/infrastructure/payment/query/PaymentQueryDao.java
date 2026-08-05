package com.tastyhouse.infrastructure.payment.query;

import java.util.Optional;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;
import com.tastyhouse.infrastructure.shared.query.ConvertedIdPaths;

import static com.tastyhouse.infrastructure.order.persistence.QOrderJpaEntity.orderJpaEntity;
import static com.tastyhouse.infrastructure.payment.persistence.QPaymentJpaEntity.paymentJpaEntity;
import static com.tastyhouse.infrastructure.payment.persistence.QPaymentRefundJpaEntity.paymentRefundJpaEntity;

/**
 * 결제 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code PaymentRepository} 등)와 역할이 겹치지 않는다. 소비 모듈(web-api)의
 * {@code PaymentQueryService}가 이 DAO를 주입해 사용한다.
 *
 * <p>현재는 소비 모듈이 실제로 쓰는 조회 둘만 갖는다 — 주문별 결제 조회({@link #findPaymentByOrderId},
 * 회원의 결제 확인 화면)와 PK 조회({@link #findPaymentById}, command 커밋 후 응답 조립용 재조회). 공통
 * 지침 패턴 3의 "소비 모듈이 실제 쓰는 메서드·필드만 이관" 원칙에 따른다. 관리자 결제·환불 내역 조회는
 * admin-api에 결제 소비자가 생길 때 이 DAO에 메서드로 추가한다(호출부 없는 조회를 미리 만들지 않는다).
 *
 * <p>{@code PAYMENT.order_id}는 {@code @Convert}로 {@link OrderId} VO에 매핑된 필드라 QueryDSL이 VO
 * 타입 path를 생성하므로, {@link Expressions#numberPath}로 raw {@code Long} 컬럼 비교를 우회한다
 * ({@code OrderQueryDao}와 동일한 우회).
 */
@Repository
public class PaymentQueryDao {

    private final JPAQueryFactory queryFactory;

    public PaymentQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 주문의 결제 단건 — 결제가 없으면 {@link Optional#empty()}.
     *
     * <p>회원 스코프 검증에 쓸 주문의 {@code memberId}를 함께 투영한다. 주문이 없으면 결제도 조회되지
     * 않으므로(inner join) 소비 모듈은 "주문 없음"과 "결제 없음"을 결과 부재로 함께 처리한다.
     *
     * <p>{@code PAYMENT.order_id}에 unique 제약이 있어 주문당 결제는 최대 1건이다. 이 불변식이 깨지면
     * 임의의 한 건을 조용히 고르는 대신 {@code fetchOne}으로 즉시 실패시킨다 — 기존
     * {@code PaymentRepository#findByOrderId}와 동일한 fail-loud 시맨틱을 유지한다.
     */
    public Optional<PaymentResult> findPaymentByOrderId(OrderId orderId) {
        return Optional.ofNullable(
            selectPayment()
                .where(ConvertedIdPaths.eq(paymentJpaEntity, "orderId", OrderId.class, OrderId::of, orderId.value()))
                .fetchOne()
        );
    }

    /**
     * 결제 단건(PK) — 결제가 없으면 {@link Optional#empty()}.
     *
     * <p>command 경로(생성·승인·취소·현장완료·환불)가 커밋 후 응답을 조립할 때 재조회하는 경로다
     * (CQRS 분리 — command는 식별자만 돌려주고 조립은 조회가 담당).
     */
    public Optional<PaymentResult> findPaymentById(PaymentId paymentId) {
        return Optional.ofNullable(
            selectPayment()
                .where(paymentJpaEntity.id.eq(paymentId.value()))
                .fetchOne()
        );
    }

    /**
     * 환불 요청 단건(PK) — 없으면 {@link Optional#empty()}.
     *
     * <p>환불 요청 command가 커밋 후 응답을 조립할 때 재조회하는 경로다. 소유권은 요청 시점에 이미
     * 검증되었고 이 조회는 그 직후 재조회이므로 회원 스코프를 다시 대조하지 않는다.
     */
    public Optional<PaymentRefundResult> findRefundById(PaymentRefundId refundId) {
        return Optional.ofNullable(
            queryFactory
                .select(new QPaymentRefundResult(
                    paymentRefundJpaEntity.id,
                    refundPaymentIdValue(),
                    paymentRefundJpaEntity.refundAmount,
                    paymentRefundJpaEntity.refundReason,
                    paymentRefundJpaEntity.refundStatus,
                    paymentRefundJpaEntity.pgRefundId,
                    paymentRefundJpaEntity.refundedAt,
                    paymentRefundJpaEntity.createdAt
                ))
                .from(paymentRefundJpaEntity)
                .where(paymentRefundJpaEntity.id.eq(refundId.value()))
                .fetchOne()
        );
    }

    /**
     * 결제 단건 투영 — 두 조회 경로(주문별·PK별)가 같은 필드 셋을 쓰므로 공유한다.
     */
    private JPAQuery<PaymentResult> selectPayment() {
        return queryFactory
            .select(new QPaymentResult(
                paymentJpaEntity.id,
                paymentOrderIdValue(),
                orderJpaEntity.memberId,
                paymentJpaEntity.paymentMethod,
                paymentJpaEntity.paymentStatus,
                paymentJpaEntity.amount,
                paymentJpaEntity.pgProvider,
                paymentJpaEntity.pgTid,
                paymentJpaEntity.pgOrderId,
                paymentJpaEntity.cardCompany,
                paymentJpaEntity.cardNumber,
                paymentJpaEntity.installmentMonths,
                paymentJpaEntity.approvedAt,
                paymentJpaEntity.cancelledAt,
                paymentJpaEntity.cancelReason,
                paymentJpaEntity.receiptUrl,
                paymentJpaEntity.createdAt
            ))
            .from(paymentJpaEntity)
            .innerJoin(orderJpaEntity).on(orderJpaEntity.id.eq(paymentOrderId()));
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code PAYMENT.order_id}를 raw {@code Long}으로 다루기 위한 path.
     */
    private NumberPath<Long> paymentOrderId() {
        return Expressions.numberPath(Long.class, paymentJpaEntity, "orderId");
    }

    /**
     * {@code orderId}({@code @Convert} OrderId) 컬럼을 raw {@code Long}으로 읽기 위한 경로.
     * 투영·tuple 조회·groupBy에 쓴다 — VO 그대로 읽으면 Result 생성자/Map 키 타입이 어긋난다.
     */
    private NumberExpression<Long> paymentOrderIdValue() {
        return ConvertedIdPaths.longValue(paymentJpaEntity, "orderId", OrderId.class);
    }

    /**
     * {@code paymentId}({@code @Convert} PaymentId) 컬럼을 raw {@code Long}으로 읽기 위한 경로.
     * 투영·tuple 조회·groupBy에 쓴다 — VO 그대로 읽으면 Result 생성자/Map 키 타입이 어긋난다.
     */
    private NumberExpression<Long> refundPaymentIdValue() {
        return ConvertedIdPaths.longValue(paymentRefundJpaEntity, "paymentId", PaymentId.class);
    }
}
