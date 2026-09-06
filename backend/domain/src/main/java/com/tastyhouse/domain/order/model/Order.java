package com.tastyhouse.domain.order.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderDeliveryDestination;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class Order {
    private final Long id;
    private final MemberId memberId;
    private final ShopId shopId;
    private final String orderNumber;
    private final OrderMethod orderMethod;
    private OrderStatus orderStatus;
    private final String ordererName;
    private final String ordererPhone;
    private final String ordererEmail;
    private Integer totalProductAmount;
    private Integer productDiscountAmount;
    private Integer couponDiscountAmount;
    private Integer pointDiscountAmount;
    private Integer totalDiscountAmount;
    private Integer deliveryTipAmount;

    private Integer cupDepositAmount;
    private Integer finalAmount;
    private OrderDeliveryDestination deliveryDestination;
    private OrderSchedule schedule;
    private MemberCouponId memberCouponId;
    private Integer usedPoint;
    private Integer earnedPoint;
    private boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

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
        Integer deliveryTipAmount,
        Integer cupDepositAmount,
        Integer finalAmount,
        OrderDeliveryDestination deliveryDestination,
        OrderSchedule schedule,
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
        this.deliveryTipAmount = deliveryTipAmount;
        this.cupDepositAmount = cupDepositAmount;
        this.finalAmount = finalAmount;
        this.deliveryDestination = deliveryDestination;
        this.schedule = schedule;
        this.memberCouponId = memberCouponId;
        this.usedPoint = usedPoint;
        this.earnedPoint = earnedPoint;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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
        Integer deliveryTipAmount,
        Integer cupDepositAmount,
        Integer finalAmount,
        OrderDeliveryDestination deliveryDestination,
        OrderSchedule schedule,
        MemberCouponId memberCouponId,
        Integer usedPoint,
        Integer earnedPoint
    ) {
        int normalizedTotalProduct = orZero(totalProductAmount);
        int normalizedProductDiscount = orZero(productDiscountAmount);
        int normalizedCouponDiscount = orZero(couponDiscountAmount);
        int normalizedPointDiscount = orZero(pointDiscountAmount);
        int normalizedTotalDiscount = orZero(totalDiscountAmount);
        int normalizedDeliveryTip = orZero(deliveryTipAmount);
        int normalizedCupDeposit = orZero(cupDepositAmount);
        int normalizedFinalAmount = orZero(finalAmount);
        int normalizedUsedPoint = orZero(usedPoint);

        validateAmountConsistency(
            normalizedTotalProduct,
            normalizedProductDiscount,
            normalizedCouponDiscount,
            normalizedPointDiscount,
            normalizedTotalDiscount,
            normalizedDeliveryTip,
            normalizedCupDeposit,
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
            normalizedDeliveryTip,
            normalizedCupDeposit,
            normalizedFinalAmount,
            deliveryDestination != null ? deliveryDestination : OrderDeliveryDestination.none(),
            schedule != null ? schedule : OrderSchedule.none(),
            memberCouponId,
            normalizedUsedPoint,
            orZero(earnedPoint),
            false,
            null,
            null
        );
    }

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
        Integer deliveryTipAmount,
        Integer cupDepositAmount,
        Integer finalAmount,
        OrderDeliveryDestination deliveryDestination,
        OrderSchedule schedule,
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
            deliveryTipAmount,
            cupDepositAmount,
            finalAmount,
            deliveryDestination,
            schedule,
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

    public Integer getDeliveryTipAmount() {
        return this.deliveryTipAmount;
    }

    public Integer getFinalAmount() {
        return this.finalAmount;
    }

    public Integer getCupDepositAmount() {
        return this.cupDepositAmount;
    }

    public OrderDeliveryDestination getDeliveryDestination() {
        return this.deliveryDestination;
    }

    public OrderSchedule getSchedule() {
        return this.schedule;
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

    public void confirm() {
        transitionTo(OrderStatus.CONFIRMED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    public void changeStatus(OrderStatus status) {
        transitionTo(status);
    }

    private void transitionTo(OrderStatus target) {
        if (this.orderStatus.canTransitionTo(target)) {
            this.orderStatus = target;
            return;
        }
        throw new BusinessException(resolveTransitionErrorCode());
    }

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

    public void updateAmounts(
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer deliveryTipAmount,
        Integer cupDepositAmount,
        Integer finalAmount,
        OrderDeliveryDestination deliveryDestination,
        OrderSchedule schedule,
        MemberCouponId memberCouponId,
        Integer usedPoint
    ) {
        int normalizedTotalProduct = orZero(totalProductAmount);
        int normalizedProductDiscount = orZero(productDiscountAmount);
        int normalizedCouponDiscount = orZero(couponDiscountAmount);
        int normalizedPointDiscount = orZero(pointDiscountAmount);
        int normalizedTotalDiscount = orZero(totalDiscountAmount);
        int normalizedDeliveryTip = orZero(deliveryTipAmount);
        int normalizedCupDeposit = orZero(cupDepositAmount);
        int normalizedFinalAmount = orZero(finalAmount);
        int normalizedUsedPoint = orZero(usedPoint);

        validateAmountConsistency(
            normalizedTotalProduct,
            normalizedProductDiscount,
            normalizedCouponDiscount,
            normalizedPointDiscount,
            normalizedTotalDiscount,
            normalizedDeliveryTip,
            normalizedCupDeposit,
            normalizedFinalAmount,
            normalizedUsedPoint
        );

        this.totalProductAmount = normalizedTotalProduct;
        this.productDiscountAmount = normalizedProductDiscount;
        this.couponDiscountAmount = normalizedCouponDiscount;
        this.pointDiscountAmount = normalizedPointDiscount;
        this.totalDiscountAmount = normalizedTotalDiscount;
        this.deliveryTipAmount = normalizedDeliveryTip;
        this.cupDepositAmount = normalizedCupDeposit;
        this.finalAmount = normalizedFinalAmount;
        this.deliveryDestination = deliveryDestination != null ? deliveryDestination : OrderDeliveryDestination.none();
        this.schedule = schedule != null ? schedule : OrderSchedule.none();
        this.memberCouponId = memberCouponId;
        this.usedPoint = normalizedUsedPoint;
    }

    private static void validateAmountConsistency(
        int totalProductAmount,
        int productDiscountAmount,
        int couponDiscountAmount,
        int pointDiscountAmount,
        int totalDiscountAmount,
        int deliveryTipAmount,
        int cupDepositAmount,
        int finalAmount,
        int usedPoint
    ) {
        if (totalProductAmount < 0 || productDiscountAmount < 0 || couponDiscountAmount < 0
            || pointDiscountAmount < 0 || totalDiscountAmount < 0 || deliveryTipAmount < 0
            || cupDepositAmount < 0 || finalAmount < 0 || usedPoint < 0) {
            throw new BusinessException(ErrorCode.ORDER_AMOUNT_NEGATIVE);
        }

        int discountSum = productDiscountAmount + couponDiscountAmount + pointDiscountAmount;
        if (totalDiscountAmount != discountSum) {
            throw new BusinessException(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT,
                ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT.getDefaultMessage()
                    + " 총 할인: " + totalDiscountAmount + ", 항목 합: " + discountSum);
        }

        int expectedFinal = totalProductAmount - totalDiscountAmount + deliveryTipAmount + cupDepositAmount;
        if (finalAmount != expectedFinal) {
            throw new BusinessException(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT,
                ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT.getDefaultMessage()
                    + " 결제 금액: " + finalAmount
                    + ", 상품 금액 - 총 할인 + 배달팁 + 보증금: " + expectedFinal);
        }
    }

    private static int orZero(Integer value) {
        return value != null ? value : 0;
    }

    public void updateEarnedPoint(Integer earnedPoint) {
        this.earnedPoint = earnedPoint;
    }
}
