package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.out.ProductVegetarianStatusResult;
import com.tastyhouse.application.product.port.in.ProductVegetarianQueryUseCase;
import com.tastyhouse.application.shop.service.ShopFoodTypeCategoryReader;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.service.ProductVegetarianApprovalService;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;
import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;
import com.tastyhouse.application.product.port.out.ProductVegetarianSettingResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ProductVegetarianQueryService implements ProductVegetarianQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopFoodTypeCategoryReader shopFoodTypeCategoryReader;
    private final ProductVegetarianApprovalService productVegetarianApprovalService;

    public ProductVegetarianQueryService(
        ProductOwnerQueryPort productOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopFoodTypeCategoryReader shopFoodTypeCategoryReader,
        ProductVegetarianApprovalService productVegetarianApprovalService
    ) {
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopFoodTypeCategoryReader = shopFoodTypeCategoryReader;
        this.productVegetarianApprovalService = productVegetarianApprovalService;
    }

    @Override
    public ProductVegetarianStatusResult getVegetarianStatus(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductVegetarianSettingResult setting = productOwnerQueryPort.findVegetarianSetting(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!setting.shopId().equals(shopId)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        List<ProductVegetarianRequestResult> requests = productOwnerQueryPort.findVegetarianRequests(productId);

        Set<String> shopCategoryNames = shopFoodTypeCategoryReader.readCategoryNames(shopId);
        boolean changeable = productVegetarianApprovalService.isShopCategoryAllowed(shopCategoryNames);

        return new ProductVegetarianStatusResult(setting.vegetarianType(), requests, changeable);
    }

}
