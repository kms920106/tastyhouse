package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.application.review.port.in.ReviewBlindConsentQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewBlindNoticeResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestQueryPort;

@Service
@Transactional(readOnly = true)
public class ReviewBlindConsentQueryService implements ReviewBlindConsentQueryUseCase {

    private final ReviewBlindRequestQueryPort reviewBlindRequestQueryPort;

    public ReviewBlindConsentQueryService(ReviewBlindRequestQueryPort reviewBlindRequestQueryPort) {
        this.reviewBlindRequestQueryPort = reviewBlindRequestQueryPort;
    }

    @Override
    public ReviewBlindNoticeResult getBlindNotice(Long reviewId, Long memberId) {
        ReviewBlindNoticeResult notice = reviewBlindRequestQueryPort.findBlindNotice(reviewId, ReviewBlindStatus.APPROVED.name())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (!notice.reviewMemberId().equals(memberId)) {
            throw new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND);
        }

        return notice.withReasonDescription(
            notice.reason() == null ? null : ReviewBlindReason.valueOf(notice.reason()).getDescription());
    }
}
