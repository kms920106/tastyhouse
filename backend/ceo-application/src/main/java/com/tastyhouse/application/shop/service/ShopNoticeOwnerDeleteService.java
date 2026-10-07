package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopNoticeDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopNoticeOwnerDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeImagePersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopNoticePersistencePort;

@Service
@Transactional
class ShopNoticeOwnerDeleteService implements ShopNoticeOwnerDeleteUseCase {

    private final ShopNoticePersistencePort shopNoticePersistencePort;
    private final ShopNoticeImagePersistencePort shopNoticeImagePersistencePort;
    private final ShopNoticeOwnerReader shopNoticeOwnerReader;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopNoticeOwnerDeleteService(
        ShopNoticePersistencePort shopNoticePersistencePort,
        ShopNoticeImagePersistencePort shopNoticeImagePersistencePort,
        ShopNoticeOwnerReader shopNoticeOwnerReader,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopNoticePersistencePort = shopNoticePersistencePort;
        this.shopNoticeImagePersistencePort = shopNoticeImagePersistencePort;
        this.shopNoticeOwnerReader = shopNoticeOwnerReader;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public void deleteNotice(ShopNoticeDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long noticeId = command.noticeId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopNotice notice = shopNoticeOwnerReader.loadOwnedNotice(shopId, noticeId);
        String previousValue = describeNotice(notice);

        shopNoticeImagePersistencePort.deleteByShopNoticeId(noticeId);
        shopNoticePersistencePort.deleteById(noticeId);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.NOTICE,
            ShopChangeActionType.DELETE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            null
        );
    }

    private String describeNotice(ShopNotice notice) {
        String label = notice.isExposed() ? "노출중" : "미노출";
        return label + ": " + notice.getContent();
    }
}
