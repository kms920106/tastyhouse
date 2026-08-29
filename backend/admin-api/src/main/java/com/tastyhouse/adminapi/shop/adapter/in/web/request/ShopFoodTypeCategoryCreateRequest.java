package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapi.shop.application.port.in.ShopFoodTypeCategoryCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "음식종류 카테고리 등록 요청")
public record ShopFoodTypeCategoryCreateRequest(
    @NotBlank(message = "음식 유형은 필수입니다.")
    @Schema(description = "음식 유형", example = "KOREAN", allowableValues = {"KOREAN", "JAPANESE", "CHINESE", "WESTERN"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String foodType,

    @NotBlank(message = "표시명은 필수입니다.")
    @Schema(description = "화면 표시명", example = "한식", requiredMode = Schema.RequiredMode.REQUIRED)
    String displayName,

    @NotNull(message = "활성 아이콘 파일 ID는 필수입니다.")
    @Schema(description = "활성 상태 아이콘 파일 ID", example = "22", requiredMode = Schema.RequiredMode.REQUIRED)
    Long activeImageFileId,

    @NotNull(message = "비활성 아이콘 파일 ID는 필수입니다.")
    @Schema(description = "비활성 상태 아이콘 파일 ID", example = "23", requiredMode = Schema.RequiredMode.REQUIRED)
    Long inactiveImageFileId,

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort,

    @NotNull(message = "사용 여부는 필수입니다.")
    @Schema(description = "사용 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean visible
) {

    public ShopFoodTypeCategoryCreateCommand toCommand() {
        return new ShopFoodTypeCategoryCreateCommand(
            foodType, displayName, activeImageFileId, inactiveImageFileId, sort, visible
        );
    }
}
