package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "점주 고객 의견 목록 조회 조건")
public record ProductFeedbackSearchRequest(

    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    @Schema(description = "페이지 번호(0부터 시작)", example = "0")
    Integer page,

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    Integer size
) {
    public ProductFeedbackSearchRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null || size <= 0) {
            size = 10;
        }
    }
}
