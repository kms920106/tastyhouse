package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ProductShopLinkStatePort {
    ProductShopLinkState save(ProductShopLinkState link);

    Optional<ProductShopLinkState> findByProductIdAndShopId(Long productId, Long shopId);

    List<ProductShopLinkState> findAllByProductId(Long productId);

    List<ProductShopLinkState> findAllByShopId(Long shopId);

    boolean existsByProductIdAndShopId(Long productId, Long shopId);

    long countByProductId(Long productId);

    void deleteById(Long id);
}
