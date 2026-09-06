package com.tastyhouse.domain.member.referral.service;

import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.repository.MemberReferralRepository;
import com.tastyhouse.domain.member.referral.vo.ReferralId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class ReferralRewardCompletionService {
    private final MemberReferralRepository memberReferralRepository;

    public ReferralRewardCompletionService(MemberReferralRepository memberReferralRepository) {
        this.memberReferralRepository = memberReferralRepository;
    }

    public void complete(ReferralId referralId) {
        MemberReferral referral = memberReferralRepository.findById(referralId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REFERRAL_NOT_FOUND,
                "추천 관계를 찾을 수 없습니다. referralId=" + referralId.value()));

        referral.reward();
        memberReferralRepository.save(referral);
    }
}
