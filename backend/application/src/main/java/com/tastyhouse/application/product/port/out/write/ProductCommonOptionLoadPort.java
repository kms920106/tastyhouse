package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public interface ProductCommonOptionLoadPort {

    Optional<ProductCommonOption> findById(ProductCommonOptionId id);

    List<ProductCommonOption> findAllByIdIn(List<ProductCommonOptionId> ids);

    List<ProductCommonOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId);

    List<ProductCommonOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime);
}
