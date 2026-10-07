package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.application.review.service.ReviewBlindRequestService;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopRequestDetailQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopRequestAdjustmentDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestDetailViewResult;
import com.tastyhouse.application.shop.port.out.ShopRequestImageChangeDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRequestReviewBlindDetailResult;

@Service
@Transactional(readOnly = true)
class ShopRequestDetailQueryService implements ShopRequestDetailQueryUseCase {

    private final ShopRequestQueryPort shopRequestQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRequestDetailQueryService(
        ShopRequestQueryPort shopRequestQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRequestQueryPort = shopRequestQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopRequestDetailViewResult getRequestDetail(Long ceoId, Long shopId, Long requestId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopRequestDetailResult detail = shopRequestQueryPort.findRequestDetail(requestId)
            .filter(row -> shopId.equals(row.shopId()))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));

        return switch (ShopRequestType.valueOf(detail.requestType())) {
            case TRADEMARK_CHANGE, THUMBNAIL_CHANGE -> toImageChangeDetailResult(detail);
            case DELIVERY_AREA_ADJUSTMENT -> toAdjustmentDetailResult(detail);
            case REVIEW_BLIND -> toReviewBlindDetailResult(detail);
            case STORE_PRICE_VERIFICATION -> toStorePriceVerificationDetailResult(detail);
        };
    }

    private ShopRequestDetailViewResult toImageChangeDetailResult(ShopRequestDetailResult detail) {
        ShopRequestImageChangeDetailResult source =
            shopRequestQueryPort.findImageChangeDetail(detail.sourceRequestId())
                .map(result -> result.withImageTypeDescription(result.imageType() == null
                    ? null
                    : ShopImageType.valueOf(result.imageType()).getDescription()))
                .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));

        return toDetailViewResult(
            detail,
            toRequestStatus(ApprovalStatus.valueOf(source.status())),
            source.rejectReason(),
            source,
            null,
            null
        );
    }

    private ShopRequestStatus toRequestStatus(ApprovalStatus status) {
        return switch (status) {
            case PENDING -> ShopRequestStatus.PENDING;
            case APPROVED -> ShopRequestStatus.APPROVED;
            case REJECTED -> ShopRequestStatus.REJECTED;
            case CANCELED -> ShopRequestStatus.CANCELED;
        };
    }

    private ShopRequestDetailViewResult toReviewBlindDetailResult(ShopRequestDetailResult detail) {
        ShopRequestReviewBlindDetailResult source =
            shopRequestQueryPort.findReviewBlindDetail(detail.sourceRequestId())
                .map(result -> result.withReasonDescription(result.reason() == null
                    ? null
                    : ReviewBlindReason.valueOf(result.reason()).getDescription()))
                .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));

        return toDetailViewResult(
            detail,
            ReviewBlindRequestService.toShopRequestStatus(ReviewBlindStatus.valueOf(source.status())),
            source.rejectReason(),
            null,
            null,
            source
        );
    }

    private ShopRequestDetailViewResult toStorePriceVerificationDetailResult(ShopRequestDetailResult detail) {
        return toDetailViewResult(
            detail,
            ShopRequestStatus.valueOf(detail.status()),
            detail.rejectReason(),
            null,
            null,
            null
        );
    }

    private ShopRequestDetailViewResult toAdjustmentDetailResult(ShopRequestDetailResult detail) {
        ShopRequestAdjustmentDetailResult source =
            shopRequestQueryPort.findAdjustmentDetail(detail.sourceRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));

        return toDetailViewResult(
            detail,
            toRequestStatus(DeliveryAreaAdjustmentStatus.valueOf(source.status())),
            source.rejectReason(),
            null,
            source,
            null
        );
    }

    private ShopRequestStatus toRequestStatus(DeliveryAreaAdjustmentStatus status) {
        return switch (status) {
            case PENDING -> ShopRequestStatus.PENDING;
            case IN_PROGRESS -> ShopRequestStatus.IN_PROGRESS;
            case COMPLETED -> ShopRequestStatus.APPROVED;
            case REJECTED -> ShopRequestStatus.REJECTED;
            case CANCELED -> ShopRequestStatus.CANCELED;
        };
    }

    private ShopRequestDetailViewResult toDetailViewResult(
        ShopRequestDetailResult detail,
        ShopRequestStatus status,
        String rejectReason,
        ShopRequestImageChangeDetailResult imageChange,
        ShopRequestAdjustmentDetailResult deliveryAreaAdjustment,
        ShopRequestReviewBlindDetailResult reviewBlind
    ) {
        ShopRequestType requestType = ShopRequestType.valueOf(detail.requestType());
        return new ShopRequestDetailViewResult(
            detail.requestId(),
            requestType.name(),
            requestType.getDescription(),
            detail.summary(),
            status.name(),
            status.getDescription(),
            rejectReason,
            requestType.isContractAmending(),
            detail.attachmentUrl() != null,
            detail.commentCount(),
            detail.requestedAt(),
            detail.processedAt(),
            requestType.getAttachmentLabel(),
            detail.attachmentUrl(),
            imageChange,
            deliveryAreaAdjustment,
            reviewBlind
        );
    }
}
