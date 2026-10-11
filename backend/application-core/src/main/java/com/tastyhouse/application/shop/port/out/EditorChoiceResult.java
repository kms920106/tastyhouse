package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductSimpleResult;

public record EditorChoiceResult(
    Long id,
    Long shopId,
    String name,
    String title,
    String content,
    String shopImageUrl,
    List<ProductSimpleResult> products
) {
}
