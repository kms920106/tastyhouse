package com.tastyhouse.webapi.grade.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.webapplication.grade.port.out.GradeInfoResult;

@Schema(description = "등급 세부 조건 항목")
public record GradeInfoListItemResponse(
    @Schema(description = "등급 코드", example = "INSIDER")
    String grade,

    @Schema(description = "등급 이름", example = "인싸멤버")
    String displayName,

    @Schema(description = "해당 등급 최소 리뷰 개수", example = "500")
    int minReviewCount,

    @Schema(description = "해당 등급 최대 리뷰 개수 (최고 등급은 null)", example = "699")
    Integer maxReviewCount
) {

    public static GradeInfoListItemResponse from(GradeInfoResult result) {
        return new GradeInfoListItemResponse(
            result.grade(),
            result.displayName(),
            result.minReviewCount(),
            result.maxReviewCount()
        );
    }
}
