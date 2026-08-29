package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "품절·숨김 관리 메뉴 항목")
public record ProductAvailabilityItemResponse(
    @Schema(description = "메뉴 ID", example = "10")
    Long id,

    @Schema(description = "메뉴명", example = "떡볶이")
    String name,

    @Schema(description = "정가", example = "10000")
    Integer originalPrice,

    @Schema(description = "할인가. 할인이 없으면 null", example = "8000")
    Integer discountPrice,

    @Schema(description = "대표 이미지 URL. 이미지가 없으면 null")
    String imageUrl,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut,

    @Schema(description = "품절 자동해제 시각. 무기한 품절이거나 판매중이면 null", example = "2026-08-18T09:00:00")
    LocalDateTime soldOutUntil,

    @Schema(description = "노출 여부", example = "true")
    boolean visible,

    @Schema(description = "사장님 추천 메뉴 여부. 숨김 제약 안내에 쓰인다.", example = "false")
    boolean representative,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {

    public static ProductAvailabilityItemResponse from(
        Long id,
        String name,
        Integer originalPrice,
        Integer discountPrice,
        String imageUrl,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        boolean representative,
        Integer sort
    ) {
        return new ProductAvailabilityItemResponse(
            id,
            name,
            originalPrice,
            discountPrice,
            imageUrl,
            soldOut,
            soldOutUntil,
            visible,
            representative,
            sort
        );
    }
}
