package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.member.port.out.MemberPersonalInfoResult;

public interface MemberPersonalInfoQueryUseCase {

    MemberPersonalInfoResult getPersonalInfo(Long memberId);
}
