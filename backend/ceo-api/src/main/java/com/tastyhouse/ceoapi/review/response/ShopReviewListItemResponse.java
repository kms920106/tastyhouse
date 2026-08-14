package com.tastyhouse.ceoapi.review.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "점주 리뷰 목록 항목")
public record ShopReviewListItemResponse(
    @Schema(description = "리뷰 ID", example = "482")
    Long id,

    @Schema(description = "리뷰 고유 번호(16자리 표시용)", example = "0000000000000482")
    String reviewNumber,

    @Schema(description = "작성자 닉네임", example = "맛집탐험가")
    String memberNickname,

    @Schema(description = "종합 평점", example = "4.5")
    Double totalRating,

    @Schema(description = "리뷰 내용", example = "국물이 진하고 맛있었어요.")
    String content,

    @Schema(description = "리뷰 사진 URL 목록. 사진이 없으면 빈 배열입니다.")
    List<String> imageUrls,

    @Schema(description = "주문 메뉴명 목록. 미인증 리뷰(주문 정보 없음)면 빈 배열입니다.")
    List<String> productNames,

    @Schema(
        description = "주문유형. 미인증 리뷰면 null입니다.",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"},
        example = "DELIVERY"
    )
    String orderMethod,

    @Schema(description = "주문유형 한글명. 미인증 리뷰면 null입니다.", example = "배달")
    String orderMethodDescription,

    @Schema(description = "차단(게시중단) 여부", example = "false")
    Boolean hidden,

    @Schema(description = "사장님 답변 내용. 미답변이면 null입니다.", example = "소중한 리뷰 감사합니다.")
    String ownerReplyContent,

    @Schema(description = "사장님 답변 작성일시. 미답변이면 null입니다.", example = "2026-06-20T14:03:00")
    LocalDateTime ownerReplyCreatedAt,

    @Schema(
        description = "최근 게시중단 요청 상태. 요청 이력이 없으면 null입니다.",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"},
        example = "PENDING"
    )
    String blindRequestStatus,

    @Schema(description = "리뷰 작성일시", example = "2026-06-19T20:11:00")
    LocalDateTime createdAt
) {

    public static ShopReviewListItemResponse from(
        Long id,
        String reviewNumber,
        String memberNickname,
        Double totalRating,
        String content,
        List<String> imageUrls,
        List<String> productNames,
        String orderMethod,
        String orderMethodDescription,
        Boolean hidden,
        String ownerReplyContent,
        LocalDateTime ownerReplyCreatedAt,
        String blindRequestStatus,
        LocalDateTime createdAt
    ) {
        return new ShopReviewListItemResponse(
            id,
            reviewNumber,
            memberNickname,
            totalRating,
            content,
            imageUrls,
            productNames,
            orderMethod,
            orderMethodDescription,
            hidden,
            ownerReplyContent,
            ownerReplyCreatedAt,
            blindRequestStatus,
            createdAt
        );
    }
}
