package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageExposureResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageQueryUseCase;

/**
 * 손님용 메뉴모음컷 조회 서비스(CQRS query 측).
 *
 * <p>승인분 필터는 이 서비스가 아니라 투영({@code ShopQueryPort#findExposedMenuCollectionImages})이
 * 소유한다 — 필터를 소비 측에 두면 새 호출 경로가 생길 때 조용히 빠져 검수 전 이미지가 노출된다.
 */
@Service
@WebApp
@Transactional(readOnly = true)
public class ShopMenuCollectionImageQueryService implements ShopMenuCollectionImageQueryUseCase {

    private final ShopQueryPort shopQueryPort;

    public ShopMenuCollectionImageQueryService(ShopQueryPort shopQueryPort) {
        this.shopQueryPort = shopQueryPort;
    }

    @Override
    public List<ShopMenuCollectionImageExposureResult> getMenuCollectionImages(Long shopId) {
        return shopQueryPort.findExposedMenuCollectionImages(shopId);
    }
}
