package com.tastyhouse.webapi.crawling.bbq.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "서브 옵션 아이템 상세 응답")
public record SubOptionItemDetailResponse(
    @Schema(description = "아이템 ID", example = "475")
    Long id,

    @Schema(description = "아이템 제목", example = "한마리")
    String itemTitle,

    @Schema(description = "추가 가격", example = "0")
    Integer addPrice,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut,

    @Schema(description = "숨김 여부", example = "false")
    boolean hidden
) {
    public static SubOptionItemDetailResponse from(
        Long id,
        String itemTitle,
        Integer addPrice,
        boolean soldOut,
        boolean hidden
    ) {
        return new SubOptionItemDetailResponse(
            id,
            itemTitle,
            addPrice,
            soldOut,
            hidden
        );
    }
}
