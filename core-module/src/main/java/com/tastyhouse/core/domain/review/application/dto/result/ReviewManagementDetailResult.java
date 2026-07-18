package com.tastyhouse.core.domain.review.application.dto.result;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record ReviewManagementDetailResult(
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
    boolean hidden,
    MemberId memberId,
    String memberNickname,
    String memberProfileImageUrl,
    LocalDateTime createdAt,
    List<String> imageUrls,
    List<String> tagNames
) {
    @QueryProjection
    public ReviewManagementDetailResult(
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
        boolean hidden,
        MemberId memberId,
        String memberNickname,
        String memberProfileImageUrl,
        LocalDateTime createdAt
    ) {
        this(
            id, shopId, shopName, stationName, content,
            totalRating, tasteRating, amountRating, priceRating,
            atmosphereRating, kindnessRating, hygieneRating, willRevisit, hidden,
            memberId, memberNickname, memberProfileImageUrl, createdAt,
            List.of(), List.of()
        );
    }

    public ReviewManagementDetailResult withImageUrls(List<String> imageUrls) {
        return new ReviewManagementDetailResult(
            id, shopId, shopName, stationName, content,
            totalRating, tasteRating, amountRating, priceRating,
            atmosphereRating, kindnessRating, hygieneRating, willRevisit, hidden,
            memberId, memberNickname, memberProfileImageUrl, createdAt,
            imageUrls, tagNames
        );
    }

    public ReviewManagementDetailResult withTagNames(List<String> tagNames) {
        return new ReviewManagementDetailResult(
            id, shopId, shopName, stationName, content,
            totalRating, tasteRating, amountRating, priceRating,
            atmosphereRating, kindnessRating, hygieneRating, willRevisit, hidden,
            memberId, memberNickname, memberProfileImageUrl, createdAt,
            imageUrls, tagNames
        );
    }
}
