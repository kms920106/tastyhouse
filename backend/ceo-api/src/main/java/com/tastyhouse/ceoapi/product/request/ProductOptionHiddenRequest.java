package com.tastyhouse.ceoapi.product.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "옵션 일괄 숨김 요청")
public record ProductOptionHiddenRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @Valid
    @NotEmpty(message = "대상을 1개 이상 선택해야 합니다.")
    @Size(max = 200, message = "한 번에 200개까지 처리할 수 있습니다.")
    @Schema(description = "숨김 처리할 옵션 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ProductOptionTargetRequest> options
) {
}
