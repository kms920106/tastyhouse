package com.tastyhouse.webapplication.referral.port.in;

import java.util.List;

import com.tastyhouse.application.member.referral.port.out.MemberReferralResult;

/**
 * 추천인 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ReferralQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p>추천 등록은 가입 흐름의 일부로 도메인 서비스({@code ReferralRegistrationService})가 처리하므로
 * 이 모듈에는 쓰기 경로가 없다. 따라서 CommandUseCase 짝을 두지 않는다.
 */
public interface ReferralQueryUseCase {

    List<MemberReferralResult> getMyReferrals(Long referrerId);
}
