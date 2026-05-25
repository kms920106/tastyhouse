package com.tastyhouse.core.repository.order;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.order.Order;
import com.tastyhouse.core.entity.order.OrderItem;
import com.tastyhouse.core.entity.order.OrderItemOption;
import com.tastyhouse.core.entity.order.OrderStatus;
import com.tastyhouse.core.entity.payment.PaymentStatus;
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
import static com.tastyhouse.core.entity.order.QOrderItemOption.orderItemOption;
import static com.tastyhouse.core.entity.payment.QPayment.payment;
import static com.tastyhouse.core.domain.place.domain.model.QPlace.place;

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
        return Optional.ofNullable(
            queryFactory.selectFrom(order)
                .where(order.orderNumber.eq(orderNumber))
                .fetchOne()
        );
    }

    @Override
    public List<Order> findByMemberIdOrderByCreatedAtDesc(Long memberId) {
        return queryFactory.selectFrom(order)
            .where(order.memberId.eq(memberId))
            .orderBy(order.createdAt.desc())
            .fetch();
    }

    @Override
    public Page<Order> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable) {
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
        return queryFactory.selectFrom(order)
            .where(order.memberId.eq(memberId), order.orderStatus.eq(orderStatus))
            .orderBy(order.createdAt.desc())
            .fetch();
    }

    @Override
    public List<Order> findByPlaceIdOrderByCreatedAtDesc(Long placeId) {
        return queryFactory.selectFrom(order)
            .where(order.placeId.eq(placeId))
            .orderBy(order.createdAt.desc())
            .fetch();
    }

    @Override
    public List<Order> findByPlaceIdAndOrderStatusOrderByCreatedAtDesc(Long placeId, OrderStatus orderStatus) {
        return queryFactory.selectFrom(order)
            .where(order.placeId.eq(placeId), order.orderStatus.eq(orderStatus))
            .orderBy(order.createdAt.desc())
            .fetch();
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

    @Override
    public Page<Order> findCompletedOrCancelledOrdersByMemberId(Long memberId, Pageable pageable) {
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
        List<OrderListItemDto> content = queryFactory
            .select(new QOrderListItemDto(
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
            .innerJoin(payment).on(
                payment.orderId.eq(order.id)
                    .and(payment.paymentStatus.in(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED))
            )
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
        return queryFactory.selectFrom(orderItem)
            .where(orderItem.orderId.eq(orderId))
            .fetch();
    }

    @Override
    public List<OrderItemOption> findOrderItemOptionsByOrderItemId(Long orderItemId) {
        return queryFactory.selectFrom(orderItemOption)
            .where(orderItemOption.orderItemId.eq(orderItemId))
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
