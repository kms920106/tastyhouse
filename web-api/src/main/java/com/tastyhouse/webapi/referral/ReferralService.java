package com.tastyhouse.webapi.referral;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.infrastructure.member.referral.query.MemberReferralQueryDao;
import com.tastyhouse.infrastructure.member.referral.query.MemberReferralResult;
import com.tastyhouse.webapi.referral.response.ReferralMemberListItemResponse;

/**
 * 내 추천 목록 조회 서비스.
 *
 * <p>조회만 있는 도메인이라 QueryService만 둔다 — 추천 등록은 가입 흐름의 일부로 도메인 서비스
 * ({@code ReferralRegistrationService})가 처리하므로 이 모듈에 command 서비스가 필요하지 않다.
 * infra read 어댑터({@link MemberReferralQueryDao})만 주입한다.
 */
@Service
@Transactional(readOnly = true)
public class ReferralService {

    private final MemberReferralQueryDao memberReferralQueryDao;

    public ReferralService(MemberReferralQueryDao memberReferralQueryDao) {
        this.memberReferralQueryDao = memberReferralQueryDao;
    }

    public List<ReferralMemberListItemResponse> getMyReferrals(Long referrerId) {
        MemberId memberId = MemberId.of(referrerId);
        return memberReferralQueryDao.findByReferrerId(memberId)
            .stream()
            .map(this::toReferralMemberListItemResponse)
            .toList();
    }

    private ReferralMemberListItemResponse toReferralMemberListItemResponse(MemberReferralResult result) {
        return ReferralMemberListItemResponse.from(
            result.id(),
            result.refereeId().value(),
            result.status().name(),
            result.createdAt()
        );
    }
}
