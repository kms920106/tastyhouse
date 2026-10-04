package com.tastyhouse.application.review.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestDetailResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestListItemResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestManagementQueryPort;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestSearchCondition;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ReviewBlindRequestQueryService implements ReviewBlindRequestQueryUseCase {

    private final ReviewBlindRequestManagementQueryPort reviewBlindRequestManagementQueryPort;

    public ReviewBlindRequestQueryService(ReviewBlindRequestManagementQueryPort reviewBlindRequestManagementQueryPort) {
        this.reviewBlindRequestManagementQueryPort = reviewBlindRequestManagementQueryPort;
    }

    @Override
    public PageResult<ReviewBlindRequestListItemResult> getBlindRequests(
        Long shopId,
        String status,
        String reason,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    ) {
        String blindStatus = status == null ? null : ReviewBlindStatus.from(status).name();
        String blindReason = reason == null ? null : ReviewBlindReason.from(reason).name();

        ReviewBlindRequestSearchCondition condition = ReviewBlindRequestSearchCondition.of(
            shopId, blindStatus, blindReason, startDate, endDate
        );
        return reviewBlindRequestManagementQueryPort.findBlindRequestPage(condition, PageQuery.of(page, size))
            .map(item -> item.withDescriptions(reasonDescription(item.reason()), statusDescription(item.status())));
    }

    @Override
    public ReviewBlindRequestDetailResult getBlindRequest(Long id) {
        return reviewBlindRequestManagementQueryPort.findBlindRequestDetail(id)
            .map(detail -> detail.withDescriptions(reasonDescription(detail.reason()), statusDescription(detail.status())))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_BLIND_REQUEST_NOT_FOUND));
    }

    private static String reasonDescription(String reason) {
        return reason == null ? null : ReviewBlindReason.valueOf(reason).getDescription();
    }

    private static String statusDescription(String status) {
        return status == null ? null : ReviewBlindStatus.valueOf(status).getDescription();
    }
}
