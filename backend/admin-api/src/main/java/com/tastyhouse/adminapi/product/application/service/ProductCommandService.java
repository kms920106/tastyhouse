package com.tastyhouse.adminapi.product.application.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.adminapi.product.application.port.in.ProductCategoryCreateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductCategoryCreateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductCreateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductCreateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductDeactivateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductDeactivateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductImageCreateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductImageCreateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductOptionCreateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductOptionCreateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductOptionGroupCreateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductOptionGroupCreateUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductSoldOutCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductSoldOutUseCase;
import com.tastyhouse.adminapi.product.application.port.in.ProductUpdateCommand;
import com.tastyhouse.adminapi.product.application.port.in.ProductUpdateUseCase;
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
public class ProductCommandService implements
    ProductCreateUseCase,
    ProductUpdateUseCase,
    ProductSoldOutUseCase,
    ProductDeactivateUseCase,
    ProductOptionGroupCreateUseCase,
    ProductOptionCreateUseCase,
    ProductImageCreateUseCase,
    ProductCategoryCreateUseCase {

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

    @Override
    public Long createProduct(ProductCreateCommand command) {
        Long shopId = command.shopId();
        Long productCategoryId = command.productCategoryId();
        String name = command.name();
        String description = command.description();
        Integer originalPrice = command.originalPrice();
        Integer discountPrice = command.discountPrice();
        BigDecimal discountRate = command.discountRate();
        Double rating = command.rating();
        Integer reviewCount = command.reviewCount();
        boolean representative = command.representative();
        Integer spiciness = command.spiciness();
        boolean soldOut = command.soldOut();
        boolean visible = command.visible();
        Integer sort = command.sort();

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

    @Override
    public void updateProduct(ProductUpdateCommand command) {
        Long id = command.productId();
        Long productCategoryId = command.productCategoryId();
        String name = command.name();
        String description = command.description();
        Integer originalPrice = command.originalPrice();
        Integer discountPrice = command.discountPrice();
        BigDecimal discountRate = command.discountRate();
        boolean representative = command.representative();
        Integer spiciness = command.spiciness();
        boolean soldOut = command.soldOut();
        boolean visible = command.visible();
        Integer sort = command.sort();

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

    @Override
    public void markSoldOut(ProductSoldOutCommand command) {
        Long id = command.productId();
        ProductId productId = ProductId.of(id);
        productRegistrationService.markSoldOut(productId);
    }

    @Override
    public void deactivateProduct(ProductDeactivateCommand command) {
        Long id = command.productId();
        ProductId productId = ProductId.of(id);
        productRegistrationService.deactivateProduct(productId);
    }

    @Override
    public Long createProductOptionGroup(ProductOptionGroupCreateCommand command) {
        Long id = command.productId();
        String name = command.name();
        String description = command.description();
        boolean required = command.required();
        boolean multipleSelect = command.multipleSelect();
        Integer minSelect = command.minSelect();
        Integer maxSelect = command.maxSelect();
        Integer sort = command.sort();
        boolean visible = command.visible();
        String groupType = command.groupType();

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

    @Override
    public Long createProductOption(ProductOptionCreateCommand command) {
        Long groupId = command.optionGroupId();
        String name = command.name();
        Integer additionalPrice = command.additionalPrice();
        Integer sort = command.sort();
        boolean soldOut = command.soldOut();
        boolean visible = command.visible();
        Integer cupCount = command.cupCount();
        Integer personalCupDiscountAmount = command.personalCupDiscountAmount();

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

    @Override
    public Long createProductImage(ProductImageCreateCommand command) {
        Long id = command.productId();
        Long imageFileId = command.imageFileId();
        Integer sort = command.sort();
        boolean visible = command.visible();

        return productRegistrationService.saveProductImage(
            ProductId.of(id), UploadedFileId.of(imageFileId), sort, visible
        );
    }

    @Override
    public Long createProductCategory(ProductCategoryCreateCommand command) {
        Long shopId = command.shopId();
        String name = command.name();
        Integer sort = command.sort();
        boolean visible = command.visible();

        ProductCategory category = productRegistrationService.createProductCategory(
            ShopId.of(shopId), name, null, sort, visible
        );
        return category.getId();
    }
}
