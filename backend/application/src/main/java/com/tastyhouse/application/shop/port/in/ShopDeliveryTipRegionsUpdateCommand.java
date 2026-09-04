package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 지역별 추가 배달팁 일괄 교체 command. 빈 목록은 "전부 삭제"를 뜻하는 정상 입력이다.
 */
public record ShopDeliveryTipRegionsUpdateCommand(
    Long ceoId,
    Long shopId,
    List<ShopDeliveryTipRegionCommand> regions
) {
    public ShopDeliveryTipRegionsUpdateCommand {
        if (ceoId == null || shopId == null || regions == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
