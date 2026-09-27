package com.tastyhouse.application.product.store;

import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductBbqStatePort;

public class ProductBbqStore implements ProductBbqRepository {
    private final ProductBbqStatePort productBbqStatePort;

    public ProductBbqStore(ProductBbqStatePort productBbqStatePort) {
        this.productBbqStatePort = productBbqStatePort;
    }

    @Override
    public Optional<ProductBbq> findByProductId(ProductId productId) {
        return productBbqStatePort.findByProductId(productId.value()).map(ProductBbqStateMapper::toDomain);
    }

    @Override
    public ProductBbq save(ProductBbq productBbq) {
        return ProductBbqStateMapper.toDomain(productBbqStatePort.save(ProductBbqStateMapper.toState(productBbq)));
    }
}
