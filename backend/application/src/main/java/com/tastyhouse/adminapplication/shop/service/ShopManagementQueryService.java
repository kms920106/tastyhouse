package com.tastyhouse.adminapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;
import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopChoiceDetailResult;
import com.tastyhouse.application.shop.port.out.ShopChoiceManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageManagementResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopManagementDetailResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopSearchCondition;
import com.tastyhouse.application.shop.port.out.ShopSearchManagementQueryPort;
import com.tastyhouse.application.shop.port.out.StationResult;
import com.tastyhouse.application.shop.port.out.TagResult;
import com.tastyhouse.adminapplication.shop.port.in.ShopManagementQueryUseCase;

/**
 * admin용 가게 관리 조회 서비스(CQRS query 측).
 *
 * <p>표현 목적 조회는 전부 읽기 포트에서 Result를 받아 그대로 넘긴다. 가게 단건 관리 상세도
 * 마찬가지라 write 포트를 주입하지 않는다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class ShopManagementQueryService implements ShopManagementQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopManagementQueryPort shopManagementQueryPort;
    private final ShopSearchManagementQueryPort shopSearchManagementQueryPort;
    private final ShopChoiceManagementQueryPort shopChoiceManagementQueryPort;

    public ShopManagementQueryService(
        ShopBasicInfoQueryPort shopBasicInfoQueryPort,
        ShopManagementQueryPort shopManagementQueryPort,
        ShopSearchManagementQueryPort shopSearchManagementQueryPort,
        ShopChoiceManagementQueryPort shopChoiceManagementQueryPort
    ) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopManagementQueryPort = shopManagementQueryPort;
        this.shopSearchManagementQueryPort = shopSearchManagementQueryPort;
        this.shopChoiceManagementQueryPort = shopChoiceManagementQueryPort;
    }

    @Override
    public List<StationResult> getStations() {
        return shopChoiceManagementQueryPort.findAllStations();
    }

    @Override
    public PageResult<ShopListItemResult> getShops(
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    ) {
        ShopSearchCondition condition = ShopSearchCondition.of(name, stationId, permanentlyClosed);
        return shopSearchManagementQueryPort.findShops(condition, PageQuery.of(page, size));
    }

    /**
     * 가게 상세와 썸네일 URL을 함께 조회한다. 썸네일은 다른 읽기 포트에 있어 조회가 두 번 필요하며,
     * 이미지가 없으면 URL은 null이다.
     */
    @Override
    public ShopDetail getShop(Long id) {
        ShopManagementDetailResult shop = shopManagementQueryPort.findManagementDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        String thumbnailImageUrl = shopBasicInfoQueryPort.findShopImageUrls(shop.id())
            .map(ShopImageUrlsResult::thumbnailImageUrl)
            .orElse(null);

        return new ShopDetail(shop, thumbnailImageUrl);
    }

    @Override
    public List<ShopBusinessHourResult> getBusinessHours(Long id) {
        return shopBasicInfoQueryPort.findBusinessHours(id);
    }

    @Override
    public List<ShopBreakTimeResult> getBreakTimes(Long id) {
        return shopBasicInfoQueryPort.findBreakTimes(id);
    }

    @Override
    public List<ShopClosedDayResult> getClosedDays(Long id) {
        return shopBasicInfoQueryPort.findClosedDays(id);
    }

    @Override
    public List<ShopAmenityCategoryResult> getAmenityCategories() {
        return shopManagementQueryPort.findAllAmenityCategories();
    }

    @Override
    public List<ShopFoodTypeCategoryResult> getFoodTypeCategories() {
        return shopManagementQueryPort.findAllFoodTypeCategories();
    }

    @Override
    public List<ShopAmenityAssignmentResult> getShopAmenities(Long id) {
        return shopBasicInfoQueryPort.findAmenityAssignments(id);
    }

    @Override
    public List<ShopFoodTypeAssignmentResult> getShopFoodTypes(Long id) {
        return shopManagementQueryPort.findFoodTypeAssignments(id);
    }

    @Override
    public List<TagResult> getTags() {
        return shopChoiceManagementQueryPort.findAllTags();
    }

    @Override
    public List<ShopOrderMethodResult> getOrderMethods(Long id) {
        return shopBasicInfoQueryPort.findOrderMethods(id);
    }

    @Override
    public List<ShopBannerImageResult> getBannerImages(Long id) {
        return shopBasicInfoQueryPort.findBannerImages(id);
    }

    @Override
    public List<ShopPhotoCategoryResult> getPhotoCategories(Long id) {
        return shopBasicInfoQueryPort.findPhotoCategories(id);
    }

    @Override
    public List<ShopPhotoCategoryImageManagementResult> getPhotoCategoryImages(Long categoryId) {
        return shopManagementQueryPort.findPhotoCategoryImages(categoryId);
    }

    @Override
    public PageResult<EditorChoiceResult> getShopChoices(int page, int size) {
        return shopChoiceManagementQueryPort.findEditorChoices(PageQuery.of(page, size));
    }

    @Override
    public ShopChoiceDetailResult getShopChoice(Long id) {
        return shopChoiceManagementQueryPort.findShopChoiceById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_CHOICE_NOT_FOUND));
    }

}
