package com.tastyhouse.infrastructure.product.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.domain.model.ProductCommonOption;
import com.tastyhouse.core.domain.product.domain.repository.ProductCommonOptionRepository;

/**
 * 상품 공통 옵션 write 어댑터. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
@Repository
@RequiredArgsConstructor
public class ProductCommonOptionRepositoryImpl implements ProductCommonOptionRepository {

    private final ProductCommonOptionJpaRepository productCommonOptionJpaRepository;

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
}
