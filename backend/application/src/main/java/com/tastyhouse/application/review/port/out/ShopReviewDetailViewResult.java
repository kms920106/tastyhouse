package com.tastyhouse.application.review.port.out;

/**
 * 점주 리뷰 상세 — 조회 결과에 답변 가능 기간을 더한 형태.
 *
 * <p><b>챕터 09</b>에서 신설. 사유는 {@link ShopReviewListItemViewResult}와 같다.
 */
public record ShopReviewDetailViewResult(
    ShopReviewManagementDetailResult review,
    ShopReviewReplyWindow replyWindow
) {
}
