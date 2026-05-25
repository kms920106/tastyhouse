package com.tastyhouse.webapi.place.request;

import com.tastyhouse.core.domain.place.domain.model.Amenity;
import com.tastyhouse.core.domain.place.domain.model.FoodType;

import java.util.List;

public record LatestPlaceFilterRequest(
    Long stationId,
    List<FoodType> foodTypes,
    List<Amenity> amenities
) {
}
