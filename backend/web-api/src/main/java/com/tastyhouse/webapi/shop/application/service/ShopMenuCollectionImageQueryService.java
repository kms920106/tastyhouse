package com.tastyhouse.webapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.shop.query.ShopMenuCollectionImageExposureResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopMenuCollectionImageResponse;

/**
 * 손님용 메뉴모음컷 조회 서비스(CQRS query 측).
 *
 * <p>승인분 필터는 이 서비스가 아니라 투영({@code ShopQueryDao#findExposedMenuCollectionImages})이
 * 소유한다 — 필터를 소비 측에 두면 새 호출 경로가 생길 때 조용히 빠져 검수 전 이미지가 노출된다.
 */
@Service
@Transactional(readOnly = true)
public class ShopMenuCollectionImageQueryService {

    private final ShopQueryDao shopQueryDao;

    public ShopMenuCollectionImageQueryService(ShopQueryDao shopQueryDao) {
        this.shopQueryDao = shopQueryDao;
    }

    public List<ShopMenuCollectionImageResponse> getMenuCollectionImages(Long shopId) {
        return shopQueryDao.findExposedMenuCollectionImages(shopId).stream()
            .map(this::toShopMenuCollectionImageResponse)
            .toList();
    }

    private ShopMenuCollectionImageResponse toShopMenuCollectionImageResponse(
        ShopMenuCollectionImageExposureResult dto
    ) {
        return ShopMenuCollectionImageResponse.from(
            dto.id(),
            dto.imageUrl(),
            dto.sort()
        );
    }
}
