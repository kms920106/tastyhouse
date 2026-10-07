package com.tastyhouse.application.referral.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.referral.port.out.MemberReferralQueryPort;
import com.tastyhouse.application.member.referral.port.out.MemberReferralResult;
import com.tastyhouse.application.referral.port.in.ReferralMyListQueryUseCase;

@Service
@Transactional(readOnly = true)
class ReferralMyListQueryService implements ReferralMyListQueryUseCase {

    private final MemberReferralQueryPort memberReferralQueryPort;

    public ReferralMyListQueryService(MemberReferralQueryPort memberReferralQueryPort) {
        this.memberReferralQueryPort = memberReferralQueryPort;
    }

    @Override
    public List<MemberReferralResult> getMyReferrals(Long referrerId) {
        MemberId memberId = MemberId.of(referrerId);
        return memberReferralQueryPort.findByReferrerId(memberId.value());
    }
}
