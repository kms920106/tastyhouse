package com.tastyhouse.core.entity.coupon;

import com.tastyhouse.core.entity.BaseEntity;
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
        @Index(name = "idx_coupon_active", columnList = "is_active"),
        @Index(name = "idx_coupon_issue_period", columnList = "issue_start_at, issue_end_at"),
        @Index(name = "idx_coupon_use_period", columnList = "use_start_at, use_end_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "name", nullable = false, length = 200)
    private String name; // 쿠폰 이름

    @Column(name = "description", length = 500)
    private String description; // 쿠폰 설명

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DiscountType discountType = DiscountType.AMOUNT; // 할인 유형 (AMOUNT: 금액, RATE: 비율)

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount; // 할인 금액 또는 할인율

    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount; // 최대 할인 금액 (비율 할인 시 상한)

    @Column(name = "min_order_amount", nullable = false)
    private Integer minOrderAmount = 0; // 최소 주문 금액

    @Column(name = "max_discount_count")
    private Integer maxDiscountCount; // 최대 발급 수량

    @Column(name = "issue_start_at", nullable = false)
    private LocalDateTime issueStartAt; // 발급 시작 일시

    @Column(name = "issue_end_at", nullable = false)
    private LocalDateTime issueEndAt; // 발급 종료 일시

    @Column(name = "use_start_at", nullable = false)
    private LocalDateTime useStartAt; // 사용 가능 시작 일시

    @Column(name = "use_end_at", nullable = false)
    private LocalDateTime useEndAt; // 사용 가능 종료 일시

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 활성화 여부 (true: 활성)

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
        Boolean isActive
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
        this.isActive = isActive != null ? isActive : true;
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
        Boolean isActive
    ) {
        return new Coupon(
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
            isActive
        );
    }
}
