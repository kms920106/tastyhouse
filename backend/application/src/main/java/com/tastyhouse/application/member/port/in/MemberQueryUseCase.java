package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.member.port.out.MemberPersonalInfoResult;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface MemberQueryUseCase {

    boolean checkNicknameAvailability(String nickname);

    boolean checkPhoneAvailability(String phoneNumber);

    MemberWithProfileImageResult getMemberProfile(Long targetMemberId);

    MemberWithProfileImageResult getMyProfile(Long memberId);

    MemberPersonalInfoResult getPersonalInfo(Long memberId);
}
