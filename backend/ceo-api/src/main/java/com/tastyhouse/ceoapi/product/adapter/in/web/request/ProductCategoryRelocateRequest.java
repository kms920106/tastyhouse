package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductRelocateCommand;

@Schema(description = "메뉴 그룹 이동 요청")
public record ProductCategoryRelocateRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @Schema(description = "이동할 도착 메뉴그룹 ID. 지정하지 않으면 미분류로 옮긴다.", example = "10")
    Long targetProductCategoryId,

    @NotEmpty(message = "이동할 메뉴를 1개 이상 선택해야 합니다.")
    @Schema(description = "다른 그룹으로 옮길 메뉴 ID 목록", example = "[5, 6]",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> productIds,

    @NotEmpty(message = "도착 그룹의 메뉴 ID 목록은 비어 있을 수 없습니다.")
    @Schema(description = "이동 후 도착 그룹에 보이는 순서대로 나열한 메뉴 ID 전체 목록. 이동 대상이 "
        + "빠짐없이 포함되어야 한다.", example = "[2, 5, 6, 9]", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> targetOrderedProductIds
) {
    public ProductRelocateCommand toCommand(Long ceoId) {
        return new ProductRelocateCommand(ceoId, shopId, targetProductCategoryId, productIds, targetOrderedProductIds);
    }
}
