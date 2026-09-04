package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductOptionGroupLinkCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupLinkCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionGroupOrderChangeCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupUnlinkCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductOptionGroupLinkService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주용 메뉴-옵션그룹 연결 서비스(CQRS command 측).
 *
 * <p>단일 가게 불변식·마지막 연결 차단·정렬 정규화는 도메인 서비스
 * {@link ProductOptionGroupLinkService}가 소유하고, 이 서비스는 트랜잭션 경계·소유권 검증·VO 승격만
 * 담당한다.
 *
 * <p><b>연결 대상 그룹의 소유권도 검증한다.</b> 메뉴 소유권만 보면 남의 가게 옵션그룹 id를 실어
 * 보내는 경로가 열린다 — 도메인 서비스가
 * {@code PRODUCT_OPTION_GROUP_SHOP_MISMATCH}로 막아주지만, 그것은 "그룹에 이미 연결이 있을 때"만
 * 성립하는 방어다. 여기서 먼저 404로 끊어 존재 여부 자체를 흘리지 않는다.
 */
@Service
@CeoApp
@Transactional
public class ProductOptionGroupLinkCommandService implements ProductOptionGroupLinkCommandUseCase {

    private final ProductOptionGroupLinkService productOptionGroupLinkService;
    private final ProductRepository productRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionGroupLinkCommandService(
        ProductOptionGroupLinkService productOptionGroupLinkService,
        ProductRepository productRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productOptionGroupLinkService = productOptionGroupLinkService;
        this.productRepository = productRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    /** 메뉴에 옵션그룹을 연결한다. 이미 연결돼 있으면 아무 일도 하지 않는다(멱등). */
    @Override
    public void linkOptionGroup(ProductOptionGroupLinkCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        Long optionGroupId = command.optionGroupId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        loadOwnedProduct(shopId, productId);
        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, optionGroupId);

        productOptionGroupLinkService.link(ProductId.of(productId), ProductOptionGroupId.of(optionGroupId));
    }

    /**
     * 연결을 해제한다. 마지막 연결이면 도메인 서비스가
     * {@code PRODUCT_OPTION_GROUP_LAST_LINK_CANNOT_UNLINK}(400)로 거부한다 — 연결이 0건이면 어디서도
     * 보이지 않는 고아 그룹이 된다.
     */
    @Override
    public void unlinkOptionGroup(ProductOptionGroupUnlinkCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        Long optionGroupId = command.optionGroupId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        loadOwnedProduct(shopId, productId);
        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, optionGroupId);

        productOptionGroupLinkService.unlink(ProductId.of(productId), ProductOptionGroupId.of(optionGroupId));
    }

    /**
     * 이 메뉴에서의 옵션그룹 순서를 통째로 교체한다(replace-all).
     *
     * <p>요청 id 집합이 이 메뉴의 현재 연결 집합과 다르면 도메인 서비스가
     * {@code PRODUCT_ORDER_TARGET_MISMATCH}(400)로 거부한다 — 다른 탭에서 연결·해제된 stale 요청이다.
     */
    @Override
    public void changeOptionGroupOrder(ProductOptionGroupOrderChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        List<Long> optionGroupIds = command.optionGroupIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        loadOwnedProduct(shopId, productId);
        // 그룹 하나하나의 소유권까지 볼 필요는 없다 — 이 메뉴에 연결된 집합과 정확히 일치해야만
        // 통과하므로(불일치는 PRODUCT_ORDER_TARGET_MISMATCH), 남의 가게 그룹 id는 애초에 통과하지 못한다.
        productOptionGroupLinkService.reorder(ProductId.of(productId), toOptionGroupIds(optionGroupIds));
    }

    /**
     * 대상 메뉴를 로드하면서 소유 가게까지 대조한다. 미존재와 타 가게 소유는 같은 코드로 묶는다 —
     * 남의 가게 메뉴 id의 존재 여부를 응답으로 흘리지 않는다.
     */
    private void loadOwnedProduct(Long shopId, Long productId) {
        Product product = productRepository.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private List<ProductOptionGroupId> toOptionGroupIds(List<Long> optionGroupIds) {
        return optionGroupIds.stream().map(ProductOptionGroupId::of).toList();
    }
}
