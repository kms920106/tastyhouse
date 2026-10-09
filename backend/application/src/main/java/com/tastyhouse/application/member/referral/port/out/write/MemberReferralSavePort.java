package com.tastyhouse.application.member.referral.port.out.write;

import com.tastyhouse.domain.member.referral.model.MemberReferral;

public interface MemberReferralSavePort {

    MemberReferral save(MemberReferral referral);
}
