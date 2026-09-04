package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductOptionGroupMergeExclusionCreateCommand;

@Schema(description = "옵션그룹 합치기 추천 제외 요청")
public record ProductOptionGroupMergeExclusionCreateRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotBlank(message = "서명은 필수입니다.")
    @Schema(description = "추천 목록이 내려준 동일성 서명(불투명 토큰). 그대로 실어 보냅니다.",
        example = "3f2a...64자", requiredMode = Schema.RequiredMode.REQUIRED)
    String signature,

    @NotEmpty(message = "옵션그룹 ID는 1개 이상이어야 합니다.")
    @Schema(description = "이 묶음에 속한 옵션그룹 ID 목록. 서버가 이 ID들로 서명을 재계산해 "
        + "위조·낡은 토큰을 거부합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> optionGroupIds
) {

    public ProductOptionGroupMergeExclusionCreateCommand toCommand(Long ceoId) {
        return new ProductOptionGroupMergeExclusionCreateCommand(ceoId, shopId, signature, optionGroupIds);
    }
}
