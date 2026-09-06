package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ReviewBlindNoticeResult;
import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface ReviewBlindConsentQueryUseCase {

    ReviewBlindNoticeResult getBlindNotice(Long reviewId, Long memberId);
}
