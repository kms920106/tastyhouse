package com.tastyhouse.application.shop.port.out;

import java.util.List;

public interface ShopMediaOwnerQueryPort {

    List<ShopContentBoardResult> findContentBoards(Long shopId);

    List<ShopImageChangeRequestResult> findImageChangeRequests(Long shopId, String imageType);

    List<ShopMenuCollectionImageResult> findMenuCollectionImages(Long shopId);
}
