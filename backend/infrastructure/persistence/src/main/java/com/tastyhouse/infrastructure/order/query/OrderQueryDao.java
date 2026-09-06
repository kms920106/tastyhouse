package com.tastyhouse.infrastructure.order.query;

import com.tastyhouse.application.order.port.out.OrderProductOwnershipResult;
import com.tastyhouse.application.order.port.out.OrderManagementQueryPort;
import com.tastyhouse.application.order.port.out.OrderQueryPort;
import com.tastyhouse.application.order.port.out.OrderDetailResult;
import com.tastyhouse.application.order.port.out.OrderListItemResult;
import com.tastyhouse.application.order.port.out.OrderManagementListItemResult;
import com.tastyhouse.application.order.port.out.OrderPaymentResult;
import com.tastyhouse.application.order.port.out.OrderProductOptionResult;
import com.tastyhouse.application.order.port.out.OrderProductResult;
import com.tastyhouse.application.order.port.out.OrderSearchCondition;
import com.querydsl.core.types.Projections;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderJpaEntity.orderJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderProductOptionJpaEntity.orderProductOptionJpaEntity;
import static com.tastyhouse.infrastructure.payment.persistence.QPaymentJpaEntity.paymentJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
public class OrderQueryDao implements OrderQueryPort, OrderManagementQueryPort {
    private static final QUploadedFileJpaEntity ORDER_PRODUCT_IMAGE_FILE =
        new QUploadedFileJpaEntity("orderProductImageFile");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public OrderQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<OrderListItemResult> findOrders(MemberId memberId, PageQuery pageQuery) {
        BooleanExpression paymentJoinCondition = paymentJpaEntity.orderId
            .eq(orderJpaEntity.id)
            .and(paymentJpaEntity.paymentStatus.in(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED));

        List<OrderListItemResult> content = queryFactory
            .select(Projections.constructor(OrderListItemResult.class,
                orderJpaEntity.id,
                shopJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                orderProductJpaEntity.name.min(),
                orderProductJpaEntity.id.count().castToNum(Integer.class),
                orderJpaEntity.finalAmount,
                paymentJpaEntity.paymentStatus,
                paymentJpaEntity.approvedAt,
                orderJpaEntity.schedule.scheduledAt
            ))
            .from(orderJpaEntity)
            .innerJoin(paymentJpaEntity).on(paymentJoinCondition)
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(orderJpaEntity.shopId))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
            .leftJoin(orderProductJpaEntity).on(orderProductJpaEntity.orderId.eq(orderJpaEntity.id))
            .where(orderJpaEntity.memberId.eq(memberId.value()))
            .groupBy(
                orderJpaEntity.id,
                shopJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                orderJpaEntity.finalAmount,
                paymentJpaEntity.paymentStatus,
                paymentJpaEntity.approvedAt,
                orderJpaEntity.schedule.scheduledAt
            )
            .orderBy(orderJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedShopThumbnailImageUrl)
            .toList();

        Long total = queryFactory
            .select(orderJpaEntity.count())
            .from(orderJpaEntity)
            .innerJoin(paymentJpaEntity).on(paymentJoinCondition)
            .where(orderJpaEntity.memberId.eq(memberId.value()))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    private OrderListItemResult withResolvedShopThumbnailImageUrl(OrderListItemResult row) {
        return new OrderListItemResult(
            row.id(),
            row.shopName(),
            fileUrlResolver.resolve(row.shopThumbnailImageUrl()),
            row.firstProductName(),
            row.totalItemCount(),
            row.amount(),
            row.paymentStatus(),
            row.paymentDate(),
            row.scheduledAt()
        );
    }

