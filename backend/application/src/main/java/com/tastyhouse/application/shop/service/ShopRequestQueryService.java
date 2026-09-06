package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopRequestQueryUseCase;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.application.shop.port.out.ShopRequestAdjustmentDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;
import com.tastyhouse.application.shop.port.out.ShopRequestDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestDetailViewResult;
import com.tastyhouse.application.shop.port.out.ShopRequestListItemViewResult;
import com.tastyhouse.application.shop.port.out.ShopRequestTypeCatalogResult;
import com.tastyhouse.application.shop.port.out.ShopRequestTypeView;
import com.tastyhouse.application.shop.port.out.ShopRequestImageChangeDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestListItemResult;
import com.tastyhouse.application.shop.port.out.ShopRequestQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRequestReviewBlindDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestSearchCondition;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopRequestQueryService implements ShopRequestQueryUseCase {

    private final ShopRequestQueryPort shopRequestQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRequestQueryService(
        ShopRequestQueryPort shopRequestQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRequestQueryPort = shopRequestQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public PageResult<ShopRequestListItemViewResult> getRequests(
        Long ceoId,
        Long shopId,
        String requestType,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateDateRange(startDate, endDate);

        ShopRequestType requestTypeFilter = requestType == null ? null : ShopRequestType.from(requestType);
        ShopRequestStatus statusFilter = status == null ? null : ShopRequestStatus.from(status);

        ShopRequestSearchCondition condition = ShopRequestSearchCondition.of(
            shopId,
            requestTypeFilter,
            statusFilter,
            startDate,
            endDate
        );
        PageQuery pageQuery = PageQuery.of(page, size);

        return shopRequestQueryPort.findRequestPage(condition, pageQuery)
            .map(this::toListItemViewResult);
    }

    @Override
    public ShopRequestDetailViewResult getRequestDetail(Long ceoId, Long shopId, Long requestId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopRequestDetailResult detail = shopRequestQueryPort.findRequestDetail(requestId)
            .filter(row -> shopId.equals(row.shopId()))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));

        return switch (detail.requestType()) {
            case TRADEMARK_CHANGE, THUMBNAIL_CHANGE -> toImageChangeDetailResult(detail);
            case DELIVERY_AREA_ADJUSTMENT -> toAdjustmentDetailResult(detail);
            case REVIEW_BLIND -> toReviewBlindDetailResult(detail);
            case STORE_PRICE_VERIFICATION -> toStorePriceVerificationDetailResult(detail);
        };
    }

    @Override
    public List<ShopRequestCommentResult> getComments(Long ceoId, Long shopId, Long requestId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopRequestDetailResult detail = shopRequestQueryPort.findRequestDetail(requestId)
            .filter(row -> shopId.equals(row.shopId()))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));

        return shopRequestQueryPort.findComments(detail.requestId());
    }

    @Override
    public ShopRequestTypeCatalogResult getRequestTypes() {
        return new ShopRequestTypeCatalogResult(
            Arrays.stream(ShopRequestType.values())
                .map(requestType -> new ShopRequestTypeView(requestType, requestType.isContractAmending()))
                .toList(),
            Arrays.stream(ShopRequestStatus.values()).toList()
        );
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_DATE_RANGE_INVALID);
        }
    }

    private ShopRequestDetailViewResult toImageChangeDetailResult(ShopRequestDetailResult detail) {
        ShopRequestImageChangeDetailResult source =
            shopRequestQueryPort.findImageChangeDetail(detail.sourceRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));

        return toDetailViewResult(
            detail,
            toRequestStatus(source.status()),
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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));

        return toDetailViewResult(
            detail,
            toRequestStatus(source.status()),
            source.rejectReason(),
            null,
            null,
            source
        );
    }

    private ShopRequestDetailViewResult toStorePriceVerificationDetailResult(ShopRequestDetailResult detail) {
        return toDetailViewResult(
            detail,
            detail.status(),
            detail.rejectReason(),
            null,
            null,
            null
        );
    }

    private ShopRequestDetailViewResult toAdjustmentDetailResult(ShopRequestDetailResult detail) {
        ShopRequestAdjustmentDetailResult source =
            shopRequestQueryPort.findAdjustmentDetail(detail.sourceRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));

        return toDetailViewResult(
            detail,
            toRequestStatus(source.status()),
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
        ShopRequestType requestType = detail.requestType();
        return new ShopRequestDetailViewResult(
            detail.requestId(),
            requestType,
            detail.summary(),
            status,
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

    private ShopRequestListItemViewResult toListItemViewResult(ShopRequestListItemResult row) {
        return new ShopRequestListItemViewResult(
            row.requestId(),
            row.requestType(),
            row.summary(),
            row.status(),
            row.rejectReason(),
            row.requestType().isContractAmending(),
            row.hasAttachment(),
            row.commentCount(),
            row.requestedAt(),
            row.processedAt()
        );
    }
}
