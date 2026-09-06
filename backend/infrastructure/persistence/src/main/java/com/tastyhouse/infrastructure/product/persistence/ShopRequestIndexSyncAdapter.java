package com.tastyhouse.infrastructure.product.persistence;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.port.ShopRequestIndexSyncPort;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;

@Component
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
