package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;

public record ReviewBlindRequestDetailResult(
    Long id,
    Long reviewId,
    Long shopId,
    String shopName,
    ReviewBlindReason reason,
    String detailReason,
    ReviewBlindStatus status,
    String rejectReason,
    LocalDateTime blindUntil,
    String reviewContent,
    Double reviewTotalRating,
    List<String> reviewImageUrls,
    List<String> attachmentUrls,
    String reviewMemberNickname,
    boolean reviewHidden,
    LocalDateTime reviewCreatedAt,
    LocalDateTime createdAt
) {

    public ReviewBlindRequestDetailResult withUrls(List<String> reviewImageUrls, List<String> attachmentUrls) {
        return new ReviewBlindRequestDetailResult(
            this.id,
            this.reviewId,
            this.shopId,
            this.shopName,
            this.reason,
            this.detailReason,
            this.status,
            this.rejectReason,
            this.blindUntil,
            this.reviewContent,
            this.reviewTotalRating,
            reviewImageUrls,
            attachmentUrls,
            this.reviewMemberNickname,
            this.reviewHidden,
            this.reviewCreatedAt,
            this.createdAt
        );
    }
}
