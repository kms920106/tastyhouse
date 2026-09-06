package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.repository.ProductRepresentativeRequestRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ProductRepresentativeRequestRepositoryImpl implements ProductRepresentativeRequestRepository {
    private final ProductRepresentativeRequestJpaRepository productRepresentativeRequestJpaRepository;

    public ProductRepresentativeRequestRepositoryImpl(
        ProductRepresentativeRequestJpaRepository productRepresentativeRequestJpaRepository
    ) {
        this.productRepresentativeRequestJpaRepository = productRepresentativeRequestJpaRepository;
    }

    @Override
    public ProductRepresentativeRequest save(ProductRepresentativeRequest request) {
        if (request.getId() == null) {
            ProductRepresentativeRequestJpaEntity saved = productRepresentativeRequestJpaRepository
                .save(ProductRepresentativeRequestMapper.toEntity(request));
            return ProductRepresentativeRequestMapper.toDomain(saved);
        }

        ProductRepresentativeRequestJpaEntity entity = productRepresentativeRequestJpaRepository
            .findById(request.getId())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 사장님 추천 지정 요청입니다: " + request.getId()));
        ProductRepresentativeRequestMapper.applyChanges(entity, request);
        return ProductRepresentativeRequestMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductRepresentativeRequest> findById(ProductRepresentativeRequestId id) {
        return productRepresentativeRequestJpaRepository.findById(id.value())
            .map(ProductRepresentativeRequestMapper::toDomain);
    }

    @Override
    public List<ProductRepresentativeRequest> findAllByProductId(ProductId productId) {
        return productRepresentativeRequestJpaRepository.findAllByProductId(productId.value()).stream()
            .map(ProductRepresentativeRequestMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status) {
        return productRepresentativeRequestJpaRepository.existsByProductIdAndStatus(productId.value(), status);
    }

    @Override
    public long countByShopIdAndStatus(ShopId shopId, ApprovalStatus status) {
        return productRepresentativeRequestJpaRepository.countByShopIdAndStatus(shopId.value(), status);
    }
}
