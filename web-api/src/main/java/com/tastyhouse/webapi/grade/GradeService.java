package com.tastyhouse.webapi.grade;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.domain.model.MemberGrade;
import com.tastyhouse.webapi.grade.response.GradeInfoListItemResponse;

@Service
public class GradeService {

    /**
     * 전체 등급 세부 조건 목록 조회
     */
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
