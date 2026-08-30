package com.tastyhouse.webapplication.shop.port.in;

import java.util.List;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapplication.shop.response.ShopAmenityListItemResponse;
import com.tastyhouse.webapplication.shop.response.ShopBestListItemResponse;
import com.tastyhouse.webapplication.shop.response.ShopEditorChoiceResponse;
import com.tastyhouse.webapplication.shop.response.ShopFoodTypeListItemResponse;
import com.tastyhouse.webapplication.shop.response.ShopLatestListItemResponse;
import com.tastyhouse.webapplication.shop.response.ShopMapMarkerResponse;
import com.tastyhouse.webapplication.shop.response.ShopStationListItemResponse;

/**
 * 가게 목록·필터 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopSearchQueryUseCase {

    List<ShopMapMarkerResponse> searchMapMarkers(Double latitude, Double longitude);

    PaginationResponse<ShopBestListItemResponse> searchBestShops(Long memberId, int page, int size);

    PaginationResponse<ShopLatestListItemResponse> searchLatestShops(Long stationId, List<String> foodTypes, List<String> amenities, Long memberId, int page, int size);

    List<ShopEditorChoiceResponse> searchEditorChoices(int page, int size);

    List<ShopStationListItemResponse> searchAllStations();

    List<ShopFoodTypeListItemResponse> searchAllFoodTypes();

    List<ShopAmenityListItemResponse> searchAllAmenities();
}
