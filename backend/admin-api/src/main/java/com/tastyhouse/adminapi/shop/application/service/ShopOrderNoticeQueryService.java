package com.tastyhouse.adminapi.shop.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.shop.query.ShopOrderNoticeQueryDao;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopOrderNoticeResponse;

/**
 * 관리자용 주문안내 조회 서비스(CQRS query 측).
 *
 * <p>게시중단 여부·사유를 그대로 내려준다 — 관리자가 게시중단/해제를 판단하려면 현재 문구와 사유를
 * 먼저 봐야 한다. 소유권 검증은 하지 않는다(관리자는 전 가게에 접근 가능).
 *
 * <p>CQRS 교차 주입 금지 규칙에 따라 write 포트를 주입하지 않는다.
 */
@Service
@Transactional(readOnly = true)
public class ShopOrderNoticeQueryService {

    private final ShopOrderNoticeQueryDao shopOrderNoticeQueryDao;

    public ShopOrderNoticeQueryService(ShopOrderNoticeQueryDao shopOrderNoticeQueryDao) {
        this.shopOrderNoticeQueryDao = shopOrderNoticeQueryDao;
    }

    /**
     * 가게의 주문안내를 조회한다. 미설정이면 {@code content}가 null인 빈 응답을 돌려준다.
     */
    public ShopOrderNoticeResponse getOrderNotice(Long shopId) {
        return shopOrderNoticeQueryDao.findOrderNotice(shopId)
            .map(result -> ShopOrderNoticeResponse.of(result.content(), result.hidden(), result.hiddenReason()))
            .orElseGet(ShopOrderNoticeResponse::empty);
    }
}
