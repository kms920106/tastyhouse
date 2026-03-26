package com.tastyhouse.core.repository.referral;

import com.tastyhouse.core.entity.referral.MemberReferral;
import com.tastyhouse.core.entity.referral.ReferralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberReferralJpaRepository extends JpaRepository<MemberReferral, Long> {

    // 피추천인 기준 조회 (중복 추천 방지용)
    boolean existsByRefereeId(Long refereeId);

    // 추천인이 추천한 전체 이력 조회
    List<MemberReferral> findByReferrerId(Long referrerId);

    // 추천인 기준 특정 상태 이력 조회 (보상 지급 대기 목록 등)
    List<MemberReferral> findByReferrerIdAndStatus(Long referrerId, ReferralStatus status);

    // 피추천인의 추천 이력 단건 조회
    Optional<MemberReferral> findByRefereeId(Long refereeId);
}
