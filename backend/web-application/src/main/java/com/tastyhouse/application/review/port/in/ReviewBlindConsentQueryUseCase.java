package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ReviewBlindNoticeResult;

public interface ReviewBlindConsentQueryUseCase {

    ReviewBlindNoticeResult getBlindNotice(Long reviewId, Long memberId);
}
