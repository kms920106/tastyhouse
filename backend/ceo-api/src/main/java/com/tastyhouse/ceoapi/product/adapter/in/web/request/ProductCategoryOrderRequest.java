package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductCategoryReorderCommand;

@Schema(description = "메뉴그룹 순서 변경 요청")
public record ProductCategoryOrderRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "메뉴그룹 ID 목록은 비어 있을 수 없습니다.")
    @Schema(description = "화면에 보이는 순서대로 나열한 메뉴그룹 ID 전체 목록. 가게의 현재 메뉴그룹 집합과 "
        + "일치해야 한다.", example = "[3, 1, 7]", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> productCategoryIds
) {
    public ProductCategoryReorderCommand toCommand(Long ceoId) {
        return new ProductCategoryReorderCommand(ceoId, shopId, productCategoryIds);
    }
}
