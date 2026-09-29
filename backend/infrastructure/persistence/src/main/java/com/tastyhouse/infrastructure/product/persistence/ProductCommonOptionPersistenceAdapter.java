package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionPersistencePort;

@Repository
public class ProductCommonOptionPersistenceAdapter implements ProductCommonOptionPersistencePort {
    private final ProductCommonOptionJpaRepository productCommonOptionJpaRepository;

    public ProductCommonOptionPersistenceAdapter(ProductCommonOptionJpaRepository productCommonOptionJpaRepository) {
        this.productCommonOptionJpaRepository = productCommonOptionJpaRepository;
    }

    @Override
    public Optional<ProductCommonOption> findById(ProductCommonOptionId id) {
        return productCommonOptionJpaRepository.findById(id.value()).map(ProductCommonOptionMapper::toDomain);
    }

    @Override
    public ProductCommonOption save(ProductCommonOption productCommonOption) {
        if (productCommonOption.getId() == null) {
            ProductCommonOptionJpaEntity saved =
                productCommonOptionJpaRepository.save(ProductCommonOptionMapper.toEntity(productCommonOption));
            return ProductCommonOptionMapper.toDomain(saved);
        }

        ProductCommonOptionJpaEntity jpaEntity = productCommonOptionJpaRepository.findById(productCommonOption.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 공통 옵션입니다: " + productCommonOption.getId()));
        ProductCommonOptionMapper.applyChanges(jpaEntity, productCommonOption);
        return ProductCommonOptionMapper.toDomain(jpaEntity);
    }

    @Override
    public List<ProductCommonOption> findAllByIdIn(List<ProductCommonOptionId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productCommonOptionJpaRepository.findAllByIdIn(ids.stream().map(ProductCommonOptionId::value).toList())
            .stream()
            .map(ProductCommonOptionMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return productCommonOptionJpaRepository.findAllByOptionGroupId(optionGroupId.value()).stream()
            .map(ProductCommonOptionMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
        return productCommonOptionJpaRepository
            .findAllBySoldOutTrueAndSoldOutUntilIsNotNullAndSoldOutUntilLessThanEqual(baseTime).stream()
            .map(ProductCommonOptionMapper::toDomain)
            .toList();
    }
}
