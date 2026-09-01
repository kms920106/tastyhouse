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

/**
 * 주문 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code OrderRepository}/{@code OrderProductRepository}/{@code OrderProductOptionRepository})와
 * 역할이 겹치지 않는다. 소비 모듈(web/admin-api)의 {@code OrderQueryService}가 이 DAO를 주입해 사용한다.
 *
 * <p>소비자별 메서드 분리(공통 지침 패턴 3): 회원 화면용 {@link #findOrders(MemberId, PageQuery)},
 * 관리자 화면용 {@link #findOrders(OrderSearchCondition, PageQuery)} — 이름은 admin 마커 없이 순수
 * 동작명을 쓰고 시그니처(회원 스코프 {@code MemberId} 유무)로 구별한다. 상세 조회는 두 화면이 같은 필드
 * 셋을 쓰므로 {@link #findOrderDetail(OrderId)} 하나를 공유한다.
 *
 *
 * <p>가게 대표 이미지(주문 목록)와 주문 상품 이미지 모두 {@code UPLOADED_FILE}을 join해 얻은 저장 경로를
 * {@link FileUrlResolver}로 표시용 URL까지 변환해 Result에 담는다. 주문 상품은 주문 시점의
 * {@code UPLOADED_FILE.id}를 스냅샷해 두므로(={@code ORDER_PRODUCT.image_file_id}), 이후 상품 대표
 * 이미지가 교체돼도 과거 주문은 주문 당시 이미지를 그대로 보여준다.
 *
 * <p>두 이미지가 같은 테이블을 각각 join하므로 {@link #ORDER_PRODUCT_IMAGE_FILE} 별칭을 따로 둔다 —
 * 기본 별칭 하나로는 두 join이 충돌한다({@code EventQueryDao#findEventDetailById} 선례와 동일).
 */
@Repository
public class OrderQueryDao implements OrderQueryPort, OrderManagementQueryPort {

    /** 주문 상품 이미지용 {@code UPLOADED_FILE} 별칭 — 가게 썸네일 join과 구분한다. */
    private static final QUploadedFileJpaEntity ORDER_PRODUCT_IMAGE_FILE =
        new QUploadedFileJpaEntity("orderProductImageFile");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public OrderQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 내 주문 목록(web-api용) — 결제가 완료·취소된 주문만 최신순으로 보여준다.
     *
     * <p>상품 라인을 join해 첫 상품명과 상품 종류 수를 집계하므로 주문 단위로 {@code groupBy} 한다.
     */
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

    /**
     * 투영된 저장 경로를 표시용 URL로 바꿔 재조립한다. {@code Projections.constructor}가 생성자 직접 투영이라
     * 변환을 투영식에 넣을 수 없어 fetch 직후 호출한다.
     */
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

    /**
     * 주문 관리 목록(admin-api용) — 검색 조건에 맞는 삭제되지 않은 주문을 최신순으로 보여준다.
     *
     * <p>결제 상태 조건은 where가 아니라 join 조건에 실어, 결제가 없는 주문도 결제 상태 필터가 없을 때는
     * 목록에 남도록 한다(기존 동작 보존).
     */
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

    /**
     * 주문 단건 상세 — 헤더에 가게명·가게 전화번호를 join하고, 상품 라인(각 라인의 선택 옵션 포함)과
     * 결제 요약을 별도 조회해 덧붙인다. web-api(내 주문 상세)·admin-api(주문 관리 상세)가 공유한다.
     *
     * <p>회원 스코프 검증은 하지 않는다 — 소유권 검증은 write 경로의 도메인 모델
     * ({@code Order#validateOwnership})이 담당하고, web-api {@code OrderQueryService}가 이 결과의
     * {@code memberId}를 요청 회원과 대조한다.
     */
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

    /**
     * 주문의 상품 라인 목록 — 각 라인의 선택 옵션을 한 번의 조회로 모아 라인별로 배분한다(N+1 회피).
     */
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

    /**
     * 주문의 결제 요약 — 결제가 아직 없으면 {@code null}.
     *
     * <p>{@code PAYMENT.order_id}는 unique 제약이 있어 주문당 결제는 최대 1건이다. 이 불변식이 깨지면
     * (동일 주문에 결제 행 2건) 임의의 한 건을 조용히 고르는 대신 {@code fetchOne}으로 즉시 실패시킨다 —
     * 기존 {@code PaymentRepository#findByOrderId}와 동일한 fail-loud 시맨틱을 유지한다.
     */
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

    /**
     * 투영된 {@code Amount} VO를 경계 타입 {@code Integer}로 풀어 재조립한다.
     *
     * <p>{@code PAYMENT.amount}가 {@code @Convert} 매핑이라 QueryDSL이 {@code SimplePath<Amount>}를
     * 생성하므로 투영은 VO로 받을 수밖에 없고, {@code Projections.constructor}는 생성자 직접 투영이라
     * 변환을 투영식에 넣을 수 없다. 그래서 fetch 직후에 푼다
     * ({@code withResolvedShopThumbnailImageUrl}과 같은 형태).
     *
     * <p>이 언랩이 읽기 계약을 경계 타입으로 유지해, api 모듈이 {@code Amount.value()}를 호출하지
     * 않게 한다(챕터 07 — {@code apiModuleShouldBeDomainModelFree}의 유일한 비-enum 위반이었다).
     */
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

    /**
     * 주문 상품 한 건의 소유·상품 식별 정보 — 리뷰 작성 화면의 접근 판정용이다.
     *
     * <p>주문 상품에서 주문으로 조인해 주문자 회원 ID까지 한 번에 가져온다. 애그리거트 둘
     * ({@code OrderProduct} → {@code Order})을 차례로 로드하던 기존 형태를 한 번의 투영으로 대체한다.
     */
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
            // left join이다 — inner join으로 묶으면 주문이 사라진 주문 상품(ORDER_PRODUCT.order_id에
            // FK 제약이 없어 가능한 상태)이 "주문 상품 없음"으로 뭉뚱그려져, 소비 측이 원래 구분하던
            // ORDER_NOT_FOUND를 낼 수 없다. 주문자 ID가 null인 것으로 그 상태를 구분해 넘긴다.
            .leftJoin(orderJpaEntity).on(orderJpaEntity.id.eq(orderProductJpaEntity.orderId))
            .where(orderProductJpaEntity.id.eq(orderProductId))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 주문의 주문자 회원 ID — 조회 화면의 접근 판정용이다.
     *
     * <p>상태를 바꾸지 않고 식별자만 대조하므로 표현 목적 조회다. 주문 상태를 변경하는 경로의 소유권
     * 검증은 그대로 write 포트가 담당한다.
     */
    @Override
    public Optional<Long> findOrderMemberId(Long orderId) {
        return Optional.ofNullable(queryFactory
            .select(orderJpaEntity.memberId)
            .from(orderJpaEntity)
            .where(orderJpaEntity.id.eq(orderId))
            .fetchOne());
    }

}
