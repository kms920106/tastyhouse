package com.tastyhouse.application.product.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.service.ProductShopLinkSpec;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductCategoryPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductShopLinkPersistencePort;

@Service
public class ProductShopLinkService {

    private final ProductPersistencePort productPersistencePort;
    private final ProductShopLinkPersistencePort productShopLinkPersistencePort;
    private final ProductCategoryPersistencePort productCategoryPersistencePort;

    public ProductShopLinkService(
        ProductPersistencePort productPersistencePort,
        ProductShopLinkPersistencePort productShopLinkPersistencePort,
        ProductCategoryPersistencePort productCategoryPersistencePort
    ) {
        this.productPersistencePort = productPersistencePort;
        this.productShopLinkPersistencePort = productShopLinkPersistencePort;
        this.productCategoryPersistencePort = productCategoryPersistencePort;
    }

    public void replaceLinks(
        ProductId productId,
        List<ProductShopLinkSpec> specs,
        Set<Long> ownedShopIds
    ) {
        Product product = loadProduct(productId);

        if (specs == null || specs.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_LAST_CANNOT_UNLINK);
        }

        Map<Long, ProductShopLinkSpec> requested = toDistinctSpecsByShopId(specs);
        for (ProductShopLinkSpec spec : requested.values()) {
            validateOwned(spec.shopId(), ownedShopIds);
            validateCategory(ShopId.of(spec.shopId()), spec.productCategoryId());
        }

        Map<Long, ProductShopLink> existing = new LinkedHashMap<>();
        for (ProductShopLink link : productShopLinkPersistencePort.findAllByProductId(productId)) {
            existing.put(link.getShopId().value(), link);
        }

        for (Map.Entry<Long, ProductShopLink> entry : existing.entrySet()) {
            if (requested.containsKey(entry.getKey())) {
                continue;
            }
            validateShopKeepsVisibleProduct(product, ShopId.of(entry.getKey()));
            productShopLinkPersistencePort.delete(entry.getValue());
        }

        for (ProductShopLinkSpec spec : requested.values()) {
            ProductShopLink link = existing.get(spec.shopId());
            ProductCategoryId categoryId = ProductCategoryId.of(spec.productCategoryId());
            if (link == null) {
                ShopId targetShopId = ShopId.of(spec.shopId());
                productShopLinkPersistencePort.save(
                    ProductShopLink.of(productId, targetShopId, categoryId, nextSort(targetShopId))
                );
                continue;
            }

            link.relocate(categoryId, link.getSort());
            productShopLinkPersistencePort.save(link);
        }
    }

    public void linkToShop(ProductId productId, ShopId targetShopId, Long productCategoryId) {
        loadProduct(productId);
        validateCategory(targetShopId, productCategoryId);

        if (productShopLinkPersistencePort.existsByProductIdAndShopId(productId, targetShopId)) {
            throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_ALREADY_LINKED);
        }

        productShopLinkPersistencePort.save(
            ProductShopLink.of(productId, targetShopId, ProductCategoryId.of(productCategoryId), nextSort(targetShopId))
        );
    }

    public void unlinkFromShop(ProductId productId, ShopId targetShopId) {
        Product product = loadProduct(productId);

        ProductShopLink link = productShopLinkPersistencePort.findByProductIdAndShopId(productId, targetShopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_SHOP_LINK_NOT_FOUND));

        if (productShopLinkPersistencePort.countByProductId(productId) <= 1) {
            throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_LAST_CANNOT_UNLINK);
        }

        validateShopKeepsVisibleProduct(product, targetShopId);
        productShopLinkPersistencePort.delete(link);
    }

    public void createInitialLinks(
        ProductId productId,
        List<ProductShopLinkSpec> specs,
        Set<Long> ownedShopIds
    ) {
        if (specs == null || specs.isEmpty()) {
            return;
        }

        Map<Long, ProductShopLinkSpec> requested = toDistinctSpecsByShopId(specs);
        for (ProductShopLinkSpec spec : requested.values()) {
            validateOwned(spec.shopId(), ownedShopIds);
            ShopId targetShopId = ShopId.of(spec.shopId());
            validateCategory(targetShopId, spec.productCategoryId());

            if (productShopLinkPersistencePort.existsByProductIdAndShopId(productId, targetShopId)) {
                continue;
            }
            productShopLinkPersistencePort.save(ProductShopLink.of(
                productId, targetShopId, ProductCategoryId.of(spec.productCategoryId()), nextSort(targetShopId)
            ));
        }
    }

    private Product loadProduct(ProductId productId) {
        return productPersistencePort.findById(productId)
            .filter(found -> !found.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Map<Long, ProductShopLinkSpec> toDistinctSpecsByShopId(List<ProductShopLinkSpec> specs) {
        Map<Long, ProductShopLinkSpec> distinct = new LinkedHashMap<>();
        for (ProductShopLinkSpec spec : specs) {
            if (spec.shopId() == null) {
                throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_NOT_OWNED);
            }
            distinct.put(spec.shopId(), spec);
        }
        return distinct;
    }

    private void validateOwned(Long shopId, Set<Long> ownedShopIds) {
        if (ownedShopIds == null || !ownedShopIds.contains(shopId)) {
            throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_NOT_OWNED);
        }
    }

    private void validateCategory(ShopId shopId, Long productCategoryId) {
        if (productCategoryId == null) {
            throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_CATEGORY_REQUIRED);
        }

        ProductCategory category = productCategoryPersistencePort.findById(ProductCategoryId.of(productCategoryId))
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_CATEGORY_MISMATCH));

        if (!shopId.equals(category.getShopId())) {
            throw new BusinessException(ErrorCode.PRODUCT_SHOP_LINK_CATEGORY_MISMATCH);
        }
    }

    private void validateShopKeepsVisibleProduct(Product product, ShopId shopId) {
        if (!product.isVisible()) {
            return;
        }
        if (productPersistencePort.countVisibleByShopId(shopId) <= 1) {
            throw new BusinessException(ErrorCode.PRODUCT_LAST_VISIBLE_CANNOT_HIDE);
        }
    }

    private Integer nextSort(ShopId shopId) {
        List<ProductShopLink> links = productShopLinkPersistencePort.findAllByShopId(shopId);
        int max = -1;
        for (ProductShopLink link : links) {
            if (link.getSort() != null && link.getSort() > max) {
                max = link.getSort();
            }
        }
        return max + 1;
    }
}
