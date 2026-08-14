package com.tastyhouse.domain.review.domain.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestRepository;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 게시중단 요청 write 포트의 인메모리 fake.
 *
 * <p>{@code save}가 신규 저장 시 <b>새 인스턴스를 반환</b>하는 것까지 실제 어댑터와 같게 재현한다 —
 * 호출부가 반환값을 재할당하지 않으면 이어지는 전이가 식별자 없는 객체에 적용돼 중복 insert가 되는데,
 * 그 결함은 fake가 in-place로 id를 채우면 테스트에서 드러나지 않는다.
 */
public class FakeReviewBlindRequestRepository implements ReviewBlindRequestRepository {

    private final Map<Long, ReviewBlindRequest> requests = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Optional<ReviewBlindRequest> findById(ReviewBlindRequestId reviewBlindRequestId) {
        return Optional.ofNullable(requests.get(reviewBlindRequestId.value()));
    }

    @Override
    public boolean existsByReviewIdAndStatus(ReviewId reviewId, ApprovalStatus status) {
        return requests.values().stream().anyMatch(request ->
            request.getReviewId().equals(reviewId) && request.getStatus() == status);
    }

    @Override
    public ReviewBlindRequest save(ReviewBlindRequest reviewBlindRequest) {
        if (reviewBlindRequest.getId() != null) {
            requests.put(reviewBlindRequest.getId(), reviewBlindRequest);
            return reviewBlindRequest;
        }

        ReviewBlindRequest persisted = ReviewBlindRequest.reconstitute(
            ++sequence,
            reviewBlindRequest.getReviewId(),
            reviewBlindRequest.getShopId(),
            reviewBlindRequest.getCeoId(),
            reviewBlindRequest.getReason(),
            reviewBlindRequest.getDetailReason(),
            reviewBlindRequest.getStatus(),
            reviewBlindRequest.getRejectReason(),
            null
        );
        requests.put(persisted.getId(), persisted);
        return persisted;
    }
}
