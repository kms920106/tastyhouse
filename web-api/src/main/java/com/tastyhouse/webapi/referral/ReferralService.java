package com.tastyhouse.webapi.referral;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.referral.application.ReferralQueryService;
import com.tastyhouse.webapi.referral.response.MemberReferralListItemResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralQueryService referralQueryService;

    @Transactional(readOnly = true)
    public List<MemberReferralListItemResponse> getMyReferrals(Long referrerId) {
        MemberId memberId = MemberId.of(referrerId);
        return referralQueryService.findByReferrerId(memberId)
            .stream()
            .map(MemberReferralListItemResponse::from)
            .toList();
    }
}
