package com.tastyhouse.core.repository.order;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.order.Order;
import com.tastyhouse.core.entity.order.OrderItem;
import com.tastyhouse.core.entity.order.OrderItemOption;
import com.tastyhouse.core.entity.order.OrderStatus;
import com.tastyhouse.core.entity.order.QOrder;
import com.tastyhouse.core.entity.order.QOrderItem;
import com.tastyhouse.core.entity.order.QOrderItemOption;
import com.tastyhouse.core.entity.payment.PaymentStatus;
import com.tastyhouse.core.entity.payment.QPayment;
import com.tastyhouse.core.entity.payment.dto.OrderListItemDto;
import com.tastyhouse.core.entity.payment.dto.QOrderListItemDto;
import com.tastyhouse.core.entity.place.QPlace;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JPAQueryFactory queryFactory;
    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;
    private final OrderItemOptionJpaRepository orderItemOptionJpaRepository;

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJpaRepository.findById(orderId);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        QOrder order = QOrder.order;
        return Optional.ofNullable(
            queryFactory.selectFrom(order)
                .where(order.orderNumber.eq(orderNumber))
                .fetchOne()
        );
    }

    @Override
    public List<Order> findByMemberIdOrderByCreatedAtDesc(Long memberId) {
        QOrder order = QOrder.order;
        return queryFactory.selectFrom(order)
            .where(order.memberId.eq(memberId))
            .orderBy(order.createdAt.desc())
            .fetch();
    }

    @Override
    public Page<Order> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable) {
        QOrder order = QOrder.order;
        List<Order> content = queryFactory.selectFrom(order)
            .where(order.memberId.eq(memberId))
            .orderBy(order.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
        long total = queryFactory.select(order.count()).from(order)
            .where(order.memberId.eq(memberId)).fetchOne();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Order> findByMemberIdAndOrderStatusOrderByCreatedAtDesc(Long memberId, OrderStatus orderStatus) {
        QOrder order = QOrder.order;
        return queryFactory.selectFrom(order)
            .where(order.memberId.eq(memberId), order.orderStatus.eq(orderStatus))
            .orderBy(order.createdAt.desc())
            .fetch();
    }

    @Override
    public List<Order> findByPlaceIdOrderByCreatedAtDesc(Long placeId) {
        QOrder order = QOrder.order;
        return queryFactory.selectFrom(order)
            .where(order.placeId.eq(placeId))
            .orderBy(order.createdAt.desc())
            .fetch();
    }

    @Override
    public List<Order> findByPlaceIdAndOrderStatusOrderByCreatedAtDesc(Long placeId, OrderStatus orderStatus) {
        QOrder order = QOrder.order;
        return queryFactory.selectFrom(order)
            .where(order.placeId.eq(placeId), order.orderStatus.eq(orderStatus))
            .orderBy(order.createdAt.desc())
            .fetch();
    }

    @Override
    public boolean existsByOrderNumber(String orderNumber) {
        QOrder order = QOrder.order;
        return queryFactory.selectOne().from(order)
            .where(order.orderNumber.eq(orderNumber))
            .fetchFirst() != null;
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Page<Order> findCompletedOrCancelledOrdersByMemberId(Long memberId, Pageable pageable) {
        QOrder order = QOrder.order;
        QPayment payment = QPayment.payment;

        List<Order> content = queryFactory
            .selectFrom(order)
            .innerJoin(payment).on(
                payment.orderId.eq(order.id)
                    .and(payment.paymentStatus.in(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED))
            )
            .where(order.memberId.eq(memberId))
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

    @Override
    public Page<OrderListItemDto> findOrderListByMemberId(Long memberId, Pageable pageable) {
        QOrder order = QOrder.order;
        QPayment payment = QPayment.payment;
        QPlace place = QPlace.place;
        QOrderItem orderItem = QOrderItem.orderItem;
        QOrderItem subOrderItem = new QOrderItem("subOrderItem");

        List<OrderListItemDto> content = queryFactory
            .select(new QOrderListItemDto(
                order.id,
                place.name,
                place.thumbnailImageUrl,
                JPAExpressions
                    .select(subOrderItem.productName)
                    .from(subOrderItem)
                    .where(subOrderItem.orderId.eq(order.id)
                        .and(subOrderItem.id.eq(
                            JPAExpressions
                                .select(subOrderItem.id.min())
                                .from(subOrderItem)
                                .where(subOrderItem.orderId.eq(order.id))
                        ))),
                JPAExpressions
                    .select(orderItem.id.count().intValue())
                    .from(orderItem)
                    .where(orderItem.orderId.eq(order.id)),
                order.finalAmount,
                payment.paymentStatus,
                payment.approvedAt
            ))
            .from(order)
            .innerJoin(payment).on(
                payment.orderId.eq(order.id)
                    .and(payment.paymentStatus.in(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED))
            )
            .leftJoin(place).on(place.id.eq(order.placeId))
            .where(order.memberId.eq(memberId))
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

    @Override
    public Optional<OrderItem> findOrderItemById(Long orderItemId) {
        return orderItemJpaRepository.findById(orderItemId);
    }

    @Override
    public List<OrderItem> findOrderItemsByOrderId(Long orderId) {
        QOrderItem orderItem = QOrderItem.orderItem;
        return queryFactory.selectFrom(orderItem)
            .where(orderItem.orderId.eq(orderId))
            .fetch();
    }

    @Override
    public List<OrderItemOption> findOrderItemOptionsByOrderItemId(Long orderItemId) {
        QOrderItemOption option = QOrderItemOption.orderItemOption;
        return queryFactory.selectFrom(option)
            .where(option.orderItemId.eq(orderItemId))
            .fetch();
    }

    @Override
    public OrderItem saveOrderItem(OrderItem orderItem) {
        return orderItemJpaRepository.save(orderItem);
    }

    @Override
    public void saveOrderItemOption(OrderItemOption orderItemOption) {
        orderItemOptionJpaRepository.save(orderItemOption);
    }
}
