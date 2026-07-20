package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.core.domain.shop.domain.model.Station;

final class StationMapper {

    private StationMapper() {
    }

    static Station toDomain(StationJpaEntity entity) {
        return Station.reconstitute(
            entity.getId(),
            entity.getStationName()
        );
    }
}
