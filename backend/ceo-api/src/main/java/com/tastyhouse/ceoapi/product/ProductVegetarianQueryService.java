package com.tastyhouse.ceoapi.product;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.service.ProductVegetarianApprovalService;
import com.tastyhouse.infrastructure.product.query.ProductQueryDao;
import com.tastyhouse.infrastructure.product.query.ProductVegetarianRequestResult;
import com.tastyhouse.infrastructure.product.query.ProductVegetarianSettingResult;
import com.tastyhouse.ceoapi.product.response.ProductVegetarianRequestResponse;
import com.tastyhouse.ceoapi.product.response.ProductVegetarianStatusResponse;
import com.tastyhouse.ceoapi.shop.ShopFoodTypeCategoryReader;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주용 메뉴 채식 설정 현황 조회 서비스(CQRS query 측).
 *
 * <p>현재 반영값({@code PRODUCT.vegetarian_type})과 요청 이력을 나눠 내려준다 — 승인 전 요청이 있어도
 * 반영값은 바뀌지 않으므로 두 축을 합치면 점주가 "이미 적용됐다"고 오해한다.
 */
@Service
@Transactional(readOnly = true)
public class ProductVegetarianQueryService {

    private final ProductQueryDao productQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopFoodTypeCategoryReader shopFoodTypeCategoryReader;
    private final ProductVegetarianApprovalService productVegetarianApprovalService;

    public ProductVegetarianQueryService(
        ProductQueryDao productQueryDao,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopFoodTypeCategoryReader shopFoodTypeCategoryReader,
        ProductVegetarianApprovalService productVegetarianApprovalService
    ) {
        this.productQueryDao = productQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopFoodTypeCategoryReader = shopFoodTypeCategoryReader;
        this.productVegetarianApprovalService = productVegetarianApprovalService;
    }

    public ProductVegetarianStatusResponse getVegetarianStatus(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductVegetarianSettingResult setting = productQueryDao.findVegetarianSetting(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!setting.shopId().equals(shopId)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        List<ProductVegetarianRequestResponse> requests = productQueryDao.findVegetarianRequests(productId).stream()
            .map(this::toProductVegetarianRequestResponse)
            .toList();

        Set<String> shopCategoryNames = shopFoodTypeCategoryReader.readCategoryNames(shopId);
        boolean changeable = productVegetarianApprovalService.isShopCategoryAllowed(shopCategoryNames);

        return ProductVegetarianStatusResponse.from(vegetarianTypeName(setting.vegetarianType()), requests, changeable);
    }

    private ProductVegetarianRequestResponse toProductVegetarianRequestResponse(ProductVegetarianRequestResult dto) {
        return ProductVegetarianRequestResponse.from(
            dto.id(),
            dto.vegetarianType().name(),
            dto.ingredients(),
            dto.description(),
            dto.status().name(),
            dto.rejectReason()
        );
    }

    /** 채식 메뉴가 아니면 {@code null}이다 — 해제 상태를 빈 문자열로 뭉개지 않는다. */
    private String vegetarianTypeName(VegetarianType vegetarianType) {
        return vegetarianType == null ? null : vegetarianType.name();
    }
}
