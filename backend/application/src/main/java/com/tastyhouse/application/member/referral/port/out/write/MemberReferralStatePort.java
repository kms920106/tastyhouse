package com.tastyhouse.application.member.referral.port.out.write;

import java.util.Optional;

public interface MemberReferralStatePort {
    boolean existsByRefereeId(Long refereeId);

    Optional<MemberReferralState> findById(Long id);

    MemberReferralState save(MemberReferralState state);
}
