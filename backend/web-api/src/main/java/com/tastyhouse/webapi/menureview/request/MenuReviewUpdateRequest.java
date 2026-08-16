package com.tastyhouse.webapi.menureview.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "메뉴 평가 수정 요청")
public record MenuReviewUpdateRequest(

    @NotNull(message = "평점은 필수입니다")
    @Min(value = 1, message = "평점은 1 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5 이하이어야 합니다")
    @Schema(description = "메뉴 평점 (1~5)", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer rating,

    @Size(max = 300, message = "코멘트는 300자 이내로 입력해주세요")
    @Schema(description = "짧은 코멘트 (선택)", example = "조금 짰어요")
    String comment
) {
}
