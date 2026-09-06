package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.domain.shop.model.ShopImageType;

public interface ShopOwnerQueryPort {

    List<ShopContentBoardResult> findContentBoards(Long shopId);

    List<ShopImageChangeRequestResult> findImageChangeRequests(Long shopId, ShopImageType imageType);

    List<ShopSuspensionResult> findSuspensions(Long shopId);

    List<ShopTemporaryClosureResult> findTemporaryClosures(Long shopId);

    List<String> findFoodTypeCategoryNames(Long shopId);

    List<ShopMenuCollectionImageResult> findMenuCollectionImages(Long shopId);
}
