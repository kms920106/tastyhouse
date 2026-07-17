package com.tastyhouse.webapi.referral;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.referral.application.ReferralQueryService;
import com.tastyhouse.core.domain.member.referral.application.dto.result.MemberReferralResult;
import com.tastyhouse.webapi.referral.response.ReferralMemberListItemResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralQueryService referralQueryService;

    @Transactional(readOnly = true)
    public List<ReferralMemberListItemResponse> getMyReferrals(Long referrerId) {
        MemberId memberId = MemberId.of(referrerId);
        return referralQueryService.findByReferrerId(memberId)
            .stream()
            .map(this::toReferralMemberListItemResponse)
            .toList();
    }

    private ReferralMemberListItemResponse toReferralMemberListItemResponse(MemberReferralResult result) {
        return ReferralMemberListItemResponse.from(
            result.id(),
            result.refereeId().value(),
            result.status().name(),
            result.createdAt()
        );
    }
}
