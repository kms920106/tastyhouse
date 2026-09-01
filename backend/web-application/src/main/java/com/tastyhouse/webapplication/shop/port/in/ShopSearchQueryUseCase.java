package com.tastyhouse.webapplication.shop.port.in;

import java.util.List;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopMapMarkerResult;
import com.tastyhouse.application.shop.port.out.StationResult;
import com.tastyhouse.webapplication.shop.port.out.ShopBestListItemViewResult;
import com.tastyhouse.webapplication.shop.port.out.ShopLatestListItemViewResult;

/**
 * 가게 목록·필터 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 10</b> 이후 페이징 조회는 {@code PaginationResponse}가 아니라 {@code PageResult}를
 * 반환한다 — HTTP 래퍼 조립은 컨트롤러 몫이다.
 */
public interface ShopSearchQueryUseCase {

    List<ShopMapMarkerResult> searchMapMarkers(Double latitude, Double longitude);

    PageResult<ShopBestListItemViewResult> searchBestShops(Long memberId, int page, int size);

    PageResult<ShopLatestListItemViewResult> searchLatestShops(Long stationId, List<String> foodTypes, List<String> amenities, Long memberId, int page, int size);

    List<EditorChoiceResult> searchEditorChoices(int page, int size);

    List<StationResult> searchAllStations();

    List<ShopFoodTypeCategoryResult> searchAllFoodTypes();

    List<ShopAmenityCategoryResult> searchAllAmenities();
}
