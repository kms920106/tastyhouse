package com.tastyhouse.infrastructure.persistence.product.query;

public record ProductFeedbackSummaryRow(
    Long productId,
    String name,
    String feedbackType,
    Long count
) {
}
