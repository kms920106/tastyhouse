package com.tastyhouse.domain.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.repository.ShopRequestIndexRepository;

class RecordingShopRequestIndexRepository implements ShopRequestIndexRepository {
    private final List<ShopRequestIndex> store = new ArrayList<>();
    private long sequence = 0L;

    @Override
    public ShopRequestIndex save(ShopRequestIndex shopRequestIndex) {
        if (shopRequestIndex.getId() != null) {
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
            null,
            null
        );
        store.add(persisted);
        return persisted;
    }

    @Override
    public Optional<ShopRequestIndex> findById(Long id) {
        return store.stream()
            .filter(index -> id.equals(index.getId()))
            .findFirst();
    }

    @Override
    public Optional<ShopRequestIndex> findByRequestTypeAndSourceRequestId(
        ShopRequestType requestType,
        Long sourceRequestId
    ) {
        return store.stream()
            .filter(index -> index.getRequestType() == requestType
                && sourceRequestId.equals(index.getSourceRequestId()))
            .findFirst();
    }

    ShopRequestIndex require(ShopRequestType requestType, Long sourceRequestId) {
        return findByRequestTypeAndSourceRequestId(requestType, sourceRequestId)
            .orElseThrow(() -> new AssertionError(
                "인덱스 행이 없다(배선 누락): " + requestType + " #" + sourceRequestId));
    }
}
