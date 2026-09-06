package com.tastyhouse.application.ceo.port.out;

import com.tastyhouse.domain.ceo.model.CeoStatus;

public record CeoListItemResult(
    Long id,
    String name,
    String businessRegistrationNumber,
    CeoStatus status
) {
}
