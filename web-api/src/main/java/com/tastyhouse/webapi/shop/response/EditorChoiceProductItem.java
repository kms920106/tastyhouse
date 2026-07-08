package com.tastyhouse.webapi.shop.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에디터 초이스 상품 항목")
public record EditorChoiceProductItem(
    @Schema(description = "상품 ID", example = "1")
    Long id,

    @Schema(description = "가게명", example = "BBQ치킨 성내점")
    String shopName,

    @Schema(description = "상품명", example = "황금올리브치킨")
    String name,

    @Schema(description = "상품 이미지 URL", example = "https://cdn.tastyhouse.com/product/1/thumbnail.jpg")
    String imageUrl,

    @Schema(description = "정가", example = "20000")
    Integer originalPrice,

    @Schema(description = "할인가", example = "16000")
    Integer discountPrice,

    @Schema(description = "할인율(%)", example = "20")
    BigDecimal discountRate
) {
    public static EditorChoiceProductItem from(
        Long id,
        String shopName,
        String name,
        String imageUrl,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate
    ) {
        return new EditorChoiceProductItem(
            id,
            shopName,
            name,
            imageUrl,
            originalPrice,
            discountPrice,
            discountRate
        );
    }
}
