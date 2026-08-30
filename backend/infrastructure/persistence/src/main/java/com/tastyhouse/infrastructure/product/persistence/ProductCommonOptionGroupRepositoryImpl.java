package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.repository.ProductCommonOptionGroupRepository;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

/**
 * 상품 공통 옵션 그룹 write 어댑터. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
@Repository
public class ProductCommonOptionGroupRepositoryImpl implements ProductCommonOptionGroupRepository {

    private final ProductCommonOptionGroupJpaRepository productCommonOptionGroupJpaRepository;

    public ProductCommonOptionGroupRepositoryImpl(ProductCommonOptionGroupJpaRepository productCommonOptionGroupJpaRepository) {
        this.productCommonOptionGroupJpaRepository = productCommonOptionGroupJpaRepository;
    }

    @Override
    public ProductCommonOptionGroup save(ProductCommonOptionGroup entity) {
        if (entity.getId() == null) {
            ProductCommonOptionGroupJpaEntity saved =
                productCommonOptionGroupJpaRepository.save(ProductCommonOptionGroupMapper.toEntity(entity));
            return ProductCommonOptionGroupMapper.toDomain(saved);
        }

        ProductCommonOptionGroupJpaEntity jpaEntity = productCommonOptionGroupJpaRepository.findById(entity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 공통 옵션 그룹입니다: " + entity.getId()));
        ProductCommonOptionGroupMapper.applyChanges(jpaEntity, entity);
        return ProductCommonOptionGroupMapper.toDomain(jpaEntity);
    }

    @Override
    public List<ProductCommonOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        List<Long> rawIds = ids.stream().map(ProductOptionGroupId::value).toList();
        return productCommonOptionGroupJpaRepository.findAllByIdIn(rawIds).stream()
            .map(ProductCommonOptionGroupMapper::toDomain)
            .toList();
    }
}
