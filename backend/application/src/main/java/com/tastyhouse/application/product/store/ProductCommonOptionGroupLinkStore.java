package com.tastyhouse.application.product.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupLinkStatePort;
import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public class ProductCommonOptionGroupLinkStore implements ProductCommonOptionGroupLinkRepository {
    private final ProductCommonOptionGroupLinkStatePort productCommonOptionGroupLinkStatePort;

    public ProductCommonOptionGroupLinkStore(ProductCommonOptionGroupLinkStatePort productCommonOptionGroupLinkStatePort) {
        this.productCommonOptionGroupLinkStatePort = productCommonOptionGroupLinkStatePort;
    }

    @Override
    public ProductCommonOptionGroupLink save(ProductCommonOptionGroupLink link) {
        return ProductCommonOptionGroupLinkStateMapper.toDomain(productCommonOptionGroupLinkStatePort.save(ProductCommonOptionGroupLinkStateMapper.toState(link)));
    }

    @Override
    public Optional<ProductCommonOptionGroupLink> findByProductIdAndOptionGroupId(
        ProductId productId,
        ProductOptionGroupId optionGroupId
    ) {
        return productCommonOptionGroupLinkStatePort
            .findByProductIdAndOptionGroupId(productId.value(), optionGroupId.value())
            .map(ProductCommonOptionGroupLinkStateMapper::toDomain);
    }

    @Override
    public List<ProductCommonOptionGroupLink> findAllByProductId(ProductId productId) {
        return productCommonOptionGroupLinkStatePort.findAllByProductId(productId.value()).stream()
            .map(ProductCommonOptionGroupLinkStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return productCommonOptionGroupLinkStatePort.findAllByOptionGroupId(optionGroupId.value()).stream()
            .map(ProductCommonOptionGroupLinkStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds) {
        if (optionGroupIds.isEmpty()) {
            return List.of();
        }
        return productCommonOptionGroupLinkStatePort.findAllByOptionGroupIdIn(optionGroupIds.stream().map(ProductOptionGroupId::value).toList())
            .stream()
            .map(ProductCommonOptionGroupLinkStateMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId) {
        return productCommonOptionGroupLinkStatePort.existsByProductIdAndOptionGroupId(productId.value(), optionGroupId.value());
    }

    @Override
    public void delete(ProductCommonOptionGroupLink link) {
        productCommonOptionGroupLinkStatePort.deleteById(link.getId());
    }
}
