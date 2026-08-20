package com.tastyhouse.ceoapi.product;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.service.ProductOptionGroupLinkService;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 경로 식별자가 <b>옵션그룹·옵션</b>인 엔드포인트의 소유권 검증기.
 *
 * <p>{@link ShopOwnershipValidator}만으로는 부족하다 — 그것은 "로그인 점주가 이 가게의 주인인가"만
 * 답하므로, 자기 가게 id를 실어 보내면서 <b>남의 가게 옵션그룹 id</b>를 경로에 넣는 요청을 통과시킨다.
 * 이 저장소는 정확히 이 역조회를 빠뜨려 IDOR 사고를 낸 전례가 있다(배달가능지역 삭제).
 *
 * <p>옵션그룹은 자기 가게를 모른다. 그래서 <b>그룹 → 링크 → 메뉴 → 가게</b> 3단으로 역조회하고
 * ({@link ProductOptionGroupLinkService#findOwningShopId}) 요청의 {@code shopId}와 대조한다. 단일 가게
 * 불변식 덕분에 "연결된 아무 메뉴 하나"로 판정할 수 있다.
 *
 * <p><b>연결이 0건이면 소유자를 판정할 수 없으므로 접근 불가로 다룬다</b>({@code null}을 "허용"으로
 * 읽으면 곧 인가 우회다). 등록 시 항상 링크를 함께 만들기 때문에 정상 경로에서는 발생하지 않고,
 * 발생했다면 그 그룹은 어느 화면에서도 보이지 않는 고아 상태다. 미존재와 타 가게 소유를 같은
 * {@code PRODUCT_OPTION_GROUP_NOT_FOUND}(404)로 묶어 <b>존재 여부 자체를 흘리지 않는다.</b>
 */
@Component
public class ProductOptionGroupOwnershipValidator {

    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionGroupLinkService productOptionGroupLinkService;

    public ProductOptionGroupOwnershipValidator(
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductOptionGroupLinkService productOptionGroupLinkService
    ) {
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productOptionRepository = productOptionRepository;
        this.productOptionGroupLinkService = productOptionGroupLinkService;
    }

    /**
     * 옵션그룹을 로드하면서 소유 가게가 {@code shopId}인지 대조한다.
     *
     * @throws ResourceNotFoundException 그룹이 없거나, 연결이 0건이라 소유자를 판정할 수 없거나,
     *     소유 가게가 {@code shopId}와 다른 경우
     */
    public ProductOptionGroup loadOwnedOptionGroup(Long shopId, Long optionGroupId) {
        ProductOptionGroup group = productOptionGroupRepository.findById(ProductOptionGroupId.of(optionGroupId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));
        validateOptionGroupShop(shopId, optionGroupId);
        return group;
    }

    /**
     * 옵션을 로드하면서 그 옵션이 속한 그룹의 소유 가게가 {@code shopId}인지 대조한다 — 옵션은
     * {@code 옵션 → 그룹 → 링크 → 메뉴 → 가게} 4단 역조회가 필요하다.
     *
     * @throws ResourceNotFoundException 옵션이 없거나 소유 가게가 {@code shopId}와 다른 경우
     */
    public ProductOption loadOwnedOption(Long shopId, Long optionId) {
        ProductOption option = productOptionRepository.findById(ProductOptionId.of(optionId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));
        // 옵션 경로에서는 그룹 미존재·타 가게 소유도 "옵션을 찾을 수 없다"로 응답한다 — 경로가 가리키는
        // 리소스는 옵션이므로, 그룹의 존재 여부를 별도 코드로 흘리지 않는다.
        if (doesNotOwnOptionGroup(shopId, option.getOptionGroupId().value())) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        }
        return option;
    }

    /** 옵션그룹의 소유 가게가 {@code shopId}인지 검증한다(도메인 모델 로드가 필요 없는 경로용). */
    public void validateOptionGroupShop(Long shopId, Long optionGroupId) {
        if (doesNotOwnOptionGroup(shopId, optionGroupId)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND);
        }
    }

    /**
     * 그룹 → 링크 → 메뉴 → 가게 역조회 결과가 {@code shopId}와 다른지 판정한다.
     * 소유자를 판정할 수 없으면({@code null}) <b>소유하지 않는 것으로</b> 본다.
     */
    private boolean doesNotOwnOptionGroup(Long shopId, Long optionGroupId) {
        ShopId owner = productOptionGroupLinkService.findOwningShopId(ProductOptionGroupId.of(optionGroupId));
        return owner == null || !owner.equals(ShopId.of(shopId));
    }
}
