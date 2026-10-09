package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductFeedbackRead;

public interface ProductFeedbackReadSavePort {

    ProductFeedbackRead save(ProductFeedbackRead feedbackRead);
}
