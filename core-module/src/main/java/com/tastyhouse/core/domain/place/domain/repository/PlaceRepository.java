package com.tastyhouse.core.domain.place.domain.repository;

import com.tastyhouse.core.domain.place.application.dto.result.BestPlaceItemDto;
import com.tastyhouse.core.domain.place.application.dto.result.LatestPlaceItemDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceBookmarkedItemDto;
import com.tastyhouse.core.domain.place.domain.model.Amenity;
import com.tastyhouse.core.domain.place.domain.model.FoodType;
import com.tastyhouse.core.domain.place.domain.model.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PlaceRepository {

    List<Place> findNearbyPlaces(BigDecimal latitude, BigDecimal longitude);

    Page<BestPlaceItemDto> findBestPlaces(Pageable pageable);

    Page<LatestPlaceItemDto> findLatestPlaces(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, Pageable pageable);

    Page<PlaceBookmarkedItemDto> findMyBookmarkedPlaces(Long memberId, Pageable pageable);

    Page<PlaceBookmarkedItemDto> searchByKeywordWithBookmark(String keyword, Long memberId, Pageable pageable);
}
