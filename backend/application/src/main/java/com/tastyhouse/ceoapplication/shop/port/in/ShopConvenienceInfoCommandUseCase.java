package com.tastyhouse.ceoapplication.shop.port.in;

/**
 * 점주 가게 편의정보·편의시설 쓰기 인바운드 포트.
 */
public interface ShopConvenienceInfoCommandUseCase {

    void updateConvenienceInfo(ShopConvenienceInfoUpdateCommand command);

    Long assignAmenity(ShopAmenityOwnerAssignCommand command);

    void unassignAmenity(ShopAmenityOwnerUnassignCommand command);
}
