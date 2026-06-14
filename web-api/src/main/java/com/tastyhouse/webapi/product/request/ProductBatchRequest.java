package com.tastyhouse.webapi.product.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "상품 배치 조회 요청. (상품ID, 옵션ID) 조합의 목록입니다.")
public record ProductBatchRequest(
    @Schema(description = "조회할 항목 목록")
    @NotEmpty(message = "조회할 항목 목록은 비어 있을 수 없습니다.")
    @Size(max = 200, message = "한 번에 조회할 수 있는 항목은 최대 200개입니다.")
    @Valid
    List<BatchItemRequest> items
) {
    @Schema(description = "조회 항목 (상품ID + 옵션ID)")
    public record BatchItemRequest(
        @Schema(description = "상품 ID", example = "1")
        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @Schema(description = "옵션 ID. 옵션이 없는 항목이면 null 가능", example = "1")
        Long optionId
    ) {
    }
}
