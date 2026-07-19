package com.tastyhouse.core.domain.shop.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.shop.domain.model.Amenity;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopAmenityCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopBannerImage;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopChoice;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.model.ShopFoodTypeCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopOwnerMessageHistory;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategoryImage;
import com.tastyhouse.core.domain.shop.domain.model.Station;
import com.tastyhouse.core.domain.shop.domain.model.Tag;
import com.tastyhouse.core.domain.shop.domain.repository.ShopBookmarkRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopChoiceRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.core.domain.shop.domain.repository.TagRepository;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.domain.shop.application.dto.ShopSearchCondition;
import com.tastyhouse.core.domain.shop.application.dto.result.BestShopItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.EditorChoiceResult;
import com.tastyhouse.core.domain.shop.application.dto.result.LatestShopItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityAssignmentResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityWithCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBannerImageResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBookmarkedItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeAssignmentResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopListItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopPhotoCategoryImageResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopQueryService {

    private final ShopRepository shopRepository;
    private final ShopChoiceRepository shopChoiceRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopBookmarkRepository shopBookmarkRepository;
    private final TagRepository tagRepository;
    private final FileQueryService fileQueryService;

    public List<Shop> findNearbyShops(Double latitude, Double longitude) {
        BigDecimal lat = BigDecimal.valueOf(latitude);
        BigDecimal lon = BigDecimal.valueOf(longitude);
        return shopRepository.findNearbyShops(lat, lon);
    }

    public PageResult<BestShopItemResult> findBestShops(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return shopRepository.findBestShops(pageQuery);
    }

    public PageResult<LatestShopItemResult> findLatestShops(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return shopRepository.findLatestShops(stationId, foodTypes, amenities, pageQuery);
    }

    public PageResult<EditorChoiceResult> findEditorChoices(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return shopChoiceRepository.findEditorChoice(pageQuery);
    }

    public List<Station> findAllStations() {
        return shopDetailRepository.findAllStationsOrderByName();
    }

    public List<ShopFoodTypeCategoryResult> findAllFoodTypeCategories() {
        return shopDetailRepository.findAllActiveFoodTypeCategories();
    }

    public List<ShopAmenityCategoryResult> findAllAmenityCategories() {
        return shopDetailRepository.findAllActiveAmenityCategories();
    }

    public Shop findShopById(ShopId shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    public PageResult<ShopListItemResult> findShops(ShopSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return shopRepository.findShops(condition, pageQuery);
    }

    public List<ShopBusinessHour> findShopBusinessHours(Long shopId) {
        return shopDetailRepository.findBusinessHoursByShopId(shopId);
    }

    public List<ShopBreakTime> findShopBreakTimes(Long shopId) {
        return shopDetailRepository.findBreakTimesByShopId(shopId);
    }

    public List<ShopClosedDay> findShopClosedDays(Long shopId) {
        return shopDetailRepository.findClosedDaysByShopId(shopId);
    }

    public List<ShopAmenityWithCategoryResult> findShopAmenitiesWithCategory(Long shopId) {
        return shopDetailRepository.findAmenitiesWithCategoryByShopId(shopId);
    }

    public List<ShopOrderMethod> findShopOrderMethods(Long shopId) {
        return shopDetailRepository.findOrderMethodsByShopId(shopId);
    }

    public List<ShopBannerImageResult> findShopBannerImages(Long shopId) {
        return shopDetailRepository.findBannerImagesByShopId(shopId);
    }

    public List<ShopPhotoCategory> findShopPhotoCategoriesByShopId(Long shopId) {
        return shopDetailRepository.findPhotoCategoriesByShopId(shopId);
    }

    public List<ShopPhotoCategoryImageResult> findAllShopPhotoCategoryImages() {
        return shopDetailRepository.findAllPhotoCategoryImages();
    }

    public boolean isBookmarked(Long shopId, MemberId memberId) {
        return shopBookmarkRepository.existsByShopIdAndMemberId(shopId, memberId);
    }

    public Optional<ShopOwnerMessageHistory> findLatestOwnerMessage(Long shopId) {
        return shopDetailRepository.findLatestOwnerMessageByShopId(shopId);
    }

    public PageResult<ShopBookmarkedItemResult> findMyBookmarkedShops(MemberId memberId, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return shopRepository.findMyBookmarkedShops(memberId, pageQuery);
    }

    public Optional<String> findThumbnailFilePath(Long thumbnailImageFileId) {
        if (thumbnailImageFileId == null) {
            return Optional.empty();
        }
        return fileQueryService.findFilePath(UploadedFileId.of(thumbnailImageFileId));
    }

    public List<ShopAmenityCategory> findAmenityCategories() {
        return shopDetailRepository.findAllAmenityCategories();
    }

    public List<ShopFoodTypeCategory> findFoodTypeCategories() {
        return shopDetailRepository.findAllFoodTypeCategories();
    }

    public List<ShopAmenityAssignmentResult> findShopAmenityAssignments(Long shopId) {
        return shopDetailRepository.findAmenityAssignmentsByShopId(shopId);
    }

    public List<ShopFoodTypeAssignmentResult> findShopFoodTypeAssignments(Long shopId) {
        return shopDetailRepository.findFoodTypeAssignmentsByShopId(shopId);
    }

    public List<Tag> findAllTags() {
        return tagRepository.findAllTags();
    }

    public List<ShopPhotoCategoryImage> findShopPhotoCategoryImages(Long categoryId) {
        return shopDetailRepository.findPhotoCategoryImagesByCategoryId(categoryId);
    }

    public List<ShopBannerImage> findShopBannerImageEntities(Long shopId) {
        return shopDetailRepository.findBannerImageEntitiesByShopId(shopId);
    }

    public ShopChoice findShopChoiceById(Long id) {
        return shopChoiceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_CHOICE_NOT_FOUND));
    }
}
