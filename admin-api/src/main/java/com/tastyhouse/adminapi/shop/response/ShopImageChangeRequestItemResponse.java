package com.tastyhouse.adminapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 이미지 변경 요청 목록 항목 응답")
public record ShopImageChangeRequestItemResponse(
    @Schema(description = "요청 ID", example = "1")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "이미지 유형", example = "TRADEMARK", allowableValues = {"TRADEMARK", "THUMBNAIL"})
    String imageType,

    @Schema(description = "요청된 이미지 파일 ID", example = "11")
    Long imageFileId,

    @Schema(description = "승인 상태", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    String status,

    @Schema(description = "반려 사유", example = "이미지가 흐릿합니다.")
    String rejectReason
) {
    public static ShopImageChangeRequestItemResponse of(
        Long id,
        Long shopId,
        String imageType,
        Long imageFileId,
        String status,
        String rejectReason
    ) {
        return new ShopImageChangeRequestItemResponse(
            id,
            shopId,
            imageType,
            imageFileId,
            status,
            rejectReason
        );
    }
}
