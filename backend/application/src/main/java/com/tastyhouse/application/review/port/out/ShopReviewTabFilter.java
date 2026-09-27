package com.tastyhouse.application.review.port.out;

public record ShopReviewTabFilter(
    boolean unansweredOnly,
    boolean blindedOnly,
    boolean ownerOnly
) {

    public ShopReviewTabFilter {
        int enabled = (unansweredOnly ? 1 : 0) + (blindedOnly ? 1 : 0) + (ownerOnly ? 1 : 0);
        if (enabled > 1) {
            throw new IllegalArgumentException("리뷰 탭 조건은 하나만 켤 수 있습니다.");
        }
    }
}
