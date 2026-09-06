package com.tastyhouse.application.grade.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.application.grade.port.in.GradeQueryUseCase;
import com.tastyhouse.application.grade.port.out.GradeInfoResult;

@Service
@WebApp
@Transactional(readOnly = true)
public class GradeQueryService implements GradeQueryUseCase {

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
