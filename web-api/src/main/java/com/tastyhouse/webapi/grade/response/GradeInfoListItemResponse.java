package com.tastyhouse.webapi.grade.response;

import com.tastyhouse.core.entity.user.MemberGrade;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "등급 세부 조건 항목")
public record GradeInfoListItemResponse(
    @Schema(description = "등급 코드", example = "INSIDER")
    MemberGrade grade,

    @Schema(description = "등급 이름", example = "인싸멤버")
    String displayName,

    @Schema(description = "해당 등급 최소 리뷰 개수", example = "500")
    int minReviewCount,

    @Schema(description = "해당 등급 최대 리뷰 개수 (최고 등급은 null)", example = "699")
    Integer maxReviewCount
) {
    public static GradeInfoListItemResponse from(
    MemberGrade grade,
    String displayName,
    int minReviewCount,
    Integer maxReviewCount
    ) {
    return new GradeInfoListItemResponse(
        grade,
        displayName,
        minReviewCount,
        maxReviewCount
    );
    }
}
