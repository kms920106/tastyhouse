package com.tastyhouse.core.domain.place.application;

import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.domain.place.application.dto.result.BestPlaceItemDto;
import com.tastyhouse.core.domain.place.application.dto.result.EditorChoiceDto;
import com.tastyhouse.core.domain.place.application.dto.result.LatestPlaceItemDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceAmenityCategoryDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceAmenityWithCategoryDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceBannerImageDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceBookmarkedItemDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceFoodTypeCategoryDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlacePhotoCategoryImageDto;
import com.tastyhouse.core.domain.place.domain.model.Amenity;
import com.tastyhouse.core.domain.place.domain.model.FoodType;
import com.tastyhouse.core.domain.place.domain.model.Place;
import com.tastyhouse.core.domain.place.domain.model.PlaceAmenity;
import com.tastyhouse.core.domain.place.domain.model.PlaceBreakTime;
import com.tastyhouse.core.domain.place.domain.model.PlaceBusinessHour;
import com.tastyhouse.core.domain.place.domain.model.PlaceClosedDay;
import com.tastyhouse.core.domain.place.domain.model.PlaceOrderMethod;
import com.tastyhouse.core.domain.place.domain.model.PlaceOwnerMessageHistory;
import com.tastyhouse.core.domain.place.domain.model.PlacePhotoCategory;
import com.tastyhouse.core.domain.place.domain.model.PlaceStation;
import com.tastyhouse.core.domain.place.domain.repository.PlaceBookmarkRepository;
import com.tastyhouse.core.domain.place.domain.repository.PlaceChoiceRepository;
import com.tastyhouse.core.domain.place.domain.repository.PlaceDetailRepository;
import com.tastyhouse.core.domain.place.domain.repository.PlaceRepository;
import com.tastyhouse.core.domain.place.infrastructure.persistence.PlaceJpaRepository;
import com.tastyhouse.core.domain.place.infrastructure.persistence.PlaceStationJpaRepository;
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
public class PlaceQueryService {

    private final PlaceRepository placeRepository;
    private final PlaceChoiceRepository placeChoiceRepository;
    private final PlaceDetailRepository placeDetailRepository;
    private final PlaceJpaRepository placeJpaRepository;
    private final PlaceStationJpaRepository placeStationJpaRepository;
    private final PlaceBookmarkRepository placeBookmarkRepository;
    private final FileQueryService fileQueryService;

    public List<Place> findNearbyPlaces(Double latitude, Double longitude) {
        BigDecimal lat = BigDecimal.valueOf(latitude);
        BigDecimal lon = BigDecimal.valueOf(longitude);
        return placeRepository.findNearbyPlaces(lat, lon);
    }

    public Page<BestPlaceItemDto> findBestPlaces(int page, int size) {
        return placeRepository.findBestPlaces(PageRequest.of(page, size));
    }

    public Page<LatestPlaceItemDto> findLatestPlaces(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, int page, int size) {
        return placeRepository.findLatestPlaces(stationId, foodTypes, amenities, PageRequest.of(page, size));
    }

    public Page<EditorChoiceDto> findEditorChoices(int page, int size) {
        return placeChoiceRepository.findEditorChoice(PageRequest.of(page, size));
    }

    public List<PlaceStation> findAllStations() {
        return placeDetailRepository.findAllStationsOrderByName();
    }

    public List<PlaceFoodTypeCategoryDto> findAllFoodTypeCategories() {
        return placeDetailRepository.findAllActiveFoodTypeCategories();
    }

    public List<PlaceAmenityCategoryDto> findAllAmenityCategories() {
        return placeDetailRepository.findAllActiveAmenityCategories();
    }

    public Place findPlaceById(Long placeId) {
        return placeJpaRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PLACE_NOT_FOUND));
    }

    public PlaceStation findStationById(Long stationId) {
        return placeStationJpaRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PLACE_STATION_NOT_FOUND));
    }

    public List<PlaceBusinessHour> findPlaceBusinessHours(Long placeId) {
        return placeDetailRepository.findBusinessHoursByPlaceId(placeId);
    }

    public List<PlaceBreakTime> findPlaceBreakTimes(Long placeId) {
        return placeDetailRepository.findBreakTimesByPlaceId(placeId);
    }

    public List<PlaceClosedDay> findPlaceClosedDays(Long placeId) {
        return placeDetailRepository.findClosedDaysByPlaceId(placeId);
    }

    public List<PlaceAmenity> findPlaceAmenities(Long placeId) {
        return placeDetailRepository.findAmenitiesByPlaceId(placeId);
    }

    public List<PlaceAmenityWithCategoryDto> findPlaceAmenitiesWithCategory(Long placeId) {
        return placeDetailRepository.findAmenitiesWithCategoryByPlaceId(placeId);
    }

    public List<PlaceOrderMethod> findPlaceOrderMethods(Long placeId) {
        return placeDetailRepository.findOrderMethodsByPlaceId(placeId);
    }

    public List<PlaceBannerImageDto> findPlaceBannerImages(Long placeId) {
        return placeDetailRepository.findBannerImagesByPlaceId(placeId);
    }

    public List<PlacePhotoCategory> findPlacePhotoCategoriesByPlaceId(Long placeId) {
        return placeDetailRepository.findPhotoCategoriesByPlaceId(placeId);
    }

    public List<PlacePhotoCategoryImageDto> findAllPlacePhotoCategoryImages() {
        return placeDetailRepository.findAllPhotoCategoryImages();
    }

    public boolean isBookmarked(Long placeId, Long memberId) {
        return placeBookmarkRepository.existsByPlaceIdAndMemberId(placeId, memberId);
    }

    public Optional<PlaceOwnerMessageHistory> findLatestOwnerMessage(Long placeId) {
        return placeDetailRepository.findLatestOwnerMessageByPlaceId(placeId);
    }

    public Page<PlaceBookmarkedItemDto> findMyBookmarkedPlaces(Long memberId, int page, int size) {
        return placeRepository.findMyBookmarkedPlaces(memberId, PageRequest.of(page, size));
    }

    public Optional<String> findThumbnailFilePath(Long thumbnailImageFileId) {
        return fileQueryService.findFilePath(thumbnailImageFileId);
    }
}
