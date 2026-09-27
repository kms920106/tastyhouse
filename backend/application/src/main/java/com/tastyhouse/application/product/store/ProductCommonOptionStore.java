package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.product.port.out.write.ProductCommonOptionStatePort;
import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public class ProductCommonOptionStore implements ProductCommonOptionRepository {
    private final ProductCommonOptionStatePort productCommonOptionStatePort;

    public ProductCommonOptionStore(ProductCommonOptionStatePort productCommonOptionStatePort) {
        this.productCommonOptionStatePort = productCommonOptionStatePort;
    }

    @Override
    public Optional<ProductCommonOption> findById(ProductCommonOptionId id) {
        return productCommonOptionStatePort.findById(id.value()).map(ProductCommonOptionStateMapper::toDomain);
    }

    @Override
    public ProductCommonOption save(ProductCommonOption productCommonOption) {
        return ProductCommonOptionStateMapper.toDomain(
            productCommonOptionStatePort.save(ProductCommonOptionStateMapper.toState(productCommonOption)));
    }

    @Override
    public List<ProductCommonOption> findAllByIdIn(List<ProductCommonOptionId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productCommonOptionStatePort.findAllByIdIn(ids.stream().map(ProductCommonOptionId::value).toList())
            .stream()
            .map(ProductCommonOptionStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return productCommonOptionStatePort.findAllByOptionGroupId(optionGroupId.value()).stream()
            .map(ProductCommonOptionStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
        return productCommonOptionStatePort.findAllSoldOutExpiredBefore(baseTime).stream()
            .map(ProductCommonOptionStateMapper::toDomain)
            .toList();
    }
}
