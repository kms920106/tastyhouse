package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestPersistencePort;

@Service
public class ShopRequestCancellationService {

    private final ShopImageChangeRequestPersistencePort shopImageChangeRequestPersistencePort;
    private final ShopDeliveryAreaAdjustmentRequestPersistencePort shopDeliveryAreaAdjustmentRequestPersistencePort;
    private final ReviewBlindRequestPersistencePort reviewBlindRequestPersistencePort;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopRequestCancellationService(
        ShopImageChangeRequestPersistencePort shopImageChangeRequestPersistencePort,
        ShopDeliveryAreaAdjustmentRequestPersistencePort shopDeliveryAreaAdjustmentRequestPersistencePort,
        ReviewBlindRequestPersistencePort reviewBlindRequestPersistencePort,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.shopImageChangeRequestPersistencePort = shopImageChangeRequestPersistencePort;
        this.shopDeliveryAreaAdjustmentRequestPersistencePort = shopDeliveryAreaAdjustmentRequestPersistencePort;
        this.reviewBlindRequestPersistencePort = reviewBlindRequestPersistencePort;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
    }

    public void cancel(Long requestId, Long shopId) {
        ShopRequestIndex index = shopRequestIndexRecorder.getRequestOfShop(requestId, shopId);
        Long sourceRequestId = index.getSourceRequestId();

        switch (index.getRequestType()) {
            case TRADEMARK_CHANGE, THUMBNAIL_CHANGE -> cancelImageChange(sourceRequestId);
            case DELIVERY_AREA_ADJUSTMENT -> cancelAdjustment(sourceRequestId);
            case REVIEW_BLIND -> cancelReviewBlind(sourceRequestId);
        }

        shopRequestIndexRecorder.syncCanceled(index.getRequestType(), sourceRequestId);
    }

    private void cancelImageChange(Long sourceRequestId) {
        ShopImageChangeRequest request = shopImageChangeRequestPersistencePort.findById(sourceRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));
        request.cancel();
        shopImageChangeRequestPersistencePort.save(request);
    }

    private void cancelReviewBlind(Long sourceRequestId) {
        ReviewBlindRequest request = reviewBlindRequestPersistencePort
            .findById(ReviewBlindRequestId.of(sourceRequestId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));
        if (request.getStatus() != ReviewBlindStatus.PENDING) {
            throw new DomainException(DomainErrorCode.SHOP_REQUEST_NOT_CANCELABLE);
        }
        request.cancel();
        reviewBlindRequestPersistencePort.save(request);
    }

    private void cancelAdjustment(Long sourceRequestId) {
        ShopDeliveryAreaAdjustmentRequest request = shopDeliveryAreaAdjustmentRequestPersistencePort.findById(sourceRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));
        request.cancel();
        shopDeliveryAreaAdjustmentRequestPersistencePort.save(request);
    }
}
