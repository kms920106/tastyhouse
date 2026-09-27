package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductRepresentativeRequestState;
import com.tastyhouse.application.product.port.out.write.ProductRepresentativeRequestStatePort;

@Repository
public class ProductRepresentativeRequestStatePortImpl implements ProductRepresentativeRequestStatePort {
    private final ProductRepresentativeRequestJpaRepository productRepresentativeRequestJpaRepository;

    public ProductRepresentativeRequestStatePortImpl(
        ProductRepresentativeRequestJpaRepository productRepresentativeRequestJpaRepository
    ) {
        this.productRepresentativeRequestJpaRepository = productRepresentativeRequestJpaRepository;
    }

    @Override
    public ProductRepresentativeRequestState save(ProductRepresentativeRequestState state) {
        if (state.id() == null) {
            ProductRepresentativeRequestJpaEntity saved = productRepresentativeRequestJpaRepository
                .save(ProductRepresentativeRequestMapper.toEntity(state));
            return ProductRepresentativeRequestMapper.toState(saved);
        }

        ProductRepresentativeRequestJpaEntity entity = productRepresentativeRequestJpaRepository
            .findById(state.id())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 사장님 추천 지정 요청입니다: " + state.id()));
        ProductRepresentativeRequestMapper.applyChanges(entity, state);
        return ProductRepresentativeRequestMapper.toState(entity);
    }

    @Override
    public Optional<ProductRepresentativeRequestState> findById(Long id) {
        return productRepresentativeRequestJpaRepository.findById(id)
            .map(ProductRepresentativeRequestMapper::toState);
    }

    @Override
    public List<ProductRepresentativeRequestState> findAllByProductId(Long productId) {
        return productRepresentativeRequestJpaRepository.findAllByProductId(productId).stream()
            .map(ProductRepresentativeRequestMapper::toState)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndStatus(Long productId, String status) {
        return productRepresentativeRequestJpaRepository.existsByProductIdAndStatus(productId, status);
    }

    @Override
    public long countByShopIdAndStatus(Long shopId, String status) {
        return productRepresentativeRequestJpaRepository.countByShopIdAndStatus(shopId, status);
    }
}
