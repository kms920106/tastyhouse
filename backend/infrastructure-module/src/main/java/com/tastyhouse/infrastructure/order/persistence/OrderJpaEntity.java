package com.tastyhouse.infrastructure.order.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 주문 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Order}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code OrderMapper}가 수행한다.
 */
@Entity
@Table(name = "ORDERS")
public class OrderJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 주문자 회원 ID

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 주문 대상 가게 ID

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber; // 주문 번호

    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private OrderMethod orderMethod; // 주문 방식

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private OrderStatus orderStatus; // 주문 상태

    @Column(name = "orderer_name", nullable = false, length = 100)
    private String ordererName; // 주문자 이름

    @Column(name = "orderer_phone", nullable = false, length = 20)
    private String ordererPhone; // 주문자 전화번호

    @Column(name = "orderer_email", length = 100)
    private String ordererEmail; // 주문자 이메일

    @Column(name = "total_product_amount", nullable = false)
    private Integer totalProductAmount; // 상품 금액 합계

    @Column(name = "product_discount_amount", nullable = false)
    private Integer productDiscountAmount; // 상품 할인 금액

    @Column(name = "coupon_discount_amount", nullable = false)
    private Integer couponDiscountAmount; // 쿠폰 할인 금액

    @Column(name = "point_discount_amount", nullable = false)
    private Integer pointDiscountAmount; // 포인트 할인 금액

    @Column(name = "total_discount_amount", nullable = false)
    private Integer totalDiscountAmount; // 총 할인 금액

    @Column(name = "final_amount", nullable = false)
    private Integer finalAmount; // 최종 결제 금액

    @Column(name = "member_coupon_id")
    private Long memberCouponId; // 사용한 회원 쿠폰 ID

    @Column(name = "used_point", nullable = false)
    private Integer usedPoint; // 사용한 포인트

    @Column(name = "earned_point", nullable = false)
    private Integer earnedPoint; // 적립된 포인트

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted; // 삭제 여부 (true: 삭제됨, Soft Delete)

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
        Integer finalAmount,
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
        this.finalAmount = finalAmount;
        this.memberCouponId = memberCouponId;
        this.usedPoint = usedPoint;
        this.earnedPoint = earnedPoint;
        this.deleted = deleted;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code OrderMapper#toEntity}에서만 호출한다.
     */
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
        Integer finalAmount,
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
            finalAmount,
            memberCouponId,
            usedPoint,
            earnedPoint,
            deleted
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(
        OrderStatus orderStatus,
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
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
        this.finalAmount = finalAmount;
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
