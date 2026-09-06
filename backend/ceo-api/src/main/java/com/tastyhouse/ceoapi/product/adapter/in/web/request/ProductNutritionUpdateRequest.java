package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.product.port.in.ProductNutritionUpdateCommand;

@Schema(description = "메뉴 영양성분·알레르기 등록/수정 요청")
public record ProductNutritionUpdateRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @Size(max = 50, message = "1회 제공량은 50자 이하여야 합니다.")
    @Schema(description = "1회 제공량", example = "100g")
    String servingSize,

    @Size(max = 50, message = "총 제공량은 50자 이하여야 합니다.")
    @Schema(description = "총 제공량", example = "1200g")
    String totalAmount,

    @Size(max = 50, message = "맛은 50자 이하여야 합니다.")
    @Schema(description = "맛", example = "매운맛")
    String flavor,

    @Size(max = 50, message = "사이즈는 50자 이하여야 합니다.")
    @Schema(description = "사이즈", example = "라지")
    String size,

    @Min(value = 0, message = "열량은 0 이상이어야 합니다.")
    @Schema(description = "열량(kcal). 필수 5종 — 하나라도 채우면 나머지 4개도 필수다.", example = "250")
    Integer calorie,

    @Min(value = 0, message = "당류는 0 이상이어야 합니다.")
    @Schema(description = "당류(g). 필수 5종", example = "3")
    Integer sugars,

    @Min(value = 0, message = "단백질은 0 이상이어야 합니다.")
    @Schema(description = "단백질(g). 필수 5종", example = "18")
    Integer protein,

    @Min(value = 0, message = "포화지방은 0 이상이어야 합니다.")
    @Schema(description = "포화지방(g). 필수 5종", example = "5")
    Integer saturatedFat,

    @Min(value = 0, message = "나트륨은 0 이상이어야 합니다.")
    @Schema(description = "나트륨(mg). 필수 5종", example = "540")
    Integer natrium,

    @Min(value = 0, message = "탄수화물은 0 이상이어야 합니다.")
    @Schema(description = "탄수화물(g)", example = "20")
    Integer carbohydrate,

    @Min(value = 0, message = "콜레스테롤은 0 이상이어야 합니다.")
    @Schema(description = "콜레스테롤(mg)", example = "60")
    Integer cholesterol,

    @Min(value = 0, message = "지방은 0 이상이어야 합니다.")
    @Schema(description = "지방(g)", example = "14")
    Integer fat,

    @Min(value = 0, message = "트랜스지방은 0 이상이어야 합니다.")
    @Schema(description = "트랜스지방(g)", example = "0")
    Integer transFat,

    @Min(value = 0, message = "카페인은 0 이상이어야 합니다.")
    @Schema(description = "카페인(mg)", example = "0")
    Integer caffeine,

    @Schema(description = "세트 메뉴 여부. true면 손님 화면에 메뉴별 확인 안내문구가 함께 노출된다. "
        + "지정하지 않으면 false다.", example = "false")
    Boolean setMenu,

    @Schema(description = "알레르기 유발성분 코드 배열. 빈 배열이면 알레르기 표시가 비워진다.",
        example = "[\"MILK\", \"PEANUT\"]")
    List<String> allergens
) {
    public ProductNutritionUpdateCommand toCommand(Long ceoId, Long productId) {
        return new ProductNutritionUpdateCommand(
            ceoId,
            this.shopId(),
            productId,
            this.servingSize(),
            this.totalAmount(),
            this.flavor(),
            this.size(),
            this.calorie(),
            this.sugars(),
            this.protein(),
            this.saturatedFat(),
            this.natrium(),
            this.carbohydrate(),
            this.cholesterol(),
            this.fat(),
            this.transFat(),
            this.caffeine(),
            this.setMenu(),
            this.allergens()
        );
    }
}
