package com.tastyhouse.adminapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 음식종류 카테고리 수정 요청.
 *
 * <p>음식 유형({@code foodType})은 카테고리를 식별하는 키라 생성 이후 변경할 수 없으므로
 * (도메인 {@code ShopFoodTypeCategory.foodType}이 final) 이 요청에는 포함하지 않는다.
 * 등록 시에만 지정한다 — {@link ShopFoodTypeCategoryCreateRequest}.
 */
@Schema(description = "음식종류 카테고리 수정 요청")
public record ShopFoodTypeCategoryUpdateRequest(
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
}
