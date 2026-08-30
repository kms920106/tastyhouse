package com.tastyhouse.ceoapplication.shop.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 요청처리 현황 상세 응답. 목록 항목의 전 필드에 첨부 정보와 유형별 서브 객체를 더한 형태다.
 *
 * <p>다형 응답({@code oneOf})이나 {@code Map<String,Object>} 대신 <b>nullable 서브 객체</b>를 쓴다 —
 * OpenAPI로 그대로 표현되고, 프론트 분기가 {@code requestType} 하나로 결정되며, 유형이 추가될 때 필드
 * 추가만으로 끝난다.
 *
 * <p>{@code status}·{@code rejectReason}은 <b>원본 애그리거트 값</b>이다. 인덱스 행은 파생 읽기모델이라
 * 진실원이 아니므로, drift가 생겨도 영향 범위가 목록 배지 하나로 좁혀진다.
 */
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

    public static ShopRequestDetailResponse from(
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
        LocalDateTime processedAt,
        String attachmentLabel,
        String attachmentUrl,
        ShopRequestImageChangeResponse imageChange,
        ShopRequestAdjustmentResponse deliveryAreaAdjustment,
        ShopRequestReviewBlindResponse reviewBlind
    ) {
        return new ShopRequestDetailResponse(
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
            processedAt,
            attachmentLabel,
            attachmentUrl,
            imageChange,
            deliveryAreaAdjustment,
            reviewBlind
        );
    }
}
