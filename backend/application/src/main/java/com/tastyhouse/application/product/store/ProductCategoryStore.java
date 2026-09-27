package com.tastyhouse.application.product.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.product.port.out.write.ProductCategoryStatePort;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductCategoryStore implements ProductCategoryRepository {
    private final ProductCategoryStatePort productCategoryStatePort;

    public ProductCategoryStore(ProductCategoryStatePort productCategoryStatePort) {
        this.productCategoryStatePort = productCategoryStatePort;
    }

    @Override
    public Optional<ProductCategory> findById(ProductCategoryId id) {
        return productCategoryStatePort.findById(id.value()).map(ProductCategoryStateMapper::toDomain);
    }

    @Override
    public List<ProductCategory> findCategoriesByNameAndShopId(String name, ShopId shopId) {
        return productCategoryStatePort.findCategoriesByNameAndShopId(name, shopId.value()).stream()
            .map(ProductCategoryStateMapper::toDomain)
            .toList();
    }

    @Override
    public ProductCategory save(ProductCategory productCategory) {
        return ProductCategoryStateMapper.toDomain(
            productCategoryStatePort.save(ProductCategoryStateMapper.toState(productCategory)));
    }

    @Override
    public List<ProductCategory> findAllByShopId(ShopId shopId) {
        return productCategoryStatePort.findAllByShopId(shopId.value()).stream()
            .map(ProductCategoryStateMapper::toDomain)
            .toList();
    }

    @Override
    public void delete(ProductCategory productCategory) {
        productCategoryStatePort.deleteById(productCategory.getId());
    }
}
