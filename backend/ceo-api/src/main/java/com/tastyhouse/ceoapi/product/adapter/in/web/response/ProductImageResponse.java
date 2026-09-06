package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductImageManagementResult;

@Schema(description = "메뉴 이미지")
public record ProductImageResponse(
    @Schema(description = "이미지 ID", example = "3")
    Long id,

    @Schema(description = "표시용 이미지 URL. 파일이 없으면 null", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2026%2F08%2F18%2Fmenu.jpg?alt=media")
    String imageUrl,

    @Schema(description = "정렬 순서(0부터). 노출 중 최소 순서가 대표 이미지가 된다.", example = "0")
    Integer sort,

    @Schema(description = "노출 여부", example = "true")
    boolean visible
) {
    public static ProductImageResponse from(ProductImageManagementResult result) {
        return new ProductImageResponse(
            result.id(),
            result.imageUrl(),
            result.sort(),
            result.visible()
        );
    }
}
