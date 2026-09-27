package com.tastyhouse.application.product.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkStatePort;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public class ProductOptionGroupLinkStore implements ProductOptionGroupLinkRepository {
    private final ProductOptionGroupLinkStatePort productOptionGroupLinkStatePort;

    public ProductOptionGroupLinkStore(ProductOptionGroupLinkStatePort productOptionGroupLinkStatePort) {
        this.productOptionGroupLinkStatePort = productOptionGroupLinkStatePort;
    }

    @Override
    public ProductOptionGroupLink save(ProductOptionGroupLink link) {
        return ProductOptionGroupLinkStateMapper.toDomain(productOptionGroupLinkStatePort.save(ProductOptionGroupLinkStateMapper.toState(link)));
    }

    @Override
    public Optional<ProductOptionGroupLink> findByProductIdAndOptionGroupId(
        ProductId productId,
        ProductOptionGroupId optionGroupId
    ) {
        return productOptionGroupLinkStatePort
            .findByProductIdAndOptionGroupId(productId.value(), optionGroupId.value())
            .map(ProductOptionGroupLinkStateMapper::toDomain);
    }

    @Override
    public List<ProductOptionGroupLink> findAllByProductId(ProductId productId) {
        return productOptionGroupLinkStatePort.findAllByProductId(productId.value()).stream()
            .map(ProductOptionGroupLinkStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return productOptionGroupLinkStatePort.findAllByOptionGroupId(optionGroupId.value()).stream()
            .map(ProductOptionGroupLinkStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds) {
        if (optionGroupIds.isEmpty()) {
            return List.of();
        }
        return productOptionGroupLinkStatePort.findAllByOptionGroupIdIn(optionGroupIds.stream().map(ProductOptionGroupId::value).toList())
            .stream()
            .map(ProductOptionGroupLinkStateMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId) {
        return productOptionGroupLinkStatePort.existsByProductIdAndOptionGroupId(productId.value(), optionGroupId.value());
    }

    @Override
    public void delete(ProductOptionGroupLink link) {
        productOptionGroupLinkStatePort.deleteById(link.getId());
    }
}
