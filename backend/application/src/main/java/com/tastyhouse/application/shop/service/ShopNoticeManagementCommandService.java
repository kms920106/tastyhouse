package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shop.port.in.ShopNoticeManagementCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopNoticeHideCommand;
import com.tastyhouse.application.shop.port.in.ShopNoticeUnhideCommand;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.repository.ShopNoticeRepository;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class ShopNoticeManagementCommandService implements ShopNoticeManagementCommandUseCase {

    private final ShopNoticeRepository shopNoticeRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopNoticeManagementCommandService(
        ShopNoticeRepository shopNoticeRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopNoticeRepository = shopNoticeRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public void hideNotice(ShopNoticeHideCommand command) {
        Long adminId = command.adminId();
        Long noticeId = command.noticeId();
        String reason = command.reason();
        ShopNotice notice = loadNotice(noticeId);
        if (notice.isHidden()) {
            throw new BusinessException(ErrorCode.SHOP_NOTICE_ALREADY_HIDDEN);
        }

        notice.hide();
        shopNoticeRepository.save(notice);

        shopChangeHistoryRecorder.record(
            notice.getShopId(),
            ShopChangeType.NOTICE,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.admin(adminId),
            "게시중",
            "게시중단: " + reason
        );
    }

    @Override
    public void unhideNotice(ShopNoticeUnhideCommand command) {
        Long adminId = command.adminId();
        Long noticeId = command.noticeId();
        ShopNotice notice = loadNotice(noticeId);
        if (!notice.isHidden()) {
            throw new BusinessException(ErrorCode.SHOP_NOTICE_NOT_HIDDEN);
        }

        notice.unhide();
        shopNoticeRepository.save(notice);

        shopChangeHistoryRecorder.record(
            notice.getShopId(),
            ShopChangeType.NOTICE,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.admin(adminId),
            "게시중단",
            "게시중"
        );
    }

    private ShopNotice loadNotice(Long noticeId) {
        return shopNoticeRepository.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOTICE_NOT_FOUND));
    }
}
