package com.tastyhouse.application.shop.service;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.application.product.port.out.ShopRequestIndexSyncPort;

public class ShopRequestIndexSyncAdapter implements ShopRequestIndexSyncPort {

    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopRequestIndexSyncAdapter(ShopRequestIndexRecorder shopRequestIndexRecorder) {
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
    }

    @Override
    public void syncStorePriceVerificationStatus(Long sourceRequestId, String status, String rejectReason) {
        shopRequestIndexRecorder.syncRequestStatus(
            ShopRequestType.STORE_PRICE_VERIFICATION,
            sourceRequestId,
            ShopRequestStatus.from(status),
            rejectReason
        );
    }
}
