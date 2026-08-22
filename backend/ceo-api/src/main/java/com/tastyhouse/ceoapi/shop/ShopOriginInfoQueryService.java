package com.tastyhouse.ceoapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.OriginSourceType;
import com.tastyhouse.infrastructure.shop.query.ShopOriginInfoResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.ceoapi.shop.response.ShopOriginInfoResponse;

/**
 * 점주용 가게 원산지 표시 조회 서비스(CQRS query 측).
 *
 * <p>원산지가 아직 등록되지 않은 가게는 <b>빈 기본값 응답</b>을 돌려준다({@code sourceType=DIRECT}·
 * 나머지 null) — 화면이 "미설정" 분기 없이 빈 폼을 그릴 수 있게 하려는 것이다. 편의정보 조회가 같은
 * 방식을 쓴다.
 */
@Service
@Transactional(readOnly = true)
public class ShopOriginInfoQueryService {

    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopOriginInfoQueryService(ShopQueryDao shopQueryDao, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopQueryDao = shopQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public ShopOriginInfoResponse getOriginInfo(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopQueryDao.findOriginInfo(shopId)
            .map(this::toShopOriginInfoResponse)
            .orElseGet(() -> ShopOriginInfoResponse.from(OriginSourceType.DIRECT.name(), null, null, null));
    }

    private ShopOriginInfoResponse toShopOriginInfoResponse(ShopOriginInfoResult dto) {
        return ShopOriginInfoResponse.from(
            dto.sourceType(),
            dto.content(),
            dto.url(),
            dto.updatedAt()
        );
    }
}
