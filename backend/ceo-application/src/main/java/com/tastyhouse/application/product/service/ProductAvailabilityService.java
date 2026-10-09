package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.ErrorCodeSpec;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ReleaseTarget;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.CeoErrorCode;

@Service
class ProductAvailabilityService {

    private final ProductLoadPort productLoadPort;
    private final ProductSavePort productSavePort;
    private final ProductSoldOutUntilValidator productSoldOutUntilValidator;

    public ProductAvailabilityService(
        ProductLoadPort productLoadPort,
        ProductSavePort productSavePort,
        ProductSoldOutUntilValidator productSoldOutUntilValidator
    ) {
        this.productLoadPort = productLoadPort;
        this.productSavePort = productSavePort;
        this.productSoldOutUntilValidator = productSoldOutUntilValidator;
    }

    public ProductAvailabilityChangeResult hideProducts(ShopId shopId, List<ProductId> productIds) {
        LoadedProducts loaded = loadProducts(shopId, productIds);
        List<ProductAvailabilityFailure> failed = new ArrayList<>(loaded.failed());

        List<Product> candidates = loaded.found().stream()
            .filter(Product::isVisible)
            .sorted(Comparator.comparing(Product::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        List<Product> alreadyHidden = loaded.found().stream()
            .filter(product -> !product.isVisible())
            .toList();

        long visibleShortfall =
            Math.max(0, 1 - (productLoadPort.countVisibleByShopId(shopId) - candidates.size()));
        long representativeTargets = candidates.stream().filter(Product::isRepresentative).count();
        long representativeShortfall =
            Math.max(0, 1 - (productLoadPort.countVisibleRepresentativeByShopId(shopId) - representativeTargets));

        Map<Long, ProductAvailabilityFailure> rejected = new LinkedHashMap<>();
        rejectFromTail(candidates, rejected, representativeShortfall,
            ApplicationErrorCode.PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE, Product::isRepresentative);
        rejectFromTail(candidates, rejected, visibleShortfall - rejected.size(),
            CeoErrorCode.PRODUCT_LAST_VISIBLE_CANNOT_HIDE, product -> true);

        failed.addAll(rejected.values());

        List<Long> succeeded = new ArrayList<>();
        for (Product product : candidates) {
            if (rejected.containsKey(product.getId())) {
                continue;
            }
            product.deactivate();
            productSavePort.save(product);
            succeeded.add(product.getId());
        }

        alreadyHidden.forEach(product -> succeeded.add(product.getId()));

        return ProductAvailabilityChangeResult.of(succeeded, failed);
    }

    public ProductAvailabilityChangeResult markProductsSoldOut(
        ShopId shopId,
        List<ProductId> productIds,
        LocalDateTime soldOutUntil,
        LocalDateTime now
    ) {
        productSoldOutUntilValidator.validate(soldOutUntil, now);
        LoadedProducts loaded = loadProducts(shopId, productIds);

        List<Long> succeeded = new ArrayList<>();
        for (Product product : loaded.found()) {
            if (soldOutUntil != null) {
                product.markSoldOut(soldOutUntil);
            } else {
                product.markSoldOut();
            }
            productSavePort.save(product);
            succeeded.add(product.getId());
        }
        return ProductAvailabilityChangeResult.of(succeeded, loaded.failed());
    }

    public ProductAvailabilityChangeResult releaseProductsSoldOut(ShopId shopId, List<ProductId> productIds) {
        LoadedProducts loaded = loadProducts(shopId, productIds);

        List<Long> succeeded = new ArrayList<>();
        for (Product product : loaded.found()) {
            product.releaseSoldOut();
            productSavePort.save(product);
            succeeded.add(product.getId());
        }
        return ProductAvailabilityChangeResult.of(succeeded, loaded.failed());
    }

    public ProductAvailabilityChangeResult releaseProducts(
        ShopId shopId,
        List<ProductId> productIds,
        ReleaseTarget target
    ) {
        LoadedProducts loaded = loadProducts(shopId, productIds);

        List<Long> succeeded = new ArrayList<>();
        for (Product product : loaded.found()) {
            if (target == ReleaseTarget.SOLD_OUT || target == ReleaseTarget.ALL) {
                product.releaseSoldOut();
            }
            if (target == ReleaseTarget.HIDDEN || target == ReleaseTarget.ALL) {
                product.activate();
            }
            productSavePort.save(product);
            succeeded.add(product.getId());
        }
        return ProductAvailabilityChangeResult.of(succeeded, loaded.failed());
    }

    public ProductAvailabilityChangeResult changeProductsSoldOutUntil(
        ShopId shopId,
        List<ProductId> productIds,
        LocalDateTime soldOutUntil,
        LocalDateTime now
    ) {
        productSoldOutUntilValidator.validate(soldOutUntil, now);
        LoadedProducts loaded = loadProducts(shopId, productIds);
        List<ProductAvailabilityFailure> failed = new ArrayList<>(loaded.failed());

        List<Long> succeeded = new ArrayList<>();
        for (Product product : loaded.found()) {
            if (!product.isSoldOut()) {
                failed.add(ProductAvailabilityFailure.of(
                    product.getId(), product.getName(), DomainErrorCode.PRODUCT_NOT_SOLD_OUT));
                continue;
            }
            product.changeSoldOutUntil(soldOutUntil);
            productSavePort.save(product);
            succeeded.add(product.getId());
        }
        return ProductAvailabilityChangeResult.of(succeeded, failed);
    }

    private LoadedProducts loadProducts(ShopId shopId, List<ProductId> productIds) {
        List<ProductId> distinctIds = distinct(productIds);
        List<Product> found = productLoadPort.findAllByShopIdAndIdIn(shopId, distinctIds);

        Map<Long, Product> byId = new LinkedHashMap<>();
        found.forEach(product -> byId.put(product.getId(), product));

        List<ProductAvailabilityFailure> failed = new ArrayList<>();
        for (ProductId productId : distinctIds) {
            if (!byId.containsKey(productId.value())) {
                failed.add(ProductAvailabilityFailure.of(productId.value(), null, ApplicationErrorCode.PRODUCT_NOT_FOUND));
            }
        }
        return new LoadedProducts(List.copyOf(byId.values()), failed);
    }

    private void rejectFromTail(
        List<Product> candidates,
        Map<Long, ProductAvailabilityFailure> rejected,
        long shortfall,
        ErrorCodeSpec errorCode,
        java.util.function.Predicate<Product> predicate
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

    private <T> List<T> distinct(List<T> values) {
        return values == null ? List.of() : values.stream().filter(Objects::nonNull).distinct().toList();
    }

    private record LoadedProducts(
        List<Product> found,
        List<ProductAvailabilityFailure> failed
    ) {
    }
}
