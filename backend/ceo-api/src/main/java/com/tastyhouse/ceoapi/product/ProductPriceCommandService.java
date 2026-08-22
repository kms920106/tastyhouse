package com.tastyhouse.ceoapi.product;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.service.ProductPriceService;
import com.tastyhouse.domain.product.service.ProductPriceSpec;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.ceoapi.product.request.ProductPriceItemRequest;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주용 메뉴 가격 변경 서비스(CQRS command 측).
 *
 * <p>불변식은 전부 도메인({@link ProductPriceService})이 소유한다 — 가격명 중복·정렬·1개 이상,
 * 매장 가격 인증 게이트, 할인 진행 중 차단, {@code PRODUCT.original_price} 동기화, 배달가 &gt; 매장가일
 * 때의 인증 해제까지. 이 서비스는 트랜잭션 경계·소유권 검증·VO 승격·spec 변환만 담당한다.
 *
 * <p><b>{@code now}를 여기서 만들어 넘긴다.</b> 도메인이 시계를 직접 읽으면 픽업가 설정 시각(뱃지의
 * 익일 노출 기준점)이 테스트에서 고정될 수 없고, 한 요청 안의 여러 행이 서로 다른 시각을 갖게 된다.
 *
 * <p>가게 소유권만 검증하면 남의 가게 메뉴 id를 실어 보내는 경로가 열리지만, 그 대조는 도메인
 * {@code replacePrices}가 메뉴를 가게 범위로 로드하며 함께 수행한다 — 영양성분처럼 별도
 * {@code validateProductOwnedByShop}를 두지 않는 이유다(중복 조회가 될 뿐이다).
 */
@Service
@Transactional
public class ProductPriceCommandService {

    private final ProductPriceService productPriceService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductPriceCommandService(
        ProductPriceService productPriceService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productPriceService = productPriceService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 메뉴 가격 목록을 통째로 교체한다. 요청에 담기지 않은 기존 행은 삭제된다.
     */
    public void replacePrices(Long ceoId, Long shopId, Long productId, List<ProductPriceItemRequest> prices) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ProductId targetProductId = ProductId.of(productId);
        List<ProductPriceSpec> specs = prices.stream().map(this::toProductPriceSpec).toList();
        productPriceService.replacePrices(targetShopId, targetProductId, specs, LocalDateTime.now());
    }

    private ProductPriceSpec toProductPriceSpec(ProductPriceItemRequest item) {
        return ProductPriceSpec.of(
            item.id(),
            item.priceName(),
            item.deliveryPrice(),
            item.storePrice(),
            item.pickupPrice(),
            item.sort()
        );
    }
}
