package com.tastyhouse.webapi.shop.response;

import java.util.List;

public record EditorChoiceResponse(
    Long id,
    String name,
    String imageUrl,
    String title,
    String content,
    List<EditorChoiceProductItem> products
) {
    public static EditorChoiceResponse from(
        Long id,
        String name,
        String imageUrl,
        String title,
        String content,
        List<EditorChoiceProductItem> products
    ) {
        return new EditorChoiceResponse(
            id,
            name,
            imageUrl,
            title,
            content,
            products
        );
    }
}
