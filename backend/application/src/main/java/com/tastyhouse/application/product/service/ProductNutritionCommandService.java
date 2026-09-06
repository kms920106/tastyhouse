package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductNutritionCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductNutritionDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductNutritionUpdateCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductNutritionService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductNutritionCommandService implements ProductNutritionCommandUseCase {

    private final ProductNutritionService productNutritionService;
    private final ProductRepository productRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductNutritionCommandService(
        ProductNutritionService productNutritionService,
        ProductRepository productRepository,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productNutritionService = productNutritionService;
        this.productRepository = productRepository;
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

    @Override
    public void deleteNutrition(ProductNutritionDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateProductOwnedByShop(shopId, productId);

        productNutritionService.deleteNutrition(ProductId.of(productId));
    }

    private void validateProductOwnedByShop(Long shopId, Long productId) {
        Product product = productRepository.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private List<AllergenType> toAllergenTypes(List<String> allergens) {
        if (allergens == null) {
            return List.of();
        }
        return allergens.stream().map(AllergenType::from).toList();
    }
}
