package com.tastyhouse.application.review.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestListQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestListItemResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestManagementQueryPort;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ReviewBlindRequestListQueryService implements ReviewBlindRequestListQueryUseCase {

    private final ReviewBlindRequestManagementQueryPort reviewBlindRequestManagementQueryPort;

    public ReviewBlindRequestListQueryService(ReviewBlindRequestManagementQueryPort reviewBlindRequestManagementQueryPort) {
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

    private static String reasonDescription(String reason) {
        return reason == null ? null : ReviewBlindReason.valueOf(reason).getDescription();
    }

    private static String statusDescription(String status) {
        return status == null ? null : ReviewBlindStatus.valueOf(status).getDescription();
    }
}
