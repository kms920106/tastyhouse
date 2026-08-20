package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.repository.ProductExposureHourRepository;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 메뉴 노출 요일·시간대 write 어댑터. 설정은 {@code deleteAllByProductId} → {@code saveAll}의 replace-all로 교체한다.
 */
@Repository
public class ProductExposureHourRepositoryImpl implements ProductExposureHourRepository {

    private final ProductExposureHourJpaRepository productExposureHourJpaRepository;

    public ProductExposureHourRepositoryImpl(ProductExposureHourJpaRepository productExposureHourJpaRepository) {
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
