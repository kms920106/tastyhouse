package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductImageStatusResult(
    List<ProductImageManagementResult> images,
    List<ProductImageChangeRequestResult> requests
) {
}
