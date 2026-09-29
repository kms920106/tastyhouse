package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductFeedbackPersistencePort {
    ProductFeedback save(ProductFeedback feedback);

    boolean existsRecentDuplicate(
        MemberId memberId,
        ProductId productId,
        ProductFeedbackType feedbackType,
        LocalDateTime since
    );

    boolean existsByShopIdAndCreatedAtAfter(ShopId shopId, LocalDateTime since);
}
