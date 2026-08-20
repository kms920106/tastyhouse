package com.tastyhouse.ceoapi.product.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 정보 변경 요청")
public record ProductUpdateRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @Schema(description = "메뉴그룹 ID. 지정하지 않으면 미분류 메뉴가 된다.", example = "10")
    Long productCategoryId,

    @NotBlank(message = "메뉴명은 필수입니다.")
    @Size(max = 255, message = "메뉴명은 255자 이하여야 합니다.")
    @Schema(description = "메뉴명. 가게 안에서 유일해야 하며 허용 특수문자는 : , . / ~ % & ( ) + [ ] ™ ® 뿐이다.",
        example = "매운 떡볶이", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Size(max = 500, message = "메뉴구성은 500자 이하여야 합니다.")
    @Schema(description = "메뉴구성. 목록 하단에 함께 노출되는 문구다.", example = "떡볶이 1인분 + 어묵 2장")
    String composition,

    @Size(max = 1000, message = "메뉴 설명은 1000자 이하여야 합니다.")
    @Schema(description = "메뉴 설명", example = "직접 담근 고추장으로 만든 매운 떡볶이입니다.")
    String description,

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    @Schema(description = "정가", example = "12000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer originalPrice,

    @Min(value = 0, message = "할인가는 0원 이상이어야 합니다.")
    @Schema(description = "할인가. 정가보다 클 수 없다.", example = "9900")
    Integer discountPrice,

    @Schema(description = "1인분 여부. 지정하지 않으면 false다.", example = "true")
    Boolean singleServing,

    @Schema(description = "맵기 단계", example = "2")
    Integer spiciness,

    @Schema(description = "사장님 추천 메뉴 여부. 지정하지 않으면 false다.", example = "false")
    Boolean representative,

    @Schema(description = "메뉴 평가 제외 여부(주류·사이드 등). 지정하지 않으면 false다.", example = "false")
    Boolean ratingExcluded
) {
}
