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
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestLoadPort;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestSavePort;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestSavePort;

@Service
public class ShopRequestCancellationService {

    private final ShopImageChangeRequestLoadPort shopImageChangeRequestLoadPort;
    private final ShopImageChangeRequestSavePort shopImageChangeRequestSavePort;
    private final ShopDeliveryAreaAdjustmentRequestLoadPort shopDeliveryAreaAdjustmentRequestLoadPort;
    private final ShopDeliveryAreaAdjustmentRequestSavePort shopDeliveryAreaAdjustmentRequestSavePort;
    private final ReviewBlindRequestLoadPort reviewBlindRequestLoadPort;
    private final ReviewBlindRequestSavePort reviewBlindRequestSavePort;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopRequestCancellationService(
        ShopImageChangeRequestLoadPort shopImageChangeRequestLoadPort,
        ShopImageChangeRequestSavePort shopImageChangeRequestSavePort,
        ShopDeliveryAreaAdjustmentRequestLoadPort shopDeliveryAreaAdjustmentRequestLoadPort,
        ShopDeliveryAreaAdjustmentRequestSavePort shopDeliveryAreaAdjustmentRequestSavePort,
        ReviewBlindRequestLoadPort reviewBlindRequestLoadPort,
        ReviewBlindRequestSavePort reviewBlindRequestSavePort,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.shopImageChangeRequestLoadPort = shopImageChangeRequestLoadPort;
        this.shopImageChangeRequestSavePort = shopImageChangeRequestSavePort;
        this.shopDeliveryAreaAdjustmentRequestLoadPort = shopDeliveryAreaAdjustmentRequestLoadPort;
        this.shopDeliveryAreaAdjustmentRequestSavePort = shopDeliveryAreaAdjustmentRequestSavePort;
        this.reviewBlindRequestLoadPort = reviewBlindRequestLoadPort;
        this.reviewBlindRequestSavePort = reviewBlindRequestSavePort;
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
        ShopImageChangeRequest request = shopImageChangeRequestLoadPort.findById(sourceRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));
        request.cancel();
        shopImageChangeRequestSavePort.save(request);
    }

    private void cancelReviewBlind(Long sourceRequestId) {
        ReviewBlindRequest request = reviewBlindRequestLoadPort
            .findById(ReviewBlindRequestId.of(sourceRequestId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));
        if (request.getStatus() != ReviewBlindStatus.PENDING) {
            throw new DomainException(DomainErrorCode.SHOP_REQUEST_NOT_CANCELABLE);
        }
        request.cancel();
        reviewBlindRequestSavePort.save(request);
    }

    private void cancelAdjustment(Long sourceRequestId) {
        ShopDeliveryAreaAdjustmentRequest request = shopDeliveryAreaAdjustmentRequestLoadPort.findById(sourceRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));
        request.cancel();
        shopDeliveryAreaAdjustmentRequestSavePort.save(request);
    }
}
