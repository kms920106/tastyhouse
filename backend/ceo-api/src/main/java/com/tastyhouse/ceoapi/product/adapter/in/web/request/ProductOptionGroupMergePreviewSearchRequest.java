package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "옵션그룹 합치기 미리보기(상세보기 diff) 조회 요청")
public record ProductOptionGroupMergePreviewSearchRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotNull(message = "기준 옵션그룹 ID는 필수입니다.")
    @Schema(description = "기준(살아남을) 옵션그룹 ID", example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED)
    Long baseOptionGroupId,

    @NotEmpty(message = "옵션그룹 ID는 1개 이상이어야 합니다.")
    @Schema(description = "비교 대상 옵션그룹 ID 목록(기준 포함 가능 — 서버가 제외합니다)",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> optionGroupIds
) {
}