    public PageResult<OrderManagementListItemResult> findOrders(OrderSearchCondition condition, PageQuery pageQuery) {
        BooleanExpression paymentJoinCondition = paymentJpaEntity.orderId.eq(orderJpaEntity.id);
        if (condition.paymentStatus() != null) {
            paymentJoinCondition = paymentJoinCondition.and(paymentJpaEntity.paymentStatus.eq(condition.paymentStatus()));
        }

        List<OrderManagementListItemResult> content = queryFactory
            .select(Projections.constructor(OrderManagementListItemResult.class,
                orderJpaEntity.id,
                orderJpaEntity.orderNumber,
                shopJpaEntity.name,
                orderJpaEntity.ordererName,
                orderJpaEntity.orderMethod,
                orderJpaEntity.orderStatus,
                paymentJpaEntity.paymentStatus,
                orderJpaEntity.finalAmount,
                orderProductJpaEntity.id.count().castToNum(Integer.class),
                orderJpaEntity.createdAt,
                orderJpaEntity.schedule.scheduledAt
            ))
            .from(orderJpaEntity)
            .leftJoin(paymentJpaEntity).on(paymentJoinCondition)
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(orderJpaEntity.shopId))
            .leftJoin(orderProductJpaEntity).on(orderProductJpaEntity.orderId.eq(orderJpaEntity.id))
            .where(
                orderJpaEntity.deleted.isFalse(),
                shopIdEq(condition.shopId()),
                orderStatusEq(condition.orderStatus()),
                orderMethodEq(condition.orderMethod()),
                orderNumberContains(condition.orderNumber()),
                ordererNameContains(condition.ordererName()),
                createdAtGoe(condition.startDate()),
                createdAtLoe(condition.endDate())
            )
            .groupBy(
                orderJpaEntity.id,
                orderJpaEntity.orderNumber,
                shopJpaEntity.name,
                orderJpaEntity.ordererName,
                orderJpaEntity.orderMethod,
                orderJpaEntity.orderStatus,
                paymentJpaEntity.paymentStatus,
                orderJpaEntity.finalAmount,
                orderJpaEntity.createdAt,
                orderJpaEntity.schedule.scheduledAt
            )
            .orderBy(orderJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(orderJpaEntity.countDistinct())
            .from(orderJpaEntity)
            .leftJoin(paymentJpaEntity).on(paymentJoinCondition)
            .where(
                orderJpaEntity.deleted.isFalse(),
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
    public Optional<OrderDetailResult> findOrderDetail(OrderId orderId) {
        OrderDetailResult detail = queryFactory
            .select(Projections.constructor(OrderDetailResult.class,
                orderJpaEntity.id,
                orderJpaEntity.memberId,
                orderJpaEntity.orderNumber,
                orderJpaEntity.orderMethod,
                orderJpaEntity.orderStatus,
                shopJpaEntity.name,
                shopJpaEntity.phoneNumber,
                orderJpaEntity.ordererName,
                orderJpaEntity.ordererPhone,
                orderJpaEntity.ordererEmail,
                orderJpaEntity.totalProductAmount,
                orderJpaEntity.productDiscountAmount,
                orderJpaEntity.couponDiscountAmount,
                orderJpaEntity.pointDiscountAmount,
                orderJpaEntity.totalDiscountAmount,
                orderJpaEntity.cupDepositAmount,
                orderJpaEntity.finalAmount,
                orderJpaEntity.usedPoint,
                orderJpaEntity.earnedPoint,
                orderJpaEntity.createdAt,
                orderJpaEntity.schedule.scheduledAt,
                orderJpaEntity.schedule.scheduledSlotEndAt
            ))
            .from(orderJpaEntity)
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(orderJpaEntity.shopId))
            .where(orderJpaEntity.id.eq(orderId.value()))
            .fetchOne();

        if (detail == null) {
            return Optional.empty();
        }

        return Optional.of(
            detail
                .withOrderProducts(findOrderProducts(orderId))
                .withPayment(findPayment(orderId))
        );
    }

    private List<OrderProductResult> findOrderProducts(OrderId orderId) {
        List<OrderProductResult> orderProducts = queryFactory
            .select(Projections.constructor(OrderProductResult.class,
                orderProductJpaEntity.id,
                orderProductJpaEntity.productId,
                orderProductJpaEntity.name,
                orderProductJpaEntity.priceName,
                ORDER_PRODUCT_IMAGE_FILE.filePath,
                orderProductJpaEntity.quantity,
                orderProductJpaEntity.originalPrice,
                orderProductJpaEntity.discountPrice,
                orderProductJpaEntity.totalOptionPrice,
                orderProductJpaEntity.totalPrice
            ))
            .from(orderProductJpaEntity)
            .leftJoin(ORDER_PRODUCT_IMAGE_FILE)
                .on(ORDER_PRODUCT_IMAGE_FILE.id.eq(orderProductJpaEntity.imageFileId))
            .where(orderProductJpaEntity.orderId.eq(orderId.value()))
            .orderBy(orderProductJpaEntity.id.asc())
            .fetch();

        if (orderProducts.isEmpty()) {
            return orderProducts;
        }

        List<Long> orderProductIds = orderProducts.stream()
            .map(OrderProductResult::orderProductId)
            .toList();

        Map<Long, List<OrderProductOptionResult>> optionsByOrderProductId = queryFactory
            .select(Projections.constructor(OrderProductOptionResult.class,
                orderProductOptionJpaEntity.orderProductId,
                orderProductOptionJpaEntity.id,
                orderProductOptionJpaEntity.optionGroupName,
                orderProductOptionJpaEntity.optionName,
                orderProductOptionJpaEntity.additionalPrice,
                orderProductOptionJpaEntity.optionGroupType,
                orderProductOptionJpaEntity.cupCount,
                orderProductOptionJpaEntity.depositAmount
            ))
            .from(orderProductOptionJpaEntity)
            .where(orderProductOptionJpaEntity.orderProductId.in(orderProductIds))
            .orderBy(orderProductOptionJpaEntity.id.asc())
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(OrderProductOptionResult::orderProductId));

        return orderProducts.stream()
            .map(orderProduct -> orderProduct.withResolvedImageUrl(
                fileUrlResolver.resolve(orderProduct.imageUrl()),
                optionsByOrderProductId.getOrDefault(orderProduct.orderProductId(), List.of())
            ))
            .toList();
    }

