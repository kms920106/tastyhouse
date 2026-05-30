package com.tastyhouse.webapi.shop.response;

import java.math.BigDecimal;

public record EditorChoiceProductItem(
    Long id,
    String shopName,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
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
