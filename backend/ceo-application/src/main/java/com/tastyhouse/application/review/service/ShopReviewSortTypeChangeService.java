package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.review.port.in.ShopReviewSortTypeChangeCommand;
import com.tastyhouse.application.review.port.in.ShopReviewSortTypeChangeUseCase;
import com.tastyhouse.application.review.port.out.write.ShopReviewDisplaySettingLoadPort;
import com.tastyhouse.application.review.port.out.write.ShopReviewDisplaySettingSavePort;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ShopReviewSortTypeChangeService implements ShopReviewSortTypeChangeUseCase {

    private final ShopReviewDisplaySettingLoadPort shopReviewDisplaySettingLoadPort;
    private final ShopReviewDisplaySettingSavePort shopReviewDisplaySettingSavePort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopReviewSortTypeChangeService(
        ShopReviewDisplaySettingLoadPort shopReviewDisplaySettingLoadPort,
        ShopReviewDisplaySettingSavePort shopReviewDisplaySettingSavePort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopReviewDisplaySettingLoadPort = shopReviewDisplaySettingLoadPort;
        this.shopReviewDisplaySettingSavePort = shopReviewDisplaySettingSavePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void changeSortType(ShopReviewSortTypeChangeCommand command) {
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(command.ceoId(), shopId);

        ReviewSortType newSortType = ReviewSortType.from(command.sortType());
        ShopReviewDisplaySetting setting = shopReviewDisplaySettingLoadPort.findByShopId(ShopId.of(shopId))
            .orElseGet(() -> ShopReviewDisplaySetting.of(ShopId.of(shopId), newSortType));
        setting.changeSortType(newSortType);

        shopReviewDisplaySettingSavePort.save(setting);
    }
}
