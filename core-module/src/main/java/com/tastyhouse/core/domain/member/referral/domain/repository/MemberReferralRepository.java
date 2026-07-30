package com.tastyhouse.core.domain.member.referral.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.referral.domain.model.MemberReferral;
import com.tastyhouse.core.domain.member.referral.domain.vo.ReferralId;

/**
 * 추천 관계 write 포트.
 *
 * <p>중복 검증({@code existsByRefereeId})·단건 로드·저장만 남긴다. 내 추천 목록 조회는 표현 목적
 * read이므로 infrastructure-module의 {@code MemberReferralQueryDao}가 담당한다.
 */
public interface MemberReferralRepository {

    boolean existsByRefereeId(MemberId refereeId);

    Optional<MemberReferral> findById(ReferralId id);

    MemberReferral save(MemberReferral referral);
}
