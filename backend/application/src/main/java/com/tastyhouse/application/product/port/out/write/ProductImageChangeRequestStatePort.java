package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ProductImageChangeRequestStatePort {
    ProductImageChangeRequestState save(ProductImageChangeRequestState request);

    Optional<ProductImageChangeRequestState> findById(Long id);

    List<ProductImageChangeRequestState> findAllByProductId(Long productId);

    boolean existsByProductIdAndStatus(Long productId, String status);
}
