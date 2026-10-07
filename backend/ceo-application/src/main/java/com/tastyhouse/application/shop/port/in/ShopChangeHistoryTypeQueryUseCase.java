package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopChangeCategoryResult;

public interface ShopChangeHistoryTypeQueryUseCase {

    List<ShopChangeCategoryResult> getChangeHistoryTypes();
}
