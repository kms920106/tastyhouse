package com.tastyhouse.ceoapi.shop.application.port.in;

import java.util.List;

/**
 * 점주 가게 영업 임시중지 쓰기 인바운드 포트.
 */
public interface ShopSuspensionCommandUseCase {

    List<Long> createSuspension(ShopSuspensionCreateCommand command);

    void releaseSuspension(ShopSuspensionReleaseCommand command);

    List<Long> createSuspensionsBulk(ShopSuspensionBulkCreateCommand command);
}
