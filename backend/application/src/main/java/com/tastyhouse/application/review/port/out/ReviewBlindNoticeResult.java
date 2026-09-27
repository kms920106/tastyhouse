package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewBlindNoticeResult(
    Long reviewId,
    String content,
    List<String> imageUrls,
    LocalDateTime createdAt,
    String shopName,
    String reason,
    String reasonDescription,
    String detailReason,
    LocalDateTime blindUntil,
    Long reviewMemberId
) {

    public ReviewBlindNoticeResult withImageUrls(List<String> imageUrls) {
        return new ReviewBlindNoticeResult(
            this.reviewId,
            this.content,
            imageUrls,
            this.createdAt,
            this.shopName,
            this.reason,
            this.reasonDescription,
            this.detailReason,
            this.blindUntil,
            this.reviewMemberId
        );
    }
}
