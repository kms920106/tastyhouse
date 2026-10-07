package com.tastyhouse.application.menureview.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.application.menureview.port.in.MenuReviewUpdateCommand;
import com.tastyhouse.application.menureview.port.in.MenuReviewUpdateUseCase;

@Service
@Transactional
class MenuReviewUpdateService implements MenuReviewUpdateUseCase {

    private final MenuReviewLifecycleService menuReviewLifecycleService;

    public MenuReviewUpdateService(MenuReviewLifecycleService menuReviewLifecycleService) {
        this.menuReviewLifecycleService = menuReviewLifecycleService;
    }

    @Override
    public void updateMenuReview(MenuReviewUpdateCommand command) {
        MenuReviewId menuReviewId = MenuReviewId.of(command.menuReviewId());
        menuReviewLifecycleService.modify(menuReviewId, MemberId.of(command.memberId()), command.rating(), command.comment());
    }
}
