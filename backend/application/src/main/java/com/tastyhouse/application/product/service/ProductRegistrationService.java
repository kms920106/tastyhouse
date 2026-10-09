package com.tastyhouse.application.product.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

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
import com.tastyhouse.application.product.port.out.write.ProductBbqLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductBbqSavePort;
import com.tastyhouse.application.product.port.out.write.ProductCategorySavePort;
import com.tastyhouse.application.product.port.out.write.ProductImageSavePort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkSavePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupSavePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionSavePort;
import com.tastyhouse.application.product.port.out.write.ProductSavePort;
import com.tastyhouse.application.product.port.out.write.ProductShopLinkSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class ProductRegistrationService {

    private final ProductLoadPort productLoadPort;
    private final ProductSavePort productSavePort;
    private final ProductCategorySavePort productCategorySavePort;
    private final ProductOptionGroupSavePort productOptionGroupSavePort;
    private final ProductOptionSavePort productOptionSavePort;
    private final ProductImageSavePort productImageSavePort;
    private final ProductBbqLoadPort productBbqLoadPort;
    private final ProductBbqSavePort productBbqSavePort;
    private final ProductOptionGroupLinkLoadPort productOptionGroupLinkLoadPort;
    private final ProductOptionGroupLinkSavePort productOptionGroupLinkSavePort;
    private final ProductShopLinkSavePort productShopLinkSavePort;

    public ProductRegistrationService(
        ProductLoadPort productLoadPort,
        ProductSavePort productSavePort,
        ProductCategorySavePort productCategorySavePort,
        ProductOptionGroupSavePort productOptionGroupSavePort,
        ProductOptionSavePort productOptionSavePort,
        ProductImageSavePort productImageSavePort,
        ProductBbqLoadPort productBbqLoadPort,
        ProductBbqSavePort productBbqSavePort,
        ProductOptionGroupLinkLoadPort productOptionGroupLinkLoadPort,
        ProductOptionGroupLinkSavePort productOptionGroupLinkSavePort,
        ProductShopLinkSavePort productShopLinkSavePort
    ) {
        this.productLoadPort = productLoadPort;
        this.productSavePort = productSavePort;
        this.productCategorySavePort = productCategorySavePort;
        this.productOptionGroupSavePort = productOptionGroupSavePort;
        this.productOptionSavePort = productOptionSavePort;
        this.productImageSavePort = productImageSavePort;
        this.productBbqLoadPort = productBbqLoadPort;
        this.productBbqSavePort = productBbqSavePort;
        this.productOptionGroupLinkLoadPort = productOptionGroupLinkLoadPort;
        this.productOptionGroupLinkSavePort = productOptionGroupLinkSavePort;
        this.productShopLinkSavePort = productShopLinkSavePort;
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
        Product saved = productSavePort.save(product);

        productShopLinkSavePort.save(
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
        productSavePort.save(product);
    }

    public void markSoldOut(ProductId productId) {
        Product product = loadProduct(productId);
        product.markSoldOut();
        productSavePort.save(product);
    }

    public void deactivateProduct(ProductId productId) {
        Product product = loadProduct(productId);
        product.deactivate();
        productSavePort.save(product);
    }

    public ProductCategory createProductCategory(
        ShopId shopId,
        String name,
        String description,
        Integer sort,
        boolean visible
    ) {
        ProductCategory category = ProductCategory.of(shopId, name, description, sort, visible);
        return productCategorySavePort.save(category);
    }

    public Long saveProductImage(ProductId productId, UploadedFileId imageFileId, Integer sort, boolean visible) {
        ProductImage image = ProductImage.of(productId, imageFileId, sort, visible);
        ProductImage saved = productImageSavePort.save(image);
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
            : productOptionGroupLinkLoadPort.findAllByProductId(productId).size();
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
        ProductOptionGroup saved = productOptionGroupSavePort.save(group);
        linkOptionGroup(productId, saved.getProductOptionGroupId(), resolvedSort);
        return saved;
    }

    public void linkOptionGroup(ProductId productId, ProductOptionGroupId optionGroupId, Integer sort) {
        if (productOptionGroupLinkLoadPort.existsByProductIdAndOptionGroupId(productId, optionGroupId)) {
            return;
        }
        int resolvedSort = sort != null
            ? sort
            : productOptionGroupLinkLoadPort.findAllByProductId(productId).size();
        productOptionGroupLinkSavePort.save(
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
        ProductOption saved = productOptionSavePort.save(option);
        return saved.getId();
    }

    public void saveProductBbq(ProductId productId, BbqMenuId bbqMenuId, BbqCategoryId bbqCategoryId, boolean optionsSynced) {
        ProductBbq bbq = ProductBbq.of(productId, bbqMenuId, bbqCategoryId, optionsSynced);
        productBbqSavePort.save(bbq);
    }

    public void markBbqOptionsSynced(ProductId productId) {
        ProductBbq bbq = productBbqLoadPort.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
        bbq.markOptionsSynced();
        productBbqSavePort.save(bbq);
    }

    private Product loadProduct(ProductId productId) {
        return productLoadPort.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
    }
}
