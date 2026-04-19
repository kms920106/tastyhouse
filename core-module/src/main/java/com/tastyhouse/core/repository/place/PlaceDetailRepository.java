package com.tastyhouse.core.repository.place;

import com.tastyhouse.core.entity.place.PlaceAmenity;
import com.tastyhouse.core.entity.place.PlaceBreakTime;
import com.tastyhouse.core.entity.place.PlaceBusinessHour;
import com.tastyhouse.core.entity.place.PlaceClosedDay;
import com.tastyhouse.core.entity.place.PlaceOrderMethod;
import com.tastyhouse.core.entity.place.PlaceOwnerMessageHistory;
import com.tastyhouse.core.entity.place.PlacePhotoCategory;
import com.tastyhouse.core.entity.place.PlaceStation;
import com.tastyhouse.core.entity.place.dto.PlaceAmenityCategoryDto;
import com.tastyhouse.core.entity.place.dto.PlaceAmenityWithCategoryDto;
import com.tastyhouse.core.entity.place.dto.PlaceBannerImageDto;
import com.tastyhouse.core.entity.place.dto.PlaceFoodTypeCategoryDto;
import com.tastyhouse.core.entity.place.dto.PlacePhotoCategoryImageDto;

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
