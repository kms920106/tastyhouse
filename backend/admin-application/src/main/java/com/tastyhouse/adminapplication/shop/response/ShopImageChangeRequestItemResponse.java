package com.tastyhouse.adminapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 이미지 변경 요청 목록 항목 응답")
public record ShopImageChangeRequestItemResponse(
    @Schema(description = "요청 ID", example = "1")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "이미지 유형", example = "TRADEMARK", allowableValues = {"TRADEMARK", "THUMBNAIL"})
    String imageType,

    @Schema(description = "요청된 이미지 URL(미리보기용, 파일이 없으면 null)", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2025%2F02%2F16%2Ftrademark.jpg?alt=media")
    String imageUrl,

    @Schema(description = "승인 상태", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    String status,

    @Schema(description = "반려 사유", example = "이미지가 흐릿합니다.")
    String rejectReason
) {
    public static ShopImageChangeRequestItemResponse of(
        Long id,
        Long shopId,
        String imageType,
        String imageUrl,
        String status,
        String rejectReason
    ) {
        return new ShopImageChangeRequestItemResponse(
            id,
            shopId,
            imageType,
            imageUrl,
            status,
            rejectReason
        );
    }
}
