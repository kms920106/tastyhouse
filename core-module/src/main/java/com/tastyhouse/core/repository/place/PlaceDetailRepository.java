package com.tastyhouse.core.repository.place;

import com.tastyhouse.core.entity.place.PlaceAmenity;
import com.tastyhouse.core.entity.place.PlaceAmenityCategory;
import com.tastyhouse.core.entity.place.PlaceBannerImage;
import com.tastyhouse.core.entity.place.PlaceBreakTime;
import com.tastyhouse.core.entity.place.PlaceBusinessHour;
import com.tastyhouse.core.entity.place.PlaceClosedDay;
import com.tastyhouse.core.entity.place.PlaceFoodTypeCategory;
import com.tastyhouse.core.entity.place.PlaceOrderMethod;
import com.tastyhouse.core.entity.place.PlaceOwnerMessageHistory;
import com.tastyhouse.core.entity.place.PlacePhotoCategory;
import com.tastyhouse.core.entity.place.PlacePhotoCategoryImage;
import com.tastyhouse.core.entity.place.PlaceStation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PlaceDetailRepository {

    List<PlaceStation> findAllStationsOrderByName();

    List<PlaceFoodTypeCategory> findAllActiveFoodTypeCategories();

    List<PlaceAmenityCategory> findAllActiveAmenityCategories();

    List<PlaceBusinessHour> findBusinessHoursByPlaceId(Long placeId);

    List<PlaceBreakTime> findBreakTimesByPlaceId(Long placeId);

    List<PlaceClosedDay> findClosedDaysByPlaceId(Long placeId);

    List<PlaceAmenity> findAmenitiesByPlaceId(Long placeId);

    List<PlaceOrderMethod> findOrderMethodsByPlaceId(Long placeId);

    List<PlaceBannerImage> findBannerImagesByPlaceId(Long placeId);

    Page<PlaceBannerImage> findBannerImagesByPlaceId(Long placeId, Pageable pageable);

    List<PlacePhotoCategory> findAllPhotoCategories();

    List<PlacePhotoCategoryImage> findAllPhotoCategoryImages();

    Page<PlacePhotoCategoryImage> findPhotoCategoryImagesByCategoryId(Long placePhotoCategoryId, Pageable pageable);

    Optional<PlaceOwnerMessageHistory> findLatestOwnerMessageByPlaceId(Long placeId);
}
