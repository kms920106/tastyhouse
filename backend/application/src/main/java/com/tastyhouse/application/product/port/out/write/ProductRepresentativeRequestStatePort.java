package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ProductRepresentativeRequestStatePort {
    ProductRepresentativeRequestState save(ProductRepresentativeRequestState request);

    Optional<ProductRepresentativeRequestState> findById(Long id);

    List<ProductRepresentativeRequestState> findAllByProductId(Long productId);

    boolean existsByProductIdAndStatus(Long productId, String status);

    long countByShopIdAndStatus(Long shopId, String status);
}
