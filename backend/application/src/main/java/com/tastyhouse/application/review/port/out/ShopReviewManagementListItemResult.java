package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;

public record ShopReviewManagementListItemResult(
    Long id,
    String memberNickname,
    Double totalRating,
    String content,
    List<String> imageUrls,
    List<String> productNames,
    OrderMethod orderMethod,
    boolean hidden,
    boolean ownerOnly,
    String ownerReplyContent,
    LocalDateTime ownerReplyCreatedAt,
    ReviewBlindStatus blindRequestStatus,
    LocalDateTime createdAt
) {

    public ShopReviewManagementListItemResult withImageUrls(List<String> imageUrls) {
        return new ShopReviewManagementListItemResult(
            this.id,
            this.memberNickname,
            this.totalRating,
            this.content,
            imageUrls,
            this.productNames,
            this.orderMethod,
            this.hidden,
            this.ownerOnly,
            this.ownerReplyContent,
            this.ownerReplyCreatedAt,
            this.blindRequestStatus,
            this.createdAt
        );
    }

    public ShopReviewManagementListItemResult withProductNames(List<String> productNames) {
        return new ShopReviewManagementListItemResult(
            this.id,
            this.memberNickname,
            this.totalRating,
            this.content,
            this.imageUrls,
            productNames,
            this.orderMethod,
            this.hidden,
            this.ownerOnly,
            this.ownerReplyContent,
            this.ownerReplyCreatedAt,
            this.blindRequestStatus,
            this.createdAt
        );
    }
}
