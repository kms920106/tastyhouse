package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupPersistencePort;

@Repository
public class ProductCommonOptionGroupPersistenceAdapter implements ProductCommonOptionGroupPersistencePort {
    private final ProductCommonOptionGroupJpaRepository productCommonOptionGroupJpaRepository;

    public ProductCommonOptionGroupPersistenceAdapter(ProductCommonOptionGroupJpaRepository productCommonOptionGroupJpaRepository) {
        this.productCommonOptionGroupJpaRepository = productCommonOptionGroupJpaRepository;
    }

    @Override
    public ProductCommonOptionGroup save(ProductCommonOptionGroup productCommonOptionGroup) {
        if (productCommonOptionGroup.getId() == null) {
            ProductCommonOptionGroupJpaEntity saved =
                productCommonOptionGroupJpaRepository.save(ProductCommonOptionGroupMapper.toEntity(productCommonOptionGroup));
            return ProductCommonOptionGroupMapper.toDomain(saved);
        }

        ProductCommonOptionGroupJpaEntity jpaEntity = productCommonOptionGroupJpaRepository.findById(productCommonOptionGroup.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 공통 옵션 그룹입니다: " + productCommonOptionGroup.getId()));
        ProductCommonOptionGroupMapper.applyChanges(jpaEntity, productCommonOptionGroup);
        return ProductCommonOptionGroupMapper.toDomain(jpaEntity);
    }

    @Override
    public List<ProductCommonOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productCommonOptionGroupJpaRepository.findAllByIdIn(ids.stream().map(ProductOptionGroupId::value).toList())
            .stream()
            .map(ProductCommonOptionGroupMapper::toDomain)
            .toList();
    }
}
