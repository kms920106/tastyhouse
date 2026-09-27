package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;

public interface ProductFeedbackStatePort {
    ProductFeedbackState save(ProductFeedbackState feedback);

    boolean existsRecentDuplicate(Long memberId, Long productId, String feedbackType, LocalDateTime since);

    boolean existsByShopIdAndCreatedAtAfter(Long shopId, LocalDateTime since);
}
