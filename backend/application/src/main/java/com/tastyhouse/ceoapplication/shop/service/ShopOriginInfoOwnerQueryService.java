package com.tastyhouse.ceoapplication.shop.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopOriginInfoOwnerQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;

/**
 * 점주용 가게 원산지 표시 조회 서비스(CQRS query 측).
 *
 * <p>원산지가 아직 등록되지 않은 가게는 <b>빈 기본값 응답</b>을 돌려준다({@code sourceType=DIRECT}·
 * 나머지 null) — 화면이 "미설정" 분기 없이 빈 폼을 그릴 수 있게 하려는 것이다. 편의정보 조회가 같은
 * 방식을 쓴다.
 */
@Service
@Transactional(readOnly = true)
public class ShopOriginInfoOwnerQueryService implements ShopOriginInfoOwnerQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopOriginInfoOwnerQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Optional<ShopOriginInfoResult> getOriginInfo(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findOriginInfo(shopId);
    }

}
