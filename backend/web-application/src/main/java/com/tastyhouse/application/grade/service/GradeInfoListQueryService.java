package com.tastyhouse.application.grade.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.application.grade.port.in.GradeInfoListQueryUseCase;
import com.tastyhouse.application.grade.port.out.GradeInfoResult;

@Service
@Transactional(readOnly = true)
class GradeInfoListQueryService implements GradeInfoListQueryUseCase {

    @Override
    public List<GradeInfoResult> getGradeInfoList() {
        return Arrays.stream(MemberGrade.values())
            .map(grade -> new GradeInfoResult(
                grade.name(),
                grade.getDisplayName(),
                grade.getMinReviewCount(),
                grade.getMaxReviewCount()))
            .toList();
    }
}
