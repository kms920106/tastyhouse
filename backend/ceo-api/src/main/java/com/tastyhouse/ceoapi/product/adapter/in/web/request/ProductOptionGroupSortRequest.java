package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductOptionGroupOrderChangeCommand;

@Schema(description = "메뉴 내 옵션그룹 순서 변경 요청")
public record ProductOptionGroupSortRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "옵션그룹 ID 목록은 비어 있을 수 없습니다.")
    @Schema(description = "화면에 보이는 순서대로 나열한 옵션그룹 ID 전체 목록. 이 메뉴에 연결된 현재 "
        + "옵션그룹 집합과 일치해야 한다.", example = "[3, 1, 7]",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> optionGroupIds
) {
    public ProductOptionGroupOrderChangeCommand toCommand(Long ceoId, Long productId) {
        return new ProductOptionGroupOrderChangeCommand(ceoId, shopId, productId, optionGroupIds);
    }
}
