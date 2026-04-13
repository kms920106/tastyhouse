package com.tastyhouse.webapi.place.response;

import java.math.BigDecimal;

public record EditorChoiceProductItem(
    Long id,
    String placeName,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate
) {
    public static EditorChoiceProductItem from(
        Long id,
        String placeName,
        String name,
        String imageUrl,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate
    ) {
        return new EditorChoiceProductItem(
            id,
            placeName,
            name,
            imageUrl,
            originalPrice,
            discountPrice,
            discountRate
        );
    }
}
