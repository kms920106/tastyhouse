package com.tastyhouse.domain.product.port;

public interface StorePriceVerificationPort {
    boolean isStorePriceVerified(Long shopId);

    void verifyStorePrice(Long shopId);

    void clearStorePriceVerification(Long shopId);
}
