package com.tastyhouse.application.member.referral.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.vo.ReferralId;
import com.tastyhouse.application.member.referral.port.out.write.MemberReferralPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class ReferralRewardCompletionService {

    private final MemberReferralPersistencePort memberReferralPersistencePort;

    public ReferralRewardCompletionService(MemberReferralPersistencePort memberReferralPersistencePort) {
        this.memberReferralPersistencePort = memberReferralPersistencePort;
    }

    public void complete(ReferralId referralId) {
        MemberReferral referral = memberReferralPersistencePort.findById(referralId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REFERRAL_NOT_FOUND,
                "추천 관계를 찾을 수 없습니다. referralId=" + referralId.value()));

        referral.reward();
        memberReferralPersistencePort.save(referral);
    }
}
