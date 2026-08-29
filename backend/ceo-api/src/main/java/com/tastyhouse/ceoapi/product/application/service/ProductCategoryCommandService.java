package com.tastyhouse.ceoapi.product.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.product.adapter.in.web.ProductSortApiController;
import com.tastyhouse.ceoapi.product.application.port.in.ProductCategoryCommandUseCase;
import com.tastyhouse.ceoapi.product.application.port.in.ProductCategoryCreateCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductCategoryDeleteCommand;
import com.tastyhouse.ceoapi.product.application.port.in.ProductCategoryUpdateCommand;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주용 메뉴그룹(카테고리) 등록·변경·삭제 서비스(CQRS command 측).
 *
 * <p>정렬값은 클라이언트에서 받지 않는다 — 등록 시 서버가 가게의 현재 메뉴그룹 수로 채워 맨 뒤에 붙이고,
 * 위치 조정은 순서 변경 API({@link ProductSortApiController})가 담당한다.
 */
@Service
@Transactional
public class ProductCategoryCommandService implements ProductCategoryCommandUseCase {

    /** 등록 직후의 노출 상태 — 점주가 만든 그룹은 곧바로 메뉴판에 보인다. */
    private static final boolean DEFAULT_VISIBLE = true;

    private final ProductRegistrationService productRegistrationService;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductCategoryCommandService(
        ProductRegistrationService productRegistrationService,
        ProductCategoryRepository productCategoryRepository,
        ProductRepository productRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productCategoryRepository = productCategoryRepository;
        this.productRepository = productRepository;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /** 메뉴그룹을 등록하고 생성된 id를 반환한다. */
    @Override
    public Long createProductCategory(ProductCategoryCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String name = command.name();
        String description = command.description();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(name);
        prohibitedWordValidator.validate(description);

        ProductCategory created = productRegistrationService.createProductCategory(
            ShopId.of(shopId),
            name,
            description,
            nextSort(shopId),
            DEFAULT_VISIBLE
        );
        return created.getId();
    }

    /** 메뉴그룹명·설명을 변경한다. 정렬은 이 경로로 바꾸지 않는다(순서 변경 API의 몫). */
    @Override
    public void updateProductCategory(ProductCategoryUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long productCategoryId = command.productCategoryId();
        Long shopId = command.shopId();
        String name = command.name();
        String description = command.description();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(name);
        prohibitedWordValidator.validate(description);

        ProductCategory category = loadOwnedCategory(shopId, productCategoryId);
        category.changeDetails(name, description);
        productCategoryRepository.save(category);
    }

    /**
     * 메뉴그룹을 삭제한다.
     *
     * <p>소속 메뉴가 남아 있으면 {@code PRODUCT_CATEGORY_HAS_PRODUCTS}(400)로 거부한다 — 그룹만 지우면
     * 메뉴들이 조용히 미분류로 떠내려가 점주가 의도하지 않은 메뉴판이 된다. 먼저 다른 그룹으로 옮기거나
     * 메뉴를 삭제해야 한다.
     */
    @Override
    public void deleteProductCategory(ProductCategoryDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long productCategoryId = command.productCategoryId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductCategory category = loadOwnedCategory(shopId, productCategoryId);
        if (productRepository.countByCategoryId(ProductCategoryId.of(productCategoryId)) > 0) {
            throw new BusinessException(ErrorCode.PRODUCT_CATEGORY_HAS_PRODUCTS);
        }
        productCategoryRepository.delete(category);
    }

    /**
     * 대상 메뉴그룹을 로드하면서 소유 가게까지 대조한다 — 가게 소유권만 검증하면 남의 가게 메뉴그룹 id를
     * 실어 보내는 경로가 열린다. 미존재와 타 가게 소유는 같은 코드로 묶는다.
     */
    private ProductCategory loadOwnedCategory(Long shopId, Long productCategoryId) {
        ProductCategory category = productCategoryRepository.findById(ProductCategoryId.of(productCategoryId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
        if (!category.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND);
        }
        return category;
    }

    /** 가게의 현재 메뉴그룹 수를 다음 정렬값으로 쓴다(0-based라 곧 맨 뒤 인덱스다). */
    private Integer nextSort(Long shopId) {
        return productCategoryRepository.findAllByShopId(ShopId.of(shopId)).size();
    }
}
