package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;

import com.tastyhouse.application.product.port.out.write.ProductFeedbackStatePort;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductFeedbackStore implements ProductFeedbackRepository {
    private final ProductFeedbackStatePort productFeedbackStatePort;

    public ProductFeedbackStore(ProductFeedbackStatePort productFeedbackStatePort) {
        this.productFeedbackStatePort = productFeedbackStatePort;
    }

    @Override
    public ProductFeedback save(ProductFeedback feedback) {
        return ProductFeedbackStateMapper.toDomain(productFeedbackStatePort.save(ProductFeedbackStateMapper.toState(feedback)));
    }

    @Override
    public boolean existsRecentDuplicate(
        MemberId memberId,
        ProductId productId,
        ProductFeedbackType feedbackType,
        LocalDateTime since
    ) {
        return productFeedbackStatePort.existsRecentDuplicate(
            memberId.value(), productId.value(), feedbackType == null ? null : feedbackType.name(), since
        );
    }

    @Override
    public boolean existsByShopIdAndCreatedAtAfter(ShopId shopId, LocalDateTime since) {
        return productFeedbackStatePort.existsByShopIdAndCreatedAtAfter(shopId.value(), since);
    }
}
