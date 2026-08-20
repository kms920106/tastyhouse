package com.tastyhouse.ceoapi.product;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주용 옵션 등록·변경·삭제·순서 서비스(CQRS command 측).
 *
 * <p>모든 경로가 옵션그룹의 소유 가게를 역조회해 검증한다
 * ({@link ProductOptionGroupOwnershipValidator}) — 옵션은 자기 가게를 모르므로
 * {@code 옵션 → 그룹 → 링크 → 메뉴 → 가게} 역조회 없이는 남의 가게 옵션을 조작하는 것을 막을 수 없다.
 */
@Service
@Transactional
public class ProductOptionCommandService {

    /** 등록 직후의 상태 — 점주가 만든 옵션은 곧바로 판매 가능하다. */
    private static final boolean DEFAULT_SOLD_OUT = false;
    private static final boolean DEFAULT_VISIBLE = true;

    private final ProductRegistrationService productRegistrationService;
    private final ProductOptionRepository productOptionRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionCommandService(
        ProductRegistrationService productRegistrationService,
        ProductOptionRepository productOptionRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productOptionRepository = productOptionRepository;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    /** 옵션그룹에 옵션을 등록하고 생성된 id를 반환한다. 정렬값은 서버가 목록 맨 뒤로 채운다. */
    public Long createProductOption(
        Long ceoId,
        Long shopId,
        Long optionGroupId,
        String name,
        Integer additionalPrice
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(name);
        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, optionGroupId);

        return productRegistrationService.saveProductOption(
            ProductOptionGroupId.of(optionGroupId),
            name,
            additionalPrice,
            nextSort(optionGroupId),
            DEFAULT_SOLD_OUT,
            DEFAULT_VISIBLE
        );
    }

    /** 옵션명·추가 금액을 변경한다. 품절·숨김 상태와 순서는 이 경로로 바꾸지 않는다. */
    public void updateProductOption(
        Long ceoId,
        Long optionId,
        Long shopId,
        String name,
        Integer additionalPrice
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(name);

        ProductOption option = productOptionGroupOwnershipValidator.loadOwnedOption(shopId, optionId);
        // sort·soldOut·visible은 현재 값을 그대로 넘긴다 — update가 전체 필드를 받는 형태라, 빼먹으면
        // 이름만 고쳤는데 품절이 조용히 풀리거나 순서가 초기화된다.
        option.update(name, additionalPrice, option.getSort(), option.isSoldOut(), option.isVisible());
        productOptionRepository.save(option);
    }

    /**
     * 옵션을 감춘다(소프트 삭제).
     *
     * <p>행을 지우지 않는 이유는 주문 이력이다 — 옵션은 주문 시점에 {@code ORDER_PRODUCT_OPTION}으로
     * 박제되지만 그 스냅샷이 {@code option_id}를 함께 남기므로, 하드 삭제하면 과거 주문의 참조가
     * 끊어진다. 감추기만 하면 <b>과거 주문 이력은 보존되고 메뉴판에서만 사라진다.</b>
     *
     * <p><b>그룹의 최소 선택 개수 잔여 제약을 검사한다</b> — 감추기도 품절과 똑같이 선택 가능한 옵션
     * 수를 줄이므로, 남은 판매중 옵션이 {@code minSelect}에 못 미치면
     * {@code PRODUCT_OPTION_MIN_SELECT_VIOLATION}(400)으로 거부한다. 이 검사가 없으면 필수 그룹인데
     * 고를 수 있는 옵션이 없어 <b>주문 자체가 불가능한 메뉴</b>가 만들어진다.
     */
    public void deleteProductOption(Long ceoId, Long optionId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductOption option = productOptionGroupOwnershipValidator.loadOwnedOption(shopId, optionId);
        Long optionGroupId = option.getOptionGroupId().value();
        ProductOptionGroup group =
            productOptionGroupOwnershipValidator.loadOwnedOptionGroup(shopId, optionGroupId);

        validateMinSelectAfterHiding(group, option);

        option.hide();
        productOptionRepository.save(option);
    }

    /**
     * 옵션그룹 내 옵션 순서를 통째로 교체한다(replace-all).
     *
     * <p>{@code sort} 값을 받지 않고 순서 있는 id 배열만 받아 {@code 0..N-1}을 부여한다. 요청 집합이
     * 그룹의 현재 옵션 집합과 다르면 {@code PRODUCT_ORDER_TARGET_MISMATCH}(400)로 거부한다 — 다른
     * 탭에서 추가·삭제된 stale 요청을 부분 적용하면 순서가 뒤섞인다.
     */
    public void changeProductOptionOrder(Long ceoId, Long shopId, Long optionGroupId, List<Long> optionIds) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, optionGroupId);

        Map<Long, ProductOption> byId = productOptionRepository
            .findAllByOptionGroupId(ProductOptionGroupId.of(optionGroupId)).stream()
            .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

        List<Long> requested = distinct(optionIds);
        if (byId.size() != requested.size() || !byId.keySet().containsAll(requested)) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        for (int index = 0; index < requested.size(); index++) {
            ProductOption option = byId.get(requested.get(index));
            option.update(
                option.getName(),
                option.getAdditionalPrice(),
                index,
                option.isSoldOut(),
                option.isVisible()
            );
            productOptionRepository.save(option);
        }
    }

    /**
     * 이 옵션을 감춘 뒤에도 그룹의 최소 선택 개수를 채울 수 있는지 검증한다.
     *
     * <p>선택 가능 = 품절 아님 + 노출 중. 대상 옵션이 이미 선택 불가 상태면 감춰도 개수가 줄지 않으므로
     * 검사를 건너뛴다.
     */
    private void validateMinSelectAfterHiding(ProductOptionGroup group, ProductOption target) {
        int minSelect = group.getMinSelect() != null ? group.getMinSelect() : 0;
        if (minSelect <= 0) {
            return;
        }
        if (target.isSoldOut() || !target.isVisible()) {
            return;
        }

        long selectableAfterHiding = productOptionRepository
            .findAllByOptionGroupId(group.getProductOptionGroupId()).stream()
            .filter(option -> !option.getId().equals(target.getId()))
            .filter(option -> !option.isSoldOut() && option.isVisible())
            .count();
        if (selectableAfterHiding < minSelect) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION);
        }
    }

    /** 옵션그룹의 현재 옵션 수를 다음 정렬값으로 쓴다(0-based라 곧 맨 뒤 인덱스다). */
    private Integer nextSort(Long optionGroupId) {
        return productOptionRepository.findAllByOptionGroupId(ProductOptionGroupId.of(optionGroupId)).size();
    }

    private List<Long> distinct(List<Long> ids) {
        Set<Long> unique = new LinkedHashSet<>(ids);
        return List.copyOf(unique);
    }
}
