package com.tastyhouse.core.domain.order.infrastructure.persistence;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.QOrderListItemResult;
import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.repository.OrderRepository;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.order.domain.model.QOrder.order;
import static com.tastyhouse.core.domain.order.domain.model.QOrderItem.orderItem;
import static com.tastyhouse.core.domain.payment.domain.model.QPayment.payment;
import static com.tastyhouse.core.domain.place.domain.model.QPlace.place;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JPAQueryFactory queryFactory;
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJpaRepository.findById(orderId);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return Optional.ofNullable(
            queryFactory.selectFrom(order)
                .where(order.orderNumber.eq(orderNumber))
                .fetchOne()
        );
    }

    @Override
    public Page<OrderListItemResult> findOrderListByMemberId(Long memberId, Pageable pageable) {
        var paymentJoinCondition = Expressions.numberPath(Long.class, payment, "orderId")
            .eq(order.id)
            .and(payment.paymentStatus.in(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED));

        List<OrderListItemResult> content = queryFactory
            .select(new QOrderListItemResult(
                order.id,
                place.name,
                uploadedFile.filePath,
                orderItem.productName.min(),
                orderItem.id.count().castToNum(Integer.class),
                order.finalAmount,
                payment.paymentStatus,
                payment.approvedAt
            ))
            .from(order)
            .innerJoin(payment).on(paymentJoinCondition)
            .leftJoin(place).on(place.id.eq(order.placeId))
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(place.thumbnailImageFileId))
            .leftJoin(orderItem).on(orderItem.orderId.eq(order.id))
            .where(order.memberId.eq(memberId))
            .groupBy(order.id, place.name, uploadedFile.filePath, order.finalAmount, payment.paymentStatus, payment.approvedAt)
            .orderBy(order.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(order.count())
            .from(order)
            .innerJoin(payment).on(paymentJoinCondition)
            .where(order.memberId.eq(memberId))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public boolean existsByOrderNumber(String orderNumber) {
        return queryFactory.selectOne().from(order)
            .where(order.orderNumber.eq(orderNumber))
            .fetchFirst() != null;
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }
}
