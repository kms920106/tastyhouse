package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shared.model.OrderMethod;

public class ProductPrice {
    private static final int PRICE_NAME_MAX_LENGTH = 50;

    private final Long id;
    private final ProductId productId;
    private String priceName;
    private Integer deliveryPrice;
    private Integer storePrice;
    private Integer pickupPrice;
    private Integer sort;

    private LocalDateTime pickupPriceSetAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductPrice(
        Long id,
        ProductId productId,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime pickupPriceSetAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.priceName = priceName;
        this.deliveryPrice = deliveryPrice;
        this.storePrice = storePrice;
        this.pickupPrice = pickupPrice;
        this.sort = sort;
        this.pickupPriceSetAt = pickupPriceSetAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductPrice of(
        ProductId productId,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime pickupPriceSetAt
    ) {
        validatePriceName(priceName);
        validatePrices(deliveryPrice, storePrice, pickupPrice);

        return new ProductPrice(
            null,
            productId,
            priceName,
            deliveryPrice,
            storePrice,
            pickupPrice,
            sort,
            pickupPrice != null ? pickupPriceSetAt : null,
            null,
            null
        );
    }

    public static ProductPrice reconstitute(
        Long id,
        ProductId productId,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime pickupPriceSetAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductPrice(
            id,
            productId,
            priceName,
            deliveryPrice,
            storePrice,
            pickupPrice,
            sort,
            pickupPriceSetAt,
            createdAt,
            updatedAt
        );
    }

    public int resolvePrice(OrderMethod orderMethod) {
        if (orderMethod == OrderMethod.TAKEOUT && this.pickupPrice != null) {
            return this.pickupPrice;
        }
        return this.deliveryPrice;
    }

    public StorePriceUnverifiedReason resolveUnverifiedReason() {
        if (this.storePrice == null) {
            return StorePriceUnverifiedReason.STORE_PRICE_NOT_REGISTERED;
        }
        if (this.deliveryPrice != null && this.deliveryPrice > this.storePrice) {
            return StorePriceUnverifiedReason.DELIVERY_PRICE_HIGHER_THAN_STORE;
        }
        return null;
    }

    public boolean isDeliveryPriceHigherThanStorePrice() {
        return this.storePrice != null && this.deliveryPrice != null && this.deliveryPrice > this.storePrice;
    }

    public boolean hasStoreAndPickupPrice() {
        return this.storePrice != null && this.pickupPrice != null;
    }

    public boolean isPickupPriceWithinStorePrice() {
        return this.storePrice != null && this.pickupPrice != null && this.pickupPrice <= this.storePrice;
    }

    public void applyVerifiedStorePrice(Integer storePrice, boolean applyPickupSamePrice, LocalDateTime now) {
        validatePrices(this.deliveryPrice, storePrice, applyPickupSamePrice ? storePrice : this.pickupPrice);

        this.storePrice = storePrice;
        if (applyPickupSamePrice) {
            this.pickupPrice = storePrice;
            this.pickupPriceSetAt = now;
        }
    }

    public void change(
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime now
    ) {
        validatePriceName(priceName);
        validatePrices(deliveryPrice, storePrice, pickupPrice);

        this.priceName = priceName;
        this.deliveryPrice = deliveryPrice;
        this.storePrice = storePrice;
        this.sort = sort;

        if (pickupPrice == null) {
            this.pickupPrice = null;
            this.pickupPriceSetAt = null;
        } else {
            if (!pickupPrice.equals(this.pickupPrice)) {
                this.pickupPriceSetAt = now;
            }
            this.pickupPrice = pickupPrice;
        }
    }

    public void clearStoreAndPickupPrice() {
        this.storePrice = null;
        this.pickupPrice = null;
        this.pickupPriceSetAt = null;
    }

    private static void validatePriceName(String priceName) {
        if (priceName != null && priceName.length() > PRICE_NAME_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_NAME_REQUIRED,
                "가격명은 " + PRICE_NAME_MAX_LENGTH + "자 이내여야 합니다.");
        }
    }

    private static void validatePrices(Integer deliveryPrice, Integer storePrice, Integer pickupPrice) {
        requireNonNegative(deliveryPrice, "배달 가격");
        requireNonNegative(storePrice, "매장 가격");
        requireNonNegative(pickupPrice, "픽업 가격");

        if (deliveryPrice == null) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_NEGATIVE,
                ErrorCode.PRODUCT_PRICE_NEGATIVE.getDefaultMessage() + " 배달 가격은 필수입니다.");
        }
    }

    private static void requireNonNegative(Integer price, String label) {
        if (price != null && price < 0) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_NEGATIVE,
                ErrorCode.PRODUCT_PRICE_NEGATIVE.getDefaultMessage() + " " + label + ": " + price);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public String getPriceName() {
        return this.priceName;
    }

    public Integer getDeliveryPrice() {
        return this.deliveryPrice;
    }

    public Integer getStorePrice() {
        return this.storePrice;
    }

    public Integer getPickupPrice() {
        return this.pickupPrice;
    }

    public Integer getSort() {
        return this.sort;
    }

    public LocalDateTime getPickupPriceSetAt() {
        return this.pickupPriceSetAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public ProductPriceId getProductPriceId() {
        return ProductPriceId.of(this.id);
    }
}
