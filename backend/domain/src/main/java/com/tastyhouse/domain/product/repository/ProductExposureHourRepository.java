package com.tastyhouse.domain.product.repository;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;

public interface ProductExposureHourRepository {
    List<ProductExposureHour> saveAll(List<ProductExposureHour> hours);

    List<ProductExposureHour> findAllByProductId(ProductId productId);

    void deleteAllByProductId(ProductId productId);
}
