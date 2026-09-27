package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductOptionStatePort {
    Optional<ProductOptionState> findById(Long id);

    ProductOptionState save(ProductOptionState productOption);

    List<ProductOptionState> findAllByIdIn(List<Long> ids);

    List<ProductOptionState> findAllByOptionGroupId(Long optionGroupId);

    List<ProductOptionState> findAllSoldOutExpiredBefore(LocalDateTime baseTime);
}
