package com.tastyhouse.webapi.menureview.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "메뉴 평가 등록 요청")
public record MenuReviewCreateRequest(

    @NotNull(message = "주문 상품 ID는 필수입니다")
    @Schema(description = "주문 상품 ID. 평가 가능 메뉴 목록에서 받은 값을 그대로 보냅니다.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    Long orderProductId,

    @NotNull(message = "평점은 필수입니다")
    @Min(value = 1, message = "평점은 1 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5 이하이어야 합니다")
    @Schema(description = "메뉴 평점 (1~5)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer rating,

    @Size(max = 300, message = "코멘트는 300자 이내로 입력해주세요")
    @Schema(description = "짧은 코멘트 (선택)", example = "양념이 딱 좋았어요")
    String comment
) {
}
