package com.tastyhouse.application.menureview.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.application.menureview.port.in.MenuReviewDeleteCommand;
import com.tastyhouse.application.menureview.port.in.MenuReviewDeleteUseCase;

@Service
@Transactional
class MenuReviewDeleteService implements MenuReviewDeleteUseCase {

    private final MenuReviewLifecycleService menuReviewLifecycleService;

    public MenuReviewDeleteService(MenuReviewLifecycleService menuReviewLifecycleService) {
        this.menuReviewLifecycleService = menuReviewLifecycleService;
    }

    @Override
    public void deleteMenuReview(MenuReviewDeleteCommand command) {
        MenuReviewId menuReviewId = MenuReviewId.of(command.menuReviewId());
        menuReviewLifecycleService.remove(menuReviewId, MemberId.of(command.memberId()));
    }
}
