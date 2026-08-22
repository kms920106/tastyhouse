package com.tastyhouse.adminapi.product;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.CupDepositOptionRule;
import com.tastyhouse.domain.product.service.CupDepositPolicy;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 관리자 상품 command 서비스. 트랜잭션 경계를 소유하고, 불변식·저장은 도메인 서비스
 * {@link ProductRegistrationService}에 위임한다. 조회는 {@link ProductQueryService}가 담당한다.
 *
 * <p>HTTP 경계에서 받은 {@code Long} 식별자는 이 계층에서 {@code ProductId}로 승격한다.
 */
@Service
@Transactional
public class ProductCommandService {

    private final ProductRegistrationService productRegistrationService;
    private final ProductRepository productRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ShopRepository shopRepository;
    private final CupDepositPolicy cupDepositPolicy;

    public ProductCommandService(
        ProductRegistrationService productRegistrationService,
        ProductRepository productRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ShopRepository shopRepository,
        CupDepositPolicy cupDepositPolicy
    ) {
        this.productRepository = productRepository;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.shopRepository = shopRepository;
        this.cupDepositPolicy = cupDepositPolicy;
        this.productRegistrationService = productRegistrationService;
    }

    public Long createProduct(
        Long shopId,
        Long productCategoryId,
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
        Product product = productRegistrationService.createProduct(
            ShopId.of(shopId),
            ProductCategoryId.of(productCategoryId),
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
            sort,
            false, // 관리자 등록 화면은 아직 이 세 필드를 다루지 않는다(점주 경로에서만 설정)
            null,
            false
        );
        return product.getId();
    }

    public void updateProduct(
        Long id,
        Long productCategoryId,
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
        ProductId productId = ProductId.of(id);
        productRegistrationService.updateProduct(
            productId,
            ProductCategoryId.of(productCategoryId),
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
    }

    public void markSoldOut(Long id) {
        ProductId productId = ProductId.of(id);
        productRegistrationService.markSoldOut(productId);
    }

    public void deactivateProduct(Long id) {
        ProductId productId = ProductId.of(id);
        productRegistrationService.deactivateProduct(productId);
    }

    public Long createProductOptionGroup(
        Long id,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible,
        String groupType
    ) {
        ProductOptionGroupType resolvedGroupType = ProductOptionGroupType.from(groupType);
        // 관리자 경로에도 같은 게이트를 적용한다 — 관리자가 대상 사업자 플래그를 먼저 켠 뒤 만들도록
        // 강제해야, 규제 대상이 아닌 가게에 보증금 옵션이 생기는 경로가 남지 않는다.
        if (resolvedGroupType.isCupDeposit()) {
            loadShopOf(ProductId.of(id)).validateCupDepositEnabled();
        }
        CupDepositOptionRule.validateDepositGroupSelectRange(
            resolvedGroupType, required, multipleSelect, minSelect, maxSelect
        );

        ProductOptionGroup optionGroup = productRegistrationService.saveProductOptionGroup(
            ProductId.of(id),
            name,
            description,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            sort,
            visible,
            resolvedGroupType
        );
        return optionGroup.getId();
    }

    /** 보증금 대상 사업자 검증을 위해 메뉴가 속한 가게를 로드한다. */
    private Shop loadShopOf(ProductId productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        return shopRepository.findById(product.getShopId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    public Long createProductOption(
        Long groupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        ProductOptionGroup optionGroup = productOptionGroupRepository
            .findById(ProductOptionGroupId.of(groupId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));
        CupDepositOptionRule.validateOptionValues(
            optionGroup, additionalPrice, cupCount, personalCupDiscountAmount, cupDepositPolicy
        );

        return productRegistrationService.saveProductOption(
            ProductOptionGroupId.of(groupId),
            name,
            additionalPrice,
            sort,
            soldOut,
            visible,
            cupCount,
            personalCupDiscountAmount
        );
    }

    public Long createProductImage(Long id, Long imageFileId, Integer sort, boolean visible) {
        return productRegistrationService.saveProductImage(
            ProductId.of(id), UploadedFileId.of(imageFileId), sort, visible
        );
    }

    public Long createProductCategory(Long shopId, String name, Integer sort, boolean visible) {
        ProductCategory category = productRegistrationService.createProductCategory(
            ShopId.of(shopId), name, null, sort, visible
        );
        return category.getId();
    }
}
