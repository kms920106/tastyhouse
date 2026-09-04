package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.product.port.in.ProductReleaseCommand;

@Schema(description = "메뉴 일괄 품절·숨김 해제 요청")
public record ProductReleaseRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "대상을 1개 이상 선택해야 합니다.")
    @Size(max = 200, message = "한 번에 200개까지 처리할 수 있습니다.")
    @Schema(description = "해제할 메뉴 ID 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> productIds,

    @NotBlank(message = "해제 대상은 필수입니다.")
    @Schema(description = "해제 대상. ALL은 품절과 숨김을 함께 푼다.", example = "ALL",
        allowableValues = {"SOLD_OUT", "HIDDEN", "ALL"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String target
) {

    public ProductReleaseCommand toCommand(Long ceoId) {
        return new ProductReleaseCommand(ceoId, shopId, productIds, target);
    }
}
