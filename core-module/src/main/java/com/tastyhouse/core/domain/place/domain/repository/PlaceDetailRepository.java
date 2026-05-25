package com.tastyhouse.core.domain.place.domain.repository;

import com.tastyhouse.core.domain.place.application.dto.result.PlaceAmenityCategoryDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceAmenityWithCategoryDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceBannerImageDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceFoodTypeCategoryDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlacePhotoCategoryImageDto;
import com.tastyhouse.core.domain.place.domain.model.PlaceAmenity;
import com.tastyhouse.core.domain.place.domain.model.PlaceBreakTime;
import com.tastyhouse.core.domain.place.domain.model.PlaceBusinessHour;
import com.tastyhouse.core.domain.place.domain.model.PlaceClosedDay;
import com.tastyhouse.core.domain.place.domain.model.PlaceOrderMethod;
import com.tastyhouse.core.domain.place.domain.model.PlaceOwnerMessageHistory;
import com.tastyhouse.core.domain.place.domain.model.PlacePhotoCategory;
import com.tastyhouse.core.domain.place.domain.model.PlaceStation;

import java.util.List;
import java.util.Optional;

public interface PlaceDetailRepository {

    List<PlaceStation> findAllStationsOrderByName();

    List<PlaceFoodTypeCategoryDto> findAllActiveFoodTypeCategories();

    List<PlaceAmenityCategoryDto> findAllActiveAmenityCategories();

    List<PlaceBusinessHour> findBusinessHoursByPlaceId(Long placeId);

    List<PlaceBreakTime> findBreakTimesByPlaceId(Long placeId);

    List<PlaceClosedDay> findClosedDaysByPlaceId(Long placeId);

    List<PlaceAmenity> findAmenitiesByPlaceId(Long placeId);

    List<PlaceAmenityWithCategoryDto> findAmenitiesWithCategoryByPlaceId(Long placeId);

    List<PlaceOrderMethod> findOrderMethodsByPlaceId(Long placeId);

    List<PlaceBannerImageDto> findBannerImagesByPlaceId(Long placeId);

    List<PlacePhotoCategory> findPhotoCategoriesByPlaceId(Long placeId);

    List<PlacePhotoCategoryImageDto> findAllPhotoCategoryImages();

    Optional<PlaceOwnerMessageHistory> findLatestOwnerMessageByPlaceId(Long placeId);
}
