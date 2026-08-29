package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 이미지 관리 목록 항목.
 *
 * <p>파일 식별자를 노출하지 않고 표시용 URL만 담는다 — 프론트엔드가 fileId로 URL을 조립할 공식
 * 엔드포인트가 없어 존재하지 않는 경로를 추측하게 되기 때문이다.
 */
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

    public static ProductImageResponse from(
        Long id,
        String imageUrl,
        Integer sort,
        boolean visible
    ) {
        return new ProductImageResponse(
            id,
            imageUrl,
            sort,
            visible
        );
    }
}
