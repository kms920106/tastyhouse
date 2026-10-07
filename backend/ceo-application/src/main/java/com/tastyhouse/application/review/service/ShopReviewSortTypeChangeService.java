package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.review.port.in.ShopReviewSortTypeChangeCommand;
import com.tastyhouse.application.review.port.in.ShopReviewSortTypeChangeUseCase;
import com.tastyhouse.application.review.port.out.write.ShopReviewDisplaySettingPersistencePort;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ShopReviewSortTypeChangeService implements ShopReviewSortTypeChangeUseCase {

    private final ShopReviewDisplaySettingPersistencePort shopReviewDisplaySettingPersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopReviewSortTypeChangeService(
        ShopReviewDisplaySettingPersistencePort shopReviewDisplaySettingPersistencePort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopReviewDisplaySettingPersistencePort = shopReviewDisplaySettingPersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void changeSortType(ShopReviewSortTypeChangeCommand command) {
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(command.ceoId(), shopId);

        ReviewSortType newSortType = ReviewSortType.from(command.sortType());
        ShopReviewDisplaySetting setting = shopReviewDisplaySettingPersistencePort.findByShopId(ShopId.of(shopId))
            .orElseGet(() -> ShopReviewDisplaySetting.of(ShopId.of(shopId), newSortType));
        setting.changeSortType(newSortType);

        shopReviewDisplaySettingPersistencePort.save(setting);
    }
}
