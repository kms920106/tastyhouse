package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupPersistencePort;

@Repository
class ProductOptionGroupPersistenceAdapter implements ProductOptionGroupPersistencePort {

    private final ProductOptionGroupJpaRepository productOptionGroupJpaRepository;

    public ProductOptionGroupPersistenceAdapter(ProductOptionGroupJpaRepository productOptionGroupJpaRepository) {
        this.productOptionGroupJpaRepository = productOptionGroupJpaRepository;
    }

    @Override
    public Optional<ProductOptionGroup> findById(ProductOptionGroupId id) {
        return productOptionGroupJpaRepository.findById(id.value()).map(ProductOptionGroupMapper::toDomain);
    }

    @Override
    public ProductOptionGroup save(ProductOptionGroup productOptionGroup) {
        if (productOptionGroup.getId() == null) {
            ProductOptionGroupJpaEntity saved =
                productOptionGroupJpaRepository.save(ProductOptionGroupMapper.toEntity(productOptionGroup));
            return ProductOptionGroupMapper.toDomain(saved);
        }

        ProductOptionGroupJpaEntity jpaEntity = productOptionGroupJpaRepository.findById(productOptionGroup.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 옵션 그룹입니다: " + productOptionGroup.getId()));
        ProductOptionGroupMapper.applyChanges(jpaEntity, productOptionGroup);
        return ProductOptionGroupMapper.toDomain(jpaEntity);
    }

    @Override
    public List<ProductOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productOptionGroupJpaRepository.findAllByIdIn(ids.stream().map(ProductOptionGroupId::value).toList())
            .stream()
            .map(ProductOptionGroupMapper::toDomain)
            .toList();
    }
}
