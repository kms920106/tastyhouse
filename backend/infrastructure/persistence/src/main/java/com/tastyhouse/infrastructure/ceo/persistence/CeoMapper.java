package com.tastyhouse.infrastructure.ceo.persistence;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.model.CeoStatus;
import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.infrastructure.shared.persistence.PhoneNumberEmbeddable;

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
            toPhoneNumber(entity.getPhoneNumber()),
            entity.getEmail(),
            entity.getStatus() == null ? null : CeoStatus.valueOf(entity.getStatus())
        );
    }

    static CeoJpaEntity toEntity(Ceo ceo) {
        return CeoJpaEntity.create(
            ceo.getUsername(),
            ceo.getPassword(),
            ceo.getName(),
            ceo.getBusinessRegistrationNumber(),
            toPhoneNumberEmbeddable(ceo.getPhoneNumber()),
            ceo.getEmail(),
            ceo.getStatus() == null ? null : ceo.getStatus().name()
        );
    }

    private static PhoneNumber toPhoneNumber(PhoneNumberEmbeddable embeddable) {
        String phoneNumber = embeddable == null ? null : embeddable.value();
        return phoneNumber == null ? null : new PhoneNumber(phoneNumber);
    }

    private static PhoneNumberEmbeddable toPhoneNumberEmbeddable(PhoneNumber phoneNumber) {
        String value = phoneNumber == null ? null : phoneNumber.value();
        return value == null ? null : new PhoneNumberEmbeddable(value);
    }
}
