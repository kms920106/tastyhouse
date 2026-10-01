package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductOptionGroupMergeCommand;

@Schema(description = "옵션그룹 합치기 실행 요청")
public record ProductOptionGroupMergeRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "합칠 옵션그룹 ID는 1개 이상이어야 합니다.")
    @Schema(description = "흡수될 옵션그룹 ID 목록. 기준 옵션그룹은 포함할 수 없습니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> optionGroupIds,

    @NotBlank(message = "진입 경로는 필수입니다.")
    @Schema(description = "진입 경로", example = "RECOMMENDED",
        allowableValues = {"RECOMMENDED", "MANUAL"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String entryType
) {

    public ProductOptionGroupMergeCommand toCommand(Long ceoId, Long baseOptionGroupId) {
        return new ProductOptionGroupMergeCommand(ceoId, shopId, baseOptionGroupId, optionGroupIds, entryType);
    }
}
