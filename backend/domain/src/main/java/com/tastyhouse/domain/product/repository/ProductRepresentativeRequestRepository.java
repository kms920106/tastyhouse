package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductRepresentativeRequestRepository {
    ProductRepresentativeRequest save(ProductRepresentativeRequest request);

    Optional<ProductRepresentativeRequest> findById(ProductRepresentativeRequestId id);

    List<ProductRepresentativeRequest> findAllByProductId(ProductId productId);

    boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status);

    long countByShopIdAndStatus(ShopId shopId, ApprovalStatus status);
}
