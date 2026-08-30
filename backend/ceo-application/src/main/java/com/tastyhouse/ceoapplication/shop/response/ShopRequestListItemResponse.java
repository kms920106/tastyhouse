package com.tastyhouse.ceoapplication.shop.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 요청처리 현황 목록 항목 응답.
 *
 * <p>코드와 한글 라벨을 함께 내려준다 — 코드는 프론트 분기용, 라벨은 표시용이다. 라벨을 서버가 내려주면
 * 프론트에 유형·상태 라벨 상수를 복제하지 않아 표기 변경이 서버 배포만으로 반영된다.
 *
 * <p>{@code requestId}는 <b>요청의 유일한 대외 식별자</b>다 — 상세·취소·댓글 URL이 모두 이 값 하나만 쓴다.
 *
 * <p>첨부는 존재 여부만 내려주고 URL은 상세에서 준다(목록에서 파일 join·URL 조립 비용을 치르지 않는다).
 */
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

    public static ShopRequestListItemResponse from(
        Long requestId,
        String requestType,
        String requestTypeDescription,
        String summary,
        String status,
        String statusDescription,
        String rejectReason,
        boolean contractAmending,
        boolean hasAttachment,
        long commentCount,
        LocalDateTime requestedAt,
        LocalDateTime processedAt
    ) {
        return new ShopRequestListItemResponse(
            requestId,
            requestType,
            requestTypeDescription,
            summary,
            status,
            statusDescription,
            rejectReason,
            contractAmending,
            hasAttachment,
            commentCount,
            requestedAt,
            processedAt
        );
    }
}
