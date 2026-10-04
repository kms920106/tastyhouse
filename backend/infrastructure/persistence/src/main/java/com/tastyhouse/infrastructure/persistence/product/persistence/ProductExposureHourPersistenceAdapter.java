package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductExposureHourPersistencePort;

@Repository
class ProductExposureHourPersistenceAdapter implements ProductExposureHourPersistencePort {

    private final ProductExposureHourJpaRepository productExposureHourJpaRepository;

    public ProductExposureHourPersistenceAdapter(ProductExposureHourJpaRepository productExposureHourJpaRepository) {
        this.productExposureHourJpaRepository = productExposureHourJpaRepository;
    }

    @Override
    public List<ProductExposureHour> saveAll(List<ProductExposureHour> hours) {
        if (hours.isEmpty()) {
            return List.of();
        }
        List<ProductExposureHourJpaEntity> entities = hours.stream()
            .map(ProductExposureHourMapper::toEntity)
            .toList();
        return productExposureHourJpaRepository.saveAll(entities).stream()
            .map(ProductExposureHourMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductExposureHour> findAllByProductId(ProductId productId) {
        return productExposureHourJpaRepository.findAllByProductId(productId.value()).stream()
            .map(ProductExposureHourMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteAllByProductId(ProductId productId) {
        productExposureHourJpaRepository.deleteAllByProductId(productId.value());
    }
}
