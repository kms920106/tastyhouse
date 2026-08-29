package com.tastyhouse.webapi.shop.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.shop.query.ShopOriginInfoResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopOriginInfoResponse;

/**
 * 손님용 가게 원산지 표시 조회 서비스(CQRS query 측).
 *
 * <p>대형 {@code ShopQueryService}에 메서드를 얹지 않고 별도 서비스로 둔다 — 원산지는 단일 행 조회로
 * 조립할 것이 없어 그 클래스의 다수 협력 빈을 하나도 쓰지 않는다({@code ShopOrderNoticeQueryService}가
 * 같은 판단을 따른다).
 *
 * <p>미설정이면 {@code null}을 돌려준다({@code data: null}) — 점주 화면은 빈 폼을 그려야 해서 기본값을
 * 받지만, 손님 화면은 원산지 영역을 통째로 감추면 되므로 빈 객체가 필요하지 않다.
 */
@Service
@Transactional(readOnly = true)
public class ShopOriginInfoQueryService {

    private final ShopQueryDao shopQueryDao;

    public ShopOriginInfoQueryService(ShopQueryDao shopQueryDao) {
        this.shopQueryDao = shopQueryDao;
    }

    public ShopOriginInfoResponse getOriginInfo(Long shopId) {
        return shopQueryDao.findOriginInfo(shopId)
            .map(this::toShopOriginInfoResponse)
            .orElse(null);
    }

    private ShopOriginInfoResponse toShopOriginInfoResponse(ShopOriginInfoResult dto) {
        return ShopOriginInfoResponse.from(
            dto.sourceType(),
            dto.content(),
            dto.url()
        );
    }
}
