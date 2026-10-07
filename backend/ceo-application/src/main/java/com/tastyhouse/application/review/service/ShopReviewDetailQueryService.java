package com.tastyhouse.application.review.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.review.port.in.ShopReviewDetailQueryUseCase;
import com.tastyhouse.application.review.port.out.ShopReviewDetailViewResult;
import com.tastyhouse.application.review.port.out.ShopReviewManagementDetailResult;
import com.tastyhouse.application.review.port.out.ShopReviewManagementQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewReplyWindow;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ShopReviewDetailQueryService implements ShopReviewDetailQueryUseCase {

    private final ShopReviewManagementQueryPort shopReviewManagementQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopReviewDetailQueryService(
        ShopReviewManagementQueryPort shopReviewManagementQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopReviewManagementQueryPort = shopReviewManagementQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopReviewDetailViewResult getReviewDetail(Long ceoId, Long shopId, Long reviewId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopReviewManagementDetailResult detail =
            shopReviewManagementQueryPort.findShopReviewDetail(ReviewId.of(reviewId).value())
                .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));
        if (!shopId.equals(detail.shopId())) {
            throw new ApplicationException(ApplicationErrorCode.SHOP_ACCESS_DENIED);
        }

        return toDetailViewResult(detail.withDescriptions(
            orderMethodDisplayName(detail.orderMethod()),
            detail.blindRequests().stream()
                .map(history -> history.withDescriptions(
                    blindReasonDescription(history.reason()),
                    blindStatusDescription(history.status())
                ))
                .toList()
        ));
    }

    private static String orderMethodDisplayName(String orderMethod) {
        return orderMethod == null ? null : OrderMethod.valueOf(orderMethod).getDisplayName();
    }

    private static String blindReasonDescription(String reason) {
        return reason == null ? null : ReviewBlindReason.valueOf(reason).getDescription();
    }

    private static String blindStatusDescription(String status) {
        return status == null ? null : ReviewBlindStatus.valueOf(status).getDescription();
    }

    private ShopReviewDetailViewResult toDetailViewResult(ShopReviewManagementDetailResult result) {
        return new ShopReviewDetailViewResult(result, toReplyWindow(result.createdAt()));
    }

    private ShopReviewReplyWindow toReplyWindow(LocalDateTime reviewCreatedAt) {
        LocalDate replyDeadline = reviewCreatedAt.toLocalDate().plusDays(ReviewOwnerReply.REPLY_PERIOD_DAYS);
        return new ShopReviewReplyWindow(replyDeadline, !LocalDate.now().isAfter(replyDeadline));
    }
}
