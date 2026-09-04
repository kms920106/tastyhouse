package com.tastyhouse.application.product.port.out;

import java.util.List;

/**
 * 손님용 메뉴 영양성분·알레르기 조회 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 공용 읽기 계약 {@code ProductNutritionResult}로는 이 응답을 표현할 수
 * 없다 — {@code allergens}가 <b>코드가 아니라 한글 라벨 배열</b>인데, 그 변환은 도메인 enum
 * {@code AllergenType}의 승격({@code from(String)})을 거치므로 api 모듈이 수행할 수 없다
 * ({@code apiModuleShouldOnlyReadDomainEnums}가 {@code from}을 허용하지 않는다). 라벨 변환은 서비스에서
 * 끝내고 그 결과만 이 계약으로 나른다.
 *
 * <p>{@code ProductNutritionResult}에 있던 {@code id}·{@code productId}는 담지 않는다 — 응답 계약에 없는
 * 내부 식별자다.
 */
public record ProductNutritionView(
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
}
