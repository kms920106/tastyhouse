package com.tastyhouse.core.domain.order.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.model.OrderStatus;
import com.tastyhouse.core.domain.order.domain.repository.OrderRepository;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.domain.order.application.dto.OrderSearchCondition;
import com.tastyhouse.core.domain.order.application.dto.result.OrderAdminListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.QOrderAdminListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.QOrderListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.order.domain.model.QOrder.order;
import static com.tastyhouse.core.domain.order.domain.model.QOrderProduct.orderProduct;
import static com.tastyhouse.core.domain.payment.domain.model.QPayment.payment;
import static com.tastyhouse.core.domain.shop.domain.model.QShop.shop;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JPAQueryFactory queryFactory;
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return orderJpaRepository.findById(orderId.value());
    }

    @Override
    public PageResult<OrderListItemResult> findOrderListByMemberId(MemberId memberId, PageQuery pageQuery) {
        var paymentJoinCondition = Expressions.numberPath(Long.class, payment, "orderId")
            .eq(order.id)
            .and(payment.paymentStatus.in(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED));

        List<OrderListItemResult> content = queryFactory
            .select(new QOrderListItemResult(
                order.id,
                shop.name,
                uploadedFile.filePath,
                orderProduct.name.min(),
                orderProduct.id.count().castToNum(Integer.class),
                order.finalAmount,
                payment.paymentStatus,
                payment.approvedAt
            ))
            .from(order)
            .innerJoin(payment).on(paymentJoinCondition)
            .leftJoin(shop).on(shop.id.eq(order.shopId))
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(shop.thumbnailImageFileId))
            .leftJoin(orderProduct).on(orderProduct.orderId.eq(order.id))
            .where(order.memberId.eq(memberId))
            .groupBy(order.id, shop.name, uploadedFile.filePath, order.finalAmount, payment.paymentStatus, payment.approvedAt)
            .orderBy(order.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(order.count())
            .from(order)
            .innerJoin(payment).on(paymentJoinCondition)
            .where(order.memberId.eq(memberId))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<OrderAdminListItemResult> findOrders(OrderSearchCondition condition, PageQuery pageQuery) {
        var paymentJoinCondition = Expressions.numberPath(Long.class, payment, "orderId").eq(order.id);
        if (condition.paymentStatus() != null) {
            paymentJoinCondition = paymentJoinCondition.and(payment.paymentStatus.eq(condition.paymentStatus()));
        }

        List<OrderAdminListItemResult> content = queryFactory
            .select(new QOrderAdminListItemResult(
                order.id,
                order.orderNumber,
                shop.name,
                order.ordererName,
                order.orderMethod,
                order.orderStatus,
                payment.paymentStatus,
                order.finalAmount,
                orderProduct.id.count().castToNum(Integer.class),
                order.createdAt
            ))
            .from(order)
            .leftJoin(payment).on(paymentJoinCondition)
            .leftJoin(shop).on(shop.id.eq(order.shopId))
            .leftJoin(orderProduct).on(orderProduct.orderId.eq(order.id))
            .where(
                order.deleted.isFalse(),
                shopIdEq(condition.shopId()),
                orderStatusEq(condition.orderStatus()),
                orderMethodEq(condition.orderMethod()),
                orderNumberContains(condition.orderNumber()),
                ordererNameContains(condition.ordererName()),
                createdAtGoe(condition.startDate()),
                createdAtLoe(condition.endDate())
            )
            .groupBy(order.id, order.orderNumber, shop.name, order.ordererName, order.orderMethod, order.orderStatus, payment.paymentStatus, order.finalAmount, order.createdAt)
            .orderBy(order.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(order.countDistinct())
            .from(order)
            .leftJoin(payment).on(paymentJoinCondition)
            .where(
                order.deleted.isFalse(),
                shopIdEq(condition.shopId()),
                orderStatusEq(condition.orderStatus()),
                orderMethodEq(condition.orderMethod()),
                orderNumberContains(condition.orderNumber()),
                ordererNameContains(condition.ordererName()),
                createdAtGoe(condition.startDate()),
                createdAtLoe(condition.endDate())
            )
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? order.shopId.eq(shopId) : null;
    }

    private BooleanExpression orderStatusEq(OrderStatus orderStatus) {
        return orderStatus != null ? order.orderStatus.eq(orderStatus) : null;
    }

    private BooleanExpression orderMethodEq(OrderMethod orderMethod) {
        return orderMethod != null ? order.orderMethod.eq(orderMethod) : null;
    }

    private BooleanExpression orderNumberContains(String orderNumber) {
        return orderNumber != null ? order.orderNumber.containsIgnoreCase(orderNumber) : null;
    }

    private BooleanExpression ordererNameContains(String ordererName) {
        return ordererName != null ? order.ordererName.containsIgnoreCase(ordererName) : null;
    }

    private BooleanExpression createdAtGoe(LocalDateTime startDate) {
        return startDate != null ? order.createdAt.goe(startDate) : null;
    }

    private BooleanExpression createdAtLoe(LocalDateTime endDate) {
        return endDate != null ? order.createdAt.loe(endDate) : null;
    }
}
