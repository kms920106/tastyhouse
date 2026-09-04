package com.tastyhouse.ceoapplication.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.product.port.in.ProductQueryUseCase;
import com.tastyhouse.ceoapplication.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.product.port.out.ProductManagementDetailResult;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;

/**
 * 점주용 메뉴 상세 조회 서비스(CQRS query 측).
 *
 * <p>{@code docs/tasks/backend.md}에 없던 단건 상세 GET을 이 서비스가 신설한다 — 프론트 S2(메뉴 상세) 화면
 * 전체가 이 조회에 의존한다.
 */
@Service
@Transactional(readOnly = true)
public class ProductQueryService implements ProductQueryUseCase {

    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductQueryService(ProductOwnerQueryPort productOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductManagementDetailResult getProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductManagementDetailResult dto = productOwnerQueryPort.findProductManagementDetailById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!dto.shopId().equals(shopId)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return dto;
    }

}
