package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.review.port.out.ReviewBlindNoticeResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestQueryPort;
import com.tastyhouse.application.review.port.in.ReviewBlindConsentQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class ReviewBlindConsentQueryService implements ReviewBlindConsentQueryUseCase {

    private final ReviewBlindRequestQueryPort reviewBlindRequestQueryPort;

    public ReviewBlindConsentQueryService(ReviewBlindRequestQueryPort reviewBlindRequestQueryPort) {
        this.reviewBlindRequestQueryPort = reviewBlindRequestQueryPort;
    }

    @Override
    public ReviewBlindNoticeResult getBlindNotice(Long reviewId, Long memberId) {
        ReviewBlindNoticeResult notice = reviewBlindRequestQueryPort.findBlindNotice(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (!notice.reviewMemberId().equals(memberId)) {
            throw new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND);
        }

        return notice;
    }
}
