package com.tastyhouse.webapi.shop.request;

import java.util.List;

import com.tastyhouse.core.domain.shop.domain.model.Amenity;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;

public record LatestShopFilterRequest(
    Long stationId,
    List<FoodType> foodTypes,
    List<Amenity> amenities
) {
}
