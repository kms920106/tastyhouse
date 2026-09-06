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

/**
 * 상품 등록·구성 오케스트레이션 도메인 서비스(순수 POJO).
 *
 * <p>상품 본체와 그에 딸린 카테고리·옵션그룹·옵션·이미지·BBQ 매핑은 서로 다른 애그리거트지만, 등록·변경
 * 시 한 트랜잭션 안에서 함께 저장되어야 한다(분류 C — 불변식 오케스트레이션). 액터(admin 상품 CRUD /
 * batch BBQ 크롤링)에 무관하게 동일한 규칙이므로, 소비 모듈로 복제하지 않고 이 도메인 서비스에 둔다.
 *
 * <p>{@code @Service}/{@code @Transactional}을 갖지 않는다 — 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 {@code ProductCommandService}가 소유하고, 빈 등록은 infrastructure-module의
 * {@code ProductDomainConfig}가 담당한다.
 *
 * <p>이 서비스는 도메인 이벤트를 발행하지 않는다 — 과거 {@code ProductCreatedEvent} ·
 * {@code ProductSoldOutChangedEvent} · {@code ProductDeactivatedEvent} 세 종을 발행했으나 수신
 * 리스너가 없는 no-op이어서 P9(도메인 이벤트 정비)에서 제거했다. 상품 등록·품절·비노출은 모두 호출부
 * 트랜잭션 안에서 완결되므로 비동기 후처리 수요가 생기기 전까지 발행을 되살리지 않는다(YAGNI).
 */
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

    /**
     * 상품 등록.
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
            null,  // 등록 경로는 품절 기간을 다루지 않는다(기간 지정은 점주 품절·숨김 관리 경로의 몫)
            visible,
            sort,
            ratingExcluded,
            composition,
            singleServing
        );
        Product saved = productRepository.save(product);

        // 원본 소유 가게 링크를 함께 만든다 — 메뉴판 노출의 진실원이 PRODUCT_SHOP_LINK이므로,
        // 이 링크가 없으면 등록된 메뉴가 어느 가게 메뉴판에도 나타나지 않는다.
        //
        // 이 지점에 두는 이유는 메뉴를 만드는 경로가 셋(ceo 등록·admin 등록·batch BBQ 동기화)이고
        // 전부 이 서비스를 경유하기 때문이다. 호출부마다 배선하면 한 곳이 반드시 빠진다.
        productShopLinkRepository.save(
            ProductShopLink.of(saved.getProductId(), shopId, productCategoryId, sort)
        );
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
     * 상품 품절 처리.
     */
    public void markSoldOut(ProductId productId) {
        Product product = loadProduct(productId);
        product.markSoldOut();
        productRepository.save(product);
    }

    /**
     * 상품 비노출 처리.
     */
    public void deactivateProduct(ProductId productId) {
        Product product = loadProduct(productId);
        product.deactivate();
        productRepository.save(product);
    }

    /**
     * 상품 카테고리 등록.
     */
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

    /**
     * 상품 이미지 등록.
     */
    public Long saveProductImage(ProductId productId, UploadedFileId imageFileId, Integer sort, boolean visible) {
        ProductImage image = ProductImage.of(productId, imageFileId, sort, visible);
        ProductImage saved = productImageRepository.save(image);
        return saved.getId();
    }

    /**
     * 상품 옵션 그룹 등록 + 그 메뉴로의 연결 생성.
     *
     * <p><b>그룹 저장과 링크 생성을 함께 한다</b> — 링크 없이 저장된 그룹은 어느 메뉴에서도 보이지 않는
     * 고아가 되고, 읽기 경로가 전부 링크 조인으로 바뀐 뒤에는 화면에서 완전히 사라진다.
     *
     * <p>{@code productId}·{@code sort}를 그룹에도 계속 채우는 것은 <b>2단계 배포</b>의 1단계이기
     * 때문이다 — {@code ddl-auto: validate} 환경이라 컬럼 제거(STEP 6 SQL)와 엔티티 필드 제거 배포가
     * 원자적이어야 하므로, 지금은 읽기만 링크로 전환하고 쓰기는 양쪽을 채운다(read-dead 상태).
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

    /**
     * 메뉴와 옵션그룹을 연결한다. 이미 연결돼 있으면 아무 일도 하지 않는다(멱등).
     *
     * <p>연결 자체를 별도 메서드로 분리해 둔 이유는, 기존 그룹을 다른 메뉴에도 붙이는 경로(점주
     * 메뉴-옵션그룹 연결 화면)가 그룹을 새로 만들지 않고 링크만 추가하기 때문이다.
     */
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

    /**
     * 상품 옵션 등록.
     */
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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        bbq.markOptionsSynced();
        productBbqRepository.save(bbq);
    }

    private Product loadProduct(ProductId productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
