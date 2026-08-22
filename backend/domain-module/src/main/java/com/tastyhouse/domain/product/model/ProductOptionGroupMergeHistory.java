package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 옵션그룹 합치기 이력(append-only) 순수 도메인 모델.
 *
 * <p><b>왜 이력이 필요한가</b>: 합치기는 분리 불가이고, 흡수된 그룹은 링크가 기준 그룹으로 옮겨져
 * {@code findOwningShopId}가 {@code null}을 반환하게 된다 — 즉 <b>소유 가게 역조회가 영구히
 * 불가능</b>해진다. 이력이 없으면 "내 옵션그룹이 사라졌어요" 문의에 답할 근거가 하나도 남지 않는다.
 * 되돌리기 위한 데이터가 아니라 <b>감사·문의응대 전용</b>이다.
 *
 * <p>그래서 {@code shopId}와 {@code mergedGroupName}을 <b>이 행에 박제</b>한다 — 합치기 후에는
 * 그 둘을 어디서도 되짚을 수 없고, 이후 기준 그룹만 수정되므로 흡수 시점의 이름이 유일한 식별 단서다.
 *
 * <p>append-only이므로 상태 전이 메서드를 두지 않는다(전 필드 {@code final}).
 */
public class ProductOptionGroupMergeHistory {

    private final Long id;
    private final ShopId shopId;
    private final ProductOptionGroupId baseOptionGroupId;
    private final ProductOptionGroupId mergedOptionGroupId;
    private final String mergedGroupName;
    private final ProductOptionGroupMergeEntryType entryType;
    private final CeoId actorCeoId;

    private ProductOptionGroupMergeHistory(
        Long id,
        ShopId shopId,
        ProductOptionGroupId baseOptionGroupId,
        ProductOptionGroupId mergedOptionGroupId,
        String mergedGroupName,
        ProductOptionGroupMergeEntryType entryType,
        CeoId actorCeoId
    ) {
        this.id = id;
        this.shopId = shopId;
        this.baseOptionGroupId = baseOptionGroupId;
        this.mergedOptionGroupId = mergedOptionGroupId;
        this.mergedGroupName = mergedGroupName;
        this.entryType = entryType;
        this.actorCeoId = actorCeoId;
    }

    public static ProductOptionGroupMergeHistory of(
        ShopId shopId,
        ProductOptionGroupId baseOptionGroupId,
        ProductOptionGroupId mergedOptionGroupId,
        String mergedGroupName,
        ProductOptionGroupMergeEntryType entryType,
        CeoId actorCeoId
    ) {
        return new ProductOptionGroupMergeHistory(
            null, shopId, baseOptionGroupId, mergedOptionGroupId, mergedGroupName, entryType, actorCeoId
        );
    }

    /** DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다. */
    public static ProductOptionGroupMergeHistory reconstitute(
        Long id,
        ShopId shopId,
        ProductOptionGroupId baseOptionGroupId,
        ProductOptionGroupId mergedOptionGroupId,
        String mergedGroupName,
        ProductOptionGroupMergeEntryType entryType,
        CeoId actorCeoId
    ) {
        return new ProductOptionGroupMergeHistory(
            id, shopId, baseOptionGroupId, mergedOptionGroupId, mergedGroupName, entryType, actorCeoId
        );
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ProductOptionGroupId getBaseOptionGroupId() {
        return this.baseOptionGroupId;
    }

    public ProductOptionGroupId getMergedOptionGroupId() {
        return this.mergedOptionGroupId;
    }

    public String getMergedGroupName() {
        return this.mergedGroupName;
    }

    public ProductOptionGroupMergeEntryType getEntryType() {
        return this.entryType;
    }

    public CeoId getActorCeoId() {
        return this.actorCeoId;
    }
}
