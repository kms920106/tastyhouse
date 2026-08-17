package com.tastyhouse.domain.review.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.repository.ShopRequestIndexRepository;

/**
 * 요청처리 현황 인덱스 write 포트의 인메모리 fake.
 *
 * <p>shop 테스트 패키지에도 같은 목적의 fake가 있으나 그쪽은 package-private이라 재사용할 수 없다.
 * 가시성을 넓히는 대신 이 패키지에 별도로 둔다 — 노출 범위를 좁게 유지하는 기존 판단을 존중하기 위함이다.
 *
 * <p>{@code save}가 신규 저장 시 식별자를 채운 <b>새 인스턴스</b>를 반환하는 것까지 실제 어댑터와 같게
 * 재현한다.
 */
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

    /**
     * 인덱스 행을 꺼낸다. 없으면 즉시 실패시켜 "동기화가 아예 안 된" 결함이 조용히 통과하지 않게 한다.
     */
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

        // 파라미터 순서를 선언 순서와 한 개씩 대조한다 — Long·String·LocalDateTime이 연속해 있어
        // 자리를 바꿔도 컴파일되고 값만 조용히 뒤바뀐다(DTO 조립 규칙의 경고).
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
