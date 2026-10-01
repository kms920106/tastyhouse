package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.product.service.StorePriceUnverifiedItem;
import com.tastyhouse.domain.product.service.StorePriceVerificationItemSpec;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.ShopRequestIndexSyncPort;
import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPricePersistencePort;
import com.tastyhouse.application.product.port.out.write.StorePriceVerificationPersistencePort;
import com.tastyhouse.application.shared.marker.SharedApp;

@SharedApp
public class StorePriceVerificationService {

    private static final List<StorePriceVerificationStatus> OPEN_STATUSES =
        List.of(StorePriceVerificationStatus.PENDING, StorePriceVerificationStatus.IN_PROGRESS);

    private final StorePriceVerificationPersistencePort verificationPersistencePort;
    private final ProductPricePersistencePort productPricePersistencePort;
    private final ProductPersistencePort productPersistencePort;
    private final StorePriceVerificationPort storePriceVerificationPort;
    private final ShopRequestIndexSyncPort shopRequestIndexSyncPort;

    public StorePriceVerificationService(
        StorePriceVerificationPersistencePort verificationPersistencePort,
        ProductPricePersistencePort productPricePersistencePort,
        ProductPersistencePort productPersistencePort,
        StorePriceVerificationPort storePriceVerificationPort,
        ShopRequestIndexSyncPort shopRequestIndexSyncPort
    ) {
        this.verificationPersistencePort = verificationPersistencePort;
        this.productPricePersistencePort = productPricePersistencePort;
        this.productPersistencePort = productPersistencePort;
        this.storePriceVerificationPort = storePriceVerificationPort;
        this.shopRequestIndexSyncPort = shopRequestIndexSyncPort;
    }

    public StorePriceVerification request(
        ShopId shopId,
        UploadedFileId priceListFileId,
        List<StorePriceVerificationItemSpec> items,
        Long requestedByCeoId
    ) {
        if (verificationPersistencePort.existsByShopIdAndStatusIn(shopId, OPEN_STATUSES)) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_IN_PROGRESS);
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY);
        }

        List<ResolvedItem> resolved = resolveItems(shopId, items);

        StorePriceVerification saved = verificationPersistencePort.save(
            StorePriceVerification.of(shopId, priceListFileId, requestedByCeoId));

        for (ResolvedItem item : resolved) {
            verificationPersistencePort.saveItem(StorePriceVerificationItem.of(
                saved.getVerificationId(),
                item.productId(),
                item.productPriceId(),
                item.storePrice(),
                item.applyPickupSamePrice()
            ));
        }
        return saved;
    }

    private List<ResolvedItem> resolveItems(ShopId shopId, List<StorePriceVerificationItemSpec> items) {
        List<ResolvedItem> resolved = new ArrayList<>();
        Map<Long, Product> productCache = new LinkedHashMap<>();

        for (StorePriceVerificationItemSpec item : items) {
            ProductId productId = ProductId.of(item.productId());
            Product product = productCache.computeIfAbsent(item.productId(),
                key -> loadOwnedProduct(shopId, productId));

            if (product.getDiscountPrice() != null) {
                throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_DISCOUNT_IN_PROGRESS,
                    ErrorCode.SHOP_STORE_PRICE_VERIFICATION_DISCOUNT_IN_PROGRESS.getDefaultMessage()
                        + ": " + product.getName());
            }

            ProductPrice price = productPricePersistencePort.findById(ProductPriceId.of(item.priceId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_PRICE_NOT_FOUND));

            if (!price.getProductId().equals(productId)) {
                throw new ResourceNotFoundException(ErrorCode.PRODUCT_PRICE_NOT_FOUND);
            }

            resolved.add(new ResolvedItem(
                productId,
                price.getProductPriceId(),
                item.storePrice(),
                item.applyPickupSamePrice()
            ));
        }
        return resolved;
    }

    public void startReview(StorePriceVerificationId verificationId, LocalDateTime now) {
        StorePriceVerification verification = loadVerification(verificationId);
        verification.startReview(now);
        verificationPersistencePort.save(verification);
        syncIndex(verification, null);
    }

    public void approve(StorePriceVerificationId verificationId, LocalDateTime now) {
        StorePriceVerification verification = loadVerification(verificationId);

        for (StorePriceVerificationItem item : verificationPersistencePort
            .findAllItemsByVerificationId(verificationId)) {
            ProductPrice price = productPricePersistencePort.findById(item.getProductPriceId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_PRICE_NOT_FOUND));
            price.applyVerifiedStorePrice(item.getStorePrice(), item.isApplyPickupSamePrice(), now);
            productPricePersistencePort.save(price);
        }

        verification.approve(now);
        verificationPersistencePort.save(verification);
        syncIndex(verification, null);

        storePriceVerificationPort.verifyStorePrice(verification.getShopId().value());
    }

    public void reject(StorePriceVerificationId verificationId, String rejectReason, LocalDateTime now) {
        StorePriceVerification verification = loadVerification(verificationId);
        verification.reject(rejectReason, now);
        verificationPersistencePort.save(verification);
        syncIndex(verification, rejectReason);
    }

    public void cancel(StorePriceVerificationId verificationId, LocalDateTime now) {
        StorePriceVerification verification = loadVerification(verificationId);
        verification.cancel(now);
        verificationPersistencePort.save(verification);
        syncIndex(verification, null);
    }

    public List<StorePriceUnverifiedItem> findUnverifiedItems(ShopId shopId) {
        Map<Long, StorePriceUnverifiedItem> byProductId = new LinkedHashMap<>();

        for (ProductPrice price : productPricePersistencePort.findAllByShopId(shopId)) {
            var reason = price.resolveUnverifiedReason();
            if (reason == null) {
                continue;
            }
            Long productId = price.getProductId().value();
            if (byProductId.containsKey(productId)) {
                continue;
            }
            Product product = productPersistencePort.findById(price.getProductId()).orElse(null);
            if (product == null || product.isDeleted()) {
                continue;
            }
            byProductId.put(productId, new StorePriceUnverifiedItem(productId, product.getName(), reason));
        }
        return List.copyOf(byProductId.values());
    }

    private void syncIndex(StorePriceVerification verification, String rejectReason) {
        shopRequestIndexSyncPort.syncStorePriceVerificationStatus(
            verification.getId(),
            toShopRequestStatusName(verification.getStatus()),
            rejectReason
        );
    }

    private static String toShopRequestStatusName(StorePriceVerificationStatus status) {
        return switch (status) {
            case PENDING -> "PENDING";
            case IN_PROGRESS -> "IN_PROGRESS";
            case APPROVED -> "APPROVED";
            case REJECTED -> "REJECTED";
            case CANCELED -> "CANCELED";
        };
    }

    private StorePriceVerification loadVerification(StorePriceVerificationId verificationId) {
        return verificationPersistencePort.findById(verificationId)
            .orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.SHOP_STORE_PRICE_VERIFICATION_NOT_FOUND));
    }

    private Product loadOwnedProduct(ShopId shopId, ProductId productId) {
        List<Product> found = productPersistencePort.findAllByShopIdAndIdIn(shopId, List.of(productId));
        if (found.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return found.getFirst();
    }

    private record ResolvedItem(
        ProductId productId,
        ProductPriceId productPriceId,
        Integer storePrice,
        boolean applyPickupSamePrice
    ) {
    }
}
