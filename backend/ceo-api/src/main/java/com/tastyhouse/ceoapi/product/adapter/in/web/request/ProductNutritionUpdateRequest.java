package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.ceoapi.product.application.port.in.ProductNutritionUpdateCommand;

/**
 * 메뉴 영양성분·알레르기 등록/수정 요청.
 *
 * <p><b>필수 5종에 {@code @NotNull}을 붙이지 않는다.</b> "전부 채우거나 전부 비우기"는 필드 하나로
 * 판정할 수 없는 집합 제약이고, 개별 {@code @NotNull}을 걸면 "전부 비우기"(영양성분 미표시)라는 정상
 * 요청이 400으로 막힌다. 판정은 도메인({@code ProductNutrition})이 한 곳에서 수행해
 * {@code PRODUCT_NUTRITION_REQUIRED_FIELD_MISSING}으로 응답한다.
 *
 * <p>음수 금지는 {@code @Min(0)}과 도메인 검증에 <b>이중</b>으로 있다. 도메인 쪽이 계약상의
 * {@code code}({@code PRODUCT_NUTRITION_VALUE_NEGATIVE})를 보장하는 단일 소유자이고, 여기의
 * {@code @Min}은 그 앞단에서 같은 값을 걸러 내는 방어다.
 */
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

    /**
     * 같은 타입의 영양성분 필드가 11개 연달아 있어 위치 기반 조립은 뒤바뀜을 컴파일러가 잡지 못한다.
     * 반드시 이름 기반 접근자로 조립한다.
     */
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
