package com.tastyhouse.application.product.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.product.port.out.write.ProductShopLinkStatePort;
import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductShopLinkStore implements ProductShopLinkRepository {
    private final ProductShopLinkStatePort productShopLinkStatePort;

    public ProductShopLinkStore(ProductShopLinkStatePort productShopLinkStatePort) {
        this.productShopLinkStatePort = productShopLinkStatePort;
    }

    @Override
    public ProductShopLink save(ProductShopLink link) {
        return ProductShopLinkStateMapper.toDomain(productShopLinkStatePort.save(ProductShopLinkStateMapper.toState(link)));
    }

    @Override
    public Optional<ProductShopLink> findByProductIdAndShopId(ProductId productId, ShopId shopId) {
        return productShopLinkStatePort.findByProductIdAndShopId(productId.value(), shopId.value())
            .map(ProductShopLinkStateMapper::toDomain);
    }

    @Override
    public List<ProductShopLink> findAllByProductId(ProductId productId) {
        return productShopLinkStatePort.findAllByProductId(productId.value()).stream()
            .map(ProductShopLinkStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductShopLink> findAllByShopId(ShopId shopId) {
        return productShopLinkStatePort.findAllByShopId(shopId.value()).stream()
            .map(ProductShopLinkStateMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndShopId(ProductId productId, ShopId shopId) {
        return productShopLinkStatePort.existsByProductIdAndShopId(productId.value(), shopId.value());
    }

    @Override
    public long countByProductId(ProductId productId) {
        return productShopLinkStatePort.countByProductId(productId.value());
    }

    @Override
    public void delete(ProductShopLink link) {
        if (link.getId() == null) {
            return;
        }
        productShopLinkStatePort.deleteById(link.getId());
    }
}
