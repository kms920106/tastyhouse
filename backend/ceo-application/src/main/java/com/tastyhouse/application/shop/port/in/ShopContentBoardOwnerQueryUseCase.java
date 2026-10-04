package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopContentBoardResult;

public interface ShopContentBoardOwnerQueryUseCase {

    List<ShopContentBoardResult> getContentBoards(Long ceoId, Long shopId);
}
