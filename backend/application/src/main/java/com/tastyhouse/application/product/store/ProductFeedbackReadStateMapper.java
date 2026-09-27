package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadState;

final class ProductFeedbackReadStateMapper {
    private ProductFeedbackReadStateMapper() {
    }

    static ProductFeedbackRead toDomain(ProductFeedbackReadState state) {
        return ProductFeedbackRead.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.readAt(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ProductFeedbackReadState toState(ProductFeedbackRead feedbackRead) {
        return new ProductFeedbackReadState(
            feedbackRead.getId(),
            feedbackRead.getShopId() == null ? null : feedbackRead.getShopId().value(),
            feedbackRead.getReadAt(),
            feedbackRead.getCreatedAt(),
            feedbackRead.getUpdatedAt()
        );
    }
}
