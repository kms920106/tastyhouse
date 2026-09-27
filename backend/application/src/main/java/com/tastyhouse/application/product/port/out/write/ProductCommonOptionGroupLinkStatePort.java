package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ProductCommonOptionGroupLinkStatePort {
    ProductCommonOptionGroupLinkState save(ProductCommonOptionGroupLinkState link);

    Optional<ProductCommonOptionGroupLinkState> findByProductIdAndOptionGroupId(Long productId, Long optionGroupId);

    List<ProductCommonOptionGroupLinkState> findAllByProductId(Long productId);

    List<ProductCommonOptionGroupLinkState> findAllByOptionGroupId(Long optionGroupId);

    List<ProductCommonOptionGroupLinkState> findAllByOptionGroupIdIn(List<Long> optionGroupIds);

    boolean existsByProductIdAndOptionGroupId(Long productId, Long optionGroupId);

    void deleteById(Long id);
}
