package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.repository.ProductImageChangeRequestRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

@Repository
public class ProductImageChangeRequestRepositoryImpl implements ProductImageChangeRequestRepository {
    private final ProductImageChangeRequestJpaRepository productImageChangeRequestJpaRepository;

    public ProductImageChangeRequestRepositoryImpl(
        ProductImageChangeRequestJpaRepository productImageChangeRequestJpaRepository
    ) {
        this.productImageChangeRequestJpaRepository = productImageChangeRequestJpaRepository;
    }

    @Override
    public ProductImageChangeRequest save(ProductImageChangeRequest request) {
        if (request.getId() == null) {
            ProductImageChangeRequestJpaEntity saved =
                productImageChangeRequestJpaRepository.save(ProductImageChangeRequestMapper.toEntity(request));
            return ProductImageChangeRequestMapper.toDomain(saved);
        }

        ProductImageChangeRequestJpaEntity entity = productImageChangeRequestJpaRepository.findById(request.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴 이미지 변경 요청입니다: " + request.getId()));
        ProductImageChangeRequestMapper.applyChanges(entity, request);
        return ProductImageChangeRequestMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductImageChangeRequest> findById(ProductImageChangeRequestId id) {
        return productImageChangeRequestJpaRepository.findById(id.value())
            .map(ProductImageChangeRequestMapper::toDomain);
    }

    @Override
    public List<ProductImageChangeRequest> findAllByProductId(ProductId productId) {
        return productImageChangeRequestJpaRepository.findAllByProductId(productId.value()).stream()
            .map(ProductImageChangeRequestMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status) {
        return productImageChangeRequestJpaRepository.existsByProductIdAndStatus(productId.value(), status);
    }
}
