package com.tastyhouse.infrastructure.member.referral.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.member.referral.domain.model.MemberReferralStatus;
import com.tastyhouse.infrastructure.member.persistence.MemberIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 회원 추천 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code MemberReferral}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code MemberReferralMapper}가 수행한다.
 */
@Entity
@Table(
    name = "MEMBER_REFERRAL",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_member_referral_referee_id",
        columnNames = {"referee_id"}
    ),
    indexes = {
        @Index(name = "idx_member_referral_referrer_id", columnList = "referrer_id"),
        @Index(name = "idx_member_referral_status", columnList = "status")
    }
)
public class MemberReferralJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "referrer_id", nullable = false)
    private MemberId referrerId;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "referee_id", nullable = false)
    private MemberId refereeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private MemberReferralStatus status;

    protected MemberReferralJpaEntity() {
    }

    private MemberReferralJpaEntity(MemberId referrerId, MemberId refereeId, MemberReferralStatus status) {
        this.referrerId = referrerId;
        this.refereeId = refereeId;
        this.status = status;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code MemberReferralMapper#toEntity}에서만 호출한다.
     */
    static MemberReferralJpaEntity create(MemberId referrerId, MemberId refereeId, MemberReferralStatus status) {
        return new MemberReferralJpaEntity(referrerId, refereeId, status);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(MemberReferralStatus status) {
        this.status = status;
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getReferrerId() {
        return this.referrerId;
    }

    public MemberId getRefereeId() {
        return this.refereeId;
    }

    public MemberReferralStatus getStatus() {
        return this.status;
    }
}
