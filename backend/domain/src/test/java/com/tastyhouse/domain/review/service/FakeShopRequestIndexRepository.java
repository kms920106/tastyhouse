package com.tastyhouse.domain.review.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.repository.ShopRequestIndexRepository;

public class FakeShopRequestIndexRepository implements ShopRequestIndexRepository {
    private final Map<Long, ShopRequestIndex> indexes = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Optional<ShopRequestIndex> findById(Long id) {
        return Optional.ofNullable(indexes.get(id));
    }

    @Override
    public Optional<ShopRequestIndex> findByRequestTypeAndSourceRequestId(
        ShopRequestType requestType,
        Long sourceRequestId
    ) {
        return indexes.values().stream()
            .filter(index -> index.getRequestType() == requestType)
            .filter(index -> index.getSourceRequestId().equals(sourceRequestId))
            .findFirst();
    }

    public ShopRequestIndex require(ShopRequestType requestType, Long sourceRequestId) {
        return findByRequestTypeAndSourceRequestId(requestType, sourceRequestId)
            .orElseThrow(() -> new AssertionError(
                "인덱스 행이 없다: requestType=" + requestType + ", sourceRequestId=" + sourceRequestId));
    }

    @Override
    public ShopRequestIndex save(ShopRequestIndex shopRequestIndex) {
        if (shopRequestIndex.getId() != null) {
            indexes.put(shopRequestIndex.getId(), shopRequestIndex);
            return shopRequestIndex;
        }

        ShopRequestIndex persisted = ShopRequestIndex.reconstitute(
            ++sequence,
            shopRequestIndex.getShopId(),
            shopRequestIndex.getRequestType(),
            shopRequestIndex.getSourceRequestId(),
            shopRequestIndex.getSummary(),
            shopRequestIndex.getStatus(),
            shopRequestIndex.getRejectReason(),
            shopRequestIndex.getAttachmentFileId(),
            shopRequestIndex.getRequestedByCeoId(),
            shopRequestIndex.getProcessedAt(),
            shopRequestIndex.getCreatedAt(),
            null
        );
        indexes.put(persisted.getId(), persisted);
        return persisted;
    }
}
