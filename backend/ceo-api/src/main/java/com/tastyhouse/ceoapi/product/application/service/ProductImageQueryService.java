package com.tastyhouse.ceoapi.product.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductImageChangeRequestResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductImageResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductImageStatusResponse;
import com.tastyhouse.ceoapi.product.application.port.in.ProductImageQueryUseCase;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.product.port.out.ProductImageChangeRequestResult;
import com.tastyhouse.application.product.port.out.ProductImageManagementResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;

/**
 * 점주용 메뉴 이미지 현황 조회 서비스(CQRS query 측).
 *
 * <p>반영된 이미지 목록과 검수 요청 이력을 함께 조립한다 — 점주가 "올렸는데 왜 안 보이나"를 한 화면에서
 * 판단할 수 있어야 한다.
 */
@Service
@Transactional(readOnly = true)
public class ProductImageQueryService implements ProductImageQueryUseCase {

    private final ProductQueryPort productQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductImageQueryService(ProductQueryPort productQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.productQueryPort = productQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductImageStatusResponse getImageStatus(Long ceoId, Long shopId, Long productId) {
        requireOwnedProduct(ceoId, shopId, productId);

        List<ProductImageResponse> images = productQueryPort.findProductImagesForManagement(productId).stream()
            .map(this::toProductImageResponse)
            .toList();
        List<ProductImageChangeRequestResponse> requests = productQueryPort.findImageChangeRequests(productId).stream()
            .map(this::toProductImageChangeRequestResponse)
            .toList();

        return ProductImageStatusResponse.from(images, requests);
    }

    /**
     * 가게 소유권과 메뉴-가게 관계를 함께 검증한다.
     *
     * <p>조회 경로는 write 포트를 주입할 수 없으므로(CQRS 교차 주입 금지) 메뉴의 소속 가게를 query
     * DAO로 역조회해 대조한다. "메뉴 없음"과 "남의 가게 메뉴"를 같은
     * {@code PRODUCT_NOT_FOUND}(404)로 합쳐 존재 여부가 새지 않게 한다.
     */
    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        // 메뉴-가게 연결(N:M) 도입으로 동등 비교가 아니라 포함 관계로 판정한다 — 한 메뉴가 여러 가게에
        // 걸리므로, 원본 가게만 인정하면 연결된 가게의 점주가 자기 메뉴판의 메뉴를 열지 못한다.
        boolean owned = productQueryPort.existsProductInShop(productId, shopId);
        if (!owned) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private ProductImageResponse toProductImageResponse(ProductImageManagementResult dto) {
        return ProductImageResponse.from(
            dto.id(),
            dto.imageUrl(),
            dto.sort(),
            dto.visible()
        );
    }

    private ProductImageChangeRequestResponse toProductImageChangeRequestResponse(ProductImageChangeRequestResult dto) {
        return ProductImageChangeRequestResponse.from(
            dto.id(),
            dto.imageUrl(),
            dto.status().name(),
            dto.rejectReason()
        );
    }
}
