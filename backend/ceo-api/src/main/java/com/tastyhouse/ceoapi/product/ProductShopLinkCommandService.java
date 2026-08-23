package com.tastyhouse.ceoapi.product;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.service.ProductShopLinkService;
import com.tastyhouse.domain.product.service.ProductShopLinkSpec;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.ceoapi.product.request.ProductShopLinkItemRequest;
import com.tastyhouse.ceoapi.shop.OwnedShopIdProvider;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주용 메뉴-가게 연결 변경 서비스(CQRS command 측).
 *
 * <p>불변식은 전부 도메인({@link ProductShopLinkService})이 소유한다 — 메뉴그룹의 가게 일치, 링크 최소
 * 1개 유지, 해제 후 메뉴판 노출 메뉴 잔존, 중복 연결 차단까지. 이 서비스는 트랜잭션 경계·소유권
 * 검증·VO 승격·spec 변환만 담당한다.
 *
 * <p><b>소유 가게 집합을 여기서 구해 도메인에 넘긴다.</b> 도메인은 {@code ceoId}를 알지 못하고("이
 * 점주가 이 가게를 갖고 있는가"는 ceo-api의 인가 관심사다), 연결 대상이 여러 건이라 가게마다
 * {@code ShopOwnershipValidator}를 반복 호출하면 그만큼 조회가 늘어난다.
 *
 * <p>소유 가게 집합은 {@link OwnedShopIdProvider}를 통해 얻는다 — command 서비스가 query DAO를 직접
 * 주입하는 것은 ArchUnit이 금지하므로(CQRS 교차 주입 금지), 인가 관심사를 그 빈 안에 가둔다.
 */
@Service
@Transactional
public class ProductShopLinkCommandService {

    private final ProductShopLinkService productShopLinkService;
    private final OwnedShopIdProvider ownedShopIdProvider;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductShopLinkCommandService(
        ProductShopLinkService productShopLinkService,
        OwnedShopIdProvider ownedShopIdProvider,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productShopLinkService = productShopLinkService;
        this.ownedShopIdProvider = ownedShopIdProvider;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 메뉴의 가게 연결을 통째로 교체한다. 목록에 없는 가게는 연결 해제된다.
     */
    public void replaceLinks(Long ceoId, Long shopId, Long productId, List<ProductShopLinkItemRequest> links) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<ProductShopLinkSpec> specs = links.stream().map(this::toProductShopLinkSpec).toList();
        productShopLinkService.replaceLinks(ProductId.of(productId), specs, ownedShopIds(ceoId));
    }

    /**
     * 가게 메뉴판으로 메뉴를 불러온다(가게 기준 진입).
     *
     * <p>요청 주체 가게가 아니라 <b>대상 가게</b>의 소유권을 검증한다 — 불러오는 쪽 메뉴판이 바뀌므로
     * 그 가게에 대한 권한이 필요하다.
     */
    public void linkToShop(Long ceoId, Long productId, Long targetShopId, Long productCategoryId) {
        shopOwnershipValidator.validateOwnership(ceoId, targetShopId);

        productShopLinkService.linkToShop(
            ProductId.of(productId), ShopId.of(targetShopId), productCategoryId
        );
    }

    /**
     * 가게 메뉴판에서 메뉴를 제외한다. 메뉴 자체는 삭제되지 않는다.
     */
    public void unlinkFromShop(Long ceoId, Long productId, Long targetShopId) {
        shopOwnershipValidator.validateOwnership(ceoId, targetShopId);

        productShopLinkService.unlinkFromShop(ProductId.of(productId), ShopId.of(targetShopId));
    }

    private Set<Long> ownedShopIds(Long ceoId) {
        return ownedShopIdProvider.findOwnedShopIds(ceoId);
    }

    private ProductShopLinkSpec toProductShopLinkSpec(ProductShopLinkItemRequest item) {
        return ProductShopLinkSpec.of(item.shopId(), item.productCategoryId());
    }
}
