package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestRepository;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaAdjustmentRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopImageChangeRequestRepository;

public class ShopRequestCancelService {
    private final ShopImageChangeRequestRepository shopImageChangeRequestRepository;
    private final ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository;
    private final ReviewBlindRequestRepository reviewBlindRequestRepository;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopRequestCancelService(
        ShopImageChangeRequestRepository shopImageChangeRequestRepository,
        ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository,
        ReviewBlindRequestRepository reviewBlindRequestRepository,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.shopImageChangeRequestRepository = shopImageChangeRequestRepository;
        this.shopDeliveryAreaAdjustmentRequestRepository = shopDeliveryAreaAdjustmentRequestRepository;
        this.reviewBlindRequestRepository = reviewBlindRequestRepository;
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
        ShopImageChangeRequest request = shopImageChangeRequestRepository.findById(sourceRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));
        request.cancel();
        shopImageChangeRequestRepository.save(request);
    }

    private void cancelReviewBlind(Long sourceRequestId) {
        ReviewBlindRequest request = reviewBlindRequestRepository
            .findById(ReviewBlindRequestId.of(sourceRequestId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));
        if (request.getStatus() != ReviewBlindStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_NOT_CANCELABLE);
        }
        request.cancel();
        reviewBlindRequestRepository.save(request);
    }

    private void cancelAdjustment(Long sourceRequestId) {
        ShopDeliveryAreaAdjustmentRequest request = shopDeliveryAreaAdjustmentRequestRepository.findById(sourceRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));
        request.cancel();
        shopDeliveryAreaAdjustmentRequestRepository.save(request);
    }
}
