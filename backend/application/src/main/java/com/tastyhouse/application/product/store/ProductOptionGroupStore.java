package com.tastyhouse.application.product.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupStatePort;

public class ProductOptionGroupStore implements ProductOptionGroupRepository {
    private final ProductOptionGroupStatePort productOptionGroupStatePort;

    public ProductOptionGroupStore(ProductOptionGroupStatePort productOptionGroupStatePort) {
        this.productOptionGroupStatePort = productOptionGroupStatePort;
    }

    @Override
    public Optional<ProductOptionGroup> findById(ProductOptionGroupId id) {
        return productOptionGroupStatePort.findById(id.value()).map(ProductOptionGroupStateMapper::toDomain);
    }

    @Override
    public ProductOptionGroup save(ProductOptionGroup productOptionGroup) {
        return ProductOptionGroupStateMapper.toDomain(
            productOptionGroupStatePort.save(ProductOptionGroupStateMapper.toState(productOptionGroup)));
    }

    @Override
    public List<ProductOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productOptionGroupStatePort.findAllByIdIn(ids.stream().map(ProductOptionGroupId::value).toList())
            .stream()
            .map(ProductOptionGroupStateMapper::toDomain)
            .toList();
    }
}
