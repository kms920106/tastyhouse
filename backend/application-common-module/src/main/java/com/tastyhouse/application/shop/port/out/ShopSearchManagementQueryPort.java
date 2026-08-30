package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 가게 목록 관리 조회 포트(CQRS query 측 아웃바운드 포트) — 점주·관리자 화면 공용.
 *
 * <p>검색 조건으로 가게를 찾는 관리 목록을 조회한다. 회원 탐색 조회는
 * {@link ShopSearchQueryPort}가 소유한다.
 *
 * <p>점주 화면(자기 소유 가게 목록)과 관리자 화면(전체 가게 목록)이 <b>같은 메서드 하나만</b> 쓰므로
 * 소비자별로 쪼개면 두 인터페이스가 완전히 같아진다(규칙 3). 조회 범위의 차이는 인터페이스가 아니라
 * {@link ShopSearchCondition}이 담는다.
 */
public interface ShopSearchManagementQueryPort {

    PageResult<ShopListItemResult> findShops(ShopSearchCondition condition, PageQuery pageQuery);
}
