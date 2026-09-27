package com.tastyhouse.application.referral.port.in;

import java.util.List;

import com.tastyhouse.application.member.referral.port.out.MemberReferralResult;
import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface ReferralQueryUseCase {

    List<MemberReferralResult> getMyReferrals(Long referrerId);
}
