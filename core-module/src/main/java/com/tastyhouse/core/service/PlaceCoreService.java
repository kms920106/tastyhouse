package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.place.Amenity;
import com.tastyhouse.core.entity.place.FoodType;
import com.tastyhouse.core.entity.place.Place;
import com.tastyhouse.core.entity.place.PlaceAmenity;
import com.tastyhouse.core.entity.place.PlaceAmenityCategory;
import com.tastyhouse.core.entity.place.PlaceBannerImage;
import com.tastyhouse.core.entity.place.PlaceBookmark;
import com.tastyhouse.core.entity.place.PlaceFoodTypeCategory;
import com.tastyhouse.core.entity.place.PlaceBreakTime;
import com.tastyhouse.core.entity.place.PlaceBusinessHour;
import com.tastyhouse.core.entity.place.PlaceClosedDay;
import com.tastyhouse.core.entity.place.PlaceOrderMethod;
import com.tastyhouse.core.entity.place.PlaceOwnerMessageHistory;
import com.tastyhouse.core.entity.place.PlacePhotoCategory;
import com.tastyhouse.core.entity.place.PlacePhotoCategoryImage;
import com.tastyhouse.core.entity.place.PlaceStation;
import com.tastyhouse.core.entity.place.dto.BestPlaceItemDto;
import com.tastyhouse.core.entity.place.dto.EditorChoiceDto;
import com.tastyhouse.core.entity.place.dto.LatestPlaceItemDto;
import com.tastyhouse.core.entity.place.dto.MyBookmarkedPlaceItemDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.repository.place.PlaceAmenityCategoryJpaRepository;
import com.tastyhouse.core.repository.place.PlaceBookmarkJpaRepository;
import com.tastyhouse.core.repository.place.PlaceBookmarkRepository;
import com.tastyhouse.core.repository.place.PlaceChoiceRepository;
import com.tastyhouse.core.repository.place.PlaceDetailRepository;
import com.tastyhouse.core.repository.place.PlaceJpaRepository;
import com.tastyhouse.core.repository.place.PlaceRepository;
import com.tastyhouse.core.repository.place.PlaceStationJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceCoreService {

    private final PlaceRepository placeRepository;
    private final PlaceChoiceRepository placeChoiceRepository;
    private final PlaceDetailRepository placeDetailRepository;
    private final PlaceJpaRepository placeJpaRepository;
    private final PlaceStationJpaRepository placeStationJpaRepository;
    private final PlaceAmenityCategoryJpaRepository placeAmenityCategoryJpaRepository;
    private final PlaceBookmarkRepository placeBookmarkRepository;
    private final PlaceBookmarkJpaRepository placeBookmarkJpaRepository;

    @Transactional(readOnly = true)
    public List<Place> findNearbyPlaces(Double latitude, Double longitude) {
        BigDecimal lat = BigDecimal.valueOf(latitude);
        BigDecimal lon = BigDecimal.valueOf(longitude);
        return placeRepository.findNearbyPlaces(lat, lon);
    }

    @Transactional(readOnly = true)
    public Page<BestPlaceItemDto> findBestPlaces(int page, int size) {
        return placeRepository.findBestPlaces(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<LatestPlaceItemDto> findLatestPlaces(int page, int size, Long stationId, List<FoodType> foodTypes, List<Amenity> amenities) {
        return placeRepository.findLatestPlaces(PageRequest.of(page, size), stationId, foodTypes, amenities);
    }

    @Transactional(readOnly = true)
    public Page<EditorChoiceDto> findEditorChoices(int page, int size) {
        return placeChoiceRepository.findEditorChoice(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public List<PlaceStation> findAllStations() {
        return placeDetailRepository.findAllStationsOrderByName();
    }

    @Transactional(readOnly = true)
    public List<PlaceFoodTypeCategory> findAllFoodTypeCategories() {
        return placeDetailRepository.findAllActiveFoodTypeCategories();
    }

    @Transactional(readOnly = true)
    public List<PlaceAmenityCategory> findAllAmenityCategories() {
        return placeDetailRepository.findAllActiveAmenityCategories();
    }

    @Transactional(readOnly = true)
    public Place findPlaceById(Long placeId) {
        return placeJpaRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PLACE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PlaceStation findStationById(Long stationId) {
        return placeStationJpaRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PLACE_STATION_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PlaceAmenityCategory findPlaceAmenityCategoryById(Long categoryId) {
        return placeAmenityCategoryJpaRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PLACE_AMENITY_CATEGORY_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<PlaceBusinessHour> findPlaceBusinessHours(Long placeId) {
        return placeDetailRepository.findBusinessHoursByPlaceId(placeId);
    }

    @Transactional(readOnly = true)
    public List<PlaceBreakTime> findPlaceBreakTimes(Long placeId) {
        return placeDetailRepository.findBreakTimesByPlaceId(placeId);
    }

    @Transactional(readOnly = true)
    public List<PlaceClosedDay> findPlaceClosedDays(Long placeId) {
        return placeDetailRepository.findClosedDaysByPlaceId(placeId);
    }

    @Transactional(readOnly = true)
    public List<PlaceAmenity> findPlaceAmenities(Long placeId) {
        return placeDetailRepository.findAmenitiesByPlaceId(placeId);
    }

    @Transactional(readOnly = true)
    public List<PlaceOrderMethod> findPlaceOrderMethods(Long placeId) {
        return placeDetailRepository.findOrderMethodsByPlaceId(placeId);
    }

    @Transactional(readOnly = true)
    public List<PlaceBannerImage> findPlaceBannerImages(Long placeId) {
        return placeDetailRepository.findBannerImagesByPlaceId(placeId);
    }

    @Transactional(readOnly = true)
    public List<PlacePhotoCategory> findPlacePhotoCategoriesByPlaceId(Long placeId) {
        return placeDetailRepository.findPhotoCategoriesByPlaceId(placeId);
    }

    @Transactional(readOnly = true)
    public List<PlacePhotoCategoryImage> findAllPlacePhotoCategoryImages() {
        return placeDetailRepository.findAllPhotoCategoryImages();
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(Long placeId, Long memberId) {
        return placeBookmarkRepository.existsByPlaceIdAndMemberId(placeId, memberId);
    }

    @Transactional
    public boolean toggleBookmark(Long placeId, Long memberId) {
        if (placeBookmarkRepository.existsByPlaceIdAndMemberId(placeId, memberId)) {
            placeBookmarkRepository.deleteByPlaceIdAndMemberId(placeId, memberId);
            return false;
        } else {
            findPlaceById(placeId);
            placeBookmarkJpaRepository.save(new PlaceBookmark(placeId, memberId));
            return true;
        }
    }

    @Transactional(readOnly = true)
    public Optional<PlaceOwnerMessageHistory> findLatestOwnerMessage(Long placeId) {
        return placeDetailRepository.findLatestOwnerMessageByPlaceId(placeId);
    }

    @Transactional(readOnly = true)
    public Page<MyBookmarkedPlaceItemDto> findMyBookmarkedPlaces(Long memberId, int page, int size) {
        return placeRepository.findMyBookmarkedPlaces(memberId, PageRequest.of(page, size));
    }
}
