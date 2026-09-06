package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ShopReviewCommandUseCase;
import com.tastyhouse.application.review.port.in.ShopReviewSortTypeChangeCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.review.repository.ShopReviewDisplaySettingRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ShopReviewCommandService implements ShopReviewCommandUseCase {

    private final ShopReviewDisplaySettingRepository shopReviewDisplaySettingRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopReviewCommandService(
        ShopReviewDisplaySettingRepository shopReviewDisplaySettingRepository,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopReviewDisplaySettingRepository = shopReviewDisplaySettingRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void changeSortType(ShopReviewSortTypeChangeCommand command) {
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(command.ceoId(), shopId);

        ReviewSortType newSortType = ReviewSortType.from(command.sortType());
        ShopReviewDisplaySetting setting = shopReviewDisplaySettingRepository.findByShopId(ShopId.of(shopId))
            .orElseGet(() -> ShopReviewDisplaySetting.of(ShopId.of(shopId), newSortType));
        setting.changeSortType(newSortType);

        shopReviewDisplaySettingRepository.save(setting);
    }
}
