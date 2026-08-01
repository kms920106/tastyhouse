package com.tastyhouse.domain.coupon.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.domain.coupon.domain.vo.MemberCouponId;
import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 회원 쿠폰 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code MemberCouponJpaEntity} + {@code MemberCouponMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code MemberCouponRepository#save}를 호출해야 한다.
 */
@Getter
public class MemberCoupon {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId memberId;
    private final Long couponId;
    private boolean used;
    private LocalDateTime usedAt;
    private final LocalDateTime expiredAt;

    private MemberCoupon(
        Long id,
        MemberId memberId,
        Long couponId,
        boolean used,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.couponId = couponId;
        this.used = used;
        this.usedAt = usedAt;
        this.expiredAt = expiredAt;
    }

    /**
     * 신규 회원 쿠폰을 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static MemberCoupon of(
        MemberId memberId,
        Long couponId,
        boolean used,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        return new MemberCoupon(null, memberId, couponId, used, usedAt, expiredAt);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
    public static MemberCoupon reconstitute(
        Long id,
        MemberId memberId,
        Long couponId,
        boolean used,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        return new MemberCoupon(id, memberId, couponId, used, usedAt, expiredAt);
    }

    public MemberCouponId getMemberCouponId() {
        return MemberCouponId.of(this.id);
    }

    public void use() {
        if (!isAvailable()) {
            throw new BusinessException(ErrorCode.COUPON_NOT_AVAILABLE);
        }
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * 만료 여부. {@code expiredAt}이 null이면 <b>무기한(만료 없음)</b>으로 보아 false를 반환한다.
     *
     * <p>null을 무기한으로 해석하는 것이 이 도메인에서 안전한 쪽이다 — 반대로 "만료됨"으로 보면 만료일
     * 정보가 없다는 이유만으로 정상 발급된 쿠폰의 사용이 막힌다. 신규 발급 경로는 이미 null이 될 수 없다:
     * {@code expiredAt}은 {@code Coupon.getUseEndAt()}을 승계하고({@code CouponIssueService#issueCoupon}),
     * {@code Coupon.of}가 {@code useEndAt} 필수를 강제한다. 따라서 null은 불변식 도입 이전에 저장된
     * 레거시 행이 {@code reconstitute}로 로드된 경우에만 나타난다.
     *
     * <p>이 null 가드가 없으면 {@code LocalDateTime.now().isAfter(null)}로 NPE가 났다.
     */
    public boolean isExpired() {
        return expiredAt != null && LocalDateTime.now().isAfter(expiredAt);
    }

    public boolean isAvailable() {
        return !used && !isExpired();
    }
}
