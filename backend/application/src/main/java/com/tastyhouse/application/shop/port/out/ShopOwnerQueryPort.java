package com.tastyhouse.application.shop.port.out;

import java.util.List;

public interface ShopOwnerQueryPort {

    List<ShopContentBoardResult> findContentBoards(Long shopId);

    List<ShopImageChangeRequestResult> findImageChangeRequests(Long shopId, String imageType);

    List<ShopSuspensionResult> findSuspensions(Long shopId);

    List<ShopTemporaryClosureResult> findTemporaryClosures(Long shopId);

    List<String> findFoodTypeCategoryNames(Long shopId);

    List<ShopMenuCollectionImageResult> findMenuCollectionImages(Long shopId);
}
