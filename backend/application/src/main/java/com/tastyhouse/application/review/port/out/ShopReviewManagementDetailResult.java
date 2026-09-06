package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.shared.model.OrderMethod;

public record ShopReviewManagementDetailResult(
    Long id,
    Long shopId,
    String memberNickname,
    Double totalRating,
    String content,
    List<String> imageUrls,
    List<String> productNames,
    OrderMethod orderMethod,
    boolean hidden,
    boolean ownerOnly,
    Double tasteRating,
    Double amountRating,
    Double priceRating,
    Double atmosphereRating,
    Double kindnessRating,
    Double hygieneRating,
    boolean willRevisit,
    List<String> tagNames,
    Long ownerReplyId,
    String ownerReplyContent,
    LocalDateTime ownerReplyCreatedAt,
    LocalDateTime ownerReplyUpdatedAt,
    List<ReviewBlindRequestHistoryResult> blindRequests,
    LocalDateTime createdAt,
    Integer deliveryRating,
    String deliveryComment
) {

    public ShopReviewManagementDetailResult withCollections(
        List<String> imageUrls,
        List<String> productNames,
        List<String> tagNames,
        List<ReviewBlindRequestHistoryResult> blindRequests
    ) {
        return new ShopReviewManagementDetailResult(
            this.id,
            this.shopId,
            this.memberNickname,
            this.totalRating,
            this.content,
            imageUrls,
            productNames,
            this.orderMethod,
            this.hidden,
            this.ownerOnly,
            this.tasteRating,
            this.amountRating,
            this.priceRating,
            this.atmosphereRating,
            this.kindnessRating,
            this.hygieneRating,
            this.willRevisit,
            tagNames,
            this.ownerReplyId,
            this.ownerReplyContent,
            this.ownerReplyCreatedAt,
            this.ownerReplyUpdatedAt,
            blindRequests,
            this.createdAt,
            this.deliveryRating,
            this.deliveryComment
        );
    }
}
