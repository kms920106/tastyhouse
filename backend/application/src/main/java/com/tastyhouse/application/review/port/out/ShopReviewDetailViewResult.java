package com.tastyhouse.application.review.port.out;

public record ShopReviewDetailViewResult(
    ShopReviewManagementDetailResult review,
    ShopReviewReplyWindow replyWindow
) {
}
