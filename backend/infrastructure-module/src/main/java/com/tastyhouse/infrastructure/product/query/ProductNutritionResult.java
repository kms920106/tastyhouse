package com.tastyhouse.infrastructure.product.query;

/**
 * 메뉴 영양성분 read model. 알레르기 성분은 1:N이라 이 투영에 담지 않고 DAO의 별도 조회
 * ({@code ProductQueryDao#findAllergenTypes})가 담당한다 — 한 쿼리로 조인하면 성분 개수만큼 행이
 * 늘어나 수치 14개가 중복 투영된다.
 *
 * <p>{@code productId}를 담는 이유는 소비 Service가 소유권 대조(요청 shopId와 메뉴의 가게 일치)를
 * 이미 별도로 수행하므로 응답 조립에만 쓰인다.
 */
public record ProductNutritionResult(
    Long id,
    Long productId,
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
    boolean setMenu
) {

}
