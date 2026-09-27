package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ProductPriceStatePort {
    ProductPriceState save(ProductPriceState productPrice);

    Optional<ProductPriceState> findById(Long id);

    List<ProductPriceState> findAllByProductId(Long productId);

    List<ProductPriceState> findAllByShopId(Long shopId);

    void deleteAllByIdIn(List<Long> ids);
}
