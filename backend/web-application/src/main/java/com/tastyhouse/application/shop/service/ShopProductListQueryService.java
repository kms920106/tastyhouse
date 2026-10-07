package com.tastyhouse.application.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductByShopQueryUseCase;
import com.tastyhouse.application.product.port.in.ProductCategoryByShopQueryUseCase;
import com.tastyhouse.application.product.port.out.ShopProductItemResult;
import com.tastyhouse.application.shop.port.in.ShopProductListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopProductCategoryViewResult;

@Service
@Transactional(readOnly = true)
class ShopProductListQueryService implements ShopProductListQueryUseCase {

    private static final String UNCATEGORIZED_CATEGORY_NAME = "미분류";

    private final ProductByShopQueryUseCase productByShopQueryUseCase;
    private final ProductCategoryByShopQueryUseCase productCategoryByShopQueryUseCase;

    public ShopProductListQueryService(
        ProductByShopQueryUseCase productByShopQueryUseCase,
        ProductCategoryByShopQueryUseCase productCategoryByShopQueryUseCase
    ) {
        this.productByShopQueryUseCase = productByShopQueryUseCase;
        this.productCategoryByShopQueryUseCase = productCategoryByShopQueryUseCase;
    }

    @Override
    public List<ShopProductCategoryViewResult> getShopProducts(Long shopId) {
        List<ShopProductItemResult> shopProducts = productByShopQueryUseCase.findShopProducts(shopId);

        Map<Long, List<ShopProductItemResult>> productsByCategory = shopProducts.stream()
            .filter(product -> product.productCategoryId() != null)
            .collect(Collectors.groupingBy(ShopProductItemResult::productCategoryId));

        List<ShopProductItemResult> uncategorizedProducts = shopProducts.stream()
            .filter(product -> product.productCategoryId() == null)
            .toList();

        List<ShopProductCategoryViewResult> categories = productCategoryByShopQueryUseCase.findShopProductCategories(shopId)
            .stream()
            .map(category -> new ShopProductCategoryViewResult(
                category.name(),
                productsByCategory.getOrDefault(category.id(), new ArrayList<>())
            ))
            .collect(Collectors.toCollection(ArrayList::new));

        if (!uncategorizedProducts.isEmpty()) {
            categories.add(new ShopProductCategoryViewResult(
                UNCATEGORIZED_CATEGORY_NAME,
                uncategorizedProducts
            ));
        }

        return categories;
    }
}
