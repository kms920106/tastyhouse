package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestState;
import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestStatePort;

@Repository
public class ProductImageChangeRequestStatePortImpl implements ProductImageChangeRequestStatePort {
    private final ProductImageChangeRequestJpaRepository productImageChangeRequestJpaRepository;

    public ProductImageChangeRequestStatePortImpl(
        ProductImageChangeRequestJpaRepository productImageChangeRequestJpaRepository
    ) {
        this.productImageChangeRequestJpaRepository = productImageChangeRequestJpaRepository;
    }

    @Override
    public ProductImageChangeRequestState save(ProductImageChangeRequestState state) {
        if (state.id() == null) {
            ProductImageChangeRequestJpaEntity saved =
                productImageChangeRequestJpaRepository.save(ProductImageChangeRequestMapper.toEntity(state));
            return ProductImageChangeRequestMapper.toState(saved);
        }

        ProductImageChangeRequestJpaEntity entity = productImageChangeRequestJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴 이미지 변경 요청입니다: " + state.id()));
        ProductImageChangeRequestMapper.applyChanges(entity, state);
        return ProductImageChangeRequestMapper.toState(entity);
    }

    @Override
    public Optional<ProductImageChangeRequestState> findById(Long id) {
        return productImageChangeRequestJpaRepository.findById(id)
            .map(ProductImageChangeRequestMapper::toState);
    }

    @Override
    public List<ProductImageChangeRequestState> findAllByProductId(Long productId) {
        return productImageChangeRequestJpaRepository.findAllByProductId(productId).stream()
            .map(ProductImageChangeRequestMapper::toState)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndStatus(Long productId, String status) {
        return productImageChangeRequestJpaRepository.existsByProductIdAndStatus(productId, status);
    }
}
