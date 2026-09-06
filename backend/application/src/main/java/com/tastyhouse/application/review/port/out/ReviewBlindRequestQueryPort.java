package com.tastyhouse.application.review.port.out;

import java.util.Optional;

public interface ReviewBlindRequestQueryPort {

    Optional<ReviewBlindNoticeResult> findBlindNotice(Long reviewId);
}
