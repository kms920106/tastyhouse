package com.tastyhouse.webapi.referral;

import com.tastyhouse.core.domain.referral.application.ReferralQueryService;
import com.tastyhouse.webapi.referral.response.MemberReferralListItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralQueryService referralQueryService;

    @Transactional(readOnly = true)
    public List<MemberReferralListItemResponse> getMyReferrals(Long referrerId) {
        return referralQueryService.findByReferrerId(referrerId)
            .stream()
            .map(MemberReferralListItemResponse::from)
            .toList();
    }
}
