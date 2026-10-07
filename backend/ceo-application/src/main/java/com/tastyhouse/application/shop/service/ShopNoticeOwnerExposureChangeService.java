package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopNoticeExposureChangeCommand;
import com.tastyhouse.application.shop.port.in.ShopNoticeOwnerExposureChangeUseCase;

@Service
@Transactional
class ShopNoticeOwnerExposureChangeService implements ShopNoticeOwnerExposureChangeUseCase {

    private final ShopNoticeExposureService shopNoticeExposureService;
    private final ShopNoticeOwnerReader shopNoticeOwnerReader;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopNoticeOwnerExposureChangeService(
        ShopNoticeExposureService shopNoticeExposureService,
        ShopNoticeOwnerReader shopNoticeOwnerReader,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopNoticeExposureService = shopNoticeExposureService;
        this.shopNoticeOwnerReader = shopNoticeOwnerReader;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public void changeExposure(ShopNoticeExposureChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long noticeId = command.noticeId();
        boolean exposed = command.exposed();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopNotice notice = shopNoticeOwnerReader.loadOwnedNotice(shopId, noticeId);
        String previousValue = describeNotice(notice);

        if (exposed) {
            shopNoticeExposureService.expose(ShopId.of(shopId), notice);
        } else {
            shopNoticeExposureService.unexpose(notice);
        }

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.NOTICE,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            describeNotice(notice)
        );
    }

    private String describeNotice(ShopNotice notice) {
        String label = notice.isExposed() ? "노출중" : "미노출";
        return label + ": " + notice.getContent();
    }
}
