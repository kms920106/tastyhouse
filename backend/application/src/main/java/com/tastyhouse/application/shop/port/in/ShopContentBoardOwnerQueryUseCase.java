package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopContentBoardResult;

@CeoApp
public interface ShopContentBoardOwnerQueryUseCase {

    List<ShopContentBoardResult> getContentBoards(Long ceoId, Long shopId);
}
