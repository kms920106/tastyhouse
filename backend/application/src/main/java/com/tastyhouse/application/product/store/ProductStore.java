package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductStatePort;

public class ProductStore implements ProductRepository {
    private final ProductStatePort productStatePort;

    public ProductStore(ProductStatePort productStatePort) {
        this.productStatePort = productStatePort;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return productStatePort.findById(id.value()).map(ProductStateMapper::toDomain);
    }

    @Override
    public Optional<Product> findByIdIncludingDeleted(ProductId id) {
        return productStatePort.findByIdIncludingDeleted(id.value()).map(ProductStateMapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        return ProductStateMapper.toDomain(productStatePort.save(ProductStateMapper.toState(product)));
    }

    @Override
    public List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productStatePort.findAllByShopIdAndIdIn(shopId.value(), ids.stream().map(ProductId::value).toList())
            .stream()
            .map(ProductStateMapper::toDomain)
            .toList();
    }

    @Override
    public long countVisibleByShopId(ShopId shopId) {
        return productStatePort.countVisibleByShopId(shopId.value());
    }

    @Override
    public long countVisibleRepresentativeByShopId(ShopId shopId) {
        return productStatePort.countVisibleRepresentativeByShopId(shopId.value());
    }

    @Override
    public long countRepresentativeByShopId(ShopId shopId) {
        return productStatePort.countRepresentativeByShopId(shopId.value());
    }

    @Override
    public List<Product> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
        return productStatePort.findAllSoldOutExpiredBefore(baseTime).stream()
            .map(ProductStateMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByShopIdAndName(ShopId shopId, String name) {
        return productStatePort.existsByShopIdAndName(shopId.value(), name);
    }

    @Override
    public boolean existsByShopIdAndNameAndIdNot(ShopId shopId, String name, ProductId excludedId) {
        return productStatePort.existsByShopIdAndNameAndIdNot(shopId.value(), name, excludedId.value());
    }

    @Override
    public List<Product> findAllByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId) {
        return productStatePort.findAllByShopIdAndCategoryId(
                shopId.value(), productCategoryId == null ? null : productCategoryId.value())
            .stream()
            .map(ProductStateMapper::toDomain)
            .toList();
    }

    @Override
    public long countByCategoryId(ProductCategoryId productCategoryId) {
        return productStatePort.countByCategoryId(productCategoryId.value());
    }
}
