package com.tastyhouse.ceoapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 이미지 등록 요청의 검수 상태.
 */
@Schema(description = "메뉴 이미지 검수 요청")
public record ProductImageChangeRequestResponse(
    @Schema(description = "요청 ID", example = "12")
    Long id,

    @Schema(description = "요청된 이미지의 표시용 URL. 파일이 없으면 null", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2026%2F08%2F18%2Fmenu.jpg?alt=media")
    String imageUrl,

    @Schema(description = "검수 상태", example = "PENDING",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"})
    String status,

    @Schema(description = "반려 사유. 반려가 아니면 null", example = "메뉴가 잘 보이지 않습니다.")
    String rejectReason
) {

    public static ProductImageChangeRequestResponse from(
        Long id,
        String imageUrl,
        String status,
        String rejectReason
    ) {
        return new ProductImageChangeRequestResponse(
            id,
            imageUrl,
            status,
            rejectReason
        );
    }
}
