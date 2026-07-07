package com.tastyhouse.webapi.shop.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record ShopSearchRequest(
    Long stationId,

    @Schema(allowableValues = {"KOREAN", "JAPANESE", "WESTERN", "CHINESE", "WORLD", "SNACK", "BAR", "CAFE"})
    List<String> foodTypes,

    @Schema(allowableValues = {"PARKING", "RESTROOM", "RESERVATION", "BABY_CHAIR", "PET_FRIENDLY", "OUTLET", "TAKEOUT", "DELIVERY"})
    List<String> amenities
) {
}
