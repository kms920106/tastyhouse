package com.tastyhouse.application.product.store;

import java.util.List;

import com.tastyhouse.application.product.port.out.write.ProductExposureHourStatePort;
import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;

public class ProductExposureHourStore implements ProductExposureHourRepository {
    private final ProductExposureHourStatePort productExposureHourStatePort;

    public ProductExposureHourStore(ProductExposureHourStatePort productExposureHourStatePort) {
        this.productExposureHourStatePort = productExposureHourStatePort;
    }

    @Override
    public List<ProductExposureHour> saveAll(List<ProductExposureHour> hours) {
        return productExposureHourStatePort.saveAll(hours.stream().map(ProductExposureHourStateMapper::toState).toList())
            .stream()
            .map(ProductExposureHourStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductExposureHour> findAllByProductId(ProductId productId) {
        return productExposureHourStatePort.findAllByProductId(productId.value()).stream()
            .map(ProductExposureHourStateMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteAllByProductId(ProductId productId) {
        productExposureHourStatePort.deleteAllByProductId(productId.value());
    }
}
