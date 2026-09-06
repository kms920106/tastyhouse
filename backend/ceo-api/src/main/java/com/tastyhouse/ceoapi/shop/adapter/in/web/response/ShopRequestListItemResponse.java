package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopRequestListItemViewResult;

@Schema(description = "요청처리 현황 목록 항목")
public record ShopRequestListItemResponse(

    @Schema(description = "요청 ID(상세·취소·문의의 식별자)", example = "1024")
    Long requestId,

    @Schema(
        description = "요청 유형 코드",
        example = "DELIVERY_AREA_ADJUSTMENT",
        allowableValues = {"TRADEMARK_CHANGE", "THUMBNAIL_CHANGE", "DELIVERY_AREA_ADJUSTMENT"}
    )
    String requestType,

    @Schema(description = "요청 유형 한글 라벨", example = "배달지역 조정 신청")
    String requestTypeDescription,

    @Schema(description = "무엇을 요청했는지 한 줄 요약", example = "맛있는집 강남점 (BBQ)")
    String summary,

    @Schema(
        description = "처리 상태 코드",
        example = "PENDING",
        allowableValues = {"PENDING", "IN_PROGRESS", "APPROVED", "REJECTED", "CANCELED"}
    )
    String status,

    @Schema(description = "처리 상태 한글 라벨", example = "대기중")
    String statusDescription,

    @Schema(description = "반려 사유. 반려가 아니면 null", example = "제출 서류의 사업자번호가 확인되지 않습니다.")
    String rejectReason,

    @Schema(description = "승인 시 전자계약서가 수정되는 요청인지", example = "true")
    boolean contractAmending,

    @Schema(description = "첨부 존재 여부(URL은 상세에서 제공)", example = "true")
    boolean hasAttachment,

    @Schema(description = "문의·답변 건수", example = "2")
    long commentCount,

    @Schema(description = "신청 일시", example = "2026-08-11T19:46:03")
    LocalDateTime requestedAt,

    @Schema(description = "최근 처리 일시. 접수 직후면 null", example = "2026-08-12T09:12:44")
    LocalDateTime processedAt
) {
    public static ShopRequestListItemResponse from(ShopRequestListItemViewResult result) {
        return new ShopRequestListItemResponse(
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
            result.processedAt()
        );
    }
}
