package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;

public interface MemberMyProfileQueryUseCase {

    MemberWithProfileImageResult getMyProfile(Long memberId);
}
