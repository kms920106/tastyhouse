package com.tastyhouse.core.repository.place;

import com.tastyhouse.core.entity.place.Amenity;
import com.tastyhouse.core.entity.place.FoodType;
import com.tastyhouse.core.entity.place.Place;
import com.tastyhouse.core.entity.place.dto.BestPlaceItemDto;
import com.tastyhouse.core.entity.place.dto.LatestPlaceItemDto;
import com.tastyhouse.core.entity.place.dto.MyBookmarkedPlaceItemDto;
import com.tastyhouse.core.entity.place.dto.SearchPlaceItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PlaceRepository {

    List<Place> findNearbyPlaces(BigDecimal latitude, BigDecimal longitude);

    Page<BestPlaceItemDto> findBestPlaces(Pageable pageable);

    Page<LatestPlaceItemDto> findLatestPlaces(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, Pageable pageable);

    Page<MyBookmarkedPlaceItemDto> findMyBookmarkedPlaces(Long memberId, Pageable pageable);

    Page<BestPlaceItemDto> searchByKeyword(String keyword, Pageable pageable);

    Page<SearchPlaceItemDto> searchByKeywordWithBookmark(String keyword, Long memberId, Pageable pageable);
}
