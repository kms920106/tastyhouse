package com.tastyhouse.application.product.store;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupStatePort;

public class ProductCommonOptionGroupStore implements ProductCommonOptionGroupRepository {
    private final ProductCommonOptionGroupStatePort productCommonOptionGroupStatePort;

    public ProductCommonOptionGroupStore(ProductCommonOptionGroupStatePort productCommonOptionGroupStatePort) {
        this.productCommonOptionGroupStatePort = productCommonOptionGroupStatePort;
    }

    @Override
    public ProductCommonOptionGroup save(ProductCommonOptionGroup productCommonOptionGroup) {
        return ProductCommonOptionGroupStateMapper.toDomain(
            productCommonOptionGroupStatePort.save(ProductCommonOptionGroupStateMapper.toState(productCommonOptionGroup)));
    }

    @Override
    public List<ProductCommonOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productCommonOptionGroupStatePort.findAllByIdIn(ids.stream().map(ProductOptionGroupId::value).toList())
            .stream()
            .map(ProductCommonOptionGroupStateMapper::toDomain)
            .toList();
    }
}
