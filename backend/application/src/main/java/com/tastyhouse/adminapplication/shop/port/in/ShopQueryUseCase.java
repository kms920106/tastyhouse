package com.tastyhouse.adminapplication.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;
import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopChoiceDetailResult;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;
import com.tastyhouse.application.shop.port.out.ShopManagementDetailResult;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageManagementResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;
import com.tastyhouse.application.shop.port.out.StationResult;
import com.tastyhouse.application.shop.port.out.TagResult;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 가게 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 06</b> — 반환 타입은 Swagger를 아는 {@code *Response}가 아니라 프레임워크-프리
 * {@code *Result}다. Response 조립과 {@code PaginationResponse} 매핑은 컨트롤러가 담당한다.
 */
public interface ShopQueryUseCase {

    List<StationResult> getStations();

    PageResult<ShopListItemResult> getShops(
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    );

    ShopDetail getShop(Long id);

    List<ShopBusinessHourResult> getBusinessHours(Long id);

    List<ShopBreakTimeResult> getBreakTimes(Long id);

    List<ShopClosedDayResult> getClosedDays(Long id);

    List<ShopAmenityCategoryResult> getAmenityCategories();

    List<ShopFoodTypeCategoryResult> getFoodTypeCategories();

    List<ShopAmenityAssignmentResult> getShopAmenities(Long id);

    List<ShopFoodTypeAssignmentResult> getShopFoodTypes(Long id);

    List<TagResult> getTags();

    List<ShopOrderMethodResult> getOrderMethods(Long id);

    List<ShopBannerImageResult> getBannerImages(Long id);

    List<ShopPhotoCategoryResult> getPhotoCategories(Long id);

    List<ShopPhotoCategoryImageManagementResult> getPhotoCategoryImages(Long categoryId);

    PageResult<EditorChoiceResult> getShopChoices(int page, int size);

    ShopChoiceDetailResult getShopChoice(Long id);

    /**
     * 가게 관리 상세 조회 결과 — 가게 본문({@code shop})과 썸네일 URL({@code thumbnailImageUrl})의 묶음.
     *
     * <p>썸네일은 가게 상세와 다른 읽기 포트에 있어 단일 {@code *Result}로 오지 않는다. 컨트롤러가
     * 인자 두 개를 따로 받는 대신 이 묶음을 받아 Response를 조립한다. 이미지 미등록이면 URL은 null이다.
     */
    record ShopDetail(
        ShopManagementDetailResult shop,
        String thumbnailImageUrl
    ) {
    }
}
