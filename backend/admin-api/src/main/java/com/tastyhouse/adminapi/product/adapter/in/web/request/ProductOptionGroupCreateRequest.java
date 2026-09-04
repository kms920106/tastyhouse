package com.tastyhouse.adminapi.product.adapter.in.web.request;

import com.tastyhouse.application.product.port.in.ProductOptionGroupManagementCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상품 옵션그룹 생성 요청")
public record ProductOptionGroupCreateRequest(
    @NotBlank(message = "옵션그룹명은 필수입니다.")
    @Schema(description = "옵션그룹명", example = "맵기 선택", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Schema(description = "옵션그룹 설명", example = "매운맛 정도를 선택하세요")
    String description,

    @NotNull(message = "필수 선택 여부는 필수입니다.")
    @Schema(description = "필수 선택 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean required,

    @NotNull(message = "복수 선택 가능 여부는 필수입니다.")
    @Schema(description = "복수 선택 가능 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean multipleSelect,

    @Schema(description = "최소 선택 개수", example = "1")
    Integer minSelect,

    @Schema(description = "최대 선택 개수", example = "1")
    Integer maxSelect,

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort,

    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean visible,

    @Schema(description = "옵션그룹 유형. 미지정이면 NORMAL입니다. CUP_DEPOSIT은 대상 사업자로 지정된 "
        + "가게(cupDepositEnabled=true)만 만들 수 있습니다 — 먼저 "
        + "PATCH /api/shops/v1/{id}/cup-deposit 로 지정하세요.",
        example = "NORMAL", allowableValues = {"NORMAL", "CUP_DEPOSIT"})
    String groupType
) {

    public ProductOptionGroupManagementCreateCommand toCommand(Long productId) {
        return new ProductOptionGroupManagementCreateCommand(
            productId, name, description, required, multipleSelect,
            minSelect, maxSelect, sort, visible, groupType
        );
    }
}
