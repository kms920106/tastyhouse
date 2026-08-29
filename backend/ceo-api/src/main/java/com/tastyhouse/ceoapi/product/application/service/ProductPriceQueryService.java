package com.tastyhouse.ceoapi.product.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductPriceResponse;
import com.tastyhouse.ceoapi.product.application.port.in.ProductPriceQueryUseCase;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.service.ProductPriceService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주용 메뉴 가격 조회 서비스(CQRS query 측).
 *
 * <p><b>infra query DAO가 아니라 도메인 서비스를 쓴다.</b> 가격 행은 표현 목적 투영이 아니라 전체
 * 교체(PUT)의 <b>입력이 되는 값 그대로</b>여야 한다 — 화면이 받은 목록을 그대로 되돌려 보내는 왕복
 * 구조라, 조회와 저장이 서로 다른 경로로 필드를 해석하면 왕복 중 값이 조용히 달라진다. 또한
 * {@code findPrices}가 메뉴의 가게 소속까지 대조해 주어 IDOR 방어가 한 곳에 모인다.
 *
 * <p>가격 행이 없으면 빈 목록이다 — 조회를 404로 만들면 메뉴 목록 화면 전체가 죽는다(도메인
 * {@code ProductPriceService#findPrices}의 같은 판단).
 */
@Service
@Transactional(readOnly = true)
public class ProductPriceQueryService implements ProductPriceQueryUseCase {

    private final ProductPriceService productPriceService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductPriceQueryService(
        ProductPriceService productPriceService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productPriceService = productPriceService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ProductPriceResponse> getPrices(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return productPriceService.findPrices(ShopId.of(shopId), ProductId.of(productId)).stream()
            .map(this::toProductPriceResponse)
            .toList();
    }

    private ProductPriceResponse toProductPriceResponse(ProductPrice price) {
        return ProductPriceResponse.from(
            price.getId(),
            price.getPriceName(),
            price.getDeliveryPrice(),
            price.getStorePrice(),
            price.getPickupPrice(),
            price.getSort()
        );
    }
}
