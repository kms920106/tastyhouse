package com.tastyhouse.webapi.shop.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 리뷰 목록 항목 응답")
public record ShopReviewListItemResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long id,

    @Schema(description = "리뷰 이미지 URL 목록")
    List<String> imageUrls,

    @Schema(description = "총 평점", example = "4.5")
    Double totalRating,

    @Schema(description = "리뷰 내용", example = "정말 맛있었어요!")
    String content,

    @Schema(description = "작성자 회원 ID", example = "2")
    Long memberId,

    @Schema(description = "작성자 닉네임", example = "맛집헌터")
    String memberNickname,

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://cdn.tastyhouse.com/member/2/profile.jpg")
    String memberProfileImageUrl,

    @Schema(description = "리뷰 작성 일시", example = "2026-06-03T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "주문 상품 ID", example = "3")
    Long productId,

    @Schema(description = "주문 상품명", example = "황금올리브치킨")
    String productName,

    @Schema(description = "사장님 답변 내용. 미답변이면 null입니다.", example = "소중한 리뷰 감사합니다.")
    String ownerReplyContent,

    @Schema(description = "사장님 답변 작성일시. 미답변이면 null입니다.", example = "2026-06-20T14:03:00")
    LocalDateTime ownerReplyCreatedAt
) {
    public static ShopReviewListItemResponse from(
        Long id,
        List<String> imageUrls,
        Double totalRating,
        String content,
        Long memberId,
        String memberNickname,
        String memberProfileImageUrl,
        LocalDateTime createdAt,
        Long productId,
        String productName,
        String ownerReplyContent,
        LocalDateTime ownerReplyCreatedAt
    ) {
        return new ShopReviewListItemResponse(
            id,
            imageUrls,
            totalRating,
            content,
            memberId,
            memberNickname,
            memberProfileImageUrl,
            createdAt,
            productId,
            productName,
            ownerReplyContent,
            ownerReplyCreatedAt
        );
    }
}
