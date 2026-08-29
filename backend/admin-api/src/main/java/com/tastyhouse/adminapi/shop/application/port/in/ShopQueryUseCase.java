package com.tastyhouse.adminapi.shop.application.port.in;

import java.util.List;

import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopAmenityCategoryResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopAmenityResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopBannerImageItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopChoiceDetailResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopChoiceListItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopClosedDayResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopDetailResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopFoodTypeCategoryResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopFoodTypeResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopListItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopOrderMethodItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopPhotoCategoryImageItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopPhotoCategoryResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.StationResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.TagResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.apicommon.shop.response.ShopBreakTimeResponse;
import com.tastyhouse.apicommon.shop.response.ShopBusinessHourResponse;

/**
 * 가게 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopQueryUseCase {

    List<StationResponse> getStations();

    PaginationResponse<ShopListItemResponse> getShops(
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    );

    ShopDetailResponse getShop(Long id);

    List<ShopBusinessHourResponse> getBusinessHours(Long id);

    List<ShopBreakTimeResponse> getBreakTimes(Long id);

    List<ShopClosedDayResponse> getClosedDays(Long id);

    List<ShopAmenityCategoryResponse> getAmenityCategories();

    List<ShopFoodTypeCategoryResponse> getFoodTypeCategories();

    List<ShopAmenityResponse> getShopAmenities(Long id);

    List<ShopFoodTypeResponse> getShopFoodTypes(Long id);

    List<TagResponse> getTags();

    List<ShopOrderMethodItemResponse> getOrderMethods(Long id);

    List<ShopBannerImageItemResponse> getBannerImages(Long id);

    List<ShopPhotoCategoryResponse> getPhotoCategories(Long id);

    List<ShopPhotoCategoryImageItemResponse> getPhotoCategoryImages(Long categoryId);

    PaginationResponse<ShopChoiceListItemResponse> getShopChoices(int page, int size);

    ShopChoiceDetailResponse getShopChoice(Long id);
}
