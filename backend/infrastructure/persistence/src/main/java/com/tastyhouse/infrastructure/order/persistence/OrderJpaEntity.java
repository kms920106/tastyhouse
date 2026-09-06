package com.tastyhouse.infrastructure.order.persistence;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderDeliveryDestination;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "ORDERS")
public class OrderJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private OrderMethod orderMethod;

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

    @Column(name = "delivery_tip_amount", nullable = false)
    private Integer deliveryTipAmount;

    @Column(name = "cup_deposit_amount", nullable = false)
    private Integer cupDepositAmount;

    @Column(name = "final_amount", nullable = false)
    private Integer finalAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "roadAddress", column = @Column(name = "delivery_road_address", length = 500)),
        @AttributeOverride(name = "lotAddress", column = @Column(name = "delivery_lot_address", length = 500)),
        @AttributeOverride(name = "detailAddress", column = @Column(name = "delivery_detail_address", length = 200)),
        @AttributeOverride(name = "adminDongId", column = @Column(name = "delivery_admin_dong_id")),
        @AttributeOverride(name = "latitude", column = @Column(name = "delivery_latitude", precision = 9, scale = 6)),
        @AttributeOverride(name = "longitude", column = @Column(name = "delivery_longitude", precision = 9, scale = 6)),
        @AttributeOverride(name = "distanceMeters", column = @Column(name = "delivery_distance_meters"))
    })
    private OrderDeliveryDestination deliveryDestination;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "scheduledAt", column = @Column(name = "scheduled_at")),
        @AttributeOverride(name = "scheduledSlotEndAt", column = @Column(name = "scheduled_slot_end_at"))
    })
    private OrderSchedule schedule;

    @Column(name = "member_coupon_id")
    private Long memberCouponId;

    @Column(name = "used_point", nullable = false)
    private Integer usedPoint;

    @Column(name = "earned_point", nullable = false)
    private Integer earnedPoint;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    protected OrderJpaEntity() {
    }

    private OrderJpaEntity(
        Long memberId,
        Long shopId,
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
        Long memberCouponId,
        Integer usedPoint,
        Integer earnedPoint,
        boolean deleted
    ) {
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
    }

    static OrderJpaEntity create(
        Long memberId,
        Long shopId,
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
        Long memberCouponId,
        Integer usedPoint,
        Integer earnedPoint,
        boolean deleted
    ) {
        return new OrderJpaEntity(
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
            deleted
        );
    }

    void applyChanges(
        OrderStatus orderStatus,
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
        Long memberCouponId,
        Integer usedPoint,
        Integer earnedPoint,
        boolean deleted
    ) {
        this.orderStatus = orderStatus;
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
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public Long getShopId() {
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

    public Integer getDeliveryTipAmount() {
        return this.deliveryTipAmount;
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

    public Long getMemberCouponId() {
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
}
