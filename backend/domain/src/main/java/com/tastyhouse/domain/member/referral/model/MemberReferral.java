package com.tastyhouse.domain.member.referral.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.referral.vo.ReferralId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class MemberReferral {
    private final Long id;
    private final MemberId referrerId;
    private final MemberId refereeId;
    private MemberReferralStatus status;
    private final LocalDateTime createdAt;

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

    public static MemberReferral register(MemberId referrerId, MemberId refereeId) {
        return new MemberReferral(null, referrerId, refereeId, MemberReferralStatus.PENDING, null);
    }

    public static MemberReferral reconstitute(
        Long id,
        MemberId referrerId,
        MemberId refereeId,
        MemberReferralStatus status,
        LocalDateTime createdAt
    ) {
        return new MemberReferral(id, referrerId, refereeId, status, createdAt);
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
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
