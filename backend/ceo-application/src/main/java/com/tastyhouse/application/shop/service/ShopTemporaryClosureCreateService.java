package com.tastyhouse.application.shop.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shop.port.in.ShopTemporaryClosureCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopTemporaryClosureCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureSavePort;

@Service
@Transactional
class ShopTemporaryClosureCreateService implements ShopTemporaryClosureCreateUseCase {

    private static final long MAX_ACCUMULATED_CLOSURE_DAYS = 30;

    private final ShopTemporaryClosureLoadPort shopTemporaryClosureLoadPort;
    private final ShopTemporaryClosureSavePort shopTemporaryClosureSavePort;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopTemporaryClosureCreateService(
        ShopTemporaryClosureLoadPort shopTemporaryClosureLoadPort,
        ShopTemporaryClosureSavePort shopTemporaryClosureSavePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopTemporaryClosureLoadPort = shopTemporaryClosureLoadPort;
        this.shopTemporaryClosureSavePort = shopTemporaryClosureSavePort;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long createTemporaryClosure(ShopTemporaryClosureCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        LocalDate startDate = command.startDate();
        LocalDate endDate = command.endDate();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ShopTemporaryClosure temporaryClosure = ShopTemporaryClosure.of(targetShopId, startDate, endDate);

        long accumulatedDays = shopTemporaryClosureLoadPort.findByShopId(shopId).stream()
            .mapToLong(ShopTemporaryClosure::days)
            .sum();
        if (accumulatedDays + temporaryClosure.days() > MAX_ACCUMULATED_CLOSURE_DAYS) {
            throw new ApplicationException(CeoErrorCode.SHOP_TEMPORARY_CLOSURE_LIMIT_EXCEEDED);
        }

        ShopTemporaryClosure saved = shopTemporaryClosureSavePort.save(temporaryClosure);

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.TEMPORARY_CLOSURE,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeTemporaryClosure(saved)
        );
        return saved.getId();
    }

    private String describeTemporaryClosure(ShopTemporaryClosure temporaryClosure) {
        return ShopChangeValueFormatter.dateRange(temporaryClosure.getStartDate(), temporaryClosure.getEndDate());
    }
}
