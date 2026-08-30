package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.repository.ProductCommonOptionRepository;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

/**
 * 상품 공통 옵션 write 어댑터. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
@Repository
public class ProductCommonOptionRepositoryImpl implements ProductCommonOptionRepository {

    private final ProductCommonOptionJpaRepository productCommonOptionJpaRepository;

    public ProductCommonOptionRepositoryImpl(ProductCommonOptionJpaRepository productCommonOptionJpaRepository) {
        this.productCommonOptionJpaRepository = productCommonOptionJpaRepository;
    }

    @Override
    public Optional<ProductCommonOption> findById(ProductCommonOptionId id) {
        return productCommonOptionJpaRepository.findById(id.value()).map(ProductCommonOptionMapper::toDomain);
    }

    @Override
    public ProductCommonOption save(ProductCommonOption entity) {
        if (entity.getId() == null) {
            ProductCommonOptionJpaEntity saved =
                productCommonOptionJpaRepository.save(ProductCommonOptionMapper.toEntity(entity));
            return ProductCommonOptionMapper.toDomain(saved);
        }

        ProductCommonOptionJpaEntity jpaEntity = productCommonOptionJpaRepository.findById(entity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 공통 옵션입니다: " + entity.getId()));
        ProductCommonOptionMapper.applyChanges(jpaEntity, entity);
        return ProductCommonOptionMapper.toDomain(jpaEntity);
    }

    @Override
    public List<ProductCommonOption> findAllByIdIn(List<ProductCommonOptionId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        List<Long> rawIds = ids.stream().map(ProductCommonOptionId::value).toList();
        return productCommonOptionJpaRepository.findAllByIdIn(rawIds).stream()
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
