package com.tastyhouse.application.referral.port.in;

import java.util.List;

import com.tastyhouse.application.member.referral.port.out.MemberReferralResult;

public interface ReferralMyListQueryUseCase {

    List<MemberReferralResult> getMyReferrals(Long referrerId);
}
