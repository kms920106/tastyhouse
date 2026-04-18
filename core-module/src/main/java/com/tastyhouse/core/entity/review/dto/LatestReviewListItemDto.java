package com.tastyhouse.core.entity.review.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;
import java.util.List;

public record LatestReviewListItemDto(
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
    Long commentCount
) {
    @QueryProjection
    public LatestReviewListItemDto(Long id, String stationName,
                                   Double totalRating, String content,
                                   Long memberId, String memberNickname, String memberProfileImageUrl,
                                   LocalDateTime createdAt, Long productId, String productName) {
        this(id, List.of(), stationName, totalRating, content, memberId, memberNickname, memberProfileImageUrl, createdAt, productId, productName, 0L, 0L);
    }

    @QueryProjection
    public LatestReviewListItemDto(Long id, String stationName,
                                   Double totalRating, String content,
                                   Long memberId, String memberNickname, String memberProfileImageUrl,
                                   LocalDateTime createdAt, Long productId, String productName,
                                   Long likeCount, Long commentCount) {
        this(id, List.of(), stationName, totalRating, content, memberId, memberNickname, memberProfileImageUrl, createdAt, productId, productName, likeCount, commentCount);
    }

    public LatestReviewListItemDto withImageUrls(List<String> imageUrls) {
        return new LatestReviewListItemDto(id, imageUrls, stationName, totalRating, content, memberId, memberNickname, memberProfileImageUrl, createdAt, productId, productName, likeCount, commentCount);
    }

    public LatestReviewListItemDto withLikeCount(Long likeCount) {
        return new LatestReviewListItemDto(id, imageUrls, stationName, totalRating, content, memberId, memberNickname, memberProfileImageUrl, createdAt, productId, productName, likeCount, commentCount);
    }

    public LatestReviewListItemDto withCommentCount(Long commentCount) {
        return new LatestReviewListItemDto(id, imageUrls, stationName, totalRating, content, memberId, memberNickname, memberProfileImageUrl, createdAt, productId, productName, likeCount, commentCount);
    }
}
