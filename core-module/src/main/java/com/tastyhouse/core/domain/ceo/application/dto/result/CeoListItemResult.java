package com.tastyhouse.core.domain.ceo.application.dto.result;

import com.tastyhouse.core.domain.ceo.domain.model.Ceo;
import com.tastyhouse.core.domain.ceo.domain.model.CeoStatus;

public record CeoListItemResult(
    Long id,
    String name,
    String businessRegistrationNumber,
    CeoStatus status
) {

    public static CeoListItemResult from(Ceo ceo) {
        return new CeoListItemResult(
            ceo.getId(),
            ceo.getName(),
            ceo.getBusinessRegistrationNumber(),
            ceo.getStatus()
        );
    }
}
