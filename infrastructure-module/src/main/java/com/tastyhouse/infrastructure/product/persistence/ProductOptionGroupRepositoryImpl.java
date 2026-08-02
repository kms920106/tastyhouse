package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.domain.product.domain.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.domain.vo.ProductOptionGroupId;

/**
 * 상품 옵션 그룹 write 어댑터. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
@Repository
public class ProductOptionGroupRepositoryImpl implements ProductOptionGroupRepository {

    private final ProductOptionGroupJpaRepository productOptionGroupJpaRepository;

    public ProductOptionGroupRepositoryImpl(ProductOptionGroupJpaRepository productOptionGroupJpaRepository) {
        this.productOptionGroupJpaRepository = productOptionGroupJpaRepository;
    }

    @Override
    public Optional<ProductOptionGroup> findById(ProductOptionGroupId id) {
        return productOptionGroupJpaRepository.findById(id.value()).map(ProductOptionGroupMapper::toDomain);
    }

    @Override
    public ProductOptionGroup save(ProductOptionGroup entity) {
        if (entity.getId() == null) {
            ProductOptionGroupJpaEntity saved =
                productOptionGroupJpaRepository.save(ProductOptionGroupMapper.toEntity(entity));
            return ProductOptionGroupMapper.toDomain(saved);
        }

        ProductOptionGroupJpaEntity jpaEntity = productOptionGroupJpaRepository.findById(entity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 옵션 그룹입니다: " + entity.getId()));
        ProductOptionGroupMapper.applyChanges(jpaEntity, entity);
        return ProductOptionGroupMapper.toDomain(jpaEntity);
    }
}
