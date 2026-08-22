package com.tastyhouse.webapi.product.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 영양성분·알레르기(손님 화면).
 *
 * <p>{@code allergens}는 코드가 아니라 <b>한글 라벨 배열</b>이다({@code ["우유","땅콩"]}) — 손님 화면이
 * 코드→라벨 매핑표를 들고 있지 않게 하려는 것이다. 점주 응답은 반대로 체크박스 상태 복원을 위해 코드
 * 배열을 받는다.
 *
 * <p>{@code setMenu}가 true면 화면이 "메뉴구성에 따라 영양성분이 다르므로 각각의 메뉴에 대한 영양성분을
 * 확인해 주시기 바랍니다" 안내문구를 함께 노출한다. 그 문구는 backend가 아니라 화면 상수다 — 문구
 * 자체는 표시 정책이라 배포 없이 바꿀 이유가 없고, API가 문장을 내려주면 화면 레이아웃과 결합된다.
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

    @Schema(description = "열량(kcal)", example = "250")
    Integer calorie,

    @Schema(description = "당류(g)", example = "3")
    Integer sugars,

    @Schema(description = "단백질(g)", example = "18")
    Integer protein,

    @Schema(description = "포화지방(g)", example = "5")
    Integer saturatedFat,

    @Schema(description = "나트륨(mg)", example = "540")
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

    @Schema(description = "세트 메뉴 여부. true면 화면이 메뉴별 확인 안내문구를 함께 노출한다.", example = "false")
    boolean setMenu,

    @Schema(description = "알레르기 유발성분 한글 라벨 배열", example = "[\"우유\", \"땅콩\"]")
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
