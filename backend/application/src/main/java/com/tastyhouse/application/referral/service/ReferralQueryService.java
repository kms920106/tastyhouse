package com.tastyhouse.application.referral.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.referral.port.out.MemberReferralQueryPort;
import com.tastyhouse.application.member.referral.port.out.MemberReferralResult;
import com.tastyhouse.application.referral.port.in.ReferralQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class ReferralQueryService implements ReferralQueryUseCase {

    private final MemberReferralQueryPort memberReferralQueryPort;

    public ReferralQueryService(MemberReferralQueryPort memberReferralQueryPort) {
        this.memberReferralQueryPort = memberReferralQueryPort;
    }

    @Override
    public List<MemberReferralResult> getMyReferrals(Long referrerId) {
        MemberId memberId = MemberId.of(referrerId);
        return memberReferralQueryPort.findByReferrerId(memberId);
    }
}
