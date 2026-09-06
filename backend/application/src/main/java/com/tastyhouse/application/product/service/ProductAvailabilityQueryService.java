package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.out.ProductAvailabilityGroupResult;
import com.tastyhouse.application.product.port.in.ProductAvailabilityQueryUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.application.product.port.out.ProductAvailabilityItemResult;
import com.tastyhouse.application.product.port.out.ProductAvailabilitySearchCondition;
import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityGroupResult;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ProductAvailabilityQueryService implements ProductAvailabilityQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductAvailabilityQueryService(
        ProductOwnerQueryPort productOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ProductAvailabilityGroupResult> getProductAvailability(
        Long ceoId,
        Long shopId,
        String keyword,
        Boolean soldOutOnly,
        Boolean hiddenOnly
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductAvailabilitySearchCondition condition =
            ProductAvailabilitySearchCondition.of(shopId, keyword, soldOutOnly, hiddenOnly);
        List<ProductAvailabilityItemResult> rows = productOwnerQueryPort.findProductAvailability(condition);

        Map<CategoryKey, List<ProductAvailabilityItemResult>> grouped = new LinkedHashMap<>();
        for (ProductAvailabilityItemResult row : rows) {
            CategoryKey key = new CategoryKey(row.categoryId(), row.categoryName(), row.categorySort());
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(row);
        }

        List<ProductAvailabilityGroupResult> response = new ArrayList<>();
        grouped.forEach((key, products) -> response.add(new ProductAvailabilityGroupResult(
            key.categoryId(),
            key.categoryName(),
            key.categorySort(),
            products
        )));
        return response;
    }

    @Override
    public List<ProductOptionAvailabilityGroupResult> getProductOptionAvailability(
        Long ceoId,
        Long shopId,
        String keyword,
        Boolean soldOutOnly,
        Boolean hiddenOnly
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductAvailabilitySearchCondition condition =
            ProductAvailabilitySearchCondition.of(shopId, keyword, soldOutOnly, hiddenOnly);

        return productOwnerQueryPort.findProductOptionAvailability(condition);
    }

    private record CategoryKey(Long categoryId, String categoryName, Integer categorySort) {
    }
}
