package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.ceoapplication.product.port.in.ProductDeleteCommand;

@Schema(description = "메뉴 일괄 삭제 요청")
public record ProductDeleteRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "대상을 1개 이상 선택해야 합니다.")
    @Size(max = 200, message = "한 번에 200개까지 처리할 수 있습니다.")
    @Schema(description = "삭제할 메뉴 ID 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> productIds
) {

    public ProductDeleteCommand toCommand(Long ceoId) {
        return new ProductDeleteCommand(ceoId, shopId, productIds);
    }
}
