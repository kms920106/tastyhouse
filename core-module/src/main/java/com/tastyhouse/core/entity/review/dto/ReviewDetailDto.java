package com.tastyhouse.core.entity.review.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewDetailDto(
    Long id,
    Long placeId,
    String placeName,
    String stationName,
    String content,
    Double totalRating,
    Double tasteRating,
    Double amountRating,
    Double priceRating,
    Double atmosphereRating,
    Double kindnessRating,
    Double hygieneRating,
    Boolean willRevisit,
    Long memberId,
    String memberNickname,
    String memberProfileImageUrl,
    LocalDateTime createdAt,
    List<String> imageUrls,
    List<String> tagNames
) {
    @QueryProjection
    public ReviewDetailDto(Long id, Long placeId, String placeName, String stationName, String content, Double totalRating, Double tasteRating, Double amountRating, Double priceRating, Double atmosphereRating, Double kindnessRating, Double hygieneRating, Boolean willRevisit, Long memberId, String memberNickname, String memberProfileImageUrl, LocalDateTime createdAt) {
        this(id, placeId, placeName, stationName, content, totalRating, tasteRating, amountRating, priceRating, atmosphereRating, kindnessRating, hygieneRating, willRevisit, memberId, memberNickname, memberProfileImageUrl, createdAt, List.of(), List.of());
    }

    public ReviewDetailDto withImageUrls(List<String> imageUrls) {
        return new ReviewDetailDto(id, placeId, placeName, stationName, content, totalRating, tasteRating, amountRating, priceRating, atmosphereRating, kindnessRating, hygieneRating, willRevisit, memberId, memberNickname, memberProfileImageUrl, createdAt, imageUrls, tagNames);
    }

    public ReviewDetailDto withTagNames(List<String> tagNames) {
        return new ReviewDetailDto(id, placeId, placeName, stationName, content, totalRating, tasteRating, amountRating, priceRating, atmosphereRating, kindnessRating, hygieneRating, willRevisit, memberId, memberNickname, memberProfileImageUrl, createdAt, imageUrls, tagNames);
    }
}
