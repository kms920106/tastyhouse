package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopTemporaryClosureDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopTemporaryClosureDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosurePersistencePort;

@Service
@Transactional
class ShopTemporaryClosureDeleteService implements ShopTemporaryClosureDeleteUseCase {

    private final ShopTemporaryClosurePersistencePort shopTemporaryClosurePersistencePort;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopTemporaryClosureDeleteService(
        ShopTemporaryClosurePersistencePort shopTemporaryClosurePersistencePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopTemporaryClosurePersistencePort = shopTemporaryClosurePersistencePort;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public void deleteTemporaryClosure(ShopTemporaryClosureDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long temporaryClosureId = command.temporaryClosureId();

        ShopTemporaryClosure temporaryClosure = shopTemporaryClosurePersistencePort.findById(temporaryClosureId)
            .orElseThrow(() -> new ResourceNotFoundException(CeoErrorCode.SHOP_TEMPORARY_CLOSURE_NOT_FOUND));
        shopTemporaryClosurePersistencePort.deleteById(temporaryClosureId);

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopChangeHistoryRecorder.record(
            temporaryClosure.getShopId(),
            ShopChangeType.TEMPORARY_CLOSURE,
            ShopChangeActionType.DELETE,
            actor,
            describeTemporaryClosure(temporaryClosure),
            null
        );
    }

    private String describeTemporaryClosure(ShopTemporaryClosure temporaryClosure) {
        return ShopChangeValueFormatter.dateRange(temporaryClosure.getStartDate(), temporaryClosure.getEndDate());
    }
}
