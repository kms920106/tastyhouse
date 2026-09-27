package com.tastyhouse.application.product.port.out.write;

import java.util.Optional;

public interface ProductBbqStatePort {
    Optional<ProductBbqState> findByProductId(Long productId);

    ProductBbqState save(ProductBbqState productBbq);
}
