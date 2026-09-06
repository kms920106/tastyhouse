package com.tastyhouse.infrastructure.ceo.persistence;

import com.tastyhouse.domain.ceo.model.Ceo;

final class CeoMapper {
    private CeoMapper() {
    }

    static Ceo toDomain(CeoJpaEntity entity) {
        return Ceo.reconstitute(
            entity.getId(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getName(),
            entity.getBusinessRegistrationNumber(),
            entity.getPhoneNumber(),
            entity.getEmail(),
            entity.getStatus()
        );
    }

    static CeoJpaEntity toEntity(Ceo domain) {
        return CeoJpaEntity.create(
            domain.getUsername(),
            domain.getPassword(),
            domain.getName(),
            domain.getBusinessRegistrationNumber(),
            domain.getPhoneNumber(),
            domain.getEmail(),
            domain.getStatus()
        );
    }
}
