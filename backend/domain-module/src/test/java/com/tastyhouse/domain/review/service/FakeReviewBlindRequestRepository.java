package com.tastyhouse.domain.review.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestRepository;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * 게시중단 요청 write 포트의 인메모리 fake.
 *
 * <p>{@code save}가 신규 저장 시 <b>새 인스턴스를 반환</b>하는 것까지 실제 어댑터와 같게 재현한다 —
 * 호출부가 반환값을 재할당하지 않으면 이어지는 전이가 식별자 없는 객체에 적용돼 중복 insert가 되는데,
 * 그 결함은 fake가 in-place로 id를 채우면 테스트에서 드러나지 않는다.
 */
public class FakeReviewBlindRequestRepository implements ReviewBlindRequestRepository {

    /**
     * 1회 제한의 판정 대상 — {@code CANCELED}는 제외한다(실제 어댑터와 같은 목록).
     */
    private static final List<ReviewBlindStatus> TERMINATED_STATUSES = List.of(
        ReviewBlindStatus.APPROVED,
        ReviewBlindStatus.REJECTED,
        ReviewBlindStatus.EXPIRED,
        ReviewBlindStatus.DELETED
    );

    private final Map<Long, ReviewBlindRequest> requests = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Optional<ReviewBlindRequest> findById(ReviewBlindRequestId reviewBlindRequestId) {
        return Optional.ofNullable(requests.get(reviewBlindRequestId.value()));
    }

    @Override
    public boolean existsByReviewIdAndStatus(ReviewId reviewId, ReviewBlindStatus status) {
        return requests.values().stream().anyMatch(request ->
            request.getReviewId().equals(reviewId) && request.getStatus() == status);
    }

    @Override
    public boolean existsTerminatedByReviewId(ReviewId reviewId) {
        return requests.values().stream().anyMatch(request ->
            request.getReviewId().equals(reviewId) && TERMINATED_STATUSES.contains(request.getStatus()));
    }

    @Override
    public List<ReviewBlindRequest> findExpirableBlinds(LocalDateTime now) {
        return requests.values().stream()
            .filter(request -> request.getStatus() == ReviewBlindStatus.APPROVED)
            .filter(request -> request.getBlindUntil() != null && !request.getBlindUntil().isAfter(now))
            .sorted(Comparator.comparing(ReviewBlindRequest::getBlindUntil))
            .toList();
    }

    @Override
    public Optional<ReviewBlindRequest> findApprovedByReviewId(ReviewId reviewId) {
        return requests.values().stream()
            .filter(request -> request.getReviewId().equals(reviewId))
            .filter(request -> request.getStatus() == ReviewBlindStatus.APPROVED)
            .max(Comparator.comparing(ReviewBlindRequest::getId));
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
            reviewBlindRequest.getBlindUntil(),
            null
        );
        requests.put(persisted.getId(), persisted);
        return persisted;
    }
}
