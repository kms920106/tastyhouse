package com.tastyhouse.application.product.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductVegetarianQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductApprovalRequestOwnerQueryPort;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;
import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;
import com.tastyhouse.application.product.port.out.ProductVegetarianSettingResult;
import com.tastyhouse.application.product.port.out.ProductVegetarianStatusResult;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopFoodTypeCategoryReader;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ProductVegetarianQueryService implements ProductVegetarianQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ProductApprovalRequestOwnerQueryPort productApprovalRequestOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopFoodTypeCategoryReader shopFoodTypeCategoryReader;
    private final ProductVegetarianApprovalService productVegetarianApprovalService;

    public ProductVegetarianQueryService(
        ProductOwnerQueryPort productOwnerQueryPort,
        ProductApprovalRequestOwnerQueryPort productApprovalRequestOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopFoodTypeCategoryReader shopFoodTypeCategoryReader,
        ProductVegetarianApprovalService productVegetarianApprovalService
    ) {
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.productApprovalRequestOwnerQueryPort = productApprovalRequestOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopFoodTypeCategoryReader = shopFoodTypeCategoryReader;
        this.productVegetarianApprovalService = productVegetarianApprovalService;
    }

    @Override
    public ProductVegetarianStatusResult getVegetarianStatus(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductVegetarianSettingResult setting = productOwnerQueryPort.findVegetarianSetting(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
        if (!setting.shopId().equals(shopId)) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }

        List<ProductVegetarianRequestResult> requests =
            productApprovalRequestOwnerQueryPort.findVegetarianRequests(productId);

        Set<String> shopCategoryNames = shopFoodTypeCategoryReader.readCategoryNames(shopId);
        boolean changeable = productVegetarianApprovalService.isShopCategoryAllowed(shopCategoryNames);

        return new ProductVegetarianStatusResult(
            setting.vegetarianType(),
            requests,
            changeable
        );
    }

}
