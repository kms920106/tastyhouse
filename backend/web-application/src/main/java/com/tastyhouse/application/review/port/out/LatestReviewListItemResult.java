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
    /**
     * QueryDSL 투영 전용 생성자 — 1:N인 이미지를 제외한 좁은 시그니처로, imageUrls를 빈 목록으로 채운다.
     * {@code ReviewQueryDao}가 {@code Projections.constructor}로 리플렉션 호출하므로 정적 호출부가
     * 없어 IDE가 "never used"로 경고하지만, 제거하면 투영이 런타임에 깨진다. 파라미터 개수·순서가
     * DAO의 select 인자와 정확히 일치해야 한다.
     */
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
