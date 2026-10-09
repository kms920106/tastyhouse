package com.tastyhouse.application.product.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductRepresentativeRequestPersistencePort {

    ProductRepresentativeRequest save(ProductRepresentativeRequest request);

    Optional<ProductRepresentativeRequest> findById(ProductRepresentativeRequestId id);

    boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status);

    long countByShopIdAndStatus(ShopId shopId, ApprovalStatus status);
}
