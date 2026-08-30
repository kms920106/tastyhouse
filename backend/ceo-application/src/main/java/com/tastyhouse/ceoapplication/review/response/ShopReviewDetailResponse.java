package com.tastyhouse.ceoapplication.review.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "점주 리뷰 상세")
public record ShopReviewDetailResponse(
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

    @Schema(description = "주문 메뉴명 목록. 미인증 리뷰면 빈 배열입니다.")
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

    @Schema(description = "사장님만보기 여부. 작성자가 비공개로 등록한 리뷰이며 hidden(게시중단)과는 독립이라 둘 다 true일 수 있습니다.", example = "false")
    Boolean ownerOnly,

    @Schema(description = "맛 평점", example = "5.0")
    Double tasteRating,

    @Schema(description = "양 평점", example = "4.0")
    Double amountRating,

    @Schema(description = "가격 평점", example = "4.0")
    Double priceRating,

    @Schema(description = "분위기 평점", example = "4.5")
    Double atmosphereRating,

    @Schema(description = "친절 평점", example = "5.0")
    Double kindnessRating,

    @Schema(description = "위생 평점", example = "4.5")
    Double hygieneRating,

    @Schema(description = "재방문 의사", example = "true")
    Boolean willRevisit,

    @Schema(description = "리뷰 태그 목록")
    List<String> tagNames,

    @Schema(description = "사장님 답변 ID. 미답변이면 null입니다.", example = "77")
    Long ownerReplyId,

    @Schema(description = "사장님 답변 내용. 미답변이면 null입니다.", example = "소중한 리뷰 감사합니다.")
    String ownerReplyContent,

    @Schema(description = "사장님 답변 작성일시. 미답변이면 null입니다.", example = "2026-06-20T14:03:00")
    LocalDateTime ownerReplyCreatedAt,

    @Schema(description = "사장님 답변 수정일시. 미답변이면 null입니다.", example = "2026-06-21T10:00:00")
    LocalDateTime ownerReplyUpdatedAt,

    @Schema(
        description = "최근 게시중단 요청 상태. 요청 이력이 없으면 null입니다.",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"},
        example = "REJECTED"
    )
    String blindRequestStatus,

    @Schema(description = "최근 게시중단 요청 상태 한글명. 요청 이력이 없으면 null입니다.", example = "반려")
    String blindRequestStatusDescription,

    @Schema(description = "게시중단 요청 이력(최신순). 요청한 적이 없으면 빈 배열입니다.")
    List<ReviewBlindRequestHistoryResponse> blindRequests,

    @Schema(description = "리뷰 작성일시", example = "2026-06-19T20:11:00")
    LocalDateTime createdAt,

    @Schema(description = "답변 마감일 = 리뷰 작성일 + 30일. 이 날짜까지는 하루 종일 등록할 수 있습니다.", example = "2026-07-19")
    LocalDate replyDeadline,

    @Schema(description = "오늘 기준 신규 답변 등록 가능 여부. 이미 답변이 있으면 이 값과 무관하게 수정·삭제할 수 있습니다.", example = "true")
    boolean replyable,

    @Schema(description = "배달 평점 (1~5). 미평가이거나 배달 주문이 아니면 null입니다. 점주 전용이며 고객 앱에는 표시되지 않습니다.", example = "5")
    Integer deliveryRating,

    @Schema(description = "배달 평가 내용. 미평가면 null입니다. 점주 전용이며 고객 앱에는 표시되지 않습니다.", example = "빠르게 잘 받았어요")
    String deliveryComment
) {

    public static ShopReviewDetailResponse from(
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
        Boolean ownerOnly,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        Boolean willRevisit,
        List<String> tagNames,
        Long ownerReplyId,
        String ownerReplyContent,
        LocalDateTime ownerReplyCreatedAt,
        LocalDateTime ownerReplyUpdatedAt,
        String blindRequestStatus,
        String blindRequestStatusDescription,
        List<ReviewBlindRequestHistoryResponse> blindRequests,
        LocalDateTime createdAt,
        LocalDate replyDeadline,
        boolean replyable,
        Integer deliveryRating,
        String deliveryComment
    ) {
        return new ShopReviewDetailResponse(
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
            ownerOnly,
            tasteRating,
            amountRating,
            priceRating,
            atmosphereRating,
            kindnessRating,
            hygieneRating,
            willRevisit,
            tagNames,
            ownerReplyId,
            ownerReplyContent,
            ownerReplyCreatedAt,
            ownerReplyUpdatedAt,
            blindRequestStatus,
            blindRequestStatusDescription,
            blindRequests,
            createdAt,
            replyDeadline,
            replyable,
            deliveryRating,
            deliveryComment
        );
    }
}
