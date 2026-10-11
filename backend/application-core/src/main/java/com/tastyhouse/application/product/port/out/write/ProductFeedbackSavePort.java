package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductFeedback;

public interface ProductFeedbackSavePort {

    ProductFeedback save(ProductFeedback feedback);
}
