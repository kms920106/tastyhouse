package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ProductOptionGroupLinkStatePort {
    ProductOptionGroupLinkState save(ProductOptionGroupLinkState link);

    Optional<ProductOptionGroupLinkState> findByProductIdAndOptionGroupId(Long productId, Long optionGroupId);

    List<ProductOptionGroupLinkState> findAllByProductId(Long productId);

    List<ProductOptionGroupLinkState> findAllByOptionGroupId(Long optionGroupId);

    List<ProductOptionGroupLinkState> findAllByOptionGroupIdIn(List<Long> optionGroupIds);

    boolean existsByProductIdAndOptionGroupId(Long productId, Long optionGroupId);

    void deleteById(Long id);
}
