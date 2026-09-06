package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductSortService {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public ProductSortService(
        ProductRepository productRepository,
        ProductCategoryRepository productCategoryRepository
    ) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    public void reorderCategories(ShopId shopId, List<ProductCategoryId> orderedIds) {
        List<ProductCategory> current = productCategoryRepository.findAllByShopId(shopId);
        Map<Long, ProductCategory> byId = current.stream()
            .collect(Collectors.toMap(ProductCategory::getId, Function.identity()));

        List<Long> requested = distinctRawIds(orderedIds);
        requireSameSet(byId.keySet(), requested, ErrorCode.PRODUCT_CATEGORY_ORDER_TARGET_MISMATCH);

        for (int index = 0; index < requested.size(); index++) {
            ProductCategory category = byId.get(requested.get(index));
            category.changeSort(index);
            productCategoryRepository.save(category);
        }
    }

    public void reorderProducts(
        ShopId shopId,
        ProductCategoryId productCategoryId,
        List<ProductId> orderedIds
    ) {
        List<Product> current = productRepository.findAllByShopIdAndCategoryId(shopId, productCategoryId);
        Map<Long, Product> byId = current.stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Long> requested = distinctRawIds(orderedIds);
        requireSameSet(byId.keySet(), requested, ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);

        for (int index = 0; index < requested.size(); index++) {
            Product product = byId.get(requested.get(index));
            product.changeSort(index);
            productRepository.save(product);
        }
    }

    public void relocateProducts(
        ShopId shopId,
        ProductCategoryId targetCategoryId,
        List<ProductId> movedIds,
        List<ProductId> targetOrderedIds
    ) {
        if (movedIds == null || movedIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }

        List<Long> movedRawIds = distinctRawIds(movedIds);
        List<Product> moved = productRepository.findAllByShopIdAndIdIn(shopId, movedIds);
        if (moved.size() != movedRawIds.size()) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
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
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        List<Product> targetGroup = productRepository.findAllByShopIdAndCategoryId(shopId, targetCategoryId);
        Map<Long, Product> targetById = targetGroup.stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, Product> movedById = moved.stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        Set<Long> expected = new LinkedHashSet<>(targetById.keySet());
        expected.addAll(movedRawIds);
        requireSameSet(expected, targetRawIds, ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);

        for (int index = 0; index < targetRawIds.size(); index++) {
            Long rawId = targetRawIds.get(index);
            Product product = movedById.containsKey(rawId) ? movedById.get(rawId) : targetById.get(rawId);
            product.relocate(targetCategoryId, index);
            productRepository.save(product);
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
        List<Product> remaining = productRepository.findAllByShopIdAndCategoryId(shopId, productCategoryId);
        for (int index = 0; index < remaining.size(); index++) {
            Product product = remaining.get(index);
            product.changeSort(index);
            productRepository.save(product);
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

    private void requireSameSet(Set<Long> current, List<Long> requested, ErrorCode mismatchCode) {
        if (current.size() != requested.size() || !current.containsAll(requested)) {
            throw new BusinessException(mismatchCode);
        }
    }
}
