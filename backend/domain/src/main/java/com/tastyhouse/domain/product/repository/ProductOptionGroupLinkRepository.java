package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public interface ProductOptionGroupLinkRepository {
    ProductOptionGroupLink save(ProductOptionGroupLink link);

    Optional<ProductOptionGroupLink> findByProductIdAndOptionGroupId(
        ProductId productId,
        ProductOptionGroupId optionGroupId
    );

    List<ProductOptionGroupLink> findAllByProductId(ProductId productId);

    List<ProductOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId);

    List<ProductOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds);

    boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId);

    void delete(ProductOptionGroupLink link);
}
