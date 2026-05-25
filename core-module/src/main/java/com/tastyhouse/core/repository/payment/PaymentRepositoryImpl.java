package com.tastyhouse.core.repository.payment;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.payment.Payment;
import com.tastyhouse.core.entity.payment.PaymentRefund;
import com.tastyhouse.core.entity.payment.PaymentStatus;
import com.tastyhouse.core.entity.payment.RefundStatus;
import com.tastyhouse.core.entity.payment.TossPaymentRecord;
import com.tastyhouse.core.entity.payment.dto.OrderListItemDto;
import com.tastyhouse.core.entity.payment.dto.QOrderListItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.entity.order.QOrder.order;
import static com.tastyhouse.core.entity.order.QOrderItem.orderItem;
import static com.tastyhouse.core.entity.payment.QPayment.payment;
import static com.tastyhouse.core.entity.payment.QPaymentRefund.paymentRefund;
import static com.tastyhouse.core.entity.payment.QTossPaymentRecord.tossPaymentRecord;
import static com.tastyhouse.core.domain.place.domain.model.QPlace.place;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final JPAQueryFactory queryFactory;
    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentRefundJpaRepository paymentRefundJpaRepository;
    private final TossPaymentRecordJpaRepository tossPaymentRecordJpaRepository;

    @Override
    public Optional<Payment> findById(Long paymentId) {
        return paymentJpaRepository.findById(paymentId);
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(payment)
                .where(payment.orderId.eq(orderId))
                .fetchOne()
        );
    }

    @Override
    public Optional<Payment> findByPgTid(String pgTid) {
        return Optional.ofNullable(
            queryFactory.selectFrom(payment)
                .where(payment.pgTid.eq(pgTid))
                .fetchOne()
        );
    }

    @Override
    public Optional<Payment> findByPgOrderId(String pgOrderId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(payment)
                .where(payment.pgOrderId.eq(pgOrderId))
                .fetchOne()
        );
    }

    @Override
    public List<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus) {
        return queryFactory.selectFrom(payment)
            .where(payment.paymentStatus.eq(paymentStatus))
            .orderBy(payment.createdAt.desc())
            .fetch();
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return queryFactory.selectOne().from(payment)
            .where(payment.orderId.eq(orderId))
            .fetchFirst() != null;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public List<PaymentRefund> findRefundsByPaymentIdOrderByCreatedAtDesc(Long paymentId) {
        return queryFactory.selectFrom(paymentRefund)
            .where(paymentRefund.paymentId.eq(paymentId))
            .orderBy(paymentRefund.createdAt.desc())
            .fetch();
    }

    @Override
    public Optional<PaymentRefund> findRefundByPgRefundId(String pgRefundId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(paymentRefund)
                .where(paymentRefund.pgRefundId.eq(pgRefundId))
                .fetchOne()
        );
    }

    @Override
    public List<PaymentRefund> findRefundsByRefundStatusOrderByCreatedAtDesc(RefundStatus refundStatus) {
        return queryFactory.selectFrom(paymentRefund)
            .where(paymentRefund.refundStatus.eq(refundStatus))
            .orderBy(paymentRefund.createdAt.desc())
            .fetch();
    }

    @Override
    public PaymentRefund saveRefund(PaymentRefund paymentRefund) {
        return paymentRefundJpaRepository.save(paymentRefund);
    }

    @Override
    public Optional<TossPaymentRecord> findTossRecordByPaymentId(Long paymentId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(tossPaymentRecord)
                .where(tossPaymentRecord.paymentId.eq(paymentId))
                .fetchOne()
        );
    }

    @Override
    public Optional<TossPaymentRecord> findTossRecordByPaymentKey(String paymentKey) {
        return Optional.ofNullable(
            queryFactory.selectFrom(tossPaymentRecord)
                .where(tossPaymentRecord.paymentKey.eq(paymentKey))
                .fetchOne()
        );
    }

    @Override
    public Optional<TossPaymentRecord> findTossRecordByOrderId(String orderId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(tossPaymentRecord)
                .where(tossPaymentRecord.orderId.eq(orderId))
                .fetchOne()
        );
    }

    @Override
    public TossPaymentRecord saveTossRecord(TossPaymentRecord tossPaymentRecord) {
        return tossPaymentRecordJpaRepository.save(tossPaymentRecord);
    }

    @Override
    public Page<OrderListItemDto> findOrderListByMemberId(Long memberId, Pageable pageable) {
        List<OrderListItemDto> content = queryFactory
            .select(new QOrderListItemDto(
                order.id,
                place.name,
                uploadedFile.filePath,
                orderItem.productName.min(),
                orderItem.id.count().intValue(),
                payment.amount,
                payment.paymentStatus,
                payment.approvedAt
            ))
            .from(order)
            .innerJoin(payment).on(
                payment.orderId.eq(order.id)
                    .and(payment.paymentStatus.in(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED))
            )
            .leftJoin(place).on(place.id.eq(order.placeId))
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(place.thumbnailImageFileId))
            .leftJoin(orderItem).on(orderItem.orderId.eq(order.id))
            .where(order.memberId.eq(memberId))
            .groupBy(order.id, place.name, uploadedFile.filePath, payment.amount, payment.paymentStatus, payment.approvedAt)
            .orderBy(order.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(order.count())
            .from(order)
            .innerJoin(payment).on(
                payment.orderId.eq(order.id)
                    .and(payment.paymentStatus.in(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED))
            )
            .where(order.memberId.eq(memberId))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
