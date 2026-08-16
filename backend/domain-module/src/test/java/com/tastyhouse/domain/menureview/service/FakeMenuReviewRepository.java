package com.tastyhouse.domain.menureview.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.menureview.repository.MenuReviewRepository;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.vo.OrderProductId;

/**
 * 메뉴 평가 write 포트의 인메모리 fake.
 *
 * <p>{@code save}는 실제 어댑터와 같은 시맨틱을 흉내낸다 — id가 없으면 시퀀스를 채워 재구성하고, 있으면
 * 그대로 보존한다.
 */
class FakeMenuReviewRepository implements MenuReviewRepository {

    private final Map<Long, MenuReview> menuReviews = new LinkedHashMap<>();
    private long sequence;

    @Override
    public Optional<MenuReview> findById(MenuReviewId menuReviewId) {
        return Optional.ofNullable(menuReviews.get(menuReviewId.value()));
    }

    @Override
    public Optional<MenuReview> findByIdAndMemberId(MenuReviewId menuReviewId, MemberId memberId) {
        return findById(menuReviewId)
            .filter(menuReview -> menuReview.getMemberId().equals(memberId));
    }

    @Override
    public boolean existsByOrderProductId(OrderProductId orderProductId) {
        return menuReviews.values().stream()
            .anyMatch(menuReview -> menuReview.getOrderProductId().equals(orderProductId));
    }

    @Override
    public MenuReview save(MenuReview menuReview) {
        if (menuReview.getId() != null) {
            menuReviews.put(menuReview.getId(), menuReview);
            return menuReview;
        }

        MenuReview persisted = MenuReview.reconstitute(
            ++sequence,
            menuReview.getMemberId(),
            menuReview.getShopId(),
            menuReview.getProductId(),
            menuReview.getOrderId(),
            menuReview.getOrderProductId(),
            menuReview.getRating(),
            menuReview.getComment(),
            menuReview.isHidden(),
            null,
            null
        );
        menuReviews.put(persisted.getId(), persisted);
        return persisted;
    }

    @Override
    public void deleteById(MenuReviewId menuReviewId) {
        menuReviews.remove(menuReviewId.value());
    }
}
