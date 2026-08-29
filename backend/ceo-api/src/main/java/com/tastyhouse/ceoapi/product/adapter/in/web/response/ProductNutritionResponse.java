package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 영양성분·알레르기(점주 관리 화면).
 *
 * <p>{@code allergens}는 <b>코드 배열</b>이다 — 점주 화면은 체크박스 상태를 복원해야 하므로 라벨이 아니라
 * 코드가 필요하다. 손님 응답({@code web-api})은 반대로 한글 라벨 배열을 받는다.
 */
@Schema(description = "메뉴 영양성분·알레르기")
public record ProductNutritionResponse(
    @Schema(description = "1회 제공량. 미설정이면 null", example = "100g")
    String servingSize,

    @Schema(description = "총 제공량. 미설정이면 null", example = "1200g")
    String totalAmount,

    @Schema(description = "맛. 미설정이면 null", example = "매운맛")
    String flavor,

    @Schema(description = "사이즈. 미설정이면 null", example = "라지")
    String size,

    @Schema(description = "열량(kcal). 필수 5종", example = "250")
    Integer calorie,

    @Schema(description = "당류(g). 필수 5종", example = "3")
    Integer sugars,

    @Schema(description = "단백질(g). 필수 5종", example = "18")
    Integer protein,

    @Schema(description = "포화지방(g). 필수 5종", example = "5")
    Integer saturatedFat,

    @Schema(description = "나트륨(mg). 필수 5종", example = "540")
    Integer natrium,

    @Schema(description = "탄수화물(g). 미설정이면 null", example = "20")
    Integer carbohydrate,

    @Schema(description = "콜레스테롤(mg). 미설정이면 null", example = "60")
    Integer cholesterol,

    @Schema(description = "지방(g). 미설정이면 null", example = "14")
    Integer fat,

    @Schema(description = "트랜스지방(g). 미설정이면 null", example = "0")
    Integer transFat,

    @Schema(description = "카페인(mg). 미설정이면 null", example = "0")
    Integer caffeine,

    @Schema(description = "세트 메뉴 여부", example = "false")
    boolean setMenu,

    @Schema(description = "알레르기 유발성분 코드 배열", example = "[\"MILK\", \"PEANUT\"]")
    List<String> allergens
) {

    public static ProductNutritionResponse from(
        String servingSize,
        String totalAmount,
        String flavor,
        String size,
        Integer calorie,
        Integer sugars,
        Integer protein,
        Integer saturatedFat,
        Integer natrium,
        Integer carbohydrate,
        Integer cholesterol,
        Integer fat,
        Integer transFat,
        Integer caffeine,
        boolean setMenu,
        List<String> allergens
    ) {
        return new ProductNutritionResponse(
            servingSize,
            totalAmount,
            flavor,
            size,
            calorie,
            sugars,
            protein,
            saturatedFat,
            natrium,
            carbohydrate,
            cholesterol,
            fat,
            transFat,
            caffeine,
            setMenu,
            allergens
        );
    }
}
