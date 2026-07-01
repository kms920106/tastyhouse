package com.tastyhouse.webapi.crawling.bbq.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * BBQ 상품 서브 옵션 응답 DTO
 */
@Schema(description = "BBQ 상품 서브 옵션 응답")
public record BbqProductSubOptionResponse(
    @Schema(description = "서브 옵션 ID", example = "53")
    Long id,

    @Schema(description = "서브 옵션 제목", example = "뿜치킹 부분육 선택")
    String subOptionTitle,

    @Schema(description = "필수 선택 개수", example = "1")
    Integer requiredSelectCount,

    @Schema(description = "최대 선택 개수", example = "1")
    Integer maxSelectCount,

    @Schema(description = "서브 옵션 아이템 상세 목록")
    List<SubOptionItemDetailResponse> subOptionItemDetailResponseList
) {
    public static BbqProductSubOptionResponse from(
        Long id,
        String subOptionTitle,
        Integer requiredSelectCount,
        Integer maxSelectCount,
        List<SubOptionItemDetailResponse> subOptionItemDetailResponseList
    ) {
        return new BbqProductSubOptionResponse(
            id,
            subOptionTitle,
            requiredSelectCount,
            maxSelectCount,
            subOptionItemDetailResponseList
        );
    }

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
}
