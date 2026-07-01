package com.tastyhouse.core.domain.referral.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.referral.application.dto.result.MemberReferralResult;
import com.tastyhouse.core.domain.referral.domain.repository.MemberReferralRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReferralQueryService {

    private final MemberReferralRepository memberReferralRepository;

    public List<MemberReferralResult> findByReferrerId(Long referrerId) {
        return memberReferralRepository.findByReferrerId(referrerId)
            .stream()
            .map(MemberReferralResult::from)
            .toList();
    }
}
