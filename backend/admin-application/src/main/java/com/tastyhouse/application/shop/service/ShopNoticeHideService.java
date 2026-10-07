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
import com.tastyhouse.application.shop.port.in.ShopNoticeHideCommand;
import com.tastyhouse.application.shop.port.in.ShopNoticeHideUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopNoticePersistencePort;

@Service
@Transactional
class ShopNoticeHideService implements ShopNoticeHideUseCase {

    private final ShopNoticePersistencePort shopNoticePersistencePort;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopNoticeHideService(
        ShopNoticePersistencePort shopNoticePersistencePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopNoticePersistencePort = shopNoticePersistencePort;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public void hideNotice(ShopNoticeHideCommand command) {
        Long adminId = command.adminId();
        Long noticeId = command.noticeId();
        String reason = command.reason();
        ShopNotice notice = loadNotice(noticeId);
        if (notice.isHidden()) {
            throw new ApplicationException(AdminErrorCode.SHOP_NOTICE_ALREADY_HIDDEN);
        }

        notice.hide();
        shopNoticePersistencePort.save(notice);

        shopChangeHistoryRecorder.record(
            notice.getShopId(),
            ShopChangeType.NOTICE,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.admin(adminId),
            "게시중",
            "게시중단: " + reason
        );
    }

    private ShopNotice loadNotice(Long noticeId) {
        return shopNoticePersistencePort.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOTICE_NOT_FOUND));
    }
}
