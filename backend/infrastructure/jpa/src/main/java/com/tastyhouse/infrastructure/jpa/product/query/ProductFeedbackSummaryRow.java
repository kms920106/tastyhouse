package com.tastyhouse.infrastructure.jpa.product.query;

public record ProductFeedbackSummaryRow(
    Long productId,
    String name,
    String feedbackType,
    Long count
) {
}
