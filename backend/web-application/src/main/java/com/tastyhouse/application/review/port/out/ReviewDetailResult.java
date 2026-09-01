package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.shared.model.OrderMethod;

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
    List<String> tagNames,
    String ownerReplyContent,
    LocalDateTime ownerReplyCreatedAt,
    OrderMethod orderMethod,
    Integer deliveryRating,
    String deliveryComment
) {
    /**
     * QueryDSL 투영 전용 생성자 — 1:N인 이미지·태그를 제외한 좁은 시그니처다.
     * {@code ReviewQueryDao}가 {@code Projections.constructor}로 리플렉션 호출하므로 정적 호출부가
     * 없어 IDE가 "never used"로 경고하지만, 제거하면 투영이 런타임에 깨진다. 파라미터 개수·순서가
     * DAO의 select 인자와 정확히 일치해야 한다.
     */
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
        boolean ownerOnly,
        String ownerReplyContent,
        LocalDateTime ownerReplyCreatedAt,
        OrderMethod orderMethod,
        Integer deliveryRating,
        String deliveryComment
    ) {
        this(
            id, shopId, shopName, stationName, content,
            totalRating, tasteRating, amountRating, priceRating,
            atmosphereRating, kindnessRating, hygieneRating, willRevisit,
            memberId, memberNickname, memberProfileImageUrl, createdAt, ownerOnly,
            List.of(), List.of(), ownerReplyContent, ownerReplyCreatedAt,
            orderMethod, deliveryRating, deliveryComment
        );
    }

    public ReviewDetailResult withImageUrls(List<String> imageUrls) {
        return new ReviewDetailResult(
            id, shopId, shopName, stationName, content,
            totalRating, tasteRating, amountRating, priceRating,
            atmosphereRating, kindnessRating, hygieneRating, willRevisit,
            memberId, memberNickname, memberProfileImageUrl, createdAt, ownerOnly,
            imageUrls, tagNames, ownerReplyContent, ownerReplyCreatedAt,
            orderMethod, deliveryRating, deliveryComment
        );
    }

    public ReviewDetailResult withTagNames(List<String> tagNames) {
        return new ReviewDetailResult(
            id, shopId, shopName, stationName, content,
            totalRating, tasteRating, amountRating, priceRating,
            atmosphereRating, kindnessRating, hygieneRating, willRevisit,
            memberId, memberNickname, memberProfileImageUrl, createdAt, ownerOnly,
            imageUrls, tagNames, ownerReplyContent, ownerReplyCreatedAt,
            orderMethod, deliveryRating, deliveryComment
        );
    }
}
