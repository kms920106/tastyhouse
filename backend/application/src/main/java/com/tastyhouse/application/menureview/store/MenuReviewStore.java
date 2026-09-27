package com.tastyhouse.application.menureview.store;

import java.util.Optional;

import com.tastyhouse.application.menureview.port.out.write.MenuReviewStatePort;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.vo.OrderProductId;

public class MenuReviewStore implements MenuReviewRepository {
    private final MenuReviewStatePort menuReviewStatePort;

    public MenuReviewStore(MenuReviewStatePort menuReviewStatePort) {
        this.menuReviewStatePort = menuReviewStatePort;
    }

    @Override
    public Optional<MenuReview> findById(MenuReviewId menuReviewId) {
        return menuReviewStatePort.findById(menuReviewId.value()).map(MenuReviewStateMapper::toDomain);
    }

    @Override
    public Optional<MenuReview> findByIdAndMemberId(MenuReviewId menuReviewId, MemberId memberId) {
        return menuReviewStatePort.findByIdAndMemberId(menuReviewId.value(), memberId.value())
            .map(MenuReviewStateMapper::toDomain);
    }

    @Override
    public boolean existsByOrderProductId(OrderProductId orderProductId) {
        return menuReviewStatePort.existsByOrderProductId(orderProductId.value());
    }

    @Override
    public MenuReview save(MenuReview menuReview) {
        return MenuReviewStateMapper.toDomain(menuReviewStatePort.save(MenuReviewStateMapper.toState(menuReview)));
    }

    @Override
    public void deleteById(MenuReviewId menuReviewId) {
        menuReviewStatePort.deleteById(menuReviewId.value());
    }
}
