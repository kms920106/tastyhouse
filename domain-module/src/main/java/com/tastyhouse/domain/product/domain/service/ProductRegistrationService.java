package com.tastyhouse.domain.product.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.product.domain.event.ProductCreatedEvent;
import com.tastyhouse.domain.product.domain.event.ProductDeactivatedEvent;
import com.tastyhouse.domain.product.domain.event.ProductSoldOutChangedEvent;
import com.tastyhouse.domain.product.domain.model.Product;
import com.tastyhouse.domain.product.domain.model.ProductBbq;
import com.tastyhouse.domain.product.domain.model.ProductCategory;
import com.tastyhouse.domain.product.domain.model.ProductImage;
import com.tastyhouse.domain.product.domain.model.ProductOption;
import com.tastyhouse.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.domain.product.domain.repository.ProductBbqRepository;
import com.tastyhouse.domain.product.domain.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.domain.repository.ProductImageRepository;
import com.tastyhouse.domain.product.domain.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.domain.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.domain.repository.ProductRepository;
import com.tastyhouse.domain.product.domain.vo.BbqCategoryId;
import com.tastyhouse.domain.product.domain.vo.BbqMenuId;
import com.tastyhouse.domain.product.domain.vo.ProductCategoryId;
import com.tastyhouse.domain.product.domain.vo.ProductId;
import com.tastyhouse.domain.product.domain.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 상품 등록·구성 오케스트레이션 도메인 서비스(순수 POJO).
 *
 * <p>상품 본체와 그에 딸린 카테고리·옵션그룹·옵션·이미지·BBQ 매핑은 서로 다른 애그리거트지만, 등록·변경
 * 시 한 트랜잭션 안에서 함께 저장되어야 한다(분류 C — 불변식 오케스트레이션). 액터(admin 상품 CRUD /
 * batch BBQ 크롤링)에 무관하게 동일한 규칙이므로, 소비 모듈로 복제하지 않고 이 도메인 서비스에 둔다.
 *
 * <p>{@code @Service}/{@code @Transactional}을 갖지 않는다 — 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 {@code ProductCommandService}가 소유하고, 빈 등록은 infrastructure-module의
 * {@code DomainServiceConfig}가 담당한다. 이벤트 발행은 프레임워크-프리 포트
 * {@link DomainEventPublisher}를 쓴다.
 */
public class ProductRegistrationService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductBbqRepository productBbqRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ProductRegistrationService(
        ProductRepository productRepository,
        ProductCategoryRepository productCategoryRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository,
        ProductBbqRepository productBbqRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productOptionRepository = productOptionRepository;
        this.productImageRepository = productImageRepository;
        this.productBbqRepository = productBbqRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 상품 등록. 저장 직후 {@link ProductCreatedEvent}를 발행한다.
     */
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
        Integer sort
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
            visible,
            sort
        );
        Product saved = productRepository.save(product);
        domainEventPublisher.publish(new ProductCreatedEvent(
            saved.getProductId(),
            saved.getShopId(),
            LocalDateTime.now()
        ));
        return saved;
    }

    /**
     * 상품 정보 수정.
     */
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

    /**
     * 상품 품절 처리. 상태 전이 후 {@link ProductSoldOutChangedEvent}를 발행한다.
     */
    public void markSoldOut(ProductId productId) {
        Product product = loadProduct(productId);
        product.markSoldOut();
        productRepository.save(product);
        domainEventPublisher.publish(new ProductSoldOutChangedEvent(
            product.getProductId(),
            product.getShopId(),
            true,
            LocalDateTime.now()
        ));
    }

    /**
     * 상품 비노출 처리. 상태 전이 후 {@link ProductDeactivatedEvent}를 발행한다.
     */
    public void deactivateProduct(ProductId productId) {
        Product product = loadProduct(productId);
        product.deactivate();
        productRepository.save(product);
        domainEventPublisher.publish(new ProductDeactivatedEvent(
            product.getProductId(),
            product.getShopId(),
            LocalDateTime.now()
        ));
    }

    /**
     * 상품 카테고리 등록.
     */
    public ProductCategory createProductCategory(ShopId shopId, String name, Integer sort, boolean visible) {
        ProductCategory category = ProductCategory.of(shopId, name, sort, visible);
        return productCategoryRepository.save(category);
    }

    /**
     * 상품 이미지 등록.
     */
    public Long saveProductImage(ProductId productId, UploadedFileId imageFileId, Integer sort, boolean visible) {
        ProductImage image = ProductImage.of(productId, imageFileId, sort, visible);
        ProductImage saved = productImageRepository.save(image);
        return saved.getId();
    }

    /**
     * 상품 옵션 그룹 등록.
     */
    public ProductOptionGroup saveProductOptionGroup(
        ProductId productId,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible
    ) {
        ProductOptionGroup group = ProductOptionGroup.of(
            productId,
            name,
            description,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            sort,
            visible
        );
        return productOptionGroupRepository.save(group);
    }

    /**
     * 상품 옵션 등록.
     */
    public Long saveProductOption(
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        boolean visible
    ) {
        ProductOption option = ProductOption.of(optionGroupId, name, additionalPrice, sort, soldOut, visible);
        ProductOption saved = productOptionRepository.save(option);
        return saved.getId();
    }

    /**
     * 상품 ↔ BBQ 메뉴 매핑 등록.
     */
    public void saveProductBbq(ProductId productId, BbqMenuId bbqMenuId, BbqCategoryId bbqCategoryId, boolean optionsSynced) {
        ProductBbq bbq = ProductBbq.of(productId, bbqMenuId, bbqCategoryId, optionsSynced);
        productBbqRepository.save(bbq);
    }

    /**
     * BBQ 옵션 동기화 완료 표시.
     */
    public void markBbqOptionsSynced(ProductId productId) {
        ProductBbq bbq = productBbqRepository.findByProductId(productId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        bbq.markOptionsSynced();
        productBbqRepository.save(bbq);
    }

    private Product loadProduct(ProductId productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
