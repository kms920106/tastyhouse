package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.application.review.port.in.ReviewBlindConsentQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewBlindNoticeResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class ReviewBlindConsentQueryService implements ReviewBlindConsentQueryUseCase {

    private final ReviewBlindRequestQueryPort reviewBlindRequestQueryPort;

    public ReviewBlindConsentQueryService(ReviewBlindRequestQueryPort reviewBlindRequestQueryPort) {
        this.reviewBlindRequestQueryPort = reviewBlindRequestQueryPort;
    }

    @Override
    public ReviewBlindNoticeResult getBlindNotice(Long reviewId, Long memberId) {
        ReviewBlindNoticeResult notice = reviewBlindRequestQueryPort.findBlindNotice(reviewId, ReviewBlindStatus.APPROVED.name())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        if (!notice.reviewMemberId().equals(memberId)) {
            throw new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND);
        }

        return notice.withReasonDescription(
            notice.reason() == null ? null : ReviewBlindReason.valueOf(notice.reason()).getDescription());
    }
}
