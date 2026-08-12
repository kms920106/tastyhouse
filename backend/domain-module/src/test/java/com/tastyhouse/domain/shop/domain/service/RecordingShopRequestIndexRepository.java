package com.tastyhouse.domain.shop.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.repository.ShopRequestIndexRepository;

/**
 * 요청 인덱스 write 포트 fake — 저장된 행을 들여다볼 수 있게 모아 둔다
 * ({@link RecordingShopChangeHistoryRepository} 선례. domain-module에는 Mockito 의존이 없어 fake를 손으로
 * 만든다).
 *
 * <p>{@code save}는 신규 행에 식별자를 부여해 영속 상태를 흉내 내고, 갱신 행은 <b>같은 인스턴스가
 * 이미 store에 있으므로 그대로 둔다</b> — 도메인 모델이 가변 필드({@code status} 등)를 갖고 있어
 * {@code syncStatus}가 store의 행을 직접 바꾼다.
 */
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

    /** 특정 유형·원본에 대응하는 인덱스 행. 배선 누락을 검증할 때 쓴다. */
    ShopRequestIndex require(ShopRequestType requestType, Long sourceRequestId) {
        return findByRequestTypeAndSourceRequestId(requestType, sourceRequestId)
            .orElseThrow(() -> new AssertionError(
                "인덱스 행이 없다(배선 누락): " + requestType + " #" + sourceRequestId));
    }
}
