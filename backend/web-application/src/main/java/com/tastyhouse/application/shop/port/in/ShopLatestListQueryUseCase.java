package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopLatestListItemViewResult;

public interface ShopLatestListQueryUseCase {

    PageResult<ShopLatestListItemViewResult> searchLatestShops(Long stationId, List<String> foodTypes, List<String> amenities, Long memberId, int page, int size);
}
