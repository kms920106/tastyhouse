package com.tastyhouse.adminapi.review.adapter.in.web.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 게시중단 요청 심사 상세 응답")
public record ReviewBlindRequestDetailResponse(
    @Schema(description = "게시중단 요청 ID", example = "1")
    Long id,

    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId,

    @Schema(description = "상점 ID", example = "1")
    Long shopId,

    @Schema(description = "상점명", example = "맛있는 김밥")
    String shopName,

    @Schema(description = "게시중단 요청 사유 코드", example = "ADVERTISEMENT")
    String reason,

    @Schema(description = "게시중단 요청 사유 설명", example = "광고·홍보")
    String reasonDescription,

    @Schema(description = "처리 상태 코드", example = "PENDING")
    String status,

    @Schema(description = "처리 상태 설명", example = "대기")
    String statusDescription,

    @Schema(description = "리뷰 내용", example = "정말 맛있어요")
    String reviewContent,

    @Schema(description = "리뷰 총 평점", example = "4.5")
    Double reviewTotalRating,

    @Schema(description = "요청 상세 사유(ETC 사유일 때 필수 입력)", example = "메뉴와 무관한 홍보 내용이 포함되어 있습니다.")
    String detailReason,

    @Schema(description = "반려 사유(반려 처리된 경우에만 값 존재)", example = "게시 기준 위반 사실이 확인되지 않습니다.")
    String rejectReason,

    @Schema(description = "재노출 예정일시(게시중단 상태일 때만 값 존재)", example = "2026-09-16T14:30:00")
    LocalDateTime blindUntil,

    @Schema(description = "리뷰 이미지 URL 목록")
    List<String> reviewImageUrls,

    @Schema(description = "증빙 서류 URL 목록(신분증·위임장·사업자등록증 등, 최대 3개)")
    List<String> attachmentUrls,

    @Schema(description = "리뷰 작성 회원 닉네임", example = "맛집탐험가")
    String reviewMemberNickname,

    @Schema(description = "리뷰 숨김 여부(이미 숨겨진 리뷰인지 판단용)", example = "false")
    Boolean reviewHidden,

    @Schema(description = "리뷰 작성일시", example = "2026-01-01T00:00:00")
    LocalDateTime reviewCreatedAt,

    @Schema(description = "요청 생성일시", example = "2026-01-02T00:00:00")
    LocalDateTime createdAt
) {
    public static ReviewBlindRequestDetailResponse from(
        Long id,
        Long reviewId,
        Long shopId,
        String shopName,
        String reason,
        String reasonDescription,
        String status,
        String statusDescription,
        String reviewContent,
        Double reviewTotalRating,
        String detailReason,
        String rejectReason,
        LocalDateTime blindUntil,
        List<String> reviewImageUrls,
        List<String> attachmentUrls,
        String reviewMemberNickname,
        Boolean reviewHidden,
        LocalDateTime reviewCreatedAt,
        LocalDateTime createdAt
    ) {
        return new ReviewBlindRequestDetailResponse(
            id,
            reviewId,
            shopId,
            shopName,
            reason,
            reasonDescription,
            status,
            statusDescription,
            reviewContent,
            reviewTotalRating,
            detailReason,
            rejectReason,
            blindUntil,
            reviewImageUrls,
            attachmentUrls,
            reviewMemberNickname,
            reviewHidden,
            reviewCreatedAt,
            createdAt
        );
    }
}
