package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductCategoryQueryUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.application.product.port.out.ProductCategoryManagementResult;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;

/**
 * 점주용 메뉴그룹 목록 조회 서비스(CQRS query 측).
 *
 * <p>조회는 infra query DAO가 담당하고 이 서비스는 Result → Response 조립만 한다. write 포트는 주입하지
 * 않는다(CQRS 교차 주입 금지) — 소유권 검증은 그 포트를 내부에 감싼 협력 빈
 * {@link ShopOwnershipValidator}를 경유한다.
 */
@Service
@CeoApp
@Transactional(readOnly = true)
public class ProductCategoryQueryService implements ProductCategoryQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductCategoryQueryService(
        ProductOwnerQueryPort productOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 가게의 메뉴그룹 목록을 {@code sort} 오름차순으로 반환한다. <b>숨긴 그룹도 포함</b>하며
     * 소속 메뉴 수를 함께 담는다(그룹 삭제 가능 여부를 화면이 미리 안내할 수 있게).
     */
    @Override
    public List<ProductCategoryManagementResult> getProductCategories(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return productOwnerQueryPort.findProductCategoriesForManagement(shopId);
    }

}
