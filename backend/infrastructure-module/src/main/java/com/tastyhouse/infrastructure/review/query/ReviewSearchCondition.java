package com.tastyhouse.infrastructure.review.query;

public record ReviewSearchCondition(
    Long shopId,
    Long productId,
    Long memberId,
    Boolean hidden,
    String content,
    Double minRating,
    Double maxRating
) {

    public static ReviewSearchCondition of(
        Long shopId,
        Long productId,
        Long memberId,
        Boolean hidden,
        String content,
        Double minRating,
        Double maxRating
    ) {
        return new ReviewSearchCondition(
            shopId,
            productId,
            memberId,
            hidden,
            content,
            minRating,
            maxRating
        );
    }
}
