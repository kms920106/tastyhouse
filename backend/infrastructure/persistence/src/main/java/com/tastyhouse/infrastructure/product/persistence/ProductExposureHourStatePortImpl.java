package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductExposureHourState;
import com.tastyhouse.application.product.port.out.write.ProductExposureHourStatePort;

@Repository
public class ProductExposureHourStatePortImpl implements ProductExposureHourStatePort {
    private final ProductExposureHourJpaRepository productExposureHourJpaRepository;

    public ProductExposureHourStatePortImpl(ProductExposureHourJpaRepository productExposureHourJpaRepository) {
        this.productExposureHourJpaRepository = productExposureHourJpaRepository;
    }

    @Override
    public List<ProductExposureHourState> saveAll(List<ProductExposureHourState> hours) {
        if (hours.isEmpty()) {
            return List.of();
        }
        List<ProductExposureHourJpaEntity> entities = hours.stream()
            .map(ProductExposureHourMapper::toEntity)
            .toList();
        return productExposureHourJpaRepository.saveAll(entities).stream()
            .map(ProductExposureHourMapper::toState)
            .toList();
    }

    @Override
    public List<ProductExposureHourState> findAllByProductId(Long productId) {
        return productExposureHourJpaRepository.findAllByProductId(productId).stream()
            .map(ProductExposureHourMapper::toState)
            .toList();
    }

    @Override
    public void deleteAllByProductId(Long productId) {
        productExposureHourJpaRepository.deleteAllByProductId(productId);
    }
}
