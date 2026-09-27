package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopBestListItemViewResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopLatestListItemViewResult;
import com.tastyhouse.application.shop.port.out.ShopMapMarkerResult;
import com.tastyhouse.application.shop.port.out.StationResult;

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
