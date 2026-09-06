package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.application.product.port.out.ProductNutritionResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.application.product.port.out.ProductNutritionView;
import com.tastyhouse.application.product.port.in.ProductNutritionQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class ProductNutritionQueryService implements ProductNutritionQueryUseCase {

    private final ProductQueryPort productQueryPort;

    public ProductNutritionQueryService(ProductQueryPort productQueryPort) {
        this.productQueryPort = productQueryPort;
    }

    @Override
    public ProductNutritionView getNutrition(Long productId) {
        return productQueryPort.findNutrition(productId)
            .map(dto -> toProductNutritionView(dto, toAllergenLabels(productQueryPort.findAllergenTypes(productId))))
            .orElse(null);
    }

    private List<String> toAllergenLabels(List<String> allergenCodes) {
        return allergenCodes.stream()
            .map(code -> AllergenType.from(code).getDescription())
            .toList();
    }

    private ProductNutritionView toProductNutritionView(ProductNutritionResult dto, List<String> allergens) {
        return new ProductNutritionView(
            dto.servingSize(),
            dto.totalAmount(),
            dto.flavor(),
            dto.size(),
            dto.calorie(),
            dto.sugars(),
            dto.protein(),
            dto.saturatedFat(),
            dto.natrium(),
            dto.carbohydrate(),
            dto.cholesterol(),
            dto.fat(),
            dto.transFat(),
            dto.caffeine(),
            dto.setMenu(),
            allergens
        );
    }
}
