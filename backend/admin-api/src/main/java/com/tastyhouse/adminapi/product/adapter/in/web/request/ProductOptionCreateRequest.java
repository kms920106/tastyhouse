package com.tastyhouse.adminapi.product.adapter.in.web.request;

import com.tastyhouse.application.product.port.in.ProductOptionManagementCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상품 옵션 생성 요청")
public record ProductOptionCreateRequest(
    @NotBlank(message = "옵션명은 필수입니다.")
    @Schema(description = "옵션명", example = "매운맛", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @NotNull(message = "추가 금액은 필수입니다.")
    @Schema(description = "추가 금액", example = "500", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer additionalPrice,

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort,

    @NotNull(message = "품절 여부는 필수입니다.")
    @Schema(description = "품절 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean soldOut,

    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean visible,

    @Schema(description = "일회용컵 제공 개수(1~10). 보증금 옵션그룹의 옵션만 값을 갖습니다.", example = "1")
    Integer cupCount,

    @Min(value = 0, message = "개인컵 할인 금액은 0원 이상이어야 합니다.")
    @Schema(description = "개인컵 사용 할인 금액(원). 보증금 옵션그룹 안에서만 설정할 수 있습니다.",
        example = "300")
    Integer personalCupDiscountAmount
) {
    public ProductOptionManagementCreateCommand toCommand(Long optionGroupId) {
        return new ProductOptionManagementCreateCommand(
            optionGroupId, name, additionalPrice, sort, soldOut,
            visible, cupCount, personalCupDiscountAmount
        );
    }
}
