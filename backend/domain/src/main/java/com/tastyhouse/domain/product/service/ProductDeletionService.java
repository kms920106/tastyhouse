package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductDeletionService {
    private final ProductRepository productRepository;

    public ProductDeletionService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductAvailabilityChangeResult deleteProducts(ShopId shopId, List<ProductId> productIds) {
        List<ProductId> distinctIds = distinct(productIds);
        if (distinctIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }

        List<Product> found = productRepository.findAllByShopIdAndIdIn(shopId, distinctIds);
        Map<Long, Product> byId = new LinkedHashMap<>();
        found.forEach(product -> byId.put(product.getId(), product));

        List<ProductAvailabilityFailure> failed = new ArrayList<>();
        for (ProductId productId : distinctIds) {
            if (!byId.containsKey(productId.value())) {
                failed.add(ProductAvailabilityFailure.of(productId.value(), null, ErrorCode.PRODUCT_NOT_FOUND));
            }
        }

        List<Product> visibleTargets = byId.values().stream()
            .filter(Product::isVisible)
            .sorted(Comparator.comparing(Product::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        long visibleShortfall =
            Math.max(0, 1 - (productRepository.countVisibleByShopId(shopId) - visibleTargets.size()));
        long representativeTargets = visibleTargets.stream().filter(Product::isRepresentative).count();
        long representativeShortfall = Math.max(0,
            1 - (productRepository.countVisibleRepresentativeByShopId(shopId) - representativeTargets));

        Map<Long, ProductAvailabilityFailure> rejected = new LinkedHashMap<>();
        rejectFromTail(visibleTargets, rejected, representativeShortfall,
            ErrorCode.PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE, Product::isRepresentative);
        rejectFromTail(visibleTargets, rejected, visibleShortfall - rejected.size(),
            ErrorCode.PRODUCT_LAST_VISIBLE_CANNOT_HIDE, product -> true);

        failed.addAll(rejected.values());

        List<Long> succeeded = new ArrayList<>();
        for (Product product : byId.values()) {
            if (rejected.containsKey(product.getId())) {
                continue;
            }
            if (product.isDeleted()) {
                succeeded.add(product.getId());
                continue;
            }
            product.delete();
            productRepository.save(product);
            succeeded.add(product.getId());
        }

        return ProductAvailabilityChangeResult.of(succeeded, failed);
    }

    private void rejectFromTail(
        List<Product> candidates,
        Map<Long, ProductAvailabilityFailure> rejected,
        long shortfall,
        ErrorCode errorCode,
        Predicate<Product> predicate
    ) {
        long remaining = shortfall;
        for (int i = candidates.size() - 1; i >= 0 && remaining > 0; i--) {
            Product product = candidates.get(i);
            if (!predicate.test(product) || rejected.containsKey(product.getId())) {
                continue;
            }
            rejected.put(product.getId(),
                ProductAvailabilityFailure.of(product.getId(), product.getName(), errorCode));
            remaining--;
        }
    }

    private List<ProductId> distinct(List<ProductId> ids) {
        return ids == null ? List.of() : ids.stream().filter(Objects::nonNull).distinct().toList();
    }
}
