package com.tastyhouse.core.domain.referral.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.referral.domain.repository.MemberReferralRepository;
import com.tastyhouse.core.domain.referral.application.dto.result.MemberReferralResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReferralQueryService {

    private final MemberReferralRepository memberReferralRepository;

    public List<MemberReferralResult> findByReferrerId(MemberId referrerId) {
        return memberReferralRepository.findByReferrerId(referrerId)
            .stream()
            .map(MemberReferralResult::from)
            .toList();
    }
}
