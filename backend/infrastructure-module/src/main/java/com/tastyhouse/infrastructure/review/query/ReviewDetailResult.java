package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

public record ReviewDetailResult(
    Long id,
    Long shopId,
    String shopName,
    String stationName,
    String content,
    Double totalRating,
    Double tasteRating,
    Double amountRating,
    Double priceRating,
    Double atmosphereRating,
    Double kindnessRating,
    Double hygieneRating,
    boolean willRevisit,
    Long memberId,
    String memberNickname,
    String memberProfileImageUrl,
    LocalDateTime createdAt,
    boolean ownerOnly,
    List<String> imageUrls,
    List<String> tagNames
) {
    /**
     * QueryDSL 투영 전용 생성자 — 1:N인 이미지·태그를 제외한 좁은 시그니처다. 호출부는 생성된
     * {@code QReviewDetailResult}(`ReviewQueryDao#findReviewDetail`)이므로 IDE가 "never used"로
     * 경고하지만, 제거하면 Q타입이 생성되지 않아 빌드가 깨진다.
     */
    @QueryProjection
    public ReviewDetailResult(
        Long id,
        Long shopId,
        String shopName,
        String stationName,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        Long memberId,
        String memberNickname,
        String memberProfileImageUrl,
        LocalDateTime createdAt,
        boolean ownerOnly
    ) {
        this(
            id, shopId, shopName, stationName, content,
            totalRating, tasteRating, amountRating, priceRating,
            atmosphereRating, kindnessRating, hygieneRating, willRevisit,
            memberId, memberNickname, memberProfileImageUrl, createdAt, ownerOnly,
            List.of(), List.of()
        );
    }

    public ReviewDetailResult withImageUrls(List<String> imageUrls) {
        return new ReviewDetailResult(
            id, shopId, shopName, stationName, content,
            totalRating, tasteRating, amountRating, priceRating,
            atmosphereRating, kindnessRating, hygieneRating, willRevisit,
            memberId, memberNickname, memberProfileImageUrl, createdAt, ownerOnly,
            imageUrls, tagNames
        );
    }

    public ReviewDetailResult withTagNames(List<String> tagNames) {
        return new ReviewDetailResult(
            id, shopId, shopName, stationName, content,
            totalRating, tasteRating, amountRating, priceRating,
            atmosphereRating, kindnessRating, hygieneRating, willRevisit,
            memberId, memberNickname, memberProfileImageUrl, createdAt, ownerOnly,
            imageUrls, tagNames
        );
    }
}
