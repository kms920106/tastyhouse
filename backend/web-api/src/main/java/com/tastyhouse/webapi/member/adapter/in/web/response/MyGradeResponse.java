package com.tastyhouse.webapi.member.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.webapplication.member.port.out.MyGradeResult;

@Schema(description = "내 등급 정보 응답")
public record MyGradeResponse(
    @Schema(description = "현재 등급 코드", example = "INSIDER")
    String currentGrade,

    @Schema(description = "현재 등급 이름", example = "인싸멤버")
    String currentGradeDisplayName,

    @Schema(description = "다음 등급 코드 (최고 등급이면 null)", example = "GOURMET")
    String nextGrade,

    @Schema(description = "다음 등급 이름 (최고 등급이면 null)", example = "미식멤버")
    String nextGradeDisplayName,

    @Schema(description = "현재 작성 리뷰 수", example = "625")
    int currentReviewCount,

    @Schema(description = "다음 등급까지 필요한 리뷰 개수 (최고 등급이면 0)", example = "75")
    int reviewsNeededForNextGrade
) {
    public static MyGradeResponse from(MyGradeResult result) {
        return new MyGradeResponse(
            result.currentGrade(),
            result.currentGradeDisplayName(),
            result.nextGrade(),
            result.nextGradeDisplayName(),
            result.currentReviewCount(),
            result.reviewsNeededForNextGrade()
        );
    }
}
