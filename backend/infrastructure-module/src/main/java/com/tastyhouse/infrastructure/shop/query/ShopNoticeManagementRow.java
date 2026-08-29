package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopNoticeManagementListItemResult;
import java.time.LocalDateTime;

/**
 * 점주 공지 관리 화면 목록 투영 중간 결과(이미지 결합 전).
 *
 * <p>{@link ShopNoticeRow}와 같은 이유로 본문만 먼저 투영하고, 이미지 URL을 붙여
 * {@link ShopNoticeManagementListItemResult}로 재조립한다.
 */
public record ShopNoticeManagementRow(
    Long id,
    Long shopId,
    String shopName,
    String content,
    boolean exposed,
    boolean hidden,
    LocalDateTime createdAt
) {

}
