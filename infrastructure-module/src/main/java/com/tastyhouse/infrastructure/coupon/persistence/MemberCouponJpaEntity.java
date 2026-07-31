package com.tastyhouse.infrastructure.coupon.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.infrastructure.member.persistence.MemberIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 회원 쿠폰 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code MemberCoupon}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code MemberCouponMapper}가 수행한다.
 */
@Getter
@Entity
@Table(
    name = "MEMBER_COUPON",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_member_coupon",
            columnNames = {"member_id", "coupon_id"}
        )
    },
    indexes = {
        @Index(name = "idx_member_coupon_member_id", columnList = "member_id"),
        @Index(name = "idx_member_coupon_coupon_id", columnList = "coupon_id"),
        @Index(name = "idx_member_coupon_used", columnList = "member_id, is_used")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCouponJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    private MemberCouponJpaEntity(
        MemberId memberId,
        Long couponId,
        boolean used,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        this.memberId = memberId;
        this.couponId = couponId;
        this.used = used;
        this.usedAt = usedAt;
        this.expiredAt = expiredAt;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code MemberCouponMapper#toEntity}에서만 호출한다.
     */
    static MemberCouponJpaEntity create(
        MemberId memberId,
        Long couponId,
        boolean used,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        return new MemberCouponJpaEntity(memberId, couponId, used, usedAt, expiredAt);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(boolean used, LocalDateTime usedAt) {
        this.used = used;
        this.usedAt = usedAt;
    }
}
