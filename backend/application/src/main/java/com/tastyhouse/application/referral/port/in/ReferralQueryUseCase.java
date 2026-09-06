package com.tastyhouse.application.referral.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.application.member.referral.port.out.MemberReferralResult;

@WebApp
public interface ReferralQueryUseCase {

    List<MemberReferralResult> getMyReferrals(Long referrerId);
}
