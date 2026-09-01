package com.tastyhouse.webapplication.member.port.in;

import com.tastyhouse.application.member.port.out.MemberPersonalInfoResult;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;

/**
 * 회원 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code MemberQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p>사용 가능 여부 두 연산은 {@code boolean}을 그대로 반환한다 — 감쌀 값이 하나뿐이라 별도 read
 * 계약을 만들 이유가 없고, 응답 record({@code MemberNicknameAvailabilityResponse} 등)로 감싸는 것은
 * 컨트롤러가 담당한다(선례: {@code PointQueryUseCase#getUsablePoint}).
 */
public interface MemberQueryUseCase {

    boolean checkNicknameAvailability(String nickname);

    boolean checkPhoneAvailability(String phoneNumber);

    MemberWithProfileImageResult getMemberProfile(Long targetMemberId);

    MemberWithProfileImageResult getMyProfile(Long memberId);

    MemberPersonalInfoResult getPersonalInfo(Long memberId);
}
