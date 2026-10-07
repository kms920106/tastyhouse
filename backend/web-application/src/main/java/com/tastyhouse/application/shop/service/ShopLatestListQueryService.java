package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.FoodType;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;
import com.tastyhouse.application.member.port.out.MemberDeliveryAddressQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopLatestListQueryUseCase;
import com.tastyhouse.application.shop.port.out.LatestShopItemResult;
import com.tastyhouse.application.shop.port.out.ShopLatestListItemViewResult;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;

@Service
@Transactional(readOnly = true)
class ShopLatestListQueryService implements ShopLatestListQueryUseCase {

    private final ShopSearchQueryPort shopSearchQueryPort;
    private final MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort;
    private final ShopOperatingStatusService shopOperatingStatusService;

    public ShopLatestListQueryService(
        ShopSearchQueryPort shopSearchQueryPort,
        MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort,
        ShopOperatingStatusService shopOperatingStatusService
    ) {
        this.shopSearchQueryPort = shopSearchQueryPort;
        this.memberDeliveryAddressQueryPort = memberDeliveryAddressQueryPort;
        this.shopOperatingStatusService = shopOperatingStatusService;
    }

    @Override
    public PageResult<ShopLatestListItemViewResult> searchLatestShops(
        Long stationId,
        List<String> foodTypes,
        List<String> amenities,
        Long memberId,
        int page,
        int size
    ) {
        List<String> foodTypeFilters = foodTypes == null
            ? null
            : foodTypes.stream().map(FoodType::from).map(FoodType::name).toList();
        List<String> amenityFilters = amenities == null
            ? null
            : amenities.stream().map(Amenity::from).map(Amenity::name).toList();
        PageResult<LatestShopItemResult> result = shopSearchQueryPort.findLatestShops(
            stationId,
            foodTypeFilters,
            amenityFilters,
            resolveDeliveryAdminDongId(memberId),
            PageQuery.of(page, size)
        );
        Map<Long, ShopOperatingStatus> statusMap = resolveOperatingStatuses(
            result.content().stream().map(LatestShopItemResult::id).toList()
        );
        return result.map(dto -> convertToLatestShopListItemResult(dto, statusMap));
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

    private ShopLatestListItemViewResult convertToLatestShopListItemResult(LatestShopItemResult dto, Map<Long, ShopOperatingStatus> statusMap) {
        return new ShopLatestListItemViewResult(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.rating(),
            dto.imageUrl(),
            dto.createdAt(),
            dto.reviewCount(),
            dto.bookmarkCount(),
            dto.foodTypes(),
            operatingStatusName(statusMap, dto.id()),
            dto.minOrderAmount(),
            dto.minDeliveryTip(),
            dto.maxDeliveryTip()
        );
    }
}
