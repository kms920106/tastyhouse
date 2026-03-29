package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.place.*;
import com.tastyhouse.core.entity.place.dto.BestPlaceItemDto;
import com.tastyhouse.core.entity.place.dto.EditorChoiceDto;
import com.tastyhouse.core.entity.place.dto.LatestPlaceItemDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.repository.place.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
    private final PlaceImageCategoryJpaRepository placeImageCategoryJpaRepository;

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
    public List<EditorChoiceDto> findEditorChoices() {
        return placeChoiceRepository.findEditorChoice();
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
    public PlacePhotoCategory findPlaceImageCategoryById(Long categoryId) {
        return placeImageCategoryJpaRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PLACE_IMAGE_CATEGORY_NOT_FOUND));
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
    public List<PlacePhotoCategory> findAllPlacePhotoCategories() {
        return placeDetailRepository.findAllPhotoCategories();
    }

    @Transactional(readOnly = true)
    public List<PlacePhotoCategoryImage> findAllPlacePhotoCategoryImages() {
        return placeDetailRepository.findAllPhotoCategoryImages();
    }

    @Transactional(readOnly = true)
    public Page<PlacePhotoCategoryImage> findPlacePhotoCategoryImages(Long placePhotoCategoryId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        if (placePhotoCategoryId != null) {
            return placeDetailRepository.findPhotoCategoryImagesByCategoryId(placePhotoCategoryId, pageRequest);
        }
        return placeImageCategoryJpaRepository.findAll(pageRequest).map(c -> null); // fallback not needed
    }
}
