package com.tastyhouse.webapi.order.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 상품 요청")
public record OrderProductRequest(
    @NotNull(message = "상품 ID는 필수입니다")
    @Schema(description = "상품 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    Long productId,

    @Valid
    @Schema(description = "선택한 옵션 목록")
    List<OrderProductOptionRequest> options,

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    @Schema(description = "수량", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer quantity
) {
}
