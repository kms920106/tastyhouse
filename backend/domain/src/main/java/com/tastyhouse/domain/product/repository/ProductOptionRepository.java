package com.tastyhouse.domain.product.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

public interface ProductOptionRepository {
    Optional<ProductOption> findById(ProductOptionId id);

    ProductOption save(ProductOption productOption);

    List<ProductOption> findAllByIdIn(List<ProductOptionId> ids);

    List<ProductOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId);

    List<ProductOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime);
}
