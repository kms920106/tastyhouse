package com.tastyhouse.application.product.port.in;

public interface ProductReviewCountQueryUseCase {

    int findProductReviewCount(Long productId);
}
