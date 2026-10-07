package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.member.port.out.MyGradeResult;

public interface MemberMyGradeQueryUseCase {

    MyGradeResult getMyGrade(Long memberId);
}
