package com.tastyhouse.application.product.port.out;

public interface StorePriceVerificationPort {
    boolean isStorePriceVerified(Long shopId);

    void verifyStorePrice(Long shopId);

    void clearStorePriceVerification(Long shopId);
}
