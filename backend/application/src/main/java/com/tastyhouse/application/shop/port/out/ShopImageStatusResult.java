package com.tastyhouse.application.shop.port.out;

import java.util.List;

public record ShopImageStatusResult(
    String currentImageUrl,
    List<ShopImageChangeRequestResult> requests
) {
}
