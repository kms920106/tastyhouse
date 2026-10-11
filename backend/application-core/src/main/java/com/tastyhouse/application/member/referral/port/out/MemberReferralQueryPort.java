package com.tastyhouse.application.member.referral.port.out;

import java.util.List;

public interface MemberReferralQueryPort {

    List<MemberReferralResult> findByReferrerId(Long referrerId);
}
