package com.tastyhouse.core.domain.coupon.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

/**
 * 쿠폰 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code CouponJpaEntity} + {@code CouponMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code CouponRepository#save}를
 * 호출해야 한다.
 */
@Getter
public class Coupon {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
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
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

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

    /**
     * 신규 쿠폰을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
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
            null,
            name,
            description,
            discountType != null ? discountType : DiscountType.AMOUNT,
            discountAmount,
            maxDiscountAmount,
            minOrderAmount != null ? minOrderAmount : 0,
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

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
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

    public void delete() {
        this.deleted = true;
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
