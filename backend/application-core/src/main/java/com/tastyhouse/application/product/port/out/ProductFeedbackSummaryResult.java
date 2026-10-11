package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductFeedbackSummaryResult(
    Long productId,
    String productName,
    String feedbackType,
    Integer count,
    List<String> contents
) {
}
