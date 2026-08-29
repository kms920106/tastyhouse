package com.tastyhouse.webapi.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageQuery;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.infrastructure.shop.query.ShopSearchQueryDao;
import com.tastyhouse.webapi.member.response.ShopBookmarkListItemResponse;

/**
 * 회원의 즐겨찾기 가게 목록 조회 서비스.
 *
 * <p>CQRS 전환 후 core application 서비스 대신 infra query DAO를 직접 주입한다.
 */
@Service
public class MemberShopService {

    private final ShopSearchQueryDao shopSearchQueryDao;

    public MemberShopService(ShopSearchQueryDao shopSearchQueryDao) {
        this.shopSearchQueryDao = shopSearchQueryDao;
    }

    @Transactional(readOnly = true)
    public PaginationResponse<ShopBookmarkListItemResponse> getMyBookmarkedShops(Long memberId, int page, int size) {
        return PaginationResponse.from(shopSearchQueryDao.findMyBookmarkedShops(memberId, PageQuery.of(page, size))
            .map(dto -> ShopBookmarkListItemResponse.from(
                dto.shopId(),
                dto.bookmarkId(),
                dto.shopName(),
                dto.stationName(),
                dto.rating(),
                dto.imageUrl(),
                dto.bookmarked()
            )));
    }
}
