package com.tastyhouse.webapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.shop.query.ShopOrderNoticeQueryDao;
import com.tastyhouse.webapi.shop.response.ShopOrderNoticeResponse;

/**
 * 손님용 주문안내 조회 서비스(CQRS query 측).
 *
 * <p>대형 {@code ShopQueryService}에 메서드를 얹지 않고 별도 서비스로 둔다 — 주문안내는 단일 행
 * 조회로 조립할 것이 없어 그 클래스의 다수 협력 빈을 하나도 쓰지 않고, 가게 상세 조립 경로와도
 * 얽히지 않는다.
 *
 * <p><b>게시중단된 문구는 내려가지 않는다.</b> 그 필터는 이 서비스의 분기가 아니라 DAO의
 * {@code findVisibleOrderNotice}가 쿼리 조건으로 강제한다 — Service에서 걸러내는 형태였다면 조건을
 * 빠뜨렸을 때 게시중단된 문구가 손님에게 그대로 노출되는데, 쿼리가 걸러내면 그 실수가 물리적으로
 * 불가능하다. 미설정도 게시중단도 결과가 같다({@code data: null}) — 손님 화면은 문구가 없으면 영역
 * 자체를 그리지 않으므로 두 상태를 구분할 필요가 없다.
 */
@Service
@Transactional(readOnly = true)
public class ShopOrderNoticeQueryService {

    private final ShopOrderNoticeQueryDao shopOrderNoticeQueryDao;

    public ShopOrderNoticeQueryService(ShopOrderNoticeQueryDao shopOrderNoticeQueryDao) {
        this.shopOrderNoticeQueryDao = shopOrderNoticeQueryDao;
    }

    /**
     * 가게의 주문안내를 조회한다. 미설정이거나 관리자 게시중단 상태면 {@code null}을 돌려준다
     * ({@code ApiResponse.data}가 null이 된다).
     */
    public ShopOrderNoticeResponse getOrderNotice(Long shopId) {
        return shopOrderNoticeQueryDao.findVisibleOrderNotice(shopId)
            .map(result -> ShopOrderNoticeResponse.of(result.content()))
            .orElse(null);
    }
}
