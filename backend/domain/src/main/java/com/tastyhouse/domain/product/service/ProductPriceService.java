package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.port.StorePriceVerificationPort;
import com.tastyhouse.domain.product.repository.ProductPriceRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductPriceService {
    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;
    private final StorePriceVerificationPort storePriceVerificationPort;

    public ProductPriceService(
        ProductPriceRepository productPriceRepository,
        ProductRepository productRepository,
        StorePriceVerificationPort storePriceVerificationPort
    ) {
        this.productPriceRepository = productPriceRepository;
        this.productRepository = productRepository;
        this.storePriceVerificationPort = storePriceVerificationPort;
    }

    public List<ProductPrice> findPrices(ShopId shopId, ProductId productId) {
        loadOwnedProduct(shopId, productId);
        return productPriceRepository.findAllByProductId(productId);
    }

    public void replacePrices(
        ShopId shopId,
        ProductId productId,
        List<ProductPriceSpec> specs,
        LocalDateTime now
    ) {
        Product product = loadOwnedProduct(shopId, productId);

        requireNoDiscountInProgress(product);
        validateSpecs(specs);
        requireVerifiedIfStoreOrPickupPriceGiven(shopId, specs);

        List<ProductPrice> existing = productPriceRepository.findAllByProductId(productId);
        List<ProductPrice> saved = applySpecs(productId, specs, existing, now);

        syncOriginalPrice(product, saved);
        refreshStorePriceVerification(shopId);
    }

    private List<ProductPrice> applySpecs(
        ProductId productId,
        List<ProductPriceSpec> specs,
        List<ProductPrice> existing,
        LocalDateTime now
    ) {
        Set<Long> keptIds = new LinkedHashSet<>();
        List<ProductPrice> saved = new ArrayList<>();

        for (ProductPriceSpec spec : specs) {
            if (spec.id() == null) {
                saved.add(productPriceRepository.save(ProductPrice.of(
                    productId,
                    spec.priceName(),
                    spec.deliveryPrice(),
                    spec.storePrice(),
                    spec.pickupPrice(),
                    spec.sort(),
                    now
                )));
                continue;
            }

            ProductPrice target = existing.stream()
                .filter(price -> spec.id().equals(price.getId()))
                .findFirst()

                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_PRICE_NOT_FOUND,
                    ErrorCode.PRODUCT_PRICE_NOT_FOUND.getDefaultMessage() + ": " + spec.id()));

            target.change(
                spec.priceName(),
                spec.deliveryPrice(),
                spec.storePrice(),
                spec.pickupPrice(),
                spec.sort(),
                now
            );
            saved.add(productPriceRepository.save(target));
            keptIds.add(spec.id());
        }

        List<ProductPriceId> removed = existing.stream()
            .filter(price -> !keptIds.contains(price.getId()))
            .map(ProductPrice::getProductPriceId)
            .toList();
        if (!removed.isEmpty()) {
            productPriceRepository.deleteAllByIdIn(removed);
        }

        return saved.stream()
            .sorted(Comparator.comparingInt(price -> orZero(price.getSort())))
            .toList();
    }

    private static void validateSpecs(List<ProductPriceSpec> specs) {
        if (specs == null || specs.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_EMPTY);
        }

        if (specs.size() == 1) {
            return;
        }

        Set<String> names = new HashSet<>();
        for (ProductPriceSpec spec : specs) {
            String priceName = spec.priceName();
            if (priceName == null || priceName.isBlank()) {
                throw new BusinessException(ErrorCode.PRODUCT_PRICE_NAME_REQUIRED);
            }
            if (!names.add(priceName)) {
                throw new BusinessException(ErrorCode.PRODUCT_PRICE_NAME_DUPLICATED,
                    ErrorCode.PRODUCT_PRICE_NAME_DUPLICATED.getDefaultMessage() + ": " + priceName);
            }
        }
    }

    private void requireVerifiedIfStoreOrPickupPriceGiven(ShopId shopId, List<ProductPriceSpec> specs) {
        boolean given = specs.stream()
            .anyMatch(spec -> spec.storePrice() != null || spec.pickupPrice() != null);
        if (!given) {
            return;
        }
        if (!storePriceVerificationPort.isStorePriceVerified(shopId.value())) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_STORE_NOT_VERIFIED);
        }
    }

    private static void requireNoDiscountInProgress(Product product) {
        if (product.getDiscountPrice() != null) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_DISCOUNT_IN_PROGRESS);
        }
    }

    private void syncOriginalPrice(Product product, List<ProductPrice> saved) {
        if (saved.isEmpty()) {
            return;
        }
        Integer basePrice = saved.getFirst().getDeliveryPrice();
        if (basePrice == null) {
            return;
        }
        product.syncOriginalPrice(basePrice);
        productRepository.save(product);
    }

    private void refreshStorePriceVerification(ShopId shopId) {
        if (!storePriceVerificationPort.isStorePriceVerified(shopId.value())) {
            return;
        }
        List<ProductPrice> violated = productPriceRepository.findAllByShopId(shopId).stream()
            .filter(ProductPrice::isDeliveryPriceHigherThanStorePrice)
            .toList();
        if (violated.isEmpty()) {
            return;
        }

        for (ProductPrice price : violated) {
            price.clearStoreAndPickupPrice();
            productPriceRepository.save(price);
        }
        storePriceVerificationPort.clearStorePriceVerification(shopId.value());
    }

    private Product loadOwnedProduct(ShopId shopId, ProductId productId) {
        List<Product> found = productRepository.findAllByShopIdAndIdIn(shopId, List.of(productId));
        if (found.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return found.getFirst();
    }

    private static int orZero(Integer value) {
        return value != null ? value : 0;
    }
}
