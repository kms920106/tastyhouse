package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.model.ProductPriceSpec;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductPriceLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductPriceSavePort;
import com.tastyhouse.application.product.port.out.write.ProductSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class ProductPriceService {

    private final ProductPriceLoadPort productPriceLoadPort;
    private final ProductPriceSavePort productPriceSavePort;
    private final ProductLoadPort productLoadPort;
    private final ProductSavePort productSavePort;
    private final StorePriceVerificationPort storePriceVerificationPort;

    public ProductPriceService(
        ProductPriceLoadPort productPriceLoadPort,
        ProductPriceSavePort productPriceSavePort,
        ProductLoadPort productLoadPort,
        ProductSavePort productSavePort,
        StorePriceVerificationPort storePriceVerificationPort
    ) {
        this.productPriceLoadPort = productPriceLoadPort;
        this.productPriceSavePort = productPriceSavePort;
        this.productLoadPort = productLoadPort;
        this.productSavePort = productSavePort;
        this.storePriceVerificationPort = storePriceVerificationPort;
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

        List<ProductPrice> existing = productPriceLoadPort.findAllByProductId(productId);
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
                saved.add(productPriceSavePort.save(ProductPrice.of(
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

                .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_PRICE_NOT_FOUND,
                    ApplicationErrorCode.PRODUCT_PRICE_NOT_FOUND.getDefaultMessage() + ": " + spec.id()));

            target.change(
                spec.priceName(),
                spec.deliveryPrice(),
                spec.storePrice(),
                spec.pickupPrice(),
                spec.sort(),
                now
            );
            saved.add(productPriceSavePort.save(target));
            keptIds.add(spec.id());
        }

        List<ProductPriceId> removed = existing.stream()
            .filter(price -> !keptIds.contains(price.getId()))
            .map(ProductPrice::getProductPriceId)
            .toList();
        if (!removed.isEmpty()) {
            productPriceSavePort.deleteAllByIdIn(removed);
        }

        return saved.stream()
            .sorted(Comparator.comparingInt(price -> orZero(price.getSort())))
            .toList();
    }

    private static void validateSpecs(List<ProductPriceSpec> specs) {
        if (specs == null || specs.isEmpty()) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_PRICE_EMPTY);
        }

        if (specs.size() == 1) {
            return;
        }

        Set<String> names = new HashSet<>();
        for (ProductPriceSpec spec : specs) {
            String priceName = spec.priceName();
            if (priceName == null || priceName.isBlank()) {
                throw new DomainException(DomainErrorCode.PRODUCT_PRICE_NAME_REQUIRED);
            }
            if (!names.add(priceName)) {
                throw new ApplicationException(CeoErrorCode.PRODUCT_PRICE_NAME_DUPLICATED,
                    CeoErrorCode.PRODUCT_PRICE_NAME_DUPLICATED.getDefaultMessage() + ": " + priceName);
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
            throw new ApplicationException(CeoErrorCode.PRODUCT_PRICE_STORE_NOT_VERIFIED);
        }
    }

    private static void requireNoDiscountInProgress(Product product) {
        if (product.getDiscountPrice() != null) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_PRICE_DISCOUNT_IN_PROGRESS);
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
        productSavePort.save(product);
    }

    private void refreshStorePriceVerification(ShopId shopId) {
        if (!storePriceVerificationPort.isStorePriceVerified(shopId.value())) {
            return;
        }
        List<ProductPrice> violated = productPriceLoadPort.findAllByShopId(shopId).stream()
            .filter(ProductPrice::isDeliveryPriceHigherThanStorePrice)
            .toList();
        if (violated.isEmpty()) {
            return;
        }

        for (ProductPrice price : violated) {
            price.clearStoreAndPickupPrice();
            productPriceSavePort.save(price);
        }
        storePriceVerificationPort.clearStorePriceVerification(shopId.value());
    }

    private Product loadOwnedProduct(ShopId shopId, ProductId productId) {
        List<Product> found = productLoadPort.findAllByShopIdAndIdIn(shopId, List.of(productId));
        if (found.isEmpty()) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
        return found.getFirst();
    }

    private static int orZero(Integer value) {
        return value != null ? value : 0;
    }
}
