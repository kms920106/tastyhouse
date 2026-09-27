package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductStatePort {
    Optional<ProductState> findById(Long id);

    ProductState save(ProductState product);

    List<ProductState> findAllByShopIdAndIdIn(Long shopId, List<Long> ids);

    long countVisibleByShopId(Long shopId);

    long countVisibleRepresentativeByShopId(Long shopId);

    long countRepresentativeByShopId(Long shopId);

    List<ProductState> findAllSoldOutExpiredBefore(LocalDateTime baseTime);

    boolean existsByShopIdAndName(Long shopId, String name);

    boolean existsByShopIdAndNameAndIdNot(Long shopId, String name, Long excludedId);

    List<ProductState> findAllByShopIdAndCategoryId(Long shopId, Long productCategoryId);

    long countByCategoryId(Long productCategoryId);

    Optional<ProductState> findByIdIncludingDeleted(Long id);
}
