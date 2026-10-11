package com.tastyhouse.application.review.port.out;

import java.time.LocalDate;

public record ShopReviewReplyWindow(
    LocalDate replyDeadline,
    boolean replyable
) {
}
