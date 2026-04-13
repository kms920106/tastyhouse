package com.tastyhouse.webapi.review.response;

import java.time.LocalDateTime;
import java.util.List;

public record LatestReviewListItem(
    Long id,
    List<String> imageUrls,
    String stationName,
    Double totalRating,
    String content,
    Long memberId,
    String memberNickname,
    String memberProfileImageUrl,
    LocalDateTime createdAt,
    Long likeCount,
    Long commentCount
) {
    public static LatestReviewListItem from(
        Long id,
        List<String> imageUrls,
        String stationName,
        Double totalRating,
        String content,
        Long memberId,
        String memberNickname,
        String memberProfileImageUrl,
        LocalDateTime createdAt,
        Long likeCount,
        Long commentCount
    ) {
        return new LatestReviewListItem(
            id,
            imageUrls,
            stationName,
            totalRating,
            content,
            memberId,
            memberNickname,
            memberProfileImageUrl,
            createdAt,
            likeCount,
            commentCount
        );
    }
}
