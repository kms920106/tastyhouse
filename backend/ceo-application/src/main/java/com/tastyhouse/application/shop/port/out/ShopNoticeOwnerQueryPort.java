package com.tastyhouse.application.shop.port.out;

import java.util.List;

/**
 * 가게 공지 점주 관리 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>점주가 자기 가게에 등록한 공지 전체를 조회한다(비노출 포함). 회원 노출 조회는
 * {@link ShopNoticeQueryPort}, 관리자 검수 조회는 {@link ShopNoticeManagementQueryPort}가 소유한다.
 *
 * <p>관리자 쪽이 {@code ShopNoticeManagementListItemResult}를 반환해 이미 {@code Management} 한정어를
 * 쓰고 있으므로, 점주 관리 조회는 소유 주체를 담은 {@code Owner}로 구별한다.
 */
public interface ShopNoticeOwnerQueryPort {

    List<ShopNoticeResult> findNotices(Long shopId);
}
