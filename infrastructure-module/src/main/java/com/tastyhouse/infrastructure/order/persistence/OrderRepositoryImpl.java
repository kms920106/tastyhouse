package com.tastyhouse.infrastructure.order.persistence;

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
import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderManagementListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.QOrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.QOrderManagementListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderJpaEntity.orderJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
import static com.tastyhouse.infrastructure.payment.persistence.QPaymentJpaEntity.paymentJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

/**
 * payment 전환 완료 — {@code QPaymentJpaEntity}를 조인한다.
 * {@code paymentJpaEntity.orderId}는 {@link OrderId} VO를 {@code @Convert}로 매핑한 필드라 QueryDSL이
 * VO 타입 path를 생성하므로, {@link Expressions#numberPath}로 raw {@code Long} 컬럼 비교를 우회한다.
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JPAQueryFactory queryFactory;
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return orderJpaRepository.findById(orderId.value()).map(OrderMapper::toDomain);
    }

    @Override
    public PageResult<OrderListItemResult> findOrderListByMemberId(MemberId memberId, PageQuery pageQuery) {
        var paymentJoinCondition = Expressions.numberPath(Long.class, paymentJpaEntity, "orderId")
            .eq(orderJpaEntity.id)
            .and(paymentJpaEntity.paymentStatus.in(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED));

        List<OrderListItemResult> content = queryFactory
            .select(new QOrderListItemResult(
                orderJpaEntity.id,
                shopJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                orderProductJpaEntity.name.min(),
                orderProductJpaEntity.id.count().castToNum(Integer.class),
                orderJpaEntity.finalAmount,
                paymentJpaEntity.paymentStatus,
                paymentJpaEntity.approvedAt
            ))
            .from(orderJpaEntity)
            .innerJoin(paymentJpaEntity).on(paymentJoinCondition)
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(orderJpaEntity.shopId))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
            .leftJoin(orderProductJpaEntity).on(orderProductJpaEntity.orderId.eq(orderJpaEntity.id))
            .where(orderJpaEntity.memberId.eq(memberId))
            .groupBy(orderJpaEntity.id, shopJpaEntity.name, uploadedFileJpaEntity.filePath, orderJpaEntity.finalAmount, paymentJpaEntity.paymentStatus, paymentJpaEntity.approvedAt)
            .orderBy(orderJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(orderJpaEntity.count())
            .from(orderJpaEntity)
            .innerJoin(paymentJpaEntity).on(paymentJoinCondition)
            .where(orderJpaEntity.memberId.eq(memberId))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<OrderManagementListItemResult> findOrders(OrderSearchCondition condition, PageQuery pageQuery) {
        var paymentJoinCondition = Expressions.numberPath(Long.class, paymentJpaEntity, "orderId").eq(orderJpaEntity.id);
        if (condition.paymentStatus() != null) {
            paymentJoinCondition = paymentJoinCondition.and(paymentJpaEntity.paymentStatus.eq(condition.paymentStatus()));
        }

        List<OrderManagementListItemResult> content = queryFactory
            .select(new QOrderManagementListItemResult(
                orderJpaEntity.id,
                orderJpaEntity.orderNumber,
                shopJpaEntity.name,
                orderJpaEntity.ordererName,
                orderJpaEntity.orderMethod,
                orderJpaEntity.orderStatus,
                paymentJpaEntity.paymentStatus,
                orderJpaEntity.finalAmount,
                orderProductJpaEntity.id.count().castToNum(Integer.class),
                orderJpaEntity.createdAt
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
            .groupBy(orderJpaEntity.id, orderJpaEntity.orderNumber, shopJpaEntity.name, orderJpaEntity.ordererName, orderJpaEntity.orderMethod, orderJpaEntity.orderStatus, paymentJpaEntity.paymentStatus, orderJpaEntity.finalAmount, orderJpaEntity.createdAt)
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
    public Order save(Order order) {
        if (order.getId() == null) {
            OrderJpaEntity saved = orderJpaRepository.save(OrderMapper.toEntity(order));
            return OrderMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        OrderJpaEntity entity = orderJpaRepository.findById(order.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문입니다: " + order.getId()));
        OrderMapper.applyChanges(entity, order);
        return OrderMapper.toDomain(entity);
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
}
