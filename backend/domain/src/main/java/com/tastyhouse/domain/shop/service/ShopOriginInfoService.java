package com.tastyhouse.domain.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.OriginSourceType;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopOriginInfo;
import com.tastyhouse.domain.shop.repository.ShopOriginInfoRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopOriginInfoService {
    private final ShopOriginInfoRepository shopOriginInfoRepository;
    private final ShopRepository shopRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopOriginInfoService(
        ShopOriginInfoRepository shopOriginInfoRepository,
        ShopRepository shopRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopOriginInfoRepository = shopOriginInfoRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopRepository = shopRepository;
    }

    public Optional<ShopOriginInfo> findByShopId(Long shopId) {
        return shopOriginInfoRepository.findByShopId(shopId);
    }

    public void upsertOriginInfo(
        Long shopId,
        OriginSourceType sourceType,
        String content,
        String url,
        ShopChangeActor actor
    ) {
        validateShopExists(shopId);

        ShopOriginInfo existing = shopOriginInfoRepository.findByShopId(shopId).orElse(null);
        String previousValue = describeOriginInfo(existing);

        ShopOriginInfo shopOriginInfo;
        if (existing == null) {
            shopOriginInfo = ShopOriginInfo.of(ShopId.of(shopId), sourceType, content, url);
        } else {
            existing.update(sourceType, content, url);
            shopOriginInfo = existing;
        }

        shopOriginInfoRepository.save(shopOriginInfo);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.ORIGIN_INFO,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeOriginInfo(shopOriginInfo)
        );
    }

    private void validateShopExists(Long shopId) {
        shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    private String describeOriginInfo(ShopOriginInfo originInfo) {
        if (originInfo == null) {
            return ShopChangeValueFormatter.snapshot(List.of());
        }

        List<String> lines = new ArrayList<>(2);
        lines.add("입력방식: " + originInfo.getSourceType().getDescription());
        if (originInfo.getSourceType() == OriginSourceType.DIRECT) {
            lines.add("원산지: " + valueOrUnset(originInfo.getContent()));
        } else {
            lines.add("URL: " + valueOrUnset(originInfo.getUrl()));
        }
        return ShopChangeValueFormatter.snapshot(lines);
    }

    private String valueOrUnset(String value) {
        return value == null || value.isBlank() ? ShopChangeValueFormatter.unset() : value;
    }
}
