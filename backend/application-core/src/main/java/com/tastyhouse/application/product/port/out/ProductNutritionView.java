package com.tastyhouse.application.product.port.out;

import java.util.List;

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
