package com.tastyhouse.testsupport.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexSavePort;

public class RecordingShopRequestIndexPersistence implements ShopRequestIndexLoadPort, ShopRequestIndexSavePort {

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

    public ShopRequestIndex require(ShopRequestType requestType, Long sourceRequestId) {
        return findByRequestTypeAndSourceRequestId(requestType, sourceRequestId)
            .orElseThrow(() -> new AssertionError(
                "인덱스 행이 없다(배선 누락): " + requestType + " #" + sourceRequestId));
    }
}
