package com.tastyhouse.domain.product.service;

import java.math.BigDecimal;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.repository.ProductBbqRepository;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductShopLinkRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class ProductRegistrationService {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductBbqRepository productBbqRepository;
    private final ProductOptionGroupLinkRepository productOptionGroupLinkRepository;
    private final ProductShopLinkRepository productShopLinkRepository;

    public ProductRegistrationService(
        ProductRepository productRepository,
        ProductCategoryRepository productCategoryRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository,
        ProductBbqRepository productBbqRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductShopLinkRepository productShopLinkRepository
    ) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productOptionRepository = productOptionRepository;
        this.productImageRepository = productImageRepository;
        this.productBbqRepository = productBbqRepository;
        this.productOptionGroupLinkRepository = productOptionGroupLinkRepository;
        this.productShopLinkRepository = productShopLinkRepository;
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
        Product saved = productRepository.save(product);

        productShopLinkRepository.save(
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
        productRepository.save(product);
    }

    public void markSoldOut(ProductId productId) {
        Product product = loadProduct(productId);
        product.markSoldOut();
        productRepository.save(product);
    }

    public void deactivateProduct(ProductId productId) {
        Product product = loadProduct(productId);
        product.deactivate();
        productRepository.save(product);
    }

    public ProductCategory createProductCategory(
        ShopId shopId,
        String name,
        String description,
        Integer sort,
        boolean visible
    ) {
        ProductCategory category = ProductCategory.of(shopId, name, description, sort, visible);
        return productCategoryRepository.save(category);
    }

    public Long saveProductImage(ProductId productId, UploadedFileId imageFileId, Integer sort, boolean visible) {
        ProductImage image = ProductImage.of(productId, imageFileId, sort, visible);
        ProductImage saved = productImageRepository.save(image);
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
            : productOptionGroupLinkRepository.findAllByProductId(productId).size();
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
        ProductOptionGroup saved = productOptionGroupRepository.save(group);
        linkOptionGroup(productId, saved.getProductOptionGroupId(), resolvedSort);
        return saved;
    }

    public void linkOptionGroup(ProductId productId, ProductOptionGroupId optionGroupId, Integer sort) {
        if (productOptionGroupLinkRepository.existsByProductIdAndOptionGroupId(productId, optionGroupId)) {
            return;
        }
        int resolvedSort = sort != null
            ? sort
            : productOptionGroupLinkRepository.findAllByProductId(productId).size();
        productOptionGroupLinkRepository.save(
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
        ProductOption saved = productOptionRepository.save(option);
        return saved.getId();
    }

    public void saveProductBbq(ProductId productId, BbqMenuId bbqMenuId, BbqCategoryId bbqCategoryId, boolean optionsSynced) {
        ProductBbq bbq = ProductBbq.of(productId, bbqMenuId, bbqCategoryId, optionsSynced);
        productBbqRepository.save(bbq);
    }

    public void markBbqOptionsSynced(ProductId productId) {
        ProductBbq bbq = productBbqRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        bbq.markOptionsSynced();
        productBbqRepository.save(bbq);
    }

    private Product loadProduct(ProductId productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
