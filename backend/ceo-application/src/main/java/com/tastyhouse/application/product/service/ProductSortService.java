package com.tastyhouse.application.product.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductCategoryLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductCategorySavePort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationErrorCodeSpec;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;

@Service
public class ProductSortService {

    private final ProductLoadPort productLoadPort;
    private final ProductSavePort productSavePort;
    private final ProductCategoryLoadPort productCategoryLoadPort;
    private final ProductCategorySavePort productCategorySavePort;

    public ProductSortService(
        ProductLoadPort productLoadPort,
        ProductSavePort productSavePort,
        ProductCategoryLoadPort productCategoryLoadPort,
        ProductCategorySavePort productCategorySavePort
    ) {
        this.productLoadPort = productLoadPort;
        this.productSavePort = productSavePort;
        this.productCategoryLoadPort = productCategoryLoadPort;
        this.productCategorySavePort = productCategorySavePort;
    }

    public void reorderCategories(ShopId shopId, List<ProductCategoryId> orderedIds) {
        List<ProductCategory> current = productCategoryLoadPort.findAllByShopId(shopId);
        Map<Long, ProductCategory> byId = current.stream()
            .collect(Collectors.toMap(ProductCategory::getId, Function.identity()));

        List<Long> requested = distinctRawIds(orderedIds);
        requireSameSet(byId.keySet(), requested, CeoErrorCode.PRODUCT_CATEGORY_ORDER_TARGET_MISMATCH);

        for (int index = 0; index < requested.size(); index++) {
            ProductCategory category = byId.get(requested.get(index));
            category.changeSort(index);
            productCategorySavePort.save(category);
        }
    }

    public void reorderProducts(
        ShopId shopId,
        ProductCategoryId productCategoryId,
        List<ProductId> orderedIds
    ) {
        List<Product> current = productLoadPort.findAllActiveByShopIdAndCategoryId(shopId, productCategoryId);
        Map<Long, Product> byId = current.stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Long> requested = distinctRawIds(orderedIds);
        requireSameSet(byId.keySet(), requested, ApplicationErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);

        for (int index = 0; index < requested.size(); index++) {
            Product product = byId.get(requested.get(index));
            product.changeSort(index);
            productSavePort.save(product);
        }
    }

    public void relocateProducts(
        ShopId shopId,
        ProductCategoryId targetCategoryId,
        List<ProductId> movedIds,
        List<ProductId> targetOrderedIds
    ) {
        if (movedIds == null || movedIds.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }

        List<Long> movedRawIds = distinctRawIds(movedIds);
        List<Product> moved = productLoadPort.findAllActiveByShopIdAndIdIn(shopId, movedIds);
        if (moved.size() != movedRawIds.size()) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        Set<Long> sourceCategoryIds = new LinkedHashSet<>();
        boolean movedFromUncategorized = false;
        for (Product product : moved) {
            if (product.getProductCategoryId() == null) {
                movedFromUncategorized = true;
            } else {
                sourceCategoryIds.add(product.getProductCategoryId().value());
            }
        }

        List<Long> targetRawIds = distinctRawIds(targetOrderedIds);
        if (!new LinkedHashSet<>(targetRawIds).containsAll(movedRawIds)) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        List<Product> targetGroup = productLoadPort.findAllActiveByShopIdAndCategoryId(shopId, targetCategoryId);
        Map<Long, Product> targetById = targetGroup.stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, Product> movedById = moved.stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        Set<Long> expected = new LinkedHashSet<>(targetById.keySet());
        expected.addAll(movedRawIds);
        requireSameSet(expected, targetRawIds, ApplicationErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);

        for (int index = 0; index < targetRawIds.size(); index++) {
            Long rawId = targetRawIds.get(index);
            Product product = movedById.containsKey(rawId) ? movedById.get(rawId) : targetById.get(rawId);
            product.relocate(targetCategoryId, index);
            productSavePort.save(product);
        }

        Long targetRawCategoryId = targetCategoryId == null ? null : targetCategoryId.value();
        for (Long sourceCategoryId : sourceCategoryIds) {
            if (sourceCategoryId.equals(targetRawCategoryId)) {
                continue;
            }
            renumber(shopId, ProductCategoryId.of(sourceCategoryId));
        }
        if (movedFromUncategorized && targetRawCategoryId != null) {
            renumber(shopId, null);
        }
    }

    private void renumber(ShopId shopId, ProductCategoryId productCategoryId) {
        List<Product> remaining = productLoadPort.findAllActiveByShopIdAndCategoryId(shopId, productCategoryId);
        for (int index = 0; index < remaining.size(); index++) {
            Product product = remaining.get(index);
            product.changeSort(index);
            productSavePort.save(product);
        }
    }

    private <T> List<Long> distinctRawIds(List<T> ids) {
        if (ids == null) {
            return List.of();
        }
        List<Long> raw = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>();
        for (T id : ids) {
            Long value = id instanceof ProductId(Long productValue) ? productValue
                : id instanceof ProductCategoryId(Long categoryValue) ? categoryValue
                : null;
            if (value != null && seen.add(value)) {
                raw.add(value);
            }
        }
        return raw;
    }

    private void requireSameSet(Set<Long> current, List<Long> requested, ApplicationErrorCodeSpec mismatchCode) {
        if (current.size() != requested.size() || !current.containsAll(requested)) {
            throw new ApplicationException(mismatchCode);
        }
    }
}
