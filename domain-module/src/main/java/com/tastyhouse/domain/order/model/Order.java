package com.tastyhouse.domain.order.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 주문 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code OrderJpaEntity} + {@code OrderMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code OrderRepository#save}를
 * 호출해야 한다.
 */
public class Order {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId memberId; // 주문자 회원 ID
    private final ShopId shopId; // 주문 대상 가게 ID
    private final String orderNumber; // 주문 번호 (unique)
    private final OrderMethod orderMethod; // 주문 방식
    private OrderStatus orderStatus; // 주문 상태
    private final String ordererName; // 주문자 이름
    private final String ordererPhone; // 주문자 전화번호
    private final String ordererEmail; // 주문자 이메일
    private Integer totalProductAmount; // 상품 금액 합계
    private Integer productDiscountAmount; // 상품 할인 금액
    private Integer couponDiscountAmount; // 쿠폰 할인 금액
    private Integer pointDiscountAmount; // 포인트 할인 금액
    private Integer totalDiscountAmount; // 총 할인 금액
    private Integer finalAmount; // 최종 결제 금액
    private MemberCouponId memberCouponId; // 사용한 회원 쿠폰 ID
    private Integer usedPoint; // 사용한 포인트
    private Integer earnedPoint; // 적립된 포인트
    private boolean deleted; // 삭제 여부 (true: 삭제됨, Soft Delete)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private Order(
        Long id,
        MemberId memberId,
        ShopId shopId,
        String orderNumber,
        OrderMethod orderMethod,
        OrderStatus orderStatus,
        String ordererName,
        String ordererPhone,
        String ordererEmail,
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
        MemberCouponId memberCouponId,
        Integer usedPoint,
        Integer earnedPoint,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.shopId = shopId;
        this.orderNumber = orderNumber;
        this.orderMethod = orderMethod;
        this.orderStatus = orderStatus;
        this.ordererName = ordererName;
        this.ordererPhone = ordererPhone;
        this.ordererEmail = ordererEmail;
        this.totalProductAmount = totalProductAmount;
        this.productDiscountAmount = productDiscountAmount;
        this.couponDiscountAmount = couponDiscountAmount;
        this.pointDiscountAmount = pointDiscountAmount;
        this.totalDiscountAmount = totalDiscountAmount;
        this.finalAmount = finalAmount;
        this.memberCouponId = memberCouponId;
        this.usedPoint = usedPoint;
        this.earnedPoint = earnedPoint;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 주문을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     *
     * <p>금액 정합 불변식({@link #validateAmountConsistency})을 {@code updateAmounts}와 동일하게 강제한다 —
     * 같은 규칙을 두 벌로 구현하지 않도록 검증 메서드 하나를 양쪽에서 호출한다. null 금액은 0으로 정규화한
     * 뒤 검증하므로, 전부 생략한 호출(모두 0)은 그대로 통과한다.
     *
     * <p>{@link #reconstitute}는 이 검증을 <b>거치지 않는다</b> — 기존 DB 데이터가 새 불변식을 위반해도
     * 로드는 가능해야 하기 때문이다(신규 생성만 막고 기존 데이터는 읽힌다).
     */
    public static Order of(
        MemberId memberId,
        ShopId shopId,
        String orderNumber,
        OrderMethod orderMethod,
        OrderStatus orderStatus,
        String ordererName,
        String ordererPhone,
        String ordererEmail,
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
        MemberCouponId memberCouponId,
        Integer usedPoint,
        Integer earnedPoint
    ) {
        int normalizedTotalProduct = orZero(totalProductAmount);
        int normalizedProductDiscount = orZero(productDiscountAmount);
        int normalizedCouponDiscount = orZero(couponDiscountAmount);
        int normalizedPointDiscount = orZero(pointDiscountAmount);
        int normalizedTotalDiscount = orZero(totalDiscountAmount);
        int normalizedFinalAmount = orZero(finalAmount);
        int normalizedUsedPoint = orZero(usedPoint);

        validateAmountConsistency(
            normalizedTotalProduct,
            normalizedProductDiscount,
            normalizedCouponDiscount,
            normalizedPointDiscount,
            normalizedTotalDiscount,
            normalizedFinalAmount,
            normalizedUsedPoint
        );

        return new Order(
            null,
            memberId,
            shopId,
            orderNumber,
            orderMethod,
            orderStatus != null ? orderStatus : OrderStatus.PENDING,
            ordererName,
            ordererPhone,
            ordererEmail,
            normalizedTotalProduct,
            normalizedProductDiscount,
            normalizedCouponDiscount,
            normalizedPointDiscount,
            normalizedTotalDiscount,
            normalizedFinalAmount,
            memberCouponId,
            normalizedUsedPoint,
            orZero(earnedPoint),
            false,
            null,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     *
     * <p><b>{@link #of}와 달리 금액 정합 검증을 하지 않는다</b> — 불변식 도입 이전에 저장된 기존 데이터가
     * 새 규칙을 위반하더라도 로드는 가능해야 하기 때문이다(검증은 신규 생성 경로에서만 강제한다).
     */
    public static Order reconstitute(
        Long id,
        MemberId memberId,
        ShopId shopId,
        String orderNumber,
        OrderMethod orderMethod,
        OrderStatus orderStatus,
        String ordererName,
        String ordererPhone,
        String ordererEmail,
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
        MemberCouponId memberCouponId,
        Integer usedPoint,
        Integer earnedPoint,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Order(
            id,
            memberId,
            shopId,
            orderNumber,
            orderMethod,
            orderStatus,
            ordererName,
            ordererPhone,
            ordererEmail,
            totalProductAmount,
            productDiscountAmount,
            couponDiscountAmount,
            pointDiscountAmount,
            totalDiscountAmount,
            finalAmount,
            memberCouponId,
            usedPoint,
            earnedPoint,
            deleted,
            createdAt,
            updatedAt
        );
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public OrderMethod getOrderMethod() {
        return this.orderMethod;
    }

    public OrderStatus getOrderStatus() {
        return this.orderStatus;
    }

    public String getOrdererName() {
        return this.ordererName;
    }

    public String getOrdererPhone() {
        return this.ordererPhone;
    }

    public String getOrdererEmail() {
        return this.ordererEmail;
    }

    public Integer getTotalProductAmount() {
        return this.totalProductAmount;
    }

    public Integer getProductDiscountAmount() {
        return this.productDiscountAmount;
    }

    public Integer getCouponDiscountAmount() {
        return this.couponDiscountAmount;
    }

    public Integer getPointDiscountAmount() {
        return this.pointDiscountAmount;
    }

    public Integer getTotalDiscountAmount() {
        return this.totalDiscountAmount;
    }

    public Integer getFinalAmount() {
        return this.finalAmount;
    }

    public MemberCouponId getMemberCouponId() {
        return this.memberCouponId;
    }

    public Integer getUsedPoint() {
        return this.usedPoint;
    }

    public Integer getEarnedPoint() {
        return this.earnedPoint;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public OrderId getOrderId() {
        return OrderId.of(this.id);
    }

    public void validateOwnership(MemberId memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    /**
     * 결제 승인 확정: PENDING -&gt; CONFIRMED.
     *
     * <p>확정 불가 상태는 사유별로 구분된 예외를 던져 안내 메시지를 세분화한다
     * ({@code Reservation#cancel} 선례).
     */
    public void confirm() {
        transitionTo(OrderStatus.CONFIRMED);
    }

    /**
     * 결제 취소: PENDING|CONFIRMED -&gt; CANCELLED.
     */
    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    /**
     * 관리자 수동 상태 변경 — 전이 테이블({@link OrderStatus#canTransitionTo})을 통과하는 전이만 허용한다.
     *
     * <p><b>폐기하지 않고 남긴 근거</b>: {@code PREPARING}·{@code COMPLETED}(조리 파이프라인)에 도달하는
     * 경로가 admin-api의 {@code PATCH /api/orders/v1/{id}/status} 하나뿐이라, 이 메서드를 폐기하면 주문이
     * {@code CONFIRMED}에서 더 진행되지 못한다. 따라서 "무검증 임의 전이"라는 구멍만 막고
     * (기존에는 어떤 전이든 무조건 대입했다) 메서드 자체는 전이 테이블 검증을 태워 유지한다.
     */
    public void changeStatus(OrderStatus status) {
        transitionTo(status);
    }

    /**
     * 전이 테이블을 검증한 뒤 상태를 바꾼다. 불가 전이는 현재 상태별로 구분된 에러 코드로 실패시킨다.
     */
    private void transitionTo(OrderStatus target) {
        if (this.orderStatus.canTransitionTo(target)) {
            this.orderStatus = target;
            return;
        }
        throw new BusinessException(resolveTransitionErrorCode());
    }

    /**
     * 전이가 거부된 현재 상태에 맞는 에러 코드를 고른다 — 종결·진행 상태는 사용자에게 사유를 그대로 안내할 수
     * 있도록 구분하고, 그 밖의 잘못된 전이는 일반 코드로 묶는다.
     */
    private ErrorCode resolveTransitionErrorCode() {
        return switch (this.orderStatus) {
            case CANCELLED -> ErrorCode.ORDER_ALREADY_CANCELLED;
            case COMPLETED -> ErrorCode.ORDER_ALREADY_COMPLETED;
            case PREPARING -> ErrorCode.ORDER_ALREADY_PREPARING;
            case PENDING, CONFIRMED -> ErrorCode.ORDER_INVALID_STATUS_TRANSITION;
        };
    }

    public void delete() {
        this.deleted = true;
    }

    /**
     * 확정된 주문 금액을 반영한다 — 애그리거트 불변식으로 금액 정합을 스스로 검증한다.
     *
     * <p>검증 항목: 모든 금액 음수 금지, {@code totalDiscountAmount == productDiscount + couponDiscount +
     * pointDiscount}, {@code finalAmount == totalProductAmount - totalDiscountAmount}.
     *
     * <p>{@code OrderPlacementService#validateAmounts}의 검증과 역할이 다르다 — 그쪽은 "클라이언트가 보낸
     * 금액과 서버 계산이 같은지" 대조(위조 방지)이고, 이쪽은 "저장되는 금액 자체가 성립하는지"를 보는
     * 애그리거트 불변식이다. 따라서 클라이언트 경로를 거치지 않는 호출에도 정합이 보장된다.
     */
    public void updateAmounts(
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
        MemberCouponId memberCouponId,
        Integer usedPoint
    ) {
        int normalizedTotalProduct = orZero(totalProductAmount);
        int normalizedProductDiscount = orZero(productDiscountAmount);
        int normalizedCouponDiscount = orZero(couponDiscountAmount);
        int normalizedPointDiscount = orZero(pointDiscountAmount);
        int normalizedTotalDiscount = orZero(totalDiscountAmount);
        int normalizedFinalAmount = orZero(finalAmount);
        int normalizedUsedPoint = orZero(usedPoint);

        validateAmountConsistency(
            normalizedTotalProduct,
            normalizedProductDiscount,
            normalizedCouponDiscount,
            normalizedPointDiscount,
            normalizedTotalDiscount,
            normalizedFinalAmount,
            normalizedUsedPoint
        );

        this.totalProductAmount = normalizedTotalProduct;
        this.productDiscountAmount = normalizedProductDiscount;
        this.couponDiscountAmount = normalizedCouponDiscount;
        this.pointDiscountAmount = normalizedPointDiscount;
        this.totalDiscountAmount = normalizedTotalDiscount;
        this.finalAmount = normalizedFinalAmount;
        this.memberCouponId = memberCouponId;
        this.usedPoint = normalizedUsedPoint;
    }

    /**
     * 금액 정합 불변식을 검증한다 — 호출부가 null을 0으로 정규화한 값을 넘긴다.
     *
     * <p>검증과 저장이 같은 정규화 값을 쓰도록 호출부({@code of}·{@code updateAmounts})에서 한 번만
     * 정규화한다 — 검증만 null을 0으로 보고 저장은 raw null을 넣으면, 부분 null 입력이 검증을 통과한 뒤
     * 불변식을 위반하는 상태로 저장된다.
     *
     * <p>인스턴스 상태를 읽지 않으므로 {@code static}이다 — 그래야 신규 생성 경로({@code of})와 변경
     * 경로({@code updateAmounts})가 <b>같은 검증 한 벌</b>을 공유할 수 있다.
     */
    private static void validateAmountConsistency(
        int totalProductAmount,
        int productDiscountAmount,
        int couponDiscountAmount,
        int pointDiscountAmount,
        int totalDiscountAmount,
        int finalAmount,
        int usedPoint
    ) {
        if (totalProductAmount < 0 || productDiscountAmount < 0 || couponDiscountAmount < 0
            || pointDiscountAmount < 0 || totalDiscountAmount < 0 || finalAmount < 0 || usedPoint < 0) {
            throw new BusinessException(ErrorCode.ORDER_AMOUNT_NEGATIVE);
        }

        int discountSum = productDiscountAmount + couponDiscountAmount + pointDiscountAmount;
        if (totalDiscountAmount != discountSum) {
            throw new BusinessException(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT,
                ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT.getDefaultMessage()
                    + " 총 할인: " + totalDiscountAmount + ", 항목 합: " + discountSum);
        }

        if (finalAmount != totalProductAmount - totalDiscountAmount) {
            throw new BusinessException(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT,
                ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT.getDefaultMessage()
                    + " 결제 금액: " + finalAmount
                    + ", 상품 금액 - 총 할인: " + (totalProductAmount - totalDiscountAmount));
        }
    }

    private static int orZero(Integer value) {
        return value != null ? value : 0;
    }

    public void updateEarnedPoint(Integer earnedPoint) {
        this.earnedPoint = earnedPoint;
    }
}
