package com.tastyhouse.application.shop.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.HygieneBadgeType;
import com.tastyhouse.domain.shop.model.ShopHygieneBadge;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopHygieneBadgePersistencePort;

@Service
@Transactional
class ShopHygieneBadgeCreateService implements ShopHygieneBadgeCreateUseCase {

    private final ShopHygieneBadgePersistencePort shopHygieneBadgePersistencePort;

    public ShopHygieneBadgeCreateService(ShopHygieneBadgePersistencePort shopHygieneBadgePersistencePort) {
        this.shopHygieneBadgePersistencePort = shopHygieneBadgePersistencePort;
    }

    @Override
    public Long createHygieneBadge(ShopHygieneBadgeCreateCommand command) {
        Long shopId = command.shopId();
        String badgeType = command.badgeType();
        LocalDate certifiedDate = command.certifiedDate();
        String lastInspectionMonth = command.lastInspectionMonth();

        ShopHygieneBadge saved = shopHygieneBadgePersistencePort.save(
            ShopHygieneBadge.of(
                ShopId.of(shopId),
                HygieneBadgeType.from(badgeType),
                certifiedDate,
                lastInspectionMonth
            )
        );
        return saved.getId();
    }
}
