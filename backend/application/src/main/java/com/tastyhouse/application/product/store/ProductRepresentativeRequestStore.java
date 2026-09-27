package com.tastyhouse.application.product.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.product.port.out.write.ProductRepresentativeRequestStatePort;
import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductRepresentativeRequestStore implements ProductRepresentativeRequestRepository {
    private final ProductRepresentativeRequestStatePort productRepresentativeRequestStatePort;

    public ProductRepresentativeRequestStore(ProductRepresentativeRequestStatePort productRepresentativeRequestStatePort) {
        this.productRepresentativeRequestStatePort = productRepresentativeRequestStatePort;
    }

    @Override
    public ProductRepresentativeRequest save(ProductRepresentativeRequest request) {
        return ProductRepresentativeRequestStateMapper.toDomain(
            productRepresentativeRequestStatePort.save(ProductRepresentativeRequestStateMapper.toState(request)));
    }

    @Override
    public Optional<ProductRepresentativeRequest> findById(ProductRepresentativeRequestId id) {
        return productRepresentativeRequestStatePort.findById(id.value())
            .map(ProductRepresentativeRequestStateMapper::toDomain);
    }

    @Override
    public List<ProductRepresentativeRequest> findAllByProductId(ProductId productId) {
        return productRepresentativeRequestStatePort.findAllByProductId(productId.value()).stream()
            .map(ProductRepresentativeRequestStateMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status) {
        return productRepresentativeRequestStatePort.existsByProductIdAndStatus(
            productId.value(), status == null ? null : status.name());
    }

    @Override
    public long countByShopIdAndStatus(ShopId shopId, ApprovalStatus status) {
        return productRepresentativeRequestStatePort.countByShopIdAndStatus(
            shopId.value(), status == null ? null : status.name());
    }
}
