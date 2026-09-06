package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopMapMarkerResult;
import com.tastyhouse.application.shop.port.out.StationResult;
import com.tastyhouse.application.shop.port.out.ShopBestListItemViewResult;
import com.tastyhouse.application.shop.port.out.ShopLatestListItemViewResult;

@WebApp
public interface ShopSearchQueryUseCase {

    List<ShopMapMarkerResult> searchMapMarkers(Double latitude, Double longitude);

    PageResult<ShopBestListItemViewResult> searchBestShops(Long memberId, int page, int size);

    PageResult<ShopLatestListItemViewResult> searchLatestShops(Long stationId, List<String> foodTypes, List<String> amenities, Long memberId, int page, int size);

    List<EditorChoiceResult> searchEditorChoices(int page, int size);

    List<StationResult> searchAllStations();

    List<ShopFoodTypeCategoryResult> searchAllFoodTypes();

    List<ShopAmenityCategoryResult> searchAllAmenities();
}
