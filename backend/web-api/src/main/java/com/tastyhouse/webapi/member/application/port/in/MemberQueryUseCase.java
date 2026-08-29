package com.tastyhouse.webapi.member.application.port.in;

import com.tastyhouse.webapi.member.adapter.in.web.response.MemberNicknameAvailabilityResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MemberPersonalInfoResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MemberPhoneAvailabilityResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MemberProfileResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MyProfileResponse;

/**
 * 회원 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code MemberQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface MemberQueryUseCase {

    MemberNicknameAvailabilityResponse checkNicknameAvailability(String nickname);

    MemberPhoneAvailabilityResponse checkPhoneAvailability(String phoneNumber);

    MemberProfileResponse getMemberProfile(Long targetMemberId);

    MyProfileResponse getMyProfile(Long memberId);

    MemberPersonalInfoResponse getPersonalInfo(Long memberId);
}
