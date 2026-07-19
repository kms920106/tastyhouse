package com.tastyhouse.core.domain.order.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
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
import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderManagementListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.QOrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.QOrderManagementListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.order.domain.model.QOrder.order;
import static com.tastyhouse.core.domain.order.domain.model.QOrderProduct.orderProduct;
import static com.tastyhouse.core.domain.payment.domain.model.QPayment.payment;

/**
 * {@code shop}은 infrastructure-module로 이동한 {@code ShopJpaEntity}를 가리킨다.
 * core-module은 infrastructure-module을 의존할 수 없어(의존 방향: infrastructure → core)
 * 생성된 Q타입을 import할 수 없으므로, {@link PathBuilder}로 JPA 엔티티명("ShopJpaEntity")을
 * 문자열 참조해 필요한 컬럼만 타입 세이프하게 노출한다.
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private static final PathBuilder<Object> shop = new PathBuilder<>(Object.class, "ShopJpaEntity");
    private static final NumberPath<Long> shopIdCol = shop.getNumber("id", Long.class);
    private static final StringPath shopNameCol = shop.getString("name");
    private static final NumberPath<Long> shopThumbnailImageFileIdCol = shop.getNumber("thumbnailImageFileId", Long.class);

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
                shopNameCol,
                uploadedFile.filePath,
                orderProduct.name.min(),
                orderProduct.id.count().castToNum(Integer.class),
                order.finalAmount,
                payment.paymentStatus,
                payment.approvedAt
            ))
            .from(order)
            .innerJoin(payment).on(paymentJoinCondition)
            .leftJoin(shop).on(shopIdCol.eq(order.shopId))
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(shopThumbnailImageFileIdCol))
            .leftJoin(orderProduct).on(orderProduct.orderId.eq(order.id))
            .where(order.memberId.eq(memberId))
            .groupBy(order.id, shopNameCol, uploadedFile.filePath, order.finalAmount, payment.paymentStatus, payment.approvedAt)
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
    public PageResult<OrderManagementListItemResult> findOrders(OrderSearchCondition condition, PageQuery pageQuery) {
        var paymentJoinCondition = Expressions.numberPath(Long.class, payment, "orderId").eq(order.id);
        if (condition.paymentStatus() != null) {
            paymentJoinCondition = paymentJoinCondition.and(payment.paymentStatus.eq(condition.paymentStatus()));
        }

        List<OrderManagementListItemResult> content = queryFactory
            .select(new QOrderManagementListItemResult(
                order.id,
                order.orderNumber,
                shopNameCol,
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
            .leftJoin(shop).on(shopIdCol.eq(order.shopId))
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
            .groupBy(order.id, order.orderNumber, shopNameCol, order.ordererName, order.orderMethod, order.orderStatus, payment.paymentStatus, order.finalAmount, order.createdAt)
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
