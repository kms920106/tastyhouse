package com.tastyhouse.adminapi.product.adapter.in.web.request;

import com.tastyhouse.application.product.port.in.ProductManagementCreateCommand;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상품 생성 요청")
public record ProductCreateRequest(
    @NotNull(message = "매장 ID는 필수입니다.")
    @Schema(description = "매장 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @Schema(description = "카테고리 ID", example = "1")
    Long productCategoryId,

    @NotBlank(message = "상품명은 필수입니다.")
    @Schema(description = "상품명", example = "치즈불닭볶음면", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Schema(description = "상품 설명", example = "매콤한 불닭볶음면에 치즈를 더했습니다")
    String description,

    @NotNull(message = "정가는 필수입니다.")
    @Schema(description = "정가", example = "8900", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer originalPrice,

    @Schema(description = "할인가", example = "7900")
    Integer discountPrice,

    @Schema(description = "할인율", example = "0.11")
    BigDecimal discountRate,

    @Schema(description = "평점 초기값", example = "4.5")
    Double rating,

    @Schema(description = "리뷰 수 초기값", example = "0")
    Integer reviewCount,

    @NotNull(message = "대표 상품 여부는 필수입니다.")
    @Schema(description = "대표 상품 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean representative,

    @Schema(description = "맵기", example = "2")
    Integer spiciness,

    @NotNull(message = "품절 여부는 필수입니다.")
    @Schema(description = "품절 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean soldOut,

    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean visible,

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort
) {

    public ProductManagementCreateCommand toCommand() {
        return new ProductManagementCreateCommand(
            shopId, productCategoryId, name, description,
            originalPrice, discountPrice, discountRate,
            rating, reviewCount, representative, spiciness,
            soldOut, visible, sort
        );
    }
}
