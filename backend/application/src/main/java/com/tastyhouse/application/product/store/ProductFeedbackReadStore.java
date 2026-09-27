package com.tastyhouse.application.product.store;

import java.util.Optional;

import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadStatePort;
import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductFeedbackReadStore implements ProductFeedbackReadRepository {
    private final ProductFeedbackReadStatePort productFeedbackReadStatePort;

    public ProductFeedbackReadStore(ProductFeedbackReadStatePort productFeedbackReadStatePort) {
        this.productFeedbackReadStatePort = productFeedbackReadStatePort;
    }

    @Override
    public ProductFeedbackRead save(ProductFeedbackRead feedbackRead) {
        return ProductFeedbackReadStateMapper.toDomain(
            productFeedbackReadStatePort.save(ProductFeedbackReadStateMapper.toState(feedbackRead)));
    }

    @Override
    public Optional<ProductFeedbackRead> findByShopId(ShopId shopId) {
        return productFeedbackReadStatePort.findByShopId(shopId.value()).map(ProductFeedbackReadStateMapper::toDomain);
    }
}
