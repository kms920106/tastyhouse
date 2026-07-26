package com.tastyhouse.ceoapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 이미지 변경 요청 항목 응답")
public record ShopImageChangeRequestItemResponse(
    @Schema(description = "요청 ID", example = "1")
    Long id,

    @Schema(description = "이미지 유형", example = "TRADEMARK", allowableValues = {"TRADEMARK", "THUMBNAIL"})
    String imageType,

    @Schema(description = "요청된 이미지 파일 ID", example = "11")
    Long imageFileId,

    @Schema(description = "요청된 이미지 URL(미리보기용, 파일이 없으면 null)", example = "https://cdn.tastyhouse.com/shop/trademark/11.png")
    String imageUrl,

    @Schema(description = "승인 상태", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    String status,

    @Schema(description = "반려 사유", example = "이미지가 흐릿합니다.")
    String rejectReason
) {
    public static ShopImageChangeRequestItemResponse of(
        Long id,
        String imageType,
        Long imageFileId,
        String imageUrl,
        String status,
        String rejectReason
    ) {
        return new ShopImageChangeRequestItemResponse(
            id,
            imageType,
            imageFileId,
            imageUrl,
            status,
            rejectReason
        );
    }
}
