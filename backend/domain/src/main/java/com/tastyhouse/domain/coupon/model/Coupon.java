package com.tastyhouse.domain.coupon.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class Coupon {
    private final Long id;
    private String name;
    private String description;
    private DiscountType discountType;
    private Integer discountAmount;
    private Integer maxDiscountAmount;
    private Integer minOrderAmount;
    private Integer maxDiscountCount;
    private LocalDateTime issueStartAt;
    private LocalDateTime issueEndAt;
    private LocalDateTime useStartAt;
    private LocalDateTime useEndAt;
    private boolean visible;
    private boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Coupon(
        Long id,
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
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.discountType = discountType;
        this.discountAmount = discountAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderAmount = minOrderAmount;
        this.maxDiscountCount = maxDiscountCount;
        this.issueStartAt = issueStartAt;
        this.issueEndAt = issueEndAt;
        this.useStartAt = useStartAt;
        this.useEndAt = useEndAt;
        this.visible = visible;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
        DiscountType normalizedDiscountType = discountType != null ? discountType : DiscountType.AMOUNT;
        Integer normalizedMinOrderAmount = minOrderAmount != null ? minOrderAmount : 0;

        validateCouponValues(
            normalizedDiscountType,
            discountAmount,
            maxDiscountAmount,
            normalizedMinOrderAmount,
            issueStartAt,
            issueEndAt,
            useStartAt,
            useEndAt
        );

        return new Coupon(
            null,
            name,
            description,
            normalizedDiscountType,
            discountAmount,
            maxDiscountAmount,
            normalizedMinOrderAmount,
            maxDiscountCount,
            issueStartAt,
            issueEndAt,
            useStartAt,
            useEndAt,
            visible,
            false,
            null,
            null
        );
    }

    public static Coupon reconstitute(
        Long id,
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
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Coupon(
            id,
            name,
            description,
            discountType,
            discountAmount,
            maxDiscountAmount,
            minOrderAmount,
            maxDiscountCount,
            issueStartAt,
            issueEndAt,
            useStartAt,
            useEndAt,
            visible,
            deleted,
            createdAt,
            updatedAt
        );
    }

    public CouponId getCouponId() {
        return CouponId.of(this.id);
    }

    public void update(
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
        DiscountType normalizedDiscountType = discountType != null ? discountType : DiscountType.AMOUNT;
        Integer normalizedMinOrderAmount = minOrderAmount != null ? minOrderAmount : 0;

        validateCouponValues(
            normalizedDiscountType,
            discountAmount,
            maxDiscountAmount,
            normalizedMinOrderAmount,
            issueStartAt,
            issueEndAt,
            useStartAt,
            useEndAt
        );

        this.name = name;
        this.description = description;
        this.discountType = normalizedDiscountType;
        this.discountAmount = discountAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderAmount = normalizedMinOrderAmount;
        this.maxDiscountCount = maxDiscountCount;
        this.issueStartAt = issueStartAt;
        this.issueEndAt = issueEndAt;
        this.useStartAt = useStartAt;
        this.useEndAt = useEndAt;
        this.visible = visible;
    }

    public void delete() {
        this.deleted = true;
    }

    private static void validateCouponValues(
        DiscountType discountType,
        Integer discountAmount,
        Integer maxDiscountAmount,
        Integer minOrderAmount,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt
    ) {
        if (discountType == DiscountType.RATE) {
            if (discountAmount == null || discountAmount < 1 || discountAmount > 100) {
                throw new BusinessException(ErrorCode.COUPON_DISCOUNT_RATE_INVALID,
                    ErrorCode.COUPON_DISCOUNT_RATE_INVALID.getDefaultMessage() + ": " + discountAmount);
            }
        } else if (discountAmount == null || discountAmount < 1) {
            throw new BusinessException(ErrorCode.COUPON_DISCOUNT_AMOUNT_INVALID,
                ErrorCode.COUPON_DISCOUNT_AMOUNT_INVALID.getDefaultMessage() + ": " + discountAmount);
        }

        if (maxDiscountAmount != null && maxDiscountAmount < 0) {
            throw new BusinessException(ErrorCode.COUPON_AMOUNT_NEGATIVE,
                ErrorCode.COUPON_AMOUNT_NEGATIVE.getDefaultMessage() + " 최대 할인 금액: " + maxDiscountAmount);
        }
        if (minOrderAmount != null && minOrderAmount < 0) {
            throw new BusinessException(ErrorCode.COUPON_AMOUNT_NEGATIVE,
                ErrorCode.COUPON_AMOUNT_NEGATIVE.getDefaultMessage() + " 최소 주문 금액: " + minOrderAmount);
        }

        if (useEndAt == null) {
            throw new BusinessException(ErrorCode.COUPON_USE_END_AT_REQUIRED);
        }

        validatePeriodOrder(issueStartAt, issueEndAt, "발급");
        validatePeriodOrder(useStartAt, useEndAt, "사용");
    }

    private static void validatePeriodOrder(LocalDateTime startAt, LocalDateTime endAt, String periodName) {
        if (startAt != null && endAt != null && startAt.isAfter(endAt)) {
            throw new BusinessException(ErrorCode.COUPON_PERIOD_INVALID,
                ErrorCode.COUPON_PERIOD_INVALID.getDefaultMessage()
                    + " " + periodName + " 기간: " + startAt + " ~ " + endAt);
        }
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

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public DiscountType getDiscountType() {
        return this.discountType;
    }

    public Integer getDiscountAmount() {
        return this.discountAmount;
    }

    public Integer getMaxDiscountAmount() {
        return this.maxDiscountAmount;
    }

    public Integer getMinOrderAmount() {
        return this.minOrderAmount;
    }

    public Integer getMaxDiscountCount() {
        return this.maxDiscountCount;
    }

    public LocalDateTime getIssueStartAt() {
        return this.issueStartAt;
    }

    public LocalDateTime getIssueEndAt() {
        return this.issueEndAt;
    }

    public LocalDateTime getUseStartAt() {
        return this.useStartAt;
    }

    public LocalDateTime getUseEndAt() {
        return this.useEndAt;
    }

    public boolean isVisible() {
        return this.visible;
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
}
