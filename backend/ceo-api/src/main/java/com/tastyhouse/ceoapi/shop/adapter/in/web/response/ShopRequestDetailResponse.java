package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopRequestDetailViewResult;

@Schema(description = "요청처리 현황 상세")
public record ShopRequestDetailResponse(

    @Schema(description = "요청 ID", example = "1024")
    Long requestId,

    @Schema(
        description = "요청 유형 코드",
        example = "DELIVERY_AREA_ADJUSTMENT",
        allowableValues = {"TRADEMARK_CHANGE", "THUMBNAIL_CHANGE", "DELIVERY_AREA_ADJUSTMENT", "REVIEW_BLIND"}
    )
    String requestType,

    @Schema(description = "요청 유형 한글 라벨", example = "배달지역 조정 신청")
    String requestTypeDescription,

    @Schema(description = "무엇을 요청했는지 한 줄 요약", example = "맛있는집 강남점 (BBQ)")
    String summary,

    @Schema(
        description = "처리 상태 코드(원본 애그리거트 값)",
        example = "REJECTED",
        allowableValues = {"PENDING", "IN_PROGRESS", "APPROVED", "REJECTED", "CANCELED"}
    )
    String status,

    @Schema(description = "처리 상태 한글 라벨", example = "반려")
    String statusDescription,

    @Schema(description = "반려 사유. 반려가 아니면 null", example = "제출 서류의 사업자번호가 확인되지 않습니다.")
    String rejectReason,

    @Schema(description = "승인 시 전자계약서가 수정되는 요청인지", example = "true")
    boolean contractAmending,

    @Schema(description = "첨부 존재 여부", example = "true")
    boolean hasAttachment,

    @Schema(description = "문의·답변 건수", example = "2")
    long commentCount,

    @Schema(description = "신청 일시", example = "2026-08-11T19:46:03")
    LocalDateTime requestedAt,

    @Schema(description = "최근 처리 일시. 접수 직후면 null", example = "2026-08-12T09:12:44")
    LocalDateTime processedAt,

    @Schema(description = "첨부 명칭. 첨부가 없는 유형이면 null", example = "정보제공 동의서")
    String attachmentLabel,

    @Schema(description = "첨부 표시용 URL. 미첨부면 null", example = "https://storage.example.com/2026/08/consent.pdf")
    String attachmentUrl,

    @Schema(description = "이미지 변경요청 상세. 요청 유형이 이미지 변경일 때만 채워진다")
    ShopRequestImageChangeResponse imageChange,

    @Schema(description = "배달지역 조정 신청 상세. 요청 유형이 조정 신청일 때만 채워진다")
    ShopRequestAdjustmentResponse deliveryAreaAdjustment,

    @Schema(description = "리뷰 게시중단 요청 상세. 요청 유형이 리뷰 게시중단일 때만 채워진다")
    ShopRequestReviewBlindResponse reviewBlind
) {
    public static ShopRequestDetailResponse from(ShopRequestDetailViewResult result) {
        return new ShopRequestDetailResponse(
            result.requestId(),
            result.requestType().name(),
            result.requestType().getDescription(),
            result.summary(),
            result.status().name(),
            result.status().getDescription(),
            result.rejectReason(),
            result.contractAmending(),
            result.hasAttachment(),
            result.commentCount(),
            result.requestedAt(),
            result.processedAt(),
            result.attachmentLabel(),
            result.attachmentUrl(),
            result.imageChange() == null ? null : ShopRequestImageChangeResponse.from(result.imageChange()),
            result.deliveryAreaAdjustment() == null
                ? null
                : ShopRequestAdjustmentResponse.from(result.deliveryAreaAdjustment()),
            result.reviewBlind() == null ? null : ShopRequestReviewBlindResponse.from(result.reviewBlind())
        );
    }
}
