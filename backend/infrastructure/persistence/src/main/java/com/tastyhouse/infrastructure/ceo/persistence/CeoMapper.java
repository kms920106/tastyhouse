package com.tastyhouse.infrastructure.ceo.persistence;

import com.tastyhouse.application.ceo.port.out.write.CeoState;
import com.tastyhouse.infrastructure.shared.persistence.PhoneNumberEmbeddable;

final class CeoMapper {
    private CeoMapper() {
    }

    static CeoState toState(CeoJpaEntity entity) {
        return new CeoState(
            entity.getId(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getName(),
            entity.getBusinessRegistrationNumber(),
            entity.getPhoneNumber() == null ? null : entity.getPhoneNumber().value(),
            entity.getEmail(),
            entity.getStatus()
        );
    }

    static CeoJpaEntity toEntity(CeoState state) {
        return CeoJpaEntity.create(
            state.username(),
            state.password(),
            state.name(),
            state.businessRegistrationNumber(),
            state.phoneNumber() == null ? null : new PhoneNumberEmbeddable(state.phoneNumber()),
            state.email(),
            state.status()
        );
    }
}
