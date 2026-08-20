package com.tastyhouse.ceoapi.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.product.query.ProductCategoryManagementResult;
import com.tastyhouse.infrastructure.product.query.ProductQueryDao;
import com.tastyhouse.ceoapi.product.response.ProductCategoryResponse;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주용 메뉴그룹 목록 조회 서비스(CQRS query 측).
 *
 * <p>조회는 infra query DAO가 담당하고 이 서비스는 Result → Response 조립만 한다. write 포트는 주입하지
 * 않는다(CQRS 교차 주입 금지) — 소유권 검증은 그 포트를 내부에 감싼 협력 빈
 * {@link ShopOwnershipValidator}를 경유한다.
 */
@Service
@Transactional(readOnly = true)
public class ProductCategoryQueryService {

    private final ProductQueryDao productQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductCategoryQueryService(
        ProductQueryDao productQueryDao,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productQueryDao = productQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 가게의 메뉴그룹 목록을 {@code sort} 오름차순으로 반환한다. <b>숨긴 그룹도 포함</b>하며
     * 소속 메뉴 수를 함께 담는다(그룹 삭제 가능 여부를 화면이 미리 안내할 수 있게).
     */
    public List<ProductCategoryResponse> getProductCategories(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return productQueryDao.findProductCategoriesForManagement(shopId).stream()
            .map(this::toProductCategoryResponse)
            .toList();
    }

    private ProductCategoryResponse toProductCategoryResponse(ProductCategoryManagementResult row) {
        return ProductCategoryResponse.from(
            row.id(),
            row.name(),
            row.description(),
            row.sort(),
            row.visible(),
            row.productCount()
        );
    }
}
