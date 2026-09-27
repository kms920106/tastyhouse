package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestState;
import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestStatePort;

@Repository
public class ProductVegetarianRequestStatePortImpl implements ProductVegetarianRequestStatePort {
    private final ProductVegetarianRequestJpaRepository productVegetarianRequestJpaRepository;

    public ProductVegetarianRequestStatePortImpl(
        ProductVegetarianRequestJpaRepository productVegetarianRequestJpaRepository
    ) {
        this.productVegetarianRequestJpaRepository = productVegetarianRequestJpaRepository;
    }

    @Override
    public ProductVegetarianRequestState save(ProductVegetarianRequestState state) {
        if (state.id() == null) {
            ProductVegetarianRequestJpaEntity saved =
                productVegetarianRequestJpaRepository.save(ProductVegetarianRequestMapper.toEntity(state));
            return ProductVegetarianRequestMapper.toState(saved);
        }

        ProductVegetarianRequestJpaEntity entity = productVegetarianRequestJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴 채식 설정 요청입니다: " + state.id()));
        ProductVegetarianRequestMapper.applyChanges(entity, state);
        return ProductVegetarianRequestMapper.toState(entity);
    }

    @Override
    public Optional<ProductVegetarianRequestState> findById(Long id) {
        return productVegetarianRequestJpaRepository.findById(id)
            .map(ProductVegetarianRequestMapper::toState);
    }

    @Override
    public List<ProductVegetarianRequestState> findAllByProductId(Long productId) {
        return productVegetarianRequestJpaRepository.findAllByProductId(productId).stream()
            .map(ProductVegetarianRequestMapper::toState)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndStatus(Long productId, String status) {
        return productVegetarianRequestJpaRepository.existsByProductIdAndStatus(productId, status);
    }
}
