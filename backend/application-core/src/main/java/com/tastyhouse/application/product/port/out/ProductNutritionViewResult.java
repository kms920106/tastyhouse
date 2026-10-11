package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductNutritionViewResult(
    ProductNutritionResult nutrition,
    List<String> allergens
) {
}
