package com.tastyhouse.application.product.service;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductBbqPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductCategoryPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductImagePersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductShopLinkPersistencePort;
import com.tastyhouse.application.shared.marker.SharedApp;

@SharedApp
public class ProductRegistrationService {

    private final ProductPersistencePort productPersistencePort;
    private final ProductCategoryPersistencePort productCategoryPersistencePort;
    private final ProductOptionGroupPersistencePort productOptionGroupPersistencePort;
    private final ProductOptionPersistencePort productOptionPersistencePort;
    private final ProductImagePersistencePort productImagePersistencePort;
    private final ProductBbqPersistencePort productBbqPersistencePort;
    private final ProductOptionGroupLinkPersistencePort productOptionGroupLinkPersistencePort;
    private final ProductShopLinkPersistencePort productShopLinkPersistencePort;

    public ProductRegistrationService(
        ProductPersistencePort productPersistencePort,
        ProductCategoryPersistencePort productCategoryPersistencePort,
        ProductOptionGroupPersistencePort productOptionGroupPersistencePort,
        ProductOptionPersistencePort productOptionPersistencePort,
        ProductImagePersistencePort productImagePersistencePort,
        ProductBbqPersistencePort productBbqPersistencePort,
        ProductOptionGroupLinkPersistencePort productOptionGroupLinkPersistencePort,
        ProductShopLinkPersistencePort productShopLinkPersistencePort
    ) {
        this.productPersistencePort = productPersistencePort;
        this.productCategoryPersistencePort = productCategoryPersistencePort;
        this.productOptionGroupPersistencePort = productOptionGroupPersistencePort;
        this.productOptionPersistencePort = productOptionPersistencePort;
        this.productImagePersistencePort = productImagePersistencePort;
        this.productBbqPersistencePort = productBbqPersistencePort;
        this.productOptionGroupLinkPersistencePort = productOptionGroupLinkPersistencePort;
        this.productShopLinkPersistencePort = productShopLinkPersistencePort;
    }

    public Product createProduct(
        ShopId shopId,
        ProductCategoryId productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        Double rating,
        Integer reviewCount,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        boolean visible,
        Integer sort,
        boolean ratingExcluded,
        String composition,
        boolean singleServing
    ) {
        Product product = Product.of(
            shopId,
            productCategoryId,
            name,
            description,
            originalPrice,
            discountPrice,
            discountRate,
            rating,
            reviewCount,
            representative,
            spiciness,
            soldOut,
            null,
            visible,
            sort,
            ratingExcluded,
            composition,
            singleServing
        );
        Product saved = productPersistencePort.save(product);

        productShopLinkPersistencePort.save(
            ProductShopLink.of(saved.getProductId(), shopId, productCategoryId, sort)
        );
        return saved;
    }

    public void updateProduct(
        ProductId productId,
        ProductCategoryId productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        boolean visible,
        Integer sort
    ) {
        Product product = loadProduct(productId);
        product.update(
            productCategoryId,
            name,
            description,
            originalPrice,
            discountPrice,
            discountRate,
            representative,
            spiciness,
            soldOut,
            visible,
            sort
        );
        productPersistencePort.save(product);
    }

    public void markSoldOut(ProductId productId) {
        Product product = loadProduct(productId);
        product.markSoldOut();
        productPersistencePort.save(product);
    }

    public void deactivateProduct(ProductId productId) {
        Product product = loadProduct(productId);
        product.deactivate();
        productPersistencePort.save(product);
    }

    public ProductCategory createProductCategory(
        ShopId shopId,
        String name,
        String description,
        Integer sort,
        boolean visible
    ) {
        ProductCategory category = ProductCategory.of(shopId, name, description, sort, visible);
        return productCategoryPersistencePort.save(category);
    }

    public Long saveProductImage(ProductId productId, UploadedFileId imageFileId, Integer sort, boolean visible) {
        ProductImage image = ProductImage.of(productId, imageFileId, sort, visible);
        ProductImage saved = productImagePersistencePort.save(image);
        return saved.getId();
    }

    public ProductOptionGroup saveProductOptionGroup(
        ProductId productId,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible,
        ProductOptionGroupType groupType
    ) {
        int resolvedSort = sort != null
            ? sort
            : productOptionGroupLinkPersistencePort.findAllByProductId(productId).size();
        ProductOptionGroup group = ProductOptionGroup.of(
            productId,
            name,
            description,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            resolvedSort,
            visible,
            groupType
        );
        ProductOptionGroup saved = productOptionGroupPersistencePort.save(group);
        linkOptionGroup(productId, saved.getProductOptionGroupId(), resolvedSort);
        return saved;
    }

    public void linkOptionGroup(ProductId productId, ProductOptionGroupId optionGroupId, Integer sort) {
        if (productOptionGroupLinkPersistencePort.existsByProductIdAndOptionGroupId(productId, optionGroupId)) {
            return;
        }
        int resolvedSort = sort != null
            ? sort
            : productOptionGroupLinkPersistencePort.findAllByProductId(productId).size();
        productOptionGroupLinkPersistencePort.save(
            ProductOptionGroupLink.of(productId, optionGroupId, resolvedSort));
    }

    public Long saveProductOption(
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        ProductOption option = ProductOption.of(
            optionGroupId,
            name,
            additionalPrice,
            sort,
            soldOut,
            null,
            visible,
            cupCount,
            personalCupDiscountAmount
        );
        ProductOption saved = productOptionPersistencePort.save(option);
        return saved.getId();
    }

    public void saveProductBbq(ProductId productId, BbqMenuId bbqMenuId, BbqCategoryId bbqCategoryId, boolean optionsSynced) {
        ProductBbq bbq = ProductBbq.of(productId, bbqMenuId, bbqCategoryId, optionsSynced);
        productBbqPersistencePort.save(bbq);
    }

    public void markBbqOptionsSynced(ProductId productId) {
        ProductBbq bbq = productBbqPersistencePort.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        bbq.markOptionsSynced();
        productBbqPersistencePort.save(bbq);
    }

    private Product loadProduct(ProductId productId) {
        return productPersistencePort.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
