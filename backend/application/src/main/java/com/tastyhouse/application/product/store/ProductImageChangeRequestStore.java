package com.tastyhouse.application.product.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestStatePort;
import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public class ProductImageChangeRequestStore implements ProductImageChangeRequestRepository {
    private final ProductImageChangeRequestStatePort productImageChangeRequestStatePort;

    public ProductImageChangeRequestStore(ProductImageChangeRequestStatePort productImageChangeRequestStatePort) {
        this.productImageChangeRequestStatePort = productImageChangeRequestStatePort;
    }

    @Override
    public ProductImageChangeRequest save(ProductImageChangeRequest request) {
        return ProductImageChangeRequestStateMapper.toDomain(
            productImageChangeRequestStatePort.save(ProductImageChangeRequestStateMapper.toState(request)));
    }

    @Override
    public Optional<ProductImageChangeRequest> findById(ProductImageChangeRequestId id) {
        return productImageChangeRequestStatePort.findById(id.value())
            .map(ProductImageChangeRequestStateMapper::toDomain);
    }

    @Override
    public List<ProductImageChangeRequest> findAllByProductId(ProductId productId) {
        return productImageChangeRequestStatePort.findAllByProductId(productId.value()).stream()
            .map(ProductImageChangeRequestStateMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status) {
        return productImageChangeRequestStatePort.existsByProductIdAndStatus(
            productId.value(), status == null ? null : status.name());
    }
}
