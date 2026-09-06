package com.tastyhouse.domain.menureview.repository;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.vo.OrderProductId;

public interface MenuReviewRepository {
    Optional<MenuReview> findById(MenuReviewId menuReviewId);

    Optional<MenuReview> findByIdAndMemberId(MenuReviewId menuReviewId, MemberId memberId);

    boolean existsByOrderProductId(OrderProductId orderProductId);

    MenuReview save(MenuReview menuReview);

    void deleteById(MenuReviewId menuReviewId);
}
