package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

public record LatestReviewListItemResult(
    Long id,
    List<String> imageUrls,
    String stationName,
    Double totalRating,
    String content,
    Long memberId,
    String memberNickname,
    String memberProfileImageUrl,
    LocalDateTime createdAt,
    Long productId,
    String productName,
    Long likeCount,
    Long commentCount,
    String ownerReplyContent,
    LocalDateTime ownerReplyCreatedAt
) {
    public LatestReviewListItemResult(
        Long id,
        String stationName,
        Double totalRating,
        String content,
        Long memberId,
        String memberNickname,
        String memberProfileImageUrl,
        LocalDateTime createdAt,
        Long productId,
        String productName,
        Long likeCount,
        Long commentCount,
        String ownerReplyContent,
        LocalDateTime ownerReplyCreatedAt
    ) {
        this(
            id,
            List.of(),
            stationName,
            totalRating,
            content,
            memberId,
            memberNickname,
            memberProfileImageUrl,
            createdAt,
            productId,
            productName,
            likeCount,
            commentCount,
            ownerReplyContent,
            ownerReplyCreatedAt
        );
    }

    public LatestReviewListItemResult withImageUrls(List<String> imageUrls) {
        return new LatestReviewListItemResult(
            id,
            imageUrls,
            stationName,
            totalRating,
            content,
            memberId,
            memberNickname,
            memberProfileImageUrl,
            createdAt,
            productId,
            productName,
            likeCount,
            commentCount,
            ownerReplyContent,
            ownerReplyCreatedAt
        );
    }
}
