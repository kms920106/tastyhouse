package com.tastyhouse.domain.product.port;

public interface ShopRequestIndexSyncPort {
    void syncStorePriceVerificationStatus(Long sourceRequestId, String status, String rejectReason);
}
