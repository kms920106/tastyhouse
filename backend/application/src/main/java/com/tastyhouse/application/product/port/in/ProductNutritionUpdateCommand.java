package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductNutritionUpdateCommand(
    Long ceoId,
    Long shopId,
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
    Boolean setMenu,
    List<String> allergens
) {
    public ProductNutritionUpdateCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
