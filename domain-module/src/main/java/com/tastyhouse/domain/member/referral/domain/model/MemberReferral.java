package com.tastyhouse.domain.member.referral.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.member.referral.domain.vo.ReferralId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 회원 추천 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code MemberReferralJpaEntity} + {@code MemberReferralMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code MemberReferralRepository#save}를
 * 호출해야 한다.
 */
@Getter
public class MemberReferral {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId referrerId;
    private final MemberId refereeId;
    private MemberReferralStatus status;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private MemberReferral(
        Long id,
        MemberId referrerId,
        MemberId refereeId,
        MemberReferralStatus status,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.referrerId = referrerId;
        this.refereeId = refereeId;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * 신규 추천 관계를 등록한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static MemberReferral register(MemberId referrerId, MemberId refereeId) {
        return new MemberReferral(null, referrerId, refereeId, MemberReferralStatus.PENDING, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static MemberReferral reconstitute(
        Long id,
        MemberId referrerId,
        MemberId refereeId,
        MemberReferralStatus status,
        LocalDateTime createdAt
    ) {
        return new MemberReferral(id, referrerId, refereeId, status, createdAt);
    }

    public ReferralId getReferralId() {
        return new ReferralId(this.id);
    }

    public void reward() {
        if (this.status != MemberReferralStatus.PENDING) {
            throw new BusinessException(ErrorCode.REFERRAL_INVALID_STATUS);
        }
        this.status = MemberReferralStatus.REWARDED;
    }

    public void cancel() {
        if (this.status != MemberReferralStatus.PENDING) {
            throw new BusinessException(ErrorCode.REFERRAL_INVALID_STATUS);
        }
        this.status = MemberReferralStatus.CANCELLED;
    }
}
