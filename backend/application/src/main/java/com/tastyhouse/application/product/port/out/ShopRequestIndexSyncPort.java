package com.tastyhouse.application.product.port.out;

public interface ShopRequestIndexSyncPort {
    void syncStorePriceVerificationStatus(Long sourceRequestId, String status, String rejectReason);
}
