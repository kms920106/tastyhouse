package com.tastyhouse.application.product.store;

import com.tastyhouse.application.product.port.out.write.ProductFeedbackState;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ProductFeedbackStateMapper {
    private ProductFeedbackStateMapper() {
    }

    static ProductFeedback toDomain(ProductFeedbackState state) {
        return ProductFeedback.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.feedbackType() == null ? null : ProductFeedbackType.valueOf(state.feedbackType()),
            state.content(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ProductFeedbackState toState(ProductFeedback feedback) {
        return new ProductFeedbackState(
            feedback.getId(),
            feedback.getProductId() == null ? null : feedback.getProductId().value(),
            feedback.getShopId() == null ? null : feedback.getShopId().value(),
            feedback.getMemberId() == null ? null : feedback.getMemberId().value(),
            feedback.getFeedbackType() == null ? null : feedback.getFeedbackType().name(),
            feedback.getContent(),
            feedback.getCreatedAt(),
            feedback.getUpdatedAt()
        );
    }
}
