package com.tastyhouse.domain.coupon.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
     *
     * <p>값 불변식({@link #validateCouponValues})을 강제한다 — 할인 유형별 할인값 범위, 금액 음수 금지,
     * 발급·사용 기간의 순서, {@code useEndAt} 필수.
     *
     * <p>{@link #reconstitute}는 이 검증을 <b>거치지 않는다</b> — 기존 DB 데이터가 새 불변식을 위반해도
     * 로드는 가능해야 하기 때문이다.
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

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     *
     * <p><b>{@link #of}와 달리 값 불변식 검증을 하지 않는다</b> — 불변식 도입 이전에 저장된 기존 쿠폰이
     * 새 규칙을 위반하더라도 로드는 가능해야 하기 때문이다.
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

    /**
     * 쿠폰 값 불변식을 검증한다 — 신규 생성({@code of})과 변경({@code update}) 양쪽이 같은 검증 한 벌을
     * 공유한다. 생성만 막고 변경을 열어두면 같은 위반 값이 곧바로 뒷문으로 들어오기 때문이다.
     *
     * <p>검증 항목:
     * <ul>
     *   <li>{@code RATE}면 할인율 {@code 1 <= r <= 100}, {@code AMOUNT}면 할인 금액 {@code >= 1}</li>
     *   <li>{@code maxDiscountAmount}·{@code minOrderAmount} 음수 금지</li>
     *   <li>발급 기간·사용 기간 각각 시작 &lt;= 종료</li>
     *   <li>{@code useEndAt} 필수 — {@code MemberCoupon.expiredAt}이 이 값을 승계하므로 null이면
     *       만료 판정이 불가능해진다({@code MemberCoupon#isExpired} 참고)</li>
     * </ul>
     *
     * <p>인스턴스 상태를 읽지 않으므로 {@code static}이다.
     */
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

    /**
     * 기간의 시작이 종료보다 늦지 않은지 검증한다. 둘 중 하나라도 null이면 순서를 판정할 수 없으므로
     * 통과시킨다({@code useEndAt} 필수 여부는 호출부가 따로 검증한다).
     */
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
}
