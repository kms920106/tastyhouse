package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;

public interface MemberProfileQueryUseCase {

    MemberWithProfileImageResult getMemberProfile(Long targetMemberId);
}
