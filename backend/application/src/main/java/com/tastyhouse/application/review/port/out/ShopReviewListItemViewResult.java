package com.tastyhouse.application.review.port.out;

public record ShopReviewListItemViewResult(
    ShopReviewManagementListItemResult review,
    ShopReviewReplyWindow replyWindow
) {
}
