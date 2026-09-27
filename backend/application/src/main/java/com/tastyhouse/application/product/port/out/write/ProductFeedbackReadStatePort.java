package com.tastyhouse.application.product.port.out.write;

import java.util.Optional;

public interface ProductFeedbackReadStatePort {
    ProductFeedbackReadState save(ProductFeedbackReadState feedbackRead);

    Optional<ProductFeedbackReadState> findByShopId(Long shopId);
}
