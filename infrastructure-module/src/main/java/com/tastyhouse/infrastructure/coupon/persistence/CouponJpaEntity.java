package com.tastyhouse.infrastructure.coupon.persistence;

import java.time.LocalDateTime;

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

import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;
import com.tastyhouse.core.shared.entity.BaseEntity;

/**
 * 쿠폰 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Coupon}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code CouponMapper}가 수행한다.
 */
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
public class CouponJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DiscountType discountType;

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount;

    @Column(name = "min_order_amount", nullable = false)
    private Integer minOrderAmount;

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
    private boolean visible;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    private CouponJpaEntity(
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
        boolean deleted
    ) {
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
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code CouponMapper#toEntity}에서만 호출한다.
     */
    static CouponJpaEntity create(
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
        boolean deleted
    ) {
        return new CouponJpaEntity(
            name, description, discountType, discountAmount, maxDiscountAmount,
            minOrderAmount, maxDiscountCount, issueStartAt, issueEndAt,
            useStartAt, useEndAt, visible, deleted
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(
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
        boolean deleted
    ) {
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
    }
}
