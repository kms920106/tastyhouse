package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.domain.model.ProductOption;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionRepository;
import com.tastyhouse.core.domain.product.domain.vo.ProductOptionId;

/**
 * 상품 옵션 write 어댑터. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
@Repository
@RequiredArgsConstructor
public class ProductOptionRepositoryImpl implements ProductOptionRepository {

    private final ProductOptionJpaRepository productOptionJpaRepository;

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
}
