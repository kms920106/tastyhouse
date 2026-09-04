package com.tastyhouse.application.review.port.out;

public record ReviewSearchCondition(
    Long shopId,
    Long productId,
    Long memberId,
    Boolean hidden,
    Boolean ownerOnly,
    String content,
    Double minRating,
    Double maxRating
) {

    public static ReviewSearchCondition of(
        Long shopId,
        Long productId,
        Long memberId,
        Boolean hidden,
        Boolean ownerOnly,
        String content,
        Double minRating,
        Double maxRating
    ) {
        return new ReviewSearchCondition(
            shopId,
            productId,
            memberId,
            hidden,
            ownerOnly,
            content,
            minRating,
            maxRating
        );
    }
}
