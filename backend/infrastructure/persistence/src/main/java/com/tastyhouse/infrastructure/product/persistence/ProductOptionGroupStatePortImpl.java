package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductOptionGroupState;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupStatePort;

@Repository
public class ProductOptionGroupStatePortImpl implements ProductOptionGroupStatePort {
    private final ProductOptionGroupJpaRepository productOptionGroupJpaRepository;

    public ProductOptionGroupStatePortImpl(ProductOptionGroupJpaRepository productOptionGroupJpaRepository) {
        this.productOptionGroupJpaRepository = productOptionGroupJpaRepository;
    }

    @Override
    public Optional<ProductOptionGroupState> findById(Long id) {
        return productOptionGroupJpaRepository.findById(id).map(ProductOptionGroupMapper::toState);
    }

    @Override
    public ProductOptionGroupState save(ProductOptionGroupState state) {
        if (state.id() == null) {
            ProductOptionGroupJpaEntity saved =
                productOptionGroupJpaRepository.save(ProductOptionGroupMapper.toEntity(state));
            return ProductOptionGroupMapper.toState(saved);
        }

        ProductOptionGroupJpaEntity jpaEntity = productOptionGroupJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 옵션 그룹입니다: " + state.id()));
        ProductOptionGroupMapper.applyChanges(jpaEntity, state);
        return ProductOptionGroupMapper.toState(jpaEntity);
    }

    @Override
    public List<ProductOptionGroupState> findAllByIdIn(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productOptionGroupJpaRepository.findAllByIdIn(ids).stream()
            .map(ProductOptionGroupMapper::toState)
            .toList();
    }
}
