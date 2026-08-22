package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 메뉴모음컷 검수요청 투영 — 관리자 검수 목록용.
 *
 * <p>{@code shopName}은 {@code SHOP_MENU_COLLECTION_IMAGE}에 없어 {@code SHOP}을 조인해 담는다 —
 * 관리자 검수 목록이 어느 가게 요청인지 보여야 하고, 가게 식별자만으로는 검수자가 판단할 수 없다.
 */
public record ShopMenuCollectionImageRequestResult(
    Long id,
    Long shopId,
    String shopName,
    String imageUrl,
    Integer sort,
    ApprovalStatus status,
    String rejectReason
) {
}
