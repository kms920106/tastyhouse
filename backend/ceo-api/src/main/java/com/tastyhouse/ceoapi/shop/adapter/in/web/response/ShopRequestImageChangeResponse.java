package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 요청처리 현황 상세의 이미지 변경요청 부분. {@code requestType}이 이미지 변경일 때만 채워진다.
 */
@Schema(description = "이미지 변경요청 상세")
public record ShopRequestImageChangeResponse(

    @Schema(description = "이미지 유형 코드", example = "TRADEMARK", allowableValues = {"TRADEMARK", "THUMBNAIL"})
    String imageType,

    @Schema(description = "이미지 유형 한글 라벨", example = "상표")
    String imageTypeDescription,

    @Schema(description = "요청한 이미지 URL", example = "https://storage.example.com/2026/08/trademark.png")
    String imageUrl
) {

    public static ShopRequestImageChangeResponse from(
        String imageType,
        String imageTypeDescription,
        String imageUrl
    ) {
        return new ShopRequestImageChangeResponse(
            imageType,
            imageTypeDescription,
            imageUrl
        );
    }
}
