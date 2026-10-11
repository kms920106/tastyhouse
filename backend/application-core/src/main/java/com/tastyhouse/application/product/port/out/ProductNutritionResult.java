package com.tastyhouse.application.product.port.out;

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
