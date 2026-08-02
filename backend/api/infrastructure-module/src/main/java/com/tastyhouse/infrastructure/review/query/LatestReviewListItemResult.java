package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.member.vo.MemberId;

public record LatestReviewListItemResult(
    Long id,
    List<String> imageUrls,
    String stationName,
    Double totalRating,
    String content,
    MemberId memberId,
    String memberNickname,
    String memberProfileImageUrl,
    LocalDateTime createdAt,
    Long productId,
    String productName,
    Long likeCount,
    Long commentCount
) {
    @QueryProjection
    public LatestReviewListItemResult(
        Long id,
        String stationName,
        Double totalRating,
        String content,
        MemberId memberId,
        String memberNickname,
        String memberProfileImageUrl,
        LocalDateTime createdAt,
        Long productId,
        String productName,
        Long likeCount,
        Long commentCount
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
            commentCount
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
            commentCount
        );
    }
}
