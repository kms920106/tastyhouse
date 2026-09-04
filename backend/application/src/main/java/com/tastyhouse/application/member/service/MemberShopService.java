package com.tastyhouse.application.member.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;

/**
 * 회원의 즐겨찾기 가게 목록 조회 서비스.
 *
 * <p>CQRS 전환 후 core application 서비스 대신 infra query DAO를 직접 주입한다.
 */
@Service
@WebApp
public class MemberShopService {

    private final ShopSearchQueryPort shopSearchQueryPort;

    public MemberShopService(ShopSearchQueryPort shopSearchQueryPort) {
        this.shopSearchQueryPort = shopSearchQueryPort;
    }

    @Transactional(readOnly = true)
    public PageResult<ShopBookmarkedItemResult> getMyBookmarkedShops(Long memberId, int page, int size) {
        return shopSearchQueryPort.findMyBookmarkedShops(memberId, PageQuery.of(page, size));
    }
}