    private OrderPaymentResult findPayment(OrderId orderId) {
        PaymentProjection row = queryFactory
            .select(Projections.constructor(PaymentProjection.class,
                paymentJpaEntity.id,
                paymentJpaEntity.paymentMethod,
                paymentJpaEntity.paymentStatus,
                paymentJpaEntity.amount,
                paymentJpaEntity.cardCompany,
                paymentJpaEntity.cardNumber,
                paymentJpaEntity.approvedAt,
                paymentJpaEntity.receiptUrl
            ))
            .from(paymentJpaEntity)
            .where(paymentJpaEntity.orderId.eq(orderId.value()))
            .fetchOne();

        return row == null ? null : withUnwrappedAmount(row);
    }

    private OrderPaymentResult withUnwrappedAmount(PaymentProjection row) {
        return new OrderPaymentResult(
            row.id(),
            row.paymentMethod(),
            row.paymentStatus(),
            row.amount() == null ? null : row.amount().value(),
            row.cardCompany(),
            row.cardNumber(),
            row.approvedAt(),
            row.receiptUrl()
        );
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? orderJpaEntity.shopId.eq(shopId) : null;
    }

    private BooleanExpression orderStatusEq(OrderStatus orderStatus) {
        return orderStatus != null ? orderJpaEntity.orderStatus.eq(orderStatus) : null;
    }

    private BooleanExpression orderMethodEq(OrderMethod orderMethod) {
        return orderMethod != null ? orderJpaEntity.orderMethod.eq(orderMethod) : null;
    }

    private BooleanExpression orderNumberContains(String orderNumber) {
        return orderNumber != null ? orderJpaEntity.orderNumber.containsIgnoreCase(orderNumber) : null;
    }

    private BooleanExpression ordererNameContains(String ordererName) {
        return ordererName != null ? orderJpaEntity.ordererName.containsIgnoreCase(ordererName) : null;
    }

    private BooleanExpression createdAtGoe(LocalDateTime startDate) {
        return startDate != null ? orderJpaEntity.createdAt.goe(startDate) : null;
    }

    private BooleanExpression createdAtLoe(LocalDateTime endDate) {
        return endDate != null ? orderJpaEntity.createdAt.loe(endDate) : null;
    }

    @Override
    public Optional<OrderProductOwnershipResult> findOrderProductOwnership(Long orderProductId) {
        OrderProductOwnershipResult result = queryFactory
            .select(Projections.constructor(OrderProductOwnershipResult.class,
                orderProductJpaEntity.orderId,
                orderJpaEntity.memberId,
                orderProductJpaEntity.productId,
                orderJpaEntity.orderMethod.stringValue()
            ))
            .from(orderProductJpaEntity)
            .leftJoin(orderJpaEntity).on(orderJpaEntity.id.eq(orderProductJpaEntity.orderId))
            .where(orderProductJpaEntity.id.eq(orderProductId))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Long> findOrderMemberId(Long orderId) {
        return Optional.ofNullable(queryFactory
            .select(orderJpaEntity.memberId)
            .from(orderJpaEntity)
            .where(orderJpaEntity.id.eq(orderId))
            .fetchOne());
    }
}
