package com.tastyhouse.application.product.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.model.ProductShopLinkSpec;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductCategoryLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductShopLinkLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductShopLinkSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class ProductShopLinkService {

    private final ProductLoadPort productLoadPort;
    private final ProductShopLinkLoadPort productShopLinkLoadPort;
    private final ProductShopLinkSavePort productShopLinkSavePort;
    private final ProductCategoryLoadPort productCategoryLoadPort;

    public ProductShopLinkService(
        ProductLoadPort productLoadPort,
        ProductShopLinkLoadPort productShopLinkLoadPort,
        ProductShopLinkSavePort productShopLinkSavePort,
        ProductCategoryLoadPort productCategoryLoadPort
    ) {
        this.productLoadPort = productLoadPort;
        this.productShopLinkLoadPort = productShopLinkLoadPort;
        this.productShopLinkSavePort = productShopLinkSavePort;
        this.productCategoryLoadPort = productCategoryLoadPort;
    }

    public void replaceLinks(
        ProductId productId,
        List<ProductShopLinkSpec> specs,
        Set<Long> ownedShopIds
    ) {
        Product product = loadProduct(productId);

        if (specs == null || specs.isEmpty()) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_SHOP_LINK_LAST_CANNOT_UNLINK);
        }

        Map<Long, ProductShopLinkSpec> requested = toDistinctSpecsByShopId(specs);
        for (ProductShopLinkSpec spec : requested.values()) {
            validateOwned(spec.shopId(), ownedShopIds);
            validateCategory(ShopId.of(spec.shopId()), spec.productCategoryId());
        }

        Map<Long, ProductShopLink> existing = new LinkedHashMap<>();
        for (ProductShopLink link : productShopLinkLoadPort.findAllByProductId(productId)) {
            existing.put(link.getShopId().value(), link);
        }

        for (Map.Entry<Long, ProductShopLink> entry : existing.entrySet()) {
            if (requested.containsKey(entry.getKey())) {
                continue;
            }
            validateShopKeepsVisibleProduct(product, ShopId.of(entry.getKey()));
            productShopLinkSavePort.delete(entry.getValue());
        }

        for (ProductShopLinkSpec spec : requested.values()) {
            ProductShopLink link = existing.get(spec.shopId());
            ProductCategoryId categoryId = ProductCategoryId.of(spec.productCategoryId());
            if (link == null) {
                ShopId targetShopId = ShopId.of(spec.shopId());
                productShopLinkSavePort.save(
                    ProductShopLink.of(productId, targetShopId, categoryId, nextSort(targetShopId))
                );
                continue;
            }

            link.relocate(categoryId, link.getSort());
            productShopLinkSavePort.save(link);
        }
    }

    public void linkToShop(ProductId productId, ShopId targetShopId, Long productCategoryId) {
        loadProduct(productId);
        validateCategory(targetShopId, productCategoryId);

        if (productShopLinkLoadPort.existsByProductIdAndShopId(productId, targetShopId)) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_SHOP_LINK_ALREADY_LINKED);
        }

        productShopLinkSavePort.save(
            ProductShopLink.of(productId, targetShopId, ProductCategoryId.of(productCategoryId), nextSort(targetShopId))
        );
    }

    public void unlinkFromShop(ProductId productId, ShopId targetShopId) {
        Product product = loadProduct(productId);

        ProductShopLink link = productShopLinkLoadPort.findByProductIdAndShopId(productId, targetShopId)
            .orElseThrow(() -> new ResourceNotFoundException(CeoErrorCode.PRODUCT_SHOP_LINK_NOT_FOUND));

        if (productShopLinkLoadPort.countByProductId(productId) <= 1) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_SHOP_LINK_LAST_CANNOT_UNLINK);
        }

        validateShopKeepsVisibleProduct(product, targetShopId);
        productShopLinkSavePort.delete(link);
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

            if (productShopLinkLoadPort.existsByProductIdAndShopId(productId, targetShopId)) {
                continue;
            }
            productShopLinkSavePort.save(ProductShopLink.of(
                productId, targetShopId, ProductCategoryId.of(spec.productCategoryId()), nextSort(targetShopId)
            ));
        }
    }

    private Product loadProduct(ProductId productId) {
        return productLoadPort.findById(productId)
            .filter(found -> !found.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
    }

    private Map<Long, ProductShopLinkSpec> toDistinctSpecsByShopId(List<ProductShopLinkSpec> specs) {
        Map<Long, ProductShopLinkSpec> distinct = new LinkedHashMap<>();
        for (ProductShopLinkSpec spec : specs) {
            if (spec.shopId() == null) {
                throw new ApplicationException(CeoErrorCode.PRODUCT_SHOP_LINK_NOT_OWNED);
            }
            distinct.put(spec.shopId(), spec);
        }
        return distinct;
    }

    private void validateOwned(Long shopId, Set<Long> ownedShopIds) {
        if (ownedShopIds == null || !ownedShopIds.contains(shopId)) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_SHOP_LINK_NOT_OWNED);
        }
    }

    private void validateCategory(ShopId shopId, Long productCategoryId) {
        if (productCategoryId == null) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_SHOP_LINK_CATEGORY_REQUIRED);
        }

        ProductCategory category = productCategoryLoadPort.findById(ProductCategoryId.of(productCategoryId))
            .orElseThrow(() -> new ApplicationException(CeoErrorCode.PRODUCT_SHOP_LINK_CATEGORY_MISMATCH));

        if (!shopId.equals(category.getShopId())) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_SHOP_LINK_CATEGORY_MISMATCH);
        }
    }

    private void validateShopKeepsVisibleProduct(Product product, ShopId shopId) {
        if (!product.isVisible()) {
            return;
        }
        if (productLoadPort.countVisibleByShopId(shopId) <= 1) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_LAST_VISIBLE_CANNOT_HIDE);
        }
    }

    private Integer nextSort(ShopId shopId) {
        List<ProductShopLink> links = productShopLinkLoadPort.findAllByShopId(shopId);
        int max = -1;
        for (ProductShopLink link : links) {
            if (link.getSort() != null && link.getSort() > max) {
                max = link.getSort();
            }
        }
        return max + 1;
    }
}
