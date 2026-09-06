package com.tastyhouse.domain.product.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public interface ProductCommonOptionRepository {
    Optional<ProductCommonOption> findById(ProductCommonOptionId id);

    ProductCommonOption save(ProductCommonOption productCommonOption);

    List<ProductCommonOption> findAllByIdIn(List<ProductCommonOptionId> ids);

    List<ProductCommonOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId);

    List<ProductCommonOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime);
}
