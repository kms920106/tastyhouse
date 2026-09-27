package com.tastyhouse.application.product.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductPriceStatePort;

public class ProductPriceStore implements ProductPriceRepository {
    private final ProductPriceStatePort productPriceStatePort;

    public ProductPriceStore(ProductPriceStatePort productPriceStatePort) {
        this.productPriceStatePort = productPriceStatePort;
    }

    @Override
    public ProductPrice save(ProductPrice productPrice) {
        return ProductPriceStateMapper.toDomain(productPriceStatePort.save(ProductPriceStateMapper.toState(productPrice)));
    }

    @Override
    public Optional<ProductPrice> findById(ProductPriceId id) {
        return productPriceStatePort.findById(id.value()).map(ProductPriceStateMapper::toDomain);
    }

    @Override
    public List<ProductPrice> findAllByProductId(ProductId productId) {
        return productPriceStatePort.findAllByProductId(productId.value()).stream()
            .map(ProductPriceStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductPrice> findAllByShopId(ShopId shopId) {
        return productPriceStatePort.findAllByShopId(shopId.value()).stream()
            .map(ProductPriceStateMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteAllByIdIn(List<ProductPriceId> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        productPriceStatePort.deleteAllByIdIn(ids.stream().map(ProductPriceId::value).toList());
    }
}
