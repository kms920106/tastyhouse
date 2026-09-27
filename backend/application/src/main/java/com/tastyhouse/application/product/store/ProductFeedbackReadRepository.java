package com.tastyhouse.application.product.store;

import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductFeedbackReadRepository {
    ProductFeedbackRead save(ProductFeedbackRead feedbackRead);

    Optional<ProductFeedbackRead> findByShopId(ShopId shopId);
}
