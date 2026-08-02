package com.tastyhouse.domain.shop.domain.model;

import lombok.Getter;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;

/**
 * 음식 유형 카테고리 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopFoodTypeCategoryJpaEntity} + {@code ShopFoodTypeCategoryMapper}가 담당한다.
 */
@Getter
public class ShopFoodTypeCategory {

    private final Long id;
    private final FoodType foodType;
    private String displayName;
    private UploadedFileId activeImageFileId;
    private UploadedFileId inactiveImageFileId;
    private Integer sort;
    private boolean visible;

    private ShopFoodTypeCategory(
        Long id,
        FoodType foodType,
        String displayName,
        UploadedFileId activeImageFileId,
        UploadedFileId inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        this.id = id;
        this.foodType = foodType;
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public static ShopFoodTypeCategory of(
        FoodType foodType,
        String displayName,
        UploadedFileId activeImageFileId,
        UploadedFileId inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopFoodTypeCategory(null, foodType, displayName, activeImageFileId, inactiveImageFileId, sort, visible);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopFoodTypeCategory reconstitute(
        Long id,
        FoodType foodType,
        String displayName,
        UploadedFileId activeImageFileId,
        UploadedFileId inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopFoodTypeCategory(id, foodType, displayName, activeImageFileId, inactiveImageFileId, sort, visible);
    }

    public void update(String displayName, UploadedFileId activeImageFileId, UploadedFileId inactiveImageFileId, Integer sort, boolean visible) {
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }
}
