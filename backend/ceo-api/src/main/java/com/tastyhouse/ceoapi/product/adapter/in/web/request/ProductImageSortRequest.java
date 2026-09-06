package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductImageReorderCommand;

@Schema(description = "메뉴 이미지 순서 변경 요청")
public record ProductImageSortRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "이미지 ID 목록은 비어 있을 수 없습니다.")
    @Schema(description = "화면에 보이는 순서대로 나열한 그 메뉴의 이미지 ID 전체 목록", example = "[3, 1, 7]",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> imageIds
) {
    public ProductImageReorderCommand toCommand(Long ceoId, Long productId) {
        return new ProductImageReorderCommand(ceoId, shopId, productId, imageIds);
    }
}
