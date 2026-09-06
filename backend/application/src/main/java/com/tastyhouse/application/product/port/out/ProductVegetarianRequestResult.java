package com.tastyhouse.application.product.port.out;

import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public record ProductVegetarianRequestResult(
    Long id,
    Long productId,
    Long shopId,
    String productName,
    VegetarianType vegetarianType,
    String ingredients,
    String description,
    ApprovalStatus status,
    String rejectReason
) {
}
