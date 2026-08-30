package com.tastyhouse.webapplication.grade.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.webapplication.grade.response.GradeInfoListItemResponse;
import com.tastyhouse.webapplication.grade.port.in.GradeQueryUseCase;

@Service
@Transactional(readOnly = true)
public class GradeQueryService implements GradeQueryUseCase {

    /**
     * 전체 등급 세부 조건 목록 조회
     */
    @Override
    public List<GradeInfoListItemResponse> getGradeInfoList() {
        return Arrays.stream(MemberGrade.values())
            .map(grade -> GradeInfoListItemResponse.from(
                grade.name(),
                grade.getDisplayName(),
                grade.getMinReviewCount(),
                grade.getMaxReviewCount()))
            .collect(Collectors.toList());
    }
}
