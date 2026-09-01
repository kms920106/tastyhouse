package com.tastyhouse.adminapi.review.adapter.in.web.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.review.port.out.ReviewManagementDetailResult;

@Schema(description = "리뷰 상세 응답")
public record ReviewManagementDetailResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long id,

    @Schema(description = "상점 ID", example = "1")
    Long shopId,

    @Schema(description = "상점명", example = "맛있는 식당")
    String shopName,

    @Schema(description = "인근 역명", example = "강남역")
    String stationName,

    @Schema(description = "리뷰 내용", example = "정말 맛있어요")
    String content,

    @Schema(description = "총 평점", example = "4.5")
    Double totalRating,

    @Schema(description = "맛 평점", example = "4.5")
    Double tasteRating,

    @Schema(description = "양 평점", example = "4.0")
    Double amountRating,

    @Schema(description = "가격 평점", example = "4.0")
    Double priceRating,

    @Schema(description = "분위기 평점", example = "4.0")
    Double atmosphereRating,

    @Schema(description = "친절도 평점", example = "5.0")
    Double kindnessRating,

    @Schema(description = "위생 평점", example = "5.0")
    Double hygieneRating,

    @Schema(description = "재방문 의사", example = "true")
    boolean willRevisit,

    @Schema(description = "숨김 여부", example = "false")
    boolean hidden,

    @Schema(description = "사장님만보기 여부. 작성자가 비공개로 등록한 리뷰이며 hidden(게시중단)과는 독립입니다.", example = "false")
    boolean ownerOnly,

    @Schema(description = "작성 회원 ID", example = "1")
    Long memberId,

    @Schema(description = "작성 회원 닉네임", example = "맛집탐험가")
    String memberNickname,

    @Schema(description = "작성 회원 프로필 이미지 URL", example = "https://cdn.tastyhouse.com/profile/1.jpg")
    String memberProfileImageUrl,

    @Schema(description = "작성일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt,

    @Schema(description = "첨부 이미지 URL 목록")
    List<String> imageUrls,

    @Schema(description = "태그명 목록")
    List<String> tagNames
) {
    public static ReviewManagementDetailResponse from(ReviewManagementDetailResult result) {
        return new ReviewManagementDetailResponse(
            result.id(),
            result.shopId(),
            result.shopName(),
            result.stationName(),
            result.content(),
            result.totalRating(),
            result.tasteRating(),
            result.amountRating(),
            result.priceRating(),
            result.atmosphereRating(),
            result.kindnessRating(),
            result.hygieneRating(),
            result.willRevisit(),
            result.hidden(),
            result.ownerOnly(),
            result.memberId(),
            result.memberNickname(),
            result.memberProfileImageUrl(),
            result.createdAt(),
            result.imageUrls(),
            result.tagNames()
        );
    }
}
