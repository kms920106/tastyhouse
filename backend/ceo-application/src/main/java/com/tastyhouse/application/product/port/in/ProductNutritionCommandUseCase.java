package com.tastyhouse.application.product.port.in;

public interface ProductNutritionCommandUseCase {

    void updateNutrition(ProductNutritionUpdateCommand command);

    void deleteNutrition(ProductNutritionDeleteCommand command);
}
