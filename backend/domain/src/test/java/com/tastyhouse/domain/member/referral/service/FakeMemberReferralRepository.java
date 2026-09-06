package com.tastyhouse.domain.member.referral.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.repository.MemberReferralRepository;
import com.tastyhouse.domain.member.referral.vo.ReferralId;

public class FakeMemberReferralRepository implements MemberReferralRepository {
    private final Map<Long, MemberReferral> referrals = new HashMap<>();
    private long sequence = 0L;

    @Override
    public boolean existsByRefereeId(MemberId refereeId) {
        return referrals.values().stream().anyMatch(referral -> referral.getRefereeId().equals(refereeId));
    }

    @Override
    public Optional<MemberReferral> findById(ReferralId id) {
        return Optional.ofNullable(referrals.get(id.value()));
    }

    @Override
    public MemberReferral save(MemberReferral referral) {
        if (referral.getId() != null) {
            referrals.put(referral.getId(), referral);
            return referral;
        }

        MemberReferral persisted = MemberReferral.reconstitute(
            ++sequence,
            referral.getReferrerId(),
            referral.getRefereeId(),
            referral.getStatus(),
            null
        );
        referrals.put(persisted.getId(), persisted);
        return persisted;
    }
}
