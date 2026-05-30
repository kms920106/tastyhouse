package com.tastyhouse.core.domain.shop.application;

import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.domain.shop.application.dto.result.BestShopItemDto;
import com.tastyhouse.core.domain.shop.application.dto.result.EditorChoiceDto;
import com.tastyhouse.core.domain.shop.application.dto.result.LatestShopItemDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityCategoryDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityWithCategoryDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBannerImageDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBookmarkedItemDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeCategoryDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopPhotoCategoryImageDto;
import com.tastyhouse.core.domain.shop.domain.model.Amenity;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopAmenity;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopOwnerMessageHistory;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategory;
import com.tastyhouse.core.domain.shop.domain.model.Station;
import com.tastyhouse.core.domain.shop.domain.repository.ShopBookmarkRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopChoiceRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.core.domain.shop.infrastructure.persistence.ShopJpaRepository;
import com.tastyhouse.core.domain.shop.infrastructure.persistence.StationJpaRepository;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopQueryService {

    private final ShopRepository shopRepository;
    private final ShopChoiceRepository shopChoiceRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopJpaRepository shopJpaRepository;
    private final StationJpaRepository stationJpaRepository;
    private final ShopBookmarkRepository shopBookmarkRepository;
    private final FileQueryService fileQueryService;

    public List<Shop> findNearbyShops(Double latitude, Double longitude) {
        BigDecimal lat = BigDecimal.valueOf(latitude);
        BigDecimal lon = BigDecimal.valueOf(longitude);
        return shopRepository.findNearbyShops(lat, lon);
    }

    public Page<BestShopItemDto> findBestShops(int page, int size) {
        return shopRepository.findBestShops(PageRequest.of(page, size));
    }

    public Page<LatestShopItemDto> findLatestShops(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, int page, int size) {
        return shopRepository.findLatestShops(stationId, foodTypes, amenities, PageRequest.of(page, size));
    }

    public Page<EditorChoiceDto> findEditorChoices(int page, int size) {
        return shopChoiceRepository.findEditorChoice(PageRequest.of(page, size));
    }

    public List<Station> findAllStations() {
        return shopDetailRepository.findAllStationsOrderByName();
    }

    public List<ShopFoodTypeCategoryDto> findAllFoodTypeCategories() {
        return shopDetailRepository.findAllActiveFoodTypeCategories();
    }

    public List<ShopAmenityCategoryDto> findAllAmenityCategories() {
        return shopDetailRepository.findAllActiveAmenityCategories();
    }

    public Shop findShopById(Long shopId) {
        return shopJpaRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    public Station findStationById(Long stationId) {
        return stationJpaRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.STATION_NOT_FOUND));
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

    public List<ShopAmenity> findShopAmenities(Long shopId) {
        return shopDetailRepository.findAmenitiesByShopId(shopId);
    }

    public List<ShopAmenityWithCategoryDto> findShopAmenitiesWithCategory(Long shopId) {
        return shopDetailRepository.findAmenitiesWithCategoryByShopId(shopId);
    }

    public List<ShopOrderMethod> findShopOrderMethods(Long shopId) {
        return shopDetailRepository.findOrderMethodsByShopId(shopId);
    }

    public List<ShopBannerImageDto> findShopBannerImages(Long shopId) {
        return shopDetailRepository.findBannerImagesByShopId(shopId);
    }

    public List<ShopPhotoCategory> findShopPhotoCategoriesByShopId(Long shopId) {
        return shopDetailRepository.findPhotoCategoriesByShopId(shopId);
    }

    public List<ShopPhotoCategoryImageDto> findAllShopPhotoCategoryImages() {
        return shopDetailRepository.findAllPhotoCategoryImages();
    }

    public boolean isBookmarked(Long shopId, Long memberId) {
        return shopBookmarkRepository.existsByShopIdAndMemberId(shopId, memberId);
    }

    public Optional<ShopOwnerMessageHistory> findLatestOwnerMessage(Long shopId) {
        return shopDetailRepository.findLatestOwnerMessageByShopId(shopId);
    }

    public Page<ShopBookmarkedItemDto> findMyBookmarkedShops(Long memberId, int page, int size) {
        return shopRepository.findMyBookmarkedShops(memberId, PageRequest.of(page, size));
    }

    public Optional<String> findThumbnailFilePath(Long thumbnailImageFileId) {
        return fileQueryService.findFilePath(thumbnailImageFileId);
    }
}
