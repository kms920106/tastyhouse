package com.tastyhouse.adminapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 이미지 변경 요청 검수 목록 항목.
 *
 * <p>파일 식별자 대신 표시용 URL을 담는다 — 검수자가 이미지를 눈으로 확인해야 하고, 프론트엔드가
 * fileId로 URL을 조립할 공식 경로가 없다.
 */
@Schema(description = "메뉴 이미지 변경 요청 목록 항목")
public record ProductImageChangeRequestItemResponse(
    @Schema(description = "요청 ID", example = "12")
    Long id,

    @Schema(description = "메뉴 ID", example = "5")
    Long productId,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "메뉴명", example = "비빔밥")
    String productName,

    @Schema(description = "요청된 이미지의 표시용 URL. 파일이 없으면 null", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2026%2F08%2F18%2Fmenu.jpg?alt=media")
    String imageUrl,

    @Schema(description = "승인 상태", example = "PENDING",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"})
    String status,

    @Schema(description = "반려 사유. 반려가 아니면 null", example = "메뉴가 잘 보이지 않습니다.")
    String rejectReason
) {

    public static ProductImageChangeRequestItemResponse from(
        Long id,
        Long productId,
        Long shopId,
        String productName,
        String imageUrl,
        String status,
        String rejectReason
    ) {
        return new ProductImageChangeRequestItemResponse(
            id,
            productId,
            shopId,
            productName,
            imageUrl,
            status,
            rejectReason
        );
    }
}
