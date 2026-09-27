package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductCommonOptionStatePort {
    Optional<ProductCommonOptionState> findById(Long id);

    ProductCommonOptionState save(ProductCommonOptionState productCommonOption);

    List<ProductCommonOptionState> findAllByIdIn(List<Long> ids);

    List<ProductCommonOptionState> findAllByOptionGroupId(Long optionGroupId);

    List<ProductCommonOptionState> findAllSoldOutExpiredBefore(LocalDateTime baseTime);
}
