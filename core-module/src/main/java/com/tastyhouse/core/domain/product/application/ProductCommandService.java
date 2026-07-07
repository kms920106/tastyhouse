package com.tastyhouse.core.domain.product.application;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.product.domain.event.ProductCreatedEvent;
import com.tastyhouse.core.domain.product.domain.event.ProductDeactivatedEvent;
import com.tastyhouse.core.domain.product.domain.event.ProductSoldOutChangedEvent;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.ProductBbq;
import com.tastyhouse.core.domain.product.domain.model.ProductCategory;
import com.tastyhouse.core.domain.product.domain.model.ProductImage;
import com.tastyhouse.core.domain.product.domain.model.ProductOption;
import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.core.domain.product.domain.repository.ProductBbqRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductCategoryRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductImageRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionGroupRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductRepository;
import com.tastyhouse.core.domain.product.domain.vo.ProductId;
import com.tastyhouse.core.domain.product.application.dto.command.ProductCategoryCreateCommand;
import com.tastyhouse.core.domain.product.application.dto.command.ProductCreateCommand;
import com.tastyhouse.core.domain.product.application.dto.command.SaveProductBbqCommand;
import com.tastyhouse.core.domain.product.application.dto.command.SaveProductImageCommand;
import com.tastyhouse.core.domain.product.application.dto.command.SaveProductOptionCommand;
import com.tastyhouse.core.domain.product.application.dto.command.SaveProductOptionGroupCommand;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductBbqRepository productBbqRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Product createProduct(ProductCreateCommand cmd) {
        Product product = Product.of(
            cmd.shopId(),
            cmd.productCategoryId(),
            cmd.name(),
            cmd.description(),
            cmd.originalPrice(),
            cmd.discountPrice(),
            cmd.discountRate(),
            cmd.rating(),
            cmd.reviewCount(),
            cmd.representative(),
            cmd.spiciness(),
            cmd.soldOut(),
            cmd.visible(),
            cmd.sort()
        );
        Product saved = productRepository.save(product);
        eventPublisher.publishEvent(new ProductCreatedEvent(
            saved.getId(),
            saved.getShopId(),
            LocalDateTime.now()
        ));
        return saved;
    }

    @Transactional
    public ProductCategory createProductCategory(ProductCategoryCreateCommand cmd) {
        ProductCategory category = ProductCategory.of(
            cmd.shopId(),
            cmd.name(),
            cmd.sort(),
            cmd.visible()
        );
        return productCategoryRepository.save(category);
    }

    @Transactional
    public void saveProductImage(SaveProductImageCommand cmd) {
        ProductImage image = ProductImage.of(
            cmd.productId(),
            cmd.imageFileId(),
            cmd.sort(),
            cmd.visible()
        );
        productImageRepository.save(image);
    }

    @Transactional
    public void saveProductBbq(SaveProductBbqCommand cmd) {
        ProductBbq bbq = ProductBbq.of(
            cmd.productId(),
            cmd.bbqMenuId(),
            cmd.bbqCategoryId(),
            cmd.optionsSynced()
        );
        productBbqRepository.save(bbq);
    }

    @Transactional
    public ProductOptionGroup saveProductOptionGroup(SaveProductOptionGroupCommand cmd) {
        ProductOptionGroup group = ProductOptionGroup.of(
            cmd.productId(),
            cmd.name(),
            cmd.description(),
            cmd.required(),
            cmd.multipleSelect(),
            cmd.minSelect(),
            cmd.maxSelect(),
            cmd.sort(),
            cmd.visible()
        );
        return productOptionGroupRepository.save(group);
    }

    @Transactional
    public void saveProductOption(SaveProductOptionCommand cmd) {
        ProductOption option = ProductOption.of(
            cmd.optionGroupId(),
            cmd.name(),
            cmd.additionalPrice(),
            cmd.sort(),
            cmd.soldOut(),
            cmd.visible()
        );
        productOptionRepository.save(option);
    }

    @SuppressWarnings("unused")
    @Transactional
    public void markSoldOut(ProductId productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        product.markSoldOut();
        productRepository.save(product);
        eventPublisher.publishEvent(new ProductSoldOutChangedEvent(
            product.getId(),
            product.getShopId(),
            true,
            LocalDateTime.now()
        ));
    }

    @SuppressWarnings("unused")
    @Transactional
    public void deactivateProduct(ProductId productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        product.deactivate();
        productRepository.save(product);
        eventPublisher.publishEvent(new ProductDeactivatedEvent(
            product.getId(),
            product.getShopId(),
            LocalDateTime.now()
        ));
    }

    @Transactional
    public void markBbqOptionsSynced(ProductId productId) {
        ProductBbq bbq = productBbqRepository.findByProductId(productId.value())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        bbq.markOptionsSynced();
        productBbqRepository.save(bbq);
    }
}
