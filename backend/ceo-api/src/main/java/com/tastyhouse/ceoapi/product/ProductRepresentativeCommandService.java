package com.tastyhouse.ceoapi.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.service.ProductRepresentativeApprovalService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주용 사장님 추천(대표 메뉴) 서비스(CQRS command 측).
 *
 * <p>개수 제한·이미지 요건·최소 1개 유지 세 불변식은 도메인 서비스
 * {@link ProductRepresentativeApprovalService}가 소유하고, 이 서비스는 트랜잭션 경계·가게 소유권
 * 검증·경계 타입 승격(Long → ID VO)만 담당한다.
 *
 * <p><b>메뉴가 그 가게 것인지는 도메인 서비스가 확인한다.</b> 여기서 가게 소유권만 확인하고 끝내면
 * 다른 가게의 메뉴 id를 넣은 요청이 통과하는데, 대상이 목록이라 확인이 건별로 필요하다 — 그 판정을
 * 도메인의 로드 경로에 두면 개수 제한 계산과 같은 곳에서 같은 집합을 보게 되어 빠뜨릴 수 없다
 * ({@code ProductImageCommandService#requireOwnedProduct}가 단건에서 두 축을 함께 보는 것과 같은 취지).
 */
@Service
@Transactional
public class ProductRepresentativeCommandService {

    private final ProductRepresentativeApprovalService productRepresentativeApprovalService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductRepresentativeCommandService(
        ProductRepresentativeApprovalService productRepresentativeApprovalService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productRepresentativeApprovalService = productRepresentativeApprovalService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 대표 메뉴 지정을 신청한다. 점주가 직접 켤 수는 없다 — 관리자 승인 시에만 반영된다.
     *
     * @return 생성된 검수 요청 식별자들. 이미 추천이거나 대기 중인 메뉴 몫은 담기지 않는다
     */
    public List<Long> requestRepresentative(Long ceoId, Long shopId, List<Long> productIds) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        List<ProductId> targetProductIds = productIds.stream()
            .filter(java.util.Objects::nonNull)
            .map(ProductId::of)
            .toList();

        return productRepresentativeApprovalService.requestRepresentative(targetShopId, targetProductIds);
    }

    /**
     * 대표 메뉴 지정을 해제한다. <b>승인을 거치지 않고 즉시 반영된다</b> — 검수의 목적은 부적합한
     * 메뉴가 상단에 노출되는 것을 막는 데 있고, 해제 방향에는 그 위험이 없다.
     *
     * <p>단 가게마다 최소 1개는 남아야 한다({@code PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE}, 400).
     */
    public void clearRepresentative(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ProductId targetProductId = ProductId.of(productId);

        productRepresentativeApprovalService.clearRepresentative(targetShopId, targetProductId);
    }
}
