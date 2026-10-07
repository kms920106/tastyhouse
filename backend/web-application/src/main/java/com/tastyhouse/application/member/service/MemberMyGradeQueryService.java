package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.member.port.in.MemberMyGradeQueryUseCase;
import com.tastyhouse.application.member.port.out.MyGradeResult;

@Service
@Transactional(readOnly = true)
class MemberMyGradeQueryService implements MemberMyGradeQueryUseCase {

    private final MemberGradeService memberGradeService;

    public MemberMyGradeQueryService(MemberGradeService memberGradeService) {
        this.memberGradeService = memberGradeService;
    }

    @Override
    public MyGradeResult getMyGrade(Long memberId) {
        return memberGradeService.getMyGrade(memberId);
    }
}
