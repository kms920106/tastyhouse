package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

public interface ProductOptionLoadPort {

    Optional<ProductOption> findById(ProductOptionId id);

    List<ProductOption> findAllByIdIn(List<ProductOptionId> ids);

    List<ProductOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId);

    List<ProductOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime);
}
