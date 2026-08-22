package com.tastyhouse.ceoapi.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.infrastructure.product.query.ProductManagementDetailResult;
import com.tastyhouse.infrastructure.product.query.ProductQueryDao;
import com.tastyhouse.ceoapi.product.response.ProductDetailResponse;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주용 메뉴 상세 조회 서비스(CQRS query 측).
 *
 * <p>{@code docs/tasks/backend.md}에 없던 단건 상세 GET을 이 서비스가 신설한다 — 프론트 S2(메뉴 상세) 화면
 * 전체가 이 조회에 의존한다.
 */
@Service
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductQueryDao productQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductQueryService(ProductQueryDao productQueryDao, ShopOwnershipValidator shopOwnershipValidator) {
        this.productQueryDao = productQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public ProductDetailResponse getProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductManagementDetailResult dto = productQueryDao.findProductManagementDetailById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!dto.shopId().equals(shopId)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return toProductDetailResponse(dto);
    }

    private ProductDetailResponse toProductDetailResponse(ProductManagementDetailResult dto) {
        return ProductDetailResponse.from(
            dto.id(),
            dto.shopId(),
            dto.productCategoryId(),
            dto.productCategoryName(),
            dto.name(),
            dto.composition(),
            dto.description(),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.singleServing(),
            dto.spiciness(),
            dto.representative(),
            dto.ratingExcluded(),
            dto.soldOut(),
            dto.visible(),
            dto.imageUrl(),
            dto.vegetarianType() == null ? null : dto.vegetarianType().name(),
            dto.weightText(),
            dto.exposureScheduled()
        );
    }
}
