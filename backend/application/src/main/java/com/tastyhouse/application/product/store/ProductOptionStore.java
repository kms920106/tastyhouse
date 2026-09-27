package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.application.product.port.out.write.ProductOptionStatePort;

public class ProductOptionStore implements ProductOptionRepository {
    private final ProductOptionStatePort productOptionStatePort;

    public ProductOptionStore(ProductOptionStatePort productOptionStatePort) {
        this.productOptionStatePort = productOptionStatePort;
    }

    @Override
    public Optional<ProductOption> findById(ProductOptionId id) {
        return productOptionStatePort.findById(id.value()).map(ProductOptionStateMapper::toDomain);
    }

    @Override
    public ProductOption save(ProductOption productOption) {
        return ProductOptionStateMapper.toDomain(productOptionStatePort.save(ProductOptionStateMapper.toState(productOption)));
    }

    @Override
    public List<ProductOption> findAllByIdIn(List<ProductOptionId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productOptionStatePort.findAllByIdIn(ids.stream().map(ProductOptionId::value).toList()).stream()
            .map(ProductOptionStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return productOptionStatePort.findAllByOptionGroupId(optionGroupId.value()).stream()
            .map(ProductOptionStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
        return productOptionStatePort.findAllSoldOutExpiredBefore(baseTime).stream()
            .map(ProductOptionStateMapper::toDomain)
            .toList();
    }
}
