package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopNoticeUnhideCommand;
import com.tastyhouse.application.shop.port.in.ShopNoticeUnhideUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeSavePort;

@Service
@Transactional
class ShopNoticeUnhideService implements ShopNoticeUnhideUseCase {

    private final ShopNoticeLoadPort shopNoticeLoadPort;
    private final ShopNoticeSavePort shopNoticeSavePort;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopNoticeUnhideService(
        ShopNoticeLoadPort shopNoticeLoadPort,
        ShopNoticeSavePort shopNoticeSavePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopNoticeLoadPort = shopNoticeLoadPort;
        this.shopNoticeSavePort = shopNoticeSavePort;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public void unhideNotice(ShopNoticeUnhideCommand command) {
        Long adminId = command.adminId();
        Long noticeId = command.noticeId();
        ShopNotice notice = loadNotice(noticeId);
        if (!notice.isHidden()) {
            throw new ApplicationException(AdminErrorCode.SHOP_NOTICE_NOT_HIDDEN);
        }

        notice.unhide();
        shopNoticeSavePort.save(notice);

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
        return shopNoticeLoadPort.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOTICE_NOT_FOUND));
    }
}
