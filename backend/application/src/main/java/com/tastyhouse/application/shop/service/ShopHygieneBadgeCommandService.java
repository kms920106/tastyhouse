package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeDeleteCommand;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.HygieneBadgeType;
import com.tastyhouse.domain.shop.model.ShopHygieneBadge;
import com.tastyhouse.domain.shop.repository.ShopHygieneBadgeRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class ShopHygieneBadgeCommandService implements ShopHygieneBadgeCommandUseCase {

    private final ShopHygieneBadgeRepository shopHygieneBadgeRepository;

    public ShopHygieneBadgeCommandService(ShopHygieneBadgeRepository shopHygieneBadgeRepository) {
        this.shopHygieneBadgeRepository = shopHygieneBadgeRepository;
    }

    @Override
    public Long createHygieneBadge(ShopHygieneBadgeCreateCommand command) {
        Long shopId = command.shopId();
        String badgeType = command.badgeType();
        LocalDate certifiedDate = command.certifiedDate();
        String lastInspectionMonth = command.lastInspectionMonth();

        ShopHygieneBadge saved = shopHygieneBadgeRepository.save(
            ShopHygieneBadge.of(
                ShopId.of(shopId),
                HygieneBadgeType.from(badgeType),
                certifiedDate,
                lastInspectionMonth
            )
        );
        return saved.getId();
    }

    @Override
    public void deleteHygieneBadge(ShopHygieneBadgeDeleteCommand command) {
        Long hygieneBadgeId = command.hygieneBadgeId();
        shopHygieneBadgeRepository.findById(hygieneBadgeId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_HYGIENE_BADGE_NOT_FOUND));
        shopHygieneBadgeRepository.deleteById(hygieneBadgeId);
    }
}
