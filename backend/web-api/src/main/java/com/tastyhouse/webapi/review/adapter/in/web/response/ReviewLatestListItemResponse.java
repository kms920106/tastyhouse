package com.tastyhouse.webapi.review.adapter.in.web.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최신 리뷰 목록 아이템 응답")
public record ReviewLatestListItemResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long id,

    @Schema(description = "리뷰 이미지 URL 목록")
    List<String> imageUrls,

    @Schema(description = "역/지점명", example = "강남역점")
    String stationName,

    @Schema(description = "총점", example = "4.5")
    Double totalRating,

    @Schema(description = "리뷰 내용", example = "맛있게 잘 먹었습니다.")
    String content,

    @Schema(description = "작성자 회원 ID", example = "2")
    Long memberId,

    @Schema(description = "작성자 닉네임", example = "테이스티하우스")
    String memberNickname,

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://cdn.tastyhouse.com/member/2/profile.jpg")
    String memberProfileImageUrl,

    @Schema(description = "리뷰 작성 일시", example = "2026-06-03T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "좋아요 수", example = "12")
    Long likeCount,

    @Schema(description = "댓글 수", example = "3")
    Long commentCount
) {
    public static ReviewLatestListItemResponse from(
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
        return new ReviewLatestListItemResponse(
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
