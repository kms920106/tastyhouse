package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

/**
 * 상품 옵션 write 어댑터. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
@Repository
public class ProductOptionRepositoryImpl implements ProductOptionRepository {

    private final ProductOptionJpaRepository productOptionJpaRepository;

    public ProductOptionRepositoryImpl(ProductOptionJpaRepository productOptionJpaRepository) {
        this.productOptionJpaRepository = productOptionJpaRepository;
    }

    @Override
    public Optional<ProductOption> findById(ProductOptionId id) {
        return productOptionJpaRepository.findById(id.value()).map(ProductOptionMapper::toDomain);
    }

    @Override
    public ProductOption save(ProductOption entity) {
        if (entity.getId() == null) {
            ProductOptionJpaEntity saved = productOptionJpaRepository.save(ProductOptionMapper.toEntity(entity));
            return ProductOptionMapper.toDomain(saved);
        }

        ProductOptionJpaEntity jpaEntity = productOptionJpaRepository.findById(entity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 옵션입니다: " + entity.getId()));
        ProductOptionMapper.applyChanges(jpaEntity, entity);
        return ProductOptionMapper.toDomain(jpaEntity);
    }

    @Override
    public List<ProductOption> findAllByIdIn(List<ProductOptionId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        List<Long> rawIds = ids.stream().map(ProductOptionId::value).toList();
        return productOptionJpaRepository.findAllByIdIn(rawIds).stream()
            .map(ProductOptionMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return productOptionJpaRepository.findAllByOptionGroupId(optionGroupId.value()).stream()
            .map(ProductOptionMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
        return productOptionJpaRepository
            .findAllBySoldOutTrueAndSoldOutUntilIsNotNullAndSoldOutUntilLessThanEqual(baseTime).stream()
            .map(ProductOptionMapper::toDomain)
            .toList();
    }
}
