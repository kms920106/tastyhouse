package com.tastyhouse.webapplication.referral.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.referral.port.out.MemberReferralQueryPort;
import com.tastyhouse.application.member.referral.port.out.MemberReferralResult;
import com.tastyhouse.webapplication.referral.port.in.ReferralQueryUseCase;

/**
 * 내 추천 목록 조회 서비스.
 *
 * <p>조회만 있는 도메인이라 QueryService만 둔다 — 추천 등록은 가입 흐름의 일부로 도메인 서비스
 * ({@code ReferralRegistrationService})가 처리하므로 이 모듈에 command 서비스가 필요하지 않다.
 * 읽기 포트({@link MemberReferralQueryPort})만 주입한다.
 */
@Service
@Transactional(readOnly = true)
public class ReferralQueryService implements ReferralQueryUseCase {

    private final MemberReferralQueryPort memberReferralQueryPort;

    public ReferralQueryService(MemberReferralQueryPort memberReferralQueryPort) {
        this.memberReferralQueryPort = memberReferralQueryPort;
    }

    @Override
    public List<MemberReferralResult> getMyReferrals(Long referrerId) {
        MemberId memberId = MemberId.of(referrerId);
        return memberReferralQueryPort.findByReferrerId(memberId);
    }
}
