package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;
import com.tastyhouse.application.member.port.out.MemberDeliveryAddressQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopBestListQueryUseCase;
import com.tastyhouse.application.shop.port.out.BestShopItemResult;
import com.tastyhouse.application.shop.port.out.ShopBestListItemViewResult;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;

@Service
@Transactional(readOnly = true)
class ShopBestListQueryService implements ShopBestListQueryUseCase {

    private final ShopSearchQueryPort shopSearchQueryPort;
    private final MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort;
    private final ShopOperatingStatusService shopOperatingStatusService;

    public ShopBestListQueryService(
        ShopSearchQueryPort shopSearchQueryPort,
        MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort,
        ShopOperatingStatusService shopOperatingStatusService
    ) {
        this.shopSearchQueryPort = shopSearchQueryPort;
        this.memberDeliveryAddressQueryPort = memberDeliveryAddressQueryPort;
        this.shopOperatingStatusService = shopOperatingStatusService;
    }

    @Override
    public PageResult<ShopBestListItemViewResult> searchBestShops(Long memberId, int page, int size) {
        PageResult<BestShopItemResult> result =
            shopSearchQueryPort.findBestShops(resolveDeliveryAdminDongId(memberId), PageQuery.of(page, size));
        Map<Long, ShopOperatingStatus> statusMap = resolveOperatingStatuses(
            result.content().stream().map(BestShopItemResult::id).toList()
        );
        return result.map(dto -> convertToBestShopListItemResult(dto, statusMap));
    }

    private Long resolveDeliveryAdminDongId(Long memberId) {
        if (memberId == null) {
            return null;
        }

        return memberDeliveryAddressQueryPort.findDefaultAdminDongId(MemberId.of(memberId).value()).orElse(null);
    }

    private Map<Long, ShopOperatingStatus> resolveOperatingStatuses(List<Long> shopIds) {
        return shopOperatingStatusService.findOperatingStatuses(shopIds, LocalDateTime.now());
    }

    private String operatingStatusName(Map<Long, ShopOperatingStatus> statusMap, Long shopId) {
        ShopOperatingStatus status = statusMap.get(shopId);
        return status == null ? null : status.name();
    }

    private ShopBestListItemViewResult convertToBestShopListItemResult(BestShopItemResult dto, Map<Long, ShopOperatingStatus> statusMap) {
        return new ShopBestListItemViewResult(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.rating(),
            dto.imageUrl(),
            dto.foodTypes(),
            operatingStatusName(statusMap, dto.id()),
            dto.minOrderAmount(),
            dto.minDeliveryTip(),
            dto.maxDeliveryTip()
        );
    }
}
