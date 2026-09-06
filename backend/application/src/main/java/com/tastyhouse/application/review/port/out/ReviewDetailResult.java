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
