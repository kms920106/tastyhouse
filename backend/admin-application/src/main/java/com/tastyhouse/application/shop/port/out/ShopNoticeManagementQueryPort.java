package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 가게 공지 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>전체 가게의 공지를 가게명·숨김 여부로 검색하는 관리 목록을 조회한다. 회원 노출 조회는
 * {@link ShopNoticeQueryPort}, 점주 관리 조회는 {@code ShopNoticeOwnerQueryPort}가 소유한다.
 */
public interface ShopNoticeManagementQueryPort {

    PageResult<ShopNoticeManagementListItemResult> findNoticePage(Long shopId, String shopName, Boolean hidden, PageQuery pageQuery);
}
