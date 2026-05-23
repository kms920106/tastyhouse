package com.tastyhouse.core.entity.order;

import com.tastyhouse.core.entity.BaseEntity;
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
    private Long id; // PK

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 주문한 회원 ID (MEMBER.id 참조)

    @Column(name = "place_id", nullable = false)
    private Long placeId; // 주문 장소 ID (PLACE.id 참조)

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber; // 주문 번호 (고유 식별 번호)

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private OrderStatus orderStatus; // 주문 상태 (PENDING: 대기, CONFIRMED: 확정, CANCELLED: 취소)

    @Column(name = "orderer_name", nullable = false, length = 100)
    private String ordererName; // 주문자 이름

    @Column(name = "orderer_phone", nullable = false, length = 20)
    private String ordererPhone; // 주문자 연락처

    @Column(name = "orderer_email", length = 100)
    private String ordererEmail; // 주문자 이메일

    @Column(name = "total_product_amount", nullable = false)
    private Integer totalProductAmount; // 상품 금액 합계 (할인 전)

    @Column(name = "product_discount_amount", nullable = false)
    private Integer productDiscountAmount; // 상품 자체 할인 금액

    @Column(name = "coupon_discount_amount", nullable = false)
    private Integer couponDiscountAmount; // 쿠폰 할인 금액

    @Column(name = "point_discount_amount", nullable = false)
    private Integer pointDiscountAmount; // 포인트 할인 금액

    @Column(name = "total_discount_amount", nullable = false)
    private Integer totalDiscountAmount; // 총 할인 금액 합계

    @Column(name = "final_amount", nullable = false)
    private Integer finalAmount; // 최종 결제 금액

    @Column(name = "member_coupon_id")
    private Long memberCouponId; // 사용한 회원 쿠폰 ID (MEMBER_COUPON.id 참조, null이면 쿠폰 미사용)

    @Column(name = "used_point", nullable = false)
    private Integer usedPoint; // 사용한 포인트

    @Column(name = "earned_point", nullable = false)
    private Integer earnedPoint; // 이 주문으로 적립된 포인트

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
