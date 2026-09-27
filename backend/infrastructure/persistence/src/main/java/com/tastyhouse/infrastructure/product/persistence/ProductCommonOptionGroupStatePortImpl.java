package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupState;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupStatePort;

@Repository
public class ProductCommonOptionGroupStatePortImpl implements ProductCommonOptionGroupStatePort {
    private final ProductCommonOptionGroupJpaRepository productCommonOptionGroupJpaRepository;

    public ProductCommonOptionGroupStatePortImpl(ProductCommonOptionGroupJpaRepository productCommonOptionGroupJpaRepository) {
        this.productCommonOptionGroupJpaRepository = productCommonOptionGroupJpaRepository;
    }

    @Override
    public ProductCommonOptionGroupState save(ProductCommonOptionGroupState state) {
        if (state.id() == null) {
            ProductCommonOptionGroupJpaEntity saved =
                productCommonOptionGroupJpaRepository.save(ProductCommonOptionGroupMapper.toEntity(state));
            return ProductCommonOptionGroupMapper.toState(saved);
        }

        ProductCommonOptionGroupJpaEntity jpaEntity = productCommonOptionGroupJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 공통 옵션 그룹입니다: " + state.id()));
        ProductCommonOptionGroupMapper.applyChanges(jpaEntity, state);
        return ProductCommonOptionGroupMapper.toState(jpaEntity);
    }

    @Override
    public List<ProductCommonOptionGroupState> findAllByIdIn(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productCommonOptionGroupJpaRepository.findAllByIdIn(ids).stream()
            .map(ProductCommonOptionGroupMapper::toState)
            .toList();
    }
}
