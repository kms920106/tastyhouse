package com.tastyhouse.ceoapi.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주용 옵션그룹 등록·변경·삭제 서비스(CQRS command 측).
 *
 * <p>정렬값은 클라이언트에서 받지 않는다 — 순서는 그룹이 아니라 <b>링크</b>가 갖고, 위치 조정은
 * 연결 순서 API({@link ProductOptionGroupLinkApiController})가 담당한다.
 */
@Service
@Transactional
public class ProductOptionGroupCommandService {

    /** 등록 직후의 노출 상태 — 점주가 만든 그룹은 곧바로 메뉴판에 보인다. */
    private static final boolean DEFAULT_VISIBLE = true;

    /**
     * 링크 정렬값을 서버가 정하게 맡기는 센티넬. {@code ProductRegistrationService#linkOptionGroup}이
     * {@code null}을 받으면 이 메뉴의 현재 연결 수(= 맨 뒤 인덱스)로 채운다.
     */
    private static final Integer NEXT_SORT_APPENDS_TO_TAIL = null;

    private final ProductRegistrationService productRegistrationService;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductRepository productRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionGroupCommandService(
        ProductRegistrationService productRegistrationService,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductRepository productRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productRepository = productRepository;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    /**
     * 옵션그룹을 등록하고 생성된 id를 반환한다. 대상 메뉴에 <b>곧바로 연결</b>한다.
     *
     * <p>연결을 함께 만드는 이유는 두 가지다. {@code PRODUCT_OPTION_GROUP.product_id}가 1단계 배포
     * 동안 {@code NOT NULL}이고, 무엇보다 연결이 0건인 그룹은 어느 조회 경로에서도 보이지 않으며
     * (모든 읽기가 링크를 INNER JOIN한다) 소유 가게도 판정되지 않아 되찾을 수 없는 고아가 된다.
     */
    public Long createProductOptionGroup(
        Long ceoId,
        Long shopId,
        Long productId,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(name);
        prohibitedWordValidator.validate(description);
        validateSelectRange(minSelect, maxSelect);

        Product product = loadOwnedProduct(shopId, productId);
        ProductOptionGroup created = productRegistrationService.saveProductOptionGroup(
            ProductId.of(product.getId()),
            name,
            description,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            NEXT_SORT_APPENDS_TO_TAIL,
            DEFAULT_VISIBLE
        );
        return created.getId();
    }

    /** 옵션그룹명·설명·선택 제약을 변경한다. 연결과 순서는 이 경로로 바꾸지 않는다. */
    public void updateProductOptionGroup(
        Long ceoId,
        Long optionGroupId,
        Long shopId,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(name);
        prohibitedWordValidator.validate(description);
        validateSelectRange(minSelect, maxSelect);

        ProductOptionGroup group =
            productOptionGroupOwnershipValidator.loadOwnedOptionGroup(shopId, optionGroupId);
        // sort·visible은 현재 값을 그대로 넘긴다 — update가 전체 필드를 받는 형태라, 빼먹으면 조용히
        // 0/false로 덮어써 순서와 노출 상태가 함께 초기화된다.
        group.update(
            name,
            description,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            group.getSort(),
            group.isVisible()
        );
        productOptionGroupRepository.save(group);
    }

    /**
     * 옵션그룹을 감춘다(소프트 삭제).
     *
     * <p>행을 지우지 않는 이유는 주문 이력이다 — 이 그룹의 옵션들은 주문 시점에
     * {@code ORDER_PRODUCT_OPTION}으로 박제되지만 그 스냅샷이 {@code option_group_id}를 함께 남기므로,
     * 하드 삭제하면 과거 주문의 참조가 끊어진다. 감추기만 하면 <b>과거 주문 이력은 보존되고 메뉴판에서만
     * 사라진다.</b> 연결(링크)도 지우지 않는다 — 지우면 소유 가게 역조회가 불가능해져 되살릴 수 없다.
     */
    public void deleteProductOptionGroup(Long ceoId, Long optionGroupId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductOptionGroup group =
            productOptionGroupOwnershipValidator.loadOwnedOptionGroup(shopId, optionGroupId);
        group.hide();
        productOptionGroupRepository.save(group);
    }

    /**
     * 대상 메뉴를 로드하면서 소유 가게까지 대조한다 — 가게 소유권만 검증하면 남의 가게 메뉴 id를
     * 실어 보내 그 메뉴에 옵션그룹을 붙이는 경로가 열린다.
     */
    private Product loadOwnedProduct(Long shopId, Long productId) {
        Product product = productRepository.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    /**
     * {@code minSelect} ≤ {@code maxSelect}를 검증한다. 한쪽이 미지정({@code null})이면 비교할 대상이
     * 없으므로 통과시킨다 — "무제한"과 "0보다 크다"는 서로 모순되지 않는다.
     */
    private void validateSelectRange(Integer minSelect, Integer maxSelect) {
        if (minSelect != null && maxSelect != null && minSelect > maxSelect) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_SELECT_RANGE_INVALID);
        }
    }
}
