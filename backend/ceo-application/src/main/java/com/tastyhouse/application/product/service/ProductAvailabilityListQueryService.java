package com.tastyhouse.application.product.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductAvailabilityListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductAvailabilityGroupResult;
import com.tastyhouse.application.product.port.out.ProductAvailabilityItemResult;
import com.tastyhouse.application.product.port.out.ProductAvailabilityQueryPort;
import com.tastyhouse.application.product.port.out.ProductAvailabilitySearchCondition;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ProductAvailabilityListQueryService implements ProductAvailabilityListQueryUseCase {

    private final ProductAvailabilityQueryPort productAvailabilityQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductAvailabilityListQueryService(
        ProductAvailabilityQueryPort productAvailabilityQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productAvailabilityQueryPort = productAvailabilityQueryPort;
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
        List<ProductAvailabilityItemResult> rows = productAvailabilityQueryPort.findProductAvailability(condition);

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

    private record CategoryKey(Long categoryId, String categoryName, Integer categorySort) {
    }
}
