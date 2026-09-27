package com.tastyhouse.application.product.store;

import com.tastyhouse.application.product.port.out.write.StorePriceVerificationItemState;
import com.tastyhouse.application.product.port.out.write.StorePriceVerificationState;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class StorePriceVerificationStateMapper {
    private StorePriceVerificationStateMapper() {
    }

    static StorePriceVerification toDomain(StorePriceVerificationState state) {
        return StorePriceVerification.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.priceListFileId() == null ? null : UploadedFileId.of(state.priceListFileId()),
            state.status() == null ? null : StorePriceVerificationStatus.valueOf(state.status()),
            state.rejectReason(),
            state.requestedByCeoId(),
            state.processedAt(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static StorePriceVerificationState toState(StorePriceVerification verification) {
        return new StorePriceVerificationState(
            verification.getId(),
            verification.getShopId() == null ? null : verification.getShopId().value(),
            verification.getPriceListFileId() == null ? null : verification.getPriceListFileId().value(),
            verification.getStatus() == null ? null : verification.getStatus().name(),
            verification.getRejectReason(),
            verification.getRequestedByCeoId(),
            verification.getProcessedAt(),
            verification.getCreatedAt(),
            verification.getUpdatedAt()
        );
    }

    static StorePriceVerificationItem toDomain(StorePriceVerificationItemState state) {
        return StorePriceVerificationItem.reconstitute(
            state.id(),
            state.verificationId() == null ? null : StorePriceVerificationId.of(state.verificationId()),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.productPriceId() == null ? null : ProductPriceId.of(state.productPriceId()),
            state.storePrice(),
            state.applyPickupSamePrice(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static StorePriceVerificationItemState toState(StorePriceVerificationItem item) {
        return new StorePriceVerificationItemState(
            item.getId(),
            item.getVerificationId() == null ? null : item.getVerificationId().value(),
            item.getProductId() == null ? null : item.getProductId().value(),
            item.getProductPriceId() == null ? null : item.getProductPriceId().value(),
            item.getStorePrice(),
            item.isApplyPickupSamePrice(),
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }
}
