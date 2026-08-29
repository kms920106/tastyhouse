package com.tastyhouse.ceoapi.product.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductShopLinkResponse;
import com.tastyhouse.ceoapi.product.application.port.in.ProductShopLinkQueryUseCase;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.infrastructure.product.query.ProductShopLinkQueryDao;
import com.tastyhouse.infrastructure.product.query.ProductShopLinkResult;

/**
 * 점주용 메뉴-가게 연결 조회 서비스(CQRS query 측).
 *
 * <p>{@code shopId}(요청 주체 가게)로 소유권을 먼저 검증한 뒤, 그 점주가 소유한 <b>전체 가게</b>와
 * 각각의 연결 여부를 내려보낸다 — 화면이 토글로 켜고 끄는 형태라 연결되지 않은 가게도 필요하다.
 */
@Service
@Transactional(readOnly = true)
public class ProductShopLinkQueryService implements ProductShopLinkQueryUseCase {

    private final ProductShopLinkQueryDao productShopLinkQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductShopLinkQueryService(
        ProductShopLinkQueryDao productShopLinkQueryDao,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productShopLinkQueryDao = productShopLinkQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 이 메뉴를 연결할 수 있는 가게 목록(= 점주 소유 전체 가게)과 각각의 연결 여부를 조회한다.
     */
    @Override
    public List<ProductShopLinkResponse> getShopLinks(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return productShopLinkQueryDao.findOwnedShopLinks(ceoId, productId).stream()
            .map(this::toProductShopLinkResponse)
            .toList();
    }

    private ProductShopLinkResponse toProductShopLinkResponse(ProductShopLinkResult result) {
        return ProductShopLinkResponse.from(
            result.shopId(),
            result.shopName(),
            result.productCategoryId(),
            result.productCategoryName(),
            result.linked()
        );
    }
}
