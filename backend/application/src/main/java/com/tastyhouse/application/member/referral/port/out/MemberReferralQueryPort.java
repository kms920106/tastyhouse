package com.tastyhouse.application.member.referral.port.out;

import java.util.List;

import com.tastyhouse.domain.member.vo.MemberId;

public interface MemberReferralQueryPort {

    List<MemberReferralResult> findByReferrerId(MemberId referrerId);
}
