package com.tastyhouse.application.product.port.out.write;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;

public interface ProductExposureHourLoadPort {

    List<ProductExposureHour> findAllByProductId(ProductId productId);
}
