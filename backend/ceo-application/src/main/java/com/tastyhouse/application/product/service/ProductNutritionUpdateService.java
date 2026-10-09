package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductNutritionUpdateCommand;
import com.tastyhouse.application.product.port.in.ProductNutritionUpdateUseCase;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductNutritionUpdateService implements ProductNutritionUpdateUseCase {

    private final ProductNutritionService productNutritionService;
    private final ProductLoadPort productLoadPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductNutritionUpdateService(
        ProductNutritionService productNutritionService,
        ProductLoadPort productLoadPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productNutritionService = productNutritionService;
        this.productLoadPort = productLoadPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateNutrition(ProductNutritionUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        String servingSize = command.servingSize();
        String totalAmount = command.totalAmount();
        String flavor = command.flavor();
        String size = command.size();
        Integer calorie = command.calorie();
        Integer sugars = command.sugars();
        Integer protein = command.protein();
        Integer saturatedFat = command.saturatedFat();
        Integer natrium = command.natrium();
        Integer carbohydrate = command.carbohydrate();
        Integer cholesterol = command.cholesterol();
        Integer fat = command.fat();
        Integer transFat = command.transFat();
        Integer caffeine = command.caffeine();
        Boolean setMenu = command.setMenu();
        List<String> allergens = command.allergens();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateProductOwnedByShop(shopId, productId);

        productNutritionService.upsertNutrition(
            ProductId.of(productId),
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
            Boolean.TRUE.equals(setMenu),
            toAllergenTypes(allergens)
        );
    }

    private void validateProductOwnedByShop(Long shopId, Long productId) {
        Product product = productLoadPort.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private List<AllergenType> toAllergenTypes(List<String> allergens) {
        if (allergens == null) {
            return List.of();
        }
        return allergens.stream().map(AllergenType::from).toList();
    }
}
