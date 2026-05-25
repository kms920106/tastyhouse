package com.tastyhouse.core.domain.order.domain.model;

import com.tastyhouse.core.entity.BaseEntity;
import com.tastyhouse.core.exception.AccessDeniedException;
import com.tastyhouse.core.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "ORDERS")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private OrderStatus orderStatus;

    @Column(name = "orderer_name", nullable = false, length = 100)
    private String ordererName;

    @Column(name = "orderer_phone", nullable = false, length = 20)
    private String ordererPhone;

    @Column(name = "orderer_email", length = 100)
    private String ordererEmail;

    @Column(name = "total_product_amount", nullable = false)
    private Integer totalProductAmount;

    @Column(name = "product_discount_amount", nullable = false)
    private Integer productDiscountAmount;

    @Column(name = "coupon_discount_amount", nullable = false)
    private Integer couponDiscountAmount;

    @Column(name = "point_discount_amount", nullable = false)
    private Integer pointDiscountAmount;

    @Column(name = "total_discount_amount", nullable = false)
    private Integer totalDiscountAmount;

    @Column(name = "final_amount", nullable = false)
    private Integer finalAmount;

    @Column(name = "member_coupon_id")
    private Long memberCouponId;

    @Column(name = "used_point", nullable = false)
    private Integer usedPoint;

    @Column(name = "earned_point", nullable = false)
    private Integer earnedPoint;

    private Order(
        Long memberId,
        Long placeId,
        String orderNumber,
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
        Long memberCouponId,
        Integer usedPoint,
        Integer earnedPoint
    ) {
        this.memberId = memberId;
        this.placeId = placeId;
        this.orderNumber = orderNumber;
        this.orderStatus = orderStatus != null ? orderStatus : OrderStatus.PENDING;
        this.ordererName = ordererName;
        this.ordererPhone = ordererPhone;
        this.ordererEmail = ordererEmail;
        this.totalProductAmount = totalProductAmount != null ? totalProductAmount : 0;
        this.productDiscountAmount = productDiscountAmount != null ? productDiscountAmount : 0;
        this.couponDiscountAmount = couponDiscountAmount != null ? couponDiscountAmount : 0;
        this.pointDiscountAmount = pointDiscountAmount != null ? pointDiscountAmount : 0;
        this.totalDiscountAmount = totalDiscountAmount != null ? totalDiscountAmount : 0;
        this.finalAmount = finalAmount != null ? finalAmount : 0;
        this.memberCouponId = memberCouponId;
        this.usedPoint = usedPoint != null ? usedPoint : 0;
        this.earnedPoint = earnedPoint != null ? earnedPoint : 0;
    }

    public static Order of(
        Long memberId,
        Long placeId,
        String orderNumber,
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
        Long memberCouponId,
        Integer usedPoint,
        Integer earnedPoint
    ) {
        return new Order(
            memberId,
            placeId,
            orderNumber,
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
            earnedPoint
        );
    }

    public void validateOwnership(Long memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    public void confirm() {
        this.orderStatus = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public void updateAmounts(
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
        Long memberCouponId,
        Integer usedPoint
    ) {
        this.totalProductAmount = totalProductAmount;
        this.productDiscountAmount = productDiscountAmount;
        this.couponDiscountAmount = couponDiscountAmount;
        this.pointDiscountAmount = pointDiscountAmount;
        this.totalDiscountAmount = totalDiscountAmount;
        this.finalAmount = finalAmount;
        this.memberCouponId = memberCouponId;
        this.usedPoint = usedPoint;
    }

    public void updateEarnedPoint(Integer earnedPoint) {
        this.earnedPoint = earnedPoint;
    }
}
