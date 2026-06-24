package com.tastyhouse.core.domain.coupon.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
    name = "COUPON",
    indexes = {
        @Index(name = "idx_coupon_active", columnList = "is_visible"),
        @Index(name = "idx_coupon_issue_period", columnList = "issue_start_at, issue_end_at"),
        @Index(name = "idx_coupon_use_period", columnList = "use_start_at, use_end_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DiscountType discountType = DiscountType.AMOUNT;

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount;

    @Column(name = "min_order_amount", nullable = false)
    private Integer minOrderAmount = 0;

    @Column(name = "max_discount_count")
    private Integer maxDiscountCount;

    @Column(name = "issue_start_at", nullable = false)
    private LocalDateTime issueStartAt;

    @Column(name = "issue_end_at", nullable = false)
    private LocalDateTime issueEndAt;

    @Column(name = "use_start_at", nullable = false)
    private LocalDateTime useStartAt;

    @Column(name = "use_end_at", nullable = false)
    private LocalDateTime useEndAt;

    @Column(name = "is_visible", nullable = false)
    private boolean visible = true;

    private Coupon(
        String name,
        String description,
        DiscountType discountType,
        Integer discountAmount,
        Integer maxDiscountAmount,
        Integer minOrderAmount,
        Integer maxDiscountCount,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt,
        boolean visible
    ) {
        this.name = name;
        this.description = description;
        this.discountType = discountType != null ? discountType : DiscountType.AMOUNT;
        this.discountAmount = discountAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderAmount = minOrderAmount != null ? minOrderAmount : 0;
        this.maxDiscountCount = maxDiscountCount;
        this.issueStartAt = issueStartAt;
        this.issueEndAt = issueEndAt;
        this.useStartAt = useStartAt;
        this.useEndAt = useEndAt;
        this.visible = visible;
    }

    public static Coupon of(
        String name,
        String description,
        DiscountType discountType,
        Integer discountAmount,
        Integer maxDiscountAmount,
        Integer minOrderAmount,
        Integer maxDiscountCount,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt,
        boolean visible
    ) {
        return new Coupon(
            name, description, discountType, discountAmount, maxDiscountAmount,
            minOrderAmount, maxDiscountCount, issueStartAt, issueEndAt,
            useStartAt, useEndAt, visible
        );
    }

    public CouponId getCouponId() {
        return new CouponId(this.id);
    }

    public int calculateDiscount(int orderAmount) {
        if (discountType == DiscountType.AMOUNT) {
            return discountAmount;
        }
        int calculated = (int) Math.round(orderAmount * discountAmount / 100.0);
        return (maxDiscountAmount != null) ? Math.min(calculated, maxDiscountAmount) : calculated;
    }

    public void validateMinOrderAmount(int orderAmount) {
        if (orderAmount < minOrderAmount) {
            throw new BusinessException(ErrorCode.ORDER_MINIMUM_AMOUNT_NOT_MET);
        }
    }
}
