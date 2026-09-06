package com.tastyhouse.domain.member.referral.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.repository.MemberReferralRepository;
import com.tastyhouse.domain.member.referral.vo.ReferralId;

/**
 * 추천 관계 write 포트의 인메모리 fake.
 *
 * <p>{@code save}가 신규 저장 시 <b>새 인스턴스를 반환</b>하는 것까지 실제 어댑터와 같게 재현한다 —
 * 호출부가 반환값을 재할당하지 않으면 발행되는 이벤트에 식별자가 없는 채로 실려, 커밋 후 리스너가
 * 보상 완료 전이 대상을 찾지 못한다. fake가 in-place로 id를 채우면 그 결함이 드러나지 않는다.
 */
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
