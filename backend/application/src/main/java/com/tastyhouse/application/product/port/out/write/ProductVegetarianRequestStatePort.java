package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ProductVegetarianRequestStatePort {
    ProductVegetarianRequestState save(ProductVegetarianRequestState request);

    Optional<ProductVegetarianRequestState> findById(Long id);

    List<ProductVegetarianRequestState> findAllByProductId(Long productId);

    boolean existsByProductIdAndStatus(Long productId, String status);
}
