package com.tastyhouse.application.ceo.port.out;

public record CeoListItemResult(
    Long id,
    String name,
    String businessRegistrationNumber,
    String status
) {
}
