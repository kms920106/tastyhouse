package com.tastyhouse.application.shop.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthorType;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.application.review.service.ReviewBlindRequestService;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.port.out.CodeLabelResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopRequestQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopRequestAdjustmentDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;
import com.tastyhouse.application.shop.port.out.ShopRequestDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestDetailViewResult;
import com.tastyhouse.application.shop.port.out.ShopRequestImageChangeDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestListItemResult;
import com.tastyhouse.application.shop.port.out.ShopRequestListItemViewResult;
import com.tastyhouse.application.shop.port.out.ShopRequestQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRequestReviewBlindDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestSearchCondition;
import com.tastyhouse.application.shop.port.out.ShopRequestTypeCatalogResult;
import com.tastyhouse.application.shop.port.out.ShopRequestTypeView;

@Service
@Transactional(readOnly = true)
class ShopRequestQueryService implements ShopRequestQueryUseCase {

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

        String requestTypeFilter = requestType == null ? null : ShopRequestType.from(requestType).name();
        String statusFilter = status == null ? null : ShopRequestStatus.from(status).name();

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
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));

        return switch (ShopRequestType.valueOf(detail.requestType())) {
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
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_REQUEST_NOT_FOUND));

        return withAuthorTypeDescriptions(shopRequestQueryPort.findComments(detail.requestId()));
    }

    private static List<ShopRequestCommentResult> withAuthorTypeDescriptions(List<ShopRequestCommentResult> comments) {
        return comments.stream()
            .map(comment -> comment.withAuthorTypeDescription(comment.authorType() == null
                ? null
                : ShopRequestCommentAuthorType.valueOf(comment.authorType()).getDescription()))
            .toList();
    }

    @Override
    public ShopRequestTypeCatalogResult getRequestTypes() {
        return new ShopRequestTypeCatalogResult(
            Arrays.stream(ShopRequestType.values())
                .map(requestType -> new ShopRequestTypeView(
                    requestType.name(),
                    requestType.getDescription(),
                    requestType.isContractAmending()
                ))
                .toList(),
            Arrays.stream(ShopRequestStatus.values())
                .map(status -> new CodeLabelResult(status.name(), status.getDescription()))
                .toList()
        );
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ApplicationException(CeoErrorCode.SHOP_REQUEST_DATE_RANGE_INVALID);
        }
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

    private ShopRequestListItemViewResult toListItemViewResult(ShopRequestListItemResult row) {
        ShopRequestType requestType = ShopRequestType.valueOf(row.requestType());
        ShopRequestStatus status = ShopRequestStatus.valueOf(row.status());
        return new ShopRequestListItemViewResult(
            row.requestId(),
            requestType.name(),
            requestType.getDescription(),
            row.summary(),
            status.name(),
            status.getDescription(),
            row.rejectReason(),
            requestType.isContractAmending(),
            row.hasAttachment(),
            row.commentCount(),
            row.requestedAt(),
            row.processedAt()
        );
    }
}
