package com.tastyhouse.application.product.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestStatePort;

public class ProductVegetarianRequestStore implements ProductVegetarianRequestRepository {
    private final ProductVegetarianRequestStatePort productVegetarianRequestStatePort;

    public ProductVegetarianRequestStore(ProductVegetarianRequestStatePort productVegetarianRequestStatePort) {
        this.productVegetarianRequestStatePort = productVegetarianRequestStatePort;
    }

    @Override
    public ProductVegetarianRequest save(ProductVegetarianRequest request) {
        return ProductVegetarianRequestStateMapper.toDomain(
            productVegetarianRequestStatePort.save(ProductVegetarianRequestStateMapper.toState(request)));
    }

    @Override
    public Optional<ProductVegetarianRequest> findById(ProductVegetarianRequestId id) {
        return productVegetarianRequestStatePort.findById(id.value())
            .map(ProductVegetarianRequestStateMapper::toDomain);
    }

    @Override
    public List<ProductVegetarianRequest> findAllByProductId(ProductId productId) {
        return productVegetarianRequestStatePort.findAllByProductId(productId.value()).stream()
            .map(ProductVegetarianRequestStateMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status) {
        return productVegetarianRequestStatePort.existsByProductIdAndStatus(
            productId.value(), status == null ? null : status.name());
    }
}
