package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public interface ProductCommonOptionGroupLinkRepository {
    ProductCommonOptionGroupLink save(ProductCommonOptionGroupLink link);

    Optional<ProductCommonOptionGroupLink> findByProductIdAndOptionGroupId(
        ProductId productId,
        ProductOptionGroupId optionGroupId
    );

    List<ProductCommonOptionGroupLink> findAllByProductId(ProductId productId);

    List<ProductCommonOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId);

    List<ProductCommonOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds);

    boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId);

    void delete(ProductCommonOptionGroupLink link);
}
