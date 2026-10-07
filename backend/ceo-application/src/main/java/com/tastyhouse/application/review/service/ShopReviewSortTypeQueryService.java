package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.application.review.port.in.ShopReviewSortTypeQueryUseCase;
import com.tastyhouse.application.review.port.out.ShopReviewDisplaySettingOwnerQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewSortTypeResult;
import com.tastyhouse.application.review.port.out.ShopReviewSortTypeView;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ShopReviewSortTypeQueryService implements ShopReviewSortTypeQueryUseCase {

    private final ShopReviewDisplaySettingOwnerQueryPort shopReviewDisplaySettingOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopReviewSortTypeQueryService(
        ShopReviewDisplaySettingOwnerQueryPort shopReviewDisplaySettingOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopReviewDisplaySettingOwnerQueryPort = shopReviewDisplaySettingOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopReviewSortTypeView getSortType(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopReviewDisplaySettingOwnerQueryPort.findSortTypeSettingByShopId(shopId)
            .map(this::toSortTypeView)
            .orElseGet(() -> toSortTypeView(new ShopReviewSortTypeResult(ReviewSortType.LATEST.name(), null)));
    }

    private ShopReviewSortTypeView toSortTypeView(ShopReviewSortTypeResult result) {
        ReviewSortType sortType = ReviewSortType.valueOf(result.sortType());
        return new ShopReviewSortTypeView(sortType.name(), describeSortType(sortType), result.updatedAt());
    }

    private String describeSortType(ReviewSortType sortType) {
        return switch (sortType) {
            case RECOMMENDED -> "추천순";
            case LATEST -> "최신순";
            case OLDEST -> "등록순";
        };
    }
}
