package com.tastyhouse.adminapi.shop;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.shop.domain.model.HygieneBadgeType;
import com.tastyhouse.core.domain.shop.application.ShopHygieneBadgeCommandService;
import com.tastyhouse.core.domain.shop.application.ShopHygieneBadgeQueryService;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopHygieneBadgeCreateCommand;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopHygieneBadgeResult;
import com.tastyhouse.adminapi.shop.response.ShopHygieneBadgeResponse;

/**
 * admin용 가게 위생 인증 뱃지 등록/조회/삭제 중개 서비스. 소유권 검증 없이 전체 가게를 대상으로 한다.
 */
@Service
@RequiredArgsConstructor
public class ShopHygieneBadgeAdminService {

    private final ShopHygieneBadgeCommandService shopHygieneBadgeCommandService;
    private final ShopHygieneBadgeQueryService shopHygieneBadgeQueryService;

    public List<ShopHygieneBadgeResponse> getHygieneBadges(Long shopId) {
        return shopHygieneBadgeQueryService.findByShopId(shopId).stream()
            .map(this::toShopHygieneBadgeResponse)
            .toList();
    }

    public ShopHygieneBadgeResponse createHygieneBadge(Long shopId, String badgeType, LocalDate certifiedDate, String lastInspectionMonth) {
        ShopHygieneBadgeCreateCommand command = ShopHygieneBadgeCreateCommand.of(
            shopId, HygieneBadgeType.from(badgeType), certifiedDate, lastInspectionMonth
        );
        ShopHygieneBadgeResult saved = shopHygieneBadgeCommandService.create(command);
        return toShopHygieneBadgeResponse(saved);
    }

    public void deleteHygieneBadge(Long hygieneBadgeId) {
        shopHygieneBadgeCommandService.delete(hygieneBadgeId);
    }

    private ShopHygieneBadgeResponse toShopHygieneBadgeResponse(ShopHygieneBadgeResult dto) {
        return ShopHygieneBadgeResponse.of(
            dto.id(),
            dto.shopId(),
            dto.badgeType().name(),
            dto.certifiedDate(),
            dto.lastInspectionMonth()
        );
    }
}
