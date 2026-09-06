package com.tastyhouse.application.product.port.out;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductFeedbackType;

public record ProductFeedbackSummaryResult(
    Long productId,
    String productName,
    ProductFeedbackType feedbackType,
    Integer count,
    List<String> contents
) {
}
