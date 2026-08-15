package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 점주 공지 관리 화면(admin) 목록 항목 조회 결과.
 *
 * <p>ceo용 {@link ShopNoticeResult}와 필드 셋이 달라(가게명 포함, {@code updatedAt} 미포함) 별도
 * record로 둔다. 이름은 admin 마커 대신 {@code Management} 한정어로 구별한다.
 */
public record ShopNoticeManagementListItemResult(
    Long id,
    Long shopId,
    String shopName,
    String content,
    List<String> imageUrls,
    boolean exposed,
    boolean hidden,
    LocalDateTime createdAt
) {

}
