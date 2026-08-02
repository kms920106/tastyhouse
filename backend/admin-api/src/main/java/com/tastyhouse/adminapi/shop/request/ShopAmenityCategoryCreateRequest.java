package com.tastyhouse.adminapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "편의시설 카테고리 등록 요청")
public record ShopAmenityCategoryCreateRequest(
    @NotBlank(message = "편의시설 유형은 필수입니다.")
    @Schema(description = "편의시설 유형", example = "WIFI", allowableValues = {"PARKING", "RESTROOM", "RESERVATION", "BABY_CHAIR", "PET_FRIENDLY", "OUTLET", "TAKEOUT", "DELIVERY"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String amenity,

    @NotBlank(message = "표시명은 필수입니다.")
    @Schema(description = "화면 표시명", example = "와이파이", requiredMode = Schema.RequiredMode.REQUIRED)
    String displayName,

    @NotNull(message = "활성 아이콘 파일 ID는 필수입니다.")
    @Schema(description = "활성 상태 아이콘 파일 ID", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
    Long activeImageFileId,

    @NotNull(message = "비활성 아이콘 파일 ID는 필수입니다.")
    @Schema(description = "비활성 상태 아이콘 파일 ID", example = "21", requiredMode = Schema.RequiredMode.REQUIRED)
    Long inactiveImageFileId,

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort,

    @NotNull(message = "사용 여부는 필수입니다.")
    @Schema(description = "사용 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean visible
) {
}
