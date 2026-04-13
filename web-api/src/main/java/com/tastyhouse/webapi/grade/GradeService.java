package com.tastyhouse.webapi.grade;

import com.tastyhouse.core.entity.user.MemberGrade;
import com.tastyhouse.webapi.grade.response.GradeInfoItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    /**
     * 전체 등급 세부 조건 목록 조회
     */
    public List<GradeInfoItem> getGradeInfoList() {
        return Arrays.stream(MemberGrade.values())
            .map(grade -> GradeInfoItem.from(
                grade,
                grade.getDisplayName(),
                grade.getMinReviewCount(),
                grade.getMaxReviewCount()))
            .collect(Collectors.toList());
    }
}
