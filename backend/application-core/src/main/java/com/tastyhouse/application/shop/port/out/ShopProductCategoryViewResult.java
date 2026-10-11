package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.application.product.port.out.ShopProductItemResult;

public record ShopProductCategoryViewResult(
    String categoryName,
    List<ShopProductItemResult> products
) {
}
