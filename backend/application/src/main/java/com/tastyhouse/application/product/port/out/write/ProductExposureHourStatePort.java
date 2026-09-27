package com.tastyhouse.application.product.port.out.write;

import java.util.List;

public interface ProductExposureHourStatePort {
    List<ProductExposureHourState> saveAll(List<ProductExposureHourState> hours);

    List<ProductExposureHourState> findAllByProductId(Long productId);

    void deleteAllByProductId(Long productId);
}
