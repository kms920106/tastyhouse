package com.tastyhouse.application.shop.port.out;

import java.util.List;

public record ShopPhotoCategoryViewResult(
    String name,
    List<String> imageUrls
) {
}
