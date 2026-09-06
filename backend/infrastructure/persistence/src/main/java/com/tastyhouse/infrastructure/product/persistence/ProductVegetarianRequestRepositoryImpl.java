package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.repository.ProductVegetarianRequestRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

@Repository
public class ProductVegetarianRequestRepositoryImpl implements ProductVegetarianRequestRepository {
    private final ProductVegetarianRequestJpaRepository productVegetarianRequestJpaRepository;

    public ProductVegetarianRequestRepositoryImpl(
        ProductVegetarianRequestJpaRepository productVegetarianRequestJpaRepository
    ) {
        this.productVegetarianRequestJpaRepository = productVegetarianRequestJpaRepository;
    }

    @Override
    public ProductVegetarianRequest save(ProductVegetarianRequest request) {
        if (request.getId() == null) {
            ProductVegetarianRequestJpaEntity saved =
                productVegetarianRequestJpaRepository.save(ProductVegetarianRequestMapper.toEntity(request));
            return ProductVegetarianRequestMapper.toDomain(saved);
        }

        ProductVegetarianRequestJpaEntity entity = productVegetarianRequestJpaRepository.findById(request.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴 채식 설정 요청입니다: " + request.getId()));
        ProductVegetarianRequestMapper.applyChanges(entity, request);
        return ProductVegetarianRequestMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductVegetarianRequest> findById(ProductVegetarianRequestId id) {
        return productVegetarianRequestJpaRepository.findById(id.value())
            .map(ProductVegetarianRequestMapper::toDomain);
    }

    @Override
    public List<ProductVegetarianRequest> findAllByProductId(ProductId productId) {
        return productVegetarianRequestJpaRepository.findAllByProductId(productId.value()).stream()
            .map(ProductVegetarianRequestMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status) {
        return productVegetarianRequestJpaRepository.existsByProductIdAndStatus(productId.value(), status);
    }
}
