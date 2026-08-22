package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 옵션그룹 합치기 추천 제외(append-only) 순수 도메인 모델.
 *
 * <p><b>그룹 id 쌍이 아니라 동일성 서명으로 저장한다</b> — 추천 기준은 쌍이 아니라 <b>동치류</b>이므로,
 * 쌍으로 저장하면 묶음 크기 n에 대해 O(n²) 행이 필요하고 멤버 하나가 빠진 같은 묶음이 다시 추천된다.
 *
 * <p>서명이 바뀌면(옵션명·가격 수정) <b>다시 추천되는 것이 의도된 동작</b>이다 — 점주가 제외한 것은
 * "이 정확한 중복 묶음"이지 "이 그룹 영구히"가 아니다.
 *
 * <p>append-only이므로 상태 전이 메서드를 두지 않는다(전 필드 {@code final}).
 */
public class ProductOptionGroupMergeExclusion {

    private final Long id;
    private final ShopId shopId;
    private final String groupSignature;
    private final CeoId actorCeoId;

    private ProductOptionGroupMergeExclusion(
        Long id,
        ShopId shopId,
        String groupSignature,
        CeoId actorCeoId
    ) {
        this.id = id;
        this.shopId = shopId;
        this.groupSignature = groupSignature;
        this.actorCeoId = actorCeoId;
    }

    public static ProductOptionGroupMergeExclusion of(
        ShopId shopId,
        String groupSignature,
        CeoId actorCeoId
    ) {
        return new ProductOptionGroupMergeExclusion(null, shopId, groupSignature, actorCeoId);
    }

    /** DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다. */
    public static ProductOptionGroupMergeExclusion reconstitute(
        Long id,
        ShopId shopId,
        String groupSignature,
        CeoId actorCeoId
    ) {
        return new ProductOptionGroupMergeExclusion(id, shopId, groupSignature, actorCeoId);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getGroupSignature() {
        return this.groupSignature;
    }

    public CeoId getActorCeoId() {
        return this.actorCeoId;
    }
}
